package com.lostwilderness.rpgcore.boss;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Places and restores barrier blocks to form visible arena walls (and floor)
 * for a Boundary. Used when Devoider or El Diablo spawn so boss and players stay inside.
 */
public final class BossArenaBarriers {

    private BossArenaBarriers() {}

    /**
     * Places barrier blocks on the 4 vertical walls and the floor (minY layer) of the boundary.
     * Returns a list of block states that can be passed to {@link #restore(java.util.List)} to undo.
     * Must be called from the main thread.
     */
    public static List<BlockState> placeWallsAndFloor(Boundary boundary) {
        World world = boundary.getWorld();
        int minX = boundary.getMinX();
        int maxX = boundary.getMaxX();
        int minY = boundary.getMinY();
        int maxY = boundary.getMaxY();
        int minZ = boundary.getMinZ();
        int maxZ = boundary.getMaxZ();

        List<BlockState> previous = new ArrayList<>();

        // Floor (entire minY layer)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                storeAndSet(world.getBlockAt(x, minY, z), previous);
            }
        }

        // 4 walls
        for (int y = minY + 1; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                storeAndSet(world.getBlockAt(minX, y, z), previous);
                storeAndSet(world.getBlockAt(maxX, y, z), previous);
            }
            for (int x = minX + 1; x < maxX; x++) {
                storeAndSet(world.getBlockAt(x, y, minZ), previous);
                storeAndSet(world.getBlockAt(x, y, maxZ), previous);
            }
        }

        return previous;
    }

    private static void storeAndSet(Block block, List<BlockState> previous) {
        previous.add(block.getState());
        block.setType(Material.BARRIER);
    }

    /**
     * Restores all blocks to their previous state. Must be called from the main thread.
     */
    public static void restore(List<BlockState> previous) {
        if (previous == null) return;
        for (BlockState state : previous) {
            try {
                state.update(true, false);
            } catch (Exception ignored) { }
        }
    }
}
