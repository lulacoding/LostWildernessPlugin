package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seasonal storm: autumn/winter months, chance per day; storm + lightning strikes; ends at 6 AM.
 */
public final class SeasonalStormEvent implements DailyWorldEvent {

    private static final int CLUSTER_SIZE = 100;
    private static final long LIGHTNING_PERIOD = 20L * 60 * 3;
    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;
    private BukkitRunnable lightningTask;
    private BukkitRunnable endTask;

    public SeasonalStormEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        int month = calendar.getCurrentSnapshot().date().getMonthValue();
        boolean isLateAutumnOrWinter = (month >= 10 || month <= 2);
        if (!isLateAutumnOrWinter) return;
        int chancePct = plugin.getConfig().getInt("events.seasonal-storm.chance-pct", 25);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        active = true;
        final World w = world;
        Bukkit.broadcastMessage("§7🌧 A seasonal storm rolls in… 🌧");
        w.setStorm(true);
        w.setThundering(true);
        doClusteredLightning(w);
        lightningTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); lightningTask = null; return; }
                doClusteredLightning(w);
            }
        };
        lightningTask.runTaskTimer(plugin, LIGHTNING_PERIOD, LIGHTNING_PERIOD);
        long untilSixAM = (6000 - w.getTime() + 24000) % 24000;
        if (untilSixAM == 0) untilSixAM = 24000;
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                w.setStorm(false);
                w.setThundering(false);
                if (lightningTask != null) { lightningTask.cancel(); lightningTask = null; }
                active = false;
                endTask = null;
                Bukkit.broadcastMessage("§aThe seasonal storm subsides…");
            }
        };
        endTask.runTaskLater(plugin, untilSixAM);
    }

    private void doClusteredLightning(World world) {
        Map<String, Location> clusters = new HashMap<>();
        for (Player p : world.getPlayers()) {
            Location loc = p.getLocation();
            int cx = (int) Math.floor(loc.getX() / CLUSTER_SIZE);
            int cz = (int) Math.floor(loc.getZ() / CLUSTER_SIZE);
            clusters.putIfAbsent(cx + "_" + cz, loc);
        }
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (Location center : clusters.values()) {
            boolean struck = false;
            for (int i = 0; i < 10; i++) {
                double dx = r.nextDouble() * CLUSTER_SIZE - CLUSTER_SIZE / 2.0;
                double dz = r.nextDouble() * CLUSTER_SIZE - CLUSTER_SIZE / 2.0;
                int x = center.getBlockX() + (int) dx, z = center.getBlockZ() + (int) dz;
                int y = world.getHighestBlockYAt(x, z);
                Block b = world.getBlockAt(x, y, z);
                String name = b.getType().name();
                if (name.endsWith("_LOG") || name.endsWith("_LEAVES")) {
                    world.strikeLightning(b.getLocation());
                    Block above = world.getBlockAt(x, y + 1, z);
                    if (above.getType() == Material.AIR) above.setType(Material.FIRE, false);
                    struck = true;
                    break;
                }
            }
            if (!struck) {
                world.strikeLightning(center);
            }
        }
    }

    private static World overworld() {
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (!active) return;
        if (endTask != null) { endTask.cancel(); endTask = null; }
        if (lightningTask != null) { lightningTask.cancel(); lightningTask = null; }
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                w.setStorm(false);
                w.setThundering(false);
            }
        }
        Bukkit.broadcastMessage("§aThe seasonal storm subsides…");
        active = false;
    }

    @Override
    public String getDisplayName() { return "Seasonal Storm"; }
}
