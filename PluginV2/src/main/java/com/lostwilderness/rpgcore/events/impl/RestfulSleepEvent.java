package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Restorative Sleep: when active, sleeping grants temporary absorption/health boost. Config from lw-events-extra.
 */
public final class RestfulSleepEvent implements DailyWorldEvent, Listener {

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public RestfulSleepEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("restful_sleep.enabled", true);
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("restful_sleep.base_chance_per_day", 0.10);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§aRestful Sleep night—sleep well for a health boost!");
        }
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent e) {
        if (!active) return;
        Player p = e.getPlayer();
        int seconds = lwConfigs.getEventsExtra().getInt("restful_sleep.health_boost_seconds", 600);
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, seconds * 20, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Math.min(seconds, 30) * 20, 0));
        p.sendMessage("§aYou feel well rested!");
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Restful Sleep"; }
}
