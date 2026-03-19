package com.lostwilderness.rpgcore.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records blocks changed by events (e.g. water→ice) and restores them on end or forceEnd.
 * In-memory only; no crash persistence.
 */
public final class BlockRestoreManager {

    private final Plugin plugin;
    private final Map<String, List<Record>> byTag = new ConcurrentHashMap<>();

    public BlockRestoreManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void record(String tag, Block block) {
        byTag.computeIfAbsent(tag, k -> new ArrayList<>())
            .add(new Record(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getBlockData()));
    }

    public void restoreAndClear(String tag) {
        List<Record> list = byTag.remove(tag);
        if (list == null) return;
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            for (Record r : list) {
                World w = org.bukkit.Bukkit.getWorld(r.worldName);
                if (w == null) continue;
                Block b = w.getBlockAt(r.x, r.y, r.z);
                try {
                    b.setBlockData(r.blockData, false);
                } catch (Exception ignored) {}
            }
        });
    }

    private static final class Record {
        final String worldName;
        final int x, y, z;
        final BlockData blockData;

        Record(String worldName, int x, int y, int z, BlockData blockData) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockData = blockData;
        }
    }
}
