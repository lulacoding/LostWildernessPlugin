package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Listens for pet taming events.
 * Enforces 3 pet limit and registers pets in DB.
 */
public final class PetTameListener implements Listener {

    private final PetService petService;
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;

    public PetTameListener(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }

        Entity entity = event.getEntity();
        UUID playerUuid = player.getUniqueId();

        // Check if player can tame more pets (async check but block for event)
        boolean canTame;
        try {
            canTame = petService.canTameMore(playerUuid).join();
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error checking pet limit. Try again.");
            event.setCancelled(true);
            return;
        }

        if (!canTame) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You have reached the maximum of 3 pets!");
            player.sendMessage(ChatColor.YELLOW + "Use /pets graveyard to view past companions.");
            return;
        }

        // Register pet (async but don't block event - handle post-tame)
        petService.registerPet(playerUuid, entity).thenAccept(profile -> {
            // Store pet UUID in PDC for death tracking
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            pdc.set(petUuidKey, PersistentDataType.STRING, profile.petUuid().toString());
            pdc.set(petRegisteredKey, PersistentDataType.BOOLEAN, true);

            // Notify player
            player.sendMessage(ChatColor.GREEN + "Pet registered! " + ChatColor.GRAY +
                "(" + profile.zodiacSign().displayName() + " sign)");
            player.sendMessage(ChatColor.YELLOW + "Personality: " + ChatColor.GRAY + "???");
            player.sendMessage(ChatColor.GRAY + "Visit the Lord to reveal personality.");
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Failed to register pet: " + ex.getMessage());
            return null;
        });
    }
}
