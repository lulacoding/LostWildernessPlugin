## RPG_Core_V2 – Architecture Plan for AAA-Scale Network

This document defines a modern, modular architecture for `RPG_Core_V2`, designed to support a **large network of Paper servers** (tens to hundreds of backends) and **hundreds of thousands of total players** across the network. It reuses the strongest concepts from the current codebase (calendar, events, portals, progression, reputation, skills, clans/war) while restructuring them into clean, scalable services and modules.

The old plugins remain **read-only reference**; all new work happens under `RPG_Core_V2` and its supporting infrastructure.

---

## 1. High-Level Goals

- **Network-scale design**
  - Treat each Paper server (Survival-1, Amplified-1, Lobby, Instances, etc.) as a **node** in a larger cluster.
  - Use shared infrastructure (DB, cache, messaging bus) so player state and global systems are consistent across nodes.

- **Strict main-thread protection**
  - All heavy work (DB I/O, large queries, complex computations) runs **off the main thread**.
  - Bukkit/Paper APIs that require main-thread access are wrapped in minimal sync tasks.

- **Modular gameplay**
  - Features are split into **independent modules** (Items, Quests, NPCs, Economy, Clans, Events, Portals, etc.) loaded via a `ModuleManager`.
  - Adding a new sword, quest, or NPC is done by adding a new small class/config and registering it, not editing god-classes.

- **Explicit domain boundaries**
  - Replace `CalendarSyncManager`-style god-objects with **small, focused services**:
    - `CalendarService`, `PlayerProfileService`, `ClanService`, `ReputationService`, `SkillService`, `EconomyService`, `QuestService`, `PortalService`, etc.
  - Each service owns a clear data model and async persistence strategy.

- **Horizontally scalable persistence**
  - Use per-domain repositories with async APIs and proper connection pooling.
  - Support sharding (per-region DBs) and caching (Redis or similar) where necessary.

- **Testability & observability**
  - Use interfaces and DI for services.
  - Add metrics hooks and structured logging for load and performance analysis.

---

## 2. Network Topology & Deployment Model

Although `RPG_Core_V2` is a plugin running on Paper, its architecture must fit into a **network-scale deployment**:

- **Server roles**
  - **Lobby / Social Hubs**: run lightweight `RPG_Core_V2` instances focused on authentication, cosmetics, and routing.
  - **Game backends (Survival-1, Amplified-1, future shards)**: run the full RPG mechanics stack (items, skills, quests, events, clans, etc.).
  - **Instance/arena servers (optional)**: dedicated nodes for large events, raids, or bosses; share the same core services but with fewer features loaded.

- **Shared infrastructure (outside the plugin)**
  - **Relational database** (MySQL/MariaDB or Postgres) for canonical state; tables are **normalized per domain** instead of one giant module.
  - **Redis or similar cache** (optional but recommended for AAA scale) for hot player/session data, cooldowns, and cross-node coordination.
  - **Message bus** (e.g., Redis pub/sub, RabbitMQ, Kafka, or even extended Bungee/Velocity messaging) for cross-server events:
    - Global announcements, cross-server parties, match-making, boss triggers, etc.
  - **HTTP/gRPC service layer** (optional) for non-Minecraft consumers (web dashboards, Discord bot, admin tools).

`RPG_Core_V2` integrates with this infrastructure via its `infra` layer.

---

## 3. RPG_Core_V2 Package & Module Layout

Proposed root package: `com.lostwilderness.rpgcore` (adjust to your standard).

### 3.1 Core Bootstrap & Module Management

- `core/`
  - `RPGCorePlugin`
    - Minimal Paper entrypoint:
      - Initializes infra services (config, logging, scheduler, db, cache, messaging).
      - Constructs the `ModuleManager`.
      - Registers global listeners that are strictly bootstrap-oriented.
    - Exposes **only** stable interfaces to other plugins if needed (not to be used as a god-singleton).
  - `ModuleManager`
    - Responsible for registering, enabling, disabling, and reloading modules.
    - Manages a set of `RpgModule` instances:
      - `interface RpgModule { void onLoad(Context ctx); void onEnable(); void onDisable(); }`
    - Modules receive a typed `Context` with access only to the services they need.

- `core.module/`
  - `RpgModule`
  - `ModuleContext`
  - `ModuleDescriptor` (name, dependencies, ordering constraints).
  - Support for **module dependency graph** (e.g., `QuestsModule` depends on `PlayerModule`, `ItemsModule`).

### 3.2 Infra Layer

- `infra/config`
  - `ConfigService` – typed config access with per-module sections (`player.yml`, `items.yml`, `quests.yml`, etc.).

- `infra/db`
  - `DatabaseProvider` – central connection pool provider (Hikari) with:
    - Support for multiple logical schemas or DBs (e.g., `player`, `clan`, `logs`).
  - `SqlExecutor` – async query/update helpers returning `CompletableFuture<T>`.
  - Each domain gets its own repository interface (see below), not direct JDBC callers.

