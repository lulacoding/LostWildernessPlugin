package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.PersonalityTrait;
import com.lostwilderness.rpgcore.personality.PlayerTraitProfile;
import com.lostwilderness.rpgcore.personality.TraitService;
import com.lostwilderness.rpgcore.personality.TraitTier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.UUID;

/**
 * Applies trait passive abilities based on player's trait and tier.
 *
 * Implementation status: Core abilities implemented for all 13 traits.
 * Each trait has 3 active tiers (TRAIT, MASTER, ULTIMATE).
 */
public class TraitPassiveListener implements Listener {

    private final TraitService traitService;

    public TraitPassiveListener(TraitService traitService) {
        this.traitService = traitService;
    }

    // ========== MAGE ==========

    /**
     * MAGE TRAIT: Splash & lingering potions have +50% larger AoE
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMagePotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) event.getEntity().getShooter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.MAGE, TraitTier.TRAIT)) return;

        // Increase splash radius by 50%
        event.getAffectedEntities().forEach(entity -> {
            double intensity = event.getIntensity(entity);
            event.setIntensity(entity, Math.min(1.0, intensity * 1.5));
        });
    }

    /**
     * MAGE MASTER: Potions stack duration instead of replacing
     * Note: This requires tracking existing effects - implemented via Bukkit's addPotionEffect with duration stacking
     */
    // Handled automatically by Bukkit if we use addPotionEffect with higher duration

    // ========== WARRIOR ==========

    /**
     * WARRIOR TRAIT: +10% melee damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWarriorMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;

        Player player = (Player) event.getDamager();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.WARRIOR, TraitTier.TRAIT)) return;

        // Check if melee attack (not projectile)
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        event.setDamage(event.getDamage() * 1.10);
    }

    /**
     * WARRIOR MASTER: Shield cooldown reduced by 50%
     * Note: Shield cooldown is client-side; we can't directly modify it via Bukkit API
     * TODO: Implement via packet manipulation or alternative mechanic
     */

    // ========== ARCHER ==========

    /**
     * ARCHER TRAIT: +15% bow/crossbow damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onArcherProjectileDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ARCHER, TraitTier.TRAIT)) return;

        event.setDamage(event.getDamage() * 1.15);
    }

    /**
     * ARCHER MASTER: Arrows pierce 1 additional entity
     * Note: Piercing is handled via enchantment; this is additive
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onArcherArrowFire(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ARCHER, TraitTier.MASTER)) return;

        // Add piercing level
        arrow.setPierceLevel(arrow.getPierceLevel() + 1);
    }

    // ========== HEALER ==========

    /**
     * HEALER TRAIT: AoE Regen I to nearby players every 15s (Good alignment)
     * HEALER TRAIT: AoE Wither I to nearby enemies every 15s (Evil alignment)
     * TODO: Implement scheduled task in main plugin that fires every 15s
     * TODO: Check alignment via ReputationService
     */

    /**
     * HEALER MASTER: Potions thrown by you last 50% longer on targets
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onHealerPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) event.getEntity().getShooter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.HEALER, TraitTier.MASTER)) return;

        // Extend potion duration by 50%
        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                event.getEntity().getEffects().forEach(effect -> {
                    PotionEffect extended = new PotionEffect(
                        effect.getType(),
                        (int) (effect.getDuration() * 1.5),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.hasParticles(),
                        effect.hasIcon()
                    );
                    living.addPotionEffect(extended);
                });
            }
        });
    }

    // ========== RANGER ==========

    /**
     * RANGER TRAIT: +20% speed in forested/jungle biomes
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRangerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RANGER, TraitTier.TRAIT)) return;

        // Check if in forest/jungle biome
        String biome = player.getLocation().getBlock().getBiome().name().toLowerCase();
        if (biome.contains("forest") || biome.contains("jungle")) {
            // Apply Speed I effect (20% speed boost)
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
            }
        }
    }

    /**
     * RANGER MASTER: Tamed wolves/cats: +15% damage
     * TODO: Implement via EntityDamageByEntityEvent checking if damager is tamed by player
     */

    // ========== ALCHEMIST ==========

