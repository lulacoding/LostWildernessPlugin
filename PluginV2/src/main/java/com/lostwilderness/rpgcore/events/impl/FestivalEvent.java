package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Festival: broadcast message, PDC-mark villagers/traders as Festival Merchant, lasts one MC day. Config from lw-events-extra.
 */
public final class FestivalEvent implements DailyWorldEvent, Listener {

    private static final int MARK_RADIUS = 48;
    private static final String FESTIVAL_NAME = "§6Festival Merchant";

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private final NamespacedKey keyFestival;
    private final NamespacedKey keyOriginalName;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public FestivalEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
        this.keyFestival = new NamespacedKey(plugin, "festival");
        this.keyOriginalName = new NamespacedKey(plugin, "festival_original_name");
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("festival.enabled", true);
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("festival.base_chance_per_day", 0.10);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§6§lA festival is in town! Traders have special offers.");
        }
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                endTask = null;
                clearFestivalMarks();
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!active) return;
        if (e.getEntity() instanceof Villager || e.getEntity() instanceof WanderingTrader) {
            if (e.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) return;
            for (Player p : e.getEntity().getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(e.getLocation()) <= MARK_RADIUS * MARK_RADIUS) {
                    markAsFestivalMerchant(e.getEntity());
                    break;
                }
            }
        }
    }

    private void markAsFestivalMerchant(org.bukkit.entity.LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (pdc.has(keyFestival, PersistentDataType.BYTE)) return;
        String orig = entity.getCustomName();
        pdc.set(keyFestival, PersistentDataType.BYTE, (byte) 1);
        if (orig != null && !orig.isEmpty()) {
            pdc.set(keyOriginalName, PersistentDataType.STRING, orig);
        }
        entity.setCustomName(FESTIVAL_NAME);
        entity.setCustomNameVisible(true);
    }

    void clearFestivalMarks() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (var entity : w.getLivingEntities()) {
                PersistentDataContainer pdc = entity.getPersistentDataContainer();
                if (!pdc.has(keyFestival, PersistentDataType.BYTE)) continue;
                pdc.remove(keyFestival);
                String orig = pdc.get(keyOriginalName, PersistentDataType.STRING);
                pdc.remove(keyOriginalName);
                entity.setCustomName(orig != null && !orig.isEmpty() ? orig : null);
                entity.setCustomNameVisible(false);
            }
        }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
        clearFestivalMarks();
    }

    @Override
    public String getDisplayName() { return "Festival"; }
}
