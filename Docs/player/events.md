---
title: Events Guide
description: Player guide to daily and seasonal events.
tags:
  - player
  - events
status: reference
phase: ongoing
owner: player-facing
action: none
---
# World Events

Calendar-driven weather, atmosphere, and danger (eclipse, storms, fog, blizzard, heatwave, etc.). Some events block sleeping.


## Events You May Experience

### Eclipse

- **What happens:** The sky goes dark (midnight look), a special resource pack may apply, and **hostile hordes** can spawn. Time is frozen during the eclipse.
- **When:** Triggered on certain calendar days (e.g. every 98 days, with a chance). Configurable by admins.
- **Player impact:** You cannot sleep during an eclipse. Stay alert for increased mob spawns.

### Thunderstorm

- **What happens:** Storm and thunder are turned on in the Overworld. On eclipse days, the chance for a storm is higher; **zombie horses** can spawn.
- **When:** Random chance each calendar day (higher chance on eclipse days).
- **Player impact:** You cannot sleep during an active thunderstorm.

### Fog

- **What happens:** A rolling fog is announced and view distance is reduced for a while, then restored.
- **When:** Random chance each calendar day.
- **Player impact:** You cannot sleep while the fog is active. Visibility is reduced.

### Blizzard

- **What happens:** Snowstorm in the Overworld. **Strays** spawn in snowy biomes; water can freeze. Message: "A vicious winter blizzard howls… Only snowy biomes are affected!"
- **When:** Random chance on calendar day; affects the whole world but only snowy biomes for spawns and freezing.
- **Player impact:** Dangerous in snow biomes; other areas get storm weather only.

### Seasonal storm

- **What happens:** Lightning and storm in late autumn / winter (season depends on your hemisphere). Can strike in regions where players are in the right season.
- **When:** Chance on calendar day when at least one player is in late autumn or winter (by location).
- **Player impact:** Storm and lightning in affected areas.

### Summer heatwave

- **What happens:** In **summer** (by your location), hunger can increase and **husks** may spawn; crops and farmland can be affected.
- **When:** Chance on calendar day when at least one player is in summer.
- **Player impact:** More dangerous in summer regions; manage food and avoid husks.

### Frost

- **What happens:** Cold-themed effect; can target a specific time of day (e.g. evening). Exact effects depend on server config.
- **When:** Chance on calendar day.

### Jungle monsoon

- **What happens:** Rain/storm effect; can target a specific time of day. Often relevant in or near jungle regions.
- **When:** Chance on calendar day.

### Easter week

- **What happens:** For eight calendar days (Palm Sunday through Easter Sunday), when the server day changes you see a **title** on screen for that day. Examples: "Palm Sunday: Prepare the way!", "Good Friday — The Lord was forsaken…", "He is risen! Happy Easter Sunday!" No chat spam; no sleep blocking; no special mobs or bonuses—just the daily title. The date of Easter Sunday follows the real-world Gregorian Easter for that in-game year.
- **When:** Automatically on the calendar days that match Holy Week (the week before Easter Sunday through Easter Sunday). Same for all players regardless of hemisphere.

### Spring bloom / Autumn leaf fall

- **What happens:** Seasonal flavour events (e.g. spring bloom, autumn leaves). Details depend on server setup.
- **When:** Tied to calendar and season (and sometimes month).


## Tips

- Events are driven by the **calendar day**, not real-world clock. When the day changes, new events can start.
- Your **position (Z)** decides your season, so the same calendar day can mean different seasons (and different events) for different players.
- Use **/lwdebug help** (if you have access) to see debug info about active events.
- Event chances and intervals are set in the server config; admins can tune how often each event occurs.
