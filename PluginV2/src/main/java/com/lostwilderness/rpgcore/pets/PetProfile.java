package com.lostwilderness.rpgcore.pets;

import com.lostwilderness.rpgcore.zodiac.ZodiacSign;
import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * Immutable record representing a registered pet.
 * All fields frozen at registration except death_* fields.
 */
public record PetProfile(
    UUID petUuid,
    UUID ownerUuid,
    EntityType entityType,
    String customName,
    long tameDay,           // Epoch day count from calendar
    String tameMcDate,      // Formatted MC date (e.g., "Year 5, Day 123")
    Long deathDay,          // Nullable - null if alive
    String deathMcDate,     // Nullable - null if alive
    ZodiacSign zodiacSign,
    PetPersonality personality,
    boolean personalityRevealed,
    CollarPriority collarPriority,
    boolean isLost,
    String lastSeenLocation,  // "world,x,y,z" format
    long createdAt,         // Unix millis
    long updatedAt          // Unix millis
) {
    /**
     * Check if pet is currently alive.
     */
    public boolean isAlive() {
        return deathDay == null;
    }
}
