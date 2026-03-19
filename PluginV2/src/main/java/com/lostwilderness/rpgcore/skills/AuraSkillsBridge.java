package com.lostwilderness.rpgcore.skills;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges PluginV2 to AuraSkills when the plugin is present.
 * Use for granting XP on milestones and checking skill levels for gates.
 * AuraSkills only has data for online players; offline players get 0 / no-op.
 * API is resolved lazily so we work even when RPG_Core_V2 enables before
 * AuraSkills has finished init.
 */
public final class AuraSkillsBridge {

    private final Plugin plugin;
    private volatile AuraSkillsApi api;
    private volatile boolean loggedFailure;

    public AuraSkillsBridge(Plugin plugin) {
        this.plugin = plugin;
        // Resolve AuraSkills API lazily on first real use; AuraSkills may not have
        // finished initializing its API at the moment we enable this plugin.
        plugin.getLogger().info("[skills] AuraSkills bridge created; will initialize API on first use.");
    }

    /**
     * Tries to resolve the API if not yet set. When logWhenReady is true, logs when
     * we resolve it (e.g. on first use after late init).
     */
    private void tryInit(boolean logWhenReady) {
        if (api != null)
            return;
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") == null)
            return;
        try {
            AuraSkillsApi instance = AuraSkillsApi.get();
            if (instance != null) {
                api = instance;
                if (logWhenReady) {
                    plugin.getLogger().info("[skills] AuraSkills API ready; bridge active.");
                }
            }
        } catch (Throwable t) {
            // API not initialized yet or incompatible – log once so we can see why
            if (!loggedFailure) {
                loggedFailure = true;
                plugin.getLogger()
                        .warning("[skills] AuraSkillsApi.get() failed; skill rewards disabled until this is fixed.");
                plugin.getLogger().warning("[skills] " + t.getClass().getName() + ": " + t.getMessage());
                t.printStackTrace();
            }
        }
    }

    public boolean isAvailable() {
        tryInit(true);
        return api != null;
    }

    /**
     * Grant skill XP. Only works for online players; no-op if AuraSkills
     * unavailable or player offline.
     *
     * @param playerUuid player UUID
     * @param skillKey   lowercase skill name, e.g. "farming", "mining", "fighting"
     * @param amount     XP to add
     */
    public boolean addSkillXp(UUID playerUuid, String skillKey, double amount) {
        tryInit(true);
        if (api == null || amount <= 0)
            return false;
        Skills skill = parseSkill(skillKey);
        if (skill == null)
            return false;
        try {
            SkillsUser user = api.getUser(playerUuid);
            if (user != null) {
                user.addSkillXp(skill, amount);
                plugin.getLogger().info(
                        "[skills] Granted " + amount + " " + skillKey + " XP via AuraSkills for " + playerUuid + ".");
                return true;
            } else {
                plugin.getLogger().warning(
                        "[skills] AuraSkills returned no user for " + playerUuid + " (player may not be loaded yet).");
                return false;
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("AuraSkills addSkillXp failed: " + t.getMessage());
            return false;
        }
    }

    /**
     * Grant multiple skill XP amounts in one operation.
     * Returns false before granting anything if AuraSkills/user/skill keys are
     * invalid.
     */
    public boolean addSkillXpBatch(UUID playerUuid, Map<String, Double> grants) {
        tryInit(true);
        if (api == null || grants == null || grants.isEmpty()) {
            return false;
        }

        try {
            SkillsUser user = api.getUser(playerUuid);
            if (user == null) {
                plugin.getLogger().warning("[skills] AuraSkills returned no user for " + playerUuid
                        + " (player may not be loaded yet).");
                return false;
            }

            Map<Skills, Double> parsed = new LinkedHashMap<>();
            for (Map.Entry<String, Double> entry : grants.entrySet()) {
                String skillKey = entry.getKey();
                double amount = entry.getValue() == null ? 0.0 : entry.getValue();
                if (amount <= 0) {
                    continue;
                }
                Skills skill = parseSkill(skillKey);
                if (skill == null) {
                    plugin.getLogger().warning("[skills] Unknown AuraSkills skill key: " + skillKey);
                    return false;
                }
                parsed.merge(skill, amount, Double::sum);
            }

            if (parsed.isEmpty()) {
                return false;
            }

            for (Map.Entry<Skills, Double> entry : parsed.entrySet()) {
                user.addSkillXp(entry.getKey(), entry.getValue());
            }
            plugin.getLogger().info("[skills] Granted " + parsed.size() + " skill XP entries via AuraSkills for "
                    + playerUuid + ".");
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("AuraSkills addSkillXpBatch failed: " + t.getMessage());
            return false;
        }
    }

    /**
     * Get skill level. Returns 0 if AuraSkills unavailable, player offline, or
     * unknown skill.
     */
    public int getSkillLevel(UUID playerUuid, String skillKey) {
        tryInit(true);
        if (api == null)
            return 0;
        Skills skill = parseSkill(skillKey);
        if (skill == null)
            return 0;
        try {
            SkillsUser user = api.getUser(playerUuid);
            return user != null ? user.getSkillLevel(skill) : 0;
        } catch (Throwable t) {
            plugin.getLogger().warning("AuraSkills getSkillLevel failed: " + t.getMessage());
            return 0;
        }
    }

    /**
     * Returns the player's current mana. If AuraSkills is unavailable or the
     * player is not loaded, returns 0.
     */
    public double getMana(UUID playerUuid) {
        tryInit(true);
        if (api == null) {
            return 0.0;
        }
        try {
            SkillsUser user = api.getUser(playerUuid);
            if (user == null) {
                return 0.0;
            }
            return user.getMana();
        } catch (Throwable t) {
            plugin.getLogger().warning("AuraSkills getMana failed: " + t.getMessage());
            return 0.0;
        }
    }

    /**
     * Attempts to consume the given amount of mana from the player.
     *
     * @return true if mana was successfully consumed, false otherwise.
     */
    public boolean consumeMana(UUID playerUuid, double amount) {
        if (amount <= 0) {
            return true;
        }
        tryInit(true);
        if (api == null) {
            return false;
        }
        try {
            SkillsUser user = api.getUser(playerUuid);
            if (user == null) {
                return false;
            }
            double current = user.getMana();
            if (current < amount) {
                return false;
            }
            user.setMana(current - amount);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("AuraSkills consumeMana failed: " + t.getMessage());
            return false;
        }
    }

    private static Skills parseSkill(String skillKey) {
        if (skillKey == null || skillKey.isBlank())
            return null;
        try {
            return Skills.valueOf(skillKey.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
