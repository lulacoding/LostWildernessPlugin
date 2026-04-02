---
title: Personality Mechanics
description: Passive trait effects, elemental powers, holy enchants, and ultimate items detail.
tags:
  - system
  - personality
status: implemented
phase: phase-1
owner: dev
action: none
---
# Personality System - Advanced Mechanics Implementation Guide

This document provides detailed implementation guidance for all advanced mechanics marked as TODO in Phase 4.

**Status**: Phase 4 complete with core mechanics. This document covers remaining advanced features.


## Trait Passive Abilities (TODOs)

### MAGE MASTER: Potion Duration Stacking

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitPassiveListener.java`

**Description**: When a player drinks a potion while already having that effect, instead of replacing it, the durations should stack.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onMagePotionStack(EntityPotionEffectEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    if (event.getCause() != EntityPotionEffectEvent.Cause.POTION_DRINK) return;
    if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

    Player player = (Player) event.getEntity();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.MAGE, TraitTier.MASTER)) return;

    PotionEffectType type = event.getModifiedType();
    PotionEffect newEffect = event.getNewEffect();
    PotionEffect oldEffect = event.getOldEffect();

    if (oldEffect != null && oldEffect.getType() == type) {
        // Stack durations
        int combinedDuration = oldEffect.getDuration() + newEffect.getDuration();
        int amplifier = Math.max(oldEffect.getAmplifier(), newEffect.getAmplifier());

        event.setCancelled(true);
        player.addPotionEffect(new PotionEffect(
            type,
            combinedDuration,
            amplifier,
            newEffect.isAmbient(),
            newEffect.hasParticles(),
            newEffect.hasIcon()
        ));
    }
}
```

**Testing**:
1. Drink Strength potion
2. Drink another Strength potion before it expires
3. Verify duration stacks (e.g., 3:00 + 3:00 = 6:00)


### HEALER TRAIT: AoE Regen/Wither Aura

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitPassiveListener.java` + new scheduled task

**Description**: Every 15 seconds, apply Regeneration I to nearby players (Good alignment) or Wither I to nearby enemies (Evil alignment).

**Implementation Approach**:

Create a scheduled task in `SurvivalV2Plugin`:

```java
// In SurvivalV2Plugin.onEnable():
private void startHealerAuraTask(TraitService traitService, ReputationService reputationService) {
    new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!traitService.hasActiveTrait(
                    player.getUniqueId(),
                    PersonalityTrait.HEALER,
                    TraitTier.TRAIT
                )) continue;

                // Check alignment
                int honor = reputationService.getHonor(player.getUniqueId());
                boolean isGood = honor >= 0;

                if (isGood) {
                    // AoE Regen to nearby players
                    player.getNearbyEntities(10, 10, 10).stream()
                        .filter(e -> e instanceof Player)
                        .map(e -> (Player) e)
                        .forEach(p -> p.addPotionEffect(
                            new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false, false)
                        ));
                } else {
                    // AoE Wither to nearby hostile mobs
                    player.getNearbyEntities(10, 10, 10).stream()
                        .filter(e -> e instanceof Monster)
                        .map(e -> (LivingEntity) e)
                        .forEach(mob -> mob.addPotionEffect(
                            new PotionEffect(PotionEffectType.WITHER, 100, 0, true, false, false)
                        ));
                }
            }
        }
    }.runTaskTimer(this, 0L, 300L); // Every 15 seconds (300 ticks)
}
```

**Dependencies**: ReputationService (for alignment check)

**Testing**:
1. Have HEALER trait at TRAIT tier
2. Stand near other players (Good) or mobs (Evil)
3. Verify effect applies every 15 seconds
4. Test alignment switching


### ALCHEMIST TRAIT: Brewed Potions +25% Duration

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitPassiveListener.java`

