package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.HolyEnchant;
import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies Holy Enchant effects from items with holy enchants in PDC.
 *
 * 13 Holy Enchants implemented:
 * - SOULFIRE: Burns enemies with divine flame bypassing armor
 * - DIVINE_SHIELD: Grants absorption hearts when blocking
 * - CELESTIAL_STRIKE: Calls down lightning on critical hits
 * - VOID_PIERCE: Arrows ignore portion of armor
 * - NATURES_GRASP: Roots enemies in place on hit
 * - THUNDERCLAP: Sprint attacks create knockback shockwaves
 * - SERPENTS_FANG: Applies stacking poison on melee
 * - LUNAR_BLESSING: Regenerates health during nighttime
 * - STARFALL: Arrows rain additional projectiles
 * - ANCIENT_WARD: Reduces incoming damage by flat amount
 * - PHOENIX_FLAME: Revive once per day with fire immunity
 * - TITANIC_FORCE: Bonus damage based on target's max HP
 * - ECHO_STEP: Leaves afterimages that confuse enemies
 */
public class HolyEnchantEffectListener implements Listener {

    private final HolyEnchantService holyEnchantService;

    // Cooldown tracking for PHOENIX_FLAME (daily cooldown)
    private final Map<UUID, LocalDate> phoenixFlameLastUse = new HashMap<>();

    // Cooldown tracking for ECHO_STEP (per-move cooldown)
    private final Map<UUID, Long> echoStepLastSpawn = new HashMap<>();
    private static final long ECHO_STEP_COOLDOWN_MS = 2000; // 2 seconds between spawns

    public HolyEnchantEffectListener(HolyEnchantService holyEnchantService) {
        this.holyEnchantService = holyEnchantService;
    }

    // ========== SOULFIRE ==========

    /**
     * SOULFIRE: Burns enemies with divine flame bypassing armor
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onSoulfireDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(weapon, HolyEnchant.SOULFIRE)) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Set on fire and deal bonus magic damage
            target.setFireTicks(100); // 5 seconds
            event.setDamage(event.getDamage() + 4.0); // +2 hearts raw damage
        }
    }

    // ========== DIVINE_SHIELD ==========

    /**
     * DIVINE_SHIELD: Grants absorption hearts when blocking damage
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDivineShieldBlock(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // Check shield in off-hand
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (hasHolyEnchant(offhand, HolyEnchant.DIVINE_SHIELD)) {
            // Grant absorption (2 hearts)
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, true, true));
        }

        // Check chestplate
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && hasHolyEnchant(chestplate, HolyEnchant.DIVINE_SHIELD)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1, false, true, true));
        }
    }

    // ========== CELESTIAL_STRIKE ==========

    /**
     * CELESTIAL_STRIKE: Calls down lightning on critical hits (20% chance)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCelestialStrike(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(weapon, HolyEnchant.CELESTIAL_STRIKE)) {
            // 20% chance on hit
            if (Math.random() < 0.20) {
                LivingEntity target = (LivingEntity) event.getEntity();
                target.getWorld().strikeLightning(target.getLocation());
            }
        }
    }

    // ========== VOID_PIERCE ==========

    /**
     * VOID_PIERCE: Arrows ignore 50% of target's armor
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onVoidPierceArrow(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(bow, HolyEnchant.VOID_PIERCE)) {
            // Increase base damage to simulate armor penetration
            event.setDamage(event.getDamage() * 1.5);
        }
    }

    // ========== NATURES_GRASP ==========

    /**
     * NATURES_GRASP: Roots enemies in place on hit (3 seconds)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onNaturesGrasp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        ItemStack weapon = null;
        if (event.getDamager() instanceof Player) {
            weapon = ((Player) event.getDamager()).getInventory().getItemInMainHand();
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                weapon = ((Player) arrow.getShooter()).getInventory().getItemInMainHand();
            }
        }

        if (weapon != null && hasHolyEnchant(weapon, HolyEnchant.NATURES_GRASP)) {
            LivingEntity target = (LivingEntity) event.getEntity();
            // Apply slowness to simulate rooting
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 10, false, true, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 128, false, false, false));
        }
    }

    // ========== THUNDERCLAP ==========

    /**
     * THUNDERCLAP: Sprint attacks create knockback shockwaves
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onThunderclap(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack boots = player.getInventory().getBoots();

        if (boots != null && hasHolyEnchant(boots, HolyEnchant.THUNDERCLAP) && player.isSprinting()) {
            // Knockback nearby entities
            player.getNearbyEntities(5, 3, 5).forEach(entity -> {
                if (entity instanceof LivingEntity && entity != player) {
                    entity.setVelocity(entity.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(1.5)
                        .setY(0.5));
                }
            });
        }
    }

    // ========== SERPENTS_FANG ==========

    /**
     * SERPENTS_FANG: Applies stacking poison on melee
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSerpentsFang(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(weapon, HolyEnchant.SERPENTS_FANG)) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Get existing poison duration or start at 0
            PotionEffect existing = target.getPotionEffect(PotionEffectType.POISON);
            int newDuration = existing != null ? existing.getDuration() + 100 : 100;

            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, newDuration, 0, false, true, true));
        }
    }

    // ========== LUNAR_BLESSING ==========

    /**
     * LUNAR_BLESSING: Regenerates health during nighttime
     * Note: Implemented via scheduled task in SurvivalV2Plugin
     * Task checks all players with LUNAR_BLESSING helmet during night (time > 13000 && < 23000)
     */

