---
title: Feature Inventory
description: Complete feature inventory with behaviour, commands, and configs.
tags:
  - roadmap
  - reference
status: reference
phase: ongoing
owner: admin
action: none
---
# PluginV2 – Complete Feature Inventory

Per-feature documentation at full depth. Source of truth for what exists and how it works.


## 2. Infrastructure

**Config** (`infra/config/`) – **ConfigService** loads `config/core.yml` (environment, server-role, **enabled-modules**) and `config/db.yml` (datasources with jdbc-url, username, password, maximum-pool-size). Copies JAR defaults to data folder if missing. Used by RPGCorePlugin on startup.

**Database** (`infra/db/`) – **DatabaseProvider** builds HikariCP pools from db.yml. **SqlExecutor** provides async query/update helpers. Used by player, progression, calendar, clans.

**Scheduler** (`infra/scheduler/`) – **SchedulerService** wraps Bukkit scheduler: runSync, runAsync, runSyncDelayed, runSyncRepeating.

**Messaging** (`infra/messaging/`) – **ClusterMessagingService** interface; **StubClusterMessagingService** no-op implementation for single-server.


## 4. Progression module

**Location:** `com.lostwilderness.rpgcore.progression/`

- **ProgressionService** – `hasUnlocked(uuid, key)`, `unlock(uuid, key)`, `getCounter(uuid, counterKey)`, `incrementCounter(uuid, counterKey)`; sync API with 5s timeout on repo futures. Async variants return CompletableFuture.
- **ProgressionRepository** – Tables e.g. player_achievements, progression_counters; hasUnlocked, unlock, getCounter, incrementCounter.
- **Milestones:** `first_join`, `join_3_times`, `join_10_times`. Claimed state stored separately (e.g. `first_join_claimed`).

**ProgressionJoinListener**

- **When it runs:** On **PlayerJoinEvent**; all progression work runs in an **async** task so the main thread is not blocked.
- **What it does (step-by-step):** (1) If `!hasUnlocked(uuid, first_join)`: call `unlock(uuid, first_join)` and, if present, `BetonQuestBridge.onMilestoneUnlocked(player, first_join)`. (2) `joinCount = incrementCounter(uuid, join_count)`. (3) If `joinCount >= 3` and `!hasUnlocked(uuid, join_3_times)`: unlock join_3_times, notify BetonQuest. (4) If `joinCount >= 10` and `!hasUnlocked(uuid, join_10_times)`: unlock join_10_times, notify BetonQuest. AuraSkillsBridge.isAvailable() is called for lazy-init only; **no XP is granted in this listener** — claiming is done via **V2ClaimCommand**.
- **Cleanup:** No tasks or state to clear.

**V2ProgressCommand**

- **When it runs:** Player runs `/v2progress` (player-only).
- **What it does:** Lists all unlocked milestone keys for the sender (from `ProgressionService.getUnlockedKeys(uuid)`). No args; no permission in plugin.yml.

**V2ClaimCommand**

- **When it runs:** Player runs `/v2claim <milestone>` with milestone one of: `first_join`, `join_3_times`, `join_10_times`.
- **What it does (step-by-step):** (1) Resolve milestone string to a `MilestoneReward` (milestoneKey, claimedKey, skillKey, xpAmount). If unknown, send usage and return. (2) **Async:** Check `hasUnlocked(uuid, milestoneKey)`; if false, sync message "You haven't unlocked this milestone yet." and return. (3) Check `hasUnlocked(uuid, claimedKey)`; if true, sync message "You already claimed this reward." and return. (4) If AuraSkills not available, sync message "Skill rewards are not available right now." and return. (5) `unlock(uuid, claimedKey)`. (6) **Sync:** `AuraSkillsBridge.addSkillXp(uuid, skillKey, amount)`, send "Claimed &lt;amount&gt; &lt;skill&gt; XP!", and if present `BetonQuestBridge.onMilestoneClaimed(player, milestoneKey)`. Rewards: first_join → 50 Fighting; join_3_times → 25 Fighting; join_10_times → 100 Fighting.
- **Cleanup:** None.

**RewardsCommand**

- **When it runs:** Player runs `/rewards` (player-only).
- **What it does:** If BetonQuest is loaded, dispatches as **console** the command `rpgmenu open default.milestoneRewards <playerName>` so the player gets the BetonQuest milestone rewards menu. If BetonQuest is not loaded, sends "Rewards menu is not available (BetonQuest not loaded)."
- **BetonQuestBridge** – onMilestoneUnlocked(player, key), onMilestoneClaimed(player, key) for quest integration.


## 6. Calendar module

**Location:** `com.lostwilderness.rpgcore.calendar/`

