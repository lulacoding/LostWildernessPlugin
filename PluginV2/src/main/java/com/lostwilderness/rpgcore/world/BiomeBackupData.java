package com.lostwilderness.rpgcore.world;

import com.lostwilderness.rpgcore.events.util.BiomeRegistryUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Aeternum-style chunk biome backup: grid sample (stepXZ, stepY), palette of biome keys, indices.
 * Compact storage for one chunk's biome data so it can be restored later.
 */
public final class BiomeBackupData {

    private final int stepXZ;
    private final int stepY;
    private final int minY;
    private final int maxY;
    private final String[] palette;
    private final int[] indices;

    public BiomeBackupData(int stepXZ, int stepY, int minY, int maxY, String[] palette, int[] indices) {
        this.stepXZ = stepXZ;
        this.stepY = stepY;
        this.minY = minY;
        this.maxY = maxY;
        this.palette = palette != null ? palette.clone() : new String[0];
        this.indices = indices != null ? indices.clone() : new int[0];
    }

    public int getStepXZ() { return stepXZ; }
    public int getStepY() { return stepY; }
    public int getMinY() { return minY; }
    public int getMaxY() { return maxY; }
    public String[] getPalette() { return palette.clone(); }
    public int[] getIndices() { return indices.clone(); }

    /** Number of sample points in X (0..15 with step stepXZ). */
    private int sizeX() {
        return stepXZ <= 0 ? 16 : (16 + stepXZ - 1) / stepXZ;
    }

    /** Number of sample points in Z. */
    private int sizeZ() {
        return stepXZ <= 0 ? 16 : (16 + stepXZ - 1) / stepXZ;
    }

    /** Number of sample points in Y. */
    private int sizeY() {
        if (stepY <= 0 || maxY <= minY) return 1;
        return (maxY - minY + stepY) / stepY;
    }

    /** Index into indices array for chunk-relative block position (0..15, y, 0..15). */
    public int indexAt(int relX, int y, int relZ) {
        int ix = stepXZ <= 0 ? relX : relX / stepXZ;
        int iy = (y - minY) / stepY;
        int iz = stepXZ <= 0 ? relZ : relZ / stepXZ;
        int sx = sizeX(), sz = sizeZ(), sy = sizeY();
        if (ix < 0 || ix >= sx || iy < 0 || iy >= sy || iz < 0 || iz >= sz) return -1;
        return (iy * sz + iz) * sx + ix;
    }

    /** Get biome key at chunk-relative position, or null if out of range / invalid. */
    public String getBiomeKeyAt(int relX, int y, int relZ) {
        int i = indexAt(relX, y, relZ);
        if (i < 0 || i >= indices.length) return null;
        int palIdx = indices[i];
        if (palIdx < 0 || palIdx >= palette.length) return null;
        return palette[palIdx];
    }

    /** Create backup from chunk by sampling biomes on a grid. Uses world min/max height for minY/maxY. */
    public static BiomeBackupData fromChunk(Chunk chunk, int stepXZ, int stepY) {
        World w = chunk.getWorld();
        int minY = w.getMinHeight();
        int maxY = w.getMaxHeight() - 1;
        if (stepXZ < 1) stepXZ = 1;
        if (stepY < 1) stepY = 1;

        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        Set<String> paletteSet = new LinkedHashSet<>();
        List<Integer> indexList = new ArrayList<>();

        int sx = (16 + stepXZ - 1) / stepXZ;
        int sz = (16 + stepXZ - 1) / stepXZ;
        int sy = (maxY - minY + stepY) / stepY;

        List<String> paletteOrder = new ArrayList<>();
        for (int oy = 0; oy < sy; oy++) {
            int y = minY + oy * stepY;
            for (int oz = 0; oz < sz; oz++) {
                int z = baseZ + oz * stepXZ;
                for (int ox = 0; ox < sx; ox++) {
                    int x = baseX + ox * stepXZ;
                    Biome b = w.getBiome(x, y, z);
                    String key = b.getKey().toString();
                    int idx;
                    if (!paletteSet.contains(key)) {
                        idx = paletteOrder.size();
                        paletteOrder.add(key);
                        paletteSet.add(key);
                    } else {
                        idx = paletteOrder.indexOf(key);
                    }
                    indexList.add(idx);
                }
            }
        }

        String[] pal = paletteOrder.toArray(new String[0]);
        int[] ind = indexList.stream().mapToInt(Integer::intValue).toArray();
        return new BiomeBackupData(stepXZ, stepY, minY, maxY, pal, ind);
    }

    /**
     * Create backup from a pre-captured grid (e.g. from biome painter before apply).
     * Grid order must match fromChunk: sample points in order (y, z, x) — oy, oz, ox.
     */
    public static BiomeBackupData fromGrid(org.bukkit.block.Biome[] grid, int stepXZ, int stepY, int minY, int maxY) {
        if (grid == null || grid.length == 0) return null;
        if (stepXZ < 1) stepXZ = 1;
        if (stepY < 1) stepY = 1;
        int sx = (16 + stepXZ - 1) / stepXZ;
        int sz = (16 + stepXZ - 1) / stepXZ;
        int sy = (maxY - minY + stepY) / stepY;
        int expected = sx * sz * sy;
        if (grid.length != expected) return null;
        Set<String> paletteSet = new LinkedHashSet<>();
        List<String> paletteOrder = new ArrayList<>();
        List<Integer> indexList = new ArrayList<>();
        for (org.bukkit.block.Biome b : grid) {
            String key = b.getKey().toString();
            int idx;
            if (!paletteSet.contains(key)) {
                idx = paletteOrder.size();
                paletteOrder.add(key);
                paletteSet.add(key);
            } else {
                idx = paletteOrder.indexOf(key);
            }
            indexList.add(idx);
        }
        String[] pal = paletteOrder.toArray(new String[0]);
        int[] ind = indexList.stream().mapToInt(Integer::intValue).toArray();
        return new BiomeBackupData(stepXZ, stepY, minY, maxY, pal, ind);
    }

    /** Apply this backup to the chunk (restore biomes). Chunk must be loaded. */
    public void applyToChunk(Chunk chunk) {
        World w = chunk.getWorld();
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        int sx = sizeX(), sz = sizeZ(), sy = sizeY();
        int idx = 0;
        for (int oy = 0; oy < sy && idx < indices.length; oy++) {
            int y = minY + oy * stepY;
            for (int oz = 0; oz < sz && idx < indices.length; oz++) {
                int z = baseZ + oz * stepXZ;
                for (int ox = 0; ox < sx && idx < indices.length; ox++) {
                    int x = baseX + ox * stepXZ;
                    int palIdx = indices[idx++];
                    if (palIdx >= 0 && palIdx < palette.length) {
                        Biome b = BiomeRegistryUtil.biomeFromKeyString(palette[palIdx]);
                        if (b != null) {
                            w.setBiome(x, y, z, b);
                        }
                    }
                }
            }
        }
    }
}
