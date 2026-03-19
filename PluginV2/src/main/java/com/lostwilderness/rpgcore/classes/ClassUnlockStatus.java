package com.lostwilderness.rpgcore.classes;

/**
 * A ClassUnlock paired with whether it is currently unlocked for a player.
 * Used for displaying the skill tree to players with their current progress.
 *
 * @param unlock The unlock definition (level requirement, name, description)
 * @param unlocked Whether this unlock is currently available to the player
 * @param currentLevel The player's current mastery level
 */
public record ClassUnlockStatus(ClassUnlock unlock, boolean unlocked, int currentLevel) {

    /**
     * Calculate how many levels away this unlock is.
     * @return Positive number if locked (levels remaining), 0 if unlocked
     */
    public int levelsAway() {
        return unlocked ? 0 : Math.max(0, unlock.requiredLevel() - currentLevel);
    }

    /**
     * Get formatted status prefix for display.
     * @return §a✔ if unlocked, §c✘ if close (within 10 levels), §8✘ if far away
     */
    public String getStatusPrefix() {
        if (unlocked) {
            return "§a✔";
        }
        return (levelsAway() <= 10) ? "§c✘" : "§8✘";
    }

    /**
     * Get formatted line for /class tree display.
     * Example: "§a✔ [Lv.1]  Smite — Lightning AoE"
     * Example: "§c✘ [Lv.20] Radiant Shield — Reflect 10% damage (7 levels away)"
     */
    public String formatForTree() {
        String prefix = getStatusPrefix();
        String level = String.format("[Lv.%-2d]", unlock.requiredLevel());
        String gap = unlocked ? "" : " §7(" + levelsAway() + " levels away)";
        return prefix + " " + level + " " + unlock.formatForDisplay() + gap;
    }
}
