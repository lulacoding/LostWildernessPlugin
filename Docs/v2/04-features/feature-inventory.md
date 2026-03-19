# PluginV2 – Complete Feature Inventory

Per-feature documentation at full depth. Source of truth for what exists and how it works.

---

## 1. Core and module system

**Location:** `PluginV2/src/main/java/com/lostwilderness/rpgcore/core/`

- **RPGCorePlugin** – Entry point. On enable: loads ConfigService (core.yml, db.yml), creates DatabaseProvider, SchedulerService, StubClusterMessagingService, ServiceRegistry; builds ModuleContext; reads `config/core.yml` → `enabled-modules` and registers modules in order. Registers core commands (v2progress, v2claim, v2skills, rewards, v2ping; /clan when clans enabled). Static `getInstance()` and `getService(Class)` for wrappers.
- **ModuleManager** – Load order: Player → Skills → Calendar → Events → Progression; **Clans** is loaded when `clans` is in `enabled-modules`. Calls `load(ModuleContext)` then `onEnable()` on each RpgModule.
- **RpgModule** – Interface: `load(ModuleContext)`, `onEnable()`, `onDisable()`.
- **ModuleContext / ModuleContextImpl** – Exposes plugin, ConfigService, DatabaseProvider, SchedulerService, ClusterMessagingService, ServiceRegistry.
- **ServiceRegistry** – Typed `get(Class<T>)` / `register(Class<T>, T)` for cross-module services.

Wrappers (Survival, Amplified) obtain services via `RPGCorePlugin.getInstance().getService(Class)`.

---

## 2. Infrastructure

**Config** (`infra/config/`) – **ConfigService** loads `config/core.yml` (environment, server-role, **enabled-modules**) and `config/db.yml` (datasources with jdbc-url, username, password, maximum-pool-size). Copies JAR defaults to data folder if missing. Used by RPGCorePlugin on startup.

**Database** (`infra/db/`) – **DatabaseProvider** builds HikariCP pools from db.yml. **SqlExecutor** provides async query/update helpers. Used by player, progression, calendar, clans.

**Scheduler** (`infra/scheduler/`) – **SchedulerService** wraps Bukkit scheduler: runSync, runAsync, runSyncDelayed, runSyncRepeating.

**Messaging** (`infra/messaging/`) – **ClusterMessagingService** interface; **StubClusterMessagingService** no-op implementation for single-server.

---

## 3. Player module

**Location:** `com.lostwilderness.rpgcore.player/`

- **PlayerProfile** – Model (UUID, first-join/created, lastSeenAt, etc.).
- **PlayerProfileService** – Cache (ConcurrentHashMap). `loadProfileAsync(uuid)` (from async context): repo findById, create profile if absent, put in cache. `getProfile(uuid)` (sync): cache only. `saveProfileAsync(uuid)`: set lastSeenAt, schedule async save then evict from cache.
- **PlayerProfileRepository** – DB load/save; creates table if not exists.

**PlayerProfilePreloadListener**

- **When it runs:** On **AsyncPlayerPreLoginEvent** (async, before login completes).
- **What it does:** Calls `PlayerProfileService.loadProfileAsync(uuid)` and waits up to **5 seconds** for the future to complete. If the profile loads (or is created), the player is allowed to continue. If the future does not complete in time or fails, the listener disallows login and sets a kick message.
- **Config / cleanup:** No config keys; timeout is hardcoded (5s). No world/block state.

**PlayerSessionListener**

- **When it runs:** **PlayerJoinEvent** (sync): after pre-login, profile should already be in cache; if missing, loads async. **PlayerQuitEvent** (sync): before player is fully disconnected.
- **What it does:** On join: ensures profile is in cache (if not present, triggers async load so it is available soon). On quit: calls `saveProfileAsync(uuid)` — sets lastSeenAt on the profile, schedules an async save to the repository, then evicts the profile from the cache when the save completes.
- **Cleanup:** No listeners or tasks to cancel; eviction happens in the save callback.

**Flow:** Pre-login load (5s timeout) → join ensures cache → quit save and evict. H2 or MySQL via db.yml (player datasource).

---

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

---

## 5. Skills module

**Location:** `com.lostwilderness.rpgcore.skills/`

