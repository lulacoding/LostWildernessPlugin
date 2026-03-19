package com.lostwilderness.lobbyv2.personality;

import com.lostwilderness.rpgcore.personality.TraitRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Triggers personality quiz for first-time players and handles chat input.
 */
public class QuizTriggerListener implements Listener {

    private final Plugin plugin;
    private final TraitRepository traitRepository;
    private final QuizSessionManager quizSessionManager;

    public QuizTriggerListener(Plugin plugin, TraitRepository traitRepository, QuizSessionManager quizSessionManager) {
        this.plugin = plugin;
        this.traitRepository = traitRepository;
        this.quizSessionManager = quizSessionManager;
    }

    /**
     * Check if player has completed quiz on join.
     * If not, trigger quiz after 5-tick delay.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player has completed quiz (async)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            traitRepository.hasCompletedQuiz(uuid).thenAccept(hasCompleted -> {
                if (!hasCompleted) {
                    // Trigger quiz after 5-tick delay (on main thread)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            player.sendMessage("");
                            player.sendMessage("§6§lWelcome to Lost Wilderness!");
                            player.sendMessage("§7Before you begin your journey, we need to learn about you...");
                            player.sendMessage("");

                            // Start quiz after another 3-second delay (60 ticks)
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (player.isOnline()) {
                                    quizSessionManager.startQuiz(player);
                                }
                            }, 60L);
                        }
                    }, 5L);
                }
            }).exceptionally(ex -> {
                plugin.getLogger().severe("Failed to check quiz completion for " + uuid + ": " + ex.getMessage());
                return null;
            });
        });
    }

    /**
     * Handle quiz answer input via chat.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!quizSessionManager.hasActiveSession(uuid)) {
            return; // Not in a quiz
        }

        event.setCancelled(true); // Prevent chat message from broadcasting

        String input = event.getMessage().trim();

        // Pass raw input to quiz manager (will parse and validate there)
        Bukkit.getScheduler().runTask(plugin, () -> {
            quizSessionManager.answerQuestion(player, input);
        });
    }

    /**
     * Clean up quiz session on quit.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        quizSessionManager.cancelSession(event.getPlayer().getUniqueId());
    }

}