**Description**: Potions brewed by ALCHEMIST players last 25% longer.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onAlchemistBrew(BrewEvent event) {
    // Get player who owns the brewing stand (requires tracking)
    // This is complex because BrewEvent doesn't have a player reference

    // Option 1: Track brewing stands by location
    // When player places brewing stand, store location → UUID mapping
    // On brew, look up owner and modify potion meta

    BrewerInventory inventory = event.getContents();
    Location standLocation = inventory.getLocation();

    UUID owner = getBrewingStandOwner(standLocation);
    if (owner == null) return;

    if (!hasActiveTrait(owner, PersonalityTrait.ALCHEMIST, TraitTier.TRAIT)) return;

    // Modify potion duration in inventory after brew completes
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        for (int i = 0; i < 3; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.POTION) {
                extendPotionDuration(item, 1.25);
            }
        }
    }, 1L);
}

private void extendPotionDuration(ItemStack potion, double multiplier) {
    PotionMeta meta = (PotionMeta) potion.getItemMeta();
    if (!meta.hasCustomEffects()) return;

    meta.clearCustomEffects();
    meta.getCustomEffects().forEach(effect -> {
        PotionEffect extended = new PotionEffect(
            effect.getType(),
            (int) (effect.getDuration() * multiplier),
            effect.getAmplifier(),
            effect.isAmbient(),
            effect.hasParticles(),
            effect.hasIcon()
        );
        meta.addCustomEffect(extended, true);
    });

    potion.setItemMeta(meta);
}
```

**Additional Requirement**: Track brewing stand ownership (requires new listener on BlockPlaceEvent).

**Testing**:
1. Place brewing stand as ALCHEMIST
2. Brew a potion (e.g., Strength)
3. Verify duration is 25% longer than vanilla


### SMITH TRAIT: Anvil Repair Cost -2 Levels

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitPassiveListener.java`

**Description**: Reduce anvil XP cost by 2 levels.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onSmithAnvilUse(PrepareAnvilEvent event) {
    if (event.getView().getPlayer() == null) return;

    Player player = (Player) event.getView().getPlayer();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SMITH, TraitTier.TRAIT)) return;

    AnvilInventory inventory = event.getInventory();
    int originalCost = inventory.getRepairCost();

    // Reduce cost by 2 levels (minimum 1)
    int newCost = Math.max(1, originalCost - 2);
    inventory.setRepairCost(newCost);
}
```

**Testing**:
1. Place damaged item in anvil
2. Verify XP cost is 2 levels less than normal
3. Confirm repair works correctly


### SCOUT TRAIT: Permanent +15% Speed

**Current Status**: TODO
**Complexity**: Low
**File**: New scheduled task in `SurvivalV2Plugin`

**Description**: SCOUT TRAIT players have permanent Speed I effect.

**Implementation Approach**:

```java
// In SurvivalV2Plugin.onEnable():
private void startScoutSpeedTask(TraitService traitService) {
    new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!traitService.hasActiveTrait(
                    player.getUniqueId(),
                    PersonalityTrait.SCOUT,
                    TraitTier.TRAIT
                )) continue;

                // Apply Speed I if not already active
                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        100, // 5 seconds (will be reapplied)
                        0,   // Speed I
                        true, false, false
                    ));
                }
            }
        }
    }.runTaskTimer(this, 0L, 60L); // Every 3 seconds (60 ticks)
}
```

**Testing**:
1. Login as SCOUT TRAIT player
2. Verify Speed I effect is always active
3. Test that effect persists after death/respawn


### TAMER TRAIT: +25% First-Attempt Taming Success

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitPassiveListener.java`

