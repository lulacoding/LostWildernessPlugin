package com.lostwilderness.rpgcore.world;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Aeternum-style biome backup store: save chunk biomes to disk (palette + indices),
 * restore when chunk loads in "restore" mode. Use startRestoreAll(sender, budget) to begin
 * gradual restore; restore runs as chunks load, up to budget chunks per tick.
 */
public final class BiomeBackupStore implements Listener {

    private static final int DEFAULT_STEP_XZ = 4;
    private static final int DEFAULT_STEP_Y = 8;

    private final Plugin plugin;
    private final File rootDir;
    private final AtomicBoolean diskBackupEnabled = new AtomicBoolean(true);
    private final AtomicBoolean restoreMode = new AtomicBoolean(false);
    private final AtomicBoolean paintingEnabled = new AtomicBoolean(false);
    private volatile int restoreBudgetPerTick = 6;
    private final ConcurrentLinkedQueue<ChunkKey> restoreQueue = new ConcurrentLinkedQueue<>();
    private BukkitRunnable restoreTask;

    public BiomeBackupStore(Plugin plugin) {
        this.plugin = plugin;
        this.rootDir = new File(plugin.getDataFolder(), "biome_backups");
    }

    public void setDiskBackupEnabled(boolean enabled) {
        diskBackupEnabled.set(enabled);
    }

    public boolean isDiskBackupEnabled() {
        return diskBackupEnabled.get();
    }

    public boolean isRestoreMode() {
        return restoreMode.get();
    }

    public void setPaintingEnabled(boolean enabled) {
        paintingEnabled.set(enabled);
    }

    public boolean isPaintingEnabled() {
        return paintingEnabled.get();
    }

    /** Start restore: disable painting, enqueue chunks as they load; process up to budgetPerTick per tick. */
    public void startRestoreAll(org.bukkit.command.CommandSender sender, int budgetPerTick) {
        restoreMode.set(true);
        this.restoreBudgetPerTick = Math.max(1, budgetPerTick);
        if (restoreTask != null) restoreTask.cancel();
        restoreTask = new RestoreTask();
        restoreTask.runTaskTimer(plugin, 20L, 1L);
        if (sender != null) {
            sender.sendMessage("[LW] Biome restore started. Load chunks (fly/walk) to restore; " + budgetPerTick + " chunks/tick. Do not remove plugin data until restore is done.");
        }
        plugin.getLogger().info("[BiomeBackup] Restore mode ON. Load affected chunks to restore; budget=" + budgetPerTick + "/tick.");
    }

    /** Stop restore mode and cancel the restore task. */
    public void stopRestore() {
        restoreMode.set(false);
        if (restoreTask != null) {
            restoreTask.cancel();
            restoreTask = null;
        }
        restoreQueue.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!restoreMode.get()) return;
        Chunk chunk = e.getChunk();
        ChunkKey key = new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
        if (hasBackup(key)) {
            restoreQueue.offer(key);
        }
    }

    private void processRestoreQueue() {
        if (!restoreMode.get() || restoreQueue.isEmpty()) return;
        int budget = restoreBudgetPerTick;
        while (budget > 0) {
            ChunkKey key = restoreQueue.poll();
            if (key == null) break;
            World w = plugin.getServer().getWorld(key.worldName);
            if (w != null && w.isChunkLoaded(key.cx, key.cz)) {
                restoreChunk(w, key.cx, key.cz);
                budget--;
            }
        }
    }

    public void saveChunk(World world, int chunkX, int chunkZ) {
        if (!diskBackupEnabled.get()) return;
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        BiomeBackupData data = BiomeBackupData.fromChunk(chunk, DEFAULT_STEP_XZ, DEFAULT_STEP_Y);
        saveBackupToFile(world.getName(), chunkX, chunkZ, data);
    }

    /**
     * Save a pre-captured biome grid to disk (e.g. from painter before applying season biomes).
     * Grid order must be (y, z, x) sample points — same as BiomeBackupData.fromChunk.
     */
    public void saveChunkFromGrid(World world, int chunkX, int chunkZ, org.bukkit.block.Biome[] grid, int stepXZ, int stepY) {
        if (!diskBackupEnabled.get()) return;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        BiomeBackupData data = BiomeBackupData.fromGrid(grid, stepXZ, stepY, minY, maxY);
        if (data == null) return;
        saveBackupToFile(world.getName(), chunkX, chunkZ, data);
    }

    private void saveBackupToFile(String worldName, int chunkX, int chunkZ, BiomeBackupData data) {
        File f = fileFor(worldName, chunkX, chunkZ);
        try {
            if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
            YamlConfiguration y = new YamlConfiguration();
            y.set("stepXZ", data.getStepXZ());
            y.set("stepY", data.getStepY());
            y.set("minY", data.getMinY());
            y.set("maxY", data.getMaxY());
            y.set("palette", data.getPalette());
            y.set("indices", data.getIndices());
            y.save(f);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save biome backup " + f, ex);
        }
    }

    public BiomeBackupData loadChunkBackup(String worldName, int chunkX, int chunkZ) {
        File f = fileFor(worldName, chunkX, chunkZ);
        if (!f.exists()) return null;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
        int stepXZ = y.getInt("stepXZ", DEFAULT_STEP_XZ);
        int stepY = y.getInt("stepY", DEFAULT_STEP_Y);
        int minY = y.getInt("minY", -64);
        int maxY = y.getInt("maxY", 320);
        List<String> pal = y.getStringList("palette");
        List<Integer> ind = y.getIntegerList("indices");
        if (pal == null || ind == null) return null;
        String[] palette = pal.toArray(new String[0]);
        int[] indices = ind.stream().mapToInt(Integer::intValue).toArray();
        return new BiomeBackupData(stepXZ, stepY, minY, maxY, palette, indices);
    }

    public boolean hasBackup(ChunkKey key) {
        return fileFor(key.worldName, key.cx, key.cz).exists();
    }

    public boolean hasBackup(String worldName, int chunkX, int chunkZ) {
        return fileFor(worldName, chunkX, chunkZ).exists();
    }

    /** Restore one chunk from backup. Chunk must be loaded. */
    public void restoreChunk(World world, int chunkX, int chunkZ) {
        BiomeBackupData data = loadChunkBackup(world.getName(), chunkX, chunkZ);
        if (data == null) return;
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        data.applyToChunk(chunk);
    }

    /** Get original biome at position if we have a backup for that chunk (for spawn guard etc.). */
    public org.bukkit.block.Biome getOriginalBiomeApproxOrNull(World w, int blockX, int blockY, int blockZ) {
        int cx = blockX >> 4, cz = blockZ >> 4;
        BiomeBackupData data = loadChunkBackup(w.getName(), cx, cz);
        if (data == null) return null;
        int relX = blockX & 15, relZ = blockZ & 15;
        String key = data.getBiomeKeyAt(relX, blockY, relZ);
        if (key == null) return null;
        try {
            return org.bukkit.block.Biome.valueOf(key.replace("minecraft:", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private File fileFor(String worldName, int chunkX, int chunkZ) {
        File worldDir = new File(rootDir, worldName.replace(':', '_'));
        return new File(worldDir, chunkX + "_" + chunkZ + ".yml");
    }

    public static final class ChunkKey {
        public final String worldName;
        public final int cx, cz;

        public ChunkKey(String worldName, int cx, int cz) {
            this.worldName = worldName;
            this.cx = cx;
            this.cz = cz;
        }
    }

    /** Aeternum-style restore task: processes queue with budget per tick. */
    private final class RestoreTask extends BukkitRunnable {
        @Override
        public void run() {
            processRestoreQueue();
        }
    }
}
