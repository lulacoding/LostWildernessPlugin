---
title: AeternumSeasons Features
description: AeternumSeasons seasonal weather, crops, and wildlife migration integration.
tags:
  - planning
  - events
  - seasons
status: implemented
phase: phase-1
owner: dev
action: none
---
# Plan: AeternumSeasons-style features in Lost Wilderness Events

> **Status Update (2026-03-19):** Most Aeternum features are now **IMPLEMENTED**. See status markers below.

This document plans how to adopt **AeternumSeasons** (3.9) patterns and features into **Lost Wilderness** PluginV2, under your name and using your existing event/calendar framework. Use it as a roadmap; implement in phases.

**Principles:**
- Keep LW naming and architecture (CalendarServiceV2, DailyWorldEvent, SeasonalEvent, EventServiceImpl, EventsModule).
- Reuse Aeternum's **design patterns** (biome backup before paint, PDC for mob restore, season-driven weather, etc.) rather than copying code verbatim.
- All new config under `config.yml` or `config/` with an `lw.` or `events.` prefix so it's clearly LW, not Aeternum.


## 2. Seasonal events (Blood Moon, Heat Wave, Cold Snap, Tornado, etc.) ✅ IMPLEMENTED

**Status:** ✅ All events implemented in PluginV2 under `events/impl/`:
- BloodMoonEvent, MagicStormEvent, TornadoEvent, FestivalEvent, FishingFestivalEvent, MiningBlessingEvent, RestfulSleepEvent, HolidayEvent, NewYearEvent

**Aeternum:** Single active seasonal event, calendar-driven (SeasonUpdateEvent), with one active + "queued tomorrow"; events use EventContext, onStart/onEnd/onTick.

**LW already has:** DailyWorldEvent loop, cooldowns, cascades, TPS guard, boss bar, Eclipse, Thunder, Frost, Heatwave, Blizzard, Fog, Paranoia, Seasonal Storm, Spring Bloom, Autumn Leaf Fall, Jungle Monsoon.

**LW plan - add Aeternum-style events using your framework:**

| Aeternum event | LW equivalent / action |
|----------------|-------------------------|
| **Blood Moon** | New **BloodMoonEvent** (DailyWorldEvent or SeasonalEvent): single night, no sleep, mobs buffed (PDC + tag), optional time freeze. Reuse your Eclipse night-freeze pattern; add mob buff/restore from phase3-events BloodMoonEvent pattern. |
| **Heat Wave** | You have **SummerHeatwaveEvent**. Optionally enrich with Aeternum's **shade check** (block Y vs highest block at X/Z) and **armor-piece count** for exposure damage/weakness. |
| **Cold Snap** | You have **FrostEvent**. Optionally add Aeternum's **WinterFreezeEvent** pattern: damage per tick when not near heat, configurable heat blocks and radius. |
| **Tornadoes** | New **TornadoEvent**: moving center, drift/chase, particles, pull/damage, crop break, configurable duration (e.g. 5-10 real minutes). Use world allow/block lists; respect indoor protection (e.g. roof check). See phase3-events TornadoEvent. |
| **Magical Storms** | New **MagicStormEvent**: storm + thunder, tag/PDC mobs, buff (glowing, speed/strength), on death extra enchanted loot; restore mobs on end or chunk load. |
| **Festivals** | New **SeasonFestivalEvent**: villagers/traders get PDC role, custom trades by day/season; remove PDC on end. |
| **Fishing Festival** | New **FishingFestivalEvent**: PlayerFishEvent, treasure chance, double catch; rod enchants scale. |
| **Miner's Blessing** | New **MiningBlessingEvent**: BlockBreakEvent on ores, extra drop, rare bonus, extra XP. |
| **Restorative Sleep** | New **RestfulSleepEvent**: on bed leave, temp health boost; on damage, strip buff if health low. |

