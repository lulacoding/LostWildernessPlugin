package com.lostwilderness.rpgcore.progression.command;

import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.skills.SkillXpService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lets players claim milestone rewards (e.g. AuraSkills XP) from a menu or by
 * command.
 */
public final class V2ClaimCommand implements CommandExecutor {

    private static final Map<String, MilestoneReward> REWARDS = new HashMap<>();

    static {
        // Join & Loyalty
        REWARDS.put("first_join", new MilestoneReward(AchievementKey.MILESTONE_FIRST_JOIN,
                AchievementKey.CLAIMED_FIRST_JOIN, "fighting", 50.0));
        REWARDS.put("join_3_times", new MilestoneReward(AchievementKey.MILESTONE_JOIN_3_TIMES,
                AchievementKey.CLAIMED_JOIN_3_TIMES, "fighting", 25.0));
        REWARDS.put("join_10_times", new MilestoneReward(AchievementKey.MILESTONE_JOIN_10_TIMES,
                AchievementKey.CLAIMED_JOIN_10_TIMES, "fighting", 100.0));
        REWARDS.put("join_25_times", new MilestoneReward(AchievementKey.MILESTONE_JOIN_25_TIMES,
                AchievementKey.CLAIMED_JOIN_25_TIMES, "fighting", 50.0));
        REWARDS.put("join_50_times", new MilestoneReward(AchievementKey.MILESTONE_JOIN_50_TIMES,
                AchievementKey.CLAIMED_JOIN_50_TIMES, "fighting", 75.0));
        REWARDS.put("join_100_times", new MilestoneReward(AchievementKey.MILESTONE_JOIN_100_TIMES,
                AchievementKey.CLAIMED_JOIN_100_TIMES, "fighting", 150.0));
        // Playtime
        REWARDS.put("playtime_1h", new MilestoneReward(AchievementKey.MILESTONE_PLAYTIME_1H,
                AchievementKey.CLAIMED_PLAYTIME_1H, "fighting", 25.0));
        REWARDS.put("playtime_5h", new MilestoneReward(AchievementKey.MILESTONE_PLAYTIME_5H,
                AchievementKey.CLAIMED_PLAYTIME_5H, "fighting", 50.0));
        REWARDS.put("playtime_24h", new MilestoneReward(AchievementKey.MILESTONE_PLAYTIME_24H,
                AchievementKey.CLAIMED_PLAYTIME_24H, "fighting", 100.0));
        REWARDS.put("playtime_100h", new MilestoneReward(AchievementKey.MILESTONE_PLAYTIME_100H,
                AchievementKey.CLAIMED_PLAYTIME_100H, "fighting", 250.0));
        // Seasons
        REWARDS.put("first_season_spring", new MilestoneReward(AchievementKey.MILESTONE_FIRST_SEASON_SPRING,
                AchievementKey.CLAIMED_FIRST_SEASON_SPRING, "farming", 30.0));
        REWARDS.put("first_season_summer", new MilestoneReward(AchievementKey.MILESTONE_FIRST_SEASON_SUMMER,
                AchievementKey.CLAIMED_FIRST_SEASON_SUMMER, "farming", 30.0));
        REWARDS.put("first_season_autumn", new MilestoneReward(AchievementKey.MILESTONE_FIRST_SEASON_AUTUMN,
                AchievementKey.CLAIMED_FIRST_SEASON_AUTUMN, "farming", 30.0));
        REWARDS.put("first_season_winter", new MilestoneReward(AchievementKey.MILESTONE_FIRST_SEASON_WINTER,
                AchievementKey.CLAIMED_FIRST_SEASON_WINTER, "farming", 30.0));
        REWARDS.put("new_year_login", new MilestoneReward(AchievementKey.MILESTONE_NEW_YEAR_LOGIN,
                AchievementKey.CLAIMED_NEW_YEAR_LOGIN, "fighting", 100.0));
        // Deaths
        REWARDS.put("first_death", new MilestoneReward(AchievementKey.MILESTONE_FIRST_DEATH,
                AchievementKey.CLAIMED_FIRST_DEATH, "fighting", 10.0));
        REWARDS.put("deaths_10", new MilestoneReward(AchievementKey.MILESTONE_DEATHS_10,
                AchievementKey.CLAIMED_DEATHS_10, "fighting", 25.0));
        // Clans
        REWARDS.put("joined_clan", new MilestoneReward(AchievementKey.MILESTONE_JOINED_CLAN,
                AchievementKey.CLAIMED_JOINED_CLAN, "fighting", 50.0));
    }

    private final ProgressionService progressionService;
    private final SkillXpService skillXpService;
    private final BetonQuestBridge betonBridge;
    private final SchedulerService scheduler;
    private final Plugin plugin;

    public V2ClaimCommand(ProgressionService progressionService, SkillXpService skillXpService,
            BetonQuestBridge betonBridge, SchedulerService scheduler, Plugin plugin) {
        this.progressionService = progressionService;
        this.skillXpService = skillXpService;
        this.betonBridge = betonBridge;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can claim rewards.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GRAY + "Usage: /v2claim <milestone>");
            sender.sendMessage(ChatColor.GRAY + "Open /milestones for the full list.");
            return true;
        }
        String key = args[0].toLowerCase().replace(' ', '_');
        MilestoneReward reward = REWARDS.get(key);
        if (reward == null) {
            sender.sendMessage(ChatColor.RED + "Unknown milestone. Use /milestones to see all.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        scheduler.runAsync(() -> {
            try {
                if (!progressionService.hasUnlocked(uuid, reward.milestoneKey)) {
                    scheduler.runSync(
                            () -> sender.sendMessage(ChatColor.RED + "You haven't unlocked this milestone yet."));
                    return;
                }
                if (progressionService.hasUnlocked(uuid, reward.claimedKey)) {
                    scheduler.runSync(() -> sender.sendMessage(ChatColor.GRAY + "You already claimed this reward."));
                    return;
                }
                if (skillXpService == null || !skillXpService.isAvailable()) {
                    scheduler.runSync(() -> sender.sendMessage(
                            ChatColor.RED + "Skill rewards are not available right now. Try again later."));
                    return;
                }
                double amount = reward.xpAmount;
                String skillKey = reward.skillKey;
                scheduler.runSync(() -> {
                    if (!skillXpService.grantXp(uuid, skillKey, amount)) {
                        sender.sendMessage(ChatColor.RED + "Could not grant XP right now. Try again in a few seconds.");
                        return;
                    }
                    progressionService.unlock(uuid, reward.claimedKey);
                    sender.sendMessage(ChatColor.GREEN + "Claimed " + (int) amount + " " + skillKey + " XP!");
                    if (betonBridge != null) {
                        betonBridge.onMilestoneClaimed(player, reward.milestoneKey);
                    }
                });
            } catch (Throwable t) {
                plugin.getLogger().warning("v2claim failed: " + t.getMessage());
                scheduler.runSync(() -> sender.sendMessage(ChatColor.RED + "Something went wrong. Check console."));
            }
        });
        return true;
    }

    private record MilestoneReward(String milestoneKey, String claimedKey, String skillKey, double xpAmount) {
    }
}
