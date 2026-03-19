package com.lostwilderness.rpgcore.boss;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;

import java.util.Objects;

/**
 * Immutable axis-aligned bounding box in a world.
 * Used for arena regions, world boundaries, and entity containment checks.
 */
public final class Boundary {

    private final World world;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    /**
     * Creates a boundary with normalized min/max (ensures min ≤ max per axis).
     */
    public Boundary(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = Objects.requireNonNull(world, "world");
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public World getWorld() {
        return world;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    /**
     * Returns true if the location is inside this boundary (same world, coords within range).
     */
    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;
        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX
                && loc.getBlockY() >= minY && loc.getBlockY() <= maxY
                && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }

    /**
     * Returns true if the entity's location is inside this boundary.
     */
    public boolean contains(Entity entity) {
        return entity != null && contains(entity.getLocation());
    }

    /**
     * Clamps the given location to the nearest point inside this boundary.
     * Returns null if the location's world is not this boundary's world.
     */
    public Location clamp(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        if (!loc.getWorld().equals(world)) return null;
        int x = Math.max(minX, Math.min(maxX, loc.getBlockX()));
        int y = Math.max(minY, Math.min(maxY, loc.getBlockY()));
        int z = Math.max(minZ, Math.min(maxZ, loc.getBlockZ()));
        return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
    }

    /**
     * Returns the center of the boundary (block center for X and Z).
     */
    public Location getCenter() {
        double cx = (minX + maxX) / 2.0 + 0.5;
        double cy = (minY + maxY) / 2.0;
        double cz = (minZ + maxZ) / 2.0 + 0.5;
        return new Location(world, cx, cy, cz);
    }

    /**
     * Spawns particles along the edges of this boundary so it is visible in-game.
     * Call from main thread. Uses spacing between particles (e.g. 2 = every 2 blocks).
     */
    public void showEdges(int spacing) {
        if (world == null || spacing < 1) return;
        int s = Math.max(1, spacing);
        double cx = 0.5;
        // Bottom face (minY): 4 edges
        for (int x = minX; x <= maxX; x += s)
            world.spawnParticle(Particle.ENCHANT, x + cx, minY, minZ + cx, 1, 0, 0, 0, 0);
        for (int x = minX; x <= maxX; x += s)
            world.spawnParticle(Particle.ENCHANT, x + cx, minY, maxZ + cx, 1, 0, 0, 0, 0);
        for (int z = minZ; z <= maxZ; z += s)
            world.spawnParticle(Particle.ENCHANT, minX + cx, minY, z + cx, 1, 0, 0, 0, 0);
        for (int z = minZ; z <= maxZ; z += s)
            world.spawnParticle(Particle.ENCHANT, maxX + cx, minY, z + cx, 1, 0, 0, 0, 0);
        // Top face (maxY)
        for (int x = minX; x <= maxX; x += s)
            world.spawnParticle(Particle.ENCHANT, x + cx, maxY, minZ + cx, 1, 0, 0, 0, 0);
        for (int x = minX; x <= maxX; x += s)
            world.spawnParticle(Particle.ENCHANT, x + cx, maxY, maxZ + cx, 1, 0, 0, 0, 0);
        for (int z = minZ; z <= maxZ; z += s)
            world.spawnParticle(Particle.ENCHANT, minX + cx, maxY, z + cx, 1, 0, 0, 0, 0);
        for (int z = minZ; z <= maxZ; z += s)
            world.spawnParticle(Particle.ENCHANT, maxX + cx, maxY, z + cx, 1, 0, 0, 0, 0);
        // Vertical edges
        for (int y = minY; y <= maxY; y += s) {
            world.spawnParticle(Particle.ENCHANT, minX + cx, y, minZ + cx, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.ENCHANT, minX + cx, y, maxZ + cx, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.ENCHANT, maxX + cx, y, minZ + cx, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.ENCHANT, maxX + cx, y, maxZ + cx, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Boundary boundary = (Boundary) o;
        return minX == boundary.minX && minY == boundary.minY && minZ == boundary.minZ
                && maxX == boundary.maxX && maxY == boundary.maxY && maxZ == boundary.maxZ
                && Objects.equals(world.getName(), boundary.world.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(world.getName(), minX, minY, minZ, maxX, maxY, maxZ);
    }
}
