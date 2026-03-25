package com.lostwilderness.rpgcore.pets.task;

import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import com.lostwilderness.rpgcore.zodiac.ZodiacSign;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Runs every 5 seconds (100 ticks) on the main thread.
 * Applies zodiac passive effects to pets within 32 blocks of owner.
 * Applies zodiac sync bonus when pet sign matches owner's current month sign.
 *
 * Phase 3 effects (simple potion-based):
 * - Aries: Speed I + Fire Resistance
 * - Cancer: Water Breathing
 * - Virgo: Regeneration I
 * - Libra: Resistance I
 * - Scorpio: Night Vision
 * - Aquarius: Fire Resistance
 *
 * Complex effects (Phase 4):
 * - Leo: Damage boost via EntityDamageEvent
 * - Taurus: Breeding cooldown via EntityBreedEvent
 * - Sagittarius: Mob detection via particles
 * - Capricorn: Fall damage resistance via EntityDamageEvent
 * - Gemini: Step height modification
 * - Pisces: Fishing luck via PlayerFishEvent
 * - Ophiuchus: Healing aura for nearby pets
 */
public final class PetZodiacEffectTask implements Runnable {

    // Duration 200 ticks (10s) = 2x the 5s schedule interval, so effects never expire mid-cycle
    private static final int EFFECT_DURATION = 200;
    // Amplifier 0 = level I, 1 = level II
    private static final int AMPLIFIER_1 = 0;
    private static final double PROXIMITY_RADIUS = 32.0;

    private final PetService petService;
    private final ZodiacService zodiacService;
    private final Plugin plugin;
    private BukkitTask task;

    public PetZodiacEffectTask(PetService petService, ZodiacService zodiacService, Plugin plugin) {
        this.petService = petService;
        this.zodiacService = zodiacService;
        this.plugin = plugin;
    }

    /**
     * Start the repeating task.
     */
    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 100L, 100L); // 5s delay, 5s period
    }

    /**
     * Stop the repeating task.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        ZodiacSign currentMonthSign = zodiacService.getCurrentMonthSign();

        for (Player player : Bukkit.getOnlinePlayers()) {
            processPlayer(player, currentMonthSign);
        }
    }

    private void processPlayer(Player player, ZodiacSign currentMonthSign) {
        java.util.Set<UUID> petUuids = petService.getCachedPetUuids(player.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;

            // Find the live entity
            Entity entity = Bukkit.getEntity(petUuid);
            if (entity == null || !(entity instanceof LivingEntity petEntity)) continue;

            // Proximity check: same world, within 32 blocks, chunk loaded
            if (!entity.getWorld().equals(player.getWorld())) continue;
            if (entity.getLocation().distanceSquared(player.getLocation()) > PROXIMITY_RADIUS * PROXIMITY_RADIUS) continue;
            if (!entity.getChunk().isLoaded()) continue;

            // Apply zodiac effect to the pet entity
            applyZodiacEffect(petEntity, profile.zodiacSign());

            // Sync bonus: pet sign matches current month sign
            if (profile.zodiacSign() == currentMonthSign) {
                applySyncBonus(player, petEntity);
            }
        }
    }

    private void applyZodiacEffect(LivingEntity pet, ZodiacSign sign) {
        switch (sign) {
            case ARIES -> {
                applyEffect(pet, PotionEffectType.SPEED, AMPLIFIER_1);
                applyEffect(pet, PotionEffectType.FIRE_RESISTANCE, AMPLIFIER_1);
            }
            case CANCER -> applyEffect(pet, PotionEffectType.WATER_BREATHING, AMPLIFIER_1);
            case VIRGO -> applyEffect(pet, PotionEffectType.REGENERATION, AMPLIFIER_1);
            case LIBRA -> applyEffect(pet, PotionEffectType.RESISTANCE, AMPLIFIER_1);
            case SCORPIO -> applyEffect(pet, PotionEffectType.NIGHT_VISION, AMPLIFIER_1);
            case AQUARIUS -> applyEffect(pet, PotionEffectType.FIRE_RESISTANCE, AMPLIFIER_1);
            // Phase 4: LEO, TAURUS, SAGITTARIUS, CAPRICORN, GEMINI, PISCES, OPHIUCHUS
            default -> {} // No simple effect yet
        }
    }

    /**
     * Sync bonus: when pet sign matches current month sign.
     * Applies Speed I + Strength I to the owner player.
     * The +15% all stats bonus is approximated via potions.
     */
    private void applySyncBonus(Player owner, LivingEntity pet) {
        applyEffect(owner, PotionEffectType.SPEED, AMPLIFIER_1);
        applyEffect(owner, PotionEffectType.STRENGTH, AMPLIFIER_1);
        // Also boost the pet
        applyEffect(pet, PotionEffectType.STRENGTH, AMPLIFIER_1);
    }

    private void applyEffect(LivingEntity entity, PotionEffectType type, int amplifier) {
        entity.addPotionEffect(new PotionEffect(type, EFFECT_DURATION, amplifier, true, false));
    }
}
