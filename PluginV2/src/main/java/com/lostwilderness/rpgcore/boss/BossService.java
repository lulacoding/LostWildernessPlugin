package com.lostwilderness.rpgcore.boss;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.BlockState;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BossService {

    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final AtomicBoolean diabloActive = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, List<BlockState>> arenaBarriers = new ConcurrentHashMap<>();
    private final Random rng = new Random();

    public BossService(JavaPlugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }

    public void transformToDevoider(Wither w) {
        w.setCustomName("§cDevoider");
        w.setCustomNameVisible(true);
        w.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(300);
        w.setHealth(300);
        w.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20);
        w.setMetadata("devoider", new FixedMetadataValue(plugin, true));
    }

    public void transformToElDiablo(Wither w) {
        w.setCustomName("§4El Diablo, Destroyer of Worlds");
        w.setCustomNameVisible(true);
        w.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(600);
        w.setHealth(600);
        w.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(40);
        w.setMetadata("diablo", new FixedMetadataValue(plugin, true));
        diabloActive.set(true);
    }

    public void createBossArena(Wither w, String displayName) {
        Location loc = w.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        // Constants from RoofWitherListener
        int radiusXZ = 20;
        int heightUp = 12;
        int heightDown = 2;

        Boundary boundary = new Boundary(world,
                cx - radiusXZ, cy - heightDown, cz - radiusXZ,
                cx + radiusXZ, cy + heightUp, cz + radiusXZ);

        List<BlockState> barriers = BossArenaBarriers.placeWallsAndFloor(boundary);
        String arenaId = "boss-" + w.getUniqueId();
        arenaBarriers.put(arenaId, barriers);

        BossArena arena = new BossArena(arenaId, displayName, boundary, true, false);
        arenaManager.register(arena);
        w.setMetadata("arena_id", new FixedMetadataValue(plugin, arenaId));
    }

    public void cleanupBossArena(Wither w) {
        if (!w.hasMetadata("arena_id")) return;

        String arenaId = (String) w.getMetadata("arena_id").get(0).value();
        List<BlockState> barriers = arenaBarriers.remove(arenaId);
        if (barriers != null) {
            BossArenaBarriers.restore(barriers);
        }
        arenaManager.unregister(arenaId);

        if (w.hasMetadata("diablo")) {
            diabloActive.set(false);
        }
    }

    public void spawnInitialHorde(Location loc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = loc.getWorld();
                if (world == null) return;
                List<Player> players = world.getPlayers();
                if (players.isEmpty()) return;

                for (int i = 0; i < 10; i++) {
                    Player target = players.get(rng.nextInt(players.size()));
                    Location spawn = target.getLocation()
                            .clone()
                            .add(rng.nextDouble() * 20 - 10, 0, rng.nextDouble() * 20 - 10);
                    spawn.setY(world.getHighestBlockYAt(spawn));
                    world.spawnEntity(spawn, EntityType.WITHER_SKELETON);
                }
            }
        }.runTask(plugin);
    }

    public boolean isDiabloActive() {
        return diabloActive.get();
    }

    public void setDiabloActive(boolean active) {
        diabloActive.set(active);
    }
}
