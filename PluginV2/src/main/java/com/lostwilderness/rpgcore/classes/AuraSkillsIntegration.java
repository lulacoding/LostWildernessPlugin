package com.lostwilderness.rpgcore.classes;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.ability.CustomAbility;
import dev.aurelium.auraskills.api.mana.CustomManaAbility;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Registers 5 custom mastery skills and their mana abilities with AuraSkills API.
 * Uses proper NamespacedRegistry and CustomSkill/CustomManaAbility builders.
 *
 * Phase 8: AuraSkills Integration (Option 2 - Proper API Usage per wiki.aurelium.dev)
 */
public final class AuraSkillsIntegration {

    private final Plugin plugin;
    private final AuraSkillsApi api;
    private final NamespacedRegistry registry;

    // Custom Skills
    public static CustomSkill TEMPLAR_MASTERY;
    public static CustomSkill RANGER_MASTERY;
    public static CustomSkill ARTIFICER_MASTERY;
    public static CustomSkill CULTIST_MASTERY;
    public static CustomSkill BERSERKER_MASTERY;

    // Custom Mana Abilities
    public static CustomManaAbility SMITE_ABILITY;
    public static CustomManaAbility HUNTERS_MARK_ABILITY;
    public static CustomManaAbility SLAM_ABILITY;
    public static CustomManaAbility DASH_ABILITY;
    public static CustomManaAbility WAR_CRY_ABILITY;

    public AuraSkillsIntegration(Plugin plugin) {
        this.plugin = plugin;
        this.api = AuraSkillsApi.get();

        // Create content directory for AuraSkills config files
        File contentDir = new File(plugin.getDataFolder(), "auraskills-content");
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }

        // Create subdirectories
        new File(contentDir, "sources").mkdirs();
        new File(contentDir, "rewards").mkdirs();

        // Extract bundled config files from JAR
        saveResourceIfNotExists("auraskills-content/skills.yml");
        saveResourceIfNotExists("auraskills-content/mana_abilities.yml");

        // Extract source and reward files for all 5 skills
        String[] skillNames = {"templar_mastery", "ranger_mastery", "artificer_mastery", "cultist_mastery", "berserker_mastery"};
        for (String skillName : skillNames) {
            saveResourceIfNotExists("auraskills-content/sources/" + skillName + ".yml");
            saveResourceIfNotExists("auraskills-content/rewards/" + skillName + ".yml");
        }

