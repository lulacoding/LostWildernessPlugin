package com.lostwilderness.rpgcore.progression;

import com.lostwilderness.rpgcore.progression.repo.ProgressionRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Progression/milestone API. All repo access is async; callers use get(timeout) or thenAccept for sync context.
 */
public final class ProgressionService {

    private final ProgressionRepository repo;
    private static final long DEFAULT_TIMEOUT_MS = 5_000;

    public ProgressionService(ProgressionRepository repo) {
        this.repo = repo;
    }

    public CompletableFuture<Boolean> hasUnlockedAsync(UUID playerUuid, String key) {
        return repo.hasUnlocked(playerUuid, key);
    }

    public boolean hasUnlocked(UUID playerUuid, String key) {
        try {
            return repo.hasUnlocked(playerUuid, key).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<Void> unlockAsync(UUID playerUuid, String key) {
        return repo.unlock(playerUuid, key, System.currentTimeMillis());
    }

    public void unlock(UUID playerUuid, String key) {
        repo.unlock(playerUuid, key, System.currentTimeMillis()).orTimeout(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<List<String>> getUnlockedKeysAsync(UUID playerUuid) {
        return repo.getUnlockedKeys(playerUuid);
    }

    public List<String> getUnlockedKeys(UUID playerUuid) {
        try {
            return repo.getUnlockedKeys(playerUuid).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return List.of();
        }
    }

    public CompletableFuture<Long> getCounterAsync(UUID playerUuid, String counterKey) {
        return repo.getCounter(playerUuid, counterKey);
    }

    public long getCounter(UUID playerUuid, String counterKey) {
        try {
            return repo.getCounter(playerUuid, counterKey).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0L;
        }
    }

    public CompletableFuture<Long> incrementCounterAsync(UUID playerUuid, String counterKey) {
        return repo.incrementCounter(playerUuid, counterKey);
    }

    public long incrementCounter(UUID playerUuid, String counterKey) {
        try {
            return repo.incrementCounter(playerUuid, counterKey).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0L;
        }
    }

    public CompletableFuture<Long> addToCounterAsync(UUID playerUuid, String counterKey, long delta) {
        return repo.addToCounter(playerUuid, counterKey, delta);
    }

    public long addToCounter(UUID playerUuid, String counterKey, long delta) {
        try {
            return repo.addToCounter(playerUuid, counterKey, delta).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0L;
        }
    }

    // --- Party-aware methods ---

    /**
     * Unlock an achievement for all members of a player's party.
     * If player is not in a party, unlocks only for that player.
     *
     * @param playerUuid Player UUID (can be any party member)
     * @param achievementKey Achievement key to unlock
     */
    public void unlockForParty(UUID playerUuid, String achievementKey) {
        com.lostwilderness.rpgcore.core.RPGCorePlugin core =
            com.lostwilderness.rpgcore.core.RPGCorePlugin.getInstance();
        com.lostwilderness.rpgcore.party.PartyService partyService = null;
        if (core != null) {
            partyService = core.getService(com.lostwilderness.rpgcore.party.PartyService.class);
        }

        java.util.Set<UUID> targets = new java.util.HashSet<>();
        if (partyService != null && partyService.isInParty(playerUuid)) {
            // Unlock for entire party
            targets.addAll(partyService.getPartyMembers(playerUuid));
        } else {
            // Unlock for individual player only
            targets.add(playerUuid);
        }

        // Unlock for all targets
        for (UUID targetId : targets) {
            unlock(targetId, achievementKey);
        }
    }

    /**
     * Increment a counter for the party leader's quest.
     * If contributor is in a party, increments counter for party leader.
     * If contributor is not in a party, increments their own counter.
     *
     * @param contributorUuid Player UUID who contributed (can be any party member)
     * @param counterKey Counter key to increment
     * @return New counter value for the leader (or contributor if not in party)
     */
    public long incrementCounterForParty(UUID contributorUuid, String counterKey) {
        com.lostwilderness.rpgcore.core.RPGCorePlugin core =
            com.lostwilderness.rpgcore.core.RPGCorePlugin.getInstance();
        com.lostwilderness.rpgcore.party.PartyService partyService = null;
        if (core != null) {
            partyService = core.getService(com.lostwilderness.rpgcore.party.PartyService.class);
        }

        UUID targetUuid = contributorUuid;
        if (partyService != null) {
            com.lostwilderness.rpgcore.party.Party party = partyService.getParty(contributorUuid);
            if (party != null) {
                // Increment counter for party leader
                targetUuid = party.getLeader();
            }
        }

        return incrementCounter(targetUuid, counterKey);
    }
}
