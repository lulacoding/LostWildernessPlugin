package com.lostwilderness.amplifiedv2;

import com.lostwilderness.rpgcore.core.RPGCorePlugin;
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
        new AmplifiedCalendarCommands(this).registerAll();
        new AmplifiedEventCommands(this).register();
    }

    @Override
    public void onDisable() {
        // No special shutdown logic yet.
    }
}

