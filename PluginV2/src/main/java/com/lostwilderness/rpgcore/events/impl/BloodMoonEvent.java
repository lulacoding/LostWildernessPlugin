package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Blood Moon: night freeze, no sleep, optional mob buff with PDC restore. Config from lw-events-extra or main config.
 */
public final class BloodMoonEvent implements DailyWorldEvent, Listener {

    private static final String TAG_BLOOD_MOON = "lw_blood_moon";

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private final NamespacedKey keyMaxHealth;
    private final NamespacedKey keyAttackDamage;
    private final NamespacedKey keyCustomName;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public BloodMoonEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
        this.keyMaxHealth = new NamespacedKey(plugin, "blood_moon_health");
        this.keyAttackDamage = new NamespacedKey(plugin, "blood_moon_damage");
        this.keyCustomName = new NamespacedKey(plugin, "blood_moon_name");
    }

    private boolean isEnabled() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getBoolean("blood_moon.enabled", true);
        }
        return plugin.getConfig().getBoolean("events.blood-moon.enabled", true);
    }

    private boolean isMobBuffEnabled() {
        return plugin.getConfig().getBoolean("events.blood-moon.mob-buff-enabled", true);
    }

    private double getHealthMultiplier() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getDouble("blood_moon.health_multiplier", 1.5);
        }
        return plugin.getConfig().getDouble("events.blood-moon.health-multiplier", 1.5);
    }

    private double getDamageMultiplier() {
        if (lwConfigs != null && lwConfigs.getEventsExtra() != null) {
            return lwConfigs.getEventsExtra().getDouble("blood_moon.damage_multiplier", 1.5);
        }
        return plugin.getConfig().getDouble("events.blood-moon.damage-multiplier", 1.5);
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        int minDay = lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getInt("blood_moon.min_day", 10)
            : plugin.getConfig().getInt("events.blood-moon.min-day", 10);
        if (day < minDay) return;
        double chance = lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getDouble("blood_moon.chance_per_night", 0.08)
            : plugin.getConfig().getDouble("events.blood-moon.chance-per-night", 0.08);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        active = true;
        boolean freezeNight = lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getBoolean("blood_moon.freeze_night", false)
            : plugin.getConfig().getBoolean("events.blood-moon.freeze-night", false);
        boolean worldTimeNight = lwConfigs != null && lwConfigs.getEventsExtra() != null
            ? lwConfigs.getEventsExtra().getBoolean("blood_moon.world_time_night", true)
            : plugin.getConfig().getBoolean("events.blood-moon.world-time-night", true);

        if (worldTimeNight) world.setTime(18000L);
        if (freezeNight) world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        for (Player p : world.getPlayers()) {
            p.sendTitle("§4Blood Moon", "§7You cannot sleep tonight…", 20, 80, 40);
        }

        final World w = world;
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (freezeNight) w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                w.setTime(6000L);
                restoreAllBloodMoonMobs();
                for (Player p : w.getPlayers()) {
                    p.sendMessage("§6The blood moon fades.");
                }
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!active || !isMobBuffEnabled()) return;
        if (e.getEntity().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        if (!(e.getEntity() instanceof Monster)) return;
        LivingEntity living = (LivingEntity) e.getEntity();
        PersistentDataContainer pdc = living.getPersistentDataContainer();
        double origHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 20.0;
        double origDamage = 0;
        if (living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            origDamage = living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        }
        String origName = living.getCustomName() != null ? living.getCustomName() : "";
        pdc.set(keyMaxHealth, PersistentDataType.DOUBLE, origHealth);
        pdc.set(keyAttackDamage, PersistentDataType.DOUBLE, origDamage);
        pdc.set(keyCustomName, PersistentDataType.STRING, origName);
        double healthMult = getHealthMultiplier();
        double damageMult = getDamageMultiplier();
        if (living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(origHealth * healthMult);
            living.setHealth(Math.min(living.getHealth() * healthMult, origHealth * healthMult));
        }
        if (living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(origDamage * damageMult);
        }
        String display = origName.isEmpty() ? living.getType().name().replace("_", " ") : origName;
        living.setCustomName("§4Blood Moon " + display);
        living.setCustomNameVisible(true);
        living.addScoreboardTag(TAG_BLOOD_MOON);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBed(PlayerBedEnterEvent e) {
        if (!active) return;
        if (e.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage("§4You cannot sleep during a Blood Moon.");
    }

    @Override
    public boolean isActive() { return active; }

    /** Restore a single mob that was buffed during Blood Moon (PDC + tag). */
    private void restoreBloodMoonMob(LivingEntity living) {
        if (!living.getScoreboardTags().contains(TAG_BLOOD_MOON)) return;
        PersistentDataContainer pdc = living.getPersistentDataContainer();
        Double origHealth = pdc.get(keyMaxHealth, PersistentDataType.DOUBLE);
        Double origDamage = pdc.get(keyAttackDamage, PersistentDataType.DOUBLE);
        String origName = pdc.get(keyCustomName, PersistentDataType.STRING);
        if (origHealth != null && living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            living.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(origHealth);
            living.setHealth(Math.min(living.getHealth(), origHealth));
        }
        if (origDamage != null && living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            living.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(origDamage);
        }
        living.setCustomName(origName != null ? origName : null);
        living.setCustomNameVisible(false);
        living.removeScoreboardTag(TAG_BLOOD_MOON);
        pdc.remove(keyMaxHealth);
        pdc.remove(keyAttackDamage);
        pdc.remove(keyCustomName);
    }

    /** Restore all Blood Moon mobs in all overworlds. */
    private void restoreAllBloodMoonMobs() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (org.bukkit.entity.Entity entity : w.getLivingEntities()) {
                if (entity instanceof LivingEntity && entity.getScoreboardTags().contains(TAG_BLOOD_MOON)) {
                    restoreBloodMoonMob((LivingEntity) entity);
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (active) return;
        if (e.getWorld().getEnvironment() != World.Environment.NORMAL) return;
        for (org.bukkit.entity.Entity entity : e.getChunk().getEntities()) {
            if (entity instanceof LivingEntity && entity.getScoreboardTags().contains(TAG_BLOOD_MOON)) {
                restoreBloodMoonMob((LivingEntity) entity);
            }
        }
    }

    @Override
    public void forceEndEarly() {
        restoreAllBloodMoonMobs();
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Blood Moon"; }
}
