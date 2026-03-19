package com.lostwilderness.rpgcore.boss;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * Generates a Nether bedrock floating island (Diablo's Lair) by filling
 * a Boundary's floor layer with bedrock.
 */
public final class DiabloLairGenerator {

    /**
     * Fills the bottom layer of the boundary (minY) with bedrock to form a floating platform.
     * Does not fill walls; the platform is a single horizontal layer.
     *
     * @param boundary region to fill (only the y=minY plane is filled)
     * @return number of blocks set
     */
    public static int generateFloor(Boundary boundary) {
        World world = boundary.getWorld();
        if (world == null) return 0;
        int y = boundary.getMinY();
        int count = 0;
        for (int x = boundary.getMinX(); x <= boundary.getMaxX(); x++) {
            for (int z = boundary.getMinZ(); z <= boundary.getMaxZ(); z++) {
                if (world.getBlockAt(x, y, z).getType() != Material.BEDROCK) {
                    world.getBlockAt(x, y, z).setType(Material.BEDROCK, false);
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Generates a floating platform at the given center with the given radius in X and Z.
     * The floor is one block thick at centerY.
     *
     * @param world    world to generate in (e.g. Nether)
     * @param centerX  center X
     * @param centerY  Y level of the floor
     * @param centerZ  center Z
     * @param radiusXZ half-size in X and Z (platform is 2*radiusXZ+1 on each side)
     * @return the boundary of the generated platform (floor only)
     */
    public static Boundary generatePlatform(World world, int centerX, int centerY, int centerZ, int radiusXZ) {
        Boundary boundary = new Boundary(world,
                centerX - radiusXZ, centerY, centerZ - radiusXZ,
                centerX + radiusXZ, centerY, centerZ + radiusXZ);
        generateFloor(boundary);
        return boundary;
    }
}