- **AuraSkillsBridge** – Lazy init: AuraSkillsApi.get() on first use. `isAvailable()`, `addSkillXp(uuid, skillKey, amount)` (online only), `getSkillLevel(uuid, skillKey)` (e.g. Fighting). Soft-depend on AuraSkills; no-op if unavailable.
- **SkillsDebugCommand** – `/v2skills`. Player-only; shows AuraSkills fighting level.

Skill XP is granted only via **/v2claim** milestone rewards, not on join.

---

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

---

## 7. Events module

### 7.1 Engine and context

**Location:** `com.lostwilderness.rpgcore.events/`

- **EventServiceImpl** – Listens **CalendarDayAdvancedEvent**. For each overworld: if TPS < config threshold, skip dispatch; else end any active daily events, then call each **DailyWorldEvent.onCalendarDay(world, effectiveDay)** (effectiveDay = day + world-day-offset). Runs seasonal start/queue/end and cascade triggers. Registers as Listener; also **Runnable** every 40 ticks: active SeasonalEvent.onTick(), EventBossBarManager.tick().
- **EventContext** – Overworlds list, disabled worlds, world-day-offsets, current world for dispatch.
- **EventCooldownTracker** – Per-event cooldown in days (from config `events.cooldowns.<EventName>`).
- **EventCascadeRegistry** – e.g. Eclipse → Paranoia (chance), Eclipse → Thunder (eclipse-chance-pct).
- **EventBossBarManager** – Shows active event display names to overworld players when config enabled.
- **BlockRestoreManager** – Keys (e.g. "frost", "blizzard"); record block state, restore later.
- **LWConfigs** – Loads lw-climate.yml, lw-crops.yml, lw-fauna.yml, lw-events-extra.yml from data folder.

### 7.2 Supporting listeners

- **SeasonalWeatherListener** – CalendarDayAdvancedEvent: if enabled (config + lw-climate), set overworld storm/clear and thunder from season (rainy days, thunder chance, duration).
- **WildlifeMigrationListener** – CreatureSpawnEvent: if migration enabled (lw-fauna), cancel discouraged spawns; in winter optionally cancel warm-climate spawns; for favored types can spawn extra entity + particles (boost chance per season).
- **SeasonalCropsListener** – BlockGrowEvent, BlockFertilizeEvent: if seasonal crops enabled (lw-crops), allow growth only in configured seasons per crop; off-season uses off_season_growth_chance or cancel.
- **BiomeBackupStore** – ChunkLoadEvent: in restore mode enqueue chunk backups; restore task processes queue. Used by events and Survival `/season biomes`.

### 7.3 DailyWorldEvent and SeasonalEvent – each event (detailed)

Boss bar text comes from each event's `getDisplayName()`; EventBossBarManager aggregates and ticks.

---

**ParanoiaEvent** — display: "Paranoia"

- **When it starts:** Only when Eclipse is already active. On each calendar day tick EventServiceImpl calls `onCalendarDay`; if Eclipse is active and Paranoia isn't, Paranoia starts. No separate chance/season check for Paranoia itself (cascade from Eclipse). Cascade chance is in `events.paranoia.chance` (EventsModule).
- **Duration:** One MC night: 24000 ticks (~20 min). A delayed task calls `forceEndEarly()` after 24000 ticks.
- **While active:** Listener for **PlayerJoinEvent** / **PlayerQuitEvent**. For each online overworld player it schedules repeating tasks that play random horror sounds (default: AMBIENT_CAVE, ENTITY_ENDERMAN_STARE, ENTITY_WITHER_AMBIENT; or config `events.paranoia.sounds` list) at intervals from `events.paranoia.min-interval-seconds` to `max-interval-seconds` (default 20–60). After each sound the task reschedules itself with a new random delay. New joiners get the same sound loop; leavers have their task cancelled.
- **End / cleanup:** After 24000 ticks the stop task runs `forceEndEarly()`: sets `active = false`, cancels all per-player sound tasks and the stop task. No world/block/mob state to restore.

---

**BloodMoonEvent** — display: "Blood Moon"

