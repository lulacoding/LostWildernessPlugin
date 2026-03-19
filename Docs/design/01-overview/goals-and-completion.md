# Goals and 200% completion

## 200% advancement completion

A stated goal is **200% advancement completion**: that is, **Vanilla** advancement completion **plus** full **custom** Lost Wilderness advancements. The **completionist reward** is long-term content and a clear endgame target:

- **Bases, alliances, trade, war** — all supported and tracked (e.g. calendar for war dates).
- **Calendar** and **events** (eclipses, seasons, New Year, century milestones) give the world a sense of history.
- **200% achievements** grant the **Main Theme disc** (opening cutscene theme) as a reward.

So “200%” is both a design goal and a concrete reward (the disc).

## Long-term content and history

From the chats, the previous server (Tunnelboolers187 / tb187) illustrated the intended longevity:

- It reached the **4th century** (in-game years) after about **3 years** of play.
- There was an **economy**, a **dollar** (Anti-Empirical Dollar), **fluctuating emerald/diamond/iron prices**, **debt ceiling** and “Senate-like” votes, **casinos, supermarkets, global alliances**, “Redstone War Technology.”
- The **calendar plugin** is meant to support that: e.g. **/date** when a war is declared and again when it ends, dates for **horse birth/death** for tombstones, **museum** dates. One message: *“It’s niche but it’s fitting. Basically 2b2t without hacks. Pseudo-Anarchy.”*

So the **goal** is not just “beat the boss” but **ongoing civilisation building** with a shared timeline.

## New Year and century events

Discussed features (from the source):

- **Happy New Year** title at end of each in-game year.
- **Mention of the Next Century** (e.g. when crossing into year 100, 200).
- **Firework display** at end of year or end of century (lightshow/event) — **implemented.** NewYearFireworkHandler at (0,0); scale by decade/century/millennium. Config: newyear-firework.x/z.
- **XP level-up sound** when the “new day” title plays.
- **50 seconds to an hour** with 20 TPS (in-game time).

Grovyle187: *“Yeah a new year title and a mention about the Next Century, I’d probably not do season’s until they exist within minecraft ‘Winter is coming’.”* So seasons in the UI/calendar (e.g. “winter July 06 year 1”) were considered; full biome seasons were deferred until Minecraft supports them more natively.

## Calendar as internal clock

The **calendar** is the **internal plugin clock** for the server:

- **Eclipse every 90–98 days** (exact number refined in plugin config).
- **Seasonal gameplay**: e.g. farming in spring, mining/building in winter, with the calendar driving events and titles.
- Possible **in-game natural events** synced to dates; **forced snow on Xmas** (biome rules TBD); **“days since EOC”** (Epoch) in **/date** output.

## Optional calendar item

From the chats: *“i could even make a like calander item that can be placed like a painting on a 1x1 block and then when you click it, it open like a book but like a actuall calander”* — and the response was “could be cool.” **Implemented (item):** Right-click with configured calendar item (e.g. clock named "Calendar") opens book with date/season (CalendarItemListener). Placeable 1×1 block needs texture/datapack.

## Source

- Direct Messages (Grovyle187): 200%, New Year/century titles, fireworks, XP sound, 50 sec/hour, “Winter is coming,” calendar item idea, war/museum/horse dates, 4th century and economy description.
- Bible sections 1 and 5: 200% completion, Main Theme disc, seasonal gameplay, eclipse interval.
