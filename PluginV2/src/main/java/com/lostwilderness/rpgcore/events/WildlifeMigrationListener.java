package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Aeternum-style wildlife migration: favored spawns get boost chance (extra spawn + particles),
 * discouraged spawns are cancelled; in winter warm-climate animals can be culled.
 * Config: lw-fauna.yml (migration, favored, discouraged) or events.wildlife-migration.*
 */
public final class WildlifeMigrationListener implements Listener {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private final LWConfigs lwConfigs;
    private final Random random = new Random();

    private static final EnumSet<EntityType> SPRING_BOOST = EnumSet.of(
        EntityType.SHEEP, EntityType.RABBIT, EntityType.COW, EntityType.PIG, EntityType.CHICKEN,
        EntityType.HORSE, EntityType.BEE);
    private static final EnumSet<EntityType> SUMMER_BOOST = EnumSet.of(
        EntityType.TROPICAL_FISH, EntityType.COD, EntityType.SALMON, EntityType.DOLPHIN,
        EntityType.TURTLE, EntityType.AXOLOTL, EntityType.FROG, EntityType.PARROT, EntityType.PANDA);
    private static final EnumSet<EntityType> AUTUMN_BOOST = EnumSet.of(
        EntityType.FOX, EntityType.WOLF, EntityType.GOAT, EntityType.MOOSHROOM, EntityType.MULE, EntityType.CAT);
    private static final EnumSet<EntityType> WINTER_BOOST = EnumSet.of(
        EntityType.WOLF, EntityType.POLAR_BEAR, EntityType.SNOW_GOLEM, EntityType.SKELETON_HORSE, EntityType.STRAY);
    private static final EnumSet<EntityType> WARM_CLIMATE = EnumSet.of(
        EntityType.PARROT, EntityType.TROPICAL_FISH, EntityType.PANDA, EntityType.FROG,
        EntityType.AXOLOTL, EntityType.TURTLE, EntityType.DOLPHIN);

    public WildlifeMigrationListener(Plugin plugin, CalendarServiceV2 calendar, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.lwConfigs = lwConfigs;
    }

    private boolean isEnabled() {
        if (plugin.getConfig().getBoolean("events.wildlife-migration.enabled", false)) return true;
        return lwConfigs != null && lwConfigs.getFauna().getBoolean("migration.enabled", false);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!isEnabled()) return;
        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
            && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN
            && e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS) {
            return;
        }
        World w = e.getEntity().getWorld();
        if (w.getEnvironment() != World.Environment.NORMAL) return;

        CalendarServiceV2.Season season = calendar.getCurrentSnapshot().season();
        EntityType type = e.getEntityType();

        Set<EntityType> discouraged = getDiscouraged(season);
        if (discouraged.contains(type)) {
            e.setCancelled(true);
            return;
        }
        if (season == CalendarServiceV2.Season.WINTER && WARM_CLIMATE.contains(type)) {
            double chance = getWarmInWinterChance();
            if (random.nextDouble() < chance) {
                e.setCancelled(true);
                return;
            }
        }

        Set<EntityType> favored = getFavoredSet(season);
        double boostChance = getBoostChance(season);
        if (favored.contains(type) && random.nextDouble() < boostChance) {
            Location loc = e.getLocation();
            double dx = (random.nextDouble() * 2 - 1) * 3;
            double dz = (random.nextDouble() * 2 - 1) * 3;
            Location extra = loc.clone().add(dx, 0, dz);
            extra.setY(w.getHighestBlockYAt(extra) + 1);
            int cx = extra.getBlockX() >> 4, cz = extra.getBlockZ() >> 4;
            if (w.isChunkLoaded(cx, cz)) {
                Entity spawned = w.spawnEntity(extra, type);
                if (spawned instanceof Ageable && random.nextBoolean()) {
                    ((Ageable) spawned).setBaby();
                }
                if (getParticles()) {
                    w.spawnParticle(Particle.HAPPY_VILLAGER, extra, 8, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }
    }

    private Set<EntityType> getFavoredSet(CalendarServiceV2.Season season) {
        if (lwConfigs != null) {
            List<String> list = lwConfigs.getFauna().getStringList("favored." + season.name());
            if (list != null && !list.isEmpty()) {
                Set<EntityType> set = new HashSet<>();
                for (String name : list) {
                    try {
                        set.add(EntityType.valueOf(name.toUpperCase().replace(' ', '_')));
                    } catch (IllegalArgumentException ignored) {}
                }
                return set;
            }
        }
        switch (season) {
            case SPRING: return SPRING_BOOST;
            case SUMMER: return SUMMER_BOOST;
            case AUTUMN: return AUTUMN_BOOST;
            case WINTER: return WINTER_BOOST;
            default: return EnumSet.noneOf(EntityType.class);
        }
    }

    private Set<EntityType> getDiscouraged(CalendarServiceV2.Season season) {
        if (lwConfigs != null) {
            List<String> list = lwConfigs.getFauna().getStringList("discouraged." + season.name());
            if (list != null && !list.isEmpty()) {
                Set<EntityType> set = new HashSet<>();
                for (String name : list) {
                    try {
                        set.add(EntityType.valueOf(name.toUpperCase().replace(' ', '_')));
                    } catch (IllegalArgumentException ignored) {}
                }
                return set;
            }
        }
        if (season == CalendarServiceV2.Season.WINTER) return WARM_CLIMATE;
        return Set.of();
    }

    private double getBoostChance(CalendarServiceV2.Season season) {
        if (lwConfigs != null) {
            String path = "migration.spawn." + season.name().toLowerCase() + "_boost_chance";
            return lwConfigs.getFauna().getDouble(path, 0.2);
        }
        return 0.2;
    }

    private double getWarmInWinterChance() {
        if (lwConfigs != null) {
            return lwConfigs.getFauna().getDouble("migration.soft_despawn.warm_in_winter_chance", 0.25);
        }
        return 0.25;
    }

    private boolean getParticles() {
        return lwConfigs != null && lwConfigs.getFauna().getBoolean("migration.particles", true);
    }
}
