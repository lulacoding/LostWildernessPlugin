package com.lostwilderness.rpgcore.portals.listener;

import com.lostwilderness.rpgcore.portals.PortalService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class PortalInteractListener implements Listener {

    private final PortalService portalService;

    public PortalInteractListener(PortalService portalService) {
        this.portalService = portalService;
    }

    @EventHandler
    public void onFlintAndSteelUse(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() != Material.FLINT_AND_STEEL) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        if (b == null) return;

        Player player = e.getPlayer();

        // Gate: player must have killed the Ender Dragon to activate a Corrupt Portal
        com.lostwilderness.rpgcore.core.RPGCorePlugin core =
            com.lostwilderness.rpgcore.core.RPGCorePlugin.getInstance();
        if (core != null) {
            com.lostwilderness.rpgcore.progression.ProgressionService ps =
                core.getService(com.lostwilderness.rpgcore.progression.ProgressionService.class);
            if (ps != null && !ps.hasUnlocked(player.getUniqueId(),
                    com.lostwilderness.rpgcore.progression.AchievementKey.MILESTONE_ENDER_DRAGON)) {
                player.sendMessage("§7The portal hums but will not open.");
                player.sendMessage("§7Something is missing...");
                e.setCancelled(true);
                return;
            }
        }

        portalService.tryToLightPortal(b.getLocation(), player, result -> {
            if (!result) {
                player.sendMessage("§cNo valid crying obsidian frame found. (Min size 4x5, max 7x7)");
            }
        });
    }
}
