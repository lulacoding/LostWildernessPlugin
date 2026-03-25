package com.lostwilderness.rpgcore.pets.listener;

import com.lostwilderness.rpgcore.pets.PetPersonality;
import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-driven pet personality effects.
 *
 * Phase 4 personalities handled here:
 * - LOYAL_SENTINEL  : EntityTargetEvent   → warn owner when mob targets them
 * - LUCKY_CHARM     : EntityDeathEvent    → extra drop from mob kills
 * - ARROW_SHIELD    : EntityDamageByEntityEvent → cancel projectile (1min cooldown)
 * - CHAOS_SPARK     : EntityTargetEvent   → prevent creeper targeting owner
 * - ETERNAL_YOUTH   : EntityDamageEvent   → prevent pet death (24h cooldown)
 * - GUARDIAN_ANGEL  : EntityDamageEvent   → prevent owner lethal hit (24h cooldown)
 */
public final class PetPersonalityListener implements Listener {

    private static final long COOLDOWN_24H_MS = 86_400_000L;
    private static final long COOLDOWN_1MIN_MS = 60_000L;
    private static final double PROXIMITY = 32.0;

    private final PetService petService;
    private final NamespacedKey petUuidKey;
    private final NamespacedKey petRegisteredKey;

    // petUuid -> last activation time
    private final Map<UUID, Long> arrowShieldCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> eternalYouthCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> guardianAngelCooldowns = new ConcurrentHashMap<>();

    public PetPersonalityListener(PetService petService, Plugin plugin) {
        this.petService = petService;
        this.petUuidKey = new NamespacedKey(plugin, "pet_uuid");
        this.petRegisteredKey = new NamespacedKey(plugin, "pet_registered");
    }

    // ── LOYAL_SENTINEL & CHAOS_SPARK ─────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        Set<UUID> petUuids = petService.getCachedPetUuids(player.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;

            Entity petEntity = Bukkit.getEntity(petUuid);
            if (!isNearby(petEntity, player)) continue;

            if (profile.personality() == PetPersonality.LOYAL_SENTINEL) {
                // Warn owner with sound + particles at pet location
                player.sendMessage(ChatColor.YELLOW + "⚠ " + ChatColor.WHITE +
                    petName(profile) + ChatColor.YELLOW + " senses danger nearby!");
                player.playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
                if (petEntity != null) {
                    petEntity.getWorld().spawnParticle(
                        Particle.LAVA, petEntity.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0
                    );
                }
                return; // One sentinel warning per event is enough
            }

            if (profile.personality() == PetPersonality.CHAOS_SPARK) {
                String entityType = event.getEntity().getType().name();
                if (entityType.equals("CREEPER")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    // ── LUCKY_CHARM ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Set<UUID> petUuids = petService.getCachedPetUuids(killer.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;
            if (profile.personality() != PetPersonality.LUCKY_CHARM) continue;

            Entity petEntity = Bukkit.getEntity(petUuid);
            if (!isNearby(petEntity, killer)) continue;

            // Duplicate the drop list once (adds a copy of all existing drops)
            int dropCount = event.getDrops().size();
            if (dropCount > 0) {
                var extraDrops = new java.util.ArrayList<>(event.getDrops());
                event.getDrops().addAll(extraDrops);
            }
            event.setDroppedExp((int) (event.getDroppedExp() * 1.25));
            return;
        }
    }

    // ── ARROW_SHIELD ──────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof AbstractArrow)) return;

