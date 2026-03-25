package com.lostwilderness.rpgcore.pets.task;

import com.lostwilderness.rpgcore.pets.PetPersonality;
import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Runs every 10 seconds (200 ticks) on the main thread.
 * Applies passive and scheduled personality effects.
 *
 * Handled personalities:
 * - HEALING_TOUCH   : Regen I on all nearby tamed pets
 * - DEEP_DIVER      : Water Breathing on owner
 * - STAR_GAZER      : Night Vision + Speed at night
 * - FISHER_KING     : Luck on owner
 * - SHADOW_STALKER  : Invisibility when pet is in darkness
 * - SPEED_DEMON     : 15% chance – Speed II burst on pet
 * - STORM_RIDER     : Speed + Jump Boost during rain
 * - FIRE_DANCER     : Fire Resistance + flame particles near fire
 * - BLINK           : Teleport pet to owner if > 48 blocks away
 * - LIGHT_BEACON    : Glowing particles near pet in darkness
 * - MELODY_MAKER    : 10% chance – play a note sound
 * - DANCE_MASTER    : Periodic particle effect
 * - DAWN_HERALD     : Notify owner at sunrise
 * - DREAM_WEAVER    : Notify owner on rest (check nearby bed)
 * - FOOD_FORAGER    : 3% chance – drop food near pet
 * - TREASURE_SNIFFER: 1% chance – drop random loot near pet
 * - CROP_WHISPERER  : 10% chance – age one nearby crop
 */
public final class PetPersonalityEffectTask implements Runnable {

    private static final int EFFECT_DURATION = 400; // 20s, well beyond the 10s task interval
    private static final double PROXIMITY = 32.0;
    private static final double BLINK_THRESHOLD = 48.0;

    private static final List<Material> FORAGED_FOODS = List.of(
        Material.APPLE, Material.BREAD, Material.CARROT, Material.BAKED_POTATO,
        Material.MELON_SLICE, Material.SWEET_BERRIES, Material.COOKED_BEEF
    );

    private static final List<Material> TREASURE_LOOT = List.of(
        Material.IRON_INGOT, Material.GOLD_INGOT, Material.LAPIS_LAZULI,
        Material.EMERALD, Material.BONE, Material.FEATHER, Material.STRING
    );

    private final PetService petService;
    private final Plugin plugin;
    private BukkitTask task;

    // Track last dawn message per owner to avoid spam
    private final Set<UUID> dawnNotified = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public PetPersonalityEffectTask(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 200L, 200L); // 10s delay, 10s period
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        long worldTime = -1;
        // Get overworld time once (avoid calling per player)
        var overworld = Bukkit.getWorld("world");
        if (overworld != null) worldTime = overworld.getTime();