**Description**: TAMER players have 25% higher chance of successful taming on first attempt.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onTamerTaming(EntityTameEvent event) {
    if (!(event.getOwner() instanceof Player)) return;

    Player player = (Player) event.getOwner();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.TAMER, TraitTier.TRAIT)) return;

    // If taming failed, give 25% chance to succeed anyway
    if (event.isCancelled()) {
        if (Math.random() < 0.25) {
            event.setCancelled(false);
            player.sendMessage("§a§lTamer: §7Your animal affinity helped!");
        }
    }
}
```

**Note**: This requires the taming event to be cancellable. If vanilla taming uses RNG internally, this may not work and alternative approach is needed.

**Alternative Approach**: Track taming attempts and auto-succeed every 4th attempt.

**Testing**:
1. Attempt to tame multiple wolves
2. Compare success rate with/without trait
3. Verify approximately 25% boost


### RUNEKEEPER TRAIT: Enchanted Items Glow + +10% Effectiveness

**Current Status**: TODO
**Complexity**: Very High
**File**: `TraitPassiveListener.java` + packet manipulation

**Description**: RUNEKEEPER players see enchanted items glow, and all enchantments are 10% more effective.

**Glow Effect (ProtocolLib)**:

```java
// Requires ProtocolLib
@EventHandler
public void onRunekeeperJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RUNEKEEPER, TraitTier.TRAIT)) return;

    // Apply glowing effect to all enchanted items in inventory
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
        if (!player.isOnline()) return;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getEnchantments().isEmpty()) {
                // Add glowing flag via item meta
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // Optional
                    // ProtocolLib: Send entity metadata packet with glowing flag
                }
            }
        }
    }, 0L, 20L);
}
```

**+10% Enchant Effectiveness**:

This requires modifying damage/protection calculations for each enchantment type. Example for Sharpness:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onRunekeeperEnchantDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;

    Player player = (Player) event.getDamager();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RUNEKEEPER, TraitTier.TRAIT)) return;

    ItemStack weapon = player.getInventory().getItemInMainHand();
    if (!weapon.hasItemMeta()) return;

    // Check for damage enchantments
    int sharpness = weapon.getEnchantmentLevel(Enchantment.SHARPNESS);
    if (sharpness > 0) {
        // Vanilla Sharpness adds 0.5 + 0.5 * level damage
        double vanillaDamage = 0.5 + (0.5 * sharpness);
        double bonusDamage = vanillaDamage * 0.10; // +10%
        event.setDamage(event.getDamage() + bonusDamage);
    }

    // Repeat for other damage enchantments (Smite, Bane of Arthropods, Power, etc.)
}
```

**Dependencies**: ProtocolLib (for glow effect)

**Testing**:
1. Hold enchanted sword as RUNEKEEPER
2. Verify visual glow effect
3. Test damage boost (calculate expected damage with +10%)


### ILLUSIONIST TRAIT: Decoy Spawn (5s, 2min cooldown)

**Current Status**: TODO
**Complexity**: Very High
**File**: New command + scheduled task

**Description**: ILLUSIONIST TRAIT can spawn a fake player decoy. Good alignment: harmless. Evil alignment: attracts mob aggro.

**Implementation Approach**:

Create `/trait decoy` command:

```java
public class DecoyCommand implements CommandExecutor {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 120_000; // 2 minutes

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ILLUSIONIST, TraitTier.TRAIT)) {
            player.sendMessage("§cOnly Illusionists can use decoys!");
            return true;
        }

        // Check cooldown
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            long lastUse = cooldowns.get(uuid);
            long remaining = (lastUse + COOLDOWN_MS) - System.currentTimeMillis();
            if (remaining > 0) {
                player.sendMessage("§cDecoy on cooldown! " + (remaining / 1000) + "s remaining");
                return true;
            }
        }

        // Spawn decoy (armor stand with player head)
        spawnDecoy(player);
        cooldowns.put(uuid, System.currentTimeMillis());

        return true;
    }

    private void spawnDecoy(Player player) {
        Location loc = player.getLocation();
        ArmorStand decoy = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        decoy.setCustomName(player.getName());
        decoy.setCustomNameVisible(true);
        decoy.setGravity(false);
        decoy.setVisible(false); // Make armor stand invisible
        decoy.setMarker(true);

        // Add player head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
        decoy.getEquipment().setHelmet(head);

        // Evil alignment: attract mob aggro
        int honor = reputationService.getHonor(player.getUniqueId());
        if (honor < 0) {
            // Make nearby mobs target the decoy
            decoy.getNearbyEntities(15, 15, 15).stream()
                .filter(e -> e instanceof Monster)
                .forEach(mob -> ((Monster) mob).setTarget(decoy));
        }

        // Remove after 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            decoy.remove();
            player.sendMessage("§7Your decoy vanished.");
        }, 100L);

        player.sendMessage("§a§lIllusionist: §7Decoy spawned!");
    }
}
```