- **When it starts:** Each calendar day, overworld only. If enabled (`blood_moon.enabled` in lw-events-extra or `events.blood-moon.enabled` in config.yml), day ≥ min_day (default 10), and a random roll < chance_per_night (default 0.08). Config from lw-events-extra preferred over main config.
- **Duration:** One MC night: 24000 ticks. End task runs at 24000 ticks, then sets dawn (time 6000) and restores mobs.
- **While active:** Optional night freeze: `blood_moon.freeze_night` / `world_time_night` (default true) sets world time to 18000; if freeze_night, `DO_DAYLIGHT_CYCLE = false`. Players get title "§4Blood Moon" / "You cannot sleep tonight…". **CreatureSpawnEvent:** overworld monsters get PDC-stored original health/damage/name, then health/damage multiplied (default 1.5×), custom name "§4Blood Moon …", tag `lw_blood_moon`. **PlayerBedEnterEvent:** sleep cancelled. **ChunkLoadEvent:** when event is not active, restores any loaded mobs that still have the Blood Moon tag.
- **End / cleanup:** End task re-enables daylight cycle if frozen, sets time 6000, calls `restoreAllBloodMoonMobs()`, then sets `active = false`. `forceEndEarly()` does the same restore + daylight on all overworlds and cancels the end task.

---

**MagicStormEvent** — display: "Magic Storm"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`magic_storm.enabled`, default true), random < `base_chance_per_day` (default 0.08), and current season is in `magic_storm.allowed_seasons`.
- **Duration:** One MC day: 24000 ticks. End task clears weather and restores storm mobs.
- **While active:** Sets overworld storm + thundering; announces "A magical storm sweeps across the land…". **CreatureSpawnEvent:** overworld monsters have `rare_mob_chance` (default 0.35) to become "storm" mobs — PDC stores original stats, then glowing, Speed I, Strength I, +25% health/damage, name "§dStorm …", tag `lw_magic_storm`. **EntityDeathEvent:** killing a storm mob has `extra_loot_chance` (default 0.40) to drop enchanted book or enchanted golden apple. **ChunkLoadEvent:** when event is not active, restores any loaded storm-tagged mobs.
- **End / cleanup:** End task turns off storm/thunder, calls `restoreAllStormMobs()`, sets `active = false`. `forceEndEarly()` does the same plus clears storm/thunder on all overworlds.

---

**FishingFestivalEvent** — display: "Fishing Festival"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`fishing.enabled`, default true) and roll < `base_chance_per_day` (default 0.10).
- **Duration:** One MC day: 24000 ticks. End task only sets `active = false`.
- **While active:** Message "Fishing Festival! Better catches and treasure today." **PlayerFishEvent** (state CAUGHT_FISH): config `treasure_chance` (default 0.35) and `double_catch_chance` (default 0.30); on double catch sends "Double catch! (Fishing Festival)".
- **End / cleanup:** After 24000 ticks or `forceEndEarly()`: cancel end task, set `active = false`. No world/mob cleanup.

---

**MiningBlessingEvent** — display: "Miner's Blessing"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`mining.enabled`, default true) and roll < `base_chance_per_day` (default 0.12).
- **Duration:** One MC day: 24000 ticks. End task only sets `active = false`.
- **While active:** Message "Miner's Blessing! Extra ore and XP from mining today." **BlockBreakEvent:** when breaking configured ore types, chance for extra ore drop (`extra_ore_chance` default 0.35) and for bonus XP (`exp_boost_chance` default 0.40).
- **End / cleanup:** After 24000 ticks or `forceEndEarly()`: cancel end task, set `active = false`. No block/mob cleanup.

---

**RestfulSleepEvent** — display: "Restful Sleep"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`restful_sleep.enabled`, default true) and roll < `base_chance_per_day` (default 0.10).
- **Duration:** One MC day: 24000 ticks. End task only sets `active = false`.
- **While active:** Message "Restful Sleep night—sleep well for a health boost!". **PlayerBedLeaveEvent:** when leaving bed during event, grants Absorption and Regeneration (duration from `health_boost_seconds`, default 600; regen capped at 30s) and message "You feel well rested!".
- **End / cleanup:** After 24000 ticks or `forceEndEarly()`: cancel end task, set `active = false`. No cleanup (potions are time-limited).

---

**EclipseEvent** — display: "Eclipse"

