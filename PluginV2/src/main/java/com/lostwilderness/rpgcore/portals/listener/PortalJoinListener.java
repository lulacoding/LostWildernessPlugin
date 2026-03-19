package com.lostwilderness.rpgcore.portals.listener;

import com.lostwilderness.rpgcore.portals.PortalEntitySpawner;
import com.lostwilderness.rpgcore.portals.PortalRepository;
import com.lostwilderness.rpgcore.portals.PortalService;
import com.lostwilderness.rpgcore.portals.PortalTeleportHelper;
import com.lostwilderness.rpgcore.portals.PortalRepository.PendingPortalRow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class PortalJoinListener implements Listener {

    private final Plugin plugin;
    private final PortalService portalService;
    private final PortalRepository repo;
    private final boolean isSurvival;

    public PortalJoinListener(Plugin plugin, PortalService portalService, PortalRepository repo) {
        this.plugin = plugin;
        this.portalService = portalService;
        this.repo = repo;
        this.isSurvival = portalService.isSurvival();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        repo.getPendingPortalForPlayer(uuid, isSurvival).thenAccept(row -> {
            if (row == null) return;
            plugin.getServer().getScheduler().runTask(plugin, () -> runPortalBuildAndTeleport(uuid, row));
        });
    }

    private void runPortalBuildAndTeleport(UUID uuid, PendingPortalRow row) {
        plugin.getLogger().info("[portals] Processing pending portal arrival for " + uuid + " (Portal: " + row.portalName() + ")");
        
        repo.getExitLocation(uuid, row.portalName(), isSurvival).thenAccept(exitInfo -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            World w = plugin.getServer().getWorld(row.worldName());
            if (w == null) {
                w = plugin.getServer().getWorlds().stream()
                    .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                    .findFirst().orElse(null);
            }
            if (w == null) return;

            Location frameOrigin;
            Location exitSpot;
            boolean alongX = "x".equals(row.direction());
            int width = row.width();
            int height = row.height();

            if (exitInfo != null && exitInfo.worldName() != null) {
                // Portal already exists on this side in the DB
                World exitWorld = plugin.getServer().getWorld(exitInfo.worldName());
                if (exitWorld == null) exitWorld = w;
                exitSpot = new Location(exitWorld, exitInfo.x(), exitInfo.y(), exitInfo.z());
                
                // We still need frameOrigin to ensure it's built/filled
                frameOrigin = new Location(exitWorld, row.frameX(), row.frameY(), row.frameZ());
                if (row.frameY() <= 0) {
                   frameOrigin.setY(w.getHighestBlockYAt(frameOrigin.getBlockX(), frameOrigin.getBlockZ()));
                }
                plugin.getLogger().info("[portals] Portal already exists. Using stored exit spot: " + exitSpot);
            } else {
                // New portal arrival, need to compute everything
                double targetY = row.frameY();
                
                // Smart placement logic: if in the air, find ground.
                Block bAtY = w.getBlockAt((int)Math.floor(row.frameX()), (int)Math.floor(targetY), (int)Math.floor(row.frameZ()));
                if (bAtY.getType() == Material.AIR) {
                    targetY = w.getHighestBlockYAt((int) Math.floor(row.frameX()), (int) Math.floor(row.frameZ()));
                }
                
                frameOrigin = new Location(w, row.frameX(), targetY, row.frameZ());
                exitSpot = PortalService.computeExitSpot(frameOrigin, alongX, width, height);
                plugin.getLogger().info("[portals] New portal arrival. Computed exit spot: " + exitSpot);
            }

            // Always ensure the physical portal exists
            portalService.buildReturnPortalFrame(frameOrigin, alongX, width, height);
            
            (isSurvival ? repo.updateSurvivalPortal(uuid, row.portalName(), frameOrigin) : repo.updateAmplifiedPortal(uuid, row.portalName(), frameOrigin))
                .thenCompose(v -> repo.updateExitLocation(uuid, row.portalName(), exitSpot))
                .thenRun(() -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Player p = plugin.getServer().getPlayer(uuid);
                    if (p == null || !p.isOnline()) {
                        repo.deletePendingById(row.id());
                        return;
                    }
                    
                    portalService.prepareSafeExit(exitSpot, p.getVehicle(), width, height);
                    PortalTeleportHelper.teleportPlayerAndVehicle(p, exitSpot);

                    repo.getAndClearTransferEntities(uuid, row.portalName())
                        .thenAccept(entityData -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                            if (entityData != null && !entityData.isEmpty()) {
                                plugin.getLogger().info("[portals] Spawning transferred entities for " + p.getName());
                                PortalEntitySpawner.spawnAndMount(plugin, p, exitSpot, entityData);
                            }
                            repo.deletePendingById(row.id());
                            plugin.getLogger().info("[portals] Arrival processing complete for " + p.getName());
                        }));
                }));
        }));
    }
}
