package com.lostwilderness.survivalv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PlayerTraitProfile;
import com.lostwilderness.rpgcore.personality.TraitService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies elemental passive abilities (god-tier effects).
 * Only active after player completes their first temple AND is post-game.
 *
 * 5 Elements:
 * - FIRE: Immune to fire and lava damage
 * - EARTH: Immune to suffocation damage
 * - WIND: Elytra never loses durability
 * - WATER: Permanent underwater breathing
 * - AETHER: ~90% fall damage reduction
 */
public class ElementalPassiveListener implements Listener {

    private final TraitService traitService;
    // Cache: UUID -> profile. Null value = not yet loaded. Empty optional = no profile.
    private final Map<UUID, Optional<PlayerTraitProfile>> profileCache = new ConcurrentHashMap<>();
    // Tracks which UUIDs currently have an async refresh in flight, to avoid duplicate fetches.
    private final Map<UUID, Boolean> refreshing = new ConcurrentHashMap<>();

    public ElementalPassiveListener(TraitService traitService) {
        this.traitService = traitService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        profileCache.remove(uuid);
        refreshing.remove(uuid);
    }

    // ========== FIRE ==========

    /**
     * FIRE: Immune to fire and lava damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireElementFireDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasElementActivated(player.getUniqueId(), Element.FIRE)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE ||
            cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
            cause == EntityDamageEvent.DamageCause.LAVA ||
            cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
            player.setFireTicks(0); // Remove fire visual
        }
    }

    // ========== EARTH ==========

    /**
     * EARTH: Immune to suffocation damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEarthElementSuffocation(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasElementActivated(player.getUniqueId(), Element.EARTH)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.SUFFOCATION ||
            cause == EntityDamageEvent.DamageCause.CRAMMING) {
            event.setCancelled(true);
        }
    }

    // ========== WIND ==========

    /**
     * WIND: Elytra never loses durability
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindElementElytraDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!hasElementActivated(player.getUniqueId(), Element.WIND)) return;

        ItemStack item = event.getItem();
        if (item.getType() == Material.ELYTRA) {
            event.setCancelled(true);
        }
    }

    // ========== WATER ==========

    /**
     * WATER: Permanent underwater breathing
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWaterElementMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;

        Player player = event.getPlayer();
        if (!hasElementActivated(player.getUniqueId(), Element.WATER)) return;

        // Check if player is in water
        if (player.isInWater() || player.getLocation().getBlock().getType() == Material.WATER) {
            // Apply Water Breathing if not already active
            if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.WATER_BREATHING,
                    200, // 10 seconds (will be reapplied continuously)
                    0,
                    true, // ambient
                    false, // particles (hidden for cleaner UX)
                    false  // icon (hidden)
                ));
            }
        }
    }

    /**
     * WATER: Cancel drowning damage
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWaterElementDrowning(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasElementActivated(player.getUniqueId(), Element.WATER)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }

    // ========== AETHER ==========

    /**
     * AETHER: ~90% fall damage reduction
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onAetherElementFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!hasElementActivated(player.getUniqueId(), Element.AETHER)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(event.getDamage() * 0.10); // 90% reduction
        }
    }

    // ========== UTILITY ==========

    /**
     * Check if player's element is activated (temple complete).
     * Reads from cache only — never blocks the server thread.
     */
    private boolean hasElementActivated(UUID uuid, Element element) {
        Optional<PlayerTraitProfile> cached = profileCache.get(uuid);
        if (cached == null) {
            // Not cached yet — trigger async load and return false for now
            refreshProfileAsync(uuid);
            return false;
        }
        if (cached.isEmpty()) return false;
        PlayerTraitProfile p = cached.get();
        return p.element() == element && p.elementActivated();
    }

    /**
     * Asynchronously fetches the profile and stores it in cache.
     * Only one fetch per UUID at a time.
     */
    private void refreshProfileAsync(UUID uuid) {
        if (refreshing.putIfAbsent(uuid, Boolean.TRUE) != null) return; // already in flight
        traitService.getProfile(uuid).whenComplete((profile, ex) -> {
            profileCache.put(uuid, ex == null ? profile : Optional.empty());
            refreshing.remove(uuid);
        });
    }
}
