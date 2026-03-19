package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Autumn leaf fall: first day of autumn; message and falling-dust particles for players.
 */
public final class AutumnLeafFallEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;

    public AutumnLeafFallEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        if (calendar.getCurrentSnapshot().season() != CalendarServiceV2.Season.AUTUMN) return;
        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§6🍁 Autumn has arrived! Leaves drift from the trees (in autumn biomes)!");
        }
        for (Player p : world.getPlayers()) {
            for (int i = 0; i < 30; i++) {
                double dx = ThreadLocalRandom.current().nextGaussian() * 3;
                double dz = ThreadLocalRandom.current().nextGaussian() * 3;
                Location loc = p.getLocation().clone().add(dx, 2, dz);
                p.spawnParticle(Particle.FALLING_DUST, loc, 5, 0.5, 0.5, 0.5, 0,
                    Bukkit.createBlockData(org.bukkit.Material.OAK_LEAVES));
            }
        }
        active = false;
    }

    private static World overworld() {
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() { active = false; }

    @Override
    public String getDisplayName() { return "Autumn Leaf Fall"; }
}
