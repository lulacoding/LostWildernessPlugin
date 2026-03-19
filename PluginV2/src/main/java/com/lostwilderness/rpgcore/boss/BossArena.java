package com.lostwilderness.rpgcore.boss;

import java.util.Objects;

/**
 * Immutable model for a sandboxed boss arena: id, display name, boundary, and optional flags.
 */
public final class BossArena {

    private final String id;
    private final String displayName;
    private final Boundary boundary;
    private final boolean restrictMovement;
    private final boolean resetOnEmpty;

    public BossArena(String id, String displayName, Boundary boundary, boolean restrictMovement, boolean resetOnEmpty) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = displayName != null ? displayName : id;
        this.boundary = Objects.requireNonNull(boundary, "boundary");
        this.restrictMovement = restrictMovement;
        this.resetOnEmpty = resetOnEmpty;
    }

    /**
     * Creates an arena with no movement restriction and no auto-reset.
     */
    public BossArena(String id, String displayName, Boundary boundary) {
        this(id, displayName, boundary, false, false);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public boolean isRestrictMovement() {
        return restrictMovement;
    }

    public boolean isResetOnEmpty() {
        return resetOnEmpty;
    }
}