        // Get namespaced registry (namespace = "lostwilderness")
        this.registry = api.useRegistry("lostwilderness", contentDir);
    }

    /**
     * Safely extract a resource file from JAR if it doesn't already exist.
     */
    private void saveResourceIfNotExists(String resourcePath) {
        try {
            File targetFile = new File(plugin.getDataFolder(), resourcePath);
            if (!targetFile.exists()) {
                plugin.saveResource(resourcePath, false);
                plugin.getLogger().info("[classes] Extracted " + resourcePath);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[classes] Could not extract " + resourcePath + ": " + e.getMessage());
        }
    }

    /**
     * Register all 5 custom mastery skills with AuraSkills.
     * Call this once during plugin onEnable().
     */
    public void registerCustomSkills() {
        try {
            registerManaAbilities(); // Must register mana abilities first

            registerTemplarMastery();
            registerRangerMastery();
            registerArtificerMastery();
            registerCultistMastery();
            registerBerserkerMastery();

            plugin.getLogger().info("[classes] Registered 5 custom mastery skills with AuraSkills");
        } catch (Exception e) {
            plugin.getLogger().severe("[classes] Failed to register custom skills: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Register all 5 custom mana abilities with AuraSkills.
     * Must be called BEFORE registerCustomSkills() so skills can reference them.
     */
    public void registerManaAbilities() {
        try {
            registerSmiteAbility();
            registerHuntersMarkAbility();
            registerSlamAbility();
            registerDashAbility();
            registerWarCryAbility();

            plugin.getLogger().info("[classes] Registered 5 custom mana abilities with AuraSkills");
        } catch (Exception e) {
            plugin.getLogger().severe("[classes] Failed to register mana abilities: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MANA ABILITY REGISTRATION (must be first) ====================

    private void registerSmiteAbility() {
        SMITE_ABILITY = CustomManaAbility.builder(NamespacedId.of("lostwilderness", "smite"))
            .displayName("Smite")
            .description("Call down lightning to strike your foes [Right-click + Sneak with sword]")
            .build();

        registry.registerManaAbility(SMITE_ABILITY);
    }

    private void registerHuntersMarkAbility() {
        HUNTERS_MARK_ABILITY = CustomManaAbility.builder(NamespacedId.of("lostwilderness", "hunters_mark"))
            .displayName("Hunter's Mark")
            .description("Mark a target for bonus damage [Right-click + Sneak with bow]")
            .build();

        registry.registerManaAbility(HUNTERS_MARK_ABILITY);
    }

    private void registerSlamAbility() {
        SLAM_ABILITY = CustomManaAbility.builder(NamespacedId.of("lostwilderness", "slam"))
            .displayName("Slam")
            .description("Slam the ground to knock back and slow enemies [Right-click + Sneak with pickaxe]")
            .build();

        registry.registerManaAbility(SLAM_ABILITY);
    }

    private void registerDashAbility() {
        DASH_ABILITY = CustomManaAbility.builder(NamespacedId.of("lostwilderness", "dash"))
            .displayName("Dash")
            .description("Teleport forward through shadows [Right-click + Sneak, empty hand]")
            .build();

        registry.registerManaAbility(DASH_ABILITY);
    }

    private void registerWarCryAbility() {
        WAR_CRY_ABILITY = CustomManaAbility.builder(NamespacedId.of("lostwilderness", "war_cry"))
            .displayName("War Cry")
            .description("Buff allies and knock back enemies [Right-click + Sneak with axe]")
            .build();

        registry.registerManaAbility(WAR_CRY_ABILITY);
    }

    // ==================== SKILL REGISTRATION ====================

    private void registerTemplarMastery() {
        TEMPLAR_MASTERY = CustomSkill.builder(NamespacedId.of("lostwilderness", "templar_mastery"))
            .displayName("Templar Mastery")
            .description("Holy warrior expertise. Gain power through righteous combat.")
            .manaAbility(SMITE_ABILITY) // Link mana ability
            .build();

        registry.registerSkill(TEMPLAR_MASTERY);
    }

    private void registerRangerMastery() {
        RANGER_MASTERY = CustomSkill.builder(NamespacedId.of("lostwilderness", "ranger_mastery"))
            .displayName("Ranger Mastery")
            .description("Wilderness survival expertise. Master the bow and nature.")
            .manaAbility(HUNTERS_MARK_ABILITY)
            .build();

        registry.registerSkill(RANGER_MASTERY);
    }

    private void registerArtificerMastery() {
        ARTIFICER_MASTERY = CustomSkill.builder(NamespacedId.of("lostwilderness", "artificer_mastery"))
            .displayName("Artificer Mastery")
            .description("Crafting and forging expertise. Build your legacy.")
            .manaAbility(SLAM_ABILITY)
            .build();

        registry.registerSkill(ARTIFICER_MASTERY);
    }

    private void registerCultistMastery() {
        CULTIST_MASTERY = CustomSkill.builder(NamespacedId.of("lostwilderness", "cultist_mastery"))
            .displayName("Cultist Mastery")
            .description("Dark arts and shadow expertise. Embrace the darkness.")
            .manaAbility(DASH_ABILITY)
            .build();

        registry.registerSkill(CULTIST_MASTERY);
    }

    private void registerBerserkerMastery() {
        BERSERKER_MASTERY = CustomSkill.builder(NamespacedId.of("lostwilderness", "berserker_mastery"))
            .displayName("Berserker Mastery")
            .description("Rage and combat expertise. Channel your fury.")
            .manaAbility(WAR_CRY_ABILITY)
            .build();

        registry.registerSkill(BERSERKER_MASTERY);
    }
}