**Dependencies**: ReputationService (for alignment check)

**Testing**:
1. Use `/trait decoy` as ILLUSIONIST
2. Verify armor stand spawns with player head
3. Test mob attraction (evil alignment)
4. Verify 5-second duration and 2-minute cooldown


## Holy Enchant Effects (TODOs)

### LUNAR_BLESSING: Nighttime Regeneration

**Current Status**: TODO
**Complexity**: Low
**File**: New scheduled task in `SurvivalV2Plugin`

**Description**: Players wearing items with LUNAR_BLESSING regenerate health slowly during nighttime.

**Implementation Approach**:

```java
// In SurvivalV2Plugin.onEnable():
private void startLunarBlessingTask(HolyEnchantService holyEnchantService) {
    new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Check if nighttime (13000-23000 ticks)
                long time = player.getWorld().getTime();
                if (time < 13000 || time > 23000) continue;

                // Check if player has LUNAR_BLESSING on helmet
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && holyEnchantService.hasHolyEnchant(helmet, HolyEnchant.LUNAR_BLESSING)) {
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        60, // 3 seconds
                        0,  // Regen I
                        true, false, false
                    ));
                }
            }
        }
    }.runTaskTimer(this, 0L, 40L); // Every 2 seconds (40 ticks)
}
```

**Testing**:
1. Equip helmet with LUNAR_BLESSING
2. Wait for nighttime
3. Verify Regeneration I effect applies
4. Test that it stops during daytime


### PHOENIX_FLAME: Once-Per-Day Revive

**Current Status**: TODO
**Complexity**: High
**File**: `HolyEnchantEffectListener.java` + cooldown tracking

**Description**: When player with PHOENIX_FLAME dies, they revive once per day with fire immunity.

**Implementation Approach**:

```java
private final Map<UUID, Long> phoenixCooldowns = new HashMap<>();
private static final long PHOENIX_COOLDOWN_MS = 86_400_000; // 24 hours

@EventHandler(priority = EventPriority.HIGHEST)
public void onPhoenixDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID uuid = player.getUniqueId();

    // Check if player has PHOENIX_FLAME (totem or chestplate)
    ItemStack chestplate = player.getInventory().getChestplate();
    boolean hasPhoenix = false;

    if (chestplate != null && hasHolyEnchant(chestplate, HolyEnchant.PHOENIX_FLAME)) {
        hasPhoenix = true;
    }

    // Also check for totem in hand
    ItemStack mainHand = player.getInventory().getItemInMainHand();
    ItemStack offHand = player.getInventory().getItemInOffHand();
    if ((mainHand.getType() == Material.TOTEM_OF_UNDYING && hasHolyEnchant(mainHand, HolyEnchant.PHOENIX_FLAME)) ||
        (offHand.getType() == Material.TOTEM_OF_UNDYING && hasHolyEnchant(offHand, HolyEnchant.PHOENIX_FLAME))) {
        hasPhoenix = true;
    }

    if (!hasPhoenix) return;

    // Check cooldown
    if (phoenixCooldowns.containsKey(uuid)) {
        long lastUse = phoenixCooldowns.get(uuid);
        long remaining = (lastUse + PHOENIX_COOLDOWN_MS) - System.currentTimeMillis();
        if (remaining > 0) {
            player.sendMessage("§cPhoenix Flame on cooldown! " + (remaining / 3600000) + " hours remaining");
            return;
        }
    }

    // Revive player
    event.setCancelled(true);
    phoenixCooldowns.put(uuid, System.currentTimeMillis());

    Bukkit.getScheduler().runTask(plugin, () -> {
        player.setHealth(player.getMaxHealth() / 2); // Revive at half health
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.FIRE_RESISTANCE,
            1200, // 1 minute
            0,
            false, true, true
        ));

        player.sendMessage("§6§l✦ Phoenix Flame: §7You have been reborn from the ashes!");
        player.getWorld().spawnParticle(
            Particle.FLAME,
            player.getLocation(),
            100,
            1, 1, 1,
            0.1
        );
    });
}
```

