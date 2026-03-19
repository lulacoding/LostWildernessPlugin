package com.lostwilderness.rpgcore.world;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aeternum-style biome painting: paints chunk biomes by season (land, ocean, river)
 * around players in overworld. Backup is saved before apply via BiomeBackupStore.saveChunkFromGrid.
 * Only runs when BiomeBackupStore.isPaintingEnabled() and not in restore mode.
 */
public final class BiomePainter implements Listener, Runnable {

    private static final int STEP_XZ = 4;
    private static final int STEP_Y = 4;

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final BiomeBackupStore backupStore;

    private int radiusChunks = 8;
    private int budgetPerTick = 16;
    private Set<String> disabledWorlds = Set.of();
    private final EnumMap<CalendarServiceV2.Season, Biome> seasonTarget = new EnumMap<>(CalendarServiceV2.Season.class);
    private final Set<Biome> excludedBiomes = new HashSet<>();
    private boolean oceansEnabled = true;
    private boolean riversEnabled = true;
    private final EnumMap<CalendarServiceV2.Season, Biome> oceanTarget = new EnumMap<>(CalendarServiceV2.Season.class);
    private final EnumMap<CalendarServiceV2.Season, Biome> riverTarget = new EnumMap<>(CalendarServiceV2.Season.class);
    private boolean respectNaturallySnowyBiomes = true;
    private boolean oceansKeepDeepVariants = true;

    private final Map<Long, Biome[]> backups = new ConcurrentHashMap<>();
    private final Set<Long> spoofed = ConcurrentHashMap.newKeySet();
    private final Map<Long, Biome> lastApplied = new ConcurrentHashMap<>();
    private final Map<Long, Family> familyCache = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> excludedCache = new ConcurrentHashMap<>();
    private static final Set<Long> COLD_CHUNKS = ConcurrentHashMap.newKeySet();

    private BukkitRunnable task;

    public BiomePainter(Plugin plugin, CalendarServiceV2 calendar, BiomeBackupStore backupStore) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.backupStore = backupStore;
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        var cfg = plugin.getConfig();
        radiusChunks = Math.max(1, cfg.getInt("events.biome-painting.radius-chunks", 8));
        budgetPerTick = Math.max(2, cfg.getInt("events.biome-painting.budget-chunks-per-tick", 16));
        List<String> disabled = cfg.getStringList("events.disabled_worlds");
        disabledWorlds = disabled != null ? Set.copyOf(disabled) : Set.of();
        respectNaturallySnowyBiomes = cfg.getBoolean("events.biome-painting.respect-naturally-snowy-biomes", true);
        oceansEnabled = cfg.getBoolean("events.biome-painting.oceans.enabled", true);
        riversEnabled = cfg.getBoolean("events.biome-painting.rivers.enabled", true);
        oceansKeepDeepVariants = cfg.getBoolean("events.biome-painting.oceans.keep-deep-variants", true);

        seasonTarget.clear();
        seasonTarget.put(CalendarServiceV2.Season.SPRING, readBiome(cfg, "events.biome-painting.seasons.SPRING", Biome.FLOWER_FOREST));
        seasonTarget.put(CalendarServiceV2.Season.SUMMER, readBiome(cfg, "events.biome-painting.seasons.SUMMER", Biome.PLAINS));
        seasonTarget.put(CalendarServiceV2.Season.AUTUMN, readBiome(cfg, "events.biome-painting.seasons.AUTUMN", Biome.WINDSWEPT_SAVANNA));
        seasonTarget.put(CalendarServiceV2.Season.WINTER, readBiome(cfg, "events.biome-painting.seasons.WINTER", Biome.SNOWY_PLAINS));

        oceanTarget.clear();
        oceanTarget.put(CalendarServiceV2.Season.SPRING, readBiome(cfg, "events.biome-painting.oceans.seasons.SPRING", Biome.LUKEWARM_OCEAN));
        oceanTarget.put(CalendarServiceV2.Season.SUMMER, readBiome(cfg, "events.biome-painting.oceans.seasons.SUMMER", Biome.WARM_OCEAN));
        oceanTarget.put(CalendarServiceV2.Season.AUTUMN, readBiome(cfg, "events.biome-painting.oceans.seasons.AUTUMN", Biome.OCEAN));
        oceanTarget.put(CalendarServiceV2.Season.WINTER, readBiome(cfg, "events.biome-painting.oceans.seasons.WINTER", Biome.FROZEN_OCEAN));

