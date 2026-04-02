---
title: Architecture Decisions
description: Key architectural decisions and their rationale (ADR log).
tags:
  - architecture
  - decisions
status: reference
phase: ongoing
owner: dev
action: none
---
# Architecture decisions

Log of non-obvious decisions for PluginV2. Add new entries as you make them.

**Related:** Legacy design-bible technical stubs (calendar sync, portal design, plugin placement) are consolidated in [Infrastructure](infrastructure.md) under “Technical notes”.


## No V1 jars in dev plugins/

- **Decision:** Dev server runs only PluginV2; old plugins stay in repo as read-only reference.
- **Reason:** Server is not live; clean test of V2 without legacy interference.


## Module dependency graph

- **Decision:** ModuleManager orders load/enable/disable by declared dependencies (e.g. Quests depends on Player, Progression, Economy).
- **Reason:** Ensures services exist before modules that use them; clean shutdown order.


*(Add more as you go.)*