- **CalendarServiceV2** – Interface. **CalendarSnapshot**: date (LocalDate), dayCount, season (SPRING/SUMMER/AUTUMN/WINTER), mcDay. Methods: getCurrentSnapshot(), getPlayerJoinDate(uuid), recordFirstJoinIfAbsent(uuid), advanceToNextDay(), resetCalendar().
- **CalendarServiceV2Impl** – Persists snapshot via CalendarRepository; when day advances, fires **CalendarDayAdvancedEvent** with newSnapshot and previousSnapshot.
- **CalendarDayAdvancedEvent** – Custom Bukkit event; payload newSnapshot, previousSnapshot. Fired on advance (MC day change or /nextday).
**CalendarDayChangeTitleListener**

- **When it runs:** On **CalendarDayAdvancedEvent** (fired when the calendar advances one day — MC day change or `/nextday`).
- **What it does:** (1) Calls `NewYearFireworkHandler.onDayAdvanced` (e.g. for New Year celebration fireworks). (2) Sends a **title** to all overworld players with the formatted date (e.g. "March 1, 1MC"). (3) Sends a **subtitle**: either the current season or, during Easter week, the appropriate Easter subtitle via **EasterWeekHelper** (Palm Sunday through Easter Sunday). No config keys for the title format; logic uses the new snapshot from the event.
- **EasterWeekHelper** – Palm Sunday → Easter Sunday subtitle logic.
- **NewYearFireworkHandler** – Celebration fireworks (e.g. New Year).
- **SeasonGuideBook** – Used by `/season guide`.

**Wrapper commands (Survival & Amplified):** `date` → "Date: D/M/Y"; `time` → "Time: HH:MM (MC day N)"; `eoc` → "Days since Epoch: N"; `season` → current season; `season guide` opens guide book; `datejoined` (player) → "You first joined on: D/M/Y". Survival only: `nextday` (lw.admin.calendar), `resetcalendar` (lw.admin.calendar); `season biomes [on|off|restore|backup on|off]` (lw.admin.biomes).


**ParanoiaEvent** — display: "Paranoia"

- **When it starts:** Only when Eclipse is already active. On each calendar day tick EventServiceImpl calls `onCalendarDay`; if Eclipse is active and Paranoia isn't, Paranoia starts. No separate chance/season check for Paranoia itself (cascade from Eclipse). Cascade chance is in `events.paranoia.chance` (EventsModule).
- **Duration:** One MC night: 24000 ticks (~20 min). A delayed task calls `forceEndEarly()` after 24000 ticks.
- **While active:** Listener for **PlayerJoinEvent** / **PlayerQuitEvent**. For each online overworld player it schedules repeating tasks that play random horror sounds (default: AMBIENT_CAVE, ENTITY_ENDERMAN_STARE, ENTITY_WITHER_AMBIENT; or config `events.paranoia.sounds` list) at intervals from `events.paranoia.min-interval-seconds` to `max-interval-seconds` (default 20–60). After each sound the task reschedules itself with a new random delay. New joiners get the same sound loop; leavers have their task cancelled.
- **End / cleanup:** After 24000 ticks the stop task runs `forceEndEarly()`: sets `active = false`, cancels all per-player sound tasks and the stop task. No world/block/mob state to restore.


**MagicStormEvent** — display: "Magic Storm"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`magic_storm.enabled`, default true), random < `base_chance_per_day` (default 0.08), and current season is in `magic_storm.allowed_seasons`.
- **Duration:** One MC day: 24000 ticks. End task clears weather and restores storm mobs.
- **While active:** Sets overworld storm + thundering; announces "A magical storm sweeps across the land…". **CreatureSpawnEvent:** overworld monsters have `rare_mob_chance` (default 0.35) to become "storm" mobs — PDC stores original stats, then glowing, Speed I, Strength I, +25% health/damage, name "§dStorm …", tag `lw_magic_storm`. **EntityDeathEvent:** killing a storm mob has `extra_loot_chance` (default 0.40) to drop enchanted book or enchanted golden apple. **ChunkLoadEvent:** when event is not active, restores any loaded storm-tagged mobs.
- **End / cleanup:** End task turns off storm/thunder, calls `restoreAllStormMobs()`, sets `active = false`. `forceEndEarly()` does the same plus clears storm/thunder on all overworlds.


**MiningBlessingEvent** — display: "Miner's Blessing"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`mining.enabled`, default true) and roll < `base_chance_per_day` (default 0.12).
- **Duration:** One MC day: 24000 ticks. End task only sets `active = false`.
- **While active:** Message "Miner's Blessing! Extra ore and XP from mining today." **BlockBreakEvent:** when breaking configured ore types, chance for extra ore drop (`extra_ore_chance` default 0.35) and for bonus XP (`exp_boost_chance` default 0.40).
- **End / cleanup:** After 24000 ticks or `forceEndEarly()`: cancel end task, set `active = false`. No block/mob cleanup.


