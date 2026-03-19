package com.lostwilderness.rpgcore.personality;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Set;

/**
 * Thirteen Holy Enchants - custom enchantments above vanilla max tier.
 * Granted as Ultimate rewards or Christmas drops for 300% completion players.
 */
public enum HolyEnchant {
    SOULFIRE(
            "Soulfire",
            "Burns enemies with divine flame that bypasses armor",
            Set.of(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT)),
    DIVINE_SHIELD(
            "Divine Shield",
            "Grants temporary absorption hearts when blocking damage",
            Set.of(Material.SHIELD, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE)),
    CELESTIAL_STRIKE(
            "Celestial Strike",
            "Chance to call down lightning on critical hits",
            Set.of(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.DIAMOND_AXE, Material.NETHERITE_AXE)),
    VOID_PIERCE(
            "Void Pierce",
            "Arrows ignore a portion of target's armor",
            Set.of(Material.BOW, Material.CROSSBOW)),
    NATURES_GRASP(
            "Nature's Grasp",
            "Roots enemies in place on hit, preventing movement",
            Set.of(Material.BOW, Material.TRIDENT)),
    THUNDERCLAP(
            "Thunderclap",
            "Sprint attacks create shockwaves that knock back nearby enemies",
            Set.of(Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS)),
    SERPENTS_FANG(
            "Serpent's Fang",
            "Melee attacks apply poison that stacks in duration",
            Set.of(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD)),
    LUNAR_BLESSING(
            "Lunar Blessing",
            "Regenerate health slowly during nighttime",
            Set.of(Material.DIAMOND_HELMET, Material.NETHERITE_HELMET)),
    STARFALL(
            "Starfall",
            "Arrows rain down additional projectiles on impact",
            Set.of(Material.BOW, Material.CROSSBOW)),
    ANCIENT_WARD(
            "Ancient Ward",
            "Reduce incoming damage by a flat amount",
            Set.of(Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.SHIELD)),
    PHOENIX_FLAME(
            "Phoenix Flame",
            "On death, revive once per day with fire immunity buff",
            Set.of(Material.TOTEM_OF_UNDYING, Material.NETHERITE_CHESTPLATE)),
    TITANIC_FORCE(
            "Titanic Force",
            "Melee attacks deal bonus damage based on target's max health",
            Set.of(Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.MACE)),
    ECHO_STEP(
            "Echo Step",
            "Leave behind afterimages that briefly confuse enemies",
            Set.of(Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));

    private final String displayName;
    private final String description;
    private final Set<Material> applicableTypes;

    HolyEnchant(String displayName, String description, Set<Material> applicableTypes) {
        this.displayName = displayName;
        this.description = description;
        this.applicableTypes = applicableTypes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Set<Material> getApplicableTypes() {
        return applicableTypes;
    }

    /**
     * Gold-colored name for lore display
     */
    public String getColoredName() {
        return ChatColor.GOLD + "⚡ Holy Enchant: " + displayName;
    }

    /**
     * Check if this enchant can be applied to the given material
     */
    public boolean isApplicableTo(Material material) {
        return applicableTypes.contains(material);
    }

    /**
     * Get a random Holy Enchant applicable to the given material
     */
    public static HolyEnchant getRandomFor(Material material) {
        HolyEnchant[] applicable = java.util.stream.Stream.of(values())
                .filter(e -> e.isApplicableTo(material))
                .toArray(HolyEnchant[]::new);

        if (applicable.length == 0) {
            return null;
        }

        return applicable[java.util.concurrent.ThreadLocalRandom.current().nextInt(applicable.length)];
    }
}
