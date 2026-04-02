---
title: Phase 1 Spec
description: Original Phase 1 specification document.
tags:
  - archive
  - roadmap
status: archive
phase: archive
owner: admin
action: none
---
# Phase 1 spec

Concrete spec for the first implementation slice: skeleton plugin, infra, module system, and player spine.


## Class list (minimal)

**core/**  
- `RPGCorePlugin` – onLoad / onEnable / onDisable; create infra, ModuleContext, ModuleManager; load/enable/disable modules.  
- `ModuleManager` – register modules, dependency order, loadAll(), enableAll(), disableAll().  
- `RpgModule` – interface: onLoad(ModuleContext), onEnable(), onDisable().  
- `ModuleContext` – getConfigService(), getDatabaseProvider(), getScheduler(), getServiceRegistry().

**infra/**  
- `ConfigService` – read core.yml, db.yml (and optionally player.yml).  
- `DatabaseProvider` – Hikari pools from config.  
- `SqlExecutor` – async query/update returning CompletableFuture.  
- `SchedulerService` – runSync, runAsync, runSyncDelayed, runAsyncDelayed (and optionally repeating).  
- `ClusterMessagingService` – interface + stub impl (no-op or log-only).

**player/**  
- `PlayerProfile` – uuid, lastSeenAt (minimal for Phase 1).  
- `PlayerProfileService` – cache Map<UUID, PlayerProfile>; loadProfileAsync(UUID), getProfile(UUID), saveProfileAsync(UUID).  
- `PlayerSessionManager` – optional; track online set and callbacks.  
- `player/repo/PlayerProfileRepository` – findById(UUID), save(PlayerProfile) via SqlExecutor.  
- `player/listener/PlayerProfilePreloadListener` – AsyncPlayerPreLoginEvent → loadProfileAsync.  
- `player/listener/PlayerSessionListener` – PlayerJoinEvent (ensure profile in cache), PlayerQuitEvent (saveProfileAsync + evict).

**Module:**  
- `PlayerModule` – implements RpgModule; registers PlayerProfileService, listeners; depends on nothing.


## Config files (Phase 1)

- **config/core.yml** – server-role, enabled-modules: [player].  
- **config/db.yml** – datasources (e.g. player: jdbcUrl, username, password, maximumPoolSize).  
- **config/player.yml** – optional; cache/save settings.

---

## DB (Phase 1)

One table sufficient for Phase 1:

- **player_profiles** – id (UUID), last_seen_at (timestamp); optional columns for future (e.g. username) can be added when needed.
