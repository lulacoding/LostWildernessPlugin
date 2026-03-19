package com.lostwilderness.rpgcore.events.util;

import org.bukkit.block.Biome;

import java.util.Set;

/** Biome groups for event logic (cold, desert, savanna, jungle, snowy). */
public final class BiomeGroups {

    private static final Set<Biome> COLD = Set.of(
        Biome.SNOWY_PLAINS,
        Biome.SNOWY_SLOPES,
        Biome.ICE_SPIKES,
        Biome.SNOWY_TAIGA
    );
    private static final Set<Biome> DESERT = Set.of(Biome.DESERT);
    private static final Set<Biome> SAVANNA = Set.of(
        Biome.SAVANNA,
        Biome.SAVANNA_PLATEAU,
        Biome.WINDSWEPT_SAVANNA
    );
    private static final Set<Biome> JUNGLE = Set.of(
        Biome.JUNGLE,
        Biome.BAMBOO_JUNGLE,
        Biome.SPARSE_JUNGLE
    );

    private BiomeGroups() {}

    public static boolean isCold(Biome b) { return COLD.contains(b); }
    public static boolean isDesert(Biome b) { return DESERT.contains(b); }
    public static boolean isSavanna(Biome b) { return SAVANNA.contains(b); }
    public static boolean isJungle(Biome b) { return JUNGLE.contains(b); }
    public static boolean isSnowy(Biome b) { return isCold(b); }
}
