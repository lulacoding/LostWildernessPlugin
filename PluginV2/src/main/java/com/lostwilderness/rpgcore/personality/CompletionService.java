package com.lostwilderness.rpgcore.personality;

import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Calculates player completion percentage (0-300%).
 *
 * Breakdown:
 * - 0-100%: Story milestones (Nether, Dragon, Wither, Diablo, etc.)
 * - 100-200%: Vanilla advancements (bonus layer)
 * - 200-300%: Elemental temples (bonus layer)
 */
public class CompletionService {

    private final ProgressionService progressionService;

    // Story milestone weights (total = 100%)
    private static final int NETHER_PORTAL = 10;
    private static final int ENDER_DRAGON = 20;
    private static final int WITHER_36 = 30;
    private static final int DEVOIDER_6 = 40;
    private static final int EL_DIABLO = 50;
    private static final int HEAVENLY_TOWER = 70;
    private static final int LORDS_PLATEAU = 100;

    public CompletionService(ProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    /**
     * Calculate player's total completion percentage (0-300%).
     */
    public int getCompletionPercent(UUID uuid) {
        int storyPercent = calculateStoryPercent(uuid);
        int advancementPercent = calculateAdvancementPercent(uuid);
        int elementalPercent = calculateElementalPercent(uuid);

        return storyPercent + advancementPercent + elementalPercent;
    }

    /**
     * Calculate story milestone completion (0-100%).
     */
    private int calculateStoryPercent(UUID uuid) {
        List<String> unlocked = progressionService.getUnlockedKeys(uuid);

        int percent = 0;

        // First join milestone (required baseline)
        if (unlocked.contains(AchievementKey.MILESTONE_FIRST_JOIN)) {
            percent = 5; // Base 5% for joining
        }

        // Progression milestones
        if (unlocked.contains(AchievementKey.MILESTONE_NETHER_PORTAL)) {
            percent = Math.max(percent, NETHER_PORTAL);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_ENDER_DRAGON)) {
            percent = Math.max(percent, ENDER_DRAGON);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_WITHER_36)) {
            percent = Math.max(percent, WITHER_36);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_DEVOIDER_6)) {
            percent = Math.max(percent, DEVOIDER_6);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_EL_DIABLO)) {
            percent = Math.max(percent, EL_DIABLO);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_HEAVENLY_TOWER)) {
            percent = Math.max(percent, HEAVENLY_TOWER);
        }
        if (unlocked.contains(AchievementKey.MILESTONE_LORDS_PLATEAU)) {
            percent = Math.max(percent, LORDS_PLATEAU);
        }

        return percent;
    }

    /**
     * Calculate vanilla advancement completion (0-100% bonus).
     * Uses Bukkit's AdvancementProgress API.
     */
    private int calculateAdvancementPercent(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return 0;

        int totalAdvancements = 0;
        int completedAdvancements = 0;

        // Iterate through all server advancements
        Iterator<org.bukkit.advancement.Advancement> iterator = Bukkit.getServer().advancementIterator();
        while (iterator.hasNext()) {
            org.bukkit.advancement.Advancement advancement = iterator.next();
            // Skip recipe advancements (too many, not meaningful)
            if (advancement.getKey().getKey().startsWith("recipes/")) {
                continue;
            }

            totalAdvancements++;

            org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                completedAdvancements++;
            }
        }

        if (totalAdvancements == 0) return 0;

        // Scale to 0-100% bonus
        return (completedAdvancements * 100) / totalAdvancements;
    }

    /**
     * Calculate elemental temple completion (0-100% bonus).
     * 5 temples × 20% each = 100%
     */
    private int calculateElementalPercent(UUID uuid) {
        List<String> unlocked = progressionService.getUnlockedKeys(uuid);

        int completedTemples = 0;

        if (unlocked.contains(AchievementKey.MILESTONE_TEMPLE_FIRE)) {
            completedTemples++;
        }
        if (unlocked.contains(AchievementKey.MILESTONE_TEMPLE_EARTH)) {
            completedTemples++;
        }
        if (unlocked.contains(AchievementKey.MILESTONE_TEMPLE_WIND)) {
            completedTemples++;
        }
        if (unlocked.contains(AchievementKey.MILESTONE_TEMPLE_WATER)) {
            completedTemples++;
        }
        if (unlocked.contains(AchievementKey.MILESTONE_TEMPLE_AETHER)) {
            completedTemples++;
        }

        return completedTemples * 20; // 20% per temple
    }
}
