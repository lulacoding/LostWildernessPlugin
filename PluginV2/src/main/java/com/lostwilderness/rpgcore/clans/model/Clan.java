package com.lostwilderness.rpgcore.clans.model;

import java.util.UUID;

/**
 * Clan entity: id, name, and display color (hex).
 */
public final class Clan {

    private final UUID id;
    private final String name;
    private final String colorHex;

    public Clan(UUID id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColorHex() {
        return colorHex;
    }
}
