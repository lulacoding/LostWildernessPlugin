package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Listens for pet death events.
 * Records death date in DB and notifies owner.
 */
public final class PetDeathListener implements Listener {

    private final PetService petService;
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;

    public PetDeathListener(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Must be tameable
        if (!(entity instanceof Tameable tameable)) {
            return;
        }

        // Must be tamed
        if (!tameable.isTamed()) {
            return;
        }

        // Check if registered in our system
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(petRegisteredKey, PersistentDataType.BOOLEAN)) {
            return; // Not registered
        }

        // Get pet UUID from PDC
        String petUuidStr = pdc.get(petUuidKey, PersistentDataType.STRING);
        if (petUuidStr == null) {
            return; // No UUID stored
        }

        UUID petUuid;
        try {
            petUuid = UUID.fromString(petUuidStr);
        } catch (IllegalArgumentException e) {
            return; // Invalid UUID
        }

        // Get pet profile to find owner
        petService.getPetProfile(petUuid).thenAccept(opt -> {
            if (opt.isEmpty()) {
                return; // Pet not in DB
            }

            PetProfile profile = opt.get();

            // Record death
            petService.recordDeath(petUuid).thenRun(() -> {
                // Notify owner if online
                Player owner = Bukkit.getPlayer(profile.ownerUuid());
                if (owner != null && owner.isOnline()) {
                    String petName = profile.customName() != null ? profile.customName() : "Your pet";
                    owner.sendMessage(ChatColor.RED + "☠ " + petName + " has died.");
                    owner.sendMessage(ChatColor.GRAY + "Visit /pets graveyard to remember them.");
                }
            });
        });
    }
}
