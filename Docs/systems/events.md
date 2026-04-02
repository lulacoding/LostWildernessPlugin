---
title: Events
description: Daily and seasonal events module with 22 implemented events; Nether events pending.
tags:
  - system
  - events
status: partial
phase: phase-1
owner: dev
action: needs-dev
---
# Events

Merged design-bible notes and PluginV2 module reference.


## PluginV2 module

World events (Eclipse, Paranoia, etc.) and cascades.

## Purpose

- Schedule and run world events based on calendar, player preference, and server role.
- EventRegistry + EventCascadeRegistry; heavy logic in domain handlers, not in listeners.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `WorldEvent` | Interface for a single event type (e.g. Eclipse). |
| `EventEngine` | Schedules events; holds EventRegistry and EventCascadeRegistry. |
| `EventRegistry` | Map of event id â†’ handler. |
| `EventCascadeRegistry` | Cascades (e.g. event A triggers B). |
| `PlayerEventPreferenceService` | Per-player event prefs; mapped into profile or separate store. |

## Config

- `config/events.yml` â€“ Event definitions, weights, cadence, cascade rules.

## DB

- Event state (which event is active, start time, etc.) and optionally player preferences; tables TBD per design.

## Integration

- CalendarService drives tick; EventEngine reacts and runs events. Listeners only notify the engine (e.g. â€œday changedâ€, â€œplayer joined during eventâ€).



## AeternumSeasons event classes (file-by-file)

### BloodMoonEvent
- **What it does:** Single-night event: overworld night, optional time warp/freeze, no sleeping; all spawning monsters get a tag, buffed health/attack, and a custom name; on end, tagged mobs are cleaned and attributes/names restored.
- **Config:** `YamlEvents.get(plugin)` â†’ `events.blood_moon.*` (enabled, chance_per_night, min_day, max_times_per_season, world_time_night, freeze_night).
- **EventContext:** Uses `ctx.overworlds()` for world iteration and game rules; no `getEligiblePlayers()`.
- **Pattern:** **PDC + scoreboard tag for mob restore** - save original name/visibility and base health/attack in PDC, apply buffs, restore from PDC and remove tag on event end.

### HeatWaveEvent
- **What it does:** Summer multi-day event: title on start/end; every tick, players in overworld (not aeternum_frost) in sun with insufficient armor get weakness and optional damage; optional flame particles.
- **Config:** `events.heat_wave.*` (enabled, max_per_summer, min/max_duration_days, base_chance_per_day, damage_exposed, tick_damage, apply_weakness, min_armor_protection) and `events.visual_effects.particles_enabled`.
- **EventContext:** `ctx.overworlds()` for sounds and world checks; per-player logic uses Bukkit.getOnlinePlayers() and skips frost world by name.
- **Pattern:** **Shade check** (compare block Y to highest block at X/Z) and **armor-piece count** for exposure; configurable damage and weakness.

### WinterFreezeEvent
- **What it does:** Winter multi-day event: title on start/end; every tick, players not near "heat" blocks take slowness/mining fatigue and damage; near heat or with enough armor reduces or avoids damage.
- **Config:** `events.winter_freeze.*` (enabled, max_per_winter, min/max_duration_days, base_chance_per_day, damage_per_tick, min_armor_pieces, heat_radius_blocks, heat_block_types list).
- **EventContext:** `ctx.overworlds()` for sounds; `ctx.seasons().getDaysPerSeason()` in `canStartToday` for min start day.
- **Pattern:** **Configurable heat blocks and radius** (e.g. CAMPFIRE, TORCH, LANTERN) to define safe zones; same armor-count pattern as HeatWave.

### MagicStormEvent
- **What it does:** Multi-day storm event: sets storm/thunder and keeps it; some mobs get PDC mark, glowing, speed/strength, custom name ("storm" mob); on death, chance for extra enchanted gear/book; on end or chunk load when event inactive, storm mobs are restored from PDC.
- **Config:** `events.magic_storm.*` (enabled, min/max_duration_days, base_chance_per_day, allowed_seasons, rare_mob_chance, extra_loot_chance) and particles.
- **EventContext:** `ctx.overworlds()` for weather and cleanup.
- **Pattern:** **PDC for storm mob identity + restore on end/chunk load**; LightningStrikeEvent cancelled when block protected by WinterWorldGuardHelper; random enchanted books/gear as loot.

