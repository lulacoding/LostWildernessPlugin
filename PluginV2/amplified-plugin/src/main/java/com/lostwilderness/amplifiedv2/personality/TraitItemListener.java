package com.lostwilderness.amplifiedv2.personality;

import com.lostwilderness.rpgcore.personality.HolyEnchantService;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final ReputationService reputationService;

    // Cooldown tracking
    private final Map<UUID, Long> blinkCooldowns = new HashMap<>();
    private final Map<UUID, Long> lastSneakTime = new HashMap<>();
    private final Map<UUID, Long> tomeCooldowns = new HashMap<>();
    private final Map<UUID, Long> flaskCooldowns = new HashMap<>();
    private final Map<UUID, Long> whistleCooldowns = new HashMap<>();
    private final Map<UUID, Long> staffCooldowns = new HashMap<>();
    private final Map<UUID, Long> philosopherStoneCooldowns = new HashMap<>();

    private static final long BLINK_COOLDOWN_MS = 5000; // 5 seconds
    private static final long DOUBLE_SNEAK_WINDOW_MS = 500; // 0.5 seconds
    private static final long TOME_COOLDOWN_MS = 3600000; // 1 hour
    private static final long FLASK_COOLDOWN_MS = 30000; // 30 seconds
    private static final long WHISTLE_COOLDOWN_MS = 300000; // 5 minutes
    private static final long STAFF_COOLDOWN_MS = 60000; // 1 minute
    private static final long PHILOSOPHER_STONE_COOLDOWN_MS = 120000; // 2 minutes

    public TraitItemListener(Plugin plugin, HolyEnchantService holyEnchantService, ReputationService reputationService) {
        this.plugin = plugin;
        this.holyEnchantService = holyEnchantService;
        this.reputationService = reputationService;
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

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (tomeCooldowns.containsKey(uuid)) {
            long lastUse = tomeCooldowns.get(uuid);
            if (now - lastUse < TOME_COOLDOWN_MS) {
                long remaining = (TOME_COOLDOWN_MS - (now - lastUse)) / 60000;
                player.sendMessage("§cTome on cooldown! " + remaining + " minutes remaining.");
                return;
            }
        }

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

        tomeCooldowns.put(uuid, now);
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

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (flaskCooldowns.containsKey(uuid)) {
            long lastUse = flaskCooldowns.get(uuid);
            if (now - lastUse < FLASK_COOLDOWN_MS) {
                long remaining = (FLASK_COOLDOWN_MS - (now - lastUse)) / 1000;
                player.sendMessage("§cFlask on cooldown! " + remaining + " seconds remaining.");
                return;
            }
        }

        // Random positive effect
        PotionEffectType[] positiveEffects = {
            PotionEffectType.SPEED,
            PotionEffectType.STRENGTH,
            PotionEffectType.REGENERATION,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.ABSORPTION
        };

        PotionEffectType randomEffect = positiveEffects[(int) (Math.random() * positiveEffects.length)];

        player.addPotionEffect(new PotionEffect(randomEffect, 600, 1, false, true, true));
        String effectName = randomEffect.getKey().getKey().replace("_", " ");
        effectName = effectName.substring(0, 1).toUpperCase() + effectName.substring(1);
        player.sendMessage("§a§lFlask of Eternity: §7" + effectName + " II!");

        flaskCooldowns.put(uuid, now);
    }

    // ========== WARLORD'S BLADE (Damage scales with missing health) ==========

    /**
     * Warlord's Blade: Bonus damage based on attacker's missing health
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onWarlordsBladeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if weapon has BERSERKER trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
        if (traitEnchant == null || !traitEnchant.equals("BERSERKER")) return;

        // Calculate bonus damage based on missing health
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double missingHealthPercent = (maxHealth - currentHealth) / maxHealth;

        // Up to +100% damage at 0 HP
        double damageMultiplier = 1.0 + missingHealthPercent;
        event.setDamage(event.getDamage() * damageMultiplier);
    }

    // ========== RANGER'S QUIVER (Infinity to held bows) ==========

    /**
     * Ranger's Quiver: Dynamically apply/remove Infinity when holding bows
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRangersQuiverHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack quiver = player.getInventory().getChestplate();

        // Check if wearing Ranger's Quiver
        if (quiver == null) return;

        String traitEnchant = holyEnchantService.getTraitEnchant(quiver);
        if (traitEnchant == null || !traitEnchant.equals("INFINITY_LINK")) return;

        // Schedule check for next tick (after item switch completes)
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
                if (newItem != null && (newItem.getType() == Material.BOW || newItem.getType() == Material.CROSSBOW)) {
                    ItemMeta meta = newItem.getItemMeta();
                    if (meta != null && !meta.hasEnchant(Enchantment.INFINITY)) {
                        meta.addEnchant(Enchantment.INFINITY, 1, true);
                        newItem.setItemMeta(meta);
                        player.sendMessage("§a✓ Ranger's Quiver: Infinity applied!");
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    // ========== RAGNAROK AXE (Kills refresh Speed burst) ==========

    /**
     * Ragnarok Axe: Kills grant Speed II for 3 seconds
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRagnarokKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();

        // Check if weapon has BLOODLUST trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
        if (traitEnchant == null || !traitEnchant.equals("BLOODLUST")) return;

        // Grant Speed II for 3 seconds
        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, true, true));
        killer.sendMessage("§c§lBLOODLUST!");
    }

    // ========== ANCIENT WHISTLE (Summon wolf companion) ==========

    /**
     * Ancient Whistle: Summon a persistent wolf companion
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onAncientWhistleUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack bone = event.getItem();

        if (bone == null || bone.getType() != Material.BONE) return;

        // Check if bone has BEAST_MASTER trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(bone);
        if (traitEnchant == null || !traitEnchant.equals("BEAST_MASTER")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (whistleCooldowns.containsKey(uuid)) {
            long lastUse = whistleCooldowns.get(uuid);
            if (now - lastUse < WHISTLE_COOLDOWN_MS) {
                long remaining = (WHISTLE_COOLDOWN_MS - (now - lastUse)) / 1000;
                player.sendMessage("§cWhistle on cooldown! " + remaining + " seconds remaining.");
                return;
            }
        }

        // Summon wolf
        Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
        wolf.setOwner(player);
        wolf.setAdult();
        wolf.customName(Component.text(player.getName() + "'s Companion", NamedTextColor.GOLD));
        wolf.setCustomNameVisible(true);
        wolf.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
        wolf.setHealth(40.0);

        player.sendMessage("§a§lAncient Whistle: §7Companion summoned!");
        whistleCooldowns.put(uuid, now);
    }

    // ========== MIRROR SHARD (Swap positions) ==========

    /**
     * Mirror Shard: Right-click a player to swap positions
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onMirrorShardUse(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;

        Player user = event.getPlayer();
        Player target = (Player) event.getRightClicked();

        ItemStack shard = user.getInventory().getItemInMainHand();

        // Check if holding Mirror Shard with ILLUSION trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(shard);
        if (traitEnchant == null || !traitEnchant.equals("ILLUSION")) return;

        // Swap positions
        Location userLoc = user.getLocation().clone();
        Location targetLoc = target.getLocation().clone();

        user.teleport(targetLoc);
        target.teleport(userLoc);

        user.sendMessage("§d§lMirror Shard: §7Swapped with " + target.getName() + "!");
        target.sendMessage("§d§lMirror Shard: §7" + user.getName() + " swapped with you!");

        // Particles
        user.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, targetLoc, 50, 0.5, 1.0, 0.5, 0.1);
        user.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, userLoc, 50, 0.5, 1.0, 0.5, 0.1);
    }

    // ========== STAFF OF THE COVENANT (Alignment-based effects) ==========

    /**
     * Staff of the Covenant: Cleanse (Good) or Curse (Evil) nearby players
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStaffOfCovenantUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack staff = event.getItem();

        if (staff == null) return;

        // Check if staff has SANCTIFY trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(staff);
        if (traitEnchant == null || !traitEnchant.equals("SANCTIFY")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (staffCooldowns.containsKey(uuid)) {
            long lastUse = staffCooldowns.get(uuid);
            if (now - lastUse < STAFF_COOLDOWN_MS) {
                long remaining = (STAFF_COOLDOWN_MS - (now - lastUse)) / 1000;
                player.sendMessage("§cStaff on cooldown! " + remaining + " seconds remaining.");
                return;
            }
        }

        // Get alignment (Good = positive honor, Evil = negative honor)
        int honorScore;
        try {
            honorScore = reputationService.getHonorScore(uuid).get(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            honorScore = 0; // Default to neutral
        }
        boolean isGood = honorScore >= 0;

        // Apply effects to nearby players (10 block radius)
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= 10) {
                if (isGood) {
                    // Good alignment: Cleanse and heal
                    nearby.getActivePotionEffects().forEach(effect -> {
                        if (effect.getType().equals(PotionEffectType.POISON) ||
                            effect.getType().equals(PotionEffectType.WITHER) ||
                            effect.getType().equals(PotionEffectType.WEAKNESS)) {
                            nearby.removePotionEffect(effect.getType());
                        }
                    });
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, true, true));
                    nearby.sendMessage("§a✓ Cleansed by the Covenant!");
                } else {
                    // Evil alignment: Curse
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0, false, true, true));
                    nearby.sendMessage("§4✗ Cursed by the Covenant!");
                }
            }
        }

        staffCooldowns.put(uuid, now);
        player.sendMessage(isGood ? "§a§lCovenant: §7Cleansed nearby allies!" : "§4§lCovenant: §7Cursed nearby enemies!");
    }

    // ========== RUNEBLADE (Critical hits trigger random effects) ==========

    /**
     * Runeblade: Critical hits apply random enchant-like effects
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onRunebladeCrit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if weapon has RUNIC_OVERLOAD trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
        if (traitEnchant == null || !traitEnchant.equals("RUNIC_OVERLOAD")) return;

        // Detect critical hit (player falling + melee attack)
        if (player.getFallDistance() > 0 && player.getVelocity().getY() < 0) {
            LivingEntity target = (LivingEntity) event.getEntity();

            // Random enchant effect
            int random = (int) (Math.random() * 5);
            switch (random) {
                case 0: // Fire Aspect
                    target.setFireTicks(100);
                    player.sendMessage("§c⚡ Runeblade: Fire!");
                    break;
                case 1: // Knockback
                    Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2);
                    target.setVelocity(knockback.setY(0.5));
                    player.sendMessage("§b⚡ Runeblade: Knockback!");
                    break;
                case 2: // Slowness
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                    player.sendMessage("§7⚡ Runeblade: Slow!");
                    break;
                case 3: // Weakness
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                    player.sendMessage("§8⚡ Runeblade: Weakness!");
                    break;
                case 4: // Lightning
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    event.setDamage(event.getDamage() * 1.5);
                    player.sendMessage("§e⚡ Runeblade: Lightning!");
                    break;
            }
        }
    }

    // ========== PHILOSOPHER'S STONE (Transmute materials + instant brewing) ==========

    /**
     * Philosopher's Stone: Right-click to transmute base materials into valuable ones
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhilosopherStoneUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack stone = event.getItem();

        if (stone == null) return;

        // Check if item has PHILOSOPHER trait enchant
        String traitEnchant = holyEnchantService.getTraitEnchant(stone);
        if (traitEnchant == null || !traitEnchant.equals("PHILOSOPHER")) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (philosopherStoneCooldowns.containsKey(uuid)) {
            long lastUse = philosopherStoneCooldowns.get(uuid);
            if (now - lastUse < PHILOSOPHER_STONE_COOLDOWN_MS) {
                long remaining = (PHILOSOPHER_STONE_COOLDOWN_MS - (now - lastUse)) / 1000;
                player.sendMessage("§cPhilosopher's Stone on cooldown! " + remaining + " seconds remaining.");
                return;
            }
        }

        // Get item in off-hand to transmute
        ItemStack toTransmute = player.getInventory().getItemInOffHand();
        if (toTransmute == null || toTransmute.getType() == Material.AIR) {
            player.sendMessage("§cHold material in off-hand to transmute!");
            return;
        }

        // Transmutation map: base material -> valuable material
        Material transmuted = null;
        int amount = Math.min(toTransmute.getAmount(), 64);

        switch (toTransmute.getType()) {
            case IRON_INGOT:
                transmuted = Material.GOLD_INGOT;
                break;
            case GOLD_INGOT:
                transmuted = Material.DIAMOND;
                amount = Math.max(1, amount / 4); // 4 gold = 1 diamond
                break;
            case COAL:
                transmuted = Material.IRON_INGOT;
                amount = Math.max(1, amount / 2); // 2 coal = 1 iron
                break;
            case COPPER_INGOT:
                transmuted = Material.IRON_INGOT;
                amount = Math.max(1, amount / 3); // 3 copper = 1 iron
                break;
            case REDSTONE:
                transmuted = Material.LAPIS_LAZULI;
                break;
            case LAPIS_LAZULI:
                transmuted = Material.EMERALD;
                amount = Math.max(1, amount / 8); // 8 lapis = 1 emerald
                break;
            case NETHERRACK:
                transmuted = Material.GLOWSTONE_DUST;
                amount = Math.max(1, amount / 2); // 2 netherrack = 1 glowstone dust
                break;
            case COBBLESTONE:
                transmuted = Material.STONE;
                break;
            case STONE:
                transmuted = Material.SMOOTH_STONE;
                break;
            default:
                player.sendMessage("§cCannot transmute " + toTransmute.getType().name() + "!");
                return;
        }

        // Perform transmutation
        toTransmute.setAmount(0); // Remove original
        player.getInventory().addItem(new ItemStack(transmuted, amount));

        player.sendMessage("§6§lPhilosopher's Stone: §eTransmuted " + toTransmute.getType().name() + " → " + transmuted.name() + " (x" + amount + ")!");
        player.getWorld().spawnParticle(
            org.bukkit.Particle.ENCHANT,
            player.getLocation().add(0, 1, 0),
            50,
            0.5, 1.0, 0.5,
            0.2
        );

        philosopherStoneCooldowns.put(uuid, now);
    }
}
