---
title: Lobby & Creative
description: Lobby and creative server design notes.
tags:
  - design
  - worlds
  - lobby
status: reference
phase: ongoing
owner: design
action: none
---
# Lobby and Creative

## Lobby

- **Void** world (empty End-style); **VoidWorldGenerator**; **spectator**; **port 25568**.
- **Chest GUI menus** (e.g. DeluxeMenus): **link Discord**, **clan/ally list**, **queue**.
- **Accept resource pack** before **Play**; then **client check** (Vulcan/Negativity).
- **Play** = send to **Survival** or **“where you left off.”**
- **Bedrock:** If chest GUI fails, **CLI lobby** or tour fallback.
- **Creative-while-queued** so waiting players can build in a limited creative area.
- **Staff priority** on queue (login doesn’t count toward cap).
- **PackServer** serves **/packs/*.zip**; **DefaultPackListener** applies pack; **Floodgate bypass** if Bedrock pack fails.

## Creative server

- **10 public plots** (monthly wipe); **private/clan/alliance** plots (yearly, max 2 years).
- **Superflat**, **peaceful**, no day/night, **1M×1M**.
- **Must go via Lobby**; **inventory swap + Vulcan check** when loading into Survival.
- **Command blocks disabled**.
- **Hard border** between LW worlds and Creative so players can’t cross without going through Lobby.

## Source

- Bible sections 3 and 5; 03-plugins lobby-and-queue.
