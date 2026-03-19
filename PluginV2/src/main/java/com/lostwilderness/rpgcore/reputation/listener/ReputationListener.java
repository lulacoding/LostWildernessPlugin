package com.lostwilderness.rpgcore.reputation.listener;

import com.lostwilderness.rpgcore.reputation.Faction;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import com.lostwilderness.rpgcore.core.ModuleContext;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ReputationListener implements Listener {

    private final ReputationService service;
    private final ModuleContext context;

    public ReputationListener(ReputationService service, ModuleContext context) {
        this.service = service;
        this.context = context;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        // Killing a player grants Destroyer reputation (Bad)
        service.addReputation(killer.getUniqueId(), Faction.DESTROYERS, 50);
        service.addReputation(killer.getUniqueId(), Faction.CELESTIAL, -50);
        
        killer.sendMessage(ChatColor.RED + "You lost honor for killing a player.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInnocentKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        if (victim instanceof Villager) {
            service.addReputation(killer.getUniqueId(), Faction.CELESTIAL, -30);
            service.addReputation(killer.getUniqueId(), Faction.CORRUPTED, 15);
            killer.sendMessage(ChatColor.RED + "The heavens frown upon those who harm the innocent.");
        } else if (victim instanceof Animals) {
            // Killing peaceful animals (pets or livestock)
            if (victim.customName() != null || victim instanceof Horse || victim instanceof Wolf || victim instanceof Cat) {
                service.addReputation(killer.getUniqueId(), Faction.CELESTIAL, -5);
                killer.sendMessage(ChatColor.GRAY + "Killing this creature has slightly lowered your honor.");
            }
        } else if (victim instanceof IronGolem && ((IronGolem) victim).isPlayerCreated()) {
            service.addReputation(killer.getUniqueId(), Faction.CELESTIAL, -10);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHostileKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        if (victim instanceof Monster) {
            // Small bonus for cleaning up the world
            service.addReputation(killer.getUniqueId(), Faction.WILDLANDS, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onZombieCure(EntityTransformEvent event) {
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
            if (event.getTransformedEntity() instanceof Villager) {
                // Find nearby player who might have cured it (not perfect but Bukkit doesn't track this easily in the event)
                for (Entity nearby : event.getEntity().getNearbyEntities(10, 10, 10)) {
                    if (nearby instanceof Player player) {
                        service.addReputation(player.getUniqueId(), Faction.CELESTIAL, 100);
                        player.sendMessage(ChatColor.GREEN + "You gained significant honor for curing a soul.");
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTrade(TradeSelectEvent event) {
        Player player = (Player) event.getWhoClicked();
        service.addReputation(player.getUniqueId(), Faction.CELESTIAL, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrief(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return; 

        Material type = event.getBlock().getType();
        // Placeholder for grief detection
    }

    /**
     * Siding with bosses.
     * If a player right clicks a Wither (Boss) with a WITHER_ROSE, they show "allegiance".
     */
    @EventHandler(ignoreCancelled = true)
    public void onBossInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Wither wither)) return;
        if (!wither.hasMetadata("devoider") && !wither.hasMetadata("diablo")) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.WITHER_ROSE) {
            service.addReputation(player.getUniqueId(), Faction.CORRUPTED, 100);
            service.addReputation(player.getUniqueId(), Faction.CELESTIAL, -150);
            
            player.sendMessage(ChatColor.DARK_PURPLE + "You have pledged your soul to the darkness...");
            item.setAmount(item.getAmount() - 1);
        }
    }
}
