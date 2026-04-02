---
title: Clans Guide
description: Player guide to creating, joining, and managing clans.
tags:
  - player
  - clans
status: reference
phase: ongoing
owner: player-facing
action: none
---
# Clans

## Overview

**Clans** let you form groups with a name and a color. Leaders can invite members, set the clan color, promote or demote members, and declare other clans as enemies or opposition. Your clan name/color can be shown in-game (e.g. in tab or chat) depending on server setup.


## Alliances

- **/alliance** has subcommands such as **create**, **invite**, **join**, **leave**, **info**, **list**. Some may show "not yet implemented" – check in-game or with staff.


## Typical flow

1. **Create a clan:** `/clan create My Clan #FFAA00`
2. **Invite players:** `/clan invite PlayerName` (they get a message to use `/clan accept` or `/clan deny`).
3. **Manage:** Use **/clan color**, **/clan promote**, **/clan demote** as leader; **/clan leave** to leave.
4. **Relations:** Use **/clan enemy** or **/clan opposition** to set relations with other clans (leader only).

---

## Tips

- Clan names must be 1–32 characters. Color must be a valid hex code (e.g. #FF0000 for red).
- Only one pending invite is used at a time; **/clan accept** or **/clan deny** applies to the most recent.
- If the server uses Discord integration, creating a clan or updating roles may sync to Discord (e.g. roles); that's configured by the server.
