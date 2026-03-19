package com.lostwilderness.rpgcore.classes;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Implements always-on passive effects for each class.
 * Phase 4: Added level gates for non-Lv1 passives and XP grants.
 */
public final class ClassPassiveListener implements Listener {

    private final ClassService classService;
    private final ClassSkillTreeService skillTreeService;
    private final ClassMasteryXpListener xpListener;

    private static final Set<EntityType> UNDEAD = EnumSet.of(
            EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER);

    public ClassPassiveListener(ClassService classService, ClassSkillTreeService skillTreeService,
                                 ClassMasteryXpListener xpListener) {
        this.classService = classService;
        this.skillTreeService = skillTreeService;
        this.xpListener = xpListener;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player))
            return;
        UUID uuid = player.getUniqueId();
        classService.getClass(uuid).thenAccept(opt -> opt.ifPresent(playerClass -> {
            switch (playerClass) {
                case CELESTIAL_TEMPLAR -> handleTemplarDamage(event, player);
                case CORRUPTED_CULTIST -> handleCultistLifesteal(event, player);
                default -> {
                }
            }
        }));
    }

    private void handleTemplarDamage(EntityDamageByEntityEvent event, Player player) {
        if (event.getEntity() instanceof LivingEntity target && UNDEAD.contains(target.getType())) {
            event.setDamage(event.getDamage() * 1.2); // +20% vs undead/bosses
        }
    }

    private void handleCultistLifesteal(EntityDamageByEntityEvent event, Player player) {
        if (!(event.getEntity() instanceof LivingEntity))
            return;

        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.CORRUPTED_CULTIST);

        // Lifesteal unlocks at Lv5
        if (level < 5) return;

        // Soul Drain (Lv30): 15% proc rate, 1.5 hearts heal
        // Base Lifesteal (Lv5): 5% proc rate, 1 heart heal
        double procChance = level >= 30 ? 0.15 : 0.05;
        double healAmount = level >= 30 ? 3.0 : 2.0; // 1.5 hearts = 3.0 HP, 1 heart = 2.0 HP

        if (Math.random() <= procChance) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHp = attr != null ? attr.getValue() : 20.0;
            double newHealth = Math.min(maxHp, player.getHealth() + healAmount);
            player.setHealth(newHealth);
            player.sendMessage(ChatColor.DARK_PURPLE + "You siphon life from your foe.");

            // Grant XP for lifesteal proc
            if (xpListener != null) {
                xpListener.grantLifestealXp(uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        classService.getClass(uuid).thenAccept(opt -> opt.ifPresent(playerClass -> {
            switch (playerClass) {
                case WILDLAND_RANGER -> handleRangerSpeed(player);
                case DESTROYER_BERSERKER -> handleBerserkerStrength(player);
                default -> {
                }
            }
        }));
    }

    private void handleRangerSpeed(Player player) {
        switch (player.getLocation().getBlock().getBiome()) {
            case FOREST, BIRCH_FOREST, DARK_FOREST, TAIGA, OLD_GROWTH_PINE_TAIGA,
                    JUNGLE, SPARSE_JUNGLE ->
                applyOrRefreshEffect(player, PotionEffectType.SPEED, 1);
            default -> {
                // Do not forcibly clear, just let it expire naturally
            }
        }
    }

    private void handleBerserkerStrength(Player player) {
        UUID uuid = player.getUniqueId();
        int level = skillTreeService.getMasteryLevel(uuid, PlayerClass.DESTROYER_BERSERKER);

        // Berserker's Rage unlocks at Lv5
        if (level < 5) return;

        double health = player.getHealth();
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = attr != null ? attr.getValue() : 20.0;
        boolean low = health <= maxHp * 0.3;
        if (low) {
            applyOrRefreshEffect(player, PotionEffectType.STRENGTH, 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTemplarDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.WITHER)
            return;
        UUID uuid = player.getUniqueId();
        classService.getClass(uuid).thenAccept(opt -> opt.ifPresent(playerClass -> {
            if (playerClass == PlayerClass.CELESTIAL_TEMPLAR) {
                event.setDamage(event.getDamage() * 0.5); // -50% wither damage
            }
        }));
    }

    private static void applyOrRefreshEffect(Player player, PotionEffectType type, int amplifier) {
        int durationTicks = 20 * 5; // 5s, refreshed on movement
        PotionEffect existing = player.getPotionEffect(type);
        if (existing == null || existing.getAmplifier() < amplifier || existing.getDuration() < durationTicks / 2) {
            player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, true, false, true));
        }
    }
}
