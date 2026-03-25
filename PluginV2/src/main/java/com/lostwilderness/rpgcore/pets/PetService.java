package com.lostwilderness.rpgcore.pets;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Core service for pet tracking system.
 * Phase 1: Registration, death tracking, queries.
 * Phase 2: Commands, reconciliation.
 */
public interface PetService {

    /**
     * Register a newly tamed pet.
     * Enforces 3 pet limit per player.
     *
     * @param ownerUuid Player who tamed the pet
     * @param entity The tamed entity
     * @return PetProfile if successful, throws exception if limit reached
     */
    CompletableFuture<PetProfile> registerPet(UUID ownerUuid, Entity entity);

    /**
     * Record death of a pet.
     *
     * @param petUuid UUID of the pet entity
     */
    CompletableFuture<Void> recordDeath(UUID petUuid);

    /**
     * Get all living pets for a player.
     *
     * @param ownerUuid Player UUID
     * @return List of active pet profiles
     */
    CompletableFuture<List<PetProfile>> getActivePets(UUID ownerUuid);

    /**
     * Get all dead pets for a player (graveyard).
     *
     * @param ownerUuid Player UUID
     * @return List of dead pet profiles
     */
    CompletableFuture<List<PetProfile>> getGraveyard(UUID ownerUuid);

    /**
     * Get pet profile by UUID.
     *
     * @param petUuid Pet entity UUID
     * @return Optional PetProfile
     */
    CompletableFuture<Optional<PetProfile>> getPetProfile(UUID petUuid);

    /**
     * Check if player can tame more pets (under 3 limit).
     *
     * @param ownerUuid Player UUID
     * @return true if can tame more
     */
    CompletableFuture<Boolean> canTameMore(UUID ownerUuid);

    /**
     * Get cached profile if available (sync).
     * Used by listeners for fast lookups.
     *
     * @param petUuid Pet entity UUID
     * @return Optional cached profile
     */
    Optional<PetProfile> getCachedProfile(UUID petUuid);

    /**
     * Get all cached pet UUIDs for a player (sync).
     * Used by the zodiac effect task to avoid DB calls every 5 seconds.
     *
     * @param ownerUuid Owner UUID
     * @return Set of pet UUIDs in cache (may be empty if not yet loaded)
     */
    java.util.Set<UUID> getCachedPetUuids(UUID ownerUuid);

    /**
     * Find a pet by its custom name.
     * Phase 2: Command support.
     *
     * @param ownerUuid Owner UUID
     * @param name Pet's custom name
     * @return Optional PetProfile
     */
    CompletableFuture<Optional<PetProfile>> findPetByName(UUID ownerUuid, String name);

    /**
     * Rename a pet.
     * Phase 2: Command support.
     *
     * @param ownerUuid Owner UUID
     * @param oldName Current pet name
     * @param newName New pet name
     * @return true if renamed, false if pet not found
     */
    CompletableFuture<Boolean> renamePet(UUID ownerUuid, String oldName, String newName);

    /**
     * Manual reconciliation - find pet entity near player and re-link.
     * Phase 2: Reconciliation support.
     *
     * @param ownerUuid Owner UUID
     * @param petName Pet name to search for
     * @param location Player's location (search center)
     * @return true if found and re-linked
     */
    CompletableFuture<Boolean> manualReconcile(UUID ownerUuid, String petName, Location location);

    /**
     * Perform automatic reconciliation for a player.
     * Called on player join.
     * Phase 2: Auto-reconciliation.
     *
     * @param ownerUuid Owner UUID
     * @param location Player's location
     */
    CompletableFuture<Void> autoReconcile(UUID ownerUuid, Location location);

    /**
     * Transfer pet ownership to a new player.
     * Phase 6: Golden Bone retaming.
     *
     * @param petUuid UUID of the pet to transfer
     * @param newOwnerUuid UUID of the new owner
     */
    CompletableFuture<Void> retamePet(UUID petUuid, UUID newOwnerUuid);
}
