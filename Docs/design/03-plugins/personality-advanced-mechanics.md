# Personality System - Advanced Mechanics Implementation Guide

This document provides detailed implementation guidance for all advanced mechanics marked as TODO in Phase 4.

**Status**: Phase 4 complete with core mechanics. This document covers remaining advanced features.

---

## Table of Contents

1. [Trait Passive Abilities (TODOs)](#trait-passive-abilities-todos)
2. [Holy Enchant Effects (TODOs)](#holy-enchant-effects-todos)
3. [Ultimate Item Mechanics (TODOs)](#ultimate-item-mechanics-todos)
4. [General Implementation Patterns](#general-implementation-patterns)

---

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

---

### WARRIOR MASTER: Shield Cooldown Reduction (-50%)

**Current Status**: TODO
**Complexity**: High
**File**: `TraitPassiveListener.java`

**Description**: Reduce shield cooldown from 5 seconds to 2.5 seconds.

**Problem**: Shield cooldown is client-side and not directly modifiable via Bukkit API.

**Implementation Approach (Option 1 - ProtocolLib)**:

Requires ProtocolLib dependency. Intercept cooldown packets and modify the cooldown value.

```java
@EventHandler
public void onShieldBlock(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player)) return;

    Player player = (Player) event.getEntity();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.WARRIOR, TraitTier.MASTER)) return;

    // Check if player is blocking with shield
    if (player.isBlocking()) {
        // Use ProtocolLib to modify cooldown packet
        // Send packet with reduced cooldown ticks (50 instead of 100)
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, createCooldownPacket(
            Material.SHIELD,
            50 // 2.5 seconds instead of 5
        ));
    }
}

private PacketContainer createCooldownPacket(Material material, int cooldownTicks) {
    PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_COOLDOWN);
    packet.getIntegers()
        .write(0, material.getId()) // Material ID
        .write(1, cooldownTicks);   // Cooldown ticks
    return packet;
}
```

**Implementation Approach (Option 2 - Alternative Mechanic)**:

If ProtocolLib is not available, implement an alternative mechanic:
- Grant brief Resistance II on shield block
- Or: Grant Absorption hearts on block (already implemented via DIVINE_SHIELD)

**Dependencies**: ProtocolLib (if using Option 1)

**Testing**:
1. Block attack with shield
2. Verify cooldown is 2.5 seconds instead of 5
3. Test with/without trait to confirm difference

---

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

---

### RANGER MASTER: Tamed Mob Damage Boost (+15%)

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitPassiveListener.java`

**Description**: Wolves and cats tamed by RANGER MASTER players deal +15% damage.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onRangerTamedMobDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Tameable)) return;

    Tameable tamed = (Tameable) event.getDamager();
    if (!tamed.isTamed() || !(tamed.getOwner() instanceof Player)) return;

    Player owner = (Player) tamed.getOwner();
    if (!hasActiveTrait(owner.getUniqueId(), PersonalityTrait.RANGER, TraitTier.MASTER)) return;

    // Check if tamed mob is wolf or cat
    if (tamed instanceof Wolf || tamed instanceof Cat) {
        event.setDamage(event.getDamage() * 1.15);
    }
}
```

**Testing**:
1. Tame a wolf as RANGER MASTER
2. Have wolf attack a mob
3. Compare damage with/without trait
4. Verify +15% damage increase

---

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

---

### ALCHEMIST MASTER: 4-Slot Brewing Stand

**Current Status**: TODO
**Complexity**: Very High
**File**: New custom GUI class

**Description**: ALCHEMIST MASTER can brew 4 potions simultaneously instead of 3.

**Problem**: Vanilla brewing stand GUI only has 3 slots. This requires a custom GUI.

**Implementation Approach**:

Create custom brewing GUI using Bukkit inventory system:

```java
public class CustomBrewingStand {
    private final Inventory gui;
    private final Player owner;
    private ItemStack ingredient;
    private ItemStack fuel;
    private final ItemStack[] potionSlots = new ItemStack[4];

    public CustomBrewingStand(Player owner) {
        this.owner = owner;
        this.gui = Bukkit.createInventory(null, 27, "Alchemist Brewing Stand");

        // Layout:
        // [Ingredient] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
        // [Fuel]       [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
        // [Potion1] [Potion2] [Potion3] [Potion4] [ ] [ ] [ ] [ ] [ ]

        setupGuiLayout();
    }

    private void setupGuiLayout() {
        // Set placeholder items to show slots
        // ... (GUI design implementation)
    }

    public void startBrewing() {
        // Custom brewing logic
        new BukkitRunnable() {
            int brewTicks = 0;

            @Override
            public void run() {
                if (brewTicks++ >= 400) { // 20 seconds
                    completeBrewing();
                    cancel();
                    return;
                }

                // Update progress visually
                updateBrewingProgress(brewTicks / 400.0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void completeBrewing() {
        // Apply ingredient to all 4 potion slots
        for (int i = 0; i < 4; i++) {
            if (potionSlots[i] != null) {
                applyIngredientToPotion(potionSlots[i], ingredient);
            }
        }
    }
}
```

**Alternative Approach**: Use virtual inventory and automatically start 2 brewing cycles in parallel.

**Testing**:
1. Right-click brewing stand as ALCHEMIST MASTER
2. Place 4 water bottles + ingredient
3. Verify all 4 bottles brew simultaneously

---

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

---

### SMITH MASTER: Crafting Gear Uses 20% Fewer Materials

**Current Status**: TODO
**Complexity**: High
**File**: `TraitPassiveListener.java`

**Description**: When crafting armor or tools, 20% of materials are refunded.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onSmithCraft(CraftItemEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) return;

    Player player = (Player) event.getWhoClicked();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.SMITH, TraitTier.MASTER)) return;

    ItemStack result = event.getRecipe().getResult();

    // Check if result is gear (armor, tools, weapons)
    if (!isGear(result.getType())) return;

    // Calculate 20% of materials to refund
    CraftingInventory craftingInv = event.getInventory();
    ItemStack[] matrix = craftingInv.getMatrix();

    Map<Material, Integer> materialsUsed = new HashMap<>();
    for (ItemStack item : matrix) {
        if (item != null && item.getType() != Material.AIR) {
            materialsUsed.merge(item.getType(), item.getAmount(), Integer::sum);
        }
    }

    // Refund 20% (rounded down) after crafting completes
    Bukkit.getScheduler().runTask(plugin, () -> {
        materialsUsed.forEach((material, amount) -> {
            int refundAmount = (int) Math.floor(amount * 0.20);
            if (refundAmount > 0) {
                player.getInventory().addItem(new ItemStack(material, refundAmount));
            }
        });
        player.sendMessage("§a§lSmith Master: §7Refunded 20% of materials!");
    });
}

private boolean isGear(Material type) {
    String name = type.name();
    return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") ||
           name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") ||
           name.endsWith("_SWORD") || name.endsWith("_AXE") ||
           name.endsWith("_PICKAXE") || name.endsWith("_SHOVEL") ||
           name.endsWith("_HOE");
}
```

**Testing**:
1. Craft diamond chestplate (8 diamonds)
2. Verify 1-2 diamonds are refunded
3. Test with various gear types

---

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

---

### SCOUT MASTER: Sneak Speed = Walk Speed

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitPassiveListener.java`

**Description**: SCOUT MASTER players move at normal walk speed while sneaking.

**Implementation Approach (Attribute Modification)**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onScoutSneak(PlayerToggleSneakEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    if (!hasActiveTrait(uuid, PersonalityTrait.SCOUT, TraitTier.MASTER)) return;

    AttributeInstance sneakSpeed = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
    if (sneakSpeed == null) return;

    if (event.isSneaking()) {
        // Increase movement speed to compensate for sneak penalty
        sneakSpeed.addModifier(new AttributeModifier(
            UUID.nameUUIDFromBytes("scout_sneak_speed".getBytes()),
            "scout_sneak_speed",
            0.05, // Vanilla sneak is 30% slower, so add 5% to compensate
            AttributeModifier.Operation.ADD_NUMBER
        ));
    } else {
        // Remove modifier when not sneaking
        sneakSpeed.getModifiers().stream()
            .filter(mod -> mod.getName().equals("scout_sneak_speed"))
            .forEach(sneakSpeed::removeModifier);
    }
}
```

**Alternative Approach**: Use Speed II while sneaking to counteract the slowness.

**Testing**:
1. Sneak as SCOUT MASTER
2. Compare movement speed to normal walk speed
3. Verify they're roughly equal

---

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

---

### TAMER MASTER: Tamed Mobs +30% Max HP

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitPassiveListener.java`

**Description**: When TAMER MASTER tames a mob, it gains +30% max HP.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onTamerTamedHPBoost(EntityTameEvent event) {
    if (event.isCancelled()) return;
    if (!(event.getOwner() instanceof Player)) return;

    Player player = (Player) event.getOwner();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.TAMER, TraitTier.MASTER)) return;

    LivingEntity tamed = event.getEntity();
    AttributeInstance maxHealth = tamed.getAttribute(Attribute.GENERIC_MAX_HEALTH);

    if (maxHealth != null) {
        double currentMax = maxHealth.getBaseValue();
        double newMax = currentMax * 1.30;

        maxHealth.setBaseValue(newMax);
        tamed.setHealth(newMax); // Heal to full with new max HP

        player.sendMessage("§a§lTamer Master: §7Your companion is stronger! (+" +
            String.format("%.1f", newMax - currentMax) + " HP)");
    }
}
```

**Testing**:
1. Tame wolf as TAMER MASTER
2. Check wolf's max HP (should be 20 * 1.3 = 26)
3. Verify HP boost persists after server restart

---

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

---

### RUNEKEEPER MASTER: Enchanting Without Lapis

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitPassiveListener.java`