**EclipseEvent** — display: "Eclipse"

- **When it starts:** Each calendar day, overworld, when `(day - offset) % interval == 0` (e.g. `events.eclipse.interval-days` 30, `events.eclipse.offset-days` 0) and a chance roll < `events.eclipse.chance-pct` (default 80). Day must be ≥ offset.
- **Duration:** One MC night: 24000 ticks. End task at 24000 ticks restores daylight and time.
- **While active:** Optional resource pack from `resource-packs.eclipse` applied to overworld players; daylight cycle off; time 18000; message "A lunar eclipse blankets the sky…". **EclipseHordeTask** runs every 6000 ticks: spawns hostile mobs near players (region 80 blocks, `events.eclipse.horde-size` e.g. 8). No extra Bukkit listeners on the event class itself.
- **End / cleanup:** End task re-enables daylight, sets time 6000, optionally applies `resource-packs.eclipse-empty`, message "The eclipse fades… dawn breaks anew.", cancels horde task, sets `active = false`. `forceEndEarly()` re-enables daylight on all overworlds and cancels horde and end tasks.


**FogEvent** — display: "Fog"

- **When it starts:** Any calendar day, overworld; roll < `events.fog.chance-pct` (default 20). Broadcast "A rolling fog envelops the land…".
- **Duration:** Config `events.fog.restore-delay-ticks` (default 6000). No view-distance or ProtocolLib effect in the class.
- **While active:** No listeners or ongoing tasks; only the start broadcast and the delayed end.
- **End / cleanup:** After delay: broadcast "The fog lifts, and clarity returns.", set `active = false`. `forceEndEarly()` cancels the task and sets `active = false`.


**TornadoEvent** — display: "Tornado"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`tornado.enabled`, default true), world not in `tornado.disabled_worlds`, and roll < `base_chance_per_day` (default 0.05). Center spawns at configurable distance from a random online player; drift from `tornado.drift_speed`.
- **Duration:** Random real-time minutes: `tornado.min_duration_minutes`–`tornado.max_duration_minutes` (default 5–10). A run task checks `System.currentTimeMillis() >= endTime` each tick.
- **While active:** Message "A tornado has been sighted! Seek shelter." Repeating task every 5 ticks: moves center by drift and re-anchors Y to highest block; in radius (`tornado.radius_blocks`, default 10) damages players without roof, pulls non-players toward center, 20% chance to break wheat/carrots/potatoes/beetroots; spawns cloud particles at center.
- **End / cleanup:** When `System.currentTimeMillis() >= endTime`, sets `active = false`, message "The tornado has dissipated.", cancels run task. `forceEndEarly()` sets `active = false`, cancels task. No block or entity restore.


**NewYearEvent** — display: "New Year"

- **When it starts:** Only on **first day of Spring** (March 1) from calendar snapshot, overworld, and only for the first overworld; `events.new-year.enabled` (default true).
- **Duration:** Instant for the main effect; then an optional "firework phase" for `events.new-year.firework-phase-duration-ticks` (default 60), after which `active` is set false in the same day tick.
- **While active:** Title "Happy New Year!" / "Spring has begun! Enjoy the festivities."; gift fireworks and gunpowder to all overworld players. Firework phase: every 40 ticks, spawn a celebration firework at a random overworld player. Then `active = false` is set in the same day tick.
- **End / cleanup:** `forceEndEarly()` cancels firework phase task and sets `active = false`. No world/mob cleanup.


**ThunderEvent** — display: "Thunderstorm"

- **When it starts:** **Daily:** overworld, roll < `events.thunder.base-chance-pct` (default 15). **Cascade:** can be triggered by Eclipse via EventCascadeRegistry with `events.thunder.eclipse-chance-pct` (default 40) for "eclipse storm" with undead cavalry.
- **Duration:** Until 6 AM (ticks until time 6000). End task clears storm/thunder and broadcasts "The thunderstorm subsides…".
- **While active:** Storm + thundering on. Normal: broadcast "A thunderstorm rolls across the land". Eclipse mode: broadcast "Thunderstorm Eclipse! Undead cavalry…", spawn zombie horses with golden-armored zombie riders (count `events.thunder.horses-per-eclipse`). No ongoing listeners beyond the end task.
- **End / cleanup:** End task clears storm/thunder, sets `active = false`, broadcast. `forceEndEarly()` cancels end task and clears storm/thunder on overworlds.


