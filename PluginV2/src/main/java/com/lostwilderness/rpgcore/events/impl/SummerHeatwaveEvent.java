package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import com.lostwilderness.rpgcore.events.util.BiomeGroups;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Summer heatwave: summer-only; dries farmland, husks, hunger; shade check + armor exposure (weakness/damage in desert/savanna).
 */
public final class SummerHeatwaveEvent implements DailyWorldEvent {

    private static final int RADIUS = 25;
    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final LWConfigs lwConfigs;
    private volatile boolean active = false;
    private BukkitRunnable hungerTask;
    private BukkitRunnable endTask;

    public SummerHeatwaveEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this(plugin, calendar, null);
    }

    public SummerHeatwaveEvent(Plugin plugin, CalendarServiceV2 calendar, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.lwConfigs = lwConfigs;
    }

    private boolean isExposureDamageEnabled() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getBoolean("heat_wave.damage_exposed", true);
        }
        return plugin.getConfig().getBoolean("events.heatwave.exposure-damage-enabled", true);
    }

    private double getTickDamage() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getDouble("heat_wave.tick_damage", 0.5);
        }
        return plugin.getConfig().getDouble("events.heatwave.damage-per-tick", 0.5);
    }

    private boolean isApplyWeakness() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getBoolean("heat_wave.apply_weakness", true);
        }
        return plugin.getConfig().getBoolean("events.heatwave.apply-weakness", true);
    }

    private int getMinArmorPieces() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getInt("heat_wave.min_armor_protection", 2);
        }
        return plugin.getConfig().getInt("events.heatwave.min-armor-pieces", 2);
    }

    /** True if player has no block above them at eye level (exposed to sun). */
    private boolean isExposedToSky(Player p) {
        World world = p.getWorld();
        int blockX = p.getLocation().getBlockX();
        int blockZ = p.getLocation().getBlockZ();
        int highestY = world.getHighestBlockYAt(blockX, blockZ);
        double eyeY = p.getLocation().getY() + p.getEyeHeight();
        return eyeY >= highestY + 1;
    }

    private int countArmorPieces(Player p) {
        int n = 0;
        if (p.getInventory().getHelmet() != null && !p.getInventory().getHelmet().getType().isAir()) n++;
        if (p.getInventory().getChestplate() != null && !p.getInventory().getChestplate().getType().isAir()) n++;
        if (p.getInventory().getLeggings() != null && !p.getInventory().getLeggings().getType().isAir()) n++;
        if (p.getInventory().getBoots() != null && !p.getInventory().getBoots().getType().isAir()) n++;
        return n;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        if (calendar.getCurrentSnapshot().season() != CalendarServiceV2.Season.SUMMER) return;
        int chancePct = plugin.getConfig().getInt("events.heatwave.chance-pct", 25);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        final World w = world;
        for (Player p : w.getPlayers()) {
            p.sendMessage("§c🔥 A scorching heatwave grips hot biomes! Stay cool if you're in a desert or savanna!");
        }
        applyDryAndHusks(w);
        active = true;
        startHungerTask(w);
        long now = w.getTime();
        long delay = (13000 - now + 24000) % 24000;
        if (delay == 0) delay = 24000;
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                endTask = null;
                for (Player p : w.getPlayers()) {
                    p.sendMessage("§a🌅 The heatwave subsides in hot biomes as evening cools the land. Stay hydrated!");
                    if (BiomeGroups.isDesert(p.getLocation().getBlock().getBiome()) || BiomeGroups.isSavanna(p.getLocation().getBlock().getBiome())) {
                        p.removePotionEffect(PotionEffectType.HUNGER);
                    }
                }
                if (hungerTask != null) { hungerTask.cancel(); hungerTask = null; }
            }
        };
        endTask.runTaskLater(plugin, delay);
    }

    private void applyDryAndHusks(World world) {
        for (Player p : world.getPlayers()) {
            var biome = p.getLocation().getBlock().getBiome();
            if (!BiomeGroups.isDesert(biome) && !BiomeGroups.isSavanna(biome)) continue;
            int px = p.getLocation().getBlockX(), pz = p.getLocation().getBlockZ();
            for (int x = px - RADIUS; x <= px + RADIUS; x++) {
                for (int z = pz - RADIUS; z <= pz + RADIUS; z++) {
                    int y = world.getHighestBlockYAt(x, z);
                    Block top = world.getBlockAt(x, y, z);
                    Block below = top.getRelative(0, -1, 0);
                    if (top.getBlockData() instanceof org.bukkit.block.data.Ageable && below.getType() == Material.FARMLAND) {
                        Block soil = below;
                        if (soil.getBlockData() instanceof Farmland farm && farm.getMoisture() > 0) {
                            farm.setMoisture(0);
                            soil.setBlockData(farm, false);
                        }
                    } else if (top.getType() == Material.FARMLAND && top.getBlockData() instanceof Farmland farm && farm.getMoisture() > 0) {
                        farm.setMoisture(0);
                        top.setBlockData(farm, false);
                    }
                }
            }
            int count = 1 + ThreadLocalRandom.current().nextInt(3);
            for (int i = 0; i < count; i++) {
                double dx = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * RADIUS;
                double dz = (ThreadLocalRandom.current().nextDouble() * 2 - 1) * RADIUS;
                int xx = px + (int) dx, zz = pz + (int) dz;
                int yy = world.getHighestBlockYAt(xx, zz);
                world.spawn(new org.bukkit.Location(world, xx + 0.5, yy, zz + 0.5), Husk.class);
            }
        }
    }

    private void startHungerTask(World world) {
        if (hungerTask != null) return;
        hungerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); hungerTask = null; return; }
                for (Player p : world.getPlayers()) {
                    var biome = p.getLocation().getBlock().getBiome();
                    boolean inHot = BiomeGroups.isDesert(biome) || BiomeGroups.isSavanna(biome);
                    if (inHot && !p.hasPotionEffect(PotionEffectType.HUNGER)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 15, 0, false, true));
                    } else if (!inHot && p.hasPotionEffect(PotionEffectType.HUNGER)) {
                        p.removePotionEffect(PotionEffectType.HUNGER);
                    }
                    if (inHot && isExposedToSky(p) && countArmorPieces(p) < getMinArmorPieces()) {
                        if (isApplyWeakness()) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 0, false, true));
                        }
                        if (isExposureDamageEnabled() && getTickDamage() > 0) {
                            p.damage(getTickDamage());
                        }
                    }
                }
            }
        };
        hungerTask.runTaskTimer(plugin, 0L, 100L);
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
                p.sendMessage("§a🌅 The heatwave subsides in hot biomes as evening cools the land. Stay hydrated!");
                if (BiomeGroups.isDesert(p.getLocation().getBlock().getBiome()) || BiomeGroups.isSavanna(p.getLocation().getBlock().getBiome())) {
                    p.removePotionEffect(PotionEffectType.HUNGER);
                }
            }
        }
        if (hungerTask != null) { hungerTask.cancel(); hungerTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Heatwave"; }
}
