package com.lostwilderness.rpgcore.progression.listener;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.personality.TraitService;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * On join: unlock first_join if first time; increment join counter; unlock join milestones (3,10,25,50,100);
 * optional season/New Year and clan milestones. Stores session start for playtime.
 * On quit: add session playtime to counter and unlock playtime milestones (1h, 5h, 24h, 100h).
 */
public final class ProgressionJoinListener implements Listener {

    private static final long ONE_HOUR_SECONDS = 3600L;
    private static final long FIVE_HOURS_SECONDS = 5 * ONE_HOUR_SECONDS;
    private static final long DAY_SECONDS = 24 * ONE_HOUR_SECONDS;
    private static final long HUNDRED_HOURS_SECONDS = 100 * ONE_HOUR_SECONDS;

    private final ProgressionService progressionService;
    private final SchedulerService scheduler;
    private final Plugin plugin;
    private final AuraSkillsBridge auraBridge;
    private final BetonQuestBridge betonBridge;
    private final CalendarServiceV2 calendarService;
    private final ClanService clanService;
    private final TraitService traitService;
    private final ConcurrentHashMap<UUID, Long> sessionStartMillis = new ConcurrentHashMap<>();

    public ProgressionJoinListener(ProgressionService progressionService, SchedulerService scheduler,
                                   Plugin plugin, AuraSkillsBridge auraBridge, BetonQuestBridge betonBridge,
                                   CalendarServiceV2 calendarService, ClanService clanService, TraitService traitService) {
        this.progressionService = progressionService;
        this.scheduler = scheduler;
        this.plugin = plugin;
        this.auraBridge = auraBridge;
        this.betonBridge = betonBridge;
        this.calendarService = calendarService;
        this.clanService = clanService;
        this.traitService = traitService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        sessionStartMillis.put(uuid, System.currentTimeMillis());
        if (auraBridge != null) {
            auraBridge.isAvailable();
        }
        scheduler.runAsync(() -> {
            try {
                if (!progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_FIRST_JOIN)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_FIRST_JOIN);
                    if (betonBridge != null) {
                        betonBridge.onMilestoneUnlocked(player, AchievementKey.MILESTONE_FIRST_JOIN);
                    }
                    checkTierAdvancement(uuid);
                }
                long joinCount = progressionService.incrementCounter(uuid, AchievementKey.COUNTER_JOIN_COUNT);
                unlockJoinMilestone(player, uuid, joinCount, 3, AchievementKey.MILESTONE_JOIN_3_TIMES);
                unlockJoinMilestone(player, uuid, joinCount, 10, AchievementKey.MILESTONE_JOIN_10_TIMES);
                unlockJoinMilestone(player, uuid, joinCount, 25, AchievementKey.MILESTONE_JOIN_25_TIMES);
                unlockJoinMilestone(player, uuid, joinCount, 50, AchievementKey.MILESTONE_JOIN_50_TIMES);
                unlockJoinMilestone(player, uuid, joinCount, 100, AchievementKey.MILESTONE_JOIN_100_TIMES);

                if (calendarService != null) {
                    var snap = calendarService.getCurrentSnapshot();
                    if (snap != null) {
                        var date = snap.date();
                        if (date.getMonthValue() == 3 && date.getDayOfMonth() == 1
                                && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_NEW_YEAR_LOGIN)) {
                            progressionService.unlock(uuid, AchievementKey.MILESTONE_NEW_YEAR_LOGIN);
                            checkTierAdvancement(uuid);
                        }
                        switch (snap.season()) {
                            case SPRING -> unlockSeason(player, uuid, AchievementKey.MILESTONE_FIRST_SEASON_SPRING);
                            case SUMMER -> unlockSeason(player, uuid, AchievementKey.MILESTONE_FIRST_SEASON_SUMMER);
                            case AUTUMN -> unlockSeason(player, uuid, AchievementKey.MILESTONE_FIRST_SEASON_AUTUMN);
                            case WINTER -> unlockSeason(player, uuid, AchievementKey.MILESTONE_FIRST_SEASON_WINTER);
                        }
                    }
                }
                if (clanService != null) {
                    if (clanService.getClanOfPlayer(uuid) != null
                            && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_JOINED_CLAN)) {
                        progressionService.unlock(uuid, AchievementKey.MILESTONE_JOINED_CLAN);
                        checkTierAdvancement(uuid);
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Progression join failed for " + uuid, t);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Long start = sessionStartMillis.remove(uuid);
        if (start == null) return;
        long sessionSeconds = Math.max(0L, (System.currentTimeMillis() - start) / 1000L);
        if (sessionSeconds <= 0) return;
        scheduler.runAsync(() -> {
            try {
                long total = progressionService.addToCounter(uuid, AchievementKey.COUNTER_PLAYTIME_SECONDS, sessionSeconds);
                boolean anyUnlocked = false;
                if (total >= ONE_HOUR_SECONDS && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_PLAYTIME_1H)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_PLAYTIME_1H);
                    anyUnlocked = true;
                }
                if (total >= FIVE_HOURS_SECONDS && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_PLAYTIME_5H)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_PLAYTIME_5H);
                    anyUnlocked = true;
                }
                if (total >= DAY_SECONDS && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_PLAYTIME_24H)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_PLAYTIME_24H);
                    anyUnlocked = true;
                }
                if (total >= HUNDRED_HOURS_SECONDS && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_PLAYTIME_100H)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_PLAYTIME_100H);
                    anyUnlocked = true;
                }
                if (anyUnlocked) {
                    checkTierAdvancement(uuid);
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Progression quit/playtime failed for " + uuid, t);
            }
        });
    }

    private void unlockJoinMilestone(Player player, UUID uuid, long joinCount, int threshold, String milestoneKey) {
        if (joinCount >= threshold && !progressionService.hasUnlocked(uuid, milestoneKey)) {
            progressionService.unlock(uuid, milestoneKey);
            if (betonBridge != null) {
                betonBridge.onMilestoneUnlocked(player, milestoneKey);
            }
            checkTierAdvancement(uuid);
        }
    }

    private void unlockSeason(Player player, UUID uuid, String milestoneKey) {
        if (!progressionService.hasUnlocked(uuid, milestoneKey)) {
            progressionService.unlock(uuid, milestoneKey);
            checkTierAdvancement(uuid);
        }
    }

    /**
     * Check if player should advance to next trait tier after milestone unlock.
     * Called asynchronously after any milestone unlock.
     */
    private void checkTierAdvancement(UUID uuid) {
        if (traitService != null) {
            traitService.checkAndAdvanceTier(uuid);
        }
    }
}
