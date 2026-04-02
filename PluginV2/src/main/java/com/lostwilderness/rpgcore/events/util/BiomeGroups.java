package com.lostwilderness.rpgcore.events.util;

import org.bukkit.block.Biome;

import java.util.Locale;
import java.util.Set;

/**
 * Biome groups for event logic (cold, desert, savanna, jungle, snowy).
 * Vanilla {@link Biome} enum sets are checked first; Iris/datapack biomes are matched by
 * {@link Biome#getKey()} path (e.g. {@code overworld:tropical_rainforest}).
 */
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

    private static String keyPath(Biome b) {
        return b.getKey().getKey().toLowerCase(Locale.ROOT);
    }

    /** Moist / jungle-style — monsoons, etc. Excludes volcanic rock and desert-style hot keys. */
    public static boolean isJungle(Biome b) {
        if (JUNGLE.contains(b)) return true;
        String k = keyPath(b);
        if (k.contains("mangrove")) return true;
        if (k.contains("volcano") || k.contains("volcanic") || k.contains("mesa")
            || k.contains("desert") || k.contains("dunes") || k.contains("badlands")
            || k.contains("oasis") || k.contains("savanna")) {
            return false;
        }
        if (k.contains("jungle") || k.contains("rainforest") || k.contains("bamboo")) return true;
        if (k.contains("tropical")) {
            return !k.contains("beach") && !k.contains("sea") && !k.contains("ocean")
                && !k.contains("submerged");
        }
        if (k.contains("terralost") && k.contains("jungle")) return true;
        if (k.contains("wilds") && (k.contains("tropical") || k.contains("rainforest"))) return true;
        return false;
    }

    public static boolean isDesert(Biome b) {
        if (DESERT.contains(b)) return true;
        String k = keyPath(b);
        if (k.contains("desert") || k.contains("dunes") || k.contains("oasis")) return true;
        if (k.contains("badlands") || k.contains("mesa")) return true;
        if (k.contains("ancient_sands") || k.contains("ancientsands")) return true;
        if (k.contains("gravel_desert") || k.contains("sandstone_valley")) return true;
        return k.contains("terralost") && k.contains("desert");
    }

    public static boolean isSavanna(Biome b) {
        if (SAVANNA.contains(b)) return true;
        String k = keyPath(b);
        if (k.contains("savanna") || k.contains("acacia")) return true;
        if (k.contains("fractured_savanna") || k.contains("ashen_savanna")) return true;
        return k.contains("steppe") && !k.contains("cold");
    }

    /**
     * Cold / snowy terrain for frost effects. Cherry grove is excluded (temperate).
     * Stony peaks etc. are handled via vanilla set + key heuristics.
     */
    public static boolean isCold(Biome b) {
        if (COLD.contains(b)) return true;
        String k = keyPath(b);
        if (k.contains("cherry")) return false;
        if (k.contains("snow") || k.contains("frozen") || k.contains("frost") || k.contains("ice")
            || k.contains("tundra") || k.contains("winter") || k.contains("wintry")
            || k.contains("alpine") || k.contains("glacial") || k.contains("snowy")
            || k.contains("permafrost") || k.contains("siberian")) {
            return true;
        }
        if (k.contains("tropical") || k.contains("jungle") || k.contains("desert")
            || k.contains("mesa") || k.contains("badlands") || k.contains("savanna")) {
            return false;
        }
        String n = b.name();
        return n.contains("SNOW") || n.contains("FROZEN") || n.contains("ICE")
            || "GROVE".equals(n) || n.contains("SNOWY_TAIGA") || n.contains("PEAK") || n.contains("MOUNTAIN");
    }

    public static boolean isSnowy(Biome b) {
        return isCold(b);
    }

    /**
     * Ranger speed: vanilla forest/taiga/jungle + Iris forest/woods/taiga (not cherry by name).
     */
    public static boolean isRangerForestSpeedBiome(Biome b) {
        return switch (b) {
            case FOREST, BIRCH_FOREST, DARK_FOREST, TAIGA, OLD_GROWTH_PINE_TAIGA,
                 OLD_GROWTH_BIRCH_FOREST, OLD_GROWTH_SPRUCE_TAIGA,
                 JUNGLE, SPARSE_JUNGLE, BAMBOO_JUNGLE, MANGROVE_SWAMP -> true;
            default -> isRangerForestSpeedBiomeCustom(b);
        };
    }

    private static boolean isRangerForestSpeedBiomeCustom(Biome b) {
        if (isJungle(b)) return true;
        String k = keyPath(b);
        if (k.contains("cherry")) return false;
        if (k.contains("forest") || k.contains("woods") || k.contains("woodland")) return true;
        if (k.contains("taiga") || k.contains("grove") || k.contains("redwood") || k.contains("sequo")) return true;
        if (k.contains("birch") || k.contains("oak") || k.contains("denmyre")) return true;
        if (k.contains("roofed") || k.contains("willow") || k.contains("marsh")) return true;
        return k.contains("combo") || k.contains("longtree") || k.contains("meadows");
    }

    /** Personality RANGER: forest / jungle style (broader than speed biome). */
    public static boolean isForestOrJungleTerrain(Biome b) {
        return isRangerForestSpeedBiome(b);
    }

    /** Zodiac HORSE: open grass / plains / savanna. */
    public static boolean isHorseOpenTerrain(Biome b) {
        return switch (b) {
            case PLAINS, SUNFLOWER_PLAINS, SAVANNA, SAVANNA_PLATEAU, MEADOW -> true;
            default -> {
                String k = keyPath(b);
                yield k.contains("plains") || k.contains("meadow") || k.contains("prairie")
                    || k.contains("steppe") || k.contains("savanna") || k.contains("sunflower")
                    || k.contains("estranged") || k.contains("lush_plains") || k.contains("flower");
            }
        };
    }
}
