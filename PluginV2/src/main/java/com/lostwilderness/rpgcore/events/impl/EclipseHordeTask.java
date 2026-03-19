package com.lostwilderness.rpgcore.events.impl;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns hostile mobs near overworld players during Eclipse. Runs every 6000 ticks (~5 min).
 */
final class EclipseHordeTask extends BukkitRunnable {

    private static final int REGION_SIZE = 80;
    private static final EntityType[] HOSTILES = {
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER
    };

    private final World world;
    private final int hordeSize;

    EclipseHordeTask(World world, int hordeSize) {
        this.world = world;
        this.hordeSize = Math.max(1, Math.min(hordeSize, 32));
    }

    @Override
    public void run() {
        List<Location> centers = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getWorld() != world) continue;
            centers.add(p.getLocation());
        }
        if (centers.isEmpty()) return;

        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < hordeSize; i++) {
            Location center = centers.get(i % centers.size());
            double x = center.getX() + (r.nextDouble() * REGION_SIZE - REGION_SIZE / 2);
            double z = center.getZ() + (r.nextDouble() * REGION_SIZE - REGION_SIZE / 2);
            int y = world.getHighestBlockYAt((int) x, (int) z);
            Location spawnLoc = new Location(world, x + 0.5, y, z + 0.5);
            EntityType type = HOSTILES[r.nextInt(HOSTILES.length)];
            try {
                world.spawnEntity(spawnLoc, type);
            } catch (Exception ignored) {}
        }
    }
}