**Description**: RUNEKEEPER MASTER doesn't need lapis lazuli for enchanting.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onRunekeeperEnchant(EnchantItemEvent event) {
    Player player = event.getEnchanter();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.RUNEKEEPER, TraitTier.MASTER)) return;

    // Refund lapis cost
    int lapisCost = event.getExpLevelCost();
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        player.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, lapisCost));
    }, 1L);

    player.sendMessage("§a§lRunekeeper Master: §7No lapis required!");
}
```

**Alternative**: Prevent lapis consumption entirely via PrepareItemEnchantEvent.

**Testing**:
1. Place item in enchanting table
2. Enchant without lapis in inventory
3. Verify enchantment succeeds
4. Or: Verify lapis is refunded after enchanting

---

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

---

### ILLUSIONIST MASTER: Invisibility Potions 3× Duration

**Current Status**: TODO
**Complexity**: Low
**File**: `TraitPassiveListener.java`

**Description**: When ILLUSIONIST MASTER drinks invisibility potion, duration is tripled.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.HIGH)
public void onIllusionistInvisibility(PlayerItemConsumeEvent event) {
    if (event.getItem().getType() != Material.POTION) return;

    Player player = event.getPlayer();
    if (!hasActiveTrait(player.getUniqueId(), PersonalityTrait.ILLUSIONIST, TraitTier.MASTER)) return;

    PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
    if (meta == null || !meta.hasCustomEffects()) return;

    // Check if potion has invisibility effect
    boolean hasInvisibility = meta.getCustomEffects().stream()
        .anyMatch(effect -> effect.getType() == PotionEffectType.INVISIBILITY);

    if (hasInvisibility) {
        // Triple invisibility duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PotionEffect existing = player.getPotionEffect(PotionEffectType.INVISIBILITY);
            if (existing != null) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    existing.getDuration() * 3,
                    existing.getAmplifier(),
                    existing.isAmbient(),
                    existing.hasParticles(),
                    existing.hasIcon()
                ));

                player.sendMessage("§a§lIllusionist Master: §7Invisibility extended 3×!");
            }
        }, 1L);
    }
}
```