Implement as **DailyWorldEvent** or **SeasonalEvent** depending on whether they are day-triggered (like Eclipse) or multi-day with duration (like Aeternum's seasonal events). Your EventServiceImpl already supports one active seasonal event + daily events; you can add more seasonal event types and register them.


## 4. Wildlife migration ✅ IMPLEMENTED

**Status:** ✅ Implemented as `WildlifeMigrationListener` in PluginV2. Uses `lw-fauna.yml` config with favored/discouraged entity types per season, boost chance, warm-in-winter culling, and particles.

**Aeternum:** Fauna/mob spawning or behavior by season (e.g. bees/sheep in spring, turtles/fish in summer, foxes in autumn, wolves/skeleton horses in winter).

**LW plan:**

| Item | Description |
|------|--------------|
| **Concept** | Animals "adapt" to season: spawn weights or allowed types per season; or message/particle when entering a "migration" zone. |
| **Implementation** | **CreatureSpawnEvent** listener: in overworld, filter or boost spawn chance by entity type and current season (from CalendarServiceV2). Optional: per-season "favored" and "discouraged" entity lists in config. |
| **Config** | e.g. `events.wildlife-migration.enabled`, `events.wildlife-migration.spring.favored`, `events.wildlife-migration.winter.favored`. |
| **Location** | e.g. `WildlifeMigrationListener` in events module. |


## 6. Seasonal guide ❌ NOT IMPLEMENTED

**Status:** ❌ Not yet implemented. `/season guide` command does not exist.

**Aeternum:** `/season guide` - in-game guide (book or menu), often translated via LanguageManager.

**LW plan:**

| Item | Description |
|------|--------------|
| **Command** | Add `/season guide` (or `/lwseason guide`) - opens a book or GUI with a short guide: seasons, events, crops, wildlife, weather. Reuse your existing command registration (e.g. Survival/Amplified wrapper). |
| **Content** | Static or config-driven pages (e.g. `guide.seasons`, `guide.events`, `guide.crops`). Optional: use your lang files if you have them, or keep strings in config. |
| **Location** | New command class in wrapper or core; book builder utility. |


## 8. Implementation order (suggested)

1. **Seasonal weather** - no world edit, low risk; reuses CalendarDayAdvancedEvent.
2. **Seasonal guide** - command + book; no gameplay logic.
3. **Wildlife migration** - one listener, config-driven; medium impact.
4. **Seasonal crops (simple)** - BlockGrowEvent / break bonuses by season.
5. **New events (Blood Moon, Tornado, Magic Storm, Festival, Fishing, Mining, Restful Sleep)** - one at a time, reusing EventServiceImpl and your cooldown/cascade system.
6. **Biome painting + backup** - design backup format and restore first; then add painting with "off" by default; add `/season biomes on|off|restore`.


## 10. References

- **AeternumSeasons source:** `AeternumSeasons-3.9.jar.src` (WinterWorldPainter, BiomeSpoofAdapter, SeasonalWeatherService, SeasonalFloraController, SeasonCommand, events in `events/`).
- **LW phase3-events:** `phase3-events.md` - Aeternum event class summaries and V2 checklist.
- **LW events gap:** `../gap-analysis.md` - what's already ported.

Use this plan to add features incrementally while keeping your naming, calendar, and event framework as the single source of truth.

---

## 11. Audit: Aeternum full source vs PluginV2 (post-full jar.src)

After filling in all previously INTERNAL ERROR files in `AeternumSeasons-3.9.jar.src`, this section compares each LW feature that was taken from or inspired by Aeternum. **Nothing requires a full redo.** A few optional enhancements are listed.

| Area | PluginV2 implementation | Aeternum (full source) | Verdict |
|------|--------------------------|-------------------------|--------|
| **Biome backup store** | `BiomeBackupStore`: YAML per chunk, world by name, `saveChunk(World,cx,cz)` from current chunk, restore on ChunkLoad queue, `getOriginalBiomeApproxOrNull`. | Binary `.bin`, world dir by UUID, `saveFirstTouch(Chunk, Biome[], stepXZ, stepY)` (saves pre-paint grid async), restore by iterating all backup files. | **No redo.** LW is chunk-load-driven and self-consistent. Optional: add `saveChunkFromGrid(World, cx, cz, Biome[] grid, int stepXZ, int stepY)` when you add painting, so the painter can save the grid *before* applying (same role as Aeternum's `saveFirstTouch`). |
| **Biome painting** | Only `paintingEnabled` flag and config; no painter. | `BiomeSpoofAdapter`: runnable every 10 ticks, player-centric radius, Family (LAND/OCEAN/RIVER), season→biome maps, capture-and-apply with 4×4 grid, disk backup via `saveFirstTouch`, revert on unload/season. | **New work, not redo.** When you implement painting, use full `BiomeSpoofAdapter` and `BiomeBackupStore` in jar.src as reference; add the "save from grid" API above if you keep YAML. |
| **BiomeBackupData / indexing** | Palette + indices, grid stepXZ/stepY, `indexAt` order (iy, iz, ix). | Same idea; capture order (x, z, y) → different index formula. | **No redo.** LW's order is consistent for its own save/load and `getOriginalBiomeApproxOrNull`. |
| **Summer heatwave** | `SummerHeatwaveEvent`: overworld summer, shade check (`isExposedToSky`), armor count, exposure damage, weakness, hunger, farmland dry, husks. | `HeatEnvironmentListener`: **aeternum_heat** dimension only; heat armor (PDC), 4 pieces = fire resist, else damage + weakness + slowness. | **No redo.** LW already has shade + armor; Aeternum heat is a separate dimension. |
| **Frost / cold** | `FrostEvent`: overworld winter, cold biomes, slowness/mining fatigue, **near-heat check** (torch/lava/campfire/etc. in radius), optional damage when exposed, armor count. | `FrostEnvironmentListener`: **aeternum_frost** only; outside check, armor, hot item in hand, near heat, freeze ticks, storm blindness + damage. | **No redo.** LW already has near-heat and optional damage; scope is overworld, not a frost dimension. |
| **Seasonal weather** | `SeasonalWeatherListener`: on `CalendarDayAdvancedEvent`, rainy days per season, thunder chance, storm/clear duration from lw-climate. | `SeasonalWeatherService`: rainy days (with overrides per season), thunder chance, duration, reseed rainy days each season, respect manual commands. | **No redo.** Optional: add "reseed rainy days each season" and "respect manual" if you want parity. |
| **Seasonal crops** | `SeasonalCropsListener`: BlockGrowEvent/BlockFertilizeEvent, allowed_seasons per crop, off_season_growth_chance (plan Option A). | `SeasonalFloraController` (spread/remove/restore, FloraRule) + `SeasonalCropConfig` (rain bonus, greenhouse). | **No redo.** Plan chose Option A first; Flora controller would be a later, optional step. |
| **Wildlife migration** | `WildlifeMigrationListener`: favored/discouraged by season, boost chance, lw-fauna. | Fauna/migration in Aeternum (e.g. AnimalMigrationService). | **No redo.** Design matches. |
| **Events (Blood Moon, Tornado, Magic Storm, etc.)** | Implemented as `DailyWorldEvent` / seasonal in LW with PDC, cooldowns, boss bar. | EventContext, onStart/onEnd/onTick, single active seasonal event. | **No redo.** LW uses its own framework; full Aeternum event source is useful for cross-checking behavior only. |
| **Config (lw-climate, lw-crops, lw-fauna, lw-events-extra)** | `LWConfigs` loads these; keys aligned for porting. | climate.yml, crops, fauna, YamlEvents. | **No redo.** |

**Summary:** No existing PluginV2 feature needs to be redone. When you add **biome painting**, implement a painter (using `BiomeSpoofAdapter` as reference) and add a "save from grid" API to `BiomeBackupStore` so the painter can save before applying. All other Aeternum-derived pieces are aligned with the plan and the full source.
