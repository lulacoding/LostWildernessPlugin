package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Ultimate item special mechanics:
 * - Mage Bow / Celestial Bow: Homing arrows
 * - Warlord's Blade: Damage scales with missing health
 * - Ranger's Quiver: Applies Infinity to held bows
 * - Eternal Hammer: Repairs held item by 50%
 * - Shadowstep Boots: Double-sneak to blink
 * - Ragnarok Axe: Kills refresh Speed I burst
 * - Tome of Wisdom: Grants 5 XP levels
 * - Ancient Whistle: Summons persistent wolf companion
 * - Mirror Shard: Swap position with target player
 * - Flask of Eternity: Random positive potion effect
 * - Staff of the Covenant: Cleanses/applies effects
 * - Runeblade: Critical hits trigger random enchant effect
 */
public class TraitItemListener implements Listener {

    private final Plugin plugin;
    private final HolyEnchantService holyEnchantService;

    // Cooldown tracking
    private final Map<UUID, Long> blinkCooldowns = new HashMap<>();
    private final Map<UUID, Long> lastSneakTime = new HashMap<>();

    private static final long BLINK_COOLDOWN_MS = 5000; // 5 seconds
    private static final long DOUBLE_SNEAK_WINDOW_MS = 500; // 0.5 seconds

    public TraitItemListener(Plugin plugin, HolyEnchantService holyEnchantService) {
        this.plugin = plugin;
        this.holyEnchantService = holyEnchantService;
    }

    // ========== HOMING ARROWS (Mage Bow, Celestial Bow) ==========

    /**
     * Homing arrows for Mage Bow and Celestial Bow
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onHomingArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();
        ItemStack bow = player.getInventory().getItemInMainHand();

        // Check if bow has HOMING trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(bow);
        if (traitEnchant == null || !traitEnchant.equals("HOMING")) return;

        // Start homing task
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround() || ticks++ > 100) {
                    cancel();
                    return;
                }

                // Find nearest living entity within 10 blocks
                LivingEntity target = arrow.getNearbyEntities(10, 10, 10).stream()
                    .filter(e -> e instanceof LivingEntity)
                    .filter(e -> e != player)
                    .filter(e -> !e.isDead())
                    .map(e -> (LivingEntity) e)
                    .min((a, b) -> Double.compare(
                        a.getLocation().distanceSquared(arrow.getLocation()),
                        b.getLocation().distanceSquared(arrow.getLocation())
                    ))
                    .orElse(null);

                if (target != null) {
                    // Adjust arrow velocity toward target
                    Vector direction = target.getEyeLocation().toVector()
                        .subtract(arrow.getLocation().toVector())
                        .normalize()
                        .multiply(arrow.getVelocity().length());

                    arrow.setVelocity(arrow.getVelocity().multiply(0.8).add(direction.multiply(0.2)));
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ========== SHADOWSTEP BOOTS (Double-sneak to blink) ==========

    /**
     * Double-sneak detection for Shadowstep Boots
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onShadowstepDoubleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();

        if (boots == null) return;

        // Check if boots have PHANTOM_STEP trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(boots);
        if (traitEnchant == null || !traitEnchant.equals("PHANTOM_STEP")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (blinkCooldowns.containsKey(uuid)) {
            long lastBlink = blinkCooldowns.get(uuid);
            if (now - lastBlink < BLINK_COOLDOWN_MS) {
                return; // On cooldown
            }
        }

        // Check for double-sneak
        if (lastSneakTime.containsKey(uuid)) {
            long lastSneak = lastSneakTime.get(uuid);
            if (now - lastSneak < DOUBLE_SNEAK_WINDOW_MS) {
                // Double-sneak detected!
                performBlink(player);
                blinkCooldowns.put(uuid, now);
                lastSneakTime.remove(uuid);
                return;
            }
        }

        lastSneakTime.put(uuid, now);
    }

    /**
     * Blink forward 10 blocks
     */
    private void performBlink(Player player) {
        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();
        Location target = start.clone().add(direction.multiply(10));

        // Find safe landing spot
        while (target.getBlock().getType().isSolid() && target.getY() < start.getWorld().getMaxHeight()) {
            target.add(0, 1, 0);
        }

        // Teleport
        player.teleport(target);
        player.sendMessage("§bShadowstep!");

        // Visual effect
        player.getWorld().spawnParticle(
            org.bukkit.Particle.PORTAL,
            start,
            50,
            0.5, 0.5, 0.5,
            0.1
        );
        player.getWorld().spawnParticle(
            org.bukkit.Particle.PORTAL,
            target,
            50,
            0.5, 0.5, 0.5,
            0.1
        );
    }

    // ========== ETERNAL HAMMER (Repair item 50%) ==========

