# PluginV2 – Complete Feature Inventory Plan (all features + Discord output)

## Goal

1. **Document every feature** in PluginV2 at the **same level of detail as Events**: for each feature area (Core, Infra, Player, Progression, Skills, Calendar, Events, Clans, World, Wrappers, Configs, Commands, API), explain every major class, flow, and “how it works” (like the event-by-event breakdown).
2. **Produce a Discord-pasteable version**: text that uses only Discord-supported formatting so when pasted into a Discord message it renders with **bold**, *italic*, `code`, lists, and clear section breaks. Optionally split by section so each chunk stays under Discord’s 2000-character limit.

---

## 1. Feature areas to cover (same depth as Events)

Each area gets: **what it is**, **main classes/files**, **how it works (flow)**, **config/commands where relevant**, and **notable details**.

| Area | Depth to match |
|------|----------------|
| **Core & module system** | RPGCorePlugin bootstrap, ModuleManager load order, ModuleContext, ServiceRegistry; how modules are enabled from config and how wrappers get services. |
| **Infrastructure** | ConfigService (core.yml, db.yml), DatabaseProvider/SqlExecutor, SchedulerService, ClusterMessagingService (stub); what each does and who uses it. |
| **Player** | PlayerProfile model, PlayerProfileService (cache, load/save), PlayerProfileRepository, PlayerProfilePreloadListener (AsyncPlayerPreLoginEvent), PlayerSessionListener (Join/Quit); full flow from pre-login to quit. |
| **Progression** | ProgressionService (hasUnlocked, unlock, getCounter, incrementCounter), ProgressionRepository, milestones (first_join, join_3_times, join_10_times), ProgressionJoinListener (what it does on join), V2ProgressCommand, V2ClaimCommand, RewardsCommand, BetonQuestBridge (when/how it’s notified). |
| **Skills** | AuraSkillsBridge (lazy API init, addSkillXp, getSkillLevel), SkillsDebugCommand (/v2skills); how XP is granted (via /v2claim only), soft-depend behavior. |
| **Calendar** | CalendarServiceV2 interface and CalendarSnapshot (date, dayCount, season, mcDay), advance/reset, getPlayerJoinDate; CalendarDayAdvancedEvent (when fired, payload); CalendarDayChangeTitleListener (titles, Easter, New Year); EasterWeekHelper, NewYearFireworkHandler, SeasonGuideBook; wrapper commands (/date, /time, /eoc, /season, /nextday, /resetcalendar, /datejoined). |
| **Events** | (Already done.) Event engine (EventServiceImpl, EventContext, cooldowns, cascades, boss bar, BlockRestoreManager, LWConfigs), then **each** DailyWorldEvent and SeasonalEvent: trigger, duration, what it does while active, listeners/tasks, cleanup. Plus SeasonalWeatherListener, WildlifeMigrationListener, SeasonalCropsListener, BiomeBackupStore. |
| **Clans** | ClanService API (create, get, invite, add/remove member, ranks, relations, display); ClanServiceImpl (blocking on repo futures); ClanRepository; Clan and Invite models; ClansModule, ClanCommand, ClanDisplayListener (scoreboard/team prefix). Note: not wired in RPGCorePlugin. |
| **World** | BiomeBackupData, BiomeBackupStore (ChunkLoadEvent, restore queue); how events and /season biomes use it. |
| **Wrappers** | Survival: SurvivalV2Plugin, SurvivalEventCommands, SurvivalCalendarCommands; Amplified: AmplifiedV2Plugin, AmplifiedCalendarCommands, AmplifiedEventCommands; which commands each exposes and which permissions (lw.admin.calendar, lw.admin.events, lw.admin.biomes). |
| **Configs** | config.yml (events.*, disabled_worlds, world-day-offsets, resource-packs), config/core.yml, config/db.yml, lw-climate.yml, lw-crops.yml, lw-fauna.yml, lw-events-extra.yml; which code reads which keys (table or short list per file). |
| **Commands** | All three plugins: RPG_Core_V2 (v2ping, v2progress, v2claim, v2skills, rewards), Survival (date, time, eoc, datejoined, season, nextday, resetcalendar, event list/info/start/stop), Amplified (date, time, eoc, datejoined, season, event list/info); usage and permissions. |
| **Public API** | RPGCorePlugin.getInstance(), getService(Class); list of services (ProgressionService, CalendarServiceV2, EventService, etc.); interfaces as extension points. |

---

## 2. Deliverables

1. **Feature inventory document (source of truth)**  
   - **File:** `Docs/v2/feature-inventory.md`  
   - Full content with all sections above. Can use standard Markdown (e.g. `#` headers) for docs.

2. **Discord-pasteable version**  
   - **File:** `Docs/v2/feature-inventory-discord.txt` (or `.md` with Discord-safe formatting only)  
   - **Format rules:**  
     - **Section headers:** use `**SECTION NAME**` or `──── SECTION NAME ────` (no `#`).  
     - **Bold:** `**text**`  
     - **Italic:** `*text*`  
     - **Code / file / command:** `` `code` ``  
     - **Lists:** `-` or `•`  
     - **Block quote (optional):** `>`  
     - No `#` headers (Discord doesn’t render them as headers).  
   - **Length:** If total length > 2000 characters, add clear `--- PART N —---` (or similar) boundaries so the user can paste part by part into separate Discord messages.  
   - Content: same as feature-inventory.md but with the formatting above and optional “Part 1”, “Part 2” splits.

3. **Optional:** One-line link to the feature inventory in `Docs/v2/implementation-status.md` or `Docs/v2/README.md`.

---

## 3. Execution steps (when approved)

1. Gather any missing detail (e.g. ClanCommand, ClanDisplayListener, wrapper plugin.yml) so every area has events-level depth.  
2. Write `Docs/v2/feature-inventory.md` with all sections (Core through Public API), including the full Events subsection (every event as already specified).  
3. From that content, produce `Docs/v2/feature-inventory-discord.txt`: same structure, Discord-only formatting, section splits for 2000-char paste if needed.  
4. Optionally add a reference to the feature inventory in implementation-status.md or README.md.

---

## 4. Summary

- **Scope:** All feature areas (Core, Infra, Player, Progression, Skills, Calendar, Events, Clans, World, Wrappers, Configs, Commands, API) at **events-level detail**.  
- **Output:**  
  - One full doc: `Docs/v2/feature-inventory.md`.  
  - One Discord-ready doc: `Docs/v2/feature-inventory-discord.txt` (formatting + optional multi-part splitting for paste).