- `infra/cache`
  - Optional Redis-backed cache abstractions:
    - `CacheClient`
    - `DistributedLockManager` for cross-node locks (e.g., unique boss spawns).

- `infra/scheduler`
  - `SchedulerService`
    - `runSync(Runnable)`
    - `runAsync(Runnable)`
    - `runSyncDelayed(...)`, `runAsyncDelayed(...)`, `runRepeating(...)`
  - Wraps Bukkit scheduler, ensuring:
    - All DB operations go through async methods.
    - Only minimal sync tasks for Bukkit-only calls.

- `infra/messaging`
  - `ClusterMessagingService` – abstraction over Redis pub/sub or proxy messaging (Velocity/Bungee).
    - Topic-based event distribution: `PLAYER_PROFILE_FLUSHED`, `GLOBAL_EVENT_STARTED`, `CLAN_WAR_DECLARED`, etc.

---

## 4. Player & Profile System

### 4.1 Domain Model

- `player/`
  - `PlayerProfile`
    - Contains:
      - **Core identity**: UUID, last seen, linked account IDs.
      - **Sub-documents** or references:
        - `ProgressionState` (milestones/achievements).
        - `SkillStateMap` (per skill XP/level).
        - `ReputationState` (per faction).
        - `EconomyState` (wallets, currencies).
        - `StoryState` (quest progress, chapter flags, personalities).
      - `ServerSpecificState` (for per-backend info if needed).
  - `PlayerProfileService`
    - Single point of access to player domain state.
    - Responsibilities:
      - Load profiles **asynchronously** on `AsyncPlayerPreLoginEvent`.
      - Expose them synchronously on main thread after login via a thread-safe cache.
      - Persist profiles (or per-domain deltas) asynchronously on:
        - `PlayerQuitEvent`.
        - Periodic flush.
        - High-value checkpoints (quest completion, big purchases).
    - API:
      - `CompletableFuture<PlayerProfile> loadProfileAsync(UUID)`.
      - `Optional<PlayerProfile> getProfile(UUID)` (cache only).
      - `void saveProfileAsync(UUID)` / `saveAllDirtyAsync()`.
  - `PlayerSessionManager`
    - Tracks online sessions, ensures lifecycle callbacks fire.

### 4.2 Repositories

- `player.repo/`
  - `PlayerProfileRepository`
    - Responsible for reading/writing **base profile rows**.
  - Domain-specific repositories (see below) are coordinated by `PlayerProfileService`, not used directly from listeners.

### 4.3 AAA-Scale Considerations

- **Async from the very start**
  - Never hit DB on main thread for profile data.
  - Warm everything on `AsyncPlayerPreLoginEvent`.
- **Sharding & partitioning**
  - Support multiple DB hosts or schemas per region/world group.
  - Profile keys (`UUID`) are stable partition keys.
- **Caching**
  - Use in-process caches per server plus optional shared cache for cross-node introspection.

---

## 5. Domain Modules & Services

Each major subsystem becomes a **module + service set** instead of being bundled into `CalendarSyncManager`.

### 5.1 Calendar & World Events

- `calendar/`
  - `CalendarService`
    - Responsible for:
      - Global in-game date, year, and season.
      - Leader/follower semantics:
        - One designated leader node writes to DB and emits clock ticks.
        - Followers subscribe via messaging or poll DB with backoff.
  - `CalendarRepository`
  - `SeasonModel` – formalizes seasons, eclipse cycles, etc.

- `events/`
  - `WorldEvent` (interface) – similar to V1 but stateless where possible.
  - `EventEngine`
    - Schedules events based on calendar, player preference, and backend role.
    - Hosts:
      - `EventRegistry`
      - `EventCascadeRegistry` (generalizing V1 cascades).
  - `PlayerEventPreferenceService`
    - Owns per-player event preference data (mapped into profile).

### 5.2 Economy

- `economy/`
  - `EconomyService`
    - Abstracts multi-currency wallets, similar to V1 `WalletService` but:
      - Fully async DB writes.
      - Strictly async loads (no synchronous fallback).
      - Optional eventual-consistency caches with versioning.
  - `WalletRepository`
  - `TransactionLogRepository`
  - `ShopService` & `ShopRepository`
    - Generalize the `shop_items` table into a full-fledged shop system.

### 5.3 Reputation & Factions

- `reputation/`
  - `ReputationService`
    - Manages faction/standing for each player.
    - Provides high-level APIs like `addReputation(UUID, Faction, int, Reason)` with async persistence.
  - `ReputationRepository`
  - Able to plug into quests, bosses, world events, and NPC dialogues.

### 5.4 Skills

- `skills/`
  - `SkillService`
    - Defines a registry of skills and their XP curves (improving V1’s `100*level^1.5` into configurable formulas).
    - Handles XP gain, level-ups, rewards, and integration with items/quests.
  - `SkillRepository`
  - Uses the same async, cache-first pattern as `PlayerProfileService`.

### 5.5 Progression, Milestones, Quests

