package com.lostwilderness.rpgcore.personality;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Thirteen personality traits that define player playstyle and progression.
 * Each trait has 4 tiers with progressively unlocking passive abilities.
 */
public enum PersonalityTrait {
    MAGE(
            "Mage", "🔮", ChatColor.LIGHT_PURPLE,
            Material.CROSSBOW, "Mage Bow", "MAGE_FOCUS",
            false),
    WARRIOR(
            "Warrior", "⚔", ChatColor.RED,
            Material.DIAMOND_SWORD, "Warlord's Blade", "BERSERKER",
            false),
    ARCHER(
            "Archer", "🏹", ChatColor.GREEN,
            Material.BOW, "Celestial Bow", "HOMING",
            false),
    HEALER(
            "Healer", "✚", ChatColor.YELLOW,
            Material.TRIDENT, "Staff of the Covenant", "SANCTIFY",
            true// Has Good/Evil variants
    ),
    RANGER(
            "Ranger", "🌲", ChatColor.DARK_GREEN,
            Material.LEATHER, "Ranger's Quiver", "INFINITY_LINK",
            false),
    ALCHEMIST(
            "Alchemist", "⚗", ChatColor.DARK_PURPLE,
            Material.POTION, "Flask of Eternity", "ETERNAL_BREW",
            false),
    SMITH(
            "Smith", "🔨", ChatColor.GRAY,
            Material.DIAMOND_PICKAXE, "The Eternal Hammer", "MASTER_CRAFT",
            false),
    SCOUT(
            "Scout", "👁", ChatColor.AQUA,
            Material.DIAMOND_BOOTS, "Shadowstep Boots", "PHANTOM_STEP",
            false),
    BERSERKER(
            "Berserker", "💢", ChatColor.DARK_RED,
            Material.DIAMOND_AXE, "Ragnarok Axe", "BLOODLUST",
            false),
    SAGE(
            "Sage", "📖", ChatColor.BLUE,
            Material.BOOK, "Tome of Infinite Wisdom", "KNOWLEDGE",
            false),
    TAMER(
            "Tamer", "🐺", ChatColor.WHITE,
            Material.BONE, "Ancient Whistle", "BEAST_MASTER",
            false),
    RUNEKEEPER(
            "Runekeeper", "✨", ChatColor.DARK_AQUA,
            Material.DIAMOND_SWORD, "Runeblade", "RUNIC_OVERLOAD",
            false),
    ILLUSIONIST(
            "Illusionist", "🎭", ChatColor.DARK_PURPLE,
            Material.GLASS, "Mirror Shard", "ILLUSION",
            true// Has Good/Evil variants
    );

    private final String displayName;
    private final String symbol;
    private final ChatColor color;
    private final Material ultimateItem;
    private final String ultimateItemName;
    private final String traitEnchant;
    private final boolean hasAlignmentVariants;

    PersonalityTrait(String displayName, String symbol, ChatColor color,
            Material ultimateItem, String ultimateItemName, String traitEnchant,
            boolean hasAlignmentVariants) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
        this.ultimateItem = ultimateItem;
        this.ultimateItemName = ultimateItemName;
        this.traitEnchant = traitEnchant;
        this.hasAlignmentVariants = hasAlignmentVariants;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getUltimateItem() {
        return ultimateItem;
    }

    public String getUltimateItemName() {
        return ultimateItemName;
    }

    public String getTraitEnchant() {
        return traitEnchant;
    }

