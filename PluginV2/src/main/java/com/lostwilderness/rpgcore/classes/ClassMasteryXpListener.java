package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.skills.SkillXpService;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Grants class mastery XP based on player actions.
 * Each of the 5 PlayerClasses gains XP from class-specific sources.
 *
 * Phase 3 of Class Skill Tree System.
 */
public final class ClassMasteryXpListener implements Listener {

    private final ClassService classService;
    private final ClassSkillTreeService skillTreeService;
    private final SkillXpService skillXpService;
    private final Plugin plugin;

    // Undead mob types for Templar XP
    private static final EnumSet<EntityType> UNDEAD_MOBS = EnumSet.of(
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.ZOMBIE_VILLAGER,
        EntityType.HUSK, EntityType.STRAY, EntityType.DROWNED, EntityType.PHANTOM,
        EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN
    );

    // Axe types for Berserker XP
    private static final EnumSet<Material> AXES = EnumSet.of(
        Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
        Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );

    public ClassMasteryXpListener(ClassService classService, ClassSkillTreeService skillTreeService,
                                   SkillXpService skillXpService, Plugin plugin) {
        this.classService = classService;
        this.skillTreeService = skillTreeService;
        this.skillXpService = skillXpService;
        this.plugin = plugin;
    }

    /**
     * Helper method to grant XP only if player has the correct class.
     * Performs async class check to avoid main thread blocking.
     */
    private void grantXpIfClass(UUID playerUuid, PlayerClass expectedClass, double amount) {
        classService.getClass(playerUuid).thenAccept(classOpt -> {
            if (classOpt.isPresent() && classOpt.get() == expectedClass) {
                String skillKey = skillTreeService.getMasterySkillKey(expectedClass);
                boolean success = skillXpService.grantXp(playerUuid, skillKey, amount);
                if (!success) {
                    plugin.getLogger().warning("[ClassMasteryXp] Failed to grant " + amount
                        + " XP to " + playerUuid + " for " + expectedClass + " (skill: " + skillKey + ")");
                }
            }
        });
    }

    /**
     * TEMPLAR: Killing undead mobs (20 XP)
     * RANGER: Killing with bow/crossbow (25 XP)
     * BERSERKER: Axe kills (20 XP)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        UUID uuid = killer.getUniqueId();
        EntityType victimType = event.getEntityType();

        // TEMPLAR: Killing undead mobs
        if (UNDEAD_MOBS.contains(victimType)) {
            grantXpIfClass(uuid, PlayerClass.CELESTIAL_TEMPLAR, 20.0);
        }

        // RANGER: Killing with bow/crossbow (check if killed by projectile)
        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent dmg) {
            Entity damager = dmg.getDamager();
            if (damager instanceof Arrow || damager instanceof SpectralArrow) {
                // Verify arrow was shot by this player
                if (damager instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
                    if (shooter.getUniqueId().equals(uuid)) {
                        grantXpIfClass(uuid, PlayerClass.WILDLAND_RANGER, 25.0);
                    }
                }
            }
        }

        // BERSERKER: Axe kills
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon != null && AXES.contains(weapon.getType())) {
            grantXpIfClass(uuid, PlayerClass.DESTROYER_BERSERKER, 20.0);
        }
    }

    /**
     * CULTIST: PvP kills (40 XP)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (killer.getUniqueId().equals(event.getEntity().getUniqueId())) return; // No suicide XP

        UUID uuid = killer.getUniqueId();
        grantXpIfClass(uuid, PlayerClass.CORRUPTED_CULTIST, 40.0);
    }

    /**
     * RANGER: Taming animals (50 XP)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        grantXpIfClass(uuid, PlayerClass.WILDLAND_RANGER, 50.0);
    }

    /**
     * ARTIFICER: Crafting items (5 XP per item crafted)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == Material.AIR) return;

        // Grant XP based on crafted amount (normal craft = 1, shift-click = stack)
        int amount = result.getAmount();
        double xp = 5.0 * amount;

        // Cap at 50 XP per craft event to prevent abuse
        xp = Math.min(xp, 50.0);

        grantXpIfClass(uuid, PlayerClass.REDEEMED_ARTIFICER, xp);
    }

    /**
     * ARTIFICER: Anvil repairs (15 XP)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getViewers().isEmpty()) return;
        if (!(event.getViewers().get(0) instanceof Player player)) return;

        // Only grant XP if this is a repair (has a repair cost and result item)
        ItemStack result = event.getResult();
        if (result == null || result.getType() == Material.AIR) return;

        // Check if this is actually a repair by seeing if first slot was damaged
        ItemStack firstItem = event.getInventory().getItem(0);
        if (firstItem == null || !firstItem.getType().equals(result.getType())) return;

        // If first item has durability and result has higher durability, it's a repair
        if (firstItem.getType().getMaxDurability() > 0) {
            UUID uuid = player.getUniqueId();
            grantXpIfClass(uuid, PlayerClass.REDEEMED_ARTIFICER, 15.0);
        }
    }

    /**
     * BERSERKER: Taking damage at low HP (5 XP when below 30% HP)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Only grant XP if player is below 30% HP when damage is taken
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();

        if (currentHealth / maxHealth <= 0.30) {
            UUID uuid = player.getUniqueId();
            grantXpIfClass(uuid, PlayerClass.DESTROYER_BERSERKER, 5.0);
        }
    }

    /**
     * Public method for ability usage XP grants.
     * Called by ClassAbilityListener when abilities fire successfully.
     *
     * XP amounts per ability:
     * - TEMPLAR Smite: 10 XP
     * - ARTIFICER Slam: 10 XP
     * - CULTIST Dash: 10 XP
     * - BERSERKER War Cry: 10 XP
     */
    public void grantAbilityXp(UUID playerUuid, PlayerClass playerClass) {
        grantXpIfClass(playerUuid, playerClass, 10.0);
    }

    /**
     * Public method for lifesteal proc XP grants.
     * Called by ClassPassiveListener when lifesteal heals the player.
     *
     * CULTIST Lifesteal: 8 XP per proc
     */
    public void grantLifestealXp(UUID playerUuid) {
        grantXpIfClass(playerUuid, PlayerClass.CORRUPTED_CULTIST, 8.0);
    }
}
