package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Auto-reconciliation listener.
 * Scans for nearby pets on player join and re-links them by name.
 */
public final class PetReconciliationListener implements Listener {

    private final PetService petService;
    private final Plugin plugin;

    public PetReconciliationListener(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Run async after a short delay (let player fully load)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            petService.autoReconcile(event.getPlayer().getUniqueId(), event.getPlayer().getLocation())
                .exceptionally(ex -> {
                    plugin.getLogger().warning("[pets] Auto-reconciliation failed for " +
                        event.getPlayer().getName() + ": " + ex.getMessage());
                    return null;
                });
        }, 20L); // 1 second delay
    }
}
