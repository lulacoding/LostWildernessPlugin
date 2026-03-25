package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.pets.GoldenTamingItems;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Handles Golden Taming Items - Phase 6.
 *
 * Golden Bone  → instant-tames wolves; can retame wolves owned by others
 * Golden Seeds → instant-tames parrots
 * Golden Fish  → instant-tames cats
 */
public final class GoldenTamingListener implements Listener {

    private final PetService petService;
    private final Plugin plugin;
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;

    public GoldenTamingListener(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.plugin = plugin;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Tameable animal)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!GoldenTamingItems.isGoldenTamingItem(item)) return;

        String itemType = GoldenTamingItems.getItemType(item);
        if (!GoldenTamingItems.canTameEntityType(itemType, animal.getType())) return;

        // Prevent vanilla taming logic from firing
        event.setCancelled(true);

        org.bukkit.inventory.EquipmentSlot hand = event.getHand();

        // Already owned by this player - no-op
        AnimalTamer currentOwner = animal.getOwner();
        if (animal.isTamed() && player.equals(currentOwner)) {
            player.sendMessage(ChatColor.YELLOW + "This is already your pet!");
            return;
        }

        // Already owned by someone else
        if (animal.isTamed() && currentOwner != null) {
            // Only Golden Bone can retame
            if (!GoldenTamingItems.GOLDEN_BONE.equals(itemType)) {
                player.sendMessage(ChatColor.RED + "This animal already belongs to someone else!");
                return;
            }

            // Check pet limit before retaming
            petService.canTameMore(player.getUniqueId()).thenAccept(canTame -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!canTame) {
                        player.sendMessage(ChatColor.RED + "You have reached your maximum pet limit!");
                        return;
                    }
                    executeRetame(player, animal, item, hand, currentOwner);
                });
            }).exceptionally(ex -> {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Error checking pet limit. Try again."));
                return null;
            });
            return;
        }

        // Untamed animal - instant tame
        petService.canTameMore(player.getUniqueId()).thenAccept(canTame -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!canTame) {
                    player.sendMessage(ChatColor.RED + "You have reached your maximum pet limit!");
                    return;
                }
                executeInstantTame(player, animal, item, hand);
            });
        }).exceptionally(ex -> {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                player.sendMessage(ChatColor.RED + "Error checking pet limit. Try again."));
            return null;
        });
    }

    /**
     * Force-tames an untamed animal instantly and registers it as a pet.
     */
    private void executeInstantTame(Player player, Tameable animal, ItemStack item,
                                     org.bukkit.inventory.EquipmentSlot hand) {
        animal.setOwner(player);
        animal.setTamed(true);

        consumeOne(player, item, hand);

        // Register as pet (reuse PetService - same logic as EntityTameEvent)
        petService.registerPet(player.getUniqueId(), animal).thenAccept(profile -> {
            // Set PDC on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                PersistentDataContainer pdc = animal.getPersistentDataContainer();
                pdc.set(petUuidKey, PersistentDataType.STRING, profile.petUuid().toString());
                pdc.set(petRegisteredKey, PersistentDataType.BOOLEAN, true);
            });

            player.sendMessage(ChatColor.GREEN + "Pet tamed! " + ChatColor.GRAY +
                "(" + profile.zodiacSign().displayName() + " sign)");
            player.sendMessage(ChatColor.YELLOW + "Personality: " + ChatColor.GRAY + "???");
            player.sendMessage(ChatColor.GRAY + "Visit the Lord to reveal personality.");
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Failed to register pet: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Transfers ownership of an already-tamed wolf to the player.
     * Updates the DB record if the wolf was registered.
     */
    private void executeRetame(Player player, Tameable animal, ItemStack item,
                                org.bukkit.inventory.EquipmentSlot hand, AnimalTamer previousOwner) {
        // Check if this wolf has a pet registration
        String storedUuid = animal.getPersistentDataContainer().get(petUuidKey, PersistentDataType.STRING);

        consumeOne(player, item, hand);

        // Transfer ownership physically
        animal.setOwner(player);

        String prevName = previousOwner instanceof Player p ? p.getName() : "another player";

        if (storedUuid != null) {
            // Transfer DB record to new owner
            try {
                java.util.UUID petUuid = java.util.UUID.fromString(storedUuid);
                petService.retamePet(petUuid, player.getUniqueId()).thenRun(() -> {
                    player.sendMessage(ChatColor.GOLD + "You have claimed ownership of this wolf from " + prevName + "!");
                    player.sendMessage(ChatColor.GRAY + "Pet records transferred to your account.");
                }).exceptionally(ex -> {
                    player.sendMessage(ChatColor.YELLOW + "Ownership transferred, but DB update failed: " + ex.getMessage());
                    return null;
                });
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.GOLD + "You have claimed ownership of this wolf from " + prevName + "!");
            }
        } else {
            // Wolf wasn't registered - register fresh for new owner
            petService.registerPet(player.getUniqueId(), animal).thenAccept(profile -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    PersistentDataContainer pdc = animal.getPersistentDataContainer();
                    pdc.set(petUuidKey, PersistentDataType.STRING, profile.petUuid().toString());
                    pdc.set(petRegisteredKey, PersistentDataType.BOOLEAN, true);
                });
                player.sendMessage(ChatColor.GOLD + "You have claimed this wolf from " + prevName + "!");
                player.sendMessage(ChatColor.GREEN + "Registered as your pet (" + profile.zodiacSign().displayName() + " sign).");
            }).exceptionally(ex -> {
                player.sendMessage(ChatColor.GOLD + "Wolf claimed from " + prevName + "! (Registration failed: " + ex.getMessage() + ")");
                return null;
            });
        }
    }

    /**
     * Remove one item from the correct hand slot.
     */
    private void consumeOne(Player player, ItemStack item, org.bukkit.inventory.EquipmentSlot hand) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            if (hand == org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }
}
