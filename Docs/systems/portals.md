---
title: Portals
description: Cross-server portal system using crying obsidian frames and BungeeCord.
tags:
  - system
  - portals
status: implemented
phase: phase-1
owner: dev
action: none
---
# Portals

Merged design-bible notes and PluginV2 module reference.


## PluginV2 module

Cross-server portals and transfer (Survival â†” Amplified). Enabled when `portals` is in `config/core.yml` â†’ **enabled-modules**. Uses **player** datasource (same DB as calendar, clans).

## Purpose

- Detect crying obsidian portal frames; resolve bound portal from DB (async).
- Coordinate transfer: serialize entities, write to transfer table, send player via BungeeCord Connect message.
- On destination (PlayerJoin): read pending portal, build return portal at 1:1 coords, restore entities, teleport.

## Main classes

| Class | Responsibility |
|-------|----------------|
| **PortalService** | Frame validation, tryToLightPortal, buildReturnPortalFrame, prepareSafeExit, computeExitSpot. Caps and frame sizes from config. |
| **PortalRepository** | Async persistence: portals, pending_portals, portal_transfer_entities. Same schema as legacy for compatibility. |
| **PortalEntitySerializer** | Serialize player's vehicle (horse, boat, etc.) to key=value string. |
| **PortalEntitySpawner** | Deserialize and spawn entity at location, mount player (main thread). |
| **BungeeMessenger** | Build BungeeCord plugin message (Connect subchannel). |
| **Listeners** | PortalInteractListener (flint & steel), PortalEnterListener (step on END_GATEWAY â†’ Bungee connect), PortalJoinListener (process pending, build return, teleport, spawn mount), PortalBreakListener (break crying obsidian â†’ async delete from DB). |

## Config

- **config/portals.yml** â€“ `target-server` (Bungee server name, e.g. amplified-1 / survival-1), `max-pairs` (5), `frame.min-width/height`, `frame.max-width/height`, `exit-clearance` (enabled, radius, height, large mount options). Server role from **core.yml** `server-role` (survival | amplified) determines which side we are and default target.

## DB

- **portals** â€“ player_uuid, portal_name, direction, survival_world/x/y/z, amplified_world/x/y/z, exit_x/y/z. Primary key (player_uuid, portal_name).
- **pending_portals** â€“ id, player_uuid, portal_name, direction, survival_*, amplified_*, status. Used for first-time cross (build return on other server).
- **portal_transfer_entities** â€“ player_uuid, portal_name, entity_data (TEXT), created_at. Serialized mount data; consumed and cleared on arrival.

## Commands

- **/portals** â€“ List linked portals for the player (permission `lw.portals.use`).
- **/deleteportals \<username\>** â€“ Delete all portal pairs for that player; clear blocks in loaded worlds; remove from DB (permission `lw.portals.delete`).

## Integration

- **PortalsModule** â€“ onLoad: PortalRepository, PortalsConfig, PortalService; createTablesIfNotExists. onEnable: register BungeeCord outgoing channel, register listeners and commands.
- Thin listeners: interact (flint & steel) â†’ tryToLightPortal; move (END_GATEWAY) â†’ async resolve + save transfer + Bungee connect; join â†’ async get pending â†’ main: build return, update DB, teleport, spawn mount, delete pending; block break (crying obsidian) â†’ async find owner and delete from all three tables.