- `progression/`
  - `ProgressionService`
    - Generalizes V1 `Milestone` into:
      - `AchievementKey` (namespaced IDs, e.g., `story:el_diablo`, `boss:roofwither_kills_10`).
    - Provides gating APIs for features.
  - `ProgressionRepository`

- `quests/`
  - `QuestModule`
  - `QuestService`
    - Quest definitions are data-driven (YAML/JSON) with:
      - Objectives (kill, collect, visit, talk, craft, etc.).
      - Conditions (reputation, skill levels, milestones).
      - Rewards (items, XP, currency, reputation, progression flags).
    - Quest state is stored in dedicated tables, mapped into `PlayerProfile`.
  - `QuestRepository`

### 5.6 Clans, Alliances, Wars

- `clans/`
  - `ClanService`, `AllianceService`, `WarService`
    - Re-express V1 DB schema with proper aggregation boundaries.
    - Provide APIs for:
      - Clan membership, roles, and permissions.
      - Alliance structures and relations.
      - War declarations, states, and outcomes.
  - `ClanRepository`, `AllianceRepository`, `WarRepository`
  - `ClanDisplayAdapter` – separated from service; used by UI/listeners.

### 5.7 Portals & World Routing

- `portals/`
  - `PortalService`
    - Provides an abstract API for:
      - Creating/removing portals.
      - Enqueuing cross-server transfers.
      - Managing caps (per player, per area).
  - `PortalRepository`, `TransferRepository`
  - `PortalEngine`
    - Replaces `TeleportListener` as a monolith:
      - Split responsibilities:
        - `FrameDetector`
        - `PortalResolver`
        - `TransferCoordinator`
        - `EntitySerializer` (adapter around Bukkit types).

### 5.8 Parties & Groups

- `party/`
  - `PartyService`
    - In-memory on a single node, but designed to plug into cross-node messaging for future expansions.
  - No DB by default; optionally logs for analytics or persists if cross-server parties are desired.

---

## 6. Event Handling & Listener Design

- **Listener principles**
  - Each listener class should:
    - Do minimal validation and extraction of event data.
    - Delegate to domain services/handlers.
    - Avoid querying DB or performing heavy logic directly.
  - Prefer small listener classes with clear names: `WalletJoinListener`, `SkillXpListener`, `QuestProgressListener`, etc.

- **Handler/strategy patterns**
  - Replace long `if/else` chains with:
    - Registries keyed by item/quest/event IDs.
    - Per-type handler classes implementing small interfaces.

---

## 7. Threading & Async Rules

- **Golden rules**
  - No direct JDBC or repository calls from listeners/commands.
  - All repository interfaces return `CompletableFuture` or are clearly documented as async.
  - Only `SchedulerService.runSync` may call Bukkit APIs that require main-thread access.

- **Patterns**
  - **Read-modify-write**:
    - For operations like XP gain, balance changes:
      - Load from cache (with async warmup).
      - Update local model.
      - Schedule async persistence.
  - **Cross-node coordination**:
    - Use messaging bus (or DB + cache) to maintain invariants (e.g., only one Eclipse event leader).

---

## 8. Migration Strategy from V1 to V2

1. **Stand up RPG_Core_V2 alongside legacy plugins**
   - Initially, load `RPG_Core_V2` on non-production / staging backends.
   - Mirror DB schema where necessary; use separate tables or schemas to avoid breaking V1.

2. **Implement core infra & player profiles**
   - Implement `RPGCorePlugin`, `ModuleManager`, `infra` layer, and `PlayerProfileService`.
   - Integrate with existing DB but via new repositories; do **not** reuse `CalendarSyncManager`.

3. **Port systems one by one**
   - For each domain:
     - Implement V2 module & service(s).
     - Add **feature flags** so V1 and V2 can coexist.
     - Gradually switch listeners and commands from V1 implementations to V2.
   - Suggested order:
     1. Player profiles & progression.
     2. Skills & reputation.
     3. Economy (wallets, shops).
     4. Calendar & world events.
     5. Portals and routing.
     6. Clans/alliances/war.
     7. Boss/arena system.
     8. Quests/storyline.

4. **Deprecate V1 services**
   - Once feature parity is reached for a subsystem, mark V1 services as deprecated and remove their usage from production backends.

---

## 9. Extension Points for Feature Developers

The `feature-dev` agent and human devs can extend `RPG_Core_V2` by:

- **Registering new modules**
  - Implement `RpgModule` and register in `ModuleManager` bootstrap.

- **Adding new content**
  - Items: add entries to `items.yml` and, if needed, a small behavior class implementing an `ItemBehavior` interface.
  - Quests: add entries to `quests.yml` with objectives and rewards; implement custom objective handlers when needed.
  - NPCs: define NPCs with ID, model, dialogue, and hook them to quest or reputation conditions.

- **Hooking into services**
  - Use injected interfaces (`EconomyService`, `ReputationService`, `QuestService`, etc.) instead of calling repositories or DB directly.

This architecture keeps the **core** minimal and robust, while allowing the content layer to grow without collapsing into monoliths.