### SeasonFestivalEvent
- **What it does:** Multi-day festival: villagers and wandering traders near players get festival role (PDC), optional profession override, custom name, and dynamic merchant recipes (headliner/themed/common pools, daily rotation by day index); on end, festival trades and PDC keys removed from all villagers/traders.
- **Config:** `events.festival.*` (enabled, min/max_duration_days, base_chance_per_day, max_per_season, merchant_chance, headliner_chance, min_merchants_near_player, force_profession_on_merchants) and particles.
- **EventContext:** `ctx.overworlds()` for iteration; per-player radius (48 blocks) to find villagers/traders and ensure minimum festival merchants.
- **Pattern:** **PDC for villager/trader role, day, season, year, original name/profession**; deterministic trades via seed (UUID + season + year + dayIndex); action bar on interact with festival merchant; PDC on result item to mark festival trade.

### FishingFestivalEvent
- **What it does:** Multi-day event: on catch, chance for treasure (enchanted books, rods, blocks, etc.) and double catch; rod enchant (luck/lure) scales chances.
- **Config:** `events.fishing.*` (enabled, min/max_duration_days, base_chance_per_day, max_per_year, treasure_chance, double_catch_chance) and particles.
- **EventContext:** Not used beyond interface; logic is PlayerFishEvent in overworld.
- **Pattern:** **PlayerFishEvent.State.CAUGHT_FISH**; separate treasure vs double-catch rolls; rod-based factor caps.

### MiningBlessingEvent
- **What it does:** Multi-day event: breaking configured ore types gives chance for extra primary drop, rare bonus drop, and extra XP.
- **Config:** `events.mining.*` (enabled, min/max_duration_days, base_chance_per_day, max_per_year, extra_ore_chance, exp_boost_chance, rare_bonus_chance) and particles.
- **EventContext:** Not used.
- **Pattern:** **BlockBreakEvent**; ore set (coal, iron, copper, gold, lapis, redstone, diamond, emerald); `getDrops(tool, player)` for primary drop; separate rolls for extra ore, rare drop, XP.

### TornadoEvent
- **What it does:** Per-world moving "tornado" (center location, drift, optional chase toward player): particles for viewers in range, pull/damage to entities in radius, crop break chance; world allow/disable list; duration in real-time minutes; single target player at start, respawns center if empty.
- **Config:** `events.tornado.*` (enabled, min/max_spawn_distance, min/max_duration_days, min/max_duration_minutes, base_chance_per_day, damage_per_tick, crop_break_chance, radius/height, pull_*, targets.viewer_distance, only_survival_adventure, allowed_worlds, disabled_worlds).
- **EventContext:** `ctx.overworlds()` and `pickSingleTarget(ctx, r)` using `ctx.overworlds()` and game mode filter.
- **Pattern:** **World allow/block list**; **per-world center + endTicks + drift vector**; WinterWorldGuardHelper for block modify; Bisected handling for tall plants; viewer-distance gating for particles.

### RestfulSleepEvent
- **What it does:** Multi-day event: on bed leave in overworld, apply health boost for configured seconds; on damage, delayed check to remove health boost if health low; title on start, message on end.
- **Config:** `events.restful_sleep.*` (enabled, min/max_duration_days, base_chance_per_day, health_boost_seconds).
- **EventContext:** Not used.
- **Pattern:** **Bed leave = buff, damage = conditional remove**; PlayerBedEnterEvent only sends message; EntityDamageEvent + runTaskLater to strip buff when health â‰¤ 10.

### HolidayEvent
- **What it does:** (Decompilation failed; from events.yml and service.) Date-based "forced" event like New Year; config has halloween (mob_radius, pumpkin_chance, buff_chance) and christmas (gift_radius, heal_radius, heal_amount). Treated as forced_special in SeasonalEventService.
- **Config:** `events.holiday.*` with halloween and christmas subsections.
- **EventContext:** Used by service for candidate selection.
- **Pattern:** **Calendar-driven special event** with sub-modes (Halloween/Christmas) and radius-based effects.

### NewYearEvent
- **What it does:** Fires only on Spring day 1 (calendar New Year); title + gift (fireworks, gunpowder); for ~60 seconds, periodic chance per player to spawn firework particles/sounds around them.
- **Config:** `events.new_year.*` (enabled, fireworks_chance, fireworks_bursts, gift_rockets_min/max, gift_gunpowder_min/max) and particles.
- **EventContext:** `ctx.overworlds()` for iterating players and spawning effects.
- **Pattern:** **Exact calendar day (season + day)** for canStartToday; one-day duration; tick counter for timed firework phase; gift on start.

