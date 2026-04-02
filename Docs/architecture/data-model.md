---
title: Data Model
description: Database schema, table definitions, and entity relationships.
tags:
  - architecture
  - database
status: reference
phase: ongoing
owner: dev
action: none
---
# Data model

Main entities and how they map to DB. Expand as modules are added.


## Progression

- **AchievementKey** – namespaced string (e.g. `story:el_diablo`, `boss:roofwither_kills_10`).
- **Per player:** Set of unlocked keys + optional timestamp.
- **DB:** `player_achievements` or `progression`: player_uuid, key, unlocked_at.


## Economy

- **Currency** – id, symbol, display name.
- **Wallet** – per player per currency: balance.
- **DB:** `wallets`: player_uuid, currency_id, balance, updated_at. Optional: `transaction_log`.


## Calendar

- **Global state** – current_day, year, season, last_tick; one row or small table.
- **DB:** `calendar_state` or equivalent.

---

## Events, Clans, Portals, Quests

- See respective plugin docs (plugin-events.md, plugin-clans.md, plugin-portals.md, plugin-quests.md) for entities and tables when implemented.
