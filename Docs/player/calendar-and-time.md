# Calendar and Time

Overview of the shared calendar, date/time commands, and seasons.

---

## Overview

The server uses a **shared calendar** that advances over time. Your **location** (north, south, or equator) affects which **season** you experience. Dates are shown in a simple format (e.g. "January 15, 1MC") and the calendar can track when you joined.

---

## Commands

| Command | Description |
| --- | --- |
| **/date** | Shows the current in-game date and how many days you've been on the server. |
| **/time** | Shows the current in-game time (24-hour format) in your world. |
| **/season** | Shows the current season **where you are standing**, your hemisphere (North / South / Equator), and year/day info. |
| **/datejoined** | Shows the calendar date when you first joined and how many days you've been with the server. |
| **/resetcalendar** | Admin only. Resets or changes the calendar day (e.g. set day, increment, or show "today"). |

---

---

## How the Calendar Works

- The server has a single **calendar day** that increments (often once per real-world day or on a schedule set by admins).
- **/date** shows that day in a readable form (e.g. "January 15, 1MC").
- **/time** shows the current Minecraft time in your world (sunrise/sunset, etc.), not the calendar.

---

---

## Seasons and the Equator

- **Seasons** depend on the calendar day and your **Z coordinate**:
  - **Northern** (Z > 500): Winter in Dec–Feb, Spring Mar–May, Summer Jun–Aug, Autumn Sep–Nov.
  - **Southern** (Z < -500): Summer in Dec–Feb, Autumn Mar–May, Winter Jun–Aug, Spring Sep–Nov (Australian-style).
  - **Equator** (between Z -500 and +500): **No season** – you're in a "no-season" belt. **/season** will show "No Season" and "Equator".
- The equator width (default 500 blocks each side of Z=0) can be changed by server config.

---

## Tips

- Use **/season** when you want to know what season applies to your current location.
- **/date** and **/datejoined** help you see how long the world has been running and how long you've been playing.
- Only operators can use **/resetcalendar** to change the calendar day.
