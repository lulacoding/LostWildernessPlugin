package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.calendar.EquatorZone;
import com.lostwilderness.rpgcore.events.BlockRestoreManager;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Blizzard: chance per day in winter, freezes surface water, spawns strays, storm weather; ends at sunset.
 */
public final class BlizzardEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final BlockRestoreManager blockRestore;
    private final EquatorSettings equator;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public BlizzardEvent(Plugin plugin, BlockRestoreManager blockRestore, EquatorSettings equator) {
        this.plugin = plugin;
        this.blockRestore = blockRestore != null ? blockRestore : new BlockRestoreManager(plugin);
        this.equator = equator != null ? equator : EquatorSettings.disabled();
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        int chancePct = plugin.getConfig().getInt("events.blizzard.chance-pct", 25);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        active = true;
        final World w = world;
        for (Player p : w.getPlayers()) {
            p.sendMessage("§f❄ A vicious winter blizzard howls… Only snowy biomes are affected! ❄");
        }
        w.setStorm(true);
        w.setThundering(false);
        for (org.bukkit.Chunk chunk : w.getLoadedChunks()) {
            int x = (chunk.getX() << 4) + ThreadLocalRandom.current().nextInt(16);
            int z = (chunk.getZ() << 4) + ThreadLocalRandom.current().nextInt(16);
            int y = w.getHighestBlockYAt(x, z);
            try {
                if (!equator.gateColdEvents() || !EquatorZone.isInBand(w, z, equator)) {
                    w.spawn(new Location(w, x + 0.5, y, z + 0.5), Stray.class);
                }
            } catch (Exception ignored) {}
            int bx = chunk.getX() << 4, bz = chunk.getZ() << 4;
            for (int xx = bx; xx < bx + 16; xx++) {
                for (int zz = bz; zz < bz + 16; zz++) {
                    if (equator.gateColdEvents() && EquatorZone.isInBand(w, zz, equator)) {
                        continue;
                    }
                    int yy = w.getHighestBlockYAt(xx, zz);
                    Block b = w.getBlockAt(xx, yy, zz);
                    if (b.getType() == Material.WATER) {
                        blockRestore.record("blizzard", b);
                        b.setType(Material.ICE, false);
                    }
                }
            }
        }
        long now = w.getTime();
        long untilSunset = (13000 - now + 24000) % 24000;
        if (untilSunset == 0) untilSunset = 24000;
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                blockRestore.restoreAndClear("blizzard");
                w.setStorm(false);
                for (Player p : w.getPlayers()) {
                    p.sendMessage("§b❄ The blizzard subsides and the ice melts away.");
                }
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, untilSunset);
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
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        blockRestore.restoreAndClear("blizzard");
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                w.setStorm(false);
            }
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Blizzard"; }
}
