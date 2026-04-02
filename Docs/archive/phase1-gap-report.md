---
title: Phase 1 Gap Report
description: Phase 1 gap analysis and remaining items.
tags:
  - archive
  - roadmap
status: archive
phase: archive
owner: admin
action: none
---
# Phase 1 — Plugin Gap Report (Full & Corrected)
**Generated: 2026-03-20 | Based on full codebase + server stack audit**


## ✅ WHAT IS ALREADY COMPLETE

### Lobby (LW_Lobby_V2)
- Personality quiz on first join — chat-based, triggers after 5-tick delay ✅
- Quiz completion calculates winning trait + element, saves to DB ✅
- Element hidden until The Lord reveals it later in story ✅
- Survival portal gated — players cannot enter until quiz complete ✅
- Lobby protection and portal management ✅
- Player verification system ✅
- Citizens + BetonQuest + DecentHolograms installed and ready ✅

### RPG Core (RPG_Core_V2 — shared across all servers)
- Player profiles, join/quit persistence ✅
- Reputation system (5 factions, Honor score -1000 to +1000, titles) ✅
- Clan system (create, invite, war, alliance, head drops, colored prefix) ✅
- Party system (cross-server, friendly fire off, boss credit sharing) ✅
- Personality traits (13 traits, all passives, Holy Enchants, Ultimate Items) ✅
- Zodiac system (13 signs, 14 spirit animals, sync bonuses) ✅
- Calendar + events (Eclipse, Blizzard, Frost, Fog, Heatwave, Easter, etc.) ✅
- Class system (selection, skill tree, passives, AuraSkills bridge) ✅
- Portals (Crying Obsidian frames, cross-server transfer) ✅
- Boss system (Devoider + El Diablo transform, arena, boundary, drops) ✅
- Story milestone keys defined (nether_portal, ender_dragon, wither_36, etc.) ✅
- Progression service (unlock, counter, party-aware increments) ✅

### Survival (LW_Survival_V2)
- All personality trait listeners active ✅
- Elemental passives ✅
- Holy Enchant effects ✅
- Ultimate item mechanics ✅
- Scheduled tasks (LUNAR_BLESSING 5s, HEALER AoE 15s) ✅
- /trait command ✅
- Reputation events (kill players, mobs, villagers, cure zombie, trade, Wither Rose pledge) ✅
- Citizens + BetonQuest + Denizen + MythicMobsGUI installed and ready ✅


### 2. Citizens NPC Placement — NOT DONE
**Priority: CRITICAL | Effort: Medium (creative + config work)**

Citizens is installed but no NPCs are placed in any world. Required:
- Spawn Village NPCs at 0,0 (Village Elder, Blacksmith, Herbalist, Merchant, Shrine Keeper)
- 13 Class Masters in the Class Hall
- Professor Craft main lab + roaming placement
- The Redeemer — spawns dynamically after Ender Dragon kill on Easter


### 4. Ender Dragon Kill → Redeemer Trigger — NOT IMPLEMENTED
**Priority: HIGH | Effort: Small (~50 lines Java)**

No listener exists that:
- Detects Ender Dragon death and which player/clan killed it
- Unlocks `milestone:ender_dragon` for the player
- Sets a flag for The Redeemer to spawn at 0,0 on next Easter
- Connects to `EasterWeekHelper` to actually spawn the NPC

**Files needed:** `EnderDragonKillListener.java`, `StoryGateService.java`


### 6. Wither Kill Counter Per Clan — NOT IMPLEMENTED
**Priority: HIGH | Effort: Small (~80 lines Java)**

No listener exists to:
- Detect standard Wither kills (non-Devoider)
- Increment a per-clan kill counter
- Unlock The Devoid access when counter reaches 36

**File needed:** `WitherKillListener.java`


### 8. Intro Cutscene / Lore Delivery — NOT IMPLEMENTED
**Priority: MEDIUM | Effort: Small**

No intro lore plays on first join to Survival. Options:
- Title + subtitle sequence on first Survival join
- Custom `CinematicService` with timed title/chat/sound sequences
- Or: Denizen script on Survival (Denizen is already installed)


## 📊 COMPLETE STATUS TABLE

| Feature | Status | Server | Priority | Effort |
|---|---|---|---|---|
| Personality quiz (first join) | ✅ Complete | Lobby | — | — |
| Reputation system | ✅ Complete | All | — | — |
| Clan system | ✅ Complete | All | — | — |
| Party system | ✅ Complete | All | — | — |
| Personality traits + Holy Enchants | ✅ Complete | Survival/Amplified | — | — |
| Zodiac system | ✅ Complete | All | — | — |
| Calendar + events | ✅ Complete | All | — | — |
| Class system + AuraSkills | ✅ Complete | All | — | — |
| Portals (Crying Obsidian) | ✅ Complete | All | — | — |
| Boss system (Devoider + Diablo) | ✅ Partial | Survival | — | — |
| Story milestone keys defined | ✅ Complete | All | — | — |
| Citizens installed | ✅ Installed | All | — | Needs setup |
| BetonQuest installed | ✅ Installed | Lobby + Survival | — | Needs scripts |
| Denizen installed | ✅ Installed | Survival | — | Needs scripts |
| MythicMobsGUI installed | ✅ Installed | Survival | — | Needs setup |
| DecentHolograms installed | ✅ Installed | Lobby | — | Needs setup |
| BetonQuest quest scripts | ❌ Not written | Survival | CRITICAL | Large |
| Citizens NPC placement | ❌ Not done | Survival | CRITICAL | Medium |
| Spawn Village + Class Hall builds | ❌ Not built | Survival | CRITICAL | Large |
| Ender Dragon kill → trigger | ❌ Missing | Survival | HIGH | Small |
| Father of Ender boss | ❌ Missing | Amplified | HIGH | Medium |
| Wither kill counter (clan) | ❌ Missing | Survival | HIGH | Small |
| Corrupt Portal gate check | ❌ Missing | Survival | HIGH | Very small |
| Intro cutscene / lore | ❌ Missing | Survival | MEDIUM | Small |
| Economy module | ❌ Not started | All | LOW | Large |

---

## 🎯 RECOMMENDED BUILD ORDER

1. **Spawn Village + Class Hall world builds** — everything NPCs depends on knowing the coords
2. **Citizens NPC placement** — place NPCs in the built structures
3. **BetonQuest/Denizen quest scripts** — write the actual quest content per NPC
4. **Ender Dragon kill listener** — small Java, unlocks story progression
5. **Wither kill counter** — small Java, unlocks Devoid access
6. **Corrupt Portal gate check** — 1-2 line fix in existing code
7. **Father of Ender (MythicMobs)** — mob definition + kill listener
8. **Intro cutscene** — Denizen script, quick win
9. **Economy module** — last, after everything else is playable
