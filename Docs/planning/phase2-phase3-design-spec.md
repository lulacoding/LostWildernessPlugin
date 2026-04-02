---
title: Phase 2 & 3 Design Spec
description: Design specification for Phase 2 and Phase 3 development work.
tags:
  - planning
  - roadmap
status: planned
phase: phase-2
owner: dev
action: needs-dev
---
# Phase 2 & 3 — Design Spec
> Last Updated: 2026-04-02
> Phase 2 ends when: Clan kills all 6 Devoiders → 40% completion → El Diablo's Lair access unlocked
> Phase 3 ends when: El Diablo defeated → 50% completion → Aquaria revealed


## Phase 2: Into the Void

### Overview

After 36 Wither kills, the Devoid portal activates (crying obsidian frame on Survival server,
gated by `PortalInteractListener`). Players travel through the Survival server's Nether and
break through the bedrock ceiling to reach the Nether Roof.
The Nether Roof IS "The Devoid" — 6 Devoiders roam up here.

### The Devoiders

- 6 Devoiders exist on the Survival Nether Roof
- Spawned when 6 Withers are placed on the Nether Roof (`RoofWitherListener.java` — already implemented)
- Killing each Devoider increments `clan_devoider_kills` counter in DB
- On 6th kill: `MILESTONE_DEVOIDER_6` unlocks (40%), El Diablo Lair portal activates on Amplified
- Final Devoider death triggers dialogue: *"You will never guess where El Diablo is hiding."*

### Kraft's Role (Phase 2)

- After first Devoider kill, Kraft field note:
  *"Remarkable. These creatures are not Withers — they have been transformed. Something is drawing them to the roof. Follow the energy signature to the Amplified world."*
- After all 6 killed:
  *"Spawn a Wither on the Amplified Nether Roof. It will tear open the portal to the Lair."*

### World Build: Survival Nether Roof

- Seed a bedrock gap/passage so players can reach the roof
- Scatter Wither Skeleton skulls and Nether Brick debris to signal danger
- No major structures — Devoiders roam freely on the open roof

### Gate Mechanics

- **Entry gate**: `PortalInteractListener` already checks `MILESTONE_WITHER_36` — no new code
- **Progress tracking**: New `DevoiderKillListener.java` — increments per-clan counter, fires `MILESTONE_DEVOIDER_6` at 6
- **Exit gate**: Amplified Nether Roof crying obsidian frame gated by `MILESTONE_DEVOIDER_6`

### Completion Condition

Clan devoider kill count reaches 6 → `MILESTONE_DEVOIDER_6` set → 40% → Phase 3 unlocks.


## New Java Code Required

### Phase 2

| File | What it does | Size |
|------|-------------|------|
| `DevoiderKillListener.java` | Detects Devoider kill (MythicMobs entity tag), increments per-clan `clan_devoider_kills` in DB, fires `MILESTONE_DEVOIDER_6` at 6, dialogue on final kill | S |
| `ClanRepository.java` | Add `clan_devoider_kills` column + `incrementDevoiderKills` / `getDevoiderKills` | S |
| `AchievementKey.java` | Add `MILESTONE_DEVOIDER_6` | S |
| `PortalInteractListener.java` | Add Amplified Lair portal gate: requires `MILESTONE_DEVOIDER_6` | S |
| `StoryModule.java` | Register `DevoiderKillListener` | S |

### Phase 3

| File | What it does | Size |
|------|-------------|------|
| `AchievementKey.java` | Add `MILESTONE_EL_DIABLO`, `FLAG_EVIL_PATH` | S |
| `WitherRoseAltarListener.java` | Wither Rose on PDC-tagged altar → sets FLAG_EVIL_PATH, rep change, teleport stub | S |
| `ElDiabloKillListener.java` | El Diablo death → sets MILESTONE_EL_DIABLO, Gabriel title, Kraft note, Aether unlock | S |
| `StoryModule.java` | Register `ElDiabloKillListener`, `WitherRoseAltarListener` | S |
| `PortalInteractListener.java` | Add `PORTAL_TYPE_AETHER` (glowstone + water bucket) | S |
| MythicMobs `el_diablo.yml` | 3-phase El Diablo boss definition | M |
| MythicMobs `giant_wither_skeleton.yml` | Phase 2 miniboss (300 HP, cleave) | S |
| BetonQuest `lw_kraft_phase2.yml` | Kraft field notes for Phase 2 | S |
| BetonQuest `lw_kraft_phase3.yml` | Kraft field notes for Phase 3 + Gabriel dialogue | S |

---

## Open Questions (Parked for Later)

- **Evil path full detail** — Moon, Witch of Night Sky fight → Phase 4 design doc
- **Aquaria dimension** — full world build design → Phase 4 design doc
- **El Diablo respawn timer** — 7 days suggested, confirm with lula before implementing
- **Obsidian cage restore** — WorldEdit schematic paste on respawn (simplest approach, confirm before building)
- **Mixed clan blocking** — how are mixed Good/Evil clans blocked from completing story together? Needs enforcement design.