    public boolean hasAlignmentVariants() {
        return hasAlignmentVariants;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getFullDisplay() {
        return color + symbol + " " + displayName;
    }

    /**
     * Get passive ability description for the given tier.
     * 
     * @param tier            The trait tier
     * @param isGoodAlignment True if player has positive honor (for variant traits)
     */
    public String getPassiveDescription(TraitTier tier, boolean isGoodAlignment) {
        return switch (this) {
            case MAGE -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "Splash & lingering potions have +50% larger AoE";
                case MASTER -> "Potions stack duration instead of replacing";
                case ULTIMATE -> "Granted Mage Bow with max enchants + Mage Focus trait";
            };
            case WARRIOR -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+10% melee damage";
                case MASTER -> "Shield cooldown reduced by 50%";
                case ULTIMATE -> "Granted Warlord's Blade - damage scales with missing health";
            };
            case ARCHER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+15% bow/crossbow damage";
                case MASTER -> "Arrows pierce 1 additional entity";
                case ULTIMATE -> "Granted Celestial Bow - arrows home toward targets";
            };
            case HEALER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> isGoodAlignment
                        ? "AoE Regen I to nearby players every 15s"
                        : "AoE Wither I to nearby enemies every 15s";
                case MASTER -> "Potions thrown by you last 50% longer on targets";
                case ULTIMATE -> "Granted Staff of the Covenant - cleanses/applies effects";
            };
            case RANGER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+20% speed in forested/jungle biomes";
                case MASTER -> "Tamed wolves/cats: +15% damage";
                case ULTIMATE -> "Granted Ranger's Quiver - applies Infinity to held bows";
            };
            case ALCHEMIST -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "Brewed potions have +25% duration";
                case MASTER -> "Can brew 4 potions simultaneously";
                case ULTIMATE -> "Granted Flask of Eternity - random positive potion effect";
            };
            case SMITH -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "Anvil repair costs -2 levels";
                case MASTER -> "Crafting gear uses 20% fewer materials";
                case ULTIMATE -> "Granted Eternal Hammer - repairs held item by 50%";
            };
            case SCOUT -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+15% movement speed permanently";
                case MASTER -> "Sneak speed equals walk speed";
                case ULTIMATE -> "Granted Shadowstep Boots - double-sneak to blink";
            };
            case BERSERKER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "Strength I for 5s when below 30% HP (10s cooldown)";
                case MASTER -> "+20% damage when not wearing chestplate";
                case ULTIMATE -> "Granted Ragnarok Axe - kills refresh Speed I burst";
            };
            case SAGE -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+25% XP gain from all sources";
                case MASTER -> "Enchanting table gives one extra option";
                case ULTIMATE -> "Granted Tome of Infinite Wisdom - grants 5 XP levels";
            };
            case TAMER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "+25% chance taming succeeds on first attempt";
                case MASTER -> "Tamed mobs gain +30% max HP";
                case ULTIMATE -> "Granted Ancient Whistle - summons persistent wolf companion";
            };
            case RUNEKEEPER -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> "Enchanted items glow + +10% enchant effectiveness";
                case MASTER -> "Enchanting no longer requires lapis lazuli";
                case ULTIMATE -> "Granted Runeblade - critical hits trigger random enchant effect";
            };
            case ILLUSIONIST -> switch (tier) {
                case APPRENTICE -> "No passive ability yet";
                case TRAIT -> isGoodAlignment
                        ? "Can cast harmless decoy (5s duration, 2min cooldown)"
                        : "Decoy attracts mob aggro (5s duration, 2min cooldown)";
                case MASTER -> "Invisibility potions last 3× longer";
                case ULTIMATE -> "Granted Mirror Shard - swap position with target player";
            };
        };
    }

    /**
     * Get short description for quiz result display
     */
    public String getShortDescription() {
        return switch (this) {
            case MAGE -> "Master of arcane potions and magic";
            case WARRIOR -> "Frontline fighter, skilled in melee combat";
            case ARCHER -> "Precise marksman with deadly aim";
            case HEALER -> "Supportive ally or deadly plague-bringer";
            case RANGER -> "Nature's guardian, friend to beasts";
            case ALCHEMIST -> "Brewing expert with endless concoctions";
            case SMITH -> "Craftsman who bends metal to will";
            case SCOUT -> "Swift and agile, always one step ahead";
            case BERSERKER -> "Raging warrior who thrives in chaos";
            case SAGE -> "Scholar seeking knowledge and wisdom";
            case TAMER -> "Beast master with loyal companions";
            case RUNEKEEPER -> "Enchanter wielding runic power";
            case ILLUSIONIST -> "Trickster who bends reality itself";
        };
    }
}
