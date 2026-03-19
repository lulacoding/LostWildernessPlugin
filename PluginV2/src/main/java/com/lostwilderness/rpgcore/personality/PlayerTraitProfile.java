package com.lostwilderness.rpgcore.personality;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable record representing a player's trait progression state.
 * Loaded from database on login, cached in memory during session.
 */
public record PlayerTraitProfile(
    UUID playerUuid,
    PersonalityTrait primaryTrait,
    TraitTier primaryTier,
    Element element,
    boolean elementRevealed,
    boolean elementActivated,  // After temple quest complete
    Map<Element, Boolean> templeCompletions,  // 5 temples tracking
    boolean isPostGame,  // All 5 temples + 300% completion
    Set<HolyEnchant> grantedHolyEnchants,
    Instant lastChristmasClaim,
    Instant assignedAt,
    Instant lastTierAdvance
) {
    /**
     * Create a new profile for a freshly assigned trait (from quiz completion).
     */
    public static PlayerTraitProfile createNew(UUID playerUuid, PersonalityTrait trait, Element element) {
        return new PlayerTraitProfile(
            playerUuid,
            trait,
            TraitTier.APPRENTICE,
            element,
            false,  // Element hidden until Lord reveals
            false,  // Element inactive until temple complete
            Map.of(
                Element.FIRE, false,
                Element.EARTH, false,
                Element.WIND, false,
                Element.WATER, false,
                Element.AETHER, false
            ),
            false,  // Not post-game yet
            Set.of(),  // No Holy Enchants granted yet
            null,  // No Christmas claim yet
            Instant.now(),
            null  // No tier advance yet
        );
    }

    /**
     * Create a copy with updated tier.
     */
    public PlayerTraitProfile withTier(TraitTier newTier) {
        return new PlayerTraitProfile(
            playerUuid,
            primaryTrait,
            newTier,
            element,
            elementRevealed,
            elementActivated,
            templeCompletions,
            isPostGame,
            grantedHolyEnchants,
            lastChristmasClaim,
            assignedAt,
            Instant.now()  // Update last tier advance
        );
    }

    /**
     * Create a copy with element revealed.
     */
    public PlayerTraitProfile withElementRevealed() {
        return new PlayerTraitProfile(
            playerUuid,
            primaryTrait,
            primaryTier,
            element,
            true,  // Revealed
            elementActivated,
            templeCompletions,
            isPostGame,
            grantedHolyEnchants,
            lastChristmasClaim,
            assignedAt,
            lastTierAdvance
        );
    }

    /**
     * Create a copy with temple completed.
     */
    public PlayerTraitProfile withTempleComplete(Element completedElement) {
        Map<Element, Boolean> updated = new java.util.HashMap<>(templeCompletions);
        updated.put(completedElement, true);

        // Check if all temples complete → post-game
        boolean allComplete = updated.values().stream().allMatch(Boolean::booleanValue);

        return new PlayerTraitProfile(
            playerUuid,
            primaryTrait,
            primaryTier,
            element,
            elementRevealed,
            true,  // Element activated after first temple
            updated,
            allComplete,  // Post-game if all 5 complete
            grantedHolyEnchants,
            lastChristmasClaim,
            assignedAt,
            lastTierAdvance
        );
    }

    /**
     * Create a copy with Holy Enchants granted.
     */
    public PlayerTraitProfile withHolyEnchants(Set<HolyEnchant> newEnchants) {
        Set<HolyEnchant> updated = new java.util.HashSet<>(grantedHolyEnchants);
        updated.addAll(newEnchants);

        return new PlayerTraitProfile(
            playerUuid,
            primaryTrait,
            primaryTier,
            element,
            elementRevealed,
            elementActivated,
            templeCompletions,
            isPostGame,
            updated,
            Instant.now(),  // Update Christmas claim time
            assignedAt,
            lastTierAdvance
        );
    }

    /**
     * Check if a specific temple is complete.
     */
    public boolean isTempleComplete(Element templeElement) {
        return templeCompletions.getOrDefault(templeElement, false);
    }

    /**
     * Get count of completed temples.
     */
    public int getCompletedTempleCount() {
        return (int) templeCompletions.values().stream().filter(Boolean::booleanValue).count();
    }

    /**
     * Check if player has a specific Holy Enchant.
     */
    public boolean hasHolyEnchant(HolyEnchant enchant) {
        return grantedHolyEnchants.contains(enchant);
    }
}
