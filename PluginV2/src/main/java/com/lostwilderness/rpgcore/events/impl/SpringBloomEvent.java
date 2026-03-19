package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spring bloom: first day of spring (season change to SPRING); message and age crops; spawn bees near flowers.
 */
public final class SpringBloomEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;

    public SpringBloomEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        var date = calendar.getCurrentSnapshot().date();
        if (date.getMonthValue() != 3 || date.getDayOfMonth() != 1) return; // first day of spring (March 1)
        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§2🌸 Spring Has Sprung! Flowers bloom everywhere (in spring biomes)!");
        }
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            int bx = chunk.getX() << 4, bz = chunk.getZ() << 4;
            for (int x = bx; x < bx + 16; x++) {
                for (int z = bz; z < bz + 16; z++) {
                    int y = world.getHighestBlockYAt(x, z);
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getBlockData() instanceof Ageable age && age.getAge() < age.getMaximumAge()) {
                        age.setAge(age.getAge() + 1);
                        b.setBlockData(age, false);
                    }
                }
            }
        }
        for (Player p : world.getPlayers()) {
            Location loc = p.getLocation();
            for (int i = 0; i < 5; i++) {
                double dx = ThreadLocalRandom.current().nextDouble() * 10 - 5;
                double dz = ThreadLocalRandom.current().nextDouble() * 10 - 5;
                int fx = loc.getBlockX() + (int) dx, fz = loc.getBlockZ() + (int) dz;
                int fy = world.getHighestBlockYAt(fx, fz);
                Block below = world.getBlockAt(fx, fy - 1, fz);
                if (below.getType() == Material.POPPY || below.getType() == Material.DANDELION
                    || below.getType() == Material.SUNFLOWER || below.getType() == Material.AZURE_BLUET) {
                    world.spawnEntity(new Location(world, fx, fy, fz), org.bukkit.entity.EntityType.BEE);
                }
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
    public String getDisplayName() { return "Spring Bloom"; }
}
