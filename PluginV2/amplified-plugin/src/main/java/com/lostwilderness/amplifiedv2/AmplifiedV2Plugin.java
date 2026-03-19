package com.lostwilderness.amplifiedv2;

import com.lostwilderness.amplifiedv2.personality.*;
import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.personality.CompletionService;
import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import com.lostwilderness.rpgcore.personality.TraitService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Thin Amplified wrapper plugin for RPG_Core_V2.
 * Loads only on the Amplified backend and is ready to integrate
 * Amplified-specific behaviour with the shared V2 core later.
 */
public final class AmplifiedV2Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        if (core == null || !core.isEnabled()) {
            getLogger().warning("RPG_Core_V2 is not loaded; Amplified V2 wrapper will be idle.");
            return;
        }
        getLogger().info("LW-Amplified-V2 enabled. RPG_Core_V2 " + core.getDescription().getVersion());

        // Register calendar and event commands
        new AmplifiedCalendarCommands(this).registerAll();
        new AmplifiedEventCommands(this).register();

        // Register personality system listeners if module is enabled
        registerPersonalityListeners(core);
    }

    /**
     * Register personality trait listeners if personality module is enabled.
     */
    private void registerPersonalityListeners(RPGCorePlugin core) {
        TraitService traitService = core.getService(TraitService.class);
        HolyEnchantService holyEnchantService = core.getService(HolyEnchantService.class);
        CompletionService completionService = core.getService(CompletionService.class);
        com.lostwilderness.rpgcore.reputation.ReputationService reputationService = core.getService(com.lostwilderness.rpgcore.reputation.ReputationService.class);

        if (traitService == null || holyEnchantService == null || completionService == null) {
            getLogger().info("[personality] Personality module not enabled; skipping listeners.");
            return;
        }

        if (reputationService == null) {
            getLogger().warning("[personality] ReputationService not available; Staff of the Covenant will not work.");
        }

        getLogger().info("[personality] Registering personality effect listeners...");

        // Register all 4 listeners
        getServer().getPluginManager().registerEvents(
            new TraitPassiveListener(traitService),
            this
        );

        getServer().getPluginManager().registerEvents(
            new ElementalPassiveListener(traitService),
            this
        );

        getServer().getPluginManager().registerEvents(
            new HolyEnchantEffectListener(holyEnchantService),
            this
        );

        getServer().getPluginManager().registerEvents(
            new TraitItemListener(this, holyEnchantService, reputationService),
            this
        );

        getLogger().info("[personality] Personality listeners registered successfully.");

        // Start scheduled tasks for passive effects
        startPersonalityScheduledTasks(traitService, holyEnchantService, reputationService);

        // Register /ptrait command
        TraitCommand traitCommand = new TraitCommand(this, traitService, completionService, holyEnchantService);
        TraitTabCompleter traitTabCompleter = new TraitTabCompleter();
        getCommand("ptrait").setExecutor(traitCommand);
        getCommand("ptrait").setTabCompleter(traitTabCompleter);
        getLogger().info("[personality] /ptrait command registered.");
    }

    /**
     * Start scheduled tasks for personality passive effects.
     */
    private void startPersonalityScheduledTasks(
        TraitService traitService,
        HolyEnchantService holyEnchantService,
        com.lostwilderness.rpgcore.reputation.ReputationService reputationService
    ) {
        // LUNAR_BLESSING: Nighttime regeneration (every 5 seconds)
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                // Check if it's nighttime (13000-23000 ticks)
                long time = player.getWorld().getTime();
                if (time < 13000 || time > 23000) continue;

                // Check if player has LUNAR_BLESSING on helmet
                org.bukkit.inventory.ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && holyEnchantService.hasHolyEnchant(helmet, com.lostwilderness.rpgcore.personality.HolyEnchant.LUNAR_BLESSING)) {
                    // Apply Regeneration I for 6 seconds
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.REGENERATION,
                        120, // 6 seconds
                        0,   // Level I
                        true, false, false
                    ));
                }
            }
        }, 100L, 100L); // Run every 5 seconds (100 ticks)

        // HEALER TRAIT: AoE effects every 15 seconds
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                // Check if player has HEALER trait at TRAIT tier or higher
                if (!traitService.hasActiveTrait(
                    player.getUniqueId(),
                    com.lostwilderness.rpgcore.personality.PersonalityTrait.HEALER,
                    com.lostwilderness.rpgcore.personality.TraitTier.TRAIT
                )) continue;

                // Get player alignment (Good = positive honor, Evil = negative)
                int honorScore = 0;
                if (reputationService != null) {
                    try {
                        honorScore = reputationService.getHonorScore(player.getUniqueId())
                            .get(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        // Default to neutral if reputation check fails
                    }
                }

                boolean isGood = honorScore >= 0;

                // Apply effects to nearby entities within 10 blocks
                for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (!(entity instanceof org.bukkit.entity.LivingEntity)) continue;
                    org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) entity;

                    if (isGood) {
                        // Good alignment: Regeneration to all nearby entities
                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.REGENERATION,
                            100, // 5 seconds
                            0,
                            true, false, false
                        ));
                        // Spawn heart particles
                        player.getWorld().spawnParticle(
                            org.bukkit.Particle.HEART,
                            target.getLocation().add(0, 1, 0),
                            3,
                            0.5, 0.5, 0.5,
                            0.1
                        );
                    } else {
                        // Evil alignment: Wither to nearby entities (excluding self)
                        if (target.getUniqueId().equals(player.getUniqueId())) continue;

                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.WITHER,
                            100, // 5 seconds
                            0,
                            true, false, false
                        ));
                        // Spawn smoke particles
                        player.getWorld().spawnParticle(
                            org.bukkit.Particle.SMOKE,
                            target.getLocation().add(0, 1, 0),
                            3,
                            0.5, 0.5, 0.5,
                            0.1
                        );
                    }
                }
            }
        }, 300L, 300L); // Run every 15 seconds (300 ticks)

        getLogger().info("[personality] Scheduled tasks started (LUNAR_BLESSING, HEALER AoE).");
    }

    @Override
    public void onDisable() {
        // No special shutdown logic yet.
    }
}

