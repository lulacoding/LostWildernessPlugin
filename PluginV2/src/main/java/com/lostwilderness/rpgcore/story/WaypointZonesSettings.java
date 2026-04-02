package com.lostwilderness.rpgcore.story;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Loads {@code waypoint-zones.yml}: list of arrival cylinders for Waypoints + BetonQuest integration.
 */
public final class WaypointZonesSettings {

    private final List<WaypointZone> zones;

    private WaypointZonesSettings(List<WaypointZone> zones) {
        this.zones = zones;
    }

    public static WaypointZonesSettings load(Plugin plugin) {
        String name = "waypoint-zones.yml";
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
            plugin.getLogger().log(Level.WARNING, "waypoint-zones.yml defaults merge failed", e);
        }
        List<WaypointZone> list = new ArrayList<>();
        ConfigurationSection root = cfg.getConfigurationSection("zones");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec == null) {
                    continue;
                }
                try {
                    list.add(parseZone(key, sec));
                } catch (Exception e) {
                    plugin.getLogger().warning("[story] Skipping invalid waypoint zone '" + key + "': " + e.getMessage());
                }
            }
        }
        return new WaypointZonesSettings(Collections.unmodifiableList(list));
    }

    private static WaypointZone parseZone(String id, ConfigurationSection sec) {
        String world = sec.getString("world", "world");
        int x = sec.getInt("x");
        int y = sec.getInt("y");
        int z = sec.getInt("z");
        double radius = sec.getDouble("radius", 24);
        String perm = sec.getString("waypoint_permission", "");
        String bq = sec.getString("betonquest_event", "");
        if (perm.isBlank() || bq.isBlank()) {
            throw new IllegalArgumentException("waypoint_permission and betonquest_event required");
        }
        String skipTag = sec.getString("skip_if_player_has_tag", "");
        if (skipTag != null && skipTag.isBlank()) {
            skipTag = null;
        }
        boolean removeCompass = sec.getBoolean("remove_amos_compass", false);
        return new WaypointZone(id, world, x, y, z, radius, perm, bq, skipTag, removeCompass);
    }

    public List<WaypointZone> getZones() {
        return zones;
    }
}
