package com.lostwilderness.rpgcore.personality;

import org.bukkit.ChatColor;

/**
 * Four progression tiers for personality traits.
 * Tiers unlock at specific story completion % thresholds.
 */
public enum TraitTier {
    APPRENTICE(0, "Apprentice", ChatColor.GRAY),
    TRAIT(25, "Trait", ChatColor.GREEN),
    MASTER(50, "Master", ChatColor.AQUA),
    ULTIMATE(100, "Ultimate", ChatColor.GOLD);

    private final int completionThreshold;
    private final String displayName;
    private final ChatColor color;

    TraitTier(int completionThreshold, String displayName, ChatColor color) {
        this.completionThreshold = completionThreshold;
        this.displayName = displayName;
        this.color = color;
    }

    public int getCompletionThreshold() {
        return completionThreshold;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getColoredName() {
        return color + displayName;
    }

    /**
     * Get the tier that should be active at a given completion %.
     */
    public static TraitTier getTierForCompletion(int completionPercent) {
        TraitTier result = APPRENTICE;
        for (TraitTier tier : values()) {
            if (completionPercent >= tier.completionThreshold) {
                result = tier;
            }
        }
        return result;
    }
}
