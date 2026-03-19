package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.reputation.Faction;

public enum PlayerClass {

    CELESTIAL_TEMPLAR(Faction.CELESTIAL),
    WILDLAND_RANGER(Faction.WILDLANDS),
    REDEEMED_ARTIFICER(Faction.CELESTIAL),
    CORRUPTED_CULTIST(Faction.CORRUPTED),
    DESTROYER_BERSERKER(Faction.DESTROYERS);

    private final Faction alignedFaction;

    PlayerClass(Faction alignedFaction) {
        this.alignedFaction = alignedFaction;
    }

    public Faction getAlignedFaction() {
        return alignedFaction;
    }

    public static PlayerClass fromStorageValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        if ("EPOCHIAN_ARTIFICER".equalsIgnoreCase(raw)) {
            return REDEEMED_ARTIFICER;
        }
        try {
            return PlayerClass.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
