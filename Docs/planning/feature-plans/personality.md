---
title: Personality Feature Plan
description: 13-trait personality system implementation plan.
tags:
  - planning
  - personality
status: implemented
phase: phase-1
owner: dev
action: none
---
# Plan: Personality Gameplay Implementation (Phase 4 & 5)

**Status:** ðŸ“‹ Proposed  
**Created:** 2026-03-19  
**Module:** `rpgcore.personality`


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


## Files to Create

1. `src/.../personality/listener/TraitEffectListener.java`
2. `src/.../personality/listener/ElementalPassiveListener.java`
3. `src/.../personality/listener/HolyEnchantEffectListener.java`
4. `src/.../personality/listener/TraitItemListener.java`
5. `src/.../personality/command/TraitCommand.java`
6. `src/.../personality/command/TraitTabCompleter.java`



## TONE CONTEXT
Write production-quality Java code. Follow the existing RpgModule pattern (interface + impl, ServiceRegistry, ModuleContext). Be consistent with existing codebase conventions. Minimal comments only where logic is non-obvious.
a

## DETAILED TASK DESCRIPTION & RULES

### System 1: Personality Quiz

The quiz runs in a **scripted sequence** immediately after the first-login cutscene. Player cannot move freely until quiz is complete. Three question types:

1. **Multiple choice** â€” Text-based lore questions (e.g. "You find treasure in the forest â€” do you keep it or share it with your clan?", "Do you prefer PvP or crafting?"). Answers score points toward each Trait category in a scoring matrix.

2. **Crafting challenge** â€” Player is given a specific set of items and a crafting table. Told to craft ONE item. The item crafted is recorded and scored toward traits (weapon â†’ Combat, tool â†’ Builder, food/potion â†’ Healer, etc.).

3. **Creative selection (final question)** â€” A GUI opens with all items available. Player picks one item. This is mapped by category to the **secondary personality** signal stored in `ZodiacProfile.personality`. Categories: COMBAT (weapons/armor), BUILDER (blocks/tools), NURTURER (food/potions), ARTIST (decorations/dyes), MYSTIC (enchanting/potions), EXPLORER (maps/compasses/boats).

Highest-scoring Trait = player's primary Trait. Ties broken by ThreadLocalRandom.

### System 2: Personality Traits & Tiers

Tiers advance automatically when the player's **story completion %** crosses thresholds:

| Tier | Threshold | Ability |
|------|-----------|---------|
| APPRENTICE | 0% (quiz assigned) | Title only, no abilities |
| TRAIT | 25% | Tier 1 passive |
| MASTER | 50% | Tier 2 passive |
| ULTIMATE | 100% | Tier 3 passive + Holy Enchant granted + Ultimate Trait Item given |i 

**Full Trait list with abilities:**
have 
**MAGE**
- Apprentice: No ability
- Mage: Splash & lingering potions +50% larger AoE (via PotionSplashEvent radius scaling)
- Master Mage: Potion stacking â€” same potion re-applied adds duration instead of replacing
- Ultimate Mage: Receives **Mage Bow** (Crossbow, all Vanilla enchants at max + Flame-like custom Trait Enchant). Can be **Blessed** by Lord: doubles all Vanilla enchant levels + adds random Holy Enchant.

**WARRIOR**
- Apprentice: No ability
- Warrior: +10% melee damage (EntityDamageByEntityEvent multiplier)
- Master Warrior: Shield cooldown reduced by 50%
- Ultimate Warrior: Receives **Warlord's Blade** (Sword, max enchants + custom Trait Enchant "Berserker" â€” damage scales with missing health). Blessable.

**ARCHER**
- Apprentice: No ability
- Archer: +15% bow/crossbow damage
- Master Archer: Arrows pierce 1 additional entity (Piercing I equivalent free)
- Ultimate Archer: Receives **Celestial Bow** (Bow, max enchants + custom "Homing" trait â€” arrows curve slightly toward last aimed target). Blessable.

**HEALER**
- Apprentice: No ability
- Healer (Good): AoE Regen I to nearby players every 15s (10 block radius). (Evil): AoE Wither I to nearby enemies every 15s.
- Master Healer: Potions thrown by this player last 50% longer on targets.
- Ultimate Healer: Receives **Staff of the Covenant** (custom item/trident, max enchants + "Sanctify" trait â€” on hit, cleanses negative effects from allies OR applies them to enemies). Blessable.

