package com.lostwilderness.rpgcore.classes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Claims pending class starter XP after join, when AuraSkills user data is
 * loaded.
 */
public final class ClassStarterXpJoinListener implements Listener {

    private static final long INITIAL_DELAY_TICKS = 20L;
    private static final long RETRY_INTERVAL_TICKS = 20L;
    private static final int MAX_ATTEMPTS = 5;

    private final Plugin plugin;
    private final ClassService classService;

    public ClassStarterXpJoinListener(Plugin plugin, ClassService classService) {
        this.plugin = plugin;
        this.classService = classService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        scheduleAttempt(uuid, INITIAL_DELAY_TICKS, 1);
    }

    private void scheduleAttempt(UUID playerUuid, long delayTicks, int attempt) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player == null || !player.isOnline()) {
                return;
            }

            classService.tryApplyPendingStarterXp(playerUuid).thenAccept(claimed -> {
                if (Boolean.TRUE.equals(claimed)) {
                    return;
                }
                if (attempt >= MAX_ATTEMPTS) {
                    return;
                }
                scheduleAttempt(playerUuid, RETRY_INTERVAL_TICKS, attempt + 1);
            }).exceptionally(ex -> {
                plugin.getLogger().warning("[classes] Failed starter XP claim attempt for " + playerUuid + ": "
                        + ex.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    scheduleAttempt(playerUuid, RETRY_INTERVAL_TICKS, attempt + 1);
                }
                return null;
            });
        }, delayTicks);
    }
}
