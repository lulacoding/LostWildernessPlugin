---
title: V1 Roadmap
description: Legacy V1 project roadmap.
tags:
  - archive
  - v1
status: archive
phase: archive
owner: admin
action: none
---
# Roadmap - V1 Plugin & Project-Level Priorities

> **V2 Build Phases:** For the V2 rewrite build roadmap, see [v2/05-planning/roadmap.md](v2/05-planning/roadmap.md)

This is the prioritized execution plan for Lost Wilderness. It should reflect reality: what matters most next, what can follow after that, and what belongs in longer-term vision rather than immediate execution.

For current feature truth, use [implementation-status.md](implementation-status.md). For infrastructure planning detail, use [future/README.md](future/README.md).


## Next

These items are the next layer after core stability and operations are under control.

| Area | Goal | Notes |
| --- | --- | --- |
| Lobby and queue ecosystem | Decide what stays external vs enters this repo | Void world, GUI flow, queue logic, and pack acceptance experience |
| Resource-pack delivery | Clean up Java/Bedrock pack delivery and fallback behavior | Especially important for Geyser/Floodgate compatibility |
| War experience polish | Add better war feedback beyond the minimal command layer | Music, warzone radius, UX feedback, clan-facing status |
| Multi-instance routing | Prepare for multiple lobby/survival/amplified instances | Naming, routing, and "return where you left off" behavior |
| Docs quality and support flow | Keep docs aligned with implementation after each major feature | Status, player guides, tests, and changelog should stay synchronized |


## Out Of Scope For Immediate Execution

These ideas belong in the design bible or long-term R&D, not the near-term implementation queue.

- Experimental GPU offloading ideas
- Very long-horizon tech systems like wireless infrastructure or item cloud concepts
- Fully scripted story campaigns before the core network is operationally stable

---

## Working Rule

When a feature is finished:

1. Update [implementation-status.md](implementation-status.md).
2. Update the relevant player/development/operations guide.
3. Update `CHANGELOG.md`.
4. Remove or downgrade the roadmap item if it is no longer active.