**Testing**:
1. Drink Invisibility potion (3:00 vanilla)
2. Verify duration becomes 9:00
3. Test with different potion tiers (I, II)

---

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

---

### STARFALL: Additional Arrow Projectiles

**Current Status**: TODO
**Complexity**: Medium
**File**: `HolyEnchantEffectListener.java`

**Description**: Arrows with STARFALL rain down 2 additional projectiles on impact.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.NORMAL)
public void onStarfallImpact(ProjectileHitEvent event) {
    if (!(event.getEntity() instanceof Arrow)) return;

    Arrow arrow = (Arrow) event.getEntity();
    if (!(arrow.getShooter() instanceof Player)) return;

    Player player = (Player) arrow.getShooter();
    ItemStack bow = player.getInventory().getItemInMainHand();

    if (!hasHolyEnchant(bow, HolyEnchant.STARFALL)) return;

    Location impact = arrow.getLocation();

    // Spawn 2 additional arrows from above
    for (int i = 0; i < 2; i++) {
        Location spawnLoc = impact.clone().add(
            (Math.random() - 0.5) * 3, // Random X offset
            15,                         // 15 blocks above
            (Math.random() - 0.5) * 3  // Random Z offset
        );

        Arrow extraArrow = impact.getWorld().spawnArrow(
            spawnLoc,
            new Vector(0, -1, 0), // Straight down
            1.0f,                 // Velocity
            0.1f                  // Spread
        );

        extraArrow.setShooter(player);
        extraArrow.setDamage(arrow.getDamage() * 0.5); // Half damage
        extraArrow.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY);
    }
}
```

**Testing**:
1. Shoot arrow with STARFALL enchant
2. Verify 2 additional arrows fall from sky on impact
3. Test damage is applied correctly

---

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

---

### ECHO_STEP: Afterimage Entities

**Current Status**: TODO
**Complexity**: Very High
**File**: `HolyEnchantEffectListener.java`

**Description**: Players wearing ECHO_STEP boots leave behind afterimages (fake entities) that confuse enemies.

**Implementation Approach**:

```java
private final Map<UUID, Long> lastEchoStep = new HashMap<>();