- **When it starts:** Each calendar day, overworld, when `(day - offset) % interval == 0` (e.g. `events.eclipse.interval-days` 30, `events.eclipse.offset-days` 0) and a chance roll < `events.eclipse.chance-pct` (default 80). Day must be ≥ offset.
- **Duration:** One MC night: 24000 ticks. End task at 24000 ticks restores daylight and time.
- **While active:** Optional resource pack from `resource-packs.eclipse` applied to overworld players; daylight cycle off; time 18000; message "A lunar eclipse blankets the sky…". **EclipseHordeTask** runs every 6000 ticks: spawns hostile mobs near players (region 80 blocks, `events.eclipse.horde-size` e.g. 8). No extra Bukkit listeners on the event class itself.
- **End / cleanup:** End task re-enables daylight, sets time 6000, optionally applies `resource-packs.eclipse-empty`, message "The eclipse fades… dawn breaks anew.", cancels horde task, sets `active = false`. `forceEndEarly()` re-enables daylight on all overworlds and cancels horde and end tasks.

---

**BlizzardEvent** — display: "Blizzard"

- **When it starts:** Any calendar day, overworld; roll < `events.blizzard.chance-pct` (default 25). No season filter in code.
- **Duration:** Until next sunset (ticks until time 13000 from current time). End task runs at that delay.
- **While active:** Storm on (thundering off), message "A vicious winter blizzard howls… Only snowy biomes are affected!". For each loaded chunk: one random surface block can spawn a Stray; surface water blocks are recorded with BlockRestoreManager (key "blizzard") and set to ice.
- **End / cleanup:** End task calls `blockRestore.restoreAndClear("blizzard")`, clears storm, message "The blizzard subsides and the ice melts away.", sets `active = false`. `forceEndEarly()` restores blizzard ice and clears storm on overworlds.

---

**FogEvent** — display: "Fog"

- **When it starts:** Any calendar day, overworld; roll < `events.fog.chance-pct` (default 20). Broadcast "A rolling fog envelops the land…".
- **Duration:** Config `events.fog.restore-delay-ticks` (default 6000). No view-distance or ProtocolLib effect in the class.
- **While active:** No listeners or ongoing tasks; only the start broadcast and the delayed end.
- **End / cleanup:** After delay: broadcast "The fog lifts, and clarity returns.", set `active = false`. `forceEndEarly()` cancels the task and sets `active = false`.

---

**SummerHeatwaveEvent** — display: "Heatwave"

- **When it starts:** Only in **SUMMER** (calendar season); overworld; roll < `events.heatwave.chance-pct` (default 25). Config also in lw-events-extra (`heat_wave.*`). Duration until next sunset (ticks until 13000).
- **Duration:** Until next sunset. End task runs at that delay.
- **While active:** Message "A scorching heatwave grips hot biomes! …". On start: in desert/savanna (BiomeGroups), farmland moisture set to 0, husks spawned. Repeating task (every 100 ticks): in hot biomes apply Hunger; if player exposed to sky and has fewer than min_armor_protection armor pieces, apply Weakness and optional exposure damage. lw-events-extra: `heat_wave.damage_exposed`, `apply_weakness`.
- **End / cleanup:** End task removes Hunger in hot biomes, message "The heatwave subsides…", cancels hunger task, sets `active = false`. Farmland is not re-moistened.

---

**TornadoEvent** — display: "Tornado"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`tornado.enabled`, default true), world not in `tornado.disabled_worlds`, and roll < `base_chance_per_day` (default 0.05). Center spawns at configurable distance from a random online player; drift from `tornado.drift_speed`.
- **Duration:** Random real-time minutes: `tornado.min_duration_minutes`–`tornado.max_duration_minutes` (default 5–10). A run task checks `System.currentTimeMillis() >= endTime` each tick.
- **While active:** Message "A tornado has been sighted! Seek shelter." Repeating task every 5 ticks: moves center by drift and re-anchors Y to highest block; in radius (`tornado.radius_blocks`, default 10) damages players without roof, pulls non-players toward center, 20% chance to break wheat/carrots/potatoes/beetroots; spawns cloud particles at center.
- **End / cleanup:** When `System.currentTimeMillis() >= endTime`, sets `active = false`, message "The tornado has dissipated.", cancels run task. `forceEndEarly()` sets `active = false`, cancels task. No block or entity restore.

---

**FestivalEvent** — display: "Festival"

- **When it starts:** Each calendar day, overworld, if enabled in lw-events-extra (`festival.enabled`, default true) and roll < `festival.base_chance_per_day` (default 0.10).
- **Duration:** One MC day: 24000 ticks. End task only sets `active = false`.
- **While active:** Message "A festival is in town! Traders have special offers." No listeners or gameplay effects in code (flavor only; villager/trader PDC and custom trades not implemented).
- **End / cleanup:** After 24000 ticks or `forceEndEarly()`: cancel end task, set `active = false`.

