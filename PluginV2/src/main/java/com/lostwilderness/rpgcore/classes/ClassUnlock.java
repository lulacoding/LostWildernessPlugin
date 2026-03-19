package com.lostwilderness.rpgcore.classes;

/**
 * Represents a single unlock in a class skill tree.
 * Immutable data structure defining what ability/passive is unlocked at a given level.
 *
 * @param requiredLevel Minimum mastery level required to unlock this ability
 * @param abilityName Display name of the ability (e.g., "Divine Strike")
 * @param description Short description shown in /class tree command
 * @param isActiveAbility True if this is an active ability (requires input), false for passives
 */
public record ClassUnlock(int requiredLevel, String abilityName, String description, boolean isActiveAbility) {
    public ClassUnlock {
        if (requiredLevel < 1) {
            throw new IllegalArgumentException("Required level must be >= 1");
        }
        if (abilityName == null || abilityName.isBlank()) {
            throw new IllegalArgumentException("Ability name cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
    }

    public String formatForDisplay() {
        return abilityName + " — " + description;
    }

    public String getTypeLabel() {
        return isActiveAbility ? "Active" : "Passive";
    }
}