    // ========== STARFALL ==========

    /**
     * STARFALL: Arrows rain additional projectiles on impact
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStarfall(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(bow, HolyEnchant.STARFALL)) {
            Location hitLoc = arrow.getLocation();

            // Spawn 3 additional arrows raining down from above
            for (int i = 0; i < 3; i++) {
                Location spawnLoc = hitLoc.clone().add(
                    (Math.random() - 0.5) * 4,  // Random X offset ±2 blocks
                    8,                           // 8 blocks above
                    (Math.random() - 0.5) * 4   // Random Z offset ±2 blocks
                );

                Arrow starArrow = hitLoc.getWorld().spawnArrow(
                    spawnLoc,
                    new Vector(0, -1, 0),  // Straight down
                    1.5f,                   // Velocity
                    0                       // Spread
                );
                starArrow.setShooter(player);
                starArrow.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY);
                starArrow.setDamage(arrow.getDamage() * 0.5); // 50% of original arrow damage
            }

            player.sendMessage("§e⭐ Starfall!");
        }
    }

    // ========== ANCIENT_WARD ==========

    /**
     * ANCIENT_WARD: Reduces incoming damage by 2 hearts (flat)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onAncientWard(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack chestplate = player.getInventory().getChestplate();

        if (chestplate != null && hasHolyEnchant(chestplate, HolyEnchant.ANCIENT_WARD)) {
            double reduction = 4.0; // 2 hearts
            event.setDamage(Math.max(0, event.getDamage() - reduction));
        }
    }

    // ========== PHOENIX_FLAME ==========

    /**
     * PHOENIX_FLAME: Revive once per day with fire immunity
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhoenixFlameDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player has PHOENIX_FLAME enchant on any armor piece
        boolean hasPhoenixFlame = false;
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack piece : armor) {
            if (hasHolyEnchant(piece, HolyEnchant.PHOENIX_FLAME)) {
                hasPhoenixFlame = true;
                break;
            }
        }

        if (!hasPhoenixFlame) return;

        // Check daily cooldown
        LocalDate today = LocalDate.now();
        if (phoenixFlameLastUse.containsKey(uuid)) {
            LocalDate lastUse = phoenixFlameLastUse.get(uuid);
            long daysSince = ChronoUnit.DAYS.between(lastUse, today);
            if (daysSince < 1) {
                player.sendMessage("§cPhoenix Flame is on cooldown! (Resets tomorrow)");
                return;
            }
        }

        // Cancel death and revive
        event.setCancelled(true);
        event.getDrops().clear();
        event.setKeepInventory(true);
        event.setKeepLevel(true);

        // Set cooldown
        phoenixFlameLastUse.put(uuid, today);

        // Schedule respawn effects
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> {
                player.setHealth(6.0); // 3 hearts
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0, false, true, true)); // 30s fire immunity
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true, true)); // 5s Regen II
                player.sendMessage("§6§l⚡ PHOENIX FLAME: §eYou have been revived from death!");
                player.getWorld().strikeLightningEffect(player.getLocation());

                // Flame particles
                player.getWorld().spawnParticle(
                    org.bukkit.Particle.FLAME,
                    player.getLocation().add(0, 1, 0),
                    100,
                    0.5, 1.0, 0.5,
                    0.1
                );
            },
            1L
        );
    }

    // ========== TITANIC_FORCE ==========

    /**
     * TITANIC_FORCE: Bonus damage based on target's max HP (10%)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onTitanicForce(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(weapon, HolyEnchant.TITANIC_FORCE)) {
            LivingEntity target = (LivingEntity) event.getEntity();
            double bonusDamage = target.getMaxHealth() * 0.10; // 10% of max HP
            event.setDamage(event.getDamage() + bonusDamage);
        }
    }

    // ========== ECHO_STEP ==========

    /**
     * ECHO_STEP: Leaves afterimages that confuse enemies
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEchoStepMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player has ECHO_STEP on boots
        ItemStack boots = player.getInventory().getBoots();
        if (!hasHolyEnchant(boots, HolyEnchant.ECHO_STEP)) return;

        // Check cooldown (don't spam armor stands)
        long now = System.currentTimeMillis();
        if (echoStepLastSpawn.containsKey(uuid)) {
            long lastSpawn = echoStepLastSpawn.get(uuid);
            if (now - lastSpawn < ECHO_STEP_COOLDOWN_MS) {
                return;
            }
        }

        // Only spawn when sprinting
        if (!player.isSprinting()) return;

        echoStepLastSpawn.put(uuid, now);

        // Spawn armor stand "echo" at player's old location
        Location echoLoc = event.getFrom().clone();
        ArmorStand echo = (ArmorStand) echoLoc.getWorld().spawnEntity(echoLoc, org.bukkit.entity.EntityType.ARMOR_STAND);

        // Copy player's appearance
        echo.setHelmet(player.getInventory().getHelmet());
        echo.setChestplate(player.getInventory().getChestplate());
        echo.setLeggings(player.getInventory().getLeggings());
        echo.setBoots(player.getInventory().getBoots());
        echo.setItemInHand(player.getInventory().getItemInMainHand());

        // Set properties
        echo.setGravity(false);
        echo.setVisible(true);
        echo.setBasePlate(false);
        echo.setArms(true);
        echo.setMarker(false); // Can be targeted by mobs

        // Make it slightly transparent
        echo.setCustomNameVisible(false);
        echo.setInvulnerable(false); // Mobs can hit it

        // Spawn particles
        echoLoc.getWorld().spawnParticle(
            org.bukkit.Particle.PORTAL,
            echoLoc.add(0, 1, 0),
            20,
            0.3, 0.5, 0.3,
            0.05
        );

        // Remove after 5 seconds
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            echo::remove,
            100L // 5 seconds
        );
    }

    // ========== UTILITY ==========

    /**
     * Check if item has a specific Holy Enchant.
     */
    private boolean hasHolyEnchant(ItemStack item, HolyEnchant enchant) {
        if (item == null) return false;
        return holyEnchantService.hasHolyEnchant(item, enchant);
    }
}
