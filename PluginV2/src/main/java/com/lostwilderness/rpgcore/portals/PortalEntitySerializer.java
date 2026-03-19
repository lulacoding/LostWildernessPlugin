package com.lostwilderness.rpgcore.portals;

import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializes the player's current vehicle for cross-server transfer.
 * Format: key=value;key2=value2. Must be called on main thread.
 */
public final class PortalEntitySerializer {

    private static final String SEP = ";";
    private static final String KV = "=";

    public static String serialize(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle == null) return "";

        Entity root = getRootVehicle(vehicle);
        EntityType type = root.getType();
        if (!isTransferable(root, type)) return "";

        Map<String, String> map = new LinkedHashMap<>();
        map.put("type", type.name());

        if (root instanceof LivingEntity living) {
            map.put("health", String.valueOf(Math.max(0.01, living.getHealth())));
            map.put("maxHealth", String.valueOf(living.getMaxHealth()));
            if (living.getCustomName() != null) {
                map.put("name", escape(living.getCustomName()));
            }
        }

        if (root instanceof Tameable tameable && tameable.isTamed() && tameable.getOwner() != null) {
            map.put("owner", tameable.getOwner().getUniqueId().toString());
        }

        if (root instanceof AbstractHorse ah) {
            try {
                ItemStack saddle = ah.getInventory().getSaddle();
                if (saddle != null && !saddle.getType().isAir()) map.put("saddle", saddle.getType().name());
            } catch (Throwable ignored) {}
            if (root instanceof ChestedHorse ch) {
                map.put("hasChest", String.valueOf(ch.isCarryingChest()));
            }
            try {
                ItemStack[] contents = ah.getInventory().getContents();
                String inv = encodeItems(contents);
                if (!inv.isEmpty()) map.put("inv", inv);
            } catch (Throwable ignored) {}
            if (root instanceof Horse horse) {
                map.put("style", horse.getStyle().name());
                try {
                    ItemStack armor = horse.getInventory().getArmor();
                    if (armor != null && !armor.getType().isAir()) map.put("armor", armor.getType().name());
                } catch (Throwable ignored) {}
            }
        }

        if (root instanceof Boat boat) {
            try {
                map.put("boatType", boat.getBoatType().name());
            } catch (Throwable ignored) {}
        }

        return mapToString(map);
    }

    private static Entity getRootVehicle(Entity entity) {
        Entity current = entity;
        while (current.getVehicle() != null) current = current.getVehicle();
        return current;
    }

    private static boolean isTransferable(Entity root, EntityType type) {
        if (root instanceof AbstractHorse) return true;
        String name = type.name();
        if (name.contains("BOAT") || name.contains("MINECART")) return true;
        if (name.contains("GHAST")) return true;
        return switch (type) {
            case PIG, STRIDER -> true;
            default -> false;
        };
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(";", ",").replace("=", ":");
    }

    private static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(SEP);
            sb.append(e.getKey()).append(KV).append(e.getValue());
        }
        return sb.toString();
    }

    private static String encodeItems(ItemStack[] items) {
        if (items == null) return "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(baos)) {
            oos.writeInt(items.length);
            for (ItemStack it : items) {
                oos.writeObject(it);
            }
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    public static Map<String, String> parse(String data) {
        Map<String, String> map = new LinkedHashMap<>();
        if (data == null || data.isEmpty()) return map;
        for (String part : data.split(SEP)) {
            int i = part.indexOf(KV);
            if (i > 0) {
                map.put(part.substring(0, i), part.substring(i + 1));
            }
        }
        return map;
    }
}
