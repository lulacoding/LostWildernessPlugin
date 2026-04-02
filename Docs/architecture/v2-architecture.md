---
title: V2 Architecture
description: "Detailed V2 plugin architecture: modules, services, and data flow."
tags:
  - architecture
  - v2
status: reference
phase: ongoing
owner: dev
action: none
---
## RPG_Core_V2 – Architecture Plan for AAA-Scale Network

This document defines a modern, modular architecture for `RPG_Core_V2`, designed to support a **large network of Paper servers** (tens to hundreds of backends) and **hundreds of thousands of total players** across the network. It reuses the strongest concepts from the current codebase (calendar, events, portals, progression, reputation, skills, clans/war) while restructuring them into clean, scalable services and modules.

The old plugins remain **read-only reference**; all new work happens under `RPG_Core_V2` and its supporting infrastructure.


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

