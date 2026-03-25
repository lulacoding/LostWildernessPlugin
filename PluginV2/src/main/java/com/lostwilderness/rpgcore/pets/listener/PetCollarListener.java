package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.clans.model.Clan;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.pets.CollarPriority;
import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Manages pet collar colors based on clan membership and custom dye.
 *
 * Priority: Clan color > Custom dye > Default (red)
 *
 * Collar updates are triggered on:
 * - Player join
 * - Pet tame
 * - Player right-clicks wolf/cat with a dye item (custom dye)
 */
public final class PetCollarListener implements Listener {

    private final PetService petService;
    private final ClanService clanService;
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;
    private final NamespacedKey customCollarColorKey;
    private final Plugin plugin;

    public PetCollarListener(PetService petService, ClanService clanService, Plugin plugin) {
        this.petService = petService;
        this.clanService = clanService;
        this.plugin = plugin;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
        this.customCollarColorKey = new NamespacedKey(plugin, "pet_collar_color");
    }

    // ── On player join: refresh all pet collars ───────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay 2s to let reconciliation run first and entities load
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) updateCollarForAllPets(player);
        }, 40L);
    }

    // ── On tame: apply initial collar ────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;
        Entity entity = event.getEntity();

        // Slight delay to let PetTameListener register the pet first
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            DyeColor color = calculateCollarColor(player, entity);
            applyCollar(entity, color);
        }, 5L);
    }

    // ── Custom dye: right-click wolf or cat with a dye item ──────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();
        if (!(entity instanceof Wolf) && !(entity instanceof Cat)) return;

        // Must be registered in our system
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(petRegisteredKey, PersistentDataType.BOOLEAN)) return;

        // Must be tamed by this player
        String petUuidStr = pdc.get(petUuidKey, PersistentDataType.STRING);
        if (petUuidStr == null) return;

        PetProfile profile;
        try {
            profile = petService.getCachedProfile(UUID.fromString(petUuidStr)).orElse(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (profile == null || !profile.ownerUuid().equals(event.getPlayer().getUniqueId())) return;

        // Check if holding a dye item
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        DyeColor dyeColor = dyeColorFromMaterial(held.getType());
        if (dyeColor == null) return;

        // If player is in a clan, custom dye is ignored (clan color takes priority)
        UUID clanId = clanService.getClanOfPlayer(event.getPlayer().getUniqueId());
        if (clanId != null) {
            event.getPlayer().sendMessage(org.bukkit.ChatColor.YELLOW +
                "Your clan color overrides custom dye. Leave your clan to use a custom color.");
            return;
        }

        event.setCancelled(true); // Prevent other right-click interactions

        // Store custom color in PDC and apply
        pdc.set(customCollarColorKey, PersistentDataType.STRING, dyeColor.name());
        applyCollar(entity, dyeColor);

        // Remove one dye from stack
        if (held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
        } else {
            event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        event.getPlayer().sendMessage(org.bukkit.ChatColor.GREEN +
            "Set " + petName(profile) + "'s collar to " +
            dyeColor.name().toLowerCase().replace('_', ' ') + "!");
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    /**
     * Update collar colors for all active pets of a player.
     */
    public void updateCollarForAllPets(Player player) {
        petService.getActivePets(player.getUniqueId()).thenAccept(pets -> {
            // Must run on main thread (entity access)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (PetProfile pet : pets) {
                    Entity entity = Bukkit.getEntity(pet.petUuid());
                    if (entity == null) continue;
                    DyeColor color = calculateCollarColor(player, entity);
                    applyCollar(entity, color);
                }
            });
        });
    }

    /**
     * Calculate the appropriate collar color for a pet entity.
     * Priority: Clan > Custom > Default
     */
    private DyeColor calculateCollarColor(Player owner, Entity entity) {
        // 1. Clan color
        UUID clanId = clanService.getClanOfPlayer(owner.getUniqueId());
        if (clanId != null) {
            Clan clan = clanService.getClanById(clanId);
            if (clan != null && clan.getColorHex() != null) {
                return hexToDyeColor(clan.getColorHex());
            }
        }

        // 2. Custom dye (stored in PDC on entity)
        String customColor = entity.getPersistentDataContainer()
            .get(customCollarColorKey, PersistentDataType.STRING);
        if (customColor != null) {
            try {
                return DyeColor.valueOf(customColor);
            } catch (IllegalArgumentException ignored) {}
        }

        // 3. Default
        return DyeColor.RED;
    }

    /**
     * Apply collar color to a Wolf or Cat entity.
     */
    private void applyCollar(Entity entity, DyeColor color) {
        if (entity instanceof Wolf wolf) {
            wolf.setCollarColor(color);
        } else if (entity instanceof Cat cat) {
            cat.setCollarColor(color);
        }
    }

    /**
     * Convert a hex color string (#RRGGBB) to the closest DyeColor.
     * Uses Euclidean distance in RGB space.
     */
    private DyeColor hexToDyeColor(String hex) {
        if (hex == null || hex.length() < 7) return DyeColor.RED;
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            DyeColor closest = DyeColor.RED;
            double minDist = Double.MAX_VALUE;

            for (DyeColor dye : DyeColor.values()) {
                org.bukkit.Color c = dye.getColor();
                double dist = Math.sqrt(
                    Math.pow(r - c.getRed(), 2) +
                    Math.pow(g - c.getGreen(), 2) +
                    Math.pow(b - c.getBlue(), 2)
                );
                if (dist < minDist) {
                    minDist = dist;
                    closest = dye;
                }
            }
            return closest;
        } catch (NumberFormatException e) {
            return DyeColor.RED;
        }
    }

    /**
     * Extract DyeColor from a dye Material (e.g. RED_DYE → DyeColor.RED).
     * Returns null if the material is not a dye.
     */
    private DyeColor dyeColorFromMaterial(Material material) {
        String name = material.name();
        if (!name.endsWith("_DYE")) return null;
        try {
            return DyeColor.valueOf(name.replace("_DYE", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String petName(PetProfile profile) {
        return profile.customName() != null ? profile.customName() : profile.entityType().name();
    }
}
