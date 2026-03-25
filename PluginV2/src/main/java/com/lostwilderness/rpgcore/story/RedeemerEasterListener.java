package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.calendar.CalendarDayAdvancedEvent;
import com.lostwilderness.rpgcore.calendar.EasterWeekHelper;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.reputation.Faction;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.time.LocalDate;
import java.util.UUID;

/**
 * On Easter Sunday, checks if any online players have killed the Ender Dragon
 * and have positive Celestial rep. If so, announces The Redeemer's arrival.
 *
 * Also handles the Heaven's Gate reveal after Father of Ender is slain.
 *
 * Citizens NPC placement is done manually in-game; this class handles
 * the detection + notification logic.
 */
public final class RedeemerEasterListener implements Listener {

    private final ProgressionService progressionService;
    private final ReputationService reputationService;
    private final Plugin plugin;

    public RedeemerEasterListener(ProgressionService progressionService,
                                   ReputationService reputationService,
                                   Plugin plugin) {
        this.progressionService = progressionService;
        this.reputationService = reputationService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDayAdvanced(CalendarDayAdvancedEvent event) {
        LocalDate newDate = event.getNewSnapshot().date();
        int year = newDate.getYear();
        LocalDate easterSunday = EasterWeekHelper.computeEasterSunday(year);

        if (!newDate.equals(easterSunday)) return;

        // It's Easter Sunday — check who qualifies for The Redeemer
        plugin.getLogger().info("[story] Easter Sunday detected — checking for Redeemer eligibility.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            checkAndNotifyPlayer(player, uuid);
        }
    }

    private void checkAndNotifyPlayer(Player player, UUID uuid) {
        boolean hasDragonKill = progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_ENDER_DRAGON);
        if (!hasDragonKill) return;

        // Check Celestial rep asynchronously
        reputationService.getPointsAsync(uuid, Faction.CELESTIAL).thenAccept(celestialRep -> {
            if (celestialRep <= 0) return; // Evil path — no Redeemer

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                // Set pending flag (used by Citizens NPC dialogue conditions)
                progressionService.unlock(uuid, AchievementKey.FLAG_REDEEMER_PENDING);

                // Notify the player
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + ChatColor.BOLD +
                    "The Redeemer has arrived at the Spawn Village.");
                player.sendMessage(ChatColor.GRAY + "  He has much to tell you about the Corrupt Portal...");
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 1.2f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);

                // Title
                player.sendTitle(
                    ChatColor.GOLD + "" + ChatColor.BOLD + "The Redeemer Arrives",
                    ChatColor.YELLOW + "Return to spawn. He awaits.",
                    10, 80, 20
                );

                // Also check Heaven's Gate condition
                boolean hasFatherOfEnder = progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_FATHER_OF_ENDER);
                if (hasFatherOfEnder && !progressionService.hasUnlocked(uuid, AchievementKey.FLAG_HEAVENS_GATE_PENDING)) {
                    progressionService.unlock(uuid, AchievementKey.FLAG_HEAVENS_GATE_PENDING);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!player.isOnline()) return;
                        player.sendMessage(ChatColor.AQUA + "✦ " + ChatColor.WHITE + ChatColor.BOLD +
                            "The Redeemer also speaks of Heaven's Gate...");
                        player.sendMessage(ChatColor.GRAY + "  A portal in the Amplified world. Y=602.");
                    }, 60L);
                }
            });
        });
    }
}
