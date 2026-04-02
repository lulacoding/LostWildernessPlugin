---
title: Calendar Guide
description: Player guide to the in-game calendar and seasons.
tags:
  - player
  - calendar
status: reference
phase: ongoing
owner: player-facing
action: none
---
# Calendar and Time

Overview of the shared calendar, date/time commands, and seasons.


## Commands

| Command | Description |
| --- | --- |
| **/date** | Shows the current in-game date and how many days you've been on the server. |
| **/time** | Shows the current in-game time (24-hour format) in your world. |
| **/season** | Shows the current season **where you are standing**, your hemisphere (North / South / Equator), and year/day info. |
| **/datejoined** | Shows the calendar date when you first joined and how many days you've been with the server. |
| **/resetcalendar** | Admin only. Resets or changes the calendar day (e.g. set day, increment, or show "today"). |


## How the Calendar Works

- The server has a single **calendar day** that increments (often once per real-world day or on a schedule set by admins).
- **/date** shows that day in a readable form (e.g. "January 15, 1MC").
- **/time** shows the current Minecraft time in your world (sunrise/sunset, etc.), not the calendar.


## Seasons and the Equator

- **Seasons** depend on the calendar day and your **Z coordinate**:
  - **Northern** (Z > 500): Winter in Decâ€“Feb, Spring Marâ€“May, Summer Junâ€“Aug, Autumn Sepâ€“Nov.
  - **Southern** (Z < -500): Summer in Decâ€“Feb, Autumn Marâ€“May, Winter Junâ€“Aug, Spring Sepâ€“Nov (Australian-style).
  - **Equator** (between Z -500 and +500): **No season** â€“ you're in a "no-season" belt. **/season** will show "No Season" and "Equator".
- The equator width (default 500 blocks each side of Z=0) can be changed by server config.


## Calendar plugin (what runs behind the scenes)

## Overview

**LostWilderness-Calendar** is the plugin that runs the **shared calendar** and keeps it in sync (e.g. as the "leader" when multiple servers use the same calendar). As a player you don't run Calendar by itself; you play on **Survival** or **Amplified**, which use the calendar. Calendar provides the date, seasons, and join-day tracking that those servers use.


## Calendar item

- If the server gives you a **calendar item** (e.g. a clock named "Calendar"), **right-click with it** to open a book showing the current in-game date, year, season, and day. This is the same date that **/date** and **/season** use.

---

## Tips

- Use the [above](#calendar-and-time) guide for how **/date**, **/time**, **/season**, and the equator work. That's the player-facing side of what the Calendar plugin provides.
