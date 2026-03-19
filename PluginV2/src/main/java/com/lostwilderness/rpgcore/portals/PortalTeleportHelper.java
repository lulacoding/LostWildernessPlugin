package com.lostwilderness.rpgcore.portals;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Teleports player and their vehicle (e.g. horse, boat) together.
 */
public final class PortalTeleportHelper {

    public static void teleportPlayerAndVehicle(Player player, Location dest) {
        if (player == null || dest == null) return;
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            Entity root = getRootVehicle(vehicle);
            root.teleport(dest);
        } else {
            player.teleport(dest);
        }
    }

    private static Entity getRootVehicle(Entity entity) {
        Entity current = entity;
        while (current.getVehicle() != null) {
            current = current.getVehicle();
        }
        return current;
    }
}
