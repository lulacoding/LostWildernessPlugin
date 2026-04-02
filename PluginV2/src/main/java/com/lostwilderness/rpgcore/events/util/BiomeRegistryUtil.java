package com.lostwilderness.rpgcore.events.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

/** Resolve biomes from datapack / Iris keys (Registry) — {@link Biome#valueOf} only works for vanilla enum names. */
public final class BiomeRegistryUtil {

    private BiomeRegistryUtil() {}

    /** Parse {@code minecraft:plains} or {@code overworld:tropical_volcano}; returns null if unknown. */
    public static @Nullable Biome biomeFromKeyString(String key) {
        if (key == null || key.isEmpty()) return null;
        NamespacedKey nk = NamespacedKey.fromString(key);
        if (nk == null) return null;
        return Registry.BIOME.get(nk);
    }
}
