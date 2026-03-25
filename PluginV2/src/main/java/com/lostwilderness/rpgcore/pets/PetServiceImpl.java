package com.lostwilderness.rpgcore.pets;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.zodiac.ZodiacSign;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pet service implementation with in-memory cache.
 */
public final class PetServiceImpl implements PetService {

    private static final int MAX_PETS_PER_PLAYER = 3;

    private final PetRepository repo;
    private final CalendarServiceV2 calendar;
    private final Plugin plugin;

    // Cache: petUuid -> PetProfile
    private final ConcurrentHashMap<UUID, PetProfile> cache = new ConcurrentHashMap<>();

    // Index: ownerUuid -> Set<petUuid> for fast sync lookup (used by effect task)
    private final ConcurrentHashMap<UUID, java.util.Set<UUID>> ownerPetIndex = new ConcurrentHashMap<>();

    // PDC keys for reconciliation
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;

    public PetServiceImpl(PetRepository repo, CalendarServiceV2 calendar, Plugin plugin) {
        this.repo = repo;
        this.calendar = calendar;
        this.plugin = plugin;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
    }

    @Override
    public CompletableFuture<PetProfile> registerPet(UUID ownerUuid, Entity entity) {
        // Validate entity is tameable
        if (!(entity instanceof Tameable tameable)) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Entity is not tameable")
            );
        }

        // Check 3 pet limit
        return canTameMore(ownerUuid).thenCompose(canTame -> {
            if (!canTame) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("Pet limit reached (max 3 pets)")
                );
            }

            // Get current calendar date
            CalendarServiceV2.CalendarSnapshot snapshot = calendar.getCurrentSnapshot();
            LocalDate date = snapshot.date();
            long tameDay = snapshot.dayCount();
            String tameMcDate = formatMcDate(date, tameDay);

            // Calculate zodiac sign from tame date
            ZodiacSign zodiacSign = ZodiacSign.getSignForMCDate(date.getMonthValue(), date.getDayOfMonth());

            // Randomly assign personality
            PetPersonality personality = PetPersonality.randomPersonality();

            // Get custom name (or null if not named)
            String customName = tameable.getCustomName();

            // Format location
            Location loc = entity.getLocation();
            String lastSeenLocation = formatLocation(loc);

            // Build profile
            UUID petUuid = entity.getUniqueId();
            long now = System.currentTimeMillis();

            PetProfile profile = new PetProfile(
                petUuid,
                ownerUuid,
                entity.getType(),
                customName,
                tameDay,
                tameMcDate,
                null,  // Not dead yet
                null,  // No death date
                zodiacSign,
                personality,
                false, // Personality not revealed
                CollarPriority.DEFAULT,
                false, // Not lost
                lastSeenLocation,
                now,   // created_at
                now    // updated_at
            );

            // Save to DB and cache
            return repo.registerPet(profile).thenApply(v -> {
                cache.put(petUuid, profile);
                ownerPetIndex.computeIfAbsent(ownerUuid, k -> ConcurrentHashMap.newKeySet()).add(petUuid);
                plugin.getLogger().info(
                    "[pets] Registered: " + (customName != null ? customName : entity.getType().name()) +
                    " (" + entity.getType() + ") for " + ownerUuid +
                    " - " + zodiacSign.displayName() + ", " + personality.getDisplayName()
                );
                return profile;
            });
        });
    }

    @Override
    public CompletableFuture<Void> recordDeath(UUID petUuid) {
        return getPetProfile(petUuid).thenCompose(opt -> {
            if (opt.isEmpty()) {
                plugin.getLogger().warning("[pets] Cannot record death: pet not found: " + petUuid);
                return CompletableFuture.completedFuture(null);
            }

            PetProfile profile = opt.get();

            // Already dead?
            if (!profile.isAlive()) {
                return CompletableFuture.completedFuture(null);
            }

            // Get current calendar date
            CalendarServiceV2.CalendarSnapshot snapshot = calendar.getCurrentSnapshot();
            LocalDate date = snapshot.date();
            long deathDay = snapshot.dayCount();
            String deathMcDate = formatMcDate(date, deathDay);

            // Record in DB
            return repo.recordDeath(petUuid, deathDay, deathMcDate).thenRun(() -> {
                // Update cache with new death info
                PetProfile updated = new PetProfile(
                    profile.petUuid(), profile.ownerUuid(), profile.entityType(),
                    profile.customName(), profile.tameDay(), profile.tameMcDate(),
                    deathDay, deathMcDate,
                    profile.zodiacSign(), profile.personality(), profile.personalityRevealed(),
                    profile.collarPriority(), profile.isLost(), profile.lastSeenLocation(),
                    profile.createdAt(), System.currentTimeMillis()
                );
                cache.put(petUuid, updated);
                // Remove from owner index since pet is dead
                java.util.Set<UUID> ownerPets = ownerPetIndex.get(profile.ownerUuid());
                if (ownerPets != null) ownerPets.remove(petUuid);

                plugin.getLogger().info(
                    "[pets] Recorded death: " + (profile.customName() != null ? profile.customName() : profile.entityType().name()) +
                    " (" + petUuid + ")"
                );
            });
        });
    }

    @Override
    public CompletableFuture<List<PetProfile>> getActivePets(UUID ownerUuid) {
        return repo.getActivePets(ownerUuid).thenApply(profiles -> {
            // Update cache and owner index
            java.util.Set<UUID> petUuids = ConcurrentHashMap.newKeySet();
            for (PetProfile p : profiles) {
                cache.put(p.petUuid(), p);
                petUuids.add(p.petUuid());
            }
            ownerPetIndex.put(ownerUuid, petUuids);
            return profiles;
        });
    }

    @Override
    public CompletableFuture<List<PetProfile>> getGraveyard(UUID ownerUuid) {
        return repo.getDeadPets(ownerUuid);
    }

    @Override
    public CompletableFuture<Optional<PetProfile>> getPetProfile(UUID petUuid) {
        // Check cache first
        PetProfile cached = cache.get(petUuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        // Load from DB
        return repo.getPetByUuid(petUuid).thenApply(opt -> {
            opt.ifPresent(p -> cache.put(petUuid, p));
            return opt;
        });
    }

    @Override
    public CompletableFuture<Boolean> canTameMore(UUID ownerUuid) {
        return repo.countActivePets(ownerUuid).thenApply(count -> count < MAX_PETS_PER_PLAYER);
    }

    @Override
    public Optional<PetProfile> getCachedProfile(UUID petUuid) {
        return Optional.ofNullable(cache.get(petUuid));
    }

    @Override
    public java.util.Set<UUID> getCachedPetUuids(UUID ownerUuid) {
        java.util.Set<UUID> set = ownerPetIndex.get(ownerUuid);
        return set != null ? set : java.util.Collections.emptySet();
    }

    @Override
    public CompletableFuture<Optional<PetProfile>> findPetByName(UUID ownerUuid, String name) {
        return repo.findPetByName(ownerUuid, name).thenApply(opt -> {
            opt.ifPresent(p -> cache.put(p.petUuid(), p));
            return opt;
        });
    }

    @Override
    public CompletableFuture<Boolean> renamePet(UUID ownerUuid, String oldName, String newName) {
        return findPetByName(ownerUuid, oldName).thenCompose(opt -> {
            if (opt.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            PetProfile pet = opt.get();

            // Update in DB
            return repo.updatePetName(pet.petUuid(), newName).thenApply(v -> {
                // Update cache
                PetProfile updated = new PetProfile(
                    pet.petUuid(), pet.ownerUuid(), pet.entityType(),
                    newName, // New name
                    pet.tameDay(), pet.tameMcDate(), pet.deathDay(), pet.deathMcDate(),
                    pet.zodiacSign(), pet.personality(), pet.personalityRevealed(),
                    pet.collarPriority(), pet.isLost(), pet.lastSeenLocation(),
                    pet.createdAt(), System.currentTimeMillis()
                );
                cache.put(pet.petUuid(), updated);

                plugin.getLogger().info("[pets] Renamed: " + oldName + " -> " + newName);
                return true;
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> manualReconcile(UUID ownerUuid, String petName, Location location) {
        return findPetByName(ownerUuid, petName).thenCompose(opt -> {
            if (opt.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            PetProfile pet = opt.get();

            // Must run getNearbyEntities on main thread
            CompletableFuture<Boolean> result = new CompletableFuture<>();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    // Search for entity within 32 blocks (main thread)
                    Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 32, 32, 32);

                    for (Entity entity : nearbyEntities) {
                        if (!(entity instanceof Tameable tameable)) continue;
                        if (!tameable.isTamed()) continue;

                        String entityName = tameable.getCustomName();
                        if (entityName != null && entityName.equalsIgnoreCase(petName)) {
                            // Found it! Re-link PDC (main thread is OK for this)
                            entity.getPersistentDataContainer().set(petUuidKey, PersistentDataType.STRING, pet.petUuid().toString());
                            entity.getPersistentDataContainer().set(petRegisteredKey, PersistentDataType.BOOLEAN, true);

                            // Update location in DB (async)
                            String newLocation = formatLocation(entity.getLocation());
                            repo.updateLastSeenLocation(pet.petUuid(), newLocation).thenRun(() -> {
                                // Mark as not lost
                                repo.markAsLost(pet.petUuid(), false);
                                plugin.getLogger().info("[pets] Reconciled: " + petName + " (" + pet.petUuid() + ")");
                            });

                            result.complete(true);
                            return;
                        }
                    }

                    result.complete(false);
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });

            return result;
        });
    }

    @Override
    public CompletableFuture<Void> retamePet(UUID petUuid, UUID newOwnerUuid) {
        return getPetProfile(petUuid).thenCompose(opt -> {
            if (opt.isEmpty()) {
                plugin.getLogger().warning("[pets] retamePet: pet not found: " + petUuid);
                return CompletableFuture.completedFuture(null);
            }

            PetProfile pet = opt.get();
            UUID oldOwnerUuid = pet.ownerUuid();

            return repo.transferOwnership(petUuid, newOwnerUuid).thenRun(() -> {
                // Update cache
                PetProfile updated = new PetProfile(
                    pet.petUuid(), newOwnerUuid, pet.entityType(),
                    pet.customName(), pet.tameDay(), pet.tameMcDate(),
                    pet.deathDay(), pet.deathMcDate(),
                    pet.zodiacSign(), pet.personality(), pet.personalityRevealed(),
                    pet.collarPriority(), false, pet.lastSeenLocation(),
                    pet.createdAt(), System.currentTimeMillis()
                );
                cache.put(petUuid, updated);

                // Remove from old owner's index, add to new owner's index
                java.util.Set<UUID> oldOwnerPets = ownerPetIndex.get(oldOwnerUuid);
                if (oldOwnerPets != null) oldOwnerPets.remove(petUuid);
                ownerPetIndex.computeIfAbsent(newOwnerUuid, k -> ConcurrentHashMap.newKeySet()).add(petUuid);

                plugin.getLogger().info("[pets] Retamed: " + petUuid + " from " + oldOwnerUuid + " to " + newOwnerUuid);
            });
        });
    }

    @Override
    public CompletableFuture<Void> autoReconcile(UUID ownerUuid, Location location) {
        return getActivePets(ownerUuid).thenCompose(pets -> {
            if (pets.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }

            // Must run getNearbyEntities on main thread
            CompletableFuture<Void> result = new CompletableFuture<>();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    // Search for entities within 32 blocks (main thread)
                    Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 32, 32, 32);

                    for (PetProfile pet : pets) {
                        if (pet.customName() == null) continue; // Can't reconcile unnamed pets

                        // Check if entity already has correct PDC
                        for (Entity entity : nearbyEntities) {
                            if (!(entity instanceof Tameable tameable)) continue;
                            if (!tameable.isTamed()) continue;

                            String entityName = tameable.getCustomName();
                            if (entityName != null && entityName.equalsIgnoreCase(pet.customName())) {
                                // Found matching entity - check/update PDC (main thread is OK)
                                String storedUuid = entity.getPersistentDataContainer().get(petUuidKey, PersistentDataType.STRING);
                                if (storedUuid == null || !storedUuid.equals(pet.petUuid().toString())) {
                                    // Re-link
                                    entity.getPersistentDataContainer().set(petUuidKey, PersistentDataType.STRING, pet.petUuid().toString());
                                    entity.getPersistentDataContainer().set(petRegisteredKey, PersistentDataType.BOOLEAN, true);

                                    plugin.getLogger().info("[pets] Auto-reconciled: " + pet.customName() + " (" + pet.petUuid() + ")");
                                }
                                break;
                            }
                        }
                    }

                    result.complete(null);
                } catch (Exception e) {
                    result.completeExceptionally(e);
                }
            });

            return result;
        });
    }

    /**
     * Format MC calendar date for display.
     */
    private String formatMcDate(LocalDate date, long dayCount) {
        return String.format("Year %d, Day %d", date.getYear(), dayCount);
    }

    /**
     * Format location as "world,x,y,z".
     */
    private String formatLocation(Location loc) {
        return String.format("%s,%d,%d,%d",
            loc.getWorld().getName(),
            loc.getBlockX(),
            loc.getBlockY(),
            loc.getBlockZ()
        );
    }
}