---

**NewYearEvent** — display: "New Year"

- **When it starts:** Only on **first day of Spring** (March 1) from calendar snapshot, overworld, and only for the first overworld; `events.new-year.enabled` (default true).
- **Duration:** Instant for the main effect; then an optional "firework phase" for `events.new-year.firework-phase-duration-ticks` (default 60), after which `active` is set false in the same day tick.
- **While active:** Title "Happy New Year!" / "Spring has begun! Enjoy the festivities."; gift fireworks and gunpowder to all overworld players. Firework phase: every 40 ticks, spawn a celebration firework at a random overworld player. Then `active = false` is set in the same day tick.
- **End / cleanup:** `forceEndEarly()` cancels firework phase task and sets `active = false`. No world/mob cleanup.

---

**HolidayEvent** — display: "Holiday"

- **When it starts:** Calendar date only: **Halloween** (Oct 31) if `events.holiday.halloween.enabled`, or **Christmas** (Dec 25) if `events.holiday.christmas.enabled`; only for the first overworld.
- **Duration:** Single day-tick: effects run once, then `active = false` the same tick.
- **While active:** **Halloween:** title "Halloween" / "Spooky spirits are about…"; for each overworld player, in radius `events.holiday.halloween.mob-radius` (24), random chance `pumpkin-chance` (0.02) to place carved pumpkin on solid blocks. **Christmas:** title "Merry Christmas!" / "Peace and joy to you."; heal by `events.holiday.christmas.heal-amount` (2.0), give cookies and snowballs. No ongoing tasks.
- **End / cleanup:** `forceEndEarly()` only sets `active = false`. No removal of pumpkins or items.

---

**ThunderEvent** — display: "Thunderstorm"

- **When it starts:** **Daily:** overworld, roll < `events.thunder.base-chance-pct` (default 15). **Cascade:** can be triggered by Eclipse via EventCascadeRegistry with `events.thunder.eclipse-chance-pct` (default 40) for "eclipse storm" with undead cavalry.
- **Duration:** Until 6 AM (ticks until time 6000). End task clears storm/thunder and broadcasts "The thunderstorm subsides…".
- **While active:** Storm + thundering on. Normal: broadcast "A thunderstorm rolls across the land". Eclipse mode: broadcast "Thunderstorm Eclipse! Undead cavalry…", spawn zombie horses with golden-armored zombie riders (count `events.thunder.horses-per-eclipse`). No ongoing listeners beyond the end task.
- **End / cleanup:** End task clears storm/thunder, sets `active = false`, broadcast. `forceEndEarly()` cancels end task and clears storm/thunder on overworlds.

---

**JungleMonsoonEvent** — display: "Jungle Monsoon"

- **When it starts:** Only in **SUMMER**; overworld; roll < `events.jungle-monsoon.chance-pct` (default 20). Start is delayed by ticks until sunset (13000); actual "run" begins at sunset.
- **Duration:** From sunset until 6 AM (ticks until 6000 from world time when monsoon runs). End task resets player weather and sets `active = false`.
- **While active:** Message "A steamy jungle monsoon begins! (Only jungle biomes are affected!)". In jungle biomes (BiomeGroups): set player weather to DOWNFALL; in loaded jungle chunks spawn slimes (2–5 per chunk); near jungle players spawn parrots (1–3 per player). Repeating logic only at start; duration is the end-task delay.
- **End / cleanup:** End task resets player weather for jungle players, message "The jungle monsoon eases into a gentle mist…", sets `active = false`. `forceEndEarly()` resets weather for all overworld jungle players and sets `active = false`.

---

**SeasonalStormEvent** — display: "Seasonal Storm"

- **When it starts:** Only in **late autumn or winter** (month ≥ 10 or ≤ 2); overworld; roll < `events.seasonal-storm.chance-pct` (default 25).
- **Duration:** Until 6 AM (ticks until time 6000). End task clears storm/thunder and cancels lightning task.
- **While active:** Broadcast "A seasonal storm rolls in…", storm + thundering. **Clustered lightning:** once at start and then every 3 MC minutes: per 100-block cluster around players, try to strike log/leaves blocks (and set fire on block above) or else strike player location.
- **End / cleanup:** End task clears storm/thunder, cancels lightning task, broadcast "The seasonal storm subsides…", sets `active = false`. `forceEndEarly()` cancels both tasks, clears storm/thunder on overworlds, same message.

