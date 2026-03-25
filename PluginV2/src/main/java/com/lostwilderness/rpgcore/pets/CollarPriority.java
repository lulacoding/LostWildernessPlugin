package com.lostwilderness.rpgcore.pets;

/**
 * Collar color priority system for dogs.
 * Phase 5 implementation - Phase 1 just stores DEFAULT.
 * Higher ordinal = higher priority.
 */
public enum CollarPriority {
    DEFAULT,    // Vanilla red collar
    CUSTOM,     // Player-dyed color
    CLAN,       // Player's clan color
    ALLIANCE;   // Clan's alliance color

    /**
     * Check if this priority wins over another.
     * Order: ALLIANCE > CLAN > CUSTOM > DEFAULT
     */
    public boolean hasHigherPriorityThan(CollarPriority other) {
        return this.ordinal() > other.ordinal();
    }
}
