package com.lostwilderness.rpgcore.events.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Loads Aeternum-style config files (climate, crops, fauna, events-extra) under LW namespace.
 * Keys match Aeternum so logic can be ported directly. Files: lw-climate.yml, lw-crops.yml, lw-fauna.yml, lw-events-extra.yml.
 */
public final class LWConfigs {

    private final Plugin plugin;
    private FileConfiguration climate;
    private FileConfiguration crops;
    private FileConfiguration fauna;
    private FileConfiguration eventsExtra;

    public LWConfigs(Plugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        climate = load("lw-climate.yml");
        crops = load("lw-crops.yml");
        fauna = load("lw-fauna.yml");
        eventsExtra = load("lw-events-extra.yml");
    }

    private FileConfiguration load(String name) {
        File f = new File(plugin.getDataFolder(), name);
        if (!f.exists()) {
            plugin.saveResource(name, false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        try {
            if (plugin.getResource(name) != null) {
                try (InputStreamReader reader = new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8)) {
                    FileConfiguration defaultCfg = YamlConfiguration.loadConfiguration(reader);
                    cfg.setDefaults(defaultCfg);
                    cfg.options().copyDefaults(true);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load defaults for " + name, e);
        }
        return cfg;
    }

    public FileConfiguration getClimate() { return climate; }
    public FileConfiguration getCrops() { return crops; }
    public FileConfiguration getFauna() { return fauna; }
    public FileConfiguration getEventsExtra() { return eventsExtra; }
}
