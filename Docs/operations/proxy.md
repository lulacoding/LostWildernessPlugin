---
title: Proxy Setup
description: BungeeCord/Velocity proxy configuration.
tags:
  - operations
  - proxy
status: reference
phase: ongoing
owner: ops
action: none
---
## Future: Multi‑Instance Servers & Player Routing

To scale beyond what a single Survival or Lobby server can handle, Lost Wilderness will eventually run **multiple instances** of the same game mode and route players intelligently between them.

This page outlines that target architecture.


### 2. Naming and proxy config

Recommended naming convention:

- Lobby:
  - `lobby-1`, `lobby-2`, ...
- Survival:
  - `survival-1`, `survival-2`, ...
- Amplified:
  - `amplified-1`, ...

Proxy (`config.yml` or equivalent):

- Define each backend under `servers:` with its own address.
- Use a **higher‑level abstraction** in your docs:
  - “Survival” = the set `{survival-1, survival-2, ...}`.
  - “Lobby” = `{lobby-1, lobby-2, ...}`.

Later, you can introduce **per‑mode balancers** or a small routing plugin at the proxy layer.


### 4. State and data sharing

The more instances you run, the more important it is to define **which data is global** and which is per‑instance:

- **Global (shared via DB):**
  - Clans, alliances, wars.
  - Calendar (already leader/follower).
  - Portals that move between worlds/instances.
  - Verification (`linked_accounts`, `pending_verification`).
- **Per‑instance:**
  - World blocks/chunks.
  - Local economy or markets (if you choose to separate).

Design rules:

- Only **one writer** per global data type at a time (e.g. Calendar leader).
- All servers **read the same global tables**, respecting leader/follower behaviour where needed.


### 6. Capacity planning with multi‑instance

Once you have good **per‑instance limits** (see `paper-performance-and-capacity.md`):

- Define mode capacities, e.g.:
  - Each Survival instance: ~120 players comfortably.
  - Each Lobby instance: ~200 players.
- Then compute:
  - 2 Survival instances → ~240 Survival slots.
  - 2 Lobby instances → ~400+ lobby slots (lobbies are light).

Scaling strategy:

- Start with `lobby-1`, `survival-1`, `amplified-1`.
- When average concurrent players approach safe limits:
  - Add `lobby-2` first (for join bursts).
  - Then `survival-2` and update routing logic.

Use this file to note **exact instance counts and routing rules** as you grow.

