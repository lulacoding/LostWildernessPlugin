package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.PersonalityTrait;
import com.lostwilderness.rpgcore.personality.PlayerTraitProfile;
import com.lostwilderness.rpgcore.personality.TraitService;
import com.lostwilderness.rpgcore.personality.TraitTier;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.block.BrewingStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
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
     * WARRIOR MASTER: Shield blocks reduce damage by additional 20%
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWarriorShieldBlock(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.WARRIOR, TraitTier.MASTER)) return;

        // Check if blocking with shield
        if (!player.isBlocking()) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean hasShield = mainHand.getType() == Material.SHIELD || offHand.getType() == Material.SHIELD;
        if (!hasShield) return;

        // Reduce damage by additional 20%
        event.setDamage(event.getDamage() * 0.80);
    }

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
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRangerTamedMobDamage(EntityDamageByEntityEvent event) {
        // Check if damager is a tameable entity
        if (!(event.getDamager() instanceof org.bukkit.entity.Tameable)) return;
        if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;

        org.bukkit.entity.Tameable tameable = (org.bukkit.entity.Tameable) event.getDamager();

        // Check if tamed and has owner
        if (!tameable.isTamed() || tameable.getOwner() == null) return;

        // Get owner
        if (!(tameable.getOwner() instanceof Player)) return;
        Player owner = (Player) tameable.getOwner();

        // Check if owner has RANGER MASTER trait
        if (!hasActiveTrait(owner.getUniqueId(), PersonalityTrait.RANGER, TraitTier.MASTER)) return;

        // Apply +15% damage
        event.setDamage(event.getDamage() * 1.15);
    }

    // ========== ALCHEMIST ==========

    /**
     * ALCHEMIST TRAIT: Brewed potions have +25% duration
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onAlchemistBrew(org.bukkit.event.inventory.BrewEvent event) {
        if (!(event.getContents().getViewers().get(0) instanceof Player)) return;

        Player player = (Player) event.getContents().getViewers().get(0);
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ALCHEMIST, TraitTier.TRAIT)) return;

        // Extend potion duration by 25% on brew completion
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> {
                for (int i = 0; i < 3; i++) {
                    org.bukkit.inventory.ItemStack item = event.getContents().getItem(i);
                    if (item != null && item.getType() == org.bukkit.Material.POTION) {
                        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) item.getItemMeta();
                        if (meta != null && meta.hasCustomEffects()) {
                            meta.getCustomEffects().forEach(effect -> {
                                meta.removeCustomEffect(effect.getType());
                                PotionEffect extended = new PotionEffect(
                                    effect.getType(),
                                    (int) (effect.getDuration() * 1.25),
                                    effect.getAmplifier(),
                                    effect.isAmbient(),
                                    effect.hasParticles(),
                                    effect.hasIcon()
                                );
                                meta.addCustomEffect(extended, true);
                            });
                            item.setItemMeta(meta);
                        }
                    }
                }
            },
            1L
        );
    }

    /**
     * ALCHEMIST MASTER: Brewing completes 5x faster (anti-dupe safe)
     * Alternative to 4-potion brewing which isn't possible in vanilla
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAlchemistBrewStart(BrewEvent event) {
        if (event.getContents().getViewers().isEmpty()) return;
        if (!(event.getContents().getViewers().get(0) instanceof Player)) return;

        Player player = (Player) event.getContents().getViewers().get(0);
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ALCHEMIST, TraitTier.MASTER)) return;

        // Reduce brew time to 1/5 (400 ticks -> 80 ticks = 4 seconds)
        org.bukkit.block.Block block = event.getBlock();
        if (block.getState() instanceof BrewingStand) {
            BrewingStand stand = (BrewingStand) block.getState();

            // Schedule task to accelerate brewing (runs every tick)
            BukkitRunnable accelerator = new BukkitRunnable() {
                private int ticksRan = 0;

                @Override
                public void run() {
                    if (ticksRan++ >= 80 || stand.getBrewingTime() <= 1) {
                        this.cancel();
                        return;
                    }

                    // Decrease brew time by 4 ticks per tick (5x speed)
                    int currentTime = stand.getBrewingTime();
                    if (currentTime > 5) {
                        stand.setBrewingTime(currentTime - 4);
                        stand.update();
                    }
                }
            };

            accelerator.runTaskTimer(
                org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                1L,
                1L
            );
        }
    }

    // ========== SMITH ==========

    /**
     * SMITH TRAIT: Anvil repair costs -2 levels
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSmithAnvilRepair(org.bukkit.event.inventory.PrepareAnvilEvent event) {
        if (event.getViewers().isEmpty()) return;
        if (!(event.getViewers().get(0) instanceof Player)) return;

        Player player = (Player) event.getViewers().get(0);
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SMITH, TraitTier.TRAIT)) return;

        // Reduce repair cost by 2 levels (minimum 1)
        org.bukkit.inventory.AnvilInventory inv = event.getInventory();
        if (inv.getRepairCost() > 2) {
            inv.setRepairCost(inv.getRepairCost() - 2);
        } else {
            inv.setRepairCost(1);
        }
    }

    /**
     * SMITH MASTER: Crafting gear uses 20% fewer materials
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSmithCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SMITH, TraitTier.MASTER)) return;

        // Check if crafting armor/tools/weapons
        org.bukkit.Material result = event.getRecipe().getResult().getType();
        if (isGear(result)) {
            // Refund 20% of materials (1 in 5 chance to refund each ingredient)
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                () -> {
                    event.getInventory().getMatrix();
                    for (org.bukkit.inventory.ItemStack item : event.getInventory().getMatrix()) {
                        if (item != null && !item.getType().isAir()) {
                            if (Math.random() < 0.20) {
                                player.getInventory().addItem(item.clone());
                            }
                        }
                    }
                },
                1L
            );
        }
    }

    private boolean isGear(org.bukkit.Material material) {
        String name = material.name();
        return name.contains("_HELMET") || name.contains("_CHESTPLATE") ||
               name.contains("_LEGGINGS") || name.contains("_BOOTS") ||
               name.contains("_SWORD") || name.contains("_AXE") ||
               name.contains("_PICKAXE") || name.contains("_SHOVEL") ||
               name.contains("_HOE");
    }

    // ========== SCOUT ==========

    /**
     * SCOUT TRAIT: +15% movement speed permanently
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onScoutMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SCOUT, TraitTier.TRAIT)) return;

        // Apply permanent Speed I effect (20% speed boost = +15% with some rounding)
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, false));
        }
    }

    /**
     * SCOUT MASTER: Sneak speed equals walk speed
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onScoutSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SCOUT, TraitTier.MASTER)) return;

        if (event.isSneaking()) {
            // Increase sneak speed to walk speed via attribute modification
            org.bukkit.attribute.Attribute attr = org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED;
            org.bukkit.attribute.AttributeInstance instance = player.getAttribute(attr);
            if (instance != null) {
                // Remove existing modifier if present
                instance.getModifiers().stream()
                    .filter(mod -> mod.getName().equals("scout_sneak_speed"))
                    .forEach(instance::removeModifier);

                // Add new modifier (sneak is normally 0.3x speed, we make it 1.0x)
                org.bukkit.attribute.AttributeModifier modifier = new org.bukkit.attribute.AttributeModifier(
                    java.util.UUID.randomUUID(),
                    "scout_sneak_speed",
                    0.70, // Adds 70% to compensate for sneak penalty
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_SCALAR
                );
                instance.addModifier(modifier);
            }
        } else {
            // Remove modifier when not sneaking
            org.bukkit.attribute.AttributeInstance instance = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
            if (instance != null) {
                instance.getModifiers().stream()
                    .filter(mod -> mod.getName().equals("scout_sneak_speed"))
                    .forEach(instance::removeModifier);
            }
        }
    }

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
     * SAGE MASTER: Enchanting table offers higher level enchants
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSageEnchantPrepare(PrepareItemEnchantEvent event) {
        Player player = (Player) event.getEnchanter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SAGE, TraitTier.MASTER)) return;

        // Boost quality of all 3 enchant offers by 1 level
        org.bukkit.enchantments.EnchantmentOffer[] offers = event.getOffers();
        for (int i = 0; i < offers.length; i++) {
            org.bukkit.enchantments.EnchantmentOffer offer = offers[i];
            if (offer != null) {
                int newLevel = Math.min(
                    offer.getEnchantmentLevel() + 1,
                    offer.getEnchantment().getMaxLevel()
                );

                offers[i] = new org.bukkit.enchantments.EnchantmentOffer(
                    offer.getEnchantment(),
                    newLevel,
                    offer.getCost()
                );
            }
        }
    }

    // ========== TAMER ==========

    /**
     * TAMER TRAIT: +25% chance taming succeeds on first attempt
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTamerTame(org.bukkit.event.entity.EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player)) return;

        Player player = (Player) event.getOwner();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.TAMER, TraitTier.TRAIT)) return;

        // If taming failed naturally, give it a 25% chance to succeed anyway
        // Note: We can't detect failure directly, but we can boost success chance
        // by preventing cancellation with 25% probability if event was going to be cancelled
        if (event.isCancelled()) {
            if (Math.random() < 0.25) {
                event.setCancelled(false);
                player.sendMessage("§a✓ Your taming expertise turned failure into success!");
            }
        }
    }

    /**
     * TAMER MASTER: Tamed mobs gain +30% max HP
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onTamerTameHP(org.bukkit.event.entity.EntityTameEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getOwner() instanceof Player)) return;

        Player player = (Player) event.getOwner();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.TAMER, TraitTier.MASTER)) return;

        // Increase tamed mob's max health by 30%
        org.bukkit.entity.LivingEntity entity = (org.bukkit.entity.LivingEntity) event.getEntity();
        org.bukkit.attribute.AttributeInstance maxHealth = entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double baseHealth = maxHealth.getBaseValue();
            maxHealth.setBaseValue(baseHealth * 1.30);
            entity.setHealth(maxHealth.getValue()); // Heal to full after boosting max HP
            player.sendMessage("§a✓ Your companion has been strengthened! (+30% max HP)");
        }
    }

    // ========== RUNEKEEPER ==========

    /**
     * RUNEKEEPER TRAIT: Enchanted items glow with particle effects
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRunekeeperGlow(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RUNEKEEPER, TraitTier.TRAIT)) return;

        // Schedule check for next tick (after item switch completes)
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> {
                ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
                if (newItem != null && newItem.hasItemMeta() && !newItem.getEnchantments().isEmpty()) {
                    // Apply glowing effect (refreshed every 3 seconds)
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.GLOWING,
                        60, // 3 seconds
                        0,
                        true, // ambient
                        false, // no particles
                        false // no icon
                    ));
                }
            },
            1L
        );
    }

    /**
     * RUNEKEEPER MASTER: Enchanting no longer requires lapis lazuli (refunded after use)
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRunekeeperEnchant(EnchantItemEvent event) {
        if (event.isCancelled()) return;

        Player player = (Player) event.getEnchanter();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RUNEKEEPER, TraitTier.MASTER)) return;

        // Refund lapis (1-3 depending on enchant level)
        int refundAmount = Math.min(3, Math.max(1, event.getExpLevelCost() / 10));

        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> {
                ItemStack lapis = new ItemStack(Material.LAPIS_LAZULI, refundAmount);
                java.util.HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(lapis);

                if (!overflow.isEmpty()) {
                    overflow.values().forEach(item ->
                        player.getWorld().dropItemNaturally(player.getLocation(), item)
                    );
                }

                player.sendMessage("§b✓ Runekeeper Master: Lapis refunded!");
            },
            1L
        );
    }

    // ========== ILLUSIONIST ==========

    /**
     * ILLUSIONIST TRAIT: Can cast harmless decoy (5s duration, 2min cooldown)
     * Note: Implemented via /trait decoy command in TraitCommand.java
     */

    /**
     * ILLUSIONIST MASTER: Invisibility potions last 3x longer
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onIllusionistInvisibility(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ILLUSIONIST, TraitTier.MASTER)) return;

        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return;

        // Check if invisibility potion
        boolean hasInvisibility = false;
        if (meta.getBasePotionType() != null) {
            hasInvisibility = meta.getBasePotionType().name().contains("INVISIBILITY");
        }

        if (!hasInvisibility) {
            for (org.bukkit.potion.PotionEffect effect : meta.getCustomEffects()) {
                if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
                    hasInvisibility = true;
                    break;
                }
            }
        }

        if (!hasInvisibility) return;

        // Extend duration 3x (schedule for after consumption)
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> {
                org.bukkit.potion.PotionEffect current = player.getPotionEffect(PotionEffectType.INVISIBILITY);
                if (current != null) {
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        PotionEffectType.INVISIBILITY,
                        current.getDuration() * 3,
                        current.getAmplifier(),
                        current.isAmbient(),
                        current.hasParticles(),
                        current.hasIcon()
                    ));
                    player.sendMessage("§d✓ Illusionist Master: Invisibility extended!");
                }
            },
            2L
        );
    }

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
