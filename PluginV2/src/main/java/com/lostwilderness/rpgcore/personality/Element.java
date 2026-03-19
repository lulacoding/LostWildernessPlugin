package com.lostwilderness.rpgcore.personality;

import org.bukkit.ChatColor;

/**
 * Five elemental affinities assigned during personality quiz.
 * Elements grant god-tier passives after temple quest completion.
 */
public enum Element {
    FIRE("Fire", "🔥", ChatColor.RED, "Immune to fire and lava damage"),
    EARTH("Earth", "🌍", ChatColor.DARK_GREEN, "Immune to suffocation damage"),
    WIND("Wind", "💨", ChatColor.WHITE, "Elytra never loses durability"),
    WATER("Water", "💧", ChatColor.BLUE, "Permanent underwater breathing"),
    AETHER("Aether", "⭐", ChatColor.LIGHT_PURPLE, "~90% fall damage reduction");

    private final String displayName;
    private final String symbol;
    private final ChatColor color;
    private final String passiveDescription;

    Element(String displayName, String symbol, ChatColor color, String passiveDescription) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
        this.passiveDescription = passiveDescription;
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

    public String getPassiveDescription() {
        return passiveDescription;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    /**
     * Full display string: colored symbol + name
     */
    public String getFullDisplay() {
        return color + symbol + " " + displayName;
    }
}
