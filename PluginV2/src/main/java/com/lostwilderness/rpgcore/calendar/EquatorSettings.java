package com.lostwilderness.rpgcore.calendar;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Config for the equatorial Z-band (spawn highway buffer). Loaded from {@code lw-climate.yml} under {@code equator:}.
 */
public final class EquatorSettings {

    private final boolean enabled;
    private final int halfWidthBlocks;
    private final boolean reduceWeather;
    private final boolean gateColdEvents;
    private final List<String> worldNames;

    public EquatorSettings(boolean enabled, int halfWidthBlocks, boolean reduceWeather, boolean gateColdEvents,
                           List<String> worldNames) {
        this.enabled = enabled;
        this.halfWidthBlocks = Math.max(0, halfWidthBlocks);
        this.reduceWeather = reduceWeather;
        this.gateColdEvents = gateColdEvents;
        this.worldNames = worldNames == null ? List.of() : List.copyOf(worldNames);
    }

    public static EquatorSettings disabled() {
        return new EquatorSettings(false, 500, false, false, List.of());
    }

    /**
     * Read {@code equator} section from lw-climate root (e.g. getClimate() from LWConfigs).
     */
    public static EquatorSettings from(FileConfiguration climateRoot) {
        if (climateRoot == null) {
            return disabled();
        }
        ConfigurationSection sec = climateRoot.getConfigurationSection("equator");
        // Old lw-climate.yml without equator: use same defaults as jar (belt on).
        if (sec == null) {
            return new EquatorSettings(true, 500, true, true, List.of());
        }
        boolean enabled = sec.getBoolean("enabled", true);
        int half = sec.getInt("half_width_blocks", 500);
        boolean reduceWeather = sec.getBoolean("reduce_weather", true);
        boolean gateCold = sec.getBoolean("gate_cold_events", true);
        List<String> worlds = sec.getStringList("worlds");
        if (worlds == null) {
            worlds = new ArrayList<>();
        }
        return new EquatorSettings(enabled, half, reduceWeather, gateCold, worlds);
    }

    /**
     * Load from plugin data folder {@code lw-climate.yml} when events module is off (calendar-only).
     */
    public static EquatorSettings loadOrDisabled(Plugin plugin) {
        File f = new File(plugin.getDataFolder(), "lw-climate.yml");
        if (!f.exists()) {
            return disabled();
        }
        try {
            return from(YamlConfiguration.loadConfiguration(f));
        } catch (Exception e) {
            return disabled();
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public int halfWidthBlocks() {
        return halfWidthBlocks;
    }

    public boolean reduceWeather() {
        return reduceWeather;
    }

    public boolean gateColdEvents() {
        return gateColdEvents;
    }

    public List<String> worldNames() {
        return Collections.unmodifiableList(worldNames);
    }

    public boolean appliesToWorld(World w) {
        if (!enabled || w == null) {
            return false;
        }
        if (w.getEnvironment() != World.Environment.NORMAL) {
            return false;
        }
        if (worldNames.isEmpty()) {
            return true;
        }
        String name = w.getName().toLowerCase(Locale.ROOT);
        for (String n : worldNames) {
            if (n != null && n.toLowerCase(Locale.ROOT).equals(name)) {
                return true;
            }
        }
        return false;
    }
}