@EventHandler(priority = EventPriority.MONITOR)
public void onEchoStepMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

    Player player = event.getPlayer();
    ItemStack boots = player.getInventory().getBoots();

    if (boots == null || !hasHolyEnchant(boots, HolyEnchant.ECHO_STEP)) return;

    UUID uuid = player.getUniqueId();
    long now = System.currentTimeMillis();

    // Spawn echo every 2 seconds while moving
    if (!lastEchoStep.containsKey(uuid) || now - lastEchoStep.get(uuid) > 2000) {
        spawnEcho(player);
        lastEchoStep.put(uuid, now);
    }
}

private void spawnEcho(Player player) {
    Location loc = player.getLocation();
    ArmorStand echo = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

    // Copy player appearance
    echo.setCustomName("§7" + player.getName());
    echo.setCustomNameVisible(false);
    echo.setGravity(false);
    echo.setVisible(false);
    echo.setMarker(true);

    // Copy armor
    echo.getEquipment().setHelmet(player.getInventory().getHelmet());
    echo.getEquipment().setChestplate(player.getInventory().getChestplate());
    echo.getEquipment().setLeggings(player.getInventory().getLeggings());
    echo.getEquipment().setBoots(player.getInventory().getBoots());
    echo.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand());

    // Make nearby mobs target the echo
    echo.getNearbyEntities(10, 10, 10).stream()
        .filter(e -> e instanceof Monster)
        .forEach(mob -> ((Monster) mob).setTarget(echo));

    // Fade and remove after 3 seconds
    new BukkitRunnable() {
        int ticks = 0;

        @Override
        public void run() {
            if (ticks++ > 60) { // 3 seconds
                echo.remove();
                cancel();
                return;
            }

            // Visual fade effect (particles)
            echo.getWorld().spawnParticle(
                Particle.SMOKE_NORMAL,
                echo.getLocation().add(0, 1, 0),
                5,
                0.2, 0.5, 0.2,
                0.01
            );
        }
    }.runTaskTimer(plugin, 0L, 1L);
}
```

**Testing**:
1. Wear boots with ECHO_STEP
2. Walk around
3. Verify afterimages spawn behind player every 2 seconds
4. Verify mobs attack afterimages
5. Verify afterimages fade after 3 seconds

---

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

---

### Ranger's Quiver: Dynamic Infinity Enchant

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitItemListener.java`

