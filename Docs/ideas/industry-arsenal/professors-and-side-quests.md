---
title: Professors & Side Quests
description: Professor NPCs delivering optional side quests and lore.
tags:
  - ideas
  - npc
  - quests
  - speculative
status: speculative
phase: post-story
owner: design
action: needs-design
---
# Lost Wilderness: Professors, NPCs & Side Quests

## Key NPC Characters

### Professor Kraft (Earth)
- **First met:** After the player's first Wither kill (Chapter 1).
- **Role:** Primary story guide for Chapters 1–6. Roams the Overworld between a network of custom labs.
- **Personality:** Brilliant, eccentric, genuinely excited by discovery. Gives tools without handholding.
- **Guides:** Chapter 1 (Amplified discovery), Chapter 2 (all 6 Withers + Devoiders), Chapter 4 (Elemental Temple intro + Chakra portal instructions).

### Larry (Kraft's Assistant)
- **Introduced:** Chapter 2, introduced by Kraft.
- **Role:** Purely fetch quest giver. Not interested in cosmic research — just wants stuff brought back.
- **Mechanic:** Hands the player maps to specific custom biomes/structures. Player brings back rare custom items found there.
- **Purpose:** Forces players to explore the world (Vanilla structures, custom biomes, etc.) between major story beats.

### Lt. Mason
- **Introduced:** Chapter 1 side quests (spawns after clearing a structure).
- **Role:** Military/exploration NPC. Gives quests that push players toward Vanilla structures they would otherwise miss.
- **Spawn condition:** Does NOT appear until the player has done the work. He steps out of a structure the player just cleared (Pillager Outpost, Jungle Temple, Ocean Monument, Trial Chamber, etc.). He is always waiting somewhere in the aftermath — not in a safe village.
- **Known spawn locations (one active per playthrough):**
  - Defeated Pillager Outpost → Mason steps out of the watchtower.
  - Looted Jungle Temple → Mason is waiting in the back chamber.
  - Cleared Trial Chamber → Mason emerges from a side room.
- **Quest style:** Exploration-focused. Directs players to other notable structures, Warden encounters, Ancient Cities, etc.

### Tracey
- **Introduced:** Chapter 2.
- **Role:** Pets system guide. Gives side quests related to taming, breeding, and pet care.

### Ricky
- **Introduced:** Chapter 2.
- **Role:** Personality/Skills guide. Gives side quests related to levelling up traits and discovering the AuraSkills system.

### The Monk (Kraft's contact)
- **Introduced:** Chapter 4, when Kraft teaches the player about Chakra portals.
- **Role:** Meets the player in the Root Chakra dimension and guides them through all 6 Chakra worlds.

### Professor Krystal (Myrikane/Andromeda)
- **First met:** On Myrikane, at the start of Chapter 7.
- **Role:** Andromeda campaign guide. Acts as mission control for the entire Zeratari System progression. Counterpart to Professor Kraft.


## Chapter 2 Side Quests

### The 6 Withers — Escalating Conditions

Kraft paces the Wither kills with side quests between each one. Each Wither has a unique mechanical requirement:

| # | Location | Condition |
|---|---|---|
| 1 | Normal Overworld (intro, no condition) | Standard summon/fight |
| 2 | Specific Biome (Kraft directs) | Must be defeated solo |
| 3 | A dimension other than the Normal Overworld | Wither summon in non-standard dimension |
| 4 | Normal Nether | Physical damage only (no projectiles, no magic) |
| 5 | Amplified Nether | Potion-only (no weapons) |
| 6 | Inside an Obsidian box | Fight contained in a sealed structure Kraft builds |

### Larry's Biome Fetches
Between Wither 2 and Wither 5, Larry gives the player maps to 3–4 distinct custom biomes and asks for specific items gathered there. No story significance — pure exploration reward.

### Tracey's Pet Quests
Between Wither 3 and Wither 5, Tracey appears and introduces the `/pets` system. Gives 2–3 quests:
- Tame your first animal companion.
- Bring your pet to The Lord to reveal its Zodiac and Personality.
- (Optional) Breed two pets and register the offspring.

### Ricky's Skills Quests
Between Wither 2 and Wither 4, Ricky introduces AuraSkills. Gives 2–3 quests:
- Level your first AuraSkill to a milestone.
- Unlock a Personality Trait.
- (Optional) Discover a hidden Personality combination.

### The Devoider Grind
After the 5th Wither, Kraft sends the player to the Nether Roof. The 6th Wither on the Normal Nether Roof mutates into **The Devoider**. Kraft tasks the player to farm 6 more Devoiders total.
- During this grind, additional Larry fetches and Tracey/Ricky side quests fill the time.
- The 6th and final Devoider taunts the player: *"You will never guess where El Diablo is hiding."*


## Chapter 4 Side Quests

*(Documented in the Elemental Temple and Chakra sections above — same file for both Good and Evil paths.)*


## Estimated Chapter Playtime (Solo Player, Casual Pace)

| Chapter | Content | Estimated Hours |
|---|---|---|
| Chapter 1 | Survival, Mother Ender, First Wither, Amplified world, Father of End | 20–40h |
| Chapter 2 | 6 Withers (escalating conditions), 6 Devoiders, El Diablo's Lair | 40–80h |
| Chapter 3 | Aether Portal, Aquaria hike, Gabriel Inquisition, Lord's Plateau | 5–10h |
| Chapter 4 | Mastery (Honor + Prestige + Personality), 5 Temples, 7 Chakra Dims | 60–120h |
| Chapter 5 | Venus, Mercury Core Bosses | 10–20h |
| Chapter 6 | Moon + 8 Solar System planets (surface + core each) | 80–150h |
| Chapter 7 | 9 Zeratari planets | 80–150h |
| Chapter 8 | Reality Checkerboard (fixed event) | 1–3h |
| **Total** | | **~300–575h** |

*Co-op parties will clear faster. The Honor grind (Chapter 4) is the single biggest time variable.*

---

## Solo & Co-Op Systems

### Solo Play
- **Dynamic Difficulty Engine:** All instanced boss fights scale to party size. A solo player vs. El Diablo gets reduced HP pool, fewer minions, lower damage.
- **NPC Allies:** Solo players receive 1 NPC Ally in Zeratari missions (Chapter 7). Scales to party size up to 8.
- **No Forced Grouping:** Every chapter is completable alone. Co-op is a choice, not a requirement.
- **/nomad System:** Solo players who haven't joined a Clan can register as a Nomad for access to Clan/War-adjacent mechanics at a lighter tier.

### Co-Op Play
- **Party System:** Up to 8 players share the same instanced story events.
- **Shared Progression:** Boss kills, portal construction, and quest completions count for all party members present.
- **PvPvE Convergence Points:** Multiple Core Bosses (Moon, Jupiter, Saturn, Uranus, The Edge of Sol) are PvPvE — Good and Evil players fight each other AND the boss simultaneously.
- **Mantle Clashes:** Jupiter and Uranus have a Mantle bottleneck — Good vs. Evil PvP before the Core. Only the winning faction proceeds first.
- **The Checkerboard (Chapter 8):** Dynamically fills to 8v8 using NPC allies if fewer human players are present.
