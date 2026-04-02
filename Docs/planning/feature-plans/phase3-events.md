---
title: Phase 3 Events
description: Phase 3 events roadmap including Nether events.
tags:
  - planning
  - events
status: partial
phase: phase-2
owner: dev
action: needs-dev
---
# Phase 3: Events V2 — AeternumSeasons Review and Checklist

> **Status Update (2026-03-19):** All Aeternum-style overworld events are **IMPLEMENTED**. Nether events are **NOT YET IMPLEMENTED**.
>
> **✅ Implemented (in `events/impl/`):**
> - BloodMoonEvent, MagicStormEvent, TornadoEvent
> - FestivalEvent, FishingFestivalEvent, MiningBlessingEvent, RestfulSleepEvent
> - HolidayEvent, NewYearEvent
> - (Plus existing: Eclipse, Thunder, Blizzard, Frost, Fog, Heatwave, Paranoia, SpringBloom, AutumnLeafFall, SeasonalStorm, JungleMonsoon)
>
> **❌ Not Implemented (Nether events):**
> - NetherFishingDerbyEvent (lava fishing)
> - PiglinMarketEvent (barter bonus)
> - QuartzRushEvent (extra quartz)
> - FungusBloomEvent (extra fungus drops)
> - BlazeSurgeEvent (buffed Blazes)
> - MagmaTidesEvent (bigger MagmaCubes)
> - GhastAlertEvent (marked Ghasts)
> - WitherLooseEvent (Wither spawn event)
> - WitherSkeletonSwarmEvent (buffed skeletons)

This document summarizes the AeternumSeasons event system (decompiled from `AeternumSeasons-3.9.jar.src`) and provides a checklist for the V2 Events module. It informs design and implementation choices for PluginV2's seasonal events.


## Core event infrastructure

### SeasonalEventService
- **Role:** Central scheduler and registry: listens for `SeasonUpdateEvent` (day advance), maintains one active event and a "queued tomorrow" event, runs a 1s tick task to call `active.onTick(st, ctx)`, and starts/stops events by duration and eligibility.
- **Key methods:** `register()` / `unregister()`, `run()` (tick), `onSeasonUpdate(SeasonUpdateEvent)` (day logic, tryStartNewEvent, updateTomorrowQueue), `startEvent` / `stopActive`, `forceStart(id, days)` / `forceStop()`, `getActive()`, `getDaysRemaining()`, `getQueuedTomorrow()`, `getRegisteredEventIds()`, `getEventById(id)`, `hasEnoughPlayersOnline()` (optional min players from config). **One active event at a time;** replacing starts new and stops current. Holiday/NewYear are "forced_special" (always chosen if eligible); others use 60% roll and random candidate. Queued event starts when its day arrives and players sufficient.

### EventContext
- **Role:** Shared context passed into event lifecycle: plugin, SeasonService, overworld list, and "disabled season fx" world set (for filtering players/worlds).
- **Key methods:** `plugin()`, `seasons()`, `overworlds()`, `setDisabledSeasonFxWorlds(Collection)`, `isSeasonFxEnabled(World|Player)`, `getEligiblePlayers()`. Used for world iteration, player filtering, and season queries (e.g. getDaysPerSeason).

### SeasonalEvent (interface)
- **Role:** Contract for all calendar-driven events: id, display name, season filter, duration range, and lifecycle hooks.
- **Key methods:** `getId()`, `getDisplayName()`, `isSeasonAllowed(Season)`, `getMinDurationDays()`, `getMaxDurationDays()`, `canStartToday(CalendarState, EventContext)`, `onStart`, `onEnd`, `onDayTick`, `onTick`. Implementations are also Bukkit Listeners; service registers them when active and unregisters on stop.

### YamlEvents
- **Role:** Single shared `events.yml` loader: `get(plugin)` returns cached FileConfiguration (creates from default if missing); `reload(plugin)` clears cache and reloads.
- **Key methods:** `get(AeternumSeasonsPlugin)`, `reload(AeternumSeasonsPlugin)`. All event classes and SeasonalEventService read via `YamlEvents.get(this.plugin)` with paths under `events.<event_section>.*`.

### EventCommand
- **Role:** `/event list | info | start <id> [days] | stop` with permission `aeternum.events`; uses plugin lang for messages.
- **Key methods:** `onCommand` (list = registered ids + display names, info = active + days remaining, start = forceStart, stop = forceStop), `onTabComplete` (subcommands and event ids for start). No config reload; display names from `getDisplayName()`.

### SeasonUpdateEvent (calendar)
- **Role:** Bukkit Event fired when calendar updates (e.g. day advance). Carries `SeasonService` source, `CalendarState` (day, season, year), and `isDayAdvanced()`. SeasonalEventService only reacts when `isDayAdvanced()` is true.


## Config and lang

- **events.yml:** Root key `events` with global options: `enabled`, `require_players.enabled` / `require_players.players`, `visual_effects.particles_enabled`. Each event has a subsection (e.g. `blood_moon`, `heat_wave`, `winter_freeze`, …) with enabled, durations, chances, and behavior options. Bilingual comments (e.g. Spanish/English) in the file. No separate file per event; one flat structure under `events.<section>.*`.
- **Lang:** Event titles and messages go through the plugin's lang system (e.g. `plugin.lang.tr(player, "event.<id>.title")`). Keys used: `event.<id>.title`, `event.<id>.subtitle`, `event.<id>.end`, and event-specific keys (e.g. `event.blood_moon.no_sleep`, `event.restful_sleep.buffed`, `event.festival.actionbar`, `cmd.event.usage`, `cmd.event.list.header`, `cmd.event.list.item` with `{id}`/`{name}`, etc.). Guide uses keys like `guide.events.blood_moon`. Display names can be hardcoded in the event class (e.g. `getDisplayName()`) or moved to lang; Aeternum uses a mix (some from lang, some fixed in code).
