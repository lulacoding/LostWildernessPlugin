package com.lostwilderness.rpgcore.portals.listener;

import com.lostwilderness.rpgcore.clans.ClanService;
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

    /**
     * Frame material for the Devoid portal.
     * Crying obsidian = Corrupt Portal (Amplified). Obsidian = Devoid portal.
     * NOTE: Update this if the Devoid portal frame design changes.
     */
    private static final Material DEVOID_FRAME_MATERIAL = Material.OBSIDIAN;

    private static final int CLAN_WITHER_THRESHOLD = 36;
    private static final int SOLO_WITHER_THRESHOLD = 6;

    private final PortalService portalService;
    /** Null when clans module is disabled or failed to load. */
    private final ClanService clanService;

    public PortalInteractListener(PortalService portalService, ClanService clanService) {
        this.portalService = portalService;
        this.clanService = clanService;
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

        // Gate: Devoid portal (obsidian frame) requires Withering Council completion
        if (isDevoidFrame(b)) {
            if (!checkWitherGate(player)) {
                player.sendMessage("§7The darkness does not welcome you.");
                player.sendMessage("§7Your clan must slay 36 Withers — or face them alone (6).");
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

    private boolean isDevoidFrame(Block b) {
        // Check if any adjacent block is the Devoid frame material
        // Simple check: the clicked block itself is DEVOID_FRAME_MATERIAL
        return b.getType() == DEVOID_FRAME_MATERIAL;
    }

    private boolean checkWitherGate(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        com.lostwilderness.rpgcore.core.RPGCorePlugin core =
            com.lostwilderness.rpgcore.core.RPGCorePlugin.getInstance();
        if (core == null) return true; // Fail open if core is not available

        com.lostwilderness.rpgcore.progression.ProgressionService ps =
            core.getService(com.lostwilderness.rpgcore.progression.ProgressionService.class);
        if (ps == null) return true;

        java.util.UUID clanId = clanService != null ? clanService.getClanOfPlayer(uuid) : null;
        if (clanId != null && clanService != null) {
            int kills = clanService.getWitherKills(clanId);
            return kills >= CLAN_WITHER_THRESHOLD;
        } else {
            // Solo player: check personal counter >= 6
            long kills = ps.getCounter(uuid,
                com.lostwilderness.rpgcore.progression.AchievementKey.COUNTER_WITHER_KILLS_SOLO);
            return kills >= SOLO_WITHER_THRESHOLD;
        }
    }
}
