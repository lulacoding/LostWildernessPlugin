package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarDayAdvancedEvent;
import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies season-based weather when the calendar day advances (Aeternum-style).
 * Uses main config or lw-climate.yml: rainy_days per season, thunder_chance, storm/clear duration.
 */
public final class SeasonalWeatherListener implements Listener {

    private static final int DAYS_PER_SEASON = 28;

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final LWConfigs lwConfigs;

    public SeasonalWeatherListener(Plugin plugin, CalendarServiceV2 calendar, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.lwConfigs = lwConfigs;
    }

    @EventHandler
    public void onDayAdvanced(CalendarDayAdvancedEvent e) {
        if (!plugin.getConfig().getBoolean("events.seasonal-weather.enabled", true)) {
            return;
        }
        FileConfiguration climate = lwConfigs != null ? lwConfigs.getClimate() : null;
        if (climate != null && !climate.getBoolean("seasonal_weather.enabled", true)) {
            return;
        }
        CalendarServiceV2.CalendarSnapshot snap = e.getNewSnapshot();
        CalendarServiceV2.Season season = snap.season();
        long dayCount = snap.dayCount();

        int rainyDays = getRainyDaysForSeason(season, climate);
        double thunderChance = getThunderChance(climate);
        int stormMin = getStormMin(climate);
        int stormMax = getStormMax(climate);
        int clearMin = getClearMin(climate);
        int clearMax = getClearMax(climate);

        int seasonDay = (int) (dayCount % DAYS_PER_SEASON);
        boolean storm = seasonDay < rainyDays;
        boolean thunder = storm && ThreadLocalRandom.current().nextDouble() < thunderChance;
        int stormTicks = clampRand(stormMin, stormMax);
        int clearTicks = clampRand(clearMin, clearMax);

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            if (storm) {
                world.setStorm(true);
                world.setWeatherDuration(stormTicks);
                world.setThunderDuration(thunder ? stormTicks : 0);
                world.setThundering(thunder);
            } else {
                world.setStorm(false);
                world.setWeatherDuration(clearTicks);
                world.setThunderDuration(0);
                world.setThundering(false);
            }
        }
    }

    private int getRainyDaysForSeason(CalendarServiceV2.Season season, FileConfiguration climate) {
        if (climate != null) {
            int def = climate.getInt("seasonal_weather.rainy_days_per_season", 9);
            return climate.getInt("seasonal_weather.rainy_days." + season.name(), def);
        }
        int defaultDays = plugin.getConfig().getInt("events.seasonal-weather.rainy-days-per-season", 10);
        return plugin.getConfig().getInt("events.seasonal-weather.rainy-days." + season.name().toLowerCase(), defaultDays);
    }

    private double getThunderChance(FileConfiguration climate) {
        if (climate != null) {
            return Math.max(0, Math.min(1, climate.getDouble("seasonal_weather.thunder_chance", 0.2)));
        }
        return Math.max(0, Math.min(1, plugin.getConfig().getDouble("events.seasonal-weather.thunder-chance", 0.15)));
    }

    private int getStormMin(FileConfiguration climate) {
        return climate != null ? climate.getInt("seasonal_weather.storm_duration_ticks.min", 6000) : 6000;
    }

    private int getStormMax(FileConfiguration climate) {
        return climate != null ? climate.getInt("seasonal_weather.storm_duration_ticks.max", 18000) : 18000;
    }

    private int getClearMin(FileConfiguration climate) {
        return climate != null ? climate.getInt("seasonal_weather.clear_duration_ticks.min", 6000) : 6000;
    }

    private int getClearMax(FileConfiguration climate) {
        return climate != null ? climate.getInt("seasonal_weather.clear_duration_ticks.max", 24000) : 24000;
    }

    private static int clampRand(int min, int max) {
        if (max < min) { int t = min; min = max; max = t; }
        if (min < 1) min = 1;
        if (max < 1) max = min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
