package com.lostwilderness.rpgcore.zodiac;

import java.util.UUID;

/**
 * Immutable zodiac profile for a player.
 * Contains month sign, year sign, Epochian status, hidden second sign, and spirit animal.
 */
public record ZodiacProfile(
    UUID playerUuid,
    ZodiacSign monthSign,
    ZodiacSign yearSign,
    boolean isEpochian,
    ZodiacSign secondSign,          // Nullable, Epochian only
    boolean secondSignRevealed,
    SpiritAnimal spiritAnimal,
    boolean spiritAnimalRevealed,
    String personality               // Placeholder for future quiz system
) {
    public ZodiacProfile {
        if (playerUuid == null) throw new IllegalArgumentException("playerUuid cannot be null");
        if (monthSign == null) throw new IllegalArgumentException("monthSign cannot be null");
        if (yearSign == null) throw new IllegalArgumentException("yearSign cannot be null");
        if (spiritAnimal == null) throw new IllegalArgumentException("spiritAnimal cannot be null");

        // Validate Epochian rules
        if (isEpochian && secondSign == null) {
            throw new IllegalArgumentException("Epochian players must have a second sign");
        }
        if (!isEpochian && secondSign != null) {
            throw new IllegalArgumentException("Non-Epochian players cannot have a second sign");
        }
    }
}
