package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarDayAdvancedEvent;
import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.personality.TraitService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

final class EventServiceImpl implements EventService, Listener, Runnable {

    private static final double RANDOM_EVENT_CHANCE = 0.6;

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final EventContext context;
    private final SchedulerService scheduler;
    private final Map<String, SeasonalEvent> registry = new LinkedHashMap<>();
    private final List<DailyWorldEvent> dailyEvents;
    private final EventCooldownTracker cooldownTracker;
    private final EventCascadeRegistry cascadeRegistry;
    private final EventBossBarManager bossBarManager;
    private final double tpsThreshold;

    private SeasonalEvent active;
    private int daysRemaining;
    private SeasonalEvent queuedTomorrow;
    private long queuedDayCount = -1;
    private boolean enabled = true;
    private volatile boolean running = false;

    EventServiceImpl(Plugin plugin, CalendarServiceV2 calendar, EventContext context,
                    SchedulerService scheduler, List<SeasonalEvent> events,
                    List<DailyWorldEvent> dailyEvents, EventCooldownTracker cooldownTracker,
                    EventCascadeRegistry cascadeRegistry, EventBossBarManager bossBarManager,
                    double tpsThreshold) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.context = context;
        this.scheduler = scheduler;
        this.dailyEvents = dailyEvents != null ? new ArrayList<>(dailyEvents) : new ArrayList<>();
        this.cooldownTracker = cooldownTracker != null ? cooldownTracker : new EventCooldownTracker();
        this.cascadeRegistry = cascadeRegistry != null ? cascadeRegistry : new EventCascadeRegistry();
        this.bossBarManager = bossBarManager != null ? bossBarManager : new EventBossBarManager(plugin);
        this.tpsThreshold = tpsThreshold > 0 ? tpsThreshold : 0;
        for (SeasonalEvent ev : events) {
            registry.put(ev.getId().toLowerCase(Locale.ROOT), ev);
        }
    }

    void register() {
        if (!enabled) return;
        running = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        updateTomorrowQueue(calendar.getCurrentSnapshot());
        scheduler.runSyncRepeating(this, 40L, 20L);
    }

    void unregister() {
        running = false;
        scheduler.runSync(() -> {
            for (DailyWorldEvent ev : dailyEvents) {
                if (ev.isActive()) ev.forceEndEarly();
            }
            stopActive("plugin_disable");
            clearQueue();
        });
        HandlerList.unregisterAll(this);
    }

    @Override
    public void run() {
        if (!running || !enabled) return;
        if (active != null) {
            CalendarServiceV2.CalendarSnapshot snapshot = calendar.getCurrentSnapshot();
            active.onTick(snapshot, context);
        }
        bossBarManager.tick();
    }

    @EventHandler
    public void onDayAdvanced(CalendarDayAdvancedEvent e) {
        if (!running || !enabled) return;
        CalendarServiceV2.CalendarSnapshot newSnapshot = e.getNewSnapshot();
        int day = (int) newSnapshot.dayCount();

        for (DailyWorldEvent ev : dailyEvents) {
            if (ev.isActive()) ev.forceEndEarly();
        }

        if (tpsThreshold > 0) {
            try {
                double tps = Bukkit.getTPS()[0];
                if (tps > 0 && tps < tpsThreshold) {
                    plugin.getLogger().warning("[Events] Skipping event dispatch for day " + day + " (TPS=" + tps + " < " + tpsThreshold + ")");
                    bossBarManager.updateFromEvents(dailyEvents, active);
                    runSeasonalDayLogic(newSnapshot);
                    return;
                }
            } catch (Throwable ignored) {}
        }

        // Dispatch once per overworld so Survival and Amplified (or other dimensions) can have events at different times
        for (World w : context.getOverworlds()) {
            if (!context.isWorldEnabled(w)) continue;
            context.setCurrentWorld(w);
            int effectiveDay = day + context.getWorldDayOffset(w);
            String worldName = w.getName();
            for (DailyWorldEvent ev : dailyEvents) {
                String key = ev.getClass().getSimpleName();
                if (key.endsWith("Event")) key = key.substring(0, key.length() - 5);
                int cooldownDays = plugin.getConfig().getInt("events.cooldowns." + key, 0);
                if (cooldownDays > 0 && !cooldownTracker.canRun(ev, worldName, effectiveDay)) continue;
                ev.onCalendarDay(effectiveDay, w);
                if (cooldownDays > 0 && ev.isActive()) {
                    cooldownTracker.setCooldown(ev, worldName, effectiveDay, cooldownDays);
                }
                for (DailyEventTrigger trigger : cascadeRegistry.getTriggers(ev)) {
                    trigger.maybeTrigger(effectiveDay, w);
                }
            }
        }
        context.setCurrentWorld(null);

        bossBarManager.updateFromEvents(dailyEvents, active);
        runSeasonalDayLogic(newSnapshot);
    }

    private void runSeasonalDayLogic(CalendarServiceV2.CalendarSnapshot newSnapshot) {
        if (active != null) {
            active.onDayTick(newSnapshot, context);
            daysRemaining--;
            if (daysRemaining <= 0) {
                stopActive("duration_ended");
            }
        }

        if (active == null) {
            if (queuedTomorrow != null && queuedDayCount == newSnapshot.dayCount()) {
                SeasonalEvent ev = queuedTomorrow;
                if (ev.isSeasonAllowed(newSnapshot.season()) && ev.canStartToday(newSnapshot, context)) {
                    int dur = randomBetween(ev.getMinDurationDays(), ev.getMaxDurationDays());
                    startEvent(ev, dur, newSnapshot, "queued_auto");
                }
                clearQueue();
            } else {
                tryStartNewEvent(newSnapshot);
            }
            updateTomorrowQueue(newSnapshot);
        }
    }

    List<DailyWorldEvent> getDailyEvents() {
        return dailyEvents;
    }

    private void stopActive(String reason) {
        if (active == null) return;
        CalendarServiceV2.CalendarSnapshot snapshot = calendar.getCurrentSnapshot();
        try {
            active.onEnd(snapshot, context);
        } catch (Throwable t) {
            plugin.getLogger().warning("[Events] Error ending event " + active.getId() + ": " + t.getMessage());
        }
        HandlerList.unregisterAll(active);
        plugin.getLogger().info("[Events] Event ended: " + active.getId() + " (" + reason + ")");
        active = null;
        daysRemaining = 0;
    }

    private void tryStartNewEvent(CalendarServiceV2.CalendarSnapshot snapshot) {
        List<SeasonalEvent> candidates = new ArrayList<>();
        for (SeasonalEvent ev : registry.values()) {
            if (!ev.isSeasonAllowed(snapshot.season()) || !ev.canStartToday(snapshot, context)) continue;
            candidates.add(ev);
        }
        if (candidates.isEmpty()) return;
        if (ThreadLocalRandom.current().nextDouble() > RANDOM_EVENT_CHANCE) return;
        SeasonalEvent chosen = candidates.size() == 1
            ? candidates.get(0)
            : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        int dur = randomBetween(chosen.getMinDurationDays(), chosen.getMaxDurationDays());
        startEvent(chosen, dur, snapshot, "auto");
    }

    private static int randomBetween(int min, int max) {
        if (max < min) return min;
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void startEvent(SeasonalEvent ev, int days, CalendarServiceV2.CalendarSnapshot snapshot, String reason) {
        if (active != null) stopActive("replaced_by_" + ev.getId());
        active = ev;
        daysRemaining = Math.max(1, days);
        Bukkit.getPluginManager().registerEvents(ev, plugin);
        try {
            ev.onStart(snapshot, context);
        } catch (Throwable t) {
            plugin.getLogger().warning("[Events] Error starting event " + ev.getId() + ": " + t.getMessage());
            stopActive("start_failed");
            return;
        }
        plugin.getLogger().info("[Events] Event started: " + ev.getId() + " for " + daysRemaining + " day(s) (" + reason + ")");
    }

    private void clearQueue() {
        queuedTomorrow = null;
        queuedDayCount = -1;
    }

    private void updateTomorrowQueue(CalendarServiceV2.CalendarSnapshot current) {
        if (active != null) {
            clearQueue();
            return;
        }
        long nextDayCount = current.dayCount() + 1;
        if (queuedTomorrow != null && queuedDayCount == nextDayCount) return;

        queuedDayCount = nextDayCount;
        CalendarServiceV2.CalendarSnapshot nextSnapshot = new CalendarServiceV2.CalendarSnapshot(
            current.date().plusDays(1), nextDayCount, seasonFor(current.date().plusDays(1)), current.mcDay());
        List<SeasonalEvent> candidates = new ArrayList<>();
        for (SeasonalEvent ev : registry.values()) {
            if (!ev.isSeasonAllowed(nextSnapshot.season()) || !ev.canStartToday(nextSnapshot, context)) continue;
            candidates.add(ev);
        }
        if (candidates.isEmpty()) {
            queuedTomorrow = null;
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() > RANDOM_EVENT_CHANCE) {
            queuedTomorrow = null;
            return;
        }
        queuedTomorrow = candidates.size() == 1
            ? candidates.get(0)
            : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    @Override
    public SeasonalEvent getActive() {
        return active;
    }

    @Override
    public int getDaysRemaining() {
        return daysRemaining;
    }

    @Override
    public SeasonalEvent getQueuedTomorrow() {
        return queuedTomorrow;
    }

    @Override
    public Set<String> getRegisteredEventIds() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    @Override
    public SeasonalEvent getEventById(String id) {
        if (id == null) return null;
        return registry.get(id.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean forceStart(String id, Integer durationOverride) {
        SeasonalEvent ev = getEventById(id);
        if (ev == null) return false;
        CalendarServiceV2.CalendarSnapshot snapshot = calendar.getCurrentSnapshot();
        int dur = (durationOverride != null && durationOverride > 0)
            ? durationOverride
            : randomBetween(ev.getMinDurationDays(), ev.getMaxDurationDays());
        startEvent(ev, dur, snapshot, "forced");
        clearQueue();
        return true;
    }

    @Override
    public void forceStop() {
        stopActive("forced_stop");
        clearQueue();
    }

    private static CalendarServiceV2.Season seasonFor(java.time.LocalDate date) {
        int month = date.getMonthValue();
        return switch (month) {
            case 3, 4, 5 -> CalendarServiceV2.Season.SPRING;
            case 6, 7, 8 -> CalendarServiceV2.Season.SUMMER;
            case 9, 10, 11 -> CalendarServiceV2.Season.AUTUMN;
            default -> CalendarServiceV2.Season.WINTER;
        };
    }

    /**
     * Check if party requirements are met for an event.
     * Counts how many players are in parties and checks against minimum.
     *
     * @param minPlayers Minimum number of players required to be in parties
     * @return true if requirement is met, false otherwise
     */
    public boolean meetsPartyRequirement(int minPlayers) {
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        if (core == null) return true; // No core = no requirement

        com.lostwilderness.rpgcore.party.PartyService partyService =
            core.getService(com.lostwilderness.rpgcore.party.PartyService.class);
        if (partyService == null) return true; // No party system = no requirement

        // Count players in parties
        Set<java.util.UUID> playersInParties = new java.util.HashSet<>();
        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (partyService.isInParty(player.getUniqueId())) {
                playersInParties.add(player.getUniqueId());
            }
        }

        return playersInParties.size() >= minPlayers;
    }

    /**
     * Apply a potion effect to all members of a player's party.
     * If player is not in a party, applies only to that player.
     *
     * @param playerUuid Player UUID
     * @param effectType Potion effect type
     * @param duration Duration in ticks
     * @param amplifier Amplifier (0 = level I, 1 = level II, etc.)
     */
    public void applyBuffToParty(java.util.UUID playerUuid, org.bukkit.potion.PotionEffectType effectType,
                                  int duration, int amplifier) {
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        com.lostwilderness.rpgcore.party.PartyService partyService = null;
        if (core != null) {
            partyService = core.getService(com.lostwilderness.rpgcore.party.PartyService.class);
        }

        Set<java.util.UUID> targets = new java.util.HashSet<>();
        if (partyService != null && partyService.isInParty(playerUuid)) {
            // Apply to entire party
            targets.addAll(partyService.getPartyMembers(playerUuid));
        } else {
            // Apply to individual player only
            targets.add(playerUuid);
        }

        // Apply effect to all targets
        org.bukkit.potion.PotionEffect effect =
            new org.bukkit.potion.PotionEffect(effectType, duration, amplifier);
        for (java.util.UUID targetId : targets) {
            org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(targetId);
            if (target != null && target.isOnline()) {
                target.addPotionEffect(effect);
            }
        }
    }

    /**
     * Calculate reward multiplier for a player based on post-game status.
     * Post-game players (300% completion, all 5 elemental temples) get +50% rewards.
     *
     * @param playerUuid Player UUID
     * @return Reward multiplier (1.0 for normal, 1.5 for post-game)
     */
    public double getRewardMultiplier(java.util.UUID playerUuid) {
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        if (core == null) return 1.0;

        TraitService traitService = core.getService(TraitService.class);
        if (traitService == null) return 1.0;

        return traitService.isPostGame(playerUuid) ? 1.5 : 1.0;
    }
}
