package com.lostwilderness.rpgcore.progression;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Lightweight bridge from PluginV2 to BetonQuest using commands only.
 * No direct BetonQuest API dependency; all calls are best-effort and no-op
 * when BetonQuest is not installed.
 */
public final class BetonQuestBridge {

    private final Plugin plugin;

    public BetonQuestBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isBetonQuestPresent() {
        return Bukkit.getPluginManager().getPlugin("BetonQuest") != null;
    }

    private void fireEventForPlayer(String playerName, String eventId) {
        if (!isBetonQuestPresent()) {
            return;
        }
        if (playerName == null || playerName.isBlank() || eventId == null || eventId.isBlank()) {
            return;
        }
        String cmd = "bq event " + playerName + " " + eventId;
        Bukkit.getScheduler().runTask(plugin, () ->
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd)
        );
    }

    public void onMilestoneUnlocked(Player player, String milestoneKey) {
        if (player == null || milestoneKey == null) return;
        // Map known milestones to BetonQuest events in default package
        switch (milestoneKey) {
            case AchievementKey.MILESTONE_FIRST_JOIN ->
                fireEventForPlayer(player.getName(), "default.milestone_first_join_unlocked");
            case AchievementKey.MILESTONE_JOIN_3_TIMES ->
                fireEventForPlayer(player.getName(), "default.milestone_join_3_times_unlocked");
            case AchievementKey.MILESTONE_JOIN_10_TIMES ->
                fireEventForPlayer(player.getName(), "default.milestone_join_ten_times_unlocked");
            default -> {
            }
        }
    }

    public void onMilestoneClaimed(Player player, String milestoneKey) {
        if (player == null || milestoneKey == null) return;
        switch (milestoneKey) {
            case AchievementKey.MILESTONE_FIRST_JOIN ->
                fireEventForPlayer(player.getName(), "default.milestone_first_join_claimed");
            case AchievementKey.MILESTONE_JOIN_3_TIMES ->
                fireEventForPlayer(player.getName(), "default.milestone_join_3_times_claimed");
            case AchievementKey.MILESTONE_JOIN_10_TIMES ->
                fireEventForPlayer(player.getName(), "default.milestone_join_ten_times_claimed");
            default -> {
            }
        }
    }
}