**Persistence**: Store cooldowns in database for cross-session persistence.

**Testing**:
1. Die with PHOENIX_FLAME enchant
2. Verify revive at half health with fire immunity
3. Die again within 24 hours - verify no revive
4. Wait 24 hours - verify revive works again


## Ultimate Item Mechanics (TODOs)

### Warlord's Blade: Damage Scales With Missing Health

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitItemListener.java`

**Description**: WARRIOR Ultimate item. Damage increases as player's health decreases.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onWarlordsBladeDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;

    Player player = (Player) event.getDamager();
    ItemStack weapon = player.getInventory().getItemInMainHand();

    // Check for BERSERKER trait enchant (Warlord's Blade)
    String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
    if (traitEnchant == null || !traitEnchant.equals("BERSERKER")) return;

    // Calculate bonus damage based on missing health
    double missingHealthPercent = 1.0 - (player.getHealth() / player.getMaxHealth());
    double bonusDamage = event.getDamage() * missingHealthPercent * 0.5; // Up to +50% at 0 HP

    event.setDamage(event.getDamage() + bonusDamage);

    if (bonusDamage > 0) {
        player.sendActionBar("§c+" + String.format("%.1f", bonusDamage) + " damage!");
    }
}
```

**Testing**:
1. Equip Warlord's Blade
2. Take damage to reduce health
3. Attack mob and verify damage increases
4. At 50% HP, damage should be +25% of base
5. At 10% HP, damage should be +45% of base


### Ragnarok Axe: Kill-Triggered Speed Burst

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitItemListener.java`

**Description**: BERSERKER Ultimate item. Each kill refreshes Speed I burst.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onRagnarokKill(EntityDeathEvent event) {
    Player killer = event.getEntity().getKiller();
    if (killer == null) return;

    ItemStack weapon = killer.getInventory().getItemInMainHand();

    // Check for BLOODLUST trait enchant (Ragnarok Axe)
    String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
    if (traitEnchant == null || !traitEnchant.equals("BLOODLUST")) return;

    // Apply Speed I for 10 seconds
    killer.addPotionEffect(new PotionEffect(
        PotionEffectType.SPEED,
        200, // 10 seconds
        0,   // Speed I
        false, true, true
    ));

    killer.sendMessage("§c§lRagnarok: §7Speed boost refreshed!");
    killer.playSound(killer.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
}
```

**Testing**:
1. Equip Ragnarok Axe
2. Kill a mob
3. Verify Speed I for 10 seconds
4. Kill another mob within 10 seconds
5. Verify effect refreshes to 10 seconds


### Mirror Shard: Position Swap

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitItemListener.java`

**Description**: ILLUSIONIST Ultimate item. Right-click a player to swap positions.

**Implementation Approach**:

```java
private final Map<UUID, Long> mirrorCooldowns = new HashMap<>();
private static final long MIRROR_COOLDOWN_MS = 30_000; // 30 seconds

