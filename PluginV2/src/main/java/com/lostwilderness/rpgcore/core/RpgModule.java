package com.lostwilderness.rpgcore.core;

/**
 * A feature module. Lifecycle: onLoad (register services) → onEnable (listeners/commands) → onDisable (cleanup).
 */
public interface RpgModule {

    /** Called first; register services and load config. No listeners yet. */
    void onLoad(ModuleContext ctx);

    /** Called after all modules are loaded; register listeners and commands. */
    void onEnable();

    /** Called on plugin disable; unregister and cleanup. */
    void onDisable();

    /** Unique module name (e.g. "player", "progression"). */
    String getName();

    /** Names of modules that must be loaded before this one. */
    default java.util.List<String> getDependencies() {
        return java.util.Collections.emptyList();
    }
}