### NetherFishingDerbyEvent
- **What it does:** Nether-only: fishing in lava; hook kept at surface (optional); after bite_min/max seconds, "bite" (sound/particles/action bar); on reel-in, must be in lava and within bite window for reward roll (cooldown_ms between rewards); loot table (quartz, gold nugget, blaze powder, etc., up to wither skull).
- **Config:** `events.nether_fishing_derby.*` (enabled, min/max_duration_days, base_chance_per_day, min_lava_seconds, cooldown_ms, reward_chance, bite_min/max_seconds, bite_window_ms, keep_hook_on_surface).
- **EventContext:** Not used; start only notifies Nether players.
- **Pattern:** **Per-player state** (cast time, last reward, hook UUID, bite ready time, scheduled tasks); float task to keep hook at lava surface; delayed task for bite; PlayerFishEvent state machine.

### PiglinMarketEvent
- **What it does:** During event, PiglinBarterEvent: chance to duplicate an outcome item and chance to add a bonus item (gold nuggets, obsidian, spectral arrow, etc.).
- **Config:** `events.piglin_market.*` (enabled, min/max_duration_days, base_chance_per_day, bonus_item_chance, duplicate_outcome_chance).
- **EventContext:** Not used.
- **Pattern:** **PiglinBarterEvent**; modify `e.getOutcome()` list in place.

### QuartzRushEvent
- **What it does:** In Nether, breaking nether quartz ore has a chance to drop extra quartz (configurable amount range) and particles.
- **Config:** `events.quartz_rush.*` (enabled, min/max_duration_days, base_chance_per_day, extra_quartz_chance, extra_min, extra_max).
- **EventContext:** Not used; canStartToday requires someone in Nether.
- **Pattern:** **BlockBreakEvent** for NETHER_QUARTZ_ORE; single extra-drop roll.

### FungusBloomEvent
- **What it does:** In Nether, breaking certain fungus/roots/vines has a chance to drop an extra primary drop and particles.
- **Config:** `events.fungus_bloom.*` (enabled, min/max_duration_days, base_chance_per_day, extra_drop_chance).
- **EventContext:** Not used; canStartToday requires someone in Nether.
- **Pattern:** **BlockBreakEvent** with fixed material set; getPrimaryDrop via getDrops(tool, player).

### BlazeSurgeEvent
- **What it does:** In Nether, Blaze spawn chance to buff (speed, glowing, higher max health); on death by player, chance for extra blaze rods.
- **Config:** `events.blaze_surge.*` (enabled, min/max_duration_days, base_chance_per_day, extra_rod_chance, extra_rod_min/max, buffed_blaze_chance).
- **EventContext:** Not used; canStartToday requires someone in Nether.
- **Pattern:** **CreatureSpawnEvent** for Blaze buff; **EntityDeathEvent** for extra drops; no PDC (buffs not restored on end).

### MagmaTidesEvent
- **What it does:** In Nether, MagmaCube spawn chance to increase size by 1 (cap 4); on death by player, chance for extra magma cream.
- **Config:** `events.magma_tides.*` (enabled, min/max_duration_days, base_chance_per_day, bigger_cube_chance, extra_cream_chance).
- **EventContext:** Not used; canStartToday requires someone in Nether.
- **Pattern:** **CreatureSpawnEvent** + **EntityDeathEvent**; cube.setSize() for bigger cubes.

### GhastAlertEvent
- **What it does:** In Nether, on Ghast spawn (natural/chunk_gen): chance to mark (name, glowing); chance to spawn extra Ghast nearby with chunk cooldown and max nearby cap; on death by player, chance for extra ghast tear.
- **Config:** `events.ghast_alert.*` (enabled, min/max_duration_days, base_chance_per_day, marked_ghast_chance, extra_tear_chance, extra_spawn_chance, chunk_cooldown_ms, max_nearby_ghasts, nearby_radius).
- **EventContext:** Not used; onStart clears chunk cooldown map.
- **Pattern:** **Chunk cooldown map** (key = world+chunk) to throttle extra spawns; World.spawn(loc, Ghast.class, consumer) for extra Ghast.

### WitherLooseEvent
- **What it does:** (Decompilation failed; from events.yml.) Wither spawn event: per-day chance, target count (min/max), survival/adventure only, spawn delay and countdown interval, distance range, wither config (max health, glowing, idle kill), custom loot (netherite scrap, fire prot book, ghast tear).
- **Config:** `events.wither_loose.*` (base_chance_per_day, min/max_duration_days, targets.*, spawn_delay_seconds, countdown_interval_seconds, spawn min/max_distance, wither.*, loot.*).
- **EventContext:** Used by service for registration and lifecycle.
- **Pattern:** **Boss-style event** with countdown, delayed spawn, and optional idle kill; configurable loot table.

