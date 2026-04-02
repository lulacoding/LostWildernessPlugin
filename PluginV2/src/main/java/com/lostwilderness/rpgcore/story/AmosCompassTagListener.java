package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Clears BetonQuest {@code amos_compass_active} when the Thornwell guide compass is dropped, lost on death, or missing after respawn.
 */
public final class AmosCompassTagListener implements Listener {

    private static final String EVENT_COMPASS_LOST = "lw_amos.amos_compass_lost";

    private final Plugin plugin;
    private final BetonQuestBridge betonQuest;

    public AmosCompassTagListener(Plugin plugin, BetonQuestBridge betonQuest) {
        this.plugin = plugin;
        this.betonQuest = betonQuest;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!ThornwellCompassItem.isThornwellCompass(plugin, event.getItemDrop().getItemStack())) {
            return;
        }
        if (betonQuest != null) {
            betonQuest.fireEventForPlayer(event.getPlayer(), EVENT_COMPASS_LOST);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory()) {
            return;
        }
        for (ItemStack stack : event.getDrops()) {
            if (stack != null && stack.getType() == Material.COMPASS
                    && ThornwellCompassItem.isThornwellCompass(plugin, stack)) {
                if (betonQuest != null) {
                    betonQuest.fireEventForPlayer(event.getEntity(), EVENT_COMPASS_LOST);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (!player.isOnline()) return;
            if (betonQuest == null) return;
            if (!betonQuest.hasTag(player.getUniqueId(), "lw_amos.amos_compass_active")) {
                return;
            }
            if (carriesGuideCompass(player)) {
                return;
            }
            betonQuest.fireEventForPlayer(player, EVENT_COMPASS_LOST);
        }, 3L);
    }

    private boolean carriesGuideCompass(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (ThornwellCompassItem.isThornwellCompass(plugin, stack)) {
                return true;
            }
        }
        return ThornwellCompassItem.isThornwellCompass(plugin, player.getInventory().getItemInOffHand());
    }
}
