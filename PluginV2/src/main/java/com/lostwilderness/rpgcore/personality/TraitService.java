package com.lostwilderness.rpgcore.personality;

import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core service interface for personality trait system.
 * Handles trait assignment, progression, Ultimate items, and Holy Enchants.
 */
public interface TraitService {

    // ========== Profile Management ==========

    /**
     * Get player's trait profile (cached in memory, loaded on login).
     */
    CompletableFuture<Optional<PlayerTraitProfile>> getProfile(UUID uuid);

    /**
     * Assign initial trait and element (called after quiz completion).
     */
    CompletableFuture<Void> assignInitialTrait(UUID uuid, PersonalityTrait trait, Element element);

    /**
     * Reload profile from database (cache refresh).
     */
    CompletableFuture<Void> reloadProfile(UUID uuid);

    // ========== Progression ==========

    /**
     * Check completion % and advance tier if thresholds crossed.
     * Called automatically on milestone unlock.
     */
    void checkAndAdvanceTier(UUID uuid);

    /**
     * Manually advance player to a new tier (admin command).
     */
    CompletableFuture<Void> advanceTier(UUID uuid, TraitTier newTier);

    /**
     * Get player's current completion percentage (0-300%).
     */
    int getCompletionPercent(UUID uuid);

    // ========== Elements ==========

    /**
     * Reveal player's element (called by Lord via command).
     */
    CompletableFuture<Void> revealElement(UUID playerUuid, UUID lordUuid);

    /**
     * Mark temple as complete for player (called by BetonQuest event).
     */
    CompletableFuture<Void> completeTemple(UUID uuid, Element element);

    /**
     * Check if player has reached post-game state (all 5 temples + 300%).
     */
    boolean isPostGame(UUID uuid);

    // ========== Ultimate Items ==========

    /**
     * Create an Ultimate item for a trait at a given tier.
     * Includes trait enchant and max vanilla enchantments.
     */
    ItemStack createUltimateItem(PersonalityTrait trait, TraitTier tier);

    /**
     * Grant Ultimate item to player (called on reaching Ultimate tier).
     */
    CompletableFuture<Void> grantUltimateItem(UUID uuid);

    // ========== Holy Enchants ==========

    /**
     * Bless an item: double enchant levels + add random Holy Enchant.
     * Lord-only command.
     */
    CompletableFuture<Void> blessItem(UUID targetUuid, ItemStack item, UUID lordUuid);

    /**
     * Grant Christmas Holy Enchants to post-game players (300% completion).
     * Called by daily scheduler on Dec 25.
     */
    CompletableFuture<Void> grantChristmasEnchants(UUID uuid, int count);

    // ========== Utility ==========

    /**
     * Check if player has a specific trait at minimum tier.
     */
    boolean hasActiveTrait(UUID uuid, PersonalityTrait trait, TraitTier minTier);

    /**
     * Get player's current trait (or null if not assigned).
     */
    PersonalityTrait getCurrentTrait(UUID uuid);

    /**
     * Get player's current tier (or APPRENTICE if not assigned).
     */
    TraitTier getCurrentTier(UUID uuid);

    /**
     * Check if player's element is revealed.
     */
    boolean isElementRevealed(UUID uuid);

    /**
     * Check if player's element is activated (temple complete).
     */
    boolean isElementActivated(UUID uuid);
}