**Description**: RANGER Ultimate item. Holding any bow while wearing/holding the quiver grants Infinity.

**Implementation Approach**:

```java
@EventHandler(priority = EventPriority.MONITOR)
public void onRangersQuiverHold(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();

    // Check if player has Ranger's Quiver (INFINITY_LINK trait enchant)
    ItemStack quiver = findRangersQuiver(player);
    if (quiver == null) return;

    // Check if switching to a bow
    ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
    if (newItem != null && (newItem.getType() == Material.BOW || newItem.getType() == Material.CROSSBOW)) {
        // Add Infinity temporarily
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemMeta meta = newItem.getItemMeta();
            if (meta != null && !meta.hasEnchant(Enchantment.INFINITY)) {
                meta.addEnchant(Enchantment.INFINITY, 1, true);
                // Store in PDC that this is temporary
                meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "temp_infinity"),
                    PersistentDataType.BYTE,
                    (byte) 1
                );
                newItem.setItemMeta(meta);
            }
        }, 1L);
    }

    // Remove Infinity from previous item if it was temporary
    ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
    if (oldItem != null && oldItem.hasItemMeta()) {
        ItemMeta meta = oldItem.getItemMeta();
        if (meta.getPersistentDataContainer().has(
            new NamespacedKey(plugin, "temp_infinity"),
            PersistentDataType.BYTE
        )) {
            meta.removeEnchant(Enchantment.INFINITY);
            meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "temp_infinity"));
            oldItem.setItemMeta(meta);
        }
    }
}

private ItemStack findRangersQuiver(Player player) {
    // Check all inventory slots for item with INFINITY_LINK trait enchant
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null) {
            String traitEnchant = holyEnchantService.getTraitEnchant(item);
            if ("INFINITY_LINK".equals(traitEnchant)) {
                return item;
            }
        }
    }
    return null;
}
```

**Testing**:
1. Obtain Ranger's Quiver
2. Hold any bow
3. Verify Infinity enchant appears
4. Switch to sword - verify Infinity disappears
5. Shoot arrows - verify no arrows consumed

---

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

---

### Ancient Whistle: Summon Persistent Wolf

**Current Status**: TODO
**Complexity**: High
**File**: `TraitItemListener.java`

**Description**: TAMER Ultimate item. Summons a wolf companion that persists until death.

**Implementation Approach**:

```java
private final Map<UUID, Wolf> summonedWolves = new HashMap<>();

@EventHandler(priority = EventPriority.NORMAL)
public void onAncientWhistleUse(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

    Player player = event.getPlayer();
    ItemStack whistle = event.getItem();

    if (whistle == null) return;

    // Check for BEAST_MASTER trait enchant (Ancient Whistle)
    String traitEnchant = holyEnchantService.getTraitEnchant(whistle);
    if (traitEnchant == null || !traitEnchant.equals("BEAST_MASTER")) return;

    UUID uuid = player.getUniqueId();

    // Check if wolf already exists
    if (summonedWolves.containsKey(uuid)) {
        Wolf existingWolf = summonedWolves.get(uuid);
        if (existingWolf != null && !existingWolf.isDead()) {
            player.sendMessage("§cYour wolf companion is already summoned!");
            return;
        }
    }

    // Summon wolf
    Wolf wolf = (Wolf) player.getWorld().spawnEntity(
        player.getLocation().add(player.getLocation().getDirection().multiply(2)),
        EntityType.WOLF
    );

    wolf.setOwner(player);
    wolf.setTamed(true);
    wolf.setCustomName("§6" + player.getName() + "'s Companion");
    wolf.setCustomNameVisible(true);

    // Enhance wolf stats
    wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0); // 15 hearts
    wolf.setHealth(30.0);
    wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(6.0); // 3 hearts

    summonedWolves.put(uuid, wolf);

    player.sendMessage("§a§lAncient Whistle: §7Your companion has been summoned!");
    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
}

// Clean up on wolf death
@EventHandler
public void onWolfDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Wolf)) return;

    Wolf wolf = (Wolf) event.getEntity();
    summonedWolves.entrySet().stream()
        .filter(entry -> entry.getValue() == wolf)
        .findFirst()
        .ifPresent(entry -> {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null) {
                owner.sendMessage("§c§lAncient Whistle: §7Your companion has fallen...");
            }
            summonedWolves.remove(entry.getKey());
        });
}
```

