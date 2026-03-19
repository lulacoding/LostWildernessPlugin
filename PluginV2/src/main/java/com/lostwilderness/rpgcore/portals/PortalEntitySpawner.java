package com.lostwilderness.rpgcore.portals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * Spawns a transferred entity at the destination and mounts the player. Must be run on main thread.
 */
public final class PortalEntitySpawner {

    public static void spawnAndMount(Plugin plugin, Player player, Location loc, String entityData) {
        if (entityData == null || entityData.trim().isEmpty()) return;

        Map<String, String> map = PortalEntitySerializer.parse(entityData);
        String typeStr = map.get("type");
        if (typeStr == null) return;

        EntityType type;
        try {
            type = EntityType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("PortalEntitySpawner: unknown entity type " + typeStr);
            return;
        }

        World world = loc.getWorld();
        if (world == null) return;

        Entity spawned = world.spawnEntity(loc, type);
        if (spawned == null) return;

        try {
            applyData(spawned, map);
            try {
                spawned.addPassenger(player);
            } catch (Throwable ignored) {}
        } catch (Exception e) {
            plugin.getLogger().warning("PortalEntitySpawner: failed to apply data or mount: " + e.getMessage());
            spawned.remove();
        }
    }

    private static void applyData(Entity entity, Map<String, String> map) {
        if (entity instanceof LivingEntity living) {
            String health = map.get("health");
            if (health != null) {
                try {
                    living.setHealth(Math.min(Double.parseDouble(health), living.getMaxHealth()));
                } catch (NumberFormatException ignored) {}
            }
            String maxHealth = map.get("maxHealth");
            if (maxHealth != null) {
                try {
                    var attr = living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                    if (attr != null) attr.setBaseValue(Double.parseDouble(maxHealth));
                } catch (Exception ignored) {}
            }
            String name = map.get("name");
            if (name != null && !name.isEmpty()) {
                living.setCustomName(name);
                living.setCustomNameVisible(true);
            }
        }

        if (entity instanceof Tameable tameable) {
            String ownerStr = map.get("owner");
            if (ownerStr != null) {
                try {
                    UUID owner = UUID.fromString(ownerStr);
                    tameable.setTamed(true);
                    tameable.setOwner(org.bukkit.Bukkit.getOfflinePlayer(owner));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (entity instanceof AbstractHorse ah) {
            if (entity instanceof ChestedHorse ch) {
                String hasChestStr = map.get("hasChest");
                if (hasChestStr != null) {
                    try {
                        ch.setCarryingChest(Boolean.parseBoolean(hasChestStr));
                    } catch (Throwable ignored) {}
                }
            }
            String invStr = map.get("inv");
            if (invStr != null && !invStr.isEmpty()) {
                ItemStack[] items = decodeItems(invStr);
                if (items != null) {
                    try {
                        ah.getInventory().setContents(items);
                    } catch (Throwable ignored) {}
                }
            }
            String saddleStr = map.get("saddle");
            if (saddleStr != null) {
                try {
                    Material m = Material.valueOf(saddleStr);
                    if (m != null && m != Material.AIR) ah.getInventory().setSaddle(new ItemStack(m));
                } catch (IllegalArgumentException ignored) {}
            }
            String armorStr = map.get("armor");
            if (armorStr != null && entity instanceof Horse horse) {
                try {
                    Material m = Material.valueOf(armorStr);
                    if (m != null && m != Material.AIR) horse.getInventory().setArmor(new ItemStack(m));
                } catch (IllegalArgumentException ignored) {}
            }
            if (entity instanceof Horse horse) {
                String styleStr = map.get("style");
                if (styleStr != null) {
                    try {
                        horse.setStyle(Horse.Style.valueOf(styleStr));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        if (entity instanceof Boat boat) {
            String boatTypeStr = map.get("boatType");
            if (boatTypeStr != null) {
                try {
                    boat.setBoatType(Boat.Type.valueOf(boatTypeStr));
                } catch (IllegalArgumentException ignored) {}
                }
        }
    }

    private static ItemStack[] decodeItems(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 BukkitObjectInputStream ois = new BukkitObjectInputStream(bais)) {
                int len = ois.readInt();
                ItemStack[] items = new ItemStack[len];
                for (int i = 0; i < len; i++) {
                    items[i] = (ItemStack) ois.readObject();
                }
                return items;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