        riverTarget.clear();
        riverTarget.put(CalendarServiceV2.Season.SPRING, readBiome(cfg, "events.biome-painting.rivers.seasons.SPRING", Biome.RIVER));
        riverTarget.put(CalendarServiceV2.Season.SUMMER, readBiome(cfg, "events.biome-painting.rivers.seasons.SUMMER", Biome.RIVER));
        riverTarget.put(CalendarServiceV2.Season.AUTUMN, readBiome(cfg, "events.biome-painting.rivers.seasons.AUTUMN", Biome.RIVER));
        riverTarget.put(CalendarServiceV2.Season.WINTER, readBiome(cfg, "events.biome-painting.rivers.seasons.WINTER", Biome.FROZEN_RIVER));

        excludedBiomes.clear();
        List<String> ex = cfg.getStringList("events.biome-painting.excluded-biomes");
        if (ex != null) {
            for (String s : ex) {
                if (s != null && !s.isEmpty()) {
                    try {
                        excludedBiomes.add(Biome.valueOf(s.trim().toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {
                        plugin.getLogger().warning("[BiomePainter] Invalid excluded biome '" + s + "' (ignored)");
                    }
                }
            }
        }
        excludedCache.clear();
    }

    private static Biome readBiome(org.bukkit.configuration.file.FileConfiguration cfg, String path, Biome def) {
        String s = cfg.getString(path, def.name());
        try {
            return Biome.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (task != null) task.cancel();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                BiomePainter.this.run();
            }
        };
        task.runTaskTimer(plugin, 40L, 10L);
    }

    public void unregister() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        org.bukkit.event.HandlerList.unregisterAll(this);
        revertAll();
        backups.clear();
        spoofed.clear();
        lastApplied.clear();
        familyCache.clear();
        excludedCache.clear();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        long k = key(e.getChunk());
        spoofed.remove(k);
        lastApplied.remove(k);
        familyCache.remove(k);
        excludedCache.remove(k);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        Chunk ch = e.getChunk();
        long k = key(ch);
        if (spoofed.remove(k)) {
            revertChunk(ch);
        }
        backups.remove(k);
        excludedCache.remove(k);
    }