**RANGER**
- Apprentice: No ability
- Ranger: +20% speed in forested/jungle biomes
- Master Ranger: Tamed wolves/cats follow more aggressively, +15% tamed mob damage
- Ultimate Ranger: Receives **Ranger's Quiver** (custom item that auto-applies Infinity to any bow held). Blessable.

**ALCHEMIST**
- Apprentice: No ability
- Alchemist: Brewed potions have +25% duration
- Master Alchemist: Can brew 4 potions simultaneously (brewing stand override)
- Ultimate Alchemist: Receives **Flask of Eternity** (custom item, on use grants a random positive potion for 10 min, 30 min cooldown). Blessable.

**SMITH**
- Apprentice: No ability
- Smith: Anvil repair costs -2 levels
- Master Smith: Crafting recipes for gear use 20% fewer materials (recipe override)
- Ultimate Smith: Receives **The Eternal Hammer** (custom item, on use repairs held item by 50%, 5 min cooldown). Blessable.

**SCOUT**
- Apprentice: No ability
- Scout: +15% movement speed permanently
- Master Scout: Sneak speed is equal to walk speed (no slow)
- Ultimate Scout: Receives **Shadowstep Boots** (custom Boots, max enchants + "Phantom Step" trait â€” short-range blink on double-sneak, 10s cooldown). Blessable.

**BERSERKER**
- Apprentice: No ability
- Berserker: Damage taken below 30% HP triggers Strength I for 5s (10s cooldown)
- Master Berserker: +20% damage when not wearing chestplate
- Ultimate Berserker: Receives **Ragnarok Axe** (Axe, max enchants + "Bloodlust" trait â€” each kill refreshes a 3s Speed I burst). Blessable.

**SAGE**
- Apprentice: No ability
- Sage: +25% XP gain from all sources
- Master Sage: Enchanting table gives one extra enchant option
- Ultimate Sage: Receives **Tome of Infinite Wisdom** (custom Book item, on use grants 5 levels of XP, 30 min cooldown). Blessable.

**TAMER**
- Apprentice: No ability
- Tamer: +25% chance taming animals succeeds on first attempt
- Master Tamer: Tamed mobs gain +30% max HP
- Ultimate Tamer: Receives **Ancient Whistle** (custom item, summons a personal wolf companion that persists across sessions). Blessable.

**RUNEKEEPER**
- Apprentice: No ability
- Runekeeper: Enchanted items in hand glow with particle effects (cosmetic) + +10% enchant effectiveness
- Master Runekeeper: Enchanting no longer requires Lapis Lazuli
- Ultimate Runekeeper: Receives **Runeblade** (custom Sword, max enchants + "Runic Overload" trait â€” on critical hit, triggers a random enchant effect from the weapon again). Blessable.

**ILLUSIONIST**
- Apprentice: No ability
- Illusionist (Good): Can cast a harmless decoy (spawn a fake "ghost" version of self for 5s, 2 min cooldown). (Evil): Decoy attracts mob aggro.
- Master Illusionist: Invisibility potions last 3Ã— longer on this player
- Ultimate Illusionist: Receives **Mirror Shard** (custom item, on use briefly swaps position with target player, 5 min cooldown). Blessable.

> Total: 13 Traits to mirror the 13 Zodiac signs thematically. ~64 personality combinations emerge from quiz scoring weights across multiple traits.

### Holy Enchants

- One Holy Enchant is granted per Ultimate Trait (5 total if all 5 Traits are Ultimated).
- Holy Enchants are **custom enchantments** above vanilla max tier with unique effects.
- Each Holy Enchant is specific to a Trait's Ultimate item.
- Only **one of each Holy Enchant** per player â€” no duplicates.
- **Christmas drop**: On December 25 (real date), players with 300% completion receive **9 Holy Enchants** randomly selected from the full pool, excluding any already held.
- Holy Enchant pool examples: SOULFIRE, DIVINE_SHIELD, CELESTIAL_STRIKE, VOID_PIERCE, NATURES_GRASP, THUNDERCLAP, SERPENTS_FANG, LUNAR_BLESSING, STARFALL, ANCIENT_WARD, PHOENIX_FLAME, TITANIC_FORCE, ECHO_STEP.

### Blessing Mechanic

- Lord-rank player (`lw.rank.lord`) can **Bless** any player's Ultimate Trait item.
- Blessing: Doubles all Vanilla enchant levels (up to hard cap) + adds one random Holy Enchant from the pool (not already on the item).
- Tracked in `player_traits` table per item.