        Set<UUID> petUuids = petService.getCachedPetUuids(player.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;
            if (profile.personality() != PetPersonality.ARROW_SHIELD) continue;

            Entity petEntity = Bukkit.getEntity(petUuid);
            if (!isNearby(petEntity, player)) continue;

            if (!isOnCooldown(arrowShieldCooldowns, petUuid, COOLDOWN_1MIN_MS)) {
                event.setCancelled(true);
                arrowShieldCooldowns.put(petUuid, System.currentTimeMillis());
                player.sendMessage(ChatColor.AQUA + petName(profile) +
                    ChatColor.WHITE + " blocked an arrow for you!");
                if (petEntity != null) {
                    petEntity.getWorld().spawnParticle(
                        Particle.CRIT, petEntity.getLocation().add(0, 1, 0), 12, 0.4, 0.4, 0.4, 0
                    );
                }
                return;
            }
        }
    }

    // ── ETERNAL_YOUTH & GUARDIAN_ANGEL ───────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damaged = event.getEntity();

        // --- ETERNAL_YOUTH: prevent pet death ---
        if (damaged instanceof LivingEntity livingPet && !(damaged instanceof Player)) {
            if (!isPetRegistered(damaged)) return;
            UUID petUuid = getPetUuidFromPdc(damaged);
            if (petUuid == null) return;

            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) return;
            if (profile.personality() != PetPersonality.ETERNAL_YOUTH) return;
            if (isOnCooldown(eternalYouthCooldowns, petUuid, COOLDOWN_24H_MS)) return;

            // Check if this damage would be lethal
            if (livingPet.getHealth() - event.getFinalDamage() > 0) return;

            event.setDamage(0);
            livingPet.setHealth(Math.min(livingPet.getHealth(), 1.0));
            eternalYouthCooldowns.put(petUuid, System.currentTimeMillis());

            // Notify owner
            Player owner = Bukkit.getPlayer(profile.ownerUuid());
            if (owner != null) {
                owner.sendMessage(ChatColor.GOLD + "✦ " + petName(profile) +
                    ChatColor.WHITE + "'s Eternal Youth prevented a fatal blow!");
            }
            damaged.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING, damaged.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0
            );
            return;
        }

        // --- GUARDIAN_ANGEL: prevent owner death ---
        if (!(damaged instanceof Player player)) return;

        Set<UUID> petUuids = petService.getCachedPetUuids(player.getUniqueId());
        if (petUuids.isEmpty()) return;

        for (UUID petUuid : petUuids) {
            PetProfile profile = petService.getCachedProfile(petUuid).orElse(null);
            if (profile == null || !profile.isAlive()) continue;
            if (profile.personality() != PetPersonality.GUARDIAN_ANGEL) continue;

            Entity petEntity = Bukkit.getEntity(petUuid);
            if (!isNearby(petEntity, player)) continue;
            if (isOnCooldown(guardianAngelCooldowns, petUuid, COOLDOWN_24H_MS)) continue;

            // Would this kill the player?
            if (player.getHealth() - event.getFinalDamage() > 0) continue;

            event.setCancelled(true);
            guardianAngelCooldowns.put(petUuid, System.currentTimeMillis());
            player.setHealth(4.0); // Left on 2 hearts
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1, false, true));

            player.sendMessage(ChatColor.GOLD + "✦ " + petName(profile) +
                ChatColor.WHITE + " saved your life! (Guardian Angel)");
            player.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0
            );
            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            return;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isPetRegistered(Entity entity) {
        return entity.getPersistentDataContainer().has(petRegisteredKey, PersistentDataType.BOOLEAN);
    }

    private UUID getPetUuidFromPdc(Entity entity) {
        String uuidStr = entity.getPersistentDataContainer().get(petUuidKey, PersistentDataType.STRING);
        if (uuidStr == null) return null;
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isNearby(Entity petEntity, Player player) {
        if (petEntity == null) return false;
        if (!petEntity.getWorld().equals(player.getWorld())) return false;
        return petEntity.getLocation().distanceSquared(player.getLocation()) <= PROXIMITY * PROXIMITY;
    }

    private boolean isOnCooldown(Map<UUID, Long> cooldowns, UUID petUuid, long durationMs) {
        Long last = cooldowns.get(petUuid);
        if (last == null) return false;
        return System.currentTimeMillis() - last < durationMs;
    }

    private String petName(PetProfile profile) {
        return profile.customName() != null ? profile.customName() : profile.entityType().name();
    }
}
