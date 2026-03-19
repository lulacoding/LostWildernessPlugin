# Plan: Personality Gameplay Implementation (Phase 4 & 5)

**Status:** 📋 Proposed  
**Created:** 2026-03-19  
**Module:** `rpgcore.personality`

---

## Overview

The core service layer for the Personality system is complete. This plan covers the implementation of the actual gameplay mechanics (passives, enchants, items) and the player-facing commands.

---

## Phase 4.1: Trait Passive Listeners

Implement `TraitEffectListener.java` to handle the 39 passive abilities.

### Combat Traits
- **Warrior:** `EntityDamageByEntityEvent` -> +10% damage.
- **Berserker:** `EntityDamageEvent` -> If health < 30%, grant Strength I.
- **Archer:** `EntityDamageByEntityEvent` -> +15% projectile damage.

### Utility Traits
- **Ranger:** `PlayerMoveEvent` -> Check biome; if forest/jungle, apply Speed I.
- **Sage:** `PlayerExpChangeEvent` -> Multiply XP by 1.25.
- **Scout:** `PlayerMoveEvent` -> Permanent Speed I; `PlayerToggleSneakEvent` -> Walk speed while sneaking.

### Magic & Support Traits
- **Mage:** `PotionSplashEvent` -> Increase radius; `EntityPotionEffectEvent` -> Stacking durations.
- **Healer:** `Scheduler` (15s) -> Check alignment; AoE Regen I (Good) or Wither I (Evil).
- **Alchemist:** `BrewEvent` -> Increase duration on result items.

---

## Phase 4.2: Elemental God-Tier Passives

Implement `ElementalPassiveListener.java`. These only activate after Temple completion.

- **FIRE:** `EntityDamageEvent` (FIRE/LAVA) -> Cancel.
- **EARTH:** `EntityDamageEvent` (SUFFOCATION) -> Cancel.
- **WIND:** `PlayerItemDamageEvent` (ELYTRA) -> Cancel.
- **WATER:** `PlayerMoveEvent` -> Permanent Water Breathing.
- **AETHER:** `EntityDamageEvent` (FALL) -> Reduce by 90%.

---

## Phase 4.3: Holy Enchants & Ultimate Items

### HolyEnchantEffectListener.java
- **SOULFIRE:** Damage bypasses armor.
- **DIVINE_SHIELD:** Absorption on block.
- **CELESTIAL_STRIKE:** Lightning on crit.
- **NATURES_GRASP:** Slowness/Root on hit.

### TraitItemListener.java
- **Ranger's Quiver:** `PlayerItemHeldEvent` -> Apply Infinity to bows.
- **Mirror Shard:** `PlayerInteractEvent` -> Swap positions with target.
- **Eternal Hammer:** `PlayerInteractEvent` -> Repair item in other hand.

---

## Phase 5: Commands

Implement `TraitCommand.java`.

- `/trait info [player]` — Show status GUI or text summary.
- `/trait revealelement <player>` — (Lord only) Reveal hidden element.
- `/trait bless <player>` — (Lord only) Bless held ultimate item.
- `/trait set <player> <trait>` — (Admin only) Force trait change.

---

## Files to Create

1. `src/.../personality/listener/TraitEffectListener.java`
2. `src/.../personality/listener/ElementalPassiveListener.java`
3. `src/.../personality/listener/HolyEnchantEffectListener.java`
4. `src/.../personality/listener/TraitItemListener.java`
5. `src/.../personality/command/TraitCommand.java`
6. `src/.../personality/command/TraitTabCompleter.java`