---

**FrostEvent** — display: "Frost"

- **When it starts:** Only in **WINTER**; overworld; roll < `events.frost.chance-pct` (default 30). Start is delayed until `events.frost.target-tick` (default 13000 = sunset).
- **Duration:** From target tick until next sunrise (24000 - target_tick). End task runs at that delay.
- **While active:** Message "A biting frost sweeps across the cold biomes…". After delay: in cold biomes (BiomeGroups) freeze surface water (BlockRestoreManager key "frost"), apply Slowness and Mining Fatigue (1 min), falling-dust snow particles per player; repeating task (every 5 ticks) reapplies effects in cold and removes them when near heat (lava, campfire, magma, torch, etc.); optional frost damage when exposed with fewer than `events.frost.min-armor-pieces-to-avoid-damage` (default 2). Config: `events.frost.damage-when-exposed`, `frost-damage-per-tick`.
- **End / cleanup:** End task restores frost ice via `blockRestore.restoreAndClear("frost")`, removes Slowness/Mining Fatigue and particle tasks for all players, message "The frost melts away as the sun rises.", sets `active = false`. `forceEndEarly()` restores frost blocks, removes all effects and overlay tasks, same message.

---

**SpringBloomEvent** — display: "Spring Bloom"

- **When it starts:** Only on **first day of Spring** (March 1) from calendar; overworld.
- **Duration:** Single day-tick; no sustained duration. Sets `active = true` then immediately runs effects and sets `active = false`.
- **While active:** Message "Spring Has Sprung! Flowers bloom everywhere (in spring biomes)!". For all loaded chunks: age up Ageable blocks (e.g. crops) by 1. For each player, near flowers (poppy, dandelion, sunflower, azure bluet) spawn bees (5 attempts). No listeners or recurring tasks.
- **End / cleanup:** `forceEndEarly()` only sets `active = false`. No block/entity restore.

---

**AutumnLeafFallEvent** — display: "Autumn Leaf Fall"

