package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Eclipse event: starts on configurable interval (e.g. every 30 days) with a chance roll.
 * Freezes night, optional resource pack, message, horde spawns; ends after one MC day (24000 ticks).
 */
public final class EclipseEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private volatile boolean active = false;
    private BukkitRunnable hordeTask;
    private BukkitRunnable endTask;

    public EclipseEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;

        int interval = plugin.getConfig().getInt("events.eclipse.interval-days", 30);
        int offset = plugin.getConfig().getInt("events.eclipse.offset-days", 0);
        if ((day - offset) < 0 || (day - offset) % interval != 0) return;

        int chancePct = plugin.getConfig().getInt("events.eclipse.chance-pct", 80);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) {
            plugin.getLogger().info("[Events] Eclipse skipped by chance on day " + day);
            return;
        }

        active = true;
        final World w = world;

        String packUrl = plugin.getConfig().getString("resource-packs.eclipse");
        String emptyUrl = plugin.getConfig().getString("resource-packs.eclipse-empty", "");
        for (Player p : w.getPlayers()) {
            if (packUrl != null && !packUrl.isEmpty()) {
                p.setResourcePack(packUrl, (byte[]) null, (String) null, false);
            }
        }
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setTime(18000L);

        for (Player p : w.getPlayers()) {
            p.sendMessage("§8A lunar eclipse blankets the sky…");
        }

        int hordeSize = plugin.getConfig().getInt("events.eclipse.horde-size", 8);
        hordeTask = new EclipseHordeTask(w, hordeSize);
        hordeTask.runTaskTimer(plugin, 0L, 6000L);

        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (hordeTask != null) {
                    hordeTask.cancel();
                    hordeTask = null;
                }
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setTime(6000L);
                for (Player p : w.getPlayers()) {
                    if (emptyUrl != null && !emptyUrl.isEmpty()) {
                        p.setResourcePack(emptyUrl, (byte[]) null, (String) null, false);
                    }
                }
                for (Player p : w.getPlayers()) {
                    p.sendMessage("§6The eclipse fades… dawn breaks anew.");
                }
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void forceEndEarly() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            }
        }
        if (hordeTask != null) {
            hordeTask.cancel();
            hordeTask = null;
        }
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        active = false;
    }

    @Override
    public String getDisplayName() {
        return "Eclipse";
    }
}
