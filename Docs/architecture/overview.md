---
title: Architecture Overview
description: High-level V2 architecture overview.
tags:
  - architecture
  - overview
status: reference
phase: ongoing
owner: dev
action: none
---
# Architecture overview

High-level structure of RPG Core V2. Full design is in repo-root [V2_ARCHITECTURE_PLAN.md](../../V2_ARCHITECTURE_PLAN.md).

## Layers

1. **Core** – Plugin entrypoint, module manager, bootstrap. No gameplay logic.
2. **Infra** – Config, database, scheduler, (optional) cache and messaging. Used by all modules.
3. **Modules** – Feature domains (player, progression, skills, economy, calendar, events, clans, portals, quests, party). Each has services, repos, and thin listeners.

## Flow

- **Bootstrap:** `RPGCorePlugin` → load config → create infra (DB, scheduler, etc.) → build `ModuleContext` → `ModuleManager.loadAll()` / `enableAll()`.
- **Player spine:** All gameplay hangs off the player profile. `PlayerProfileService` loads profiles async on `AsyncPlayerPreLoginEvent`, caches them, and exposes to other services. Domain services read/write via the profile or their own async repos; listeners only delegate.
- **Per-server role:** Same JAR everywhere; `core.yml` (or similar) sets server role and enabled modules (e.g. lobby vs full backend).

## Principles

- **Main thread:** Only minimal sync work on the main thread; DB and heavy logic go through `SchedulerService.runAsync*` and async repos.
- **Modularity:** Adding a new feature = new module or new handler registration, not editing god-classes.
- **Clean events:** Listeners validate and delegate to services; no large `if/else` blocks in `@EventHandler` methods.
