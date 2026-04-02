---
title: World Spawn Flow
description: Player spawn and world entry flow design.
tags:
  - planning
  - worlds
  - spawn
status: planned
phase: phase-2
owner: design
action: needs-design
---
# Feature: World Spawn Flow — No Hub, Organic City Discovery
**Status:** Design Confirmed · **Created:** 2026-04-01


## Confirmed Decisions

| # | Question | Decision |
|---|----------|----------|
| 1 | City name | **Thornwell** |
| 2 | Class Hall | **In Lobby** — class selection already exists there. No physical Class Hall in the world. 13 Class Masters removed from scope entirely. |
| 3 | Navigation item | **Lodestone Compass** (Variant C) |
| 4 | The Redeemer at 0,0 | Spawns only when player meets requirements (Ender Dragon kill + Easter). Non-qualifying players see nothing at 0,0. |
| 5 | No teleportation | All 3 aids: Waypoints map + Compass upgrades + Dynmap/Bluemap |
| 6 | Prof. Kraft intro | Village Elder in Thornwell hands player off to Kraft at end of tutorial chain |
| 7 | Inn + RestfulSleepEvent | Yes — inn in Thornwell, beds trigger sleep buffs |
| 8 | Direction from 0,0 | Follow terrain — decided once seed is locked |


## Player-Facing First-Join Flow

```
1. Join lobby
2. Personality quiz → assigned trait
3. Class selection menu → class chosen
4. Enter Survival portal
5. Spawn at 0,0 → StoryIntroListener fires (title/subtitle lore hit)
6. See Amos at campfire → talk to him → receive Lodestone Compass
7. Follow compass ~800 blocks → Thornwell
8. Compass deactivates on arrival ("You found it.")
9. Village Elder opens Tutorial Chain
10. Elder eventually mentions Prof. Kraft → player heads to volcano biome
```


## The Hike — Environmental Content

800 blocks, ~3–5 minutes walking. Not empty:

| Distance from 0,0 | What's There |
|---|---|
| 0 | Amos + campfire + Lodestone Compass |
| ~150 blocks | Ruined farmhouse — looted, burned (El Diablo lore). Chest with 3 leather armor pieces: "Someone left this for survivors." |
| ~300 blocks | Road fragments begin — cobblestone path remnants heading toward Thornwell. Subtle breadcrumbing, not a full road. |
| ~400 blocks | Abandoned mine entrance — surface coal + iron visible. Optional: mob spawner + chest at bottom. |
| ~500 blocks | Hostile mob drops a lore note: *"They took the Thornwell road. Don't follow at night."* |
| ~700 blocks | Smoke particles + campfire sounds become audible from the city |
| ~800 blocks | Thornwell walls visible — city comes into view |


## Integration Points

| System | Impact |
|--------|--------|
| `StoryIntroListener` | Still fires on first Survival join at 0,0. Needs text update (no longer references "the village ahead" — references Thornwell + Amos). |
| `PlayerJoinListener` | No change needed. Compass given via Amos NPC interaction, not on join. |
| `PortalInteractListener` | Unchanged. Corrupt Portal still gated by `MILESTONE_ENDER_DRAGON`. |
| BetonQuest packages | `lw_elder`, `lw_blacksmith`, `lw_herbalist`, `lw_shrine`, `lw_professor` — content unchanged, NPC coords updated to Thornwell. |
| The Redeemer | Conditional spawn at 0,0. Citizens NPC with BetonQuest condition check on interaction. Non-qualifying players get no dialogue ("A figure stands silently. He doesn't seem to notice you.") or NPC simply doesn't spawn for them. |
| `ClassSelectionMenu` (lobby) | Already fully implemented. No changes. |
| RestfulSleepEvent | Tie to Inn beds in Thornwell. Triggers existing event when player sleeps in Inn. |
| Dynmap/Bluemap | Server-level install. Configure labeled markers for Thornwell and 0,0. |
| Waypoints map item | New feature — `WaypointMapService`, custom map item, discovery radius trigger. Future scope. |
| Compass tuning system | New feature — `CompassTuneService`. Future scope. |


## What This Removes from the Build Scope

This redesign eliminates the following previously-planned work:

| Item removed | Was | Now |
|---|---|---|
| Build Spawn Village at 0,0 | CRITICAL, Large | ❌ Removed entirely |
| Build Class Hall in world | CRITICAL, Large | ❌ Removed — class selection in lobby |
| Place 13 Class Masters in world | CRITICAL, Medium | ❌ Removed |
| Write Class Masters quest chains (13 × 5 tiers) | CRITICAL, Large | ❌ Removed from Phase 1 scope |

Replaced with:
| New item | Size |
|---|------|
| Build Thornwell (~8–12 buildings) | Medium (much smaller than Spawn Village) |
| Place 5 NPCs in Thornwell | Small |
| Place Amos at 0,0 | Small |
| Build hike landmarks (farmhouse, mine, road fragments) | Small |
| Configure Redeemer conditional spawn | Small |


## Open Questions

1. **Amos's name** — "Amos" is a placeholder. Fine or want something else?
2. **Trait progression (Class Masters)** — removed from Phase 1. Long-term: individual Class Masters scattered in the world (each in a biome/dungeon matching their trait)? Or handled differently?
3. **Waypoints Map implementation priority** — Phase 1 or later? Village Elder could give the blank map as part of his tutorial chain reward.
4. **Compass tuning** — Phase 1 or later? Could simplify Phase 1 by just giving a Thornwell compass and nothing else.
5. **Redeemer non-qualifying behaviour** — NPC doesn't spawn at all (cleaner) vs. spawns but is non-interactive (more atmospheric). Which?
