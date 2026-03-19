package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.util.BiomeGroups;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Jungle monsoon: summer-only, chance per day; player rain in jungle, spawn slimes and parrots; ends at dawn.
 */
public final class JungleMonsoonEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public JungleMonsoonEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        if (calendar.getCurrentSnapshot().season() != CalendarServiceV2.Season.SUMMER) return;
        int chancePct = plugin.getConfig().getInt("events.jungle-monsoon.chance-pct", 20);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        final World w = world;
        long now = w.getTime();
        long delay = (13000 - now + 24000) % 24000;
        if (delay == 0) delay = 24000;
        Bukkit.getScheduler().runTaskLater(plugin, () -> runMonsoon(w), delay);
    }

    private void runMonsoon(World world) {
        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§3🌧️ A steamy jungle monsoon begins! (Only jungle biomes are affected!) 🌧️");
        }
        for (Player p : world.getPlayers()) {
            if (BiomeGroups.isJungle(p.getLocation().getBlock().getBiome())) {
                p.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL);
            }
        }
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            int cx = (chunk.getX() << 4) + 8, cz = (chunk.getZ() << 4) + 8;
            Biome b = world.getBlockAt(cx, world.getHighestBlockYAt(cx, cz), cz).getBiome();
            if (!BiomeGroups.isJungle(b)) continue;
            for (int i = 0, max = 2 + ThreadLocalRandom.current().nextInt(4); i < max; i++) {
                int x = (chunk.getX() << 4) + ThreadLocalRandom.current().nextInt(16);
                int z = (chunk.getZ() << 4) + ThreadLocalRandom.current().nextInt(16);
                int y = world.getHighestBlockYAt(x, z);
                world.spawn(new Location(world, x + 0.5, y, z + 0.5), Slime.class);
            }
        }
        for (Player p : world.getPlayers()) {
            if (!BiomeGroups.isJungle(p.getLocation().getBlock().getBiome())) continue;
            Location loc = p.getLocation();
            int count = 1 + ThreadLocalRandom.current().nextInt(3);
            for (int i = 0; i < count; i++) {
                double dx = ThreadLocalRandom.current().nextDouble() * 10 - 5;
                double dz = ThreadLocalRandom.current().nextDouble() * 10 - 5;
                int x = loc.getBlockX() + (int) dx, z = loc.getBlockZ() + (int) dz;
                int y = world.getHighestBlockYAt(x, z);
                world.spawnEntity(new Location(world, x + 0.5, y, z + 0.5), org.bukkit.entity.EntityType.PARROT);
            }
        }
        long until6 = (6000 - world.getTime() + 24000) % 24000;
        if (until6 == 0) until6 = 24000;
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : world.getPlayers()) {
                    if (BiomeGroups.isJungle(p.getLocation().getBlock().getBiome())) {
                        p.resetPlayerWeather();
                    }
                }
                world.getPlayers().forEach(p -> p.sendMessage("§a🌥️ The jungle monsoon eases into a gentle mist…"));
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, until6);
    }

    private static World overworld() {
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (!active) return;
        if (endTask != null) { endTask.cancel(); endTask = null; }
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player p : w.getPlayers()) {
                if (BiomeGroups.isJungle(p.getLocation().getBlock().getBiome())) p.resetPlayerWeather();
            }
            w.getPlayers().forEach(p -> p.sendMessage("§a🌥️ The jungle monsoon eases into a gentle mist…"));
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Jungle Monsoon"; }
}
