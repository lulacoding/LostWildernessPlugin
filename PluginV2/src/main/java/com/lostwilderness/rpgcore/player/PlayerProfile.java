package com.lostwilderness.rpgcore.player;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal profile for Phase 1: identity and last seen. Extend later with progression, skills, etc.
 */
public final class PlayerProfile {

    private final UUID uuid;
    private volatile Instant lastSeenAt;

    public PlayerProfile(UUID uuid, Instant lastSeenAt) {
        this.uuid = uuid;
        this.lastSeenAt = lastSeenAt != null ? lastSeenAt : Instant.now();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
