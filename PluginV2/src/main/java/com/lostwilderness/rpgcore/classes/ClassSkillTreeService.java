package com.lostwilderness.rpgcore.classes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing class skill tree unlocks and progression.
 * Each PlayerClass has a linear unlock tree with 7 levels (1/5/10/15/20/30/50).
 * Unlocks are gated by mastery level from AuraSkills custom skills.
 */
public interface ClassSkillTreeService {

    /**
     * Get the mastery skill key for a given class.
     * @param playerClass The class to query
     * @return Skill key string (e.g., "templar_mastery")
     */
    String getMasterySkillKey(PlayerClass playerClass);

    /**
     * Get current mastery level for a player's class.
     * Queries AuraSkills for the player's skill level.
     *
     * @param playerUuid Player UUID
     * @param playerClass Player's class
     * @return Current mastery level (0 if player has no class or AuraSkills unavailable)
     */
    int getMasteryLevel(UUID playerUuid, PlayerClass playerClass);

    /**
     * Check if an ability/passive is unlocked at the player's current level.
     * @param playerUuid Player UUID
     * @param playerClass Player's class
     * @param requiredLevel Minimum level required
     * @return True if player's mastery level >= requiredLevel
     */
    boolean isUnlocked(UUID playerUuid, PlayerClass playerClass, int requiredLevel);

    /**
     * Get the next locked unlock for a player.
     * Useful for showing "Next unlock: ..." messages.
     *
     * @param playerUuid Player UUID
     * @param playerClass Player's class
     * @return Optional containing the next unlock, or empty if all unlocks achieved
     */
    Optional<ClassUnlock> getNextUnlock(UUID playerUuid, PlayerClass playerClass);

    /**
     * Get all unlocks for a class with their current locked/unlocked status.
     * Used for displaying the full skill tree to players.
     *
     * @param playerUuid Player UUID
     * @param playerClass Player's class
     * @return List of all unlocks with status, ordered by required level
     */
    List<ClassUnlockStatus> getAllUnlocks(UUID playerUuid, PlayerClass playerClass);

    /**
     * Check if a player has already been granted their Ultimate item for a class.
     * @param playerUuid Player UUID
     * @param playerClass Class to check
     * @return CompletableFuture<Boolean> true if already granted
     */
    CompletableFuture<Boolean> hasUltimateItem(UUID playerUuid, PlayerClass playerClass);

    /**
     * Grant the Ultimate item for a class to a player.
     * Only grants if player doesn't already have it and is level 50+.
     * Sends title + sound effect on successful grant.
     *
     * @param playerUuid Player UUID
     * @param playerClass Player's class
     * @return CompletableFuture<Boolean> true if item was granted, false if already had it or not eligible
     */
    CompletableFuture<Boolean> grantUltimateItem(UUID playerUuid, PlayerClass playerClass);

    /**
     * Check if Sanctified Ground death prevention is available for a player.
     * Templar level 30+ passive: survive death once with 1 heart, 10 min cooldown.
     *
     * @param playerUuid Player UUID
     * @return True if off cooldown and eligible
     */
    boolean isSanctifiedGroundAvailable(UUID playerUuid);

    /**
     * Trigger Sanctified Ground death prevention.
     * Sets cooldown timestamp (10 minutes).
     *
     * @param playerUuid Player UUID
     */
    void triggerSanctifiedGround(UUID playerUuid);
}