@EventHandler(priority = EventPriority.NORMAL)
public void onMirrorShardUse(PlayerInteractEntityEvent event) {
    if (!(event.getRightClicked() instanceof Player)) return;

    Player user = event.getPlayer();
    Player target = (Player) event.getRightClicked();
    ItemStack mirror = user.getInventory().getItemInMainHand();

    // Check for ILLUSION trait enchant (Mirror Shard)
    String traitEnchant = holyEnchantService.getTraitEnchant(mirror);
    if (traitEnchant == null || !traitEnchant.equals("ILLUSION")) return;

    UUID uuid = user.getUniqueId();

    // Check cooldown
    if (mirrorCooldowns.containsKey(uuid)) {
        long lastUse = mirrorCooldowns.get(uuid);
        long remaining = (lastUse + MIRROR_COOLDOWN_MS) - System.currentTimeMillis();
        if (remaining > 0) {
            user.sendMessage("§cMirror Shard on cooldown! " + (remaining / 1000) + "s remaining");
            return;
        }
    }

    // Swap positions
    Location userLoc = user.getLocation().clone();
    Location targetLoc = target.getLocation().clone();

    user.teleport(targetLoc);
    target.teleport(userLoc);

    // Visual effects
    user.getWorld().spawnParticle(Particle.PORTAL, userLoc, 50, 0.5, 1, 0.5, 0.1);
    target.getWorld().spawnParticle(Particle.PORTAL, targetLoc, 50, 0.5, 1, 0.5, 0.1);

    user.sendMessage("§d§lMirror Shard: §7Swapped positions with " + target.getName());
    target.sendMessage("§d§lMirror Shard: §7Your position was swapped by " + user.getName());

    mirrorCooldowns.put(uuid, System.currentTimeMillis());
}
```

**Testing**:
1. Right-click another player with Mirror Shard
2. Verify positions swap
3. Test cooldown (30 seconds)
4. Test visual effects


### Runeblade: Random Enchant Effect on Crits

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitItemListener.java`

**Description**: RUNEKEEPER Ultimate item. Critical hits trigger random enchantment effects.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onRunebladeCrit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;
    if (!(event.getEntity() instanceof LivingEntity)) return;

    Player player = (Player) event.getDamager();
    ItemStack weapon = player.getInventory().getItemInMainHand();

    // Check for RUNIC_OVERLOAD trait enchant (Runeblade)
    String traitEnchant = holyEnchantService.getTraitEnchant(weapon);
    if (traitEnchant == null || !traitEnchant.equals("RUNIC_OVERLOAD")) return;

    // Check if critical hit (player falling)
    if (player.getFallDistance() <= 0) return;

    LivingEntity target = (LivingEntity) event.getEntity();

    // Random enchant effect (20% chance per hit)
    if (Math.random() < 0.20) {
        triggerRandomEnchantEffect(player, target);
    }
}

private void triggerRandomEnchantEffect(Player player, LivingEntity target) {
    String[] effects = {
        "FIRE",        // Set on fire
        "LIGHTNING",   // Strike lightning
        "FREEZE",      // Slowness + Mining Fatigue
        "POISON",      // Poison
        "KNOCKBACK",   // Strong knockback
        "WEAKNESS",    // Weakness
        "BLIND"        // Blindness
    };

    String effect = effects[(int) (Math.random() * effects.length)];

    switch (effect) {
        case "FIRE":
            target.setFireTicks(100);
            player.sendActionBar("§6Runeblade: Fire!");
            break;
        case "LIGHTNING":
            target.getWorld().strikeLightning(target.getLocation());
            player.sendActionBar("§bRuneblade: Lightning!");
            break;
        case "FREEZE":
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
            target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 2));
            player.sendActionBar("§fRuneblade: Freeze!");
            break;
        case "POISON":
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            player.sendActionBar("§2Runeblade: Poison!");
            break;
        case "KNOCKBACK":
            target.setVelocity(target.getLocation().toVector()
                .subtract(player.getLocation().toVector())
                .normalize()
                .multiply(2)
                .setY(1));
            player.sendActionBar("§eRuneblade: Knockback!");
            break;
        case "WEAKNESS":
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.sendActionBar("§7Runeblade: Weakness!");
            break;
        case "BLIND":
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            player.sendActionBar("§8Runeblade: Blindness!");
            break;
    }
}
```

**Testing**:
1. Equip Runeblade
2. Perform critical hits (jump + attack)
3. Verify random effects trigger (~20% chance)
4. Test all 7 possible effects
5. Verify action bar messages


### Scheduled Tasks

For abilities that need periodic checks (auras, regeneration, etc.):

```java
// In SurvivalV2Plugin.onEnable():
private void startPeriodicAbilityTask() {
    new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Check player traits and apply periodic effects
                checkAndApplyPeriodicAbilities(player);
            }
        }
    }.runTaskTimer(this, 0L, 20L); // Every 1 second (20 ticks)
}
```

**Best Practices**:
- Use longer intervals when possible (every 60 ticks instead of 20)
- Batch operations instead of individual checks
- Cancel tasks on plugin disable


### PDC (PersistentDataContainer) Usage

For storing metadata on items/entities:

```java
// Write to PDC
ItemMeta meta = item.getItemMeta();
PersistentDataContainer pdc = meta.getPersistentDataContainer();
NamespacedKey key = new NamespacedKey(plugin, "custom_data");