    /**
     * Right-click with Eternal Hammer to repair held item
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEternalHammerUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack tool = event.getItem();

        if (tool == null) return;

        // Check if tool has MASTER_CRAFT trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(tool);
        if (traitEnchant == null || !traitEnchant.equals("MASTER_CRAFT")) return;

        // Get item in off-hand to repair
        ItemStack toRepair = player.getInventory().getItemInOffHand();
        if (toRepair == null || toRepair.getType() == Material.AIR) {
            player.sendMessage("§cHold an item in your off-hand to repair!");
            return;
        }

        // Check if item is damageable
        if (!(toRepair.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable)) {
            player.sendMessage("§cThat item cannot be repaired!");
            return;
        }

        org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) toRepair.getItemMeta();
        int currentDamage = meta.getDamage();

        if (currentDamage == 0) {
            player.sendMessage("§cThat item is already fully repaired!");
            return;
        }

        // Repair 50% of max durability
        int maxDurability = toRepair.getType().getMaxDurability();
        int repairAmount = maxDurability / 2;
        int newDamage = Math.max(0, currentDamage - repairAmount);

        meta.setDamage(newDamage);
        toRepair.setItemMeta(meta);

        player.sendMessage("§a§lEternal Hammer: §7Repaired " + repairAmount + " durability!");
        player.getWorld().spawnParticle(
            org.bukkit.Particle.ENCHANT,
            player.getLocation().add(0, 1, 0),
            30,
            0.5, 0.5, 0.5,
            0.1
        );
    }

    // ========== TOME OF WISDOM (Grant 5 XP levels) ==========

    /**
     * Right-click Tome of Wisdom to gain 5 XP levels (1 hour cooldown)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onTomeOfWisdomUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack book = event.getItem();

        if (book == null || book.getType() != Material.BOOK) return;

        // Check if book has KNOWLEDGE trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(book);
        if (traitEnchant == null || !traitEnchant.equals("KNOWLEDGE")) return;

        // Grant 5 levels
        player.giveExpLevels(5);
        player.sendMessage("§a§lTome of Infinite Wisdom: §7+5 Levels!");
        player.getWorld().spawnParticle(
            org.bukkit.Particle.ENCHANT,
            player.getLocation().add(0, 1, 0),
            50,
            0.5, 1.0, 0.5,
            0.2
        );

        // TODO: Add cooldown tracking (1 hour)
    }

    // ========== FLASK OF ETERNITY (Random positive potion) ==========

    /**
     * Right-click Flask of Eternity for random positive effect (30 second cooldown)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onFlaskOfEternityUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack potion = event.getItem();

        if (potion == null || potion.getType() != Material.POTION) return;

        // Check if potion has ETERNAL_BREW trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(potion);
        if (traitEnchant == null || !traitEnchant.equals("ETERNAL_BREW")) return;

        // Random positive effect
        org.bukkit.potion.PotionEffectType[] positiveEffects = {
            org.bukkit.potion.PotionEffectType.SPEED,
            org.bukkit.potion.PotionEffectType.STRENGTH,
            org.bukkit.potion.PotionEffectType.REGENERATION,
            org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE,
            org.bukkit.potion.PotionEffectType.WATER_BREATHING,
            org.bukkit.potion.PotionEffectType.NIGHT_VISION,
            org.bukkit.potion.PotionEffectType.ABSORPTION
        };

        org.bukkit.potion.PotionEffectType randomEffect =
            positiveEffects[(int) (Math.random() * positiveEffects.length)];

        player.addPotionEffect(new org.bukkit.potion.PotionEffect(randomEffect, 600, 1, false, true, true));
        player.sendMessage("§a§lFlask of Eternity: §7" + randomEffect.getName() + " II!");

        // TODO: Add cooldown tracking (30 seconds)
    }

    // ========== STUBS FOR OTHER ULTIMATE ITEMS ==========

    /**
     * TODO: Warlord's Blade - Damage scales with missing health
     * Implement: EntityDamageByEntityEvent, check weapon, calculate bonus based on (maxHP - currentHP)
     */

    /**
     * TODO: Ranger's Quiver - Applies Infinity to held bows
     * Implement: PlayerItemHeldEvent, add/remove Infinity enchant dynamically
     */

    /**
     * TODO: Ragnarok Axe - Kills refresh Speed I burst
     * Implement: EntityDeathEvent, check if killer has axe, apply Speed effect
     */

    /**
     * TODO: Ancient Whistle - Summons persistent wolf companion
     * Implement: PlayerInteractEvent, spawn wolf with custom AI, track owner
     */

    /**
     * TODO: Mirror Shard - Swap position with target player
     * Implement: PlayerInteractEntityEvent, swap locations with clicked player
     */

    /**
     * TODO: Staff of the Covenant - Cleanses/applies effects based on alignment
     * Implement: PlayerInteractEvent, check alignment, apply Regeneration (good) or Wither (evil) to nearby
     */

    /**
     * TODO: Runeblade - Critical hits trigger random enchant effect
     * Implement: EntityDamageByEntityEvent, check for crit (fall damage + attack), apply random effect
     */
}
