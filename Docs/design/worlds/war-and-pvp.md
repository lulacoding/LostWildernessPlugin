---
title: War & PvP Design
description: War zone and PvP mechanics design.
tags:
  - design
  - worlds
  - clans
  - pvp
status: reference
phase: ongoing
owner: design
action: none
---
# War and PvP

- **No /home** — so raiding and war are about **location** and **travel** (lodestones, maps).
- **Declared war** vs **raids:** **War** = formal, **music/effects** in 100×100 radius, **war day** subtitle; **raids** = stealth, no war theme.
- **Player head drops:** **1/1000 in war**, **1/10000** otherwise — **implemented** (WarHeadDropListener). On PvP kill, if clans at war roll 1/1000 else 1/10000; drop victim head at death location.
- **Discord war webhook:** Optional webhook on declare/ceasefire/end (config `discord.war-webhook-url`). See [test guide](../../testing/test-guide.md).
- **Locator bar:** **Clan-only** or **/alliance locationrequest** (to be confirmed).
- **100×100 warzone radius** for war music and PvP effects.
- **Calendar:** Use **/date** at war declaration and at war end for history (museum, “length of wars”).

## Source

- Bible sections 3 and 5; Direct Messages (Grovyle187) war tracking; 03-plugins clans-and-war.
