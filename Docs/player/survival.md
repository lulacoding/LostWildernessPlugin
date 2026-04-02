---
title: Survival Guide
description: Player guide to the Survival server.
tags:
  - player
  - survival
status: reference
phase: ongoing
owner: player-facing
action: none
---
# Survival Server

## Overview

The **Survival** server ties together calendar, seasons, world events, clans, and portals. You get access to date/time/season commands, world events (eclipse, storms, blizzards, etc.), clans, and cross-server portals to the Amplified server. Some events block sleeping.

When you first arrive from the lobby, you will spawn in the **Survival spawn village at (0,0)** – a protected Equator hub with NPCs that help you choose a **class**, learn about the calendar and seasons, and understand how travel works before you head out into the wider world.


## What You'll Experience

- **Calendar & seasons:** One shared calendar for the whole server. Near **Z = 0** (configurable half-width in `lw-climate.yml` → `equator`) you are in the **Equator belt**: softer rain on you, frost/blizzard surface ice and stray spawns skipped in that band, and `/season` explains the buffer. **Northern vs southern hemisphere seasons (opposite summer/winter by Z sign)** are not implemented yet—only this equator band.
- **World events:** Eclipse, thunderstorms, fog, blizzards, heatwaves, seasonal storms, etc. Some block sleeping (eclipse, thunder, fog). See [World Events](events.md).
- **Resource packs:** The server may prompt you to accept a default or event pack (e.g. for eclipses). Accept to see the intended visuals.
- **Clans:** Create or join clans, invite members, set colors, declare enemies. See [Clans](clans.md).
- **Portals:** Build portal frames (crying obsidian), light them, and use them to travel to the **Amplified** server. See [Portals](portals.md).
- **Discord verification:** Verify once through the lobby and Discord bot, then your linked account works across the network.

---

## Tips

- Use **/lwhelp** to see which systems are available and quick command hints.
- If you can't sleep, check if an eclipse, thunderstorm, or fog event is active.
- Portals require a **BungeeCord** (or compatible) proxy; connect through the proxy address (e.g. port 25565) so portal travel works.
