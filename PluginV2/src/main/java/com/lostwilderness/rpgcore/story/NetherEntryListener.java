package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;

/**
 * Detects first Nether entry and unlocks MILESTONE_NETHER_PORTAL.
 */
public final class NetherEntryListener implements Listener {

    private final ProgressionService progressionService;
    private final Plugin plugin;

    public NetherEntryListener(ProgressionService progressionService, Plugin plugin) {
        this.progressionService = progressionService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() != World.Environment.NETHER) return;

        progressionService.hasUnlockedAsync(player.getUniqueId(), AchievementKey.MILESTONE_NETHER_PORTAL)
            .thenAccept(already -> {
                if (already) return;
                progressionService.unlock(player.getUniqueId(), AchievementKey.MILESTONE_NETHER_PORTAL);
                plugin.getLogger().info("[story] " + player.getName() + " entered the Nether — MILESTONE_NETHER_PORTAL unlocked.");
            });
    }
}
