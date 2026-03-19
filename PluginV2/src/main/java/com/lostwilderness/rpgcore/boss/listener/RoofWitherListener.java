package com.lostwilderness.rpgcore.boss.listener;

import com.lostwilderness.rpgcore.boss.ArenaManager;
import com.lostwilderness.rpgcore.boss.BossKillRepository;
import com.lostwilderness.rpgcore.boss.BossService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class RoofWitherListener implements Listener {
    private static final int WITHER_KILLS_REQUIRED = 6;
    private static final int DEVOIDER_KILLS_REQUIRED = 6;

    private final JavaPlugin plugin;
    private final boolean amplified;
    private final BossKillRepository repository;
    private final BossService bossService;
    private final ArenaManager arenaManager;

    public RoofWitherListener(JavaPlugin plugin, boolean amplified, BossKillRepository repository, BossService bossService, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.amplified = amplified;
        this.repository = repository;
        this.bossService = bossService;
        this.arenaManager = arenaManager;
        startBossContainmentTask();
    }

    private void startBossContainmentTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (World w : plugin.getServer().getWorlds()) {
                if (w.getEnvironment() != World.Environment.NETHER) continue;
                for (Wither wither : w.getEntitiesByClass(Wither.class)) {
                    if (!wither.hasMetadata("devoider") && !wither.hasMetadata("diablo")) continue;
                    if (!wither.hasMetadata("arena_id")) continue;
                    String aid = (String) wither.getMetadata("arena_id").get(0).value();
                    arenaManager.getById(aid).ifPresent(arena -> {
                        if (!arena.getBoundary().contains(wither)) {
                            Location clamped = arena.getBoundary().clamp(wither.getLocation());
                            if (clamped != null) wither.teleport(clamped);
                        }
                    });
                }
            }
        }, 40L, 40L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onWitherSpawn(EntitySpawnEvent ev) {
        if (!(ev.getEntity() instanceof Wither w)) return;
        Location loc = w.getLocation();
        if (loc.getWorld().getEnvironment() != World.Environment.NETHER) return;
        int blockY = loc.getBlockY();
        if (blockY != 127 && blockY != 128) return;

        if (amplified) {
            if (bossService.isDiabloActive()) {
                ev.setCancelled(true);
                return;
            }
            repository.anyPlayerOrClanHasDevoiderKills(DEVOIDER_KILLS_REQUIRED).thenAccept(eligible -> {
                if (eligible) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        bossService.transformToElDiablo(w);
                        bossService.createBossArena(w, "El Diablo");
                        bossService.spawnInitialHorde(loc);
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> w.remove());
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Error checking diablo eligibility", ex);
                return null;
            });
        } else {
            repository.anyPlayerOrClanHasWitherKills(WITHER_KILLS_REQUIRED).thenAccept(eligible -> {
                if (eligible) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        bossService.transformToDevoider(w);
                        bossService.createBossArena(w, "Devoider");
                        bossService.spawnInitialHorde(loc);
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> w.remove());
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Error checking devoider eligibility", ex);
                return null;
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkullHit(ProjectileHitEvent ev) {
        if (!(ev.getEntity() instanceof WitherSkull skull)) return;
        if (!skull.isCharged()) return;
        if (!(skull.getShooter() instanceof Wither w)) return;
        if (!w.hasMetadata("diablo")) return;

        Block b = skull.getLocation().getBlock();
        if (b.getType() == Material.BEDROCK) {
            b.setType(Material.AIR, false);
        }
    }

    @EventHandler
    public void onWitherDeath(EntityDeathEvent ev) {
        if (!(ev.getEntity() instanceof Wither w)) return;
        bossService.cleanupBossArena(w);
    }
}
