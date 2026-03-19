package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fishing Festival: when active, treasure chance and double catch. Config from lw-events-extra.
 */
public final class FishingFestivalEvent implements DailyWorldEvent, Listener {

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public FishingFestivalEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("fishing.enabled", true);
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("fishing.base_chance_per_day", 0.10);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§bFishing Festival! Better catches and treasure today.");
        }
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (!active || e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        double treasure = lwConfigs.getEventsExtra().getDouble("fishing.treasure_chance", 0.35);
        double doubleCatch = lwConfigs.getEventsExtra().getDouble("fishing.double_catch_chance", 0.30);
        if (ThreadLocalRandom.current().nextDouble() < doubleCatch) {
            e.getPlayer().sendMessage("§bDouble catch! (Fishing Festival)");
        }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Fishing Festival"; }
}
