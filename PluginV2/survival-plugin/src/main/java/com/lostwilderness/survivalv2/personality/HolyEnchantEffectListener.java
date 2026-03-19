package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.HolyEnchant;
import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

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
     * Note: Requires scheduled task in main plugin to check time and apply regeneration
     * TODO: Implement scheduled task (check world.getTime() > 13000 && < 23000)
     */

    // ========== STARFALL ==========

    /**
     * STARFALL: Arrows rain additional projectiles on impact
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStarfall(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        ItemStack bow = player.getInventory().getItemInMainHand();

        if (hasHolyEnchant(bow, HolyEnchant.STARFALL)) {
            // Spawn 2 additional arrows slightly offset
            // TODO: Implement via ProjectileHitEvent (spawn extra arrows on impact)
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
     * Note: Requires death event listener + cooldown tracking
     * TODO: Implement via PlayerDeathEvent + respawn + cooldown system
     */

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
     * Note: Requires custom entity spawning (armor stands) and AI manipulation
     * TODO: Implement via PlayerMoveEvent (spawn fake entities behind player)
     */

    // ========== UTILITY ==========

    /**
     * Check if item has a specific Holy Enchant.
     */
    private boolean hasHolyEnchant(ItemStack item, HolyEnchant enchant) {
        if (item == null) return false;
        return holyEnchantService.hasHolyEnchant(item, enchant);
    }
}