pdc.set(key, PersistentDataType.STRING, "value");
item.setItemMeta(meta);

// Read from PDC
if (pdc.has(key, PersistentDataType.STRING)) {
    String value = pdc.get(key, PersistentDataType.STRING);
}

// Remove from PDC
pdc.remove(key);
```

**Use Cases**:
- Temporary enchantments (Ranger's Quiver Infinity)
- Cooldown markers
- Item ownership tracking
- Custom mob properties


## Priority Order for Implementation

Based on complexity and impact, here's the recommended order:

### High Priority (Easy + High Impact)
1. SAGE TRAIT: +25% XP ✅ (Already done)
2. SMITH TRAIT: Anvil cost -2 levels
3. SCOUT TRAIT: Permanent +15% speed
4. RANGER MASTER: Tamed mob damage boost
5. TAMER MASTER: Tamed mob HP boost
6. ILLUSIONIST MASTER: Invisibility 3× duration
7. LUNAR_BLESSING: Nighttime regeneration
8. Warlord's Blade: Scaling damage
9. Ragnarok Axe: Kill speed burst

### Medium Priority (Medium Complexity)
10. HEALER TRAIT: AoE Regen/Wither aura
11. ALCHEMIST TRAIT: Brew duration +25%
12. SMITH MASTER: Crafting material refund
13. SCOUT MASTER: Sneak = walk speed
14. TAMER TRAIT: Taming success boost
15. STARFALL: Additional arrow projectiles
16. Ranger's Quiver: Dynamic Infinity
17. Ancient Whistle: Wolf summon
18. Mirror Shard: Position swap
19. Staff of the Covenant: Alignment AoE
20. Runeblade: Random enchant crits

### Low Priority (Complex / Requires External Dependencies)
21. MAGE MASTER: Potion stacking
22. WARRIOR MASTER: Shield cooldown (ProtocolLib)
23. ALCHEMIST MASTER: 4-slot brewing (Custom GUI)
24. RUNEKEEPER TRAIT: Enchant glow (ProtocolLib)
25. RUNEKEEPER MASTER: No lapis enchanting
26. ILLUSIONIST TRAIT: Decoy spawn
27. PHOENIX_FLAME: Once-per-day revive
28. ECHO_STEP: Afterimage entities


## Documentation Standards

When implementing each mechanic, update:

1. **Code Comments**: Explain the logic inline
2. **implementation-status.md**: Change ❌ to ✅
3. **CHANGELOG.md**: Add entry with implementation details
4. **Testing Results**: Document actual behavior vs expected

---

## Conclusion

This document provides complete implementation guidance for all 28 advanced mechanics. Each section includes:
- Clear description
- Code examples
- Testing procedures
- Complexity ratings
- Dependencies

For questions or clarifications, refer to:
- `personality-system.md` for system overview
- `TraitPassiveListener.java` for implemented examples
- Bukkit/Spigot API documentation for event handling

**Estimated Total Implementation Time**: 60-80 hours (assuming solo developer)

**Next Steps**: Prioritize based on "Priority Order" section above, starting with easy + high-impact mechanics.
