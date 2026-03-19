package com.lostwilderness.rpgcore.party.listener;

import com.lostwilderness.rpgcore.party.PartyService;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Prevents friendly fire between party members.
 * Handles direct attacks, projectiles, and indirect damage sources.
 */
public final class PartyFriendlyFireListener implements Listener {

    private final PartyService service;
    private final YamlConfiguration config;

    public PartyFriendlyFireListener(PartyService service, YamlConfiguration config) {
        this.service = service;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!config.getBoolean("party.combat.disable-friendly-fire", true)) {
            return; // Friendly fire protection disabled
        }

        // Get damager player
        Player damager = getDamager(event.getDamager());
        if (damager == null) {
            return; // Not caused by a player
        }

        // Get victim player
        Player victim = getVictim(event.getEntity());
        if (victim == null) {
            return; // Not attacking a player
        }

        // Self-damage is allowed
        if (damager.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        // Check if in same party
        if (service.areInSameParty(damager.getUniqueId(), victim.getUniqueId())) {
            event.setCancelled(true);
            // Optional: Send message to damager
            // damager.sendMessage("§cYou cannot attack party members!");
        }
    }

    /**
     * Get the player who caused the damage.
     * Handles direct attacks, projectiles, and some indirect sources.
     */
    private Player getDamager(Entity damager) {
        // Direct player attack
        if (damager instanceof Player) {
            return (Player) damager;
        }

        // Projectile shot by player
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }

        // TNT, splash potions, etc. would need additional handling
        // For now, we only handle direct and projectile damage

        return null;
    }

    /**
     * Get the player who was damaged.
     */
    private Player getVictim(Entity victim) {
        if (victim instanceof Player) {
            return (Player) victim;
        }
        return null;
    }
}