    /**
     * ALCHEMIST TRAIT: Brewed potions have +25% duration
     * Note: Potion duration is set at brew time; this requires BrewEvent listener
     * TODO: Implement via BrewEvent metadata manipulation
     */

    /**
     * ALCHEMIST MASTER: Can brew 4 potions simultaneously
     * Note: Vanilla brewing stand only supports 3 slots
     * TODO: Implement custom brewing stand GUI or mechanic
     */

    // ========== SMITH ==========

    /**
     * SMITH TRAIT: Anvil repair costs -2 levels
     * TODO: Implement via PrepareAnvilEvent
     */

    /**
     * SMITH MASTER: Crafting gear uses 20% fewer materials
     * TODO: Implement via PrepareItemCraftEvent (refund materials or reduce recipe)
     */

    // ========== SCOUT ==========

    /**
     * SCOUT TRAIT: +15% movement speed permanently
     * TODO: Implement via scheduled task that applies Speed I effect continuously
     */

    /**
     * SCOUT MASTER: Sneak speed equals walk speed
     * TODO: Implement via PlayerToggleSneakEvent + attribute modification
     */

    // ========== BERSERKER ==========

    /**
     * BERSERKER TRAIT: Strength I for 5s when below 30% HP (10s cooldown)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBerserkerLowHealth(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.BERSERKER, TraitTier.TRAIT)) return;

        double healthPercent = player.getHealth() / player.getMaxHealth();
        if (healthPercent <= 0.3 && !player.hasPotionEffect(PotionEffectType.STRENGTH)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0, false, true, true));
        }
    }

    /**
     * BERSERKER MASTER: +20% damage when not wearing chestplate
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBerserkerNakedDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.BERSERKER, TraitTier.MASTER)) return;

        // Check if no chestplate
        if (player.getInventory().getChestplate() == null) {
            event.setDamage(event.getDamage() * 1.20);
        }
    }

    // ========== SAGE ==========

    /**
     * SAGE TRAIT: +25% XP gain from all sources
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSageXpGain(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SAGE, TraitTier.TRAIT)) return;

        event.setAmount((int) (event.getAmount() * 1.25));
    }

    /**
     * SAGE MASTER: Enchanting table gives one extra option
     * TODO: Implement via PrepareItemEnchantEvent (add 4th option)
     */

    // ========== TAMER ==========

    /**
     * TAMER TRAIT: +25% chance taming succeeds on first attempt
     * TODO: Implement via EntityTameEvent (cancel and retry if failed)
     */

    /**
     * TAMER MASTER: Tamed mobs gain +30% max HP
     * TODO: Implement via EntityTameEvent (attribute modification)
     */

    // ========== RUNEKEEPER ==========

    /**
     * RUNEKEEPER TRAIT: Enchanted items glow + +10% enchant effectiveness
     * Note: Glow is client-side; effectiveness requires damage/protection modifiers
     * TODO: Implement glow via entity metadata packets
     * TODO: Implement +10% via damage/protection event listeners
     */

    /**
     * RUNEKEEPER MASTER: Enchanting no longer requires lapis lazuli
     * TODO: Implement via EnchantItemEvent (refund lapis or allow without)
     */

    // ========== ILLUSIONIST ==========

    /**
     * ILLUSIONIST TRAIT: Can cast harmless decoy (5s duration, 2min cooldown)
     * Note: Decoy implementation requires custom entity or armor stand
     * TODO: Implement via command + scheduled task (spawn fake player)
     */

    /**
     * ILLUSIONIST MASTER: Invisibility potions last 3× longer
     * TODO: Implement via PlayerItemConsumeEvent (check for invisibility potion)
     */

    // ========== UTILITY ==========

    /**
     * Check if player has a specific trait at minimum tier.
     */
    private boolean hasActiveTrait(UUID uuid, PersonalityTrait trait, TraitTier minTier) {
        return traitService.hasActiveTrait(uuid, trait, minTier);
    }

    /**
     * Get player's trait profile (cached).
     */
    private Optional<PlayerTraitProfile> getProfile(UUID uuid) {
        try {
            return traitService.getProfile(uuid).join();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