**Testing**:
1. Right-click with Ancient Whistle
2. Verify wolf spawns and is tamed
3. Verify wolf has 30 HP and 6 attack damage
4. Kill wolf - verify death message
5. Try summoning again - verify new wolf spawns

---

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

---

### Staff of the Covenant: Alignment-Based AoE Effects

**Current Status**: TODO
**Complexity**: Medium
**File**: `TraitItemListener.java`

**Description**: HEALER Ultimate item. Right-click to apply effects based on alignment. Good: AoE Regeneration. Evil: AoE Wither.

**Implementation Approach**:

```java
private final Map<UUID, Long> staffCooldowns = new HashMap<>();
private static final long STAFF_COOLDOWN_MS = 15_000; // 15 seconds

@EventHandler(priority = EventPriority.NORMAL)
public void onStaffOfCovenantUse(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

    Player player = event.getPlayer();
    ItemStack staff = event.getItem();

    if (staff == null || staff.getType() != Material.TRIDENT) return;

    // Check for SANCTIFY trait enchant (Staff of the Covenant)
    String traitEnchant = holyEnchantService.getTraitEnchant(staff);
    if (traitEnchant == null || !traitEnchant.equals("SANCTIFY")) return;

    UUID uuid = player.getUniqueId();

    // Check cooldown
    if (staffCooldowns.containsKey(uuid)) {
        long lastUse = staffCooldowns.get(uuid);
        long remaining = (lastUse + STAFF_COOLDOWN_MS) - System.currentTimeMillis();
        if (remaining > 0) {
            player.sendMessage("§cStaff on cooldown! " + (remaining / 1000) + "s remaining");
            return;
        }
    }

    // Check alignment
    int honor = reputationService.getHonor(uuid);
    boolean isGood = honor >= 0;

    if (isGood) {
        // AoE Regeneration to nearby players
        player.getNearbyEntities(10, 10, 10).stream()
            .filter(e -> e instanceof Player)
            .map(e -> (Player) e)
            .forEach(p -> {
                p.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    200, // 10 seconds
                    1,   // Regen II
                    false, true, true
                ));
                p.sendMessage("§a" + player.getName() + " blessed you with Regeneration!");
            });

        player.sendMessage("§a§lStaff of the Covenant: §7Blessing cast!");
    } else {
        // AoE Wither to nearby hostile mobs and players
        player.getNearbyEntities(10, 10, 10).stream()
            .filter(e -> e instanceof LivingEntity)
            .filter(e -> !(e instanceof Player) || ((Player) e).getGameMode() == GameMode.SURVIVAL)
            .map(e -> (LivingEntity) e)
            .forEach(entity -> {
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.WITHER,
                    200, // 10 seconds
                    1,   // Wither II
                    false, true, true
                ));
            });

        player.sendMessage("§c§lStaff of the Covenant: §7Curse cast!");
    }

    // Visual effect
    player.getWorld().spawnParticle(
        isGood ? Particle.HEART : Particle.SMOKE_LARGE,
        player.getLocation().add(0, 1, 0),
        50,
        5, 2, 5,
        0.1
    );

    staffCooldowns.put(uuid, System.currentTimeMillis());
}
```

**Dependencies**: ReputationService (for alignment check)

**Testing**:
1. Right-click with Staff of the Covenant (Good alignment)
2. Verify nearby players get Regeneration II
3. Switch to Evil alignment
4. Verify nearby entities get Wither II
5. Test 15-second cooldown

---

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

---

## General Implementation Patterns

### Cooldown Tracking

Most advanced mechanics require cooldown tracking. Use this pattern:

```java
private final Map<UUID, Long> cooldowns = new HashMap<>();
private static final long COOLDOWN_MS = 60_000; // 1 minute

private boolean isOnCooldown(UUID uuid) {
    if (!cooldowns.containsKey(uuid)) return false;

    long lastUse = cooldowns.get(uuid);
    long remaining = (lastUse + COOLDOWN_MS) - System.currentTimeMillis();

    return remaining > 0;
}

private void setCooldown(UUID uuid) {
    cooldowns.put(uuid, System.currentTimeMillis());
}

private long getRemainingCooldown(UUID uuid) {
    if (!cooldowns.containsKey(uuid)) return 0;

    long lastUse = cooldowns.get(uuid);
    long remaining = (lastUse + COOLDOWN_MS) - System.currentTimeMillis();

    return Math.max(0, remaining);
}
```

For **persistent cooldowns** (survive server restart), store in database:

```java
// TraitRepository.java
public CompletableFuture<Long> getAbilityCooldown(UUID uuid, String abilityName) {
    return sql.query(DATASOURCE,
        "SELECT last_used FROM player_ability_cooldowns WHERE player_uuid = ? AND ability_name = ?",
        rs -> rs.next() ? rs.getLong("last_used") : 0L,
        uuid.toString(), abilityName
    );
}

public CompletableFuture<Void> setAbilityCooldown(UUID uuid, String abilityName) {
    String upsert = mysql
        ? "INSERT INTO player_ability_cooldowns (player_uuid, ability_name, last_used) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE last_used = VALUES(last_used)"
        : "MERGE INTO player_ability_cooldowns (player_uuid, ability_name, last_used) KEY(player_uuid, ability_name) VALUES (?, ?, ?)";

    return sql.update(DATASOURCE, upsert,
        uuid.toString(),
        abilityName,
        System.currentTimeMillis()
    ).thenRun(() -> {});
}
```

---

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

---

### Attribute Modification

For permanent stat changes (HP, speed, armor, etc.):

```java
private void applyAttributeModifier(LivingEntity entity, Attribute attribute, String name, double amount) {
    AttributeInstance instance = entity.getAttribute(attribute);
    if (instance == null) return;

    // Remove existing modifier with same name
    instance.getModifiers().stream()
        .filter(mod -> mod.getName().equals(name))
        .forEach(instance::removeModifier);

    // Add new modifier
    AttributeModifier modifier = new AttributeModifier(
        UUID.nameUUIDFromBytes(name.getBytes()),
        name,
        amount,
        AttributeModifier.Operation.ADD_NUMBER
    );

    instance.addModifier(modifier);
}
```

**Common Attributes**:
- `GENERIC_MAX_HEALTH` - Max HP
- `GENERIC_MOVEMENT_SPEED` - Walk speed
- `GENERIC_ATTACK_DAMAGE` - Melee damage
- `GENERIC_ARMOR` - Armor points
- `GENERIC_ATTACK_SPEED` - Attack speed

---

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

---

### ProtocolLib Integration

For packet manipulation (shield cooldowns, glowing effects, etc.):

**Add Dependency** (`build.gradle`):
```gradle
dependencies {
    compileOnly 'com.comphenix.protocol:ProtocolLib:5.1.0'
}
```

**Usage Example**:
```java
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.PacketType;

ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

// Create packet
PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_COOLDOWN);
packet.getIntegers().write(0, Material.SHIELD.getId());
packet.getIntegers().write(1, 50); // Cooldown ticks

// Send to player
protocolManager.sendServerPacket(player, packet);
```

**Common Packet Types**:
- `SET_COOLDOWN` - Item cooldowns
- `ENTITY_METADATA` - Entity glowing, invisibility
- `SPAWN_ENTITY` - Fake entities (decoys)

---

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

---

## Testing Framework

Create test command for admins:

```java
/traittest <mechanic> - Test a specific mechanic
/traittest cooldown <ability> <player> - Check cooldown status
/traittest trigger <ability> <player> - Force trigger an ability
```

This allows rapid iteration without requiring full gameplay scenarios.

---

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
