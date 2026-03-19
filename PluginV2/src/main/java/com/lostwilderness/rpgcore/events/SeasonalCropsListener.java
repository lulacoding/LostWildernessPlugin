package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

/**
 * Aeternum-style seasonal crops: growth only allowed in configured seasons per crop.
 * Config: lw-crops.yml seasonal_crops.crops.<MATERIAL>.allowed_seasons; off_season_growth_chance for rare growth.
 */
public final class SeasonalCropsListener implements Listener {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final LWConfigs lwConfigs;
    private final Random random = new Random();

    public SeasonalCropsListener(Plugin plugin, CalendarServiceV2 calendar, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.lwConfigs = lwConfigs;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        if (!isEnabled()) return;
        Material type = e.getBlock().getType();
        List<String> allowed = getAllowedSeasons(type);
        if (allowed == null || allowed.isEmpty()) return;
        CalendarServiceV2.Season season = calendar.getCurrentSnapshot().season();
        if (allowed.contains(season.name())) return;
        double offChance = getOffSeasonGrowthChance();
        if (offChance <= 0 || random.nextDouble() >= offChance) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent e) {
        if (!isEnabled()) return;
        Material type = e.getBlock().getType();
        List<String> allowed = getAllowedSeasons(type);
        if (allowed == null || allowed.isEmpty()) return;
        CalendarServiceV2.Season season = calendar.getCurrentSnapshot().season();
        if (allowed.contains(season.name())) return;
        double offChance = getOffSeasonGrowthChance();
        if (offChance <= 0 || random.nextDouble() >= offChance) {
            e.setCancelled(true);
        }
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getCrops().getBoolean("seasonal_crops.enabled", false);
    }

    private List<String> getAllowedSeasons(Material type) {
        FileConfiguration crops = lwConfigs.getCrops();
        String path = "seasonal_crops.crops." + type.name();
        if (!crops.contains(path + ".allowed_seasons")) return null;
        return crops.getStringList(path + ".allowed_seasons");
    }

    private double getOffSeasonGrowthChance() {
        return lwConfigs.getCrops().getDouble("seasonal_crops.off_season_growth_chance", 0.1);
    }
}
