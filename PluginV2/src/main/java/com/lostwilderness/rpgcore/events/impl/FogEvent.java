package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fog event: chance per day, broadcast message; optional view-distance effect if ProtocolLib present.
 */
public final class FogEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public FogEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        int chancePct = plugin.getConfig().getInt("events.fog.chance-pct", 20);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        Bukkit.broadcastMessage("§7🕸 A rolling fog envelops the land… 🕸");
        active = true;
        long restoreDelay = plugin.getConfig().getLong("events.fog.restore-delay-ticks", 6000L);
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§aThe fog lifts, and clarity returns.");
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, restoreDelay);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (!active) return;
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Fog"; }
}
