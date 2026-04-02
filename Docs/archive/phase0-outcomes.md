---
title: Phase 0 Outcomes
description: Phase 0 completion outcomes and lessons.
tags:
  - archive
  - roadmap
status: archive
phase: archive
owner: admin
action: none
---
# Phase 0 – Outcomes

Decisions and context from the orientation phase. Update as you refine.


## Servers (fixed – three nodes)

PluginV2 targets **exactly three server roles**, matching the current setup:

| Server | Role | Purpose |
|--------|------|--------|
| **Lobby** | `lobby` | Auth, verification, cosmetics, routing to Survival/Amplified |
| **Survival** | `survival` | Main survival world; full RPG stack (calendar leader, events, clans, portals, etc.) |
| **Amplified** | `amplified` | Amplified world; calendar follower; same RPG stack as Survival |

Same JAR on all three; config sets `server-role` and `enabled-modules` per server. Instance/arena servers are optional later.


## Environments

| Env | Purpose |
|-----|--------|
| **dev** | Local machine; single backend + proxy (or single server); only PluginV2 in `plugins/`. |
| **stage** | Optional test network (proxy + lobby + 1–2 backends) before prod. |
| **prod** | Live network when you go live; same jar, config per role. |

Phase 1 testing uses **dev** only.


## Reference

- Architecture: [v2-architecture.md](../architecture/v2-architecture.md)
- **Feature parity checklist:** [v2-feature-parity.md](../roadmap/feature-parity.md)
- PluginV2 docs: [README](../README.md)
- Next: [Phase 1 spec](phase1-spec.md) and [roadmap](roadmap.md)
