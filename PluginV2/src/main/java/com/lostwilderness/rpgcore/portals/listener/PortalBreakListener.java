package com.lostwilderness.rpgcore.portals.listener;

import com.lostwilderness.rpgcore.portals.PortalRepository;
import com.lostwilderness.rpgcore.portals.PortalService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

public final class PortalBreakListener implements Listener {

    private final Plugin plugin;
    private final PortalService portalService;
    private final PortalRepository repo;
    private final boolean isSurvival;

    public PortalBreakListener(Plugin plugin, PortalService portalService, PortalRepository repo) {
        this.plugin = plugin;
        this.portalService = portalService;
        this.repo = repo;
        this.isSurvival = portalService.isSurvival();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.CRYING_OBSIDIAN) return;

        PortalService.FrameOrigin origin = portalService.findFrameOriginContaining(block);
        if (origin == null) return;

        PortalService.PortalFrame frame = portalService.findValidPortalFrameAtOrigin(origin.location().getBlock(), origin.alongX());
        if (frame != null) {
            portalService.clearPortalBlocks(origin.location(), origin.alongX(), frame.width(), frame.height());
        }

        org.bukkit.Location loc = origin.location();
        String worldName = loc.getWorld() != null ? loc.getWorld().getName() : null;
        if (worldName == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            repo.findPortalOwnerAtFrame(isSurvival, worldName, x, y, z)
                .thenAccept(owner -> {
                    if (owner != null) {
                        repo.deletePortalByOwnerAndName(owner.playerUuid(), owner.portalName());
                    }
                });
        });
    }
}
