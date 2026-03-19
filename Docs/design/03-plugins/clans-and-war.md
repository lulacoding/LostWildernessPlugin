# Clans and war

**Implementation:** See [implementation-status.md](../../implementation-status.md). Implemented: /clan create, invite, accept, deny, leave, color, promote, demote, enemy, opposition, info, list; /alliance create, invite, join, leave, info, list; /war declare, ceasefire, end, status; **war head drops** (1/1000 in war, 1/10000 else); **Discord war webhook** on declare/end. **Not implemented:** /clan reside; /nomad.

## Clan system (same plugin as LWEvents)

- **Commands:** **/clan create**, **invite**, **accept**, **deny**, **leave**, **promote**, **demote**, **enemy**, **opposition**, **color** (hex). **reside** and **/clan list** **[Not implemented]**.
- **Alliance** (create, invite, join, leave, info, list) **[Not implemented — all return "not yet implemented."]**.
- **Discord webhooks:** Bot creates **clan roles and channels**; **ally chats** for alliances.
- **MySQL per-clan**; **no /home**, **no built-in economy**.
- **/nomad** — for **solo players** who don’t join a clan. **[Not implemented]**
- **Devoider/El Diablo kill counts** tracked per clan/player; **Easter week** (no meat = reward) tied to clan/events.

## War

- **/war declare**, **ceasefire**, **end**, **status**. **[Implemented.]**
- **Declared war** vs **raids:** **War** = music/effects, formal; **raids** = stealth. **War day** subtitle; **100×100 warzone radius** for war music and effects (radius not in code yet).
- **Player head drops:** **1/1000 in war**, **1/10000** else — **implemented** (WarHeadDropListener).
- **Discord war webhook:** Optional embed on declare/ceasefire/end (config discord.war-webhook-url).
- **Locator bar:** **Clan-only** or **/alliance locationrequest** (to be confirmed).
- **Calendar:** Use **/date** when war is declared and when it ends for history/museum.

## Help commands (TODO)

- **/clan help**, **/alliance help**, **/war help**, **/lostwilderness help** — descriptions to be filled.

## Source

- Direct Messages (Grovyle187): Epochians and Brotherhood, war tracking with /date.
- Bible sections 3 and 5: clan commands, no /home, war vs raids, head drops, warzone radius.
