package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tornado: moving center, damage and pull in radius, crop break chance, indoor protection. Config from lw-events-extra.
 */
public final class TornadoEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private volatile boolean active = false;
    private BukkitRunnable runTask;
    private Location center;
    private Vector drift;
    private World world;
    private long endTime;

    public TornadoEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("tornado.enabled", true);
    }

    @Override
    public void onCalendarDay(int day, World worldParam) {
        if (worldParam == null) worldParam = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
        if (worldParam == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("tornado.base_chance_per_day", 0.05);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        world = worldParam;
        List<?> disabled = lwConfigs.getEventsExtra().getList("tornado.disabled_worlds");
        if (disabled != null && disabled.stream().anyMatch(o -> world.getName().equalsIgnoreCase(o.toString()))) return;

        Player target = world.getPlayers().stream().filter(Player::isOnline).findFirst().orElse(null);
        if (target == null) return;
        double minD = lwConfigs.getEventsExtra().getDouble("tornado.min_spawn_distance", 20);
        double maxD = lwConfigs.getEventsExtra().getDouble("tornado.max_spawn_distance", 24);
        double dist = minD + ThreadLocalRandom.current().nextDouble() * (maxD - minD);
        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        center = target.getLocation().clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        center.setY(world.getHighestBlockYAt(center) + 1);
        double speed = lwConfigs.getEventsExtra().getDouble("tornado.drift_speed", 0.18);
        drift = new Vector(ThreadLocalRandom.current().nextDouble() - 0.5, 0, ThreadLocalRandom.current().nextDouble() - 0.5).normalize().multiply(speed);

        int minMin = lwConfigs.getEventsExtra().getInt("tornado.min_duration_minutes", 5);
        int maxMin = lwConfigs.getEventsExtra().getInt("tornado.max_duration_minutes", 10);
        int minutes = minMin + ThreadLocalRandom.current().nextInt(maxMin - minMin + 1);
        endTime = System.currentTimeMillis() + minutes * 60L * 1000L;
        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§c§lA tornado has been sighted! Seek shelter.");
        }

        double radius = lwConfigs.getEventsExtra().getDouble("tornado.radius_blocks", 10);
        double damagePerTick = 0.5;
        double cropBreakChance = 0.2;
        runTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || center == null || world == null) {
                    cancel();
                    return;
                }
                if (System.currentTimeMillis() >= endTime) {
                    active = false;
                    for (Player p : world.getPlayers()) p.sendMessage("§7The tornado has dissipated.");
                    cancel();
                    return;
                }
                center.add(drift);
                center.setY(world.getHighestBlockYAt(center) + 1);
                for (Entity e : center.getWorld().getNearbyEntities(center, radius, 24, radius)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        if (hasRoof(p, 4)) continue;
                    }
                    double distSq = e.getLocation().distanceSquared(center);
                    if (distSq < radius * radius && e instanceof Player) {
                        ((Player) e).damage(damagePerTick);
                    }
                    if (distSq < radius * radius && !(e instanceof Player)) {
                        Vector toCenter = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.15);
                        e.setVelocity(e.getVelocity().add(toCenter));
                    }
                }
                for (int x = (int) (center.getX() - radius); x <= center.getX() + radius; x++) {
                    for (int z = (int) (center.getZ() - radius); z <= center.getZ() + radius; z++) {
                        if (ThreadLocalRandom.current().nextDouble() >= cropBreakChance) continue;
                        int y = world.getHighestBlockYAt(x, z);
                        Block b = world.getBlockAt(x, y, z);
                        if (b.getType() == Material.WHEAT || b.getType() == Material.CARROTS || b.getType() == Material.POTATOES || b.getType() == Material.BEETROOTS)
                            b.breakNaturally();
                    }
                }
                world.spawnParticle(Particle.CLOUD, center, 30, (float) radius, 12, (float) radius, 0.05);
            }
        };
        runTask.runTaskTimer(plugin, 20L, 5L);
    }

    private boolean hasRoof(Player p, int blocksUp) {
        Location loc = p.getLocation();
        for (int i = 1; i <= blocksUp; i++) {
            Block above = loc.clone().add(0, i, 0).getBlock();
            if (above.getType().isSolid()) return true;
        }
        return false;
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        active = false;
        if (runTask != null) { runTask.cancel(); runTask = null; }
        center = null;
        world = null;
    }

    @Override
    public String getDisplayName() { return "Tornado"; }
}
