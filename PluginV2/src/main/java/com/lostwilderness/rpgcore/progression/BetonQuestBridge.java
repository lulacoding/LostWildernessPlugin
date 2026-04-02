package com.lostwilderness.rpgcore.progression;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.database.PlayerData;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

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

    /**
     * Fires a BetonQuest event for an online player (full id e.g. {@code lw_amos.thornwell_arrived}).
     */
    public void fireEventForPlayer(Player player, String packageDotEventId) {
        if (player == null) return;
        fireEventForPlayerByName(player.getName(), packageDotEventId);
    }

    private void fireEventForPlayerByName(String playerName, String eventId) {
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
                fireEventForPlayerByName(player.getName(), "default.milestone_first_join_unlocked");
            case AchievementKey.MILESTONE_JOIN_3_TIMES ->
                fireEventForPlayerByName(player.getName(), "default.milestone_join_3_times_unlocked");
            case AchievementKey.MILESTONE_JOIN_10_TIMES ->
                fireEventForPlayerByName(player.getName(), "default.milestone_join_ten_times_unlocked");
            default -> {
            }
        }
    }

    /**
     * Check whether a player has a BetonQuest tag.
     *
     * @param uuid    player UUID (must be online)
     * @param fullTag e.g. "lw_elder.elder_intro_done"
     * @return true if the player has the tag, false if BQ is absent or player is offline
     */
    public boolean hasTag(UUID uuid, String fullTag) {
        if (!isBetonQuestPresent()) return false;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;
        try {
            PlayerData data = BetonQuest.getInstance().getPlayerData(
                    PlayerConverter.getID(player));
            return data != null && data.hasTag(fullTag);
        } catch (Exception e) {
            return false;
        }
    }

    public void onMilestoneClaimed(Player player, String milestoneKey) {
        if (player == null || milestoneKey == null) return;
        switch (milestoneKey) {
            case AchievementKey.MILESTONE_FIRST_JOIN ->
                fireEventForPlayerByName(player.getName(), "default.milestone_first_join_claimed");
            case AchievementKey.MILESTONE_JOIN_3_TIMES ->
                fireEventForPlayerByName(player.getName(), "default.milestone_join_3_times_claimed");
            case AchievementKey.MILESTONE_JOIN_10_TIMES ->
                fireEventForPlayerByName(player.getName(), "default.milestone_join_ten_times_claimed");
            default -> {
            }
        }
    }
}

