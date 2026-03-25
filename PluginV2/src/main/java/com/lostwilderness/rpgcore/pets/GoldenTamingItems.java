package com.lostwilderness.rpgcore.pets;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;

/**
 * Factory for Golden Taming Items - Phase 6.
 * Three craftable items that allow instant/forced taming of pets.
 *
 * Golden Bone  → wolves  (can also retame wolves owned by others)
 * Golden Seeds → parrots (instant guaranteed tame)
 * Golden Fish  → cats    (instant guaranteed tame)
 */
public final class GoldenTamingItems {

    public static final String GOLDEN_BONE = "GOLDEN_BONE";
    public static final String GOLDEN_SEEDS = "GOLDEN_SEEDS";
    public static final String GOLDEN_FISH = "GOLDEN_FISH";

    /** Entity types each item can tame. */
    public static final Set<EntityType> BONE_TARGETS  = Set.of(EntityType.WOLF);
    public static final Set<EntityType> SEEDS_TARGETS = Set.of(EntityType.PARROT);
    public static final Set<EntityType> FISH_TARGETS  = Set.of(EntityType.CAT);

    private static NamespacedKey itemTypeKey;

    public static void initialize(Plugin plugin) {
        itemTypeKey = new NamespacedKey(plugin, "golden_taming_item");
    }

    // -------------------------------------------------------------------------
    // Item builders
    // -------------------------------------------------------------------------

    public static ItemStack buildGoldenBone() {
        ItemStack item = new ItemStack(Material.BONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lGolden Bone");
        meta.setLore(List.of(
            "§7A bone coated in pure gold dust.",
            "§7Instantly tames any wolf.",
            "§6Can retame wolves that belong to others."
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, GOLDEN_BONE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack buildGoldenSeeds() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lGolden Seeds");
        meta.setLore(List.of(
            "§7Seeds dusted with golden pollen.",
            "§7Instantly tames any parrot.",
            "§7Success is guaranteed."
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, GOLDEN_SEEDS);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack buildGoldenFish() {
        ItemStack item = new ItemStack(Material.COD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lGolden Fish");
        meta.setLore(List.of(
            "§7A gleaming fish from enchanted waters.",
            "§7Instantly tames any cat.",
            "§7Success is guaranteed."
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, GOLDEN_FISH);
        item.setItemMeta(meta);
        return item;
    }

    // -------------------------------------------------------------------------
    // Recipe registration
    // -------------------------------------------------------------------------

    public static void registerRecipes(Plugin plugin) {
        // Golden Bone: 8 gold nuggets surrounding a bone
        ShapedRecipe boneRecipe = new ShapedRecipe(new NamespacedKey(plugin, "golden_bone"), buildGoldenBone());
        boneRecipe.shape("GGG", "GBG", "GGG");
        boneRecipe.setIngredient('G', Material.GOLD_NUGGET);
        boneRecipe.setIngredient('B', Material.BONE);
        plugin.getServer().addRecipe(boneRecipe);

        // Golden Seeds: 8 gold nuggets surrounding wheat seeds
        ShapedRecipe seedsRecipe = new ShapedRecipe(new NamespacedKey(plugin, "golden_seeds"), buildGoldenSeeds());
        seedsRecipe.shape("GGG", "GSG", "GGG");
        seedsRecipe.setIngredient('G', Material.GOLD_NUGGET);
        seedsRecipe.setIngredient('S', Material.WHEAT_SEEDS);
        plugin.getServer().addRecipe(seedsRecipe);

        // Golden Fish: 8 gold nuggets surrounding cod
        ShapedRecipe fishRecipe = new ShapedRecipe(new NamespacedKey(plugin, "golden_fish"), buildGoldenFish());
        fishRecipe.shape("GGG", "GFG", "GGG");
        fishRecipe.setIngredient('G', Material.GOLD_NUGGET);
        fishRecipe.setIngredient('F', Material.COD);
        plugin.getServer().addRecipe(fishRecipe);
    }

    // -------------------------------------------------------------------------
    // Identification helpers
    // -------------------------------------------------------------------------

    public static boolean isGoldenTamingItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(itemTypeKey, PersistentDataType.STRING);
    }

    public static String getItemType(ItemStack item) {
        if (!isGoldenTamingItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
    }

    public static boolean canTameEntityType(String itemType, EntityType entityType) {
        return switch (itemType) {
            case GOLDEN_BONE  -> BONE_TARGETS.contains(entityType);
            case GOLDEN_SEEDS -> SEEDS_TARGETS.contains(entityType);
            case GOLDEN_FISH  -> FISH_TARGETS.contains(entityType);
            default           -> false;
        };
    }
}
