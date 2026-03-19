package com.lostwilderness.rpgcore.party.listener;

import com.lostwilderness.rpgcore.party.Party;
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
 * Applies party buffs based on party size.
 * Damage boost scales with number of party members.
 */
public final class PartyBuffListener implements Listener {

    private final PartyService service;
    private final YamlConfiguration config;

    public PartyBuffListener(PartyService service, YamlConfiguration config) {
        this.service = service;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!config.getBoolean("party.combat.party-buff.enabled", true)) {
            return; // Party buffs disabled
        }

        // Get damager player
        Player damager = getDamager(event.getDamager());
        if (damager == null) {
            return; // Not caused by a player
        }

        // Check if damager is in a party
        Party party = service.getParty(damager.getUniqueId());
        if (party == null) {
            return; // Not in a party
        }

        // Calculate damage boost
        double boostPerMember = config.getDouble("party.combat.party-buff.damage-boost-per-member", 0.05);
        int memberCount = party.size();

        // Formula: multiplier = 1.0 + (0.05 * (memberCount - 1))
        // Example: 2 members = 1.05x, 3 members = 1.10x, 6 members = 1.25x
        double multiplier = 1.0 + (boostPerMember * (memberCount - 1));

        // Apply damage boost
        double originalDamage = event.getDamage();
        double boostedDamage = originalDamage * multiplier;
        event.setDamage(boostedDamage);

        // Optional: Debug logging
        // damager.sendMessage(String.format("§7Party buff: %.1fx damage (%.1f → %.1f)",
        //     multiplier, originalDamage, boostedDamage));
    }

    /**
     * Get the player who caused the damage.
     * Handles direct attacks and projectiles.
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

        return null;
    }
}
