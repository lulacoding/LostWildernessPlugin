---
title: Migration from V1
description: Guide for migrating code and data from V1 to V2.
tags:
  - development
  - migration
status: reference
phase: ongoing
owner: dev
action: none
---
# Migration from V1

Mapping from current (V1) plugins and classes to PluginV2 modules. Fill in as you port.


## Class / concept mapping

*(To be filled as you port. Example:)*

| V1 class / concept | V2 equivalent |
|--------------------|----------------|
| CalendarSyncManager | CalendarService + EventEngine + messaging |
| Milestone (common) | ProgressionService + AchievementKey |
| Wallet / economy in common | EconomyService / WalletService + WalletRepository |
| TeleportListener (portals) | PortalEngine + listeners delegating to PortalService |

---

## Data migration

- Prefer **new V2 tables** (e.g. `player_profiles`, `player_achievements`) and optional one-off scripts to copy from V1 tables if needed.
- Document any schema differences and migration steps here or in implementation-status.md.
