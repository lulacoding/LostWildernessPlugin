package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.calendar.EquatorZone;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Clears rain/storm client-side for players standing in the equator band when {@code equator.reduce_weather} is true.
 */
public final class EquatorWeatherListener implements Listener {

    private final Plugin plugin;
    private final EquatorSettings settings;
    private int taskId = -1;

    public EquatorWeatherListener(Plugin plugin, EquatorSettings settings) {
        this.plugin = plugin;
        this.settings = settings != null ? settings : EquatorSettings.disabled();
        if (this.settings.enabled() && this.settings.reduceWeather()) {
            this.taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::tickAll,
                    40L, 40L);
        }
    }

    public void cancel() {
        if (taskId >= 0) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        applyOne(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        applyOne(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.getPlayer().resetPlayerWeather();
    }

    private void tickAll() {
        if (!settings.enabled() || !settings.reduceWeather()) {
            return;
        }
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            applyOne(p);
        }
    }

    private void applyOne(Player p) {
        if (p == null || !p.isOnline()) {
            return;
        }
        World w = p.getWorld();
        if (w.getEnvironment() != World.Environment.NORMAL) {
            p.resetPlayerWeather();
            return;
        }
        if (!settings.enabled() || !settings.reduceWeather()) {
            p.resetPlayerWeather();
            return;
        }
        if (EquatorZone.isInBand(p, settings)) {
            p.setPlayerWeather(WeatherType.CLEAR);
        } else {
            p.resetPlayerWeather();
        }
    }
}
