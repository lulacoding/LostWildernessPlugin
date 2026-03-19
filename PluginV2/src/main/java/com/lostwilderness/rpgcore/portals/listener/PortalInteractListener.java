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
        portalService.tryToLightPortal(b.getLocation(), player, result -> {
            if (!result) {
                player.sendMessage("§cNo valid crying obsidian frame found. (Min size 4x5, max 7x7)");
            }
        });
    }
}
