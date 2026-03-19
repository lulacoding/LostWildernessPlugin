package com.lostwilderness.rpgcore.reputation;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Faction {
    CELESTIAL("Celestial", NamedTextColor.AQUA, true),
    EPOCHIANS("Epochians", NamedTextColor.GOLD, true),
    WILDLANDS("Wildlands", NamedTextColor.GREEN, null), // Neutral
    CORRUPTED("Corrupted", NamedTextColor.DARK_PURPLE, false),
    DESTROYERS("Destroyers", NamedTextColor.DARK_RED, false);

    private final String displayName;
    private final NamedTextColor color;
    private final Boolean good; // true = good, false = bad, null = neutral

    Faction(String displayName, NamedTextColor color, Boolean good) {
        this.displayName = displayName;
        this.color = color;
        this.good = good;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public Boolean isGood() {
        return good;
    }
}
