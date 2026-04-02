package com.lostwilderness.rpgcore.calendar;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * True when block Z is within the configured equatorial band for that world.
 */
public final class EquatorZone {

    private EquatorZone() {}

    public static boolean isInBand(World world, int blockZ, EquatorSettings settings) {
        if (settings == null || !settings.enabled()) {
            return false;
        }
        if (!settings.appliesToWorld(world)) {
            return false;
        }
        return Math.abs(blockZ) <= settings.halfWidthBlocks();
    }

    public static boolean isInBand(Player player, EquatorSettings settings) {
        if (player == null) {
            return false;
        }
        return isInBand(player.getWorld(), player.getLocation().getBlockZ(), settings);
    }
}
