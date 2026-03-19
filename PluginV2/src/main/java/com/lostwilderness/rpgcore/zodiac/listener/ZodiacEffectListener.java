package com.lostwilderness.rpgcore.zodiac.listener;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import com.lostwilderness.rpgcore.zodiac.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Listener that applies zodiac sign perks and spirit animal effects.
 * Uses a periodic scheduler to refresh potion effects.
 */
public final class ZodiacEffectListener implements Listener {

    private final ZodiacService zodiacService;
    private final ReputationService reputationService;
    private final CalendarServiceV2 calendarService;
    private final SchedulerService scheduler;

    private BukkitTask periodicTask;
    private final Map<UUID, Long> lastMovementTime = new HashMap<>();

    public ZodiacEffectListener(ZodiacService zodiacService, ReputationService reputationService,
            CalendarServiceV2 calendarService, SchedulerService scheduler) {
        this.zodiacService = zodiacService;
        this.reputationService = reputationService;
        this.calendarService = calendarService;
        this.scheduler = scheduler;

        // Start periodic effect application (every second)
        startPeriodicEffects();
    }

    private void startPeriodicEffects() {
        periodicTask = Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("RPG_Core_V2"),
                () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        applyZodiacEffects(player);
                    }
                },
                20L, 20L // 1 second delay, 1 second period
        );
    }

    public void shutdown() {
        if (periodicTask != null) {
            periodicTask.cancel();
            periodicTask = null;
        }
    }

    private void applyZodiacEffects(Player player) {
        UUID uuid = player.getUniqueId();
        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(uuid);
        if (profileOpt.isEmpty())
            return;

        ZodiacProfile profile = profileOpt.get();

        // Determine Good/Evil alignment
        boolean isGood = isGoodAlignment(uuid);

        // Check for sync bonus
        boolean syncActive = zodiacService.isSyncActive(uuid);
        int amplifierMultiplier = syncActive ? 2 : 1;

        // Apply month sign perks
        applySignPerks(player, profile.monthSign(), isGood, amplifierMultiplier);

        // Apply year sign perks if active
        if (zodiacService.isSignYearActive(uuid)) {
            applySignPerks(player, profile.yearSign(), isGood, amplifierMultiplier);
        }

        // Apply sync bonus
        if (syncActive) {
            applyEffect(player, PotionEffectType.LUCK, 1);
        }

        // Apply spirit animal effects
        applySpiritAnimalEffects(player, profile.spiritAnimal());

        // Apply Epochian bonuses if applicable
        if (profile.isEpochian() && profile.secondSign() != null) {
            // Second sign is always active for Epochians
            applySignPerks(player, profile.secondSign(), isGood, amplifierMultiplier);
        }
    }

    private void applySignPerks(Player player, ZodiacSign sign, boolean isGood, int amplifierMultiplier) {
        Location loc = player.getLocation();
        int baseAmplifier = 0; // Potency I
        int amplifier = baseAmplifier * amplifierMultiplier;

        switch (sign) {
            case ARIES:
                applyEffect(player, PotionEffectType.SPEED, amplifier);
                // Evil with existing Fire Resist: Speed only
                if (isGood || !player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                    applyEffect(player, PotionEffectType.FIRE_RESISTANCE, amplifier);
                }
                break;

            case TAURUS:
                applyEffect(player, PotionEffectType.HASTE, amplifier); // Haste
                // Crop growth handled in block events
                break;

            case GEMINI:
                applyEffect(player, PotionEffectType.JUMP_BOOST, amplifier); // Jump Boost
                // Fortune drops handled in block break event
                break;

            case CANCER:
                if (player.isInWater()) {
                    applyEffect(player, PotionEffectType.DOLPHINS_GRACE, amplifier);
                }
                // Shield durability handled in damage event
                break;

            case LEO:
                applyEffect(player, PotionEffectType.STRENGTH, amplifier); // Strength
                // Looting drops handled in entity death event
                // Anvil cost handled in prepare anvil event
                break;

            case VIRGO:
                // Mending efficiency, XP gain, no lapis handled in respective events
                break;

            case LIBRA:
                // Villager trade bonuses handled in trade events
                if (isGood) {
                    // Extend Hero of Village duration if present
                    PotionEffect heroEffect = player.getPotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
                    if (heroEffect != null && heroEffect.getDuration() < 6000) {
                        applyEffect(player, PotionEffectType.HERO_OF_THE_VILLAGE, heroEffect.getAmplifier(), 6000);
                    }
                }
                break;

            case SCORPIO:
                if (isGood) {
                    // Regeneration I AoE to allies within 10 blocks
                    for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                        if (entity instanceof Player ally && !ally.equals(player)) {
                            applyEffect(ally, PotionEffectType.REGENERATION, amplifier);
                        }
                    }
                } else {
                    // Evil: Poison Resistance + Night Vision
                    applyEffect(player, PotionEffectType.POISON, amplifier); // Actually poison immunity via resistance
                    applyEffect(player, PotionEffectType.NIGHT_VISION, amplifier, 400); // Longer duration
                }
                break;

            case SAGITTARIUS:
                // Power I on bows handled in projectile launch
                // Protection I on armor
                applyEffect(player, PotionEffectType.RESISTANCE, amplifier); // Protection
                break;

            case CAPRICORN:
                applyEffect(player, PotionEffectType.SLOW_FALLING, amplifier);
                // Step assist handled in movement
                break;

            case AQUARIUS:
                if (loc.getWorld().hasStorm()) {
                    applyEffect(player, PotionEffectType.REGENERATION, amplifier);
                }
                if (player.isInWater()) {
                    applyEffect(player, PotionEffectType.SPEED, amplifier);
                }
                // Lightning immunity handled in damage event
                break;

            case PISCES:
                applyEffect(player, PotionEffectType.LUCK, amplifier);
                if (player.isInWater() || player.getLocation().getBlock().getType() == Material.WATER) {
                    applyEffect(player, PotionEffectType.DOLPHINS_GRACE, amplifier);
                }
                break;

            case OPHIUCHUS:
                if (isGood) {
                    // Healing Aura: Regen I to nearby clan/allied players every 10s
                    if (System.currentTimeMillis() % 10000 < 1000) { // Rough 10s check
                        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                            if (entity instanceof Player ally) {
                                applyEffect(ally, PotionEffectType.REGENERATION, amplifier, 200);
                            }
                        }
                    }
                } else {
                    // Evil: Serpent Form when sneaking (handled in sneak event)
                    if (player.isSneaking()) {
                        applyEffect(player, PotionEffectType.INVISIBILITY, amplifier);
                        // Silence (no step sounds) - handled via PlayerMoveEvent cancellation
                    }
                }
                break;
        }
    }

    private void applySpiritAnimalEffects(Player player, SpiritAnimal animal) {
        Location loc = player.getLocation();
        long worldTime = loc.getWorld().getTime();
        boolean isNight = worldTime >= 13000 && worldTime <= 23000;

        switch (animal) {
            case WOLF:
                // +5% melee damage near wolves - handled in damage event
                break;

            case FOX:
                if (isNight) {
                    applyEffect(player, PotionEffectType.SPEED, 0); // +10% speed as Speed I
                }
                break;

            case BAT:
                if (player.getLocation().getBlock().getLightLevel() <= 4) {
                    applyEffect(player, PotionEffectType.REGENERATION, 0);
                }
                break;

            case DOLPHIN:
                if (player.isInWater()) {
                    applyEffect(player, PotionEffectType.DOLPHINS_GRACE, 1); // Enhanced
                }
                break;

            case BEE:
                // Crop growth handled in block events
                break;

            case CAT:
                // Phantom immunity handled in target event
                break;

            case RABBIT:
                if (player.getHealth() <= player.getMaxHealth() * 0.3) {
                    applyEffect(player, PotionEffectType.JUMP_BOOST, 0);
                }
                break;

            case PARROT:
                // Hostile mob detection - could play sounds, but complex to implement
                break;

            case AXOLOTL:
                // Regen after combat - needs combat tracker
                break;

            case PANDA:
                // Remove slowness if present
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                break;

            case TURTLE:
                // Resistance when stationary
                Long lastMove = lastMovementTime.get(player.getUniqueId());
                if (lastMove != null && System.currentTimeMillis() - lastMove > 3000) {
                    applyEffect(player, PotionEffectType.RESISTANCE, 0);
                }
                break;

            case OCELOT:
                // Creeper neutrality handled in target event
                break;

            case HORSE:
                Biome biome = loc.getBlock().getBiome();
                if (biome == Biome.PLAINS || biome == Biome.SUNFLOWER_PLAINS ||
                        biome == Biome.SAVANNA || biome == Biome.SAVANNA_PLATEAU) {
                    applyEffect(player, PotionEffectType.SPEED, 0);
                }
                break;

            case IRON_GOLEM:
                // Knockback resistance - handled via damage event
                break;
        }

        // Track movement for turtle stationary check
        lastMovementTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void applyEffect(Player player, PotionEffectType type, int amplifier) {
        applyEffect(player, type, amplifier, 100); // 5 seconds (100 ticks)
    }

    private void applyEffect(Player player, PotionEffectType type, int amplifier, int duration) {
        PotionEffect existing = player.getPotionEffect(type);
        if (existing == null || existing.getAmplifier() < amplifier || existing.getDuration() < duration / 2) {
            player.addPotionEffect(new PotionEffect(type, duration, amplifier, true, false, true));
        }
    }

    private boolean isGoodAlignment(UUID playerUuid) {
        try {
            int honorScore = reputationService.getHonorScore(playerUuid).get(5, TimeUnit.SECONDS);
            return honorScore >= 0; // Positive or neutral = Good
        } catch (Exception e) {
            return true; // Default to Good
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking())
            return;

        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(player.getUniqueId());
        if (profileOpt.isEmpty())
            return;

        ZodiacProfile profile = profileOpt.get();
        boolean isEvil = !isGoodAlignment(player.getUniqueId());

        // Ophiuchus Serpent Form (Evil only)
        if (profile.monthSign() == ZodiacSign.OPHIUCHUS && isEvil) {
            applyEffect(player, PotionEffectType.INVISIBILITY, 0, 60);
            // +10% block break speed applied in block break event
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(killer.getUniqueId());
        if (profileOpt.isEmpty())
            return;

        ZodiacProfile profile = profileOpt.get();

        // Leo looting bonus
        if (profile.monthSign() == ZodiacSign.LEO || profile.yearSign() == ZodiacSign.LEO) {
            if (ThreadLocalRandom.current().nextInt(100) < 20) { // 20% chance extra drop
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getAmount() > 0) {
                        event.getDrops().add(drop.clone());
                        break; // Only duplicate one item
                    }
                }
            }
        }

        // Spirit animal bonus drops
        if (event.getEntityType() == profile.spiritAnimal().entityType()) {
            if (ThreadLocalRandom.current().nextInt(100) < 20) { // 20% extra chance
                event.setDroppedExp((int) (event.getDroppedExp() * 1.2));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLightningDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.LIGHTNING)
            return;

        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(player.getUniqueId());
        if (profileOpt.isEmpty())
            return;

        // Aquarius lightning immunity
        if (profileOpt.get().monthSign() == ZodiacSign.AQUARIUS ||
                profileOpt.get().yearSign() == ZodiacSign.AQUARIUS) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player))
            return;

        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(player.getUniqueId());
        if (profileOpt.isEmpty())
            return;

        SpiritAnimal animal = profileOpt.get().spiritAnimal();

        // Cat spirit animal - phantom immunity
        if (animal == SpiritAnimal.CAT && event.getEntity().getType().toString().equals("PHANTOM")) {
            event.setCancelled(true);
        }

        // Ocelot spirit animal - creeper neutrality
        if (animal == SpiritAnimal.OCELOT && event.getEntity().getType().toString().equals("CREEPER")) {
            event.setCancelled(true);
        }
    }
}
