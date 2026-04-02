---
title: Phase 1 Implementation Plan
description: Phase 1 in-game setup and implementation notes.
tags:
  - archive
  - roadmap
status: archive
phase: archive
owner: admin
action: none
---
# Phase 1 Implementation Plan — Working Core
**Created:** 2026-03-24
**Goal:** Fully working Phase 1 core — spawn village functional, story gates wired, NPCs talking, intro lore plays


## What Needs to Be Built

### BLOCK A — Story Gate Listeners (Java, ~3 files)
Small code additions that wire vanilla events into the progression system.

#### A1. `EnderDragonKillListener.java`
- Listen: `EnderDragonChangePhaseEvent` (death phase) or `EntityDeathEvent` (EntityType.ENDER_DRAGON)
- Track: which player/party/clan landed the kill
- Unlock: `AchievementKey.MILESTONE_ENDER_DRAGON` for all involved players
- Set: a server flag `redeemer_pending = true` so The Redeemer spawns next Easter
- File: `PluginV2/.../progression/EnderDragonKillListener.java`

#### A2. `WitherKillListener.java`
- Listen: `EntityDeathEvent` (EntityType.WITHER) — skip Devoiders (check PDC)
- Track: per-clan kill counter (new DB column or in-memory with persist on shutdown)
- At 36 kills: unlock `AchievementKey.MILESTONE_WITHER_36` for all clan members
- Broadcast a server-wide message when a clan hits 36
- File: `PluginV2/.../progression/WitherKillListener.java`

#### A3. Corrupt Portal Gate Check (1–2 lines)
- File: existing `PortalInteractListener.java`
- Add: check `progressionService.hasUnlocked(uuid, MILESTONE_ENDER_DRAGON)` before activating portal
- Message: "The portal hums but will not open. Something is missing..."


### BLOCK C — NPC Quest Scripts (BetonQuest YAML, ~5 files)
No Java needed. These are `.yml` files placed in `BetonQuest/QuestPackages/` on the Survival server.

#### C1. Village Elder — Tutorial Chain
4 quests:
1. Talk to the Elder → get starter kit
2. Craft a bed + build a shelter → reward: 50 XP, +10 Wildlands rep
3. Kill 10 hostile mobs → reward: +20 Wildlands rep
4. Enter the Nether (milestone: `nether_portal` unlocked) → reward: Elder's Blessing item, trait XP

#### C2. Shrine Keeper — Zodiac Intro
1 quest/dialogue:
- Shows player their zodiac sign
- 13 dialogue variants (one per sign)
- Gives a small zodiac-sign-specific buff item

#### C3. Blacksmith — Gear Chain (basic)
3 quests:
1. Bring 32 iron ingots → reward: iron gear set (trait-appropriate)
2. Craft a diamond sword/bow/staff (class-dependent) → reward: class enchant
3. Kill 5 mobs with crafted gear → reward: AuraSkills XP grant

#### C4. Herbalist — Alchemy Chain (basic)
2 quests (expandable):
1. Bring 5 mushrooms + 5 wheat → reward: 3× healing potions
2. Brew a Potion of Strength → reward: unique recipe unlock, +10 Wildlands rep

#### C5. Professor Craft — First Contact
1 quest (entry quest, rest of chain Phase 2):
- Trigger: player enters volcano biome near his lab (distance condition)
- Quest: "Find my lab and speak to me" → player finds the lab
- Reward: crafting blueprint book (display item), advancement `[The Scientific Method]`


### BLOCK E — Father of Ender (MythicMobs)
#### E1. MythicMobs mob definition
- File: `MythicMobs/Mobs/FatherOfEnder.yml`
- Based on Ender Dragon but buffed (3× HP, additional projectile phases)
- Spawns in the Amplified End dimension only
- Drops: unique item + Nether Star

#### E2. `FatherOfEnderKillListener.java`
- Listen: EntityDeathEvent for the MythicMob type `FatherOfEnder`
- Unlock: `AchievementKey.MILESTONE_FATHER_OF_ENDER`
- Set: server flag `heavens_gate_pending = true`


## Minimum Viable Phase 1 (What "Done" Looks Like)

- [ ] Player joins Survival → intro lore plays once
- [ ] Player talks to Village Elder → gets starter kit, 4-quest tutorial chain
- [ ] Player enters Nether → milestone unlocked, progression ticks to 10%
- [ ] Player kills Ender Dragon → milestone unlocked, progression to 20%, Redeemer flag set
- [ ] Player tries Corrupt Portal without dragon kill → blocked with message
- [ ] Clan hits 36 Withers → milestone unlocked, Devoid access flag set
- [ ] Shrine Keeper shows zodiac info
- [ ] Blacksmith + Herbalist have at least 2 quests each
- [ ] Professor Craft has First Contact quest active

---

## Decisions Made

1. **Wither unlock = 6 kills** — Phase 2 / Devoid access unlocks at 6 clan Wither kills. `milestones.md` (which said 36) is outdated and needs correcting.
2. **The Redeemer auto-spawns** — plugin spawns him via Citizens on the next Easter calendar event, only for players with positive Celestial rep (good path).
3. **Spawn Village** — being built in-game. Block D (NPC placement) happens after build is done.
4. **Economy** — scoped out for now. Merchant NPC gets placeholder dialogue only.
