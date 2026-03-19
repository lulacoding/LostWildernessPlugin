package com.lostwilderness.rpgcore.classes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Factory for creating Class Mastery Items (level 50 unlock rewards).
 * Each class receives a unique legendary item with custom enchantments and mechanics.
 */
public final class UltimateItemBuilder {

    private static Plugin plugin;
    private static NamespacedKey classMasteryKey;

    /**
     * Initialize the builder with plugin instance for PDC keys.
     * Must be called during module initialization.
     */
    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
        classMasteryKey = new NamespacedKey(plugin, "class_mastery_enchant");
    }

    /**
     * Give the appropriate Class Mastery Item to a player based on their class.
     * Item is added directly to inventory (drops if full).
     *
     * @param player The player receiving the item
     * @param playerClass The player's class
     */
    public static void giveUltimateItem(Player player, PlayerClass playerClass) {
        ItemStack item = switch (playerClass) {
            case CELESTIAL_TEMPLAR -> buildHolyAvenger();
            case WILDLAND_RANGER -> buildRangersQuiver();
            case REDEEMED_ARTIFICER -> buildEternalHammer();
            case CORRUPTED_CULTIST -> buildMirrorShard();
            case DESTROYER_BERSERKER -> buildRagnarokAxe();
        };

        player.getInventory().addItem(item);
    }

    /**
     * CELESTIAL TEMPLAR - Holy Avenger
     * Diamond sword with enhanced Smite and Sharpness.
     */
    private static ItemStack buildHolyAvenger() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§6§lHoly Avenger");
        meta.addEnchant(Enchantment.SHARPNESS, 5, false);
        meta.addEnchant(Enchantment.SMITE, 10, true); // Above vanilla max
        meta.addEnchant(Enchantment.UNBREAKING, 3, false);

        meta.setLore(List.of(
            "§5⚔ Class Mastery Item",
            "§7Smite deals chain lightning to nearby undead.",
            "§7Divine strikes empower your holy abilities."
        ));

        meta.getPersistentDataContainer().set(classMasteryKey, PersistentDataType.STRING, "HOLY_AVENGER");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * WILDLAND RANGER - Ranger's Quiver
     * Leather chestplate that grants Infinity effect to equipped bows.
     */
    private static ItemStack buildRangersQuiver() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§a§lRanger's Quiver");
        meta.addEnchant(Enchantment.PROTECTION, 4, false);
        meta.addEnchant(Enchantment.UNBREAKING, 3, false);

        meta.setLore(List.of(
            "§5⚔ Class Mastery Item",
            "§7Grants Infinity effect to your bow.",
            "§7Arrows never run out while equipped."
        ));

        meta.getPersistentDataContainer().set(classMasteryKey, PersistentDataType.STRING, "RANGERS_QUIVER");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * REDEEMED ARTIFICER - Eternal Hammer
     * Netherite pickaxe with Unbreaking X and repair ability.
     */
    private static ItemStack buildEternalHammer() {
        ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§b§lEternal Hammer");
        meta.addEnchant(Enchantment.EFFICIENCY, 5, false);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true); // Above vanilla max
        meta.addEnchant(Enchantment.MENDING, 1, false);

        meta.setLore(List.of(
            "§5⚔ Class Mastery Item",
            "§7Right-click to repair held tool (5 min cooldown).",
            "§7Virtually indestructible with extreme durability."
        ));

        meta.getPersistentDataContainer().set(classMasteryKey, PersistentDataType.STRING, "ETERNAL_HAMMER");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * CORRUPTED CULTIST - Mirror Shard
     * Ender pearl that can swap positions with targeted entity.
     */
    private static ItemStack buildMirrorShard() {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§5§lMirror Shard");

        meta.setLore(List.of(
            "§5⚔ Class Mastery Item",
            "§7Right-click entity to swap positions (5 min cooldown).",
            "§7Teleport mechanics bend to your will."
        ));

        meta.getPersistentDataContainer().set(classMasteryKey, PersistentDataType.STRING, "MIRROR_SHARD");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * DESTROYER BERSERKER - Ragnarok Axe
     * Netherite axe with Bloodlust proc on kill.
     */
    private static ItemStack buildRagnarokAxe() {
        ItemStack item = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§c§lRagnarok Axe");
        meta.addEnchant(Enchantment.SHARPNESS, 5, false);
        meta.addEnchant(Enchantment.UNBREAKING, 5, false);

        meta.setLore(List.of(
            "§5⚔ Class Mastery Item",
            "§7Kills refresh Speed I and grant brief invulnerability.",
            "§7Channel the fury of ancient berserkers."
        ));

        meta.getPersistentDataContainer().set(classMasteryKey, PersistentDataType.STRING, "RAGNAROK_AXE");
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Check if an item is a Class Mastery Item by checking PDC.
     *
     * @param item The item to check
     * @return True if the item has a class_mastery_enchant key
     */
    public static boolean isClassMasteryItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(classMasteryKey, PersistentDataType.STRING);
    }

    /**
     * Get the mastery enchant type from an item's PDC.
     *
     * @param item The item to check
     * @return The mastery enchant identifier (e.g., "HOLY_AVENGER"), or null if not a mastery item
     */
    public static String getClassMasteryEnchant(ItemStack item) {
        if (!isClassMasteryItem(item)) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(classMasteryKey, PersistentDataType.STRING);
    }
}