- **When it starts:** Only in **AUTUMN** (any day); overworld. No chance roll; runs every autumn day.
- **Duration:** Single day-tick; no sustained duration. Sets `active = true`, runs effects, then `active = false`.
- **While active:** Message "Autumn has arrived! Leaves drift from the trees (in autumn biomes)!". For each player, spawn 30 falling-dust particles (oak leaves) in a Gaussian spread. No listeners or recurring tasks.
- **End / cleanup:** `forceEndEarly()` only sets `active = false`. No cleanup.`

---

**TestEvent** (SeasonalEvent) — id: "test", display: "Test Event"

- **When it starts:** Never by calendar: `canStartToday` always returns false. Can be force-started via command (e.g. `/event start test` on Survival). All seasons allowed (`isSeasonAllowed` true).
- **Duration:** 1 day (min and max duration days = 1).
- **While active:** On start, sends eligible players "Test event started (day N)." No listeners or gameplay logic in the class.
- **End / cleanup:** On end, sends "Test event ended." No world/mob cleanup.

Boss bar text comes from each event’s getDisplayName(); EventBossBarManager aggregates and ticks.

---

## 8. Clans module

**Location:** `com.lostwilderness.rpgcore.clans/`

Enabled when `clans` is in `config/core.yml` → enabled-modules. Uses player datasource from db.yml.

- **ClanService** – createClan(name, colorHex, leaderUuid), getClanById/ByName, getClanOfPlayer(uuid), invitePlayerToClan, getPendingInvites(uuid), addMember/removeMemberFromClan, updateClanColor, getClanMemberCount, getMemberRank, addClanRelation, removeClanAndData, getAllClans, getClanMembersWithRanks, removeInvitesForPlayer, updatePlayerDisplay(player), refreshClanDisplay(clanId).
- **ClanServiceImpl** – Blocks on repo CompletableFutures (10s timeout). Scoreboard: main scoreboard; team id = clan_ + first 8 hex of clan UUID; prefix "[ClanName] " with clan hex color (Adventure Component); add player to team. updatePlayerDisplay: remove player from all teams then add to clan team if in clan. refreshClanDisplay: updatePlayerDisplay for each online member.
- **ClanRepository** – Tables: clans, clan_members, clan_invites, clan_relations. createTablesIfNotExists(), saveClan, getClanById/ByName, addMember, getClanOfPlayer, createInvite, getPendingInvites, etc.
- **Clan, Invite** – Models (id, name, colorHex; clanId, playerUuid, inviterUuid, timestamp).
**ClanCommand** (permission **lw.clan.use**)

- **create** &lt;name&gt; &lt;#hex&gt; — When: leader/caller runs it. Creates clan (name 1–32 chars, hex e.g. #FFAA00), saves to repo, adds caller as Leader; then **updatePlayerDisplay** for the caller.
- **invite** &lt;player&gt; — Leader invites an online player; creates invite in repo.
- **accept** — Player accepts first pending invite; adds player to clan as Member; **updatePlayerDisplay**; removes invite.
- **deny** — Clears all pending invites for the player.
- **leave** — Removes member from clan; if no members left, **removeClanAndData**; otherwise **updatePlayerDisplay** for leaver.
- **color** &lt;#hex&gt; — Leader sets clan color; repo update then **refreshClanDisplay(clanId)** so all online members get the new prefix color.
- **promote** / **demote** &lt;player&gt; — Leader changes member rank (Leader/Member); cannot demote self. Repo update then **updatePlayerDisplay** for the target.
- **enemy** / **opposition** &lt;clanName&gt; — Leader declares target clan as enemy (relation in repo).
- **info** [clanName] — Shows own clan or named clan: name, color, members with ranks.
- **list** — Lists all clans with name, color, member count.

**ClanDisplayListener**

- **When it runs:** On **PlayerJoinEvent** only.
- **What it does:** Calls `clanService.updatePlayerDisplay(e.getPlayer())`. That removes the player from every scoreboard team they are in (cleanup from old clans), then if the player is in a clan: gets or creates the clan team on the **main scoreboard** (team id = `clan_` + first 8 hex chars of clan UUID), sets **prefix** to `[ClanName] ` with the clan hex color (Adventure Component), sets team color, and adds the player to the team. If not in a clan, no team is added. Display is also refreshed by **ClanCommand** after create, accept, leave, color, promote, demote.

---

## 9. World module

**Location:** `com.lostwilderness.rpgcore.world/`

- **BiomeBackupData** – Data for biome backup/restore.
- **BiomeBackupStore** – Listens ChunkLoadEvent; in restore mode enqueues chunk backups; restore task processes queue. Used by events (biome painting) and Survival `/season biomes` (backup on/off, restore, painting).

---

## 10. Wrappers

**LW-Survival-V2** (`survival-plugin/`) – Depends on RPG_Core_V2. Commands: date, time, eoc, datejoined, season (guide, biomes with lw.admin.biomes), nextday, resetcalendar (lw.admin.calendar), event list|info|start|stop (start/stop require lw.admin.events). Permissions in plugin.yml: lw.admin.calendar, lw.admin.events; lw.admin.biomes checked in code only.

**LW-Amplified-V2** (`amplified-plugin/`) – Same calendar/season/datejoined; event list|info only (no start/stop). No permission block in plugin.yml.

---

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

---

## 12. Commands

**RPG_Core_V2** (plugin.yml): **v2ping** – test; **v2progress** – list unlocked milestones (player); **v2claim** &lt;milestone&gt; – claim reward (player); **v2skills** – AuraSkills fighting level debug (player); **rewards** – open BetonQuest milestone menu (player). **clan** – full clan subcommands when clans module enabled (lw.clan.use).

**LW-Survival-V2:** date, time, eoc, datejoined, season, season guide, season biomes (lw.admin.biomes), nextday (lw.admin.calendar), resetcalendar (lw.admin.calendar), event list|info|start|stop (lw.admin.events for start/stop).

**LW-Amplified-V2:** date, time, eoc, datejoined, season, season guide, event list|info.

---

## 13. Public API

- **RPGCorePlugin.getInstance()** – Singleton.
- **RPGCorePlugin.getService(Class&lt;T&gt;)** – Returns service when module is enabled: ProgressionService, BetonQuestBridge, AuraSkillsBridge, CalendarServiceV2, EventService, PlayerProfileService, ClanService (when clans enabled), BiomeBackupStore.

**Interfaces (extension points):** CalendarServiceV2, EventService, SeasonalEvent, ProgressionService, PlayerProfileService, ClanService, ClusterMessagingService. No separate API JAR; wrappers depend on RPG_Core_V2 and use getService().