package com.lostwilderness.rpgcore.story;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Loads {@code story-compass.yml}: Thornwell lodestone target and arrival radius.
 */
public final class StoryCompassSettings {

    private final String worldName;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final double arrivalRadius;

    private StoryCompassSettings(String worldName, int blockX, int blockY, int blockZ, double arrivalRadius) {
        this.worldName = worldName;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.arrivalRadius = arrivalRadius;
    }

    public static StoryCompassSettings load(Plugin plugin) {
        String name = "story-compass.yml";
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        try {
            if (plugin.getResource(name) != null) {
                try (InputStreamReader reader = new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8)) {
                    FileConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                    cfg.setDefaults(defaults);
                    cfg.options().copyDefaults(true);
                    cfg.save(file);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "story-compass.yml defaults merge failed", e);
        }
        var sec = cfg.getConfigurationSection("thornwell");
        if (sec == null) {
            plugin.getLogger().warning("[story] story-compass.yml missing 'thornwell' section — using defaults.");
            return new StoryCompassSettings("world", -53, 76, 311, 28);
        }
        return new StoryCompassSettings(
                sec.getString("world", "world"),
                sec.getInt("x", -53),
                sec.getInt("y", 76),
                sec.getInt("z", 311),
                sec.getDouble("arrival-radius", 28)
        );
    }

    public String getWorldName() {
        return worldName;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public double getArrivalRadius() {
        return arrivalRadius;
    }
}