### WitherSkeletonSwarmEvent
- **What it does:** In Nether, WitherSkeleton spawn: chance to buff (tag, speed, 2Ã— health/attack, glowing, name); chance to spawn extra WitherSkeleton with chunk cooldown; on death by player, chance for extra loot and small chance for skull.
- **Config:** `events.wither_skeleton_swarm.*` (enabled, min/max_duration_days, base_chance_per_day, buff_chance, extra_spawn_chance, chunk_cooldown_ms, extra_loot_chance, extra_skull_chance).
- **EventContext:** Not used; running flag set in onStart/onEnd.
- **Pattern:** **Scoreboard tag** for buffed mobs (no PDC restore); chunk-key cooldown for extra spawns; skip if SpawnReason.CUSTOM.


## V2 events checklist

### Adopt from Aeternum
- **EventContext** - Shared context (plugin, season service, overworlds, disabled-fx worlds, eligible players) passed into all event methods.
- **Explicit lifecycle** - `onStart`, `onEnd`, `onDayTick`, `onTick`; service calls them at the right time and registers/unregisters the active event as a Listener.
- **Single `events.yml`** - One file under `events.*` with per-event sections (enabled, durations, chances, behavior flags); YamlEvents-style loader with optional reload.
- **/event start | stop** - Admin command to force start (with optional duration) and force stop; list registered events and show active + days remaining.
- **One active + "tomorrow" queue** - Only one event active; optional pre-chosen event for the next day for announcements (e.g. Season Clock / placeholders).
- **Per-event config** - Each event type has its own subsection (e.g. `events.blood_moon.*`, `events.heat_wave.*`) with sensible defaults.
- **Lang keys for titles/messages** - e.g. `event.<id>.title`, `event.<id>.subtitle`, `event.<id>.end`, and action-bar or tooltip keys where needed.
- **PDC for reversible mob/entity state** - When events buff or rename mobs, store original state in PDC and restore on end (or chunk load if event ended); use scoreboard tags for quick filtering.
- **Game rules / world time** - Blood Moon pattern: optional warp to night and freeze day/night cycle; restore on end.
- **World allow/block lists** - Tornado-style allowed_worlds / disabled_worlds for events that should not run in lobby or specific worlds.
- **Calendar-driven specials** - Holiday/New Year style: exact calendar day (or range) forces the event when eligible; no 60% roll.

### Keep from old LW plugin
- **Cascades** - EventCascadeRegistry / cascade chains (e.g. event A can trigger event B after delay or condition); keep if V2 wants chained or follow-up events.
- **Cooldowns** - EventCooldownTracker; per-event or global cooldowns so the same event (or type) doesn't repeat too soon; complement Aeternum's per-season/per-year caps.
- **TPS guard** - Skip or throttle heavy event logic when TPS is low (e.g. in tick or day logic) to avoid lag spikes.
- **Boss bar** - EventBossBarManager; optional boss bar for active event name/duration for visibility; Aeternum uses titles only.
- **Multiple active events (optional)** - LW may allow more than one concurrent event; if V2 wants that, extend the service to hold a set of active events and call lifecycle for each, with clear rules for interaction and priority.
- **Player preferences** - PlayerEventPreferences / opt-out or per-event toggles so players can disable certain events or notifications; Aeternum has no opt-out.
- **DayChangeListener / calendar sync** - Keep calendar sync and "day advanced" semantics so V2 events still hook into a single day-advance signal (like SeasonUpdateEvent with isDayAdvanced).

---

## Config and lang

- **events.yml:** Root key `events` with global options: `enabled`, `require_players.enabled` / `require_players.players`, `visual_effects.particles_enabled`. Each event has a subsection (e.g. `blood_moon`, `heat_wave`, `winter_freeze`, â€¦) with enabled, durations, chances, and behavior options. Bilingual comments (e.g. Spanish/English) in the file. No separate file per event; one flat structure under `events.<section>.*`.
- **Lang:** Event titles and messages go through the plugin's lang system (e.g. `plugin.lang.tr(player, "event.<id>.title")`). Keys used: `event.<id>.title`, `event.<id>.subtitle`, `event.<id>.end`, and event-specific keys (e.g. `event.blood_moon.no_sleep`, `event.restful_sleep.buffed`, `event.festival.actionbar`, `cmd.event.usage`, `cmd.event.list.header`, `cmd.event.list.item` with `{id}`/`{name}`, etc.). Guide uses keys like `guide.events.blood_moon`. Display names can be hardcoded in the event class (e.g. `getDisplayName()`) or moved to lang; Aeternum uses a mix (some from lang, some fixed in code).
