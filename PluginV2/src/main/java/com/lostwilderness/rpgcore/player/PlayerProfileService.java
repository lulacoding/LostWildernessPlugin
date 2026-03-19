package com.lostwilderness.rpgcore.player;

import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.player.repo.PlayerProfileRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads profiles async on pre-login, caches them, saves async on quit.
 */
public final class PlayerProfileService {

    private final PlayerProfileRepository repo;
    private final SchedulerService scheduler;
    private final ConcurrentHashMap<UUID, PlayerProfile> cache = new ConcurrentHashMap<>();

    public PlayerProfileService(PlayerProfileRepository repo, SchedulerService scheduler) {
        this.repo = repo;
        this.scheduler = scheduler;
    }

    /** Call from async context (e.g. pre-login listener). */
    public CompletableFuture<PlayerProfile> loadProfileAsync(UUID uuid) {
        return repo.findById(uuid)
            .thenApply(opt -> {
                PlayerProfile p = opt.orElseGet(() -> new PlayerProfile(uuid, Instant.now()));
                cache.put(uuid, p);
                return p;
            });
    }

    /** Call from main thread; returns cached profile only. */
    public Optional<PlayerProfile> getProfile(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    /** Schedule async save and evict from cache. Call on quit. */
    public void saveProfileAsync(UUID uuid) {
        PlayerProfile p = cache.get(uuid);
        if (p == null) return;
        p.setLastSeenAt(Instant.now());
        scheduler.runAsync(() -> repo.save(p).thenRun(() -> cache.remove(uuid)));
    }

    public void evict(UUID uuid) {
        cache.remove(uuid);
    }
}
