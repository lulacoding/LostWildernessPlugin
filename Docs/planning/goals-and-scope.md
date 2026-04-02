---
title: Goals & Scope
description: Project goals, scope, and out-of-scope boundaries.
tags:
  - planning
  - overview
status: reference
phase: ongoing
owner: admin
action: none
---
# Goals and scope

## Goals

- **One jar:** A single plugin JAR (`RPG_Core_V2`) contains all core and feature modules; config controls which modules load per server role.
- **Three servers:** Target deployment is **Lobby**, **Survival**, and **Amplified** (same as current). Same jar on each; `server-role` and `enabled-modules` in config differ per server.
- **Full remake:** PluginV2 is a **recoding** of the current plugin. Every feature that exists in the current plugin and in **Docs** (implementation-status, design docs) must be reimplemented in V2—no feature drop unless explicitly decided. See [v2-feature-parity.md](../roadmap/feature-parity.md).
- **Modular:** Features (player, progression, skills, economy, calendar, events, clans, portals, quests, party, lobby behaviour) are independent modules with clear boundaries and dependencies.
- **Async-first:** All heavy work (DB, large computation) runs off the main thread; listeners stay thin and delegate to services.
- **Scalable:** Design supports many backends and large player counts (network-scale); DB/cache/messaging are shared across nodes.
- **Playable while building:** Server is not live; dev server runs only the V2 jar. Features are added one vertical slice at a time so the game stays playable.

## Scope

### In scope

- New code only under the PluginV2 (or `RPG_Core_V2`) tree.
- **Full feature parity** with current plugin + Docs: player, progression, skills, economy, calendar, events, clans, portals, quests, party, lobby, bosses, storyline gates, etc. (see [v2-feature-parity.md](../roadmap/feature-parity.md)).
- Infra: config, DB (Hikari), scheduler, optional cache/messaging.
- Module system: load order, dependencies, per-role enable/disable (lobby vs survival vs amplified).

### Out of scope (for this doc set)

- Changes to existing `Plugin/` code (read-only reference).
- Non-PluginV2 docs (those stay in repo `Docs/`).

## Constraints

- **No live server:** No need to keep old JARs in `plugins/` during development; dev server can run only the V2 jar.
- **Reference only:** Old plugins are used for logic, formulas, and behaviour reference—not for structure.
- **Step-by-step:** Foundation first (infra + player spine), then one vertical slice at a time.
