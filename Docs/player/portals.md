---
title: Portals Guide
description: Player guide to portal travel between servers.
tags:
  - player
  - portals
status: reference
phase: ongoing
owner: player-facing
action: none
---
# Portals

## Overview

**Portals** let you build linked gates between the **Survival** and **Amplified** servers. You build a frame out of **Crying Obsidian**, light it, then walk through to travel. Each player has a limited number of portal pairs (e.g. 5); the exact limit is set in the server config.


## Commands

| Command | Description |
| --- | --- |
| **/portals** | Lists your linked portals: name, location, and direction. |
| **/deleteportals &lt;username&gt;** | Admin only. Deletes all portals for that player. |


## Entity transfer (vehicles and mounts)

- When you cross a portal **on a horse, boat, or other allowed vehicle**, the plugin can transfer that entity to the other server so you arrive still mounted. Supported types include horses, donkeys, boats, striders, pigs, and minecarts. The entity is saved when you step into the portal and restored when you appear on the other server.

---

## Tips

- If the portal doesn't light, check that the frame is **only** Crying Obsidian and the inside is empty (no water, blocks, etc.).
- Use **/portals** to see where your portals are and avoid building too many.
- When you join after creating a portal on one server, the proxy may process the link and send you to the other server when you walk through.
- To test entity transfer: saddle a horse (or use a boat), ride it into the portal, and confirm you appear on the other server still mounted.