### System 3: Element Assignment & Reveal

Each player is silently assigned **one of 5 Elements** from quiz scores. The Element is **not revealed** until the Lord runs `/trait revealelement <player>`. Before reveal: Element has NO passive effect.

| Element | Passive (after Temple quest complete) |
|---------|--------------------------------------|
| FIRE | Immune to fire and lava damage |
| WIND | Elytra never loses durability |
| EARTH | Immune to suffocation damage |
| WATER | Permanent underwater breathing |
| AETHER | ~90% fall damage reduction |

Elements are only **activated** after:
1. All assigned Traits are ULTIMATE tier.
2. Lord has revealed the Element.
3. Corresponding Elemental Temple quest is complete (BetonQuest-driven).

### System 4: Elemental Journey Flow

After Lord's Plateau in the story, the Lord reveals Element 1. Player completes Temple 1 â†’ returns to Lord â†’ Lord reveals Element 2. Repeat for all 5.

After all 5 complete: Credits cutscene plays. Player is **post-game**:
- All 5 elemental passives active simultaneously. make
- Flag `is_postgame = true` stored in DB.
- EventServiceImpl checks this flag: post-game players get +50% drops/XP from world events.
- `/datejoined` shows "â­ Post-Game God" status.

### Trait Progression Tracking

Completion % comes from BetonQuest milestone tracking (existing `rpgcore.progression` module). The `TraitService` subscribes to completion % updates and auto-advances tiers.

Tier check method: `TraitServiceImpl.checkAndAdvanceTier(UUID)` â€” called:
- On PlayerJoinEvent (catch up any offline progression)
- When BetonQuest fires a milestone event (hook into existing milestone listener)


## IMMEDIATE TASK

Implement the full `rpgcore.personality` module. Build in this order:

1. **`PersonalityTrait.java`** enum â€” 13 values, each with tier ability descriptions, Ultimate item definition, Holy Enchant assignment.
2. **`Element.java`** enum â€” 5 values with passive description and unlock requirement.
3. **`TraitTier.java`** enum â€” APPRENTICE, TRAIT, MASTER, ULTIMATE with completion % thresholds.
4. **`HolyEnchant.java`** enum â€” full pool of ~13 Holy Enchants with name, effect description, applicable item types.
5. **`PlayerTraitProfile.java`** record â€” trait slots (up to 5), tiers per slot, active trait index, element slots (up to 5), element revealed/quest-complete flags, is_postgame flag, is_blessed per item.
6. **`TraitRepository.java`** â€” async DB CRUD for `player_traits`, `player_elements`, `player_quiz_answers` tables (H2/MySQL, follow existing repository pattern).
7. **`TraitService.java`** interface + **`TraitServiceImpl.java`** â€” assign on quiz, advance tiers, reveal element, reveal second trait (Lord only), grant Christmas enchants, check post-game status.
8. **`QuizSessionManager.java`** â€” in-memory state machine per UUID: tracks current question index, answers, scores. Questions defined as static list of `QuizQuestion` records.
9. **`QuizCompletionHandler.java`** â€” scores answers â†’ assigns Trait + Element â†’ calls TraitService â†’ notifies ZodiacService of secondary personality.
10. **`TraitEffectListener.java`** â€” Bukkit listeners applying all tier passive effects (PotionSplashEvent, EntityDamageByEntityEvent, PlayerMoveEvent, etc.). Check `traitService.getProfile(uuid)` for active trait and tier.
11. **`TraitCommand.java`** â€” `/trait info [player]`, `/trait set <trait>`, `/trait revealelement <player>` (Lord only), `/trait bless <player>` (Lord only), `/trait quiz` (admin restart quiz).
12. **`PersonalityModule.java`** â€” `RpgModule` wiring everything together, loads on survival-plugin.

Place all files in: `PluginV2/src/main/java/rpgcore/personality/`

Also extend:
- `SurvivalCalendarCommands` â†’ `/datejoined` shows active Trait, tier, Element status, completion %.
- `EventServiceImpl` â†’ check `traitService.isPostGame(uuid)` for +50% event rewards.


## OUTPUT FORMAT

Output each Java file completely with full package declarations and imports. After all files, provide:
1. A summary table of all files created and their purpose.
2. Any SQL migration scripts for the new tables.
3. Integration steps: what to add to the survival-plugin module loader list and any config keys to add to `core.yml`.
