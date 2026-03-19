package com.lostwilderness.survivalv2;

import com.lostwilderness.rpgcore.core.RPGCorePlugin;
import com.lostwilderness.rpgcore.personality.CompletionService;
import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import com.lostwilderness.rpgcore.personality.TraitService;
import com.lostwilderness.survivalv2.personality.*;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Thin Survival wrapper plugin for RPG_Core_V2.
 * Loads only on the Survival backend and is ready to integrate
 * Survival-specific behaviour with the shared V2 core later.
 */
public final class SurvivalV2Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        RPGCorePlugin core = RPGCorePlugin.getInstance();
        if (core == null || !core.isEnabled()) {
            getLogger().warning("RPG_Core_V2 is not loaded; Survival V2 wrapper will be idle.");
            return;
        }
        getLogger().info("LW-Survival-V2 enabled. RPG_Core_V2 " + core.getDescription().getVersion());

        // Register calendar and event commands
        new SurvivalCalendarCommands(this).registerAll();
        new SurvivalEventCommands(this).register();

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

        if (traitService == null || holyEnchantService == null || completionService == null) {
            getLogger().info("[personality] Personality module not enabled; skipping listeners.");
            return;
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
            new TraitItemListener(this, holyEnchantService),
            this
        );

        getLogger().info("[personality] Personality listeners registered successfully.");

        // Register /trait command
        TraitCommand traitCommand = new TraitCommand(this, traitService, completionService, holyEnchantService);
        TraitTabCompleter traitTabCompleter = new TraitTabCompleter();
        getCommand("trait").setExecutor(traitCommand);
        getCommand("trait").setTabCompleter(traitTabCompleter);
        getLogger().info("[personality] /trait command registered.");
    }

    @Override
    public void onDisable() {
        // No special shutdown logic yet.
    }
}