    @Override
    public void run() {
        if (backupStore.isRestoreMode() || !backupStore.isPaintingEnabled()) return;

        CalendarServiceV2.CalendarSnapshot snap = calendar.getCurrentSnapshot();
        CalendarServiceV2.Season season = snap.season();
        Biome currentLand = seasonTarget.getOrDefault(season, Biome.PLAINS);
        Biome currentOcean = oceanTarget.getOrDefault(season, Biome.OCEAN);
        Biome currentRiver = riverTarget.getOrDefault(season, Biome.RIVER);

        int budget = budgetPerTick;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (budget <= 0) break;
            World w = p.getWorld();
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            if (disabledWorlds.contains(w.getName().toLowerCase(Locale.ROOT))) continue;

            int view = Bukkit.getViewDistance();
            int radius = Math.max(radiusChunks, view + 1);
            Location loc = p.getLocation();
            int pcx = loc.getBlockX() >> 4;
            int pcz = loc.getBlockZ() >> 4;
            Vector look = loc.getDirection().clone();
            look.setY(0);
            if (look.lengthSquared() < 1e-4) look = new Vector(0, 0, 1);
            else look.normalize();

            // Center chunk
            if (w.isChunkLoaded(pcx, pcz) && budget > 0) {
                Chunk center = w.getChunkAt(pcx, pcz);
                budget = processChunk(w, center, currentLand, currentOcean, currentRiver, budget);
            }
            if (budget <= 0) break;

            // Offsets: distance then forward
            List<Offset> offsets = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    int dist = Math.max(Math.abs(dx), Math.abs(dz));
                    Vector dir = new Vector(dx, 0, dz);
                    double forward = dir.lengthSquared() < 1e-4 ? 0 : look.dot(dir.normalize());
                    offsets.add(new Offset(dx, dz, dist, forward));
                }
            }
            offsets.sort(Comparator.comparingInt((Offset o) -> o.dist).thenComparingDouble((Offset o) -> -o.forwardScore));

            for (Offset off : offsets) {
                if (budget <= 0) break;
                int cx = pcx + off.dx;
                int cz = pcz + off.dz;
                if (!w.isChunkLoaded(cx, cz)) continue;
                Chunk ch = w.getChunkAt(cx, cz);
                budget = processChunk(w, ch, currentLand, currentOcean, currentRiver, budget);
            }
        }
    }

    private int processChunk(World w, Chunk ch, Biome currentLand, Biome currentOcean, Biome currentRiver, int budget) {
        long k = key(ch);
        if (isChunkExcluded(ch)) {
            if (spoofed.remove(k)) revertChunk(ch);
            lastApplied.remove(k);
            familyCache.remove(k);
            return budget;
        }
        Family fam = familyCache.computeIfAbsent(k, x -> classifyFamily(ch));
        Biome target = chooseTarget(ch, fam, currentLand, currentOcean, currentRiver);
        if (shouldSkipSpoof(ch)) {
            if (spoofed.remove(k)) revertChunk(ch);
            lastApplied.remove(k);
            familyCache.remove(k);
            return budget;
        }
        Biome last = lastApplied.get(k);
        if (last == target) return budget;
        if (last == null && isChunkAtTarget(ch, target)) {
            lastApplied.put(k, target);
            return budget;
        }
        Biome[] old = captureAndApply(ch, target);
        if (old != null) {
            if (!backups.containsKey(k)) backups.put(k, old);
            if (backupStore.isDiskBackupEnabled()) {
                backupStore.saveChunkFromGrid(w, ch.getX(), ch.getZ(), old, STEP_XZ, STEP_Y);
            }
            spoofed.add(k);
            lastApplied.put(k, target);
            return budget - 1;
        }
        return budget;
    }

    private boolean isChunkExcluded(Chunk ch) {
        if (excludedBiomes.isEmpty()) return false;
        long k = key(ch);
        Boolean cached = excludedCache.get(k);
        if (cached != null) return cached;
        boolean result = false;
        Biome[] old = backups.get(k);
        if (old != null) {
            for (Biome b : old) {
                if (excludedBiomes.contains(b)) {
                    result = true;
                    break;
                }
            }
        } else {
            World w = ch.getWorld();
            int bx = ch.getX() << 4, bz = ch.getZ() << 4;
            int minY = w.getMinHeight(), maxY = w.getMaxHeight();
            for (int x = 0; x < 16; x += 8) {
                for (int z = 0; z < 16; z += 8) {
                    for (int y = minY; y < maxY; y += 32) {
                        if (excludedBiomes.contains(w.getBiome(bx + x, y, bz + z))) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        excludedCache.put(k, result);
        return result;
    }

    private Family classifyFamily(Chunk ch) {
        long k = key(ch);
        Biome[] old = backups.get(k);
        if (old != null && old.length > 0) {
            return classifyFromSamples(Arrays.asList(old));
        }
        World w = ch.getWorld();
        int bx = ch.getX() << 4, bz = ch.getZ() << 4;
        int minY = w.getMinHeight(), maxY = w.getMaxHeight();
        List<Biome> samples = new ArrayList<>();
        for (int x = 0; x < 16; x += 8) {
            for (int z = 0; z < 16; z += 8) {
                for (int y = minY; y < maxY; y += 32) {
                    samples.add(w.getBiome(bx + x, y, bz + z));
                }
            }
        }
        return classifyFromSamples(samples);
    }

    private Family classifyFromSamples(Iterable<Biome> samples) {
        int river = 0, ocean = 0, shore = 0, land = 0;
        for (Biome b : samples) {
            if (b == null) continue;
            if (riversEnabled && isRiver(b)) river++;
            else if (oceansEnabled && isOcean(b)) ocean++;
            else if (oceansEnabled && isShore(b)) shore++;
            else land++;
        }
        int waterish = ocean + shore + river;
        int oceanish = ocean + shore;
        if (riversEnabled && river > 0) {
            if (river >= 2) return Family.RIVER;
            if (river >= oceanish && river >= land) return Family.RIVER;
        }
        if (oceansEnabled && oceanish > 0) {
            if (ocean > 0 && oceanish > land) return Family.OCEAN;
            if (ocean >= 2 && oceanish >= land) return Family.OCEAN;
        }
        if (land >= waterish) return Family.LAND;
        if (riversEnabled && river > 0 && river >= ocean) return Family.RIVER;
        if (oceansEnabled && oceanish > 0) return Family.OCEAN;
        return Family.LAND;
    }

    private static boolean isOcean(Biome b) {
        return b.name().contains("OCEAN");
    }

    private static boolean isRiver(Biome b) {
        return b.name().contains("RIVER");
    }

    private static boolean isShore(Biome b) {
        String n = b.name();
        return n.contains("BEACH") || n.contains("SHORE");
    }

    private static boolean isDeepOcean(Biome b) {
        return b.name().contains("DEEP_") && isOcean(b);
    }

    private Biome chooseTarget(Chunk ch, Family family, Biome currentLand, Biome currentOcean, Biome currentRiver) {
        if (family == Family.RIVER && riversEnabled) {
            return currentRiver;
        }
        if (family == Family.OCEAN && oceansEnabled) {
            Biome ocean = currentOcean;
            Biome origOcean = getRepresentativeOcean(ch);
            if (isOcean(origOcean) && oceansKeepDeepVariants && isDeepOcean(origOcean)) {
                String name = ocean.name();
                if (!name.startsWith("DEEP_")) {
                    try {
                        return Biome.valueOf("DEEP_" + name);
                    } catch (IllegalArgumentException ignored) {
                        return ocean;
                    }
                }
            }
            return ocean;
        }
        return currentLand;
    }

    private Biome getRepresentativeOcean(Chunk ch) {
        long k = key(ch);
        Biome[] old = backups.get(k);
        if (old != null) {
            for (Biome b : old) {
                if (isOcean(b)) return b;
            }
            return old[0];
        }
        World w = ch.getWorld();
        int bx = ch.getX() << 4, bz = ch.getZ() << 4;
        int minY = w.getMinHeight();
        for (int x = 0; x < 16; x += 8) {
            for (int z = 0; z < 16; z += 8) {
                Biome b = w.getBiome(bx + x, minY, bz + z);
                if (isOcean(b)) return b;
            }
        }
        return Biome.OCEAN;
    }

    private boolean isChunkAtTarget(Chunk ch, Biome target) {
        World w = ch.getWorld();
        int bx = ch.getX() << 4, bz = ch.getZ() << 4;
        int minY = w.getMinHeight(), maxY = w.getMaxHeight();
        for (int x = 0; x < 16; x += 8) {
            for (int z = 0; z < 16; z += 8) {
                for (int y = minY; y < maxY; y += 32) {
                    if (w.getBiome(bx + x, y, bz + z) != target) return false;
                }
            }
        }
        return true;
    }

    private static boolean isColdBiome(Biome b) {
        if (b == Biome.CHERRY_GROVE) return false;
        String n = b.name();
        return n.contains("SNOW") || n.contains("FROZEN") || n.contains("ICE")
            || "GROVE".equals(n) || n.contains("SNOWY_TAIGA") || n.contains("PEAK") || n.contains("MOUNTAIN");
    }

    private boolean shouldSkipSpoof(Chunk ch) {
        if (!respectNaturallySnowyBiomes) return false;
        long k = key(ch);
        if (COLD_CHUNKS.contains(k)) return true;
        if (backups.containsKey(k)) return false;
        World w = ch.getWorld();
        int bx = ch.getX() << 4, bz = ch.getZ() << 4;
        int minY = w.getMinHeight(), maxY = w.getMaxHeight();
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                for (int y = minY; y < maxY; y += 32) {
                    if (isColdBiome(w.getBiome(bx + x, y, bz + z))) {
                        COLD_CHUNKS.add(k);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Capture current biomes in (y,z,x) order (matches BiomeBackupData), then apply target.
     * Returns the captured array for backup/store, or null if no change.
     */
    private Biome[] captureAndApply(Chunk ch, Biome target) {
        try {
            World w = ch.getWorld();
            int bx = ch.getX() << 4, bz = ch.getZ() << 4;
            int minY = w.getMinHeight(), maxY = w.getMaxHeight();
            List<Biome> prevList = new ArrayList<>();
            boolean anyChange = false;
            int stepXZ = STEP_XZ, stepY = STEP_Y;
            int sx = (16 + stepXZ - 1) / stepXZ;
            int sz = (16 + stepXZ - 1) / stepXZ;
            int sy = (maxY - minY + stepY) / stepY;
            for (int oy = 0; oy < sy; oy++) {
                int y = minY + oy * stepY;
                for (int oz = 0; oz < sz; oz++) {
                    int z = bz + oz * stepXZ;
                    for (int ox = 0; ox < sx; ox++) {
                        int x = bx + ox * stepXZ;
                        Biome cur = w.getBiome(x, y, z);
                        prevList.add(cur);
                        if (cur != target) {
                            anyChange = true;
                            w.setBiome(x, y, z, target);
                        }
                    }
                }
            }
            if (!anyChange) return null;
            w.refreshChunk(ch.getX(), ch.getZ());
            Biome[] arr = prevList.toArray(new Biome[0]);
            long k = key(ch);
            for (Biome b : arr) {
                if (isColdBiome(b)) {
                    COLD_CHUNKS.add(k);
                    break;
                }
            }
            return arr;
        } catch (Throwable t) {
            plugin.getLogger().warning("[BiomePainter] Error at " + ch.getX() + "," + ch.getZ() + ": " + t.getMessage());
            return null;
        }
    }

    private void revertChunk(Chunk ch) {
        Biome[] old = backups.get(key(ch));
        if (old == null) return;
        try {
            World w = ch.getWorld();
            int bx = ch.getX() << 4, bz = ch.getZ() << 4;
            int minY = w.getMinHeight(), maxY = w.getMaxHeight();
            int stepXZ = STEP_XZ, stepY = STEP_Y;
            int sx = (16 + stepXZ - 1) / stepXZ;
            int sz = (16 + stepXZ - 1) / stepXZ;
            int i = 0;
            for (int oy = 0; oy < (maxY - minY + stepY) / stepY && i < old.length; oy++) {
                int y = minY + oy * stepY;
                for (int oz = 0; oz < sz && i < old.length; oz++) {
                    int z = bz + oz * stepXZ;
                    for (int ox = 0; ox < sx && i < old.length; ox++) {
                        int x = bx + ox * stepXZ;
                        w.setBiome(x, y, z, old[i++]);
                    }
                }
            }
            w.refreshChunk(ch.getX(), ch.getZ());
        } catch (Throwable t) {
            plugin.getLogger().warning("[BiomePainter] Revert error " + ch.getX() + "," + ch.getZ() + ": " + t.getMessage());
        }
        lastApplied.remove(key(ch));
        familyCache.remove(key(ch));
    }

    private void revertAll() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Chunk ch : w.getLoadedChunks()) {
                if (spoofed.contains(key(ch))) revertChunk(ch);
            }
        }
        spoofed.clear();
        backups.clear();
        lastApplied.clear();
        familyCache.clear();
        excludedCache.clear();
    }

    private long key(Chunk ch) {
        return key(ch.getWorld(), ch.getX(), ch.getZ());
    }

    private long key(World w, int cx, int cz) {
        long k = ((long) cx & 0xFFFFFFFFL) << 32 | ((long) cz & 0xFFFFFFFFL);
        return k ^ w.getUID().getMostSignificantBits() ^ w.getUID().getLeastSignificantBits();
    }

    private enum Family { LAND, OCEAN, RIVER }

    private static final class Offset {
        final int dx, dz, dist;
        final double forwardScore;
        Offset(int dx, int dz, int dist, double forwardScore) {
            this.dx = dx;
            this.dz = dz;
            this.dist = dist;
            this.forwardScore = forwardScore;
        }
    }
}
