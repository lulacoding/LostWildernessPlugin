package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Active mana-based abilities, using AuraSkills mana.
 * Phase 4: Added ability upgrades and XP grants.
 */
public final class ClassAbilityListener implements Listener {

    private final ClassService classService;
    private final AuraSkillsBridge auraBridge;
    private final ClassSkillTreeService skillTreeService;
    private final ClassMasteryXpListener xpListener;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ClassAbilityListener(ClassService classService, AuraSkillsBridge auraBridge,
                                 ClassSkillTreeService skillTreeService, ClassMasteryXpListener xpListener) {
        this.classService = classService;
        this.auraBridge = auraBridge;
        this.skillTreeService = skillTreeService;
        this.xpListener = xpListener;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (!event.getAction().isRightClick())
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        classService.getClass(uuid).thenAccept(opt -> opt.ifPresent(playerClass -> {
            switch (playerClass) {
                case CELESTIAL_TEMPLAR -> handleTemplarSmite(player);
                case REDEEMED_ARTIFICER -> handleArtificerSlam(player);
                case CORRUPTED_CULTIST -> handleCultistDash(player);
                case DESTROYER_BERSERKER -> handleBerserkerWarCry(player);
                default -> {
                }
            }
        }));
    }

    private boolean onCooldown(Player player, long cooldownMillis) {
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && now - last < cooldownMillis) {
            long remain = (cooldownMillis - (now - last)) / 1000;
            player.sendMessage(ChatColor.RED + "Ability is on cooldown (" + remain + "s).");
            return true;
        }
        cooldowns.put(player.getUniqueId(), now);
        return false;
    }

    private boolean tryConsumeMana(Player player, double amount) {
        if (auraBridge == null || !auraBridge.isAvailable()) {
            player.sendMessage(ChatColor.RED + "Mana system is unavailable.");
            return false;
        }
        if (!auraBridge.consumeMana(player.getUniqueId(), amount)) {
            player.sendMessage(ChatColor.RED + "Not enough mana.");
            return false;
        }
        return true;
    }

    private void handleTemplarSmite(Player player) {
        if (!player.isSneaking())
            return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == org.bukkit.Material.AIR || !hand.getType().toString().endsWith("_SWORD"))
            return;
        if (onCooldown(player, 8000))
            return;

        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.CELESTIAL_TEMPLAR);

        // Ascension (Lv50) uses 50 mana, base uses 30
        double manaCost = level >= 50 ? 50.0 : 30.0;
        if (!tryConsumeMana(player, manaCost))
            return;

        Location center = player.getLocation();
        center.getWorld().strikeLightningEffect(center);
        center.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.2f, 1.0f);
        center.getWorld().spawnParticle(Particle.END_ROD, center, 80, 2, 1, 2, 0.1);

        // Determine max targets based on level
        // Lv1-14: 1 target, Lv15-49: 2 targets (Divine Strike), Lv50+: 3 targets (Ascension)
        int maxTargets = level >= 50 ? 3 : (level >= 15 ? 2 : 1);

        double radius = 5.0;
        java.util.List<LivingEntity> targets = new java.util.ArrayList<>();
        for (var entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                targets.add(target);
            }
        }

        // Sort by distance and hit closest N targets
        targets.sort((a, b) -> Double.compare(
            a.getLocation().distanceSquared(center),
            b.getLocation().distanceSquared(center)
        ));

        int hitCount = 0;
        for (LivingEntity target : targets) {
            if (hitCount >= maxTargets) break;
            target.damage(8.0, player); // 4 hearts baseline
            hitCount++;
        }

        // Grant XP after successful ability use
        if (xpListener != null) {
            xpListener.grantAbilityXp(uuid, PlayerClass.CELESTIAL_TEMPLAR);
        }
    }

    private void handleArtificerSlam(Player player) {
        if (!player.isSneaking())
            return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        String typeName = hand.getType().toString();
        if (!(typeName.endsWith("_PICKAXE") || typeName.endsWith("_SHOVEL")))
            return;
        if (onCooldown(player, 10000))
            return;
        if (!tryConsumeMana(player, 40))
            return;

        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.REDEEMED_ARTIFICER);

        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.1f);
        center.getWorld().spawnParticle(Particle.BLOCK, center, 80, 1.5, 0.2, 1.5, 0.2,
                center.getBlock().getBlockData());

        // Power Slam (Lv15): radius 6.0, Slowness III
        double radius = level >= 15 ? 6.0 : 4.0;
        int slownessAmplifier = level >= 15 ? 2 : 1; // Slowness III = amplifier 2

        for (var entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                Vector knock = target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.6);
                knock.setY(0.7);
                target.setVelocity(knock);
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 20 * 4, slownessAmplifier, true, true, true));
            }
        }

        // Grant XP after successful ability use
        if (xpListener != null) {
            xpListener.grantAbilityXp(uuid, PlayerClass.REDEEMED_ARTIFICER);
        }
    }

    private void handleCultistDash(Player player) {
        if (!player.isSneaking())
            return;
        if (player.getInventory().getItemInMainHand().getType() != org.bukkit.Material.AIR)
            return;
        if (onCooldown(player, 6000))
            return;
        if (!tryConsumeMana(player, 25))
            return;

        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.CORRUPTED_CULTIST);

        Location start = player.getLocation();
        Vector direction = start.getDirection().normalize();

        // Shadow Step (Lv10): range increases to 12 blocks
        double range = level >= 10 ? 12.0 : 8.0;
        Location target = start.clone().add(direction.multiply(range));

        player.teleport(target);
        start.getWorld().playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.6f);
        start.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, start, 40, 1, 1, 1, 0.01);
        target.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, target, 40, 1, 1, 1, 0.01);

        // Shadow Step adds SMOKE_LARGE particle trail at Lv10+
        if (level >= 10) {
            target.getWorld().spawnParticle(Particle.SMOKE, target, 80, 1, 1, 1, 0.05);
        }

        // Grant XP after successful ability use
        if (xpListener != null) {
            xpListener.grantAbilityXp(uuid, PlayerClass.CORRUPTED_CULTIST);
        }
    }

    private void handleBerserkerWarCry(Player player) {
        if (!player.isSneaking())
            return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!hand.getType().toString().endsWith("_AXE"))
            return;
        if (onCooldown(player, 12000))
            return;
        if (!tryConsumeMana(player, 35))
            return;

        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.DESTROYER_BERSERKER);

        Location center = player.getLocation();
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.2f);
        center.getWorld().spawnParticle(Particle.CRIT, center, 60, 2, 1, 2, 0.1);

        double radius = 6.0;
        for (var entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player ally) {
                // Always grant Resistance II
                ally.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.RESISTANCE, 20 * 5, 1, true, true, true));

                // Brutal War Cry (Lv15): also grant Strength I
                if (level >= 15) {
                    ally.addPotionEffect(new org.bukkit.potion.PotionEffect(
                            org.bukkit.potion.PotionEffectType.STRENGTH, 20 * 5, 0, true, true, true));
                }
            } else if (entity instanceof LivingEntity target) {
                Vector away = target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(0.8);
                away.setY(0.4);
                target.setVelocity(away);
            }
        }

        // Grant XP after successful ability use
        if (xpListener != null) {
            xpListener.grantAbilityXp(uuid, PlayerClass.DESTROYER_BERSERKER);
        }
    }
}