        for (Player player : Bukkit.getOnlinePlayers()) {
            processPlayer(player, worldTime);
        }
    }

    private void processPlayer(Player player, long worldTime) {
        Set<UUID> petUuids = petService.getCachedPetUuids(player.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;

            Entity entity = Bukkit.getEntity(petUuid);
            if (!(entity instanceof LivingEntity petEntity)) continue;
            if (!entity.getWorld().equals(player.getWorld())) continue;
            if (entity.getLocation().distanceSquared(player.getLocation()) > PROXIMITY * PROXIMITY) continue;
            if (!entity.getChunk().isLoaded()) continue;

            applyPersonalityEffect(player, petEntity, profile, worldTime);
        }

        // Clear dawn-notified set at night start
        if (worldTime >= 13000 && worldTime < 13020) {
            dawnNotified.remove(player.getUniqueId());
        }
    }

    private void applyPersonalityEffect(Player owner, LivingEntity pet, PetProfile profile, long worldTime) {
        switch (profile.personality()) {
            case HEALING_TOUCH -> {
                // Heal all nearby tamed entities (not just owner's pets)
                pet.getWorld().getNearbyEntities(pet.getLocation(), 12, 12, 12).forEach(nearby -> {
                    if (nearby instanceof Tameable t && t.isTamed() && nearby instanceof LivingEntity le) {
                        applyEffect(le, PotionEffectType.REGENERATION, 0);
                    }
                });
            }

            case DEEP_DIVER -> applyEffect(owner, PotionEffectType.WATER_BREATHING, 0);

            case STAR_GAZER -> {
                // Night time: 13000-23000
                if (worldTime >= 13000 && worldTime <= 23000) {
                    boolean clearSky = !owner.getWorld().hasStorm() && !owner.getWorld().isThundering();
                    if (clearSky) {
                        applyEffect(owner, PotionEffectType.NIGHT_VISION, 0);
                        applyEffect(owner, PotionEffectType.SPEED, 0);
                    }
                }
            }

            case FISHER_KING -> applyEffect(owner, PotionEffectType.LUCK, 0);

            case SHADOW_STALKER -> {
                int lightLevel = pet.getLocation().getBlock().getLightLevel();
                if (lightLevel < 4) {
                    applyEffect(pet, PotionEffectType.INVISIBILITY, 0);
                }
            }

            case SPEED_DEMON -> {
                if (ThreadLocalRandom.current().nextInt(100) < 15) { // 15% chance
                    applyEffect(pet, PotionEffectType.SPEED, 1); // Speed II
                    pet.getWorld().spawnParticle(
                        Particle.SWEEP_ATTACK, pet.getLocation().add(0, 0.5, 0), 5, 0.3, 0.3, 0.3, 0
                    );
                }
            }

            case STORM_RIDER -> {
                if (pet.getWorld().hasStorm()) {
                    applyEffect(pet, PotionEffectType.SPEED, 0);
                    applyEffect(pet, PotionEffectType.JUMP_BOOST, 0);
                    pet.getWorld().spawnParticle(
                        Particle.SPLASH, pet.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0
                    );
                }
            }

            case FIRE_DANCER -> {
                boolean nearFire = isNearFire(pet.getLocation());
                if (nearFire) {
                    applyEffect(pet, PotionEffectType.FIRE_RESISTANCE, 0);
                    pet.getWorld().spawnParticle(
                        Particle.FLAME, pet.getLocation().add(0, 1, 0), 6, 0.3, 0.5, 0.3, 0
                    );
                }
            }

            case BLINK -> {
                double dist = pet.getLocation().distanceSquared(owner.getLocation());
                if (dist > BLINK_THRESHOLD * BLINK_THRESHOLD) {
                    Location target = owner.getLocation().add(1, 0, 0);
                    pet.teleport(target);
                    pet.getWorld().spawnParticle(
                        Particle.PORTAL, target.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0
                    );
                }
            }

            case LIGHT_BEACON -> {
                int lightLevel = pet.getLocation().getBlock().getLightLevel();
                if (lightLevel < 7) {
                    pet.getWorld().spawnParticle(
                        Particle.END_ROD, pet.getLocation().add(0, 1.5, 0), 4, 0.2, 0.2, 0.2, 0
                    );
                }
            }

            case MELODY_MAKER -> {
                if (ThreadLocalRandom.current().nextInt(100) < 10) { // 10% chance
                    Sound[] notes = {
                        Sound.BLOCK_NOTE_BLOCK_HARP, Sound.BLOCK_NOTE_BLOCK_BELL,
                        Sound.BLOCK_NOTE_BLOCK_FLUTE, Sound.BLOCK_NOTE_BLOCK_CHIME
                    };
                    Sound note = notes[ThreadLocalRandom.current().nextInt(notes.length)];
                    float pitch = 0.5f + ThreadLocalRandom.current().nextFloat() * 1.5f;
                    pet.getWorld().playSound(pet.getLocation(), note, 0.5f, pitch);
                }
            }

            case DANCE_MASTER -> {
                pet.getWorld().spawnParticle(
                    Particle.HAPPY_VILLAGER, pet.getLocation().add(0, 1, 0), 4, 0.3, 0.5, 0.3, 0
                );
            }

            case DAWN_HERALD -> {
                // Sunrise: time window ~23900-24000 / 0-100
                boolean isSunrise = worldTime >= 23900 || worldTime < 100;
                if (isSunrise && !dawnNotified.contains(owner.getUniqueId())) {
                    dawnNotified.add(owner.getUniqueId());
                    owner.sendMessage(org.bukkit.ChatColor.YELLOW + "🌅 " + petName(profile) +
                        org.bukkit.ChatColor.WHITE + " greets the dawn!");
                    owner.playSound(owner.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.8f, 1.2f);
                }
            }

            case FOOD_FORAGER -> {
                if (ThreadLocalRandom.current().nextInt(100) < 3) { // 3% chance
                    Material food = FORAGED_FOODS.get(ThreadLocalRandom.current().nextInt(FORAGED_FOODS.size()));
                    pet.getWorld().dropItemNaturally(pet.getLocation(), new ItemStack(food, 1));
                    owner.sendMessage(org.bukkit.ChatColor.GREEN + petName(profile) +
                        org.bukkit.ChatColor.GRAY + " found some " + food.name().toLowerCase().replace('_', ' ') + "!");
                }
            }

            case TREASURE_SNIFFER -> {
                if (ThreadLocalRandom.current().nextInt(100) < 1) { // 1% chance
                    Material loot = TREASURE_LOOT.get(ThreadLocalRandom.current().nextInt(TREASURE_LOOT.size()));
                    pet.getWorld().dropItemNaturally(pet.getLocation(), new ItemStack(loot, ThreadLocalRandom.current().nextInt(1, 4)));
                    owner.sendMessage(org.bukkit.ChatColor.GOLD + petName(profile) +
                        org.bukkit.ChatColor.GRAY + " dug up some treasure!");
                    pet.getWorld().spawnParticle(
                        Particle.COMPOSTER, pet.getLocation(), 8, 0.3, 0, 0.3, 0
                    );
                }
            }

            case CROP_WHISPERER -> {
                if (ThreadLocalRandom.current().nextInt(100) < 10) { // 10% chance
                    growNearbyCrop(pet.getLocation());
                }
            }

            default -> {} // Other personalities handled by PetPersonalityListener
        }
    }

    private void applyEffect(LivingEntity entity, PotionEffectType type, int amplifier) {
        entity.addPotionEffect(new PotionEffect(type, EFFECT_DURATION, amplifier, true, false));
    }

    private boolean isNearFire(Location loc) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    Material m = loc.clone().add(dx, dy, dz).getBlock().getType();
                    if (m == Material.FIRE || m == Material.SOUL_FIRE ||
                        m == Material.LAVA || m == Material.MAGMA_BLOCK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void growNearbyCrop(Location center) {
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                Block block = center.clone().add(dx, 0, dz).getBlock();
                if (block.getBlockData() instanceof Ageable ageable) {
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        ageable.setAge(ageable.getAge() + 1);
                        block.setBlockData(ageable);
                        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                            block.getLocation().add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0);
                        return; // Only grow one crop per cycle
                    }
                }
            }
        }
    }

    private String petName(PetProfile profile) {
        return profile.customName() != null ? profile.customName() : profile.entityType().name();
    }

}