**SeasonalStormEvent** — display: "Seasonal Storm"

- **When it starts:** Only in **late autumn or winter** (month ≥ 10 or ≤ 2); overworld; roll < `events.seasonal-storm.chance-pct` (default 25).
- **Duration:** Until 6 AM (ticks until time 6000). End task clears storm/thunder and cancels lightning task.
- **While active:** Broadcast "A seasonal storm rolls in…", storm + thundering. **Clustered lightning:** once at start and then every 3 MC minutes: per 100-block cluster around players, try to strike log/leaves blocks (and set fire on block above) or else strike player location.
- **End / cleanup:** End task clears storm/thunder, cancels lightning task, broadcast "The seasonal storm subsides…", sets `active = false`. `forceEndEarly()` cancels both tasks, clears storm/thunder on overworlds, same message.


**SpringBloomEvent** — display: "Spring Bloom"

- **When it starts:** Only on **first day of Spring** (March 1) from calendar; overworld.
- **Duration:** Single day-tick; no sustained duration. Sets `active = true` then immediately runs effects and sets `active = false`.
- **While active:** Message "Spring Has Sprung! Flowers bloom everywhere (in spring biomes)!". For all loaded chunks: age up Ageable blocks (e.g. crops) by 1. For each player, near flowers (poppy, dandelion, sunflower, azure bluet) spawn bees (5 attempts). No listeners or recurring tasks.
- **End / cleanup:** `forceEndEarly()` only sets `active = false`. No block/entity restore.


**TestEvent** (SeasonalEvent) — id: "test", display: "Test Event"

- **When it starts:** Never by calendar: `canStartToday` always returns false. Can be force-started via command (e.g. `/event start test` on Survival). All seasons allowed (`isSeasonAllowed` true).
- **Duration:** 1 day (min and max duration days = 1).
- **While active:** On start, sends eligible players "Test event started (day N)." No listeners or gameplay logic in the class.
- **End / cleanup:** On end, sends "Test event ended." No world/mob cleanup.

Boss bar text comes from each event’s getDisplayName(); EventBossBarManager aggregates and ticks.


## 9. World module

**Location:** `com.lostwilderness.rpgcore.world/`

- **BiomeBackupData** – Data for biome backup/restore.
- **BiomeBackupStore** – Listens ChunkLoadEvent; in restore mode enqueues chunk backups; restore task processes queue. Used by events (biome painting) and Survival `/season biomes` (backup on/off, restore, painting).


## 11. Configs

| File | Purpose | Key consumers |
|------|---------|----------------|
| **config.yml** | Main plugin config: events (pause-if-tps-below, boss-bar, cooldowns.*, blood-moon, frost, new-year, holiday, eclipse, thunder, fog, blizzard, heatwave, paranoia, seasonal-weather, wildlife-migration, biome-painting), disabled_worlds, world-day-offsets, resource-packs.eclipse | EventServiceImpl, EventBossBarManager, event impls, SeasonalWeatherListener |
| **config/core.yml** | environment, server-role, enabled-modules | ConfigService, RPGCorePlugin |
| **config/db.yml** | datasources.* (jdbc-url, username, password, maximum-pool-size) | ConfigService, DatabaseProvider |
| **lw-climate.yml** | seasonal_weather (enabled, rainy_days_per_season, thunder_chance, storm/clear duration) | LWConfigs, SeasonalWeatherListener |
| **lw-crops.yml** | seasonal_crops (enabled, off_season_growth_chance, crops.*.allowed_seasons) | LWConfigs, SeasonalCropsListener |
| **lw-fauna.yml** | migration (enabled, particles, spawn boost chances, soft_despawn), favored/discouraged per season | LWConfigs, WildlifeMigrationListener |
| **lw-events-extra.yml** | blood_moon, heat_wave, magic_storm, festival, fishing, mining, tornado, restful_sleep (enabled, chances, effects) | LWConfigs, BloodMoonEvent, SummerHeatwaveEvent, MagicStormEvent, FestivalEvent, FishingFestivalEvent, MiningBlessingEvent, TornadoEvent, RestfulSleepEvent |


## 13. Public API

- **RPGCorePlugin.getInstance()** – Singleton.
- **RPGCorePlugin.getService(Class&lt;T&gt;)** – Returns service when module is enabled: ProgressionService, BetonQuestBridge, AuraSkillsBridge, CalendarServiceV2, EventService, PlayerProfileService, ClanService (when clans enabled), BiomeBackupStore.

**Interfaces (extension points):** CalendarServiceV2, EventService, SeasonalEvent, ProgressionService, PlayerProfileService, ClanService, ClusterMessagingService. No separate API JAR; wrappers depend on RPG_Core_V2 and use getService().