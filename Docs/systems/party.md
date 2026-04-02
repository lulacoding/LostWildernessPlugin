---
title: Party
description: Cross-server party system with buffs, visual effects, and shared objectives.
tags:
  - system
  - party
status: implemented
phase: phase-1
owner: dev
action: none
---
# Party system

PluginV2 **Party** module: cross-server parties, invites, friendly fire, buffs, and DB-backed state. Phases 1–5 were completed in development; detailed phase logs live in the repository under `PluginV2/PARTY_PHASE_*_COMPLETE.md` and are summarized in [Party phase archive](party-phases.md).

For current behaviour and files, see [Implementation status](../roadmap/implementation-status.md) (Party row) and `PluginV2` source (`PartyModule`, `PartyService`, `PartyRepository`, listeners).

## Quick facts

- Commands: `/party create`, `invite`, `accept`, `leave`, `disband`, `info`, `kick`
- Storage: `parties`, `party_members`, `party_invites` tables; cluster messaging for sync
- Config: `config/party.yml`