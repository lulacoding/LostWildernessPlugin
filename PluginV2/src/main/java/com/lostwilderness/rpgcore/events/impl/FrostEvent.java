package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.calendar.EquatorSettings;
import com.lostwilderness.rpgcore.calendar.EquatorZone;
import com.lostwilderness.rpgcore.events.BlockRestoreManager;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.util.BiomeGroups;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Frost: winter-only; freezes water, slowness/mining fatigue; heat clears effects; optional damage when exposed (not near heat).
 */
public final class FrostEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final BlockRestoreManager blockRestore;
    private final EquatorSettings equator;
    private volatile boolean active = false;

    private boolean isDamageWhenExposed() {
        return plugin.getConfig().getBoolean("events.frost.damage-when-exposed", false);
    }

    private double getFrostDamagePerTick() {
        return plugin.getConfig().getDouble("events.frost.frost-damage-per-tick", 0.5);
    }

    private int getMinArmorPiecesToAvoidDamage() {
        return plugin.getConfig().getInt("events.frost.min-armor-pieces-to-avoid-damage", 2);
    }

    private static int countArmorPieces(Player p) {
        int n = 0;
        if (p.getInventory().getHelmet() != null && !p.getInventory().getHelmet().getType().isAir()) n++;
        if (p.getInventory().getChestplate() != null && !p.getInventory().getChestplate().getType().isAir()) n++;
        if (p.getInventory().getLeggings() != null && !p.getInventory().getLeggings().getType().isAir()) n++;
        if (p.getInventory().getBoots() != null && !p.getInventory().getBoots().getType().isAir()) n++;
        return n;
    }
    private BukkitRunnable heatCheckTask;
    private BukkitRunnable endTask;
    private final Map<Player, BukkitRunnable> overlayTasks = new ConcurrentHashMap<>();

    public FrostEvent(Plugin plugin, CalendarServiceV2 calendar, BlockRestoreManager blockRestore, EquatorSettings equator) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.blockRestore = blockRestore != null ? blockRestore : new BlockRestoreManager(plugin);
        this.equator = equator != null ? equator : EquatorSettings.disabled();
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        if (calendar.getCurrentSnapshot().season() != CalendarServiceV2.Season.WINTER) return;
        int chancePct = plugin.getConfig().getInt("events.frost.chance-pct", 30);
        if (ThreadLocalRandom.current().nextInt(100) >= chancePct) return;
        final World w = world;
        long targetTick = plugin.getConfig().getLong("events.frost.target-tick", 13000L);
        long now = w.getTime();
        long delay = (13000L - now + 24000L) % 24000L;
        if (delay == 0) delay = 24000L;
        active = true;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyFrost(w);
            startHeatCheckTask(w);
            long untilMorning = 24000L - targetTick;
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    blockRestore.restoreAndClear("frost");
                    active = false;
                    endTask = null;
                    for (Player p : w.getPlayers()) {
                        removeEffects(p);
                    }
                    w.getPlayers().forEach(p -> p.sendMessage("§b❄ The frost melts away as the sun rises."));
                }
            };
            endTask.runTaskLater(plugin, untilMorning);
        }, delay);
        w.getPlayers().forEach(p -> p.sendMessage("§b❄ A biting frost sweeps across the cold biomes… ❄"));
    }

    private void applyFrost(World world) {
        for (Player p : world.getPlayers()) {
            if (skipColdFor(p)) continue;
            if (!BiomeGroups.isCold(p.getLocation().getBlock().getBiome())) continue;
            giveEffects(p);
        }
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            int bx = chunk.getX() << 4, bz = chunk.getZ() << 4;
            for (int xx = bx; xx < bx + 16; xx++) {
                for (int zz = bz; zz < bz + 16; zz++) {
                    if (equator.gateColdEvents() && EquatorZone.isInBand(world, zz, equator)) {
                        continue;
                    }
                    int y = world.getHighestBlockYAt(xx, zz);
                    Block b = world.getBlockAt(xx, y, zz);
                    if (b.getType() == Material.WATER) {
                        blockRestore.record("frost", b);
                        b.setType(Material.ICE, false);
                    }
                }
            }
        }
    }

    private void startHeatCheckTask(World world) {
        if (heatCheckTask != null) return;
        heatCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    heatCheckTask = null;
                    return;
                }
                for (Player p : world.getPlayers()) {
                    if (skipColdFor(p)) {
                        removeEffects(p);
                        continue;
                    }
                    boolean inCold = BiomeGroups.isCold(p.getLocation().getBlock().getBiome());
                    boolean nearHeat = isNearHeatSource(p, 3);
                    boolean hasFrost = p.hasPotionEffect(PotionEffectType.SLOWNESS);
                    if (nearHeat && hasFrost) removeEffects(p);
                    else if (active && inCold && !nearHeat && !hasFrost) giveEffects(p);
                    if (active && inCold && !nearHeat && isDamageWhenExposed() && countArmorPieces(p) < getMinArmorPiecesToAvoidDamage()) {
                        double dmg = getFrostDamagePerTick();
                        if (dmg > 0) p.damage(dmg);
                    }
                }
            }
        };
        heatCheckTask.runTaskTimer(plugin, 0L, 5L);
    }

    private void giveEffects(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 60, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 60, 0));
        overlayTasks.computeIfAbsent(p, k -> {
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!p.isOnline()) { overlayTasks.remove(p); return; }
                    var eye = p.getEyeLocation();
                    p.spawnParticle(Particle.FALLING_DUST, eye.getX(), eye.getY(), eye.getZ(), 5, 0.2, 0.1, 0.2, 0,
                        Bukkit.createBlockData(Material.SNOW_BLOCK));
                }
            };
            r.runTaskTimer(plugin, 0L, 10L);
            return r;
        });
    }

    private void removeEffects(Player p) {
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        BukkitRunnable ov = overlayTasks.remove(p);
        if (ov != null) ov.cancel();
    }

    private boolean skipColdFor(Player p) {
        return equator.gateColdEvents() && EquatorZone.isInBand(p, equator);
    }

    private static boolean isNearHeatSource(Player p, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    switch (p.getLocation().getBlock().getRelative(dx, dy, dz).getType()) {
                        case LAVA, CAMPFIRE, SOUL_CAMPFIRE, MAGMA_BLOCK, TORCH, LANTERN, SOUL_TORCH, SOUL_LANTERN -> { return true; }
                        default -> {}
                    }
                }
            }
        }
        return false;
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
        if (!active && heatCheckTask == null && endTask == null) return;
        if (endTask != null) { endTask.cancel(); endTask = null; }
        if (heatCheckTask != null) { heatCheckTask.cancel(); heatCheckTask = null; }
        blockRestore.restoreAndClear("frost");
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                for (Player p : w.getPlayers()) removeEffects(p);
                w.getPlayers().forEach(p -> p.sendMessage("§b❄ The frost melts away as the sun rises."));
            }
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Frost"; }
}
