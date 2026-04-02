package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Plays a one-time lore intro sequence when a player first joins Survival.
 * Uses FLAG_INTRO_SEEN in the progression DB to ensure it only plays once.
 */
public final class StoryIntroListener implements Listener {

    private final ProgressionService progressionService;
    private final Plugin plugin;

    public StoryIntroListener(ProgressionService progressionService, Plugin plugin) {
        this.progressionService = progressionService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Async check — don't block the join thread
        progressionService.hasUnlockedAsync(player.getUniqueId(), AchievementKey.FLAG_INTRO_SEEN)
            .thenAccept(seen -> {
                if (seen) return;
                // Schedule intro on main thread after a short delay
                Bukkit.getScheduler().runTaskLater(plugin, () -> playIntro(player), 40L);
            });
    }

    private void playIntro(Player player) {
        if (!player.isOnline()) return;

        // Mark seen immediately so it won't re-trigger
        progressionService.unlock(player.getUniqueId(), AchievementKey.FLAG_INTRO_SEEN);

        // Opening title
        player.sendTitle(
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "Lost Wilderness",
            ChatColor.GRAY + "A world torn apart by darkness...",
            20, 80, 20
        );

        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.6f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.4f, 0.7f);

        // Lore lines — staggered with delays
        schedule(player, 120L, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC +
            "Long ago, El Diablo descended from the Nether...");
        schedule(player, 180L, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC +
            "He corrupted the dragons, split the firmament, and seized the portal network.");
        schedule(player, 250L, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC +
            "The Lord, desperate, tore a hole between worlds — and called upon you.");
        schedule(player, 330L, ChatColor.GOLD + "" + ChatColor.ITALIC +
            "You are not from this world. But this world needs you.");
        schedule(player, 400L, () -> {
            if (!player.isOnline()) return;
            player.sendTitle(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "The First Steps",
                ChatColor.GRAY + "Find the traveller by the campfire.",
                10, 60, 20
            );
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
        });
    }

    private void schedule(Player player, long ticks, String message) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) player.sendMessage(message);
        }, ticks);
    }

    private void schedule(Player player, long ticks, Runnable task) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) task.run();
        }, ticks);
    }
}
