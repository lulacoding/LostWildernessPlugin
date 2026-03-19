# LWEvents and Eclipse

**Implementation:** See [implementation-status.md](../../implementation-status.md). Eclipse, Fog, Frost, Blizzard, Heatwave, Thunder, Easter week, NaturalHordeSpawner, SleepBlocker, and RoofWitherListener are **implemented**. Nether sicknesses, Bog (Fog/Bog 50/50), and some Eclipse 5-act/COD-style round UI are design-only or **not implemented**.

## Eclipse / Blood Moon

- **Eclipse** (Blood Moon) is a **dynamic event** driven by the calendar: roughly every **90–98 days** (configurable). When it triggers:
  - **Time** is frozen at a set value (e.g. **18000**).
  - A **red moon resource pack** is forced; when the event ends an **empty pack** is sent so the sky returns to normal.
  - **Zombie (and variant) hordes** spawn; **no sleep** during the event (20-minute in-game “day”).
  - Chat message: **“☾ A solar eclipse blankets the sky! ☾”**
  - **Spider jockey** and **chicken jockey** rates are increased; **skeleton horse traps** on eclipse.
  - **Seasons** start on **21st March, June, September, December** (equinox/solstice); eclipse marks season start.
  - **5-part “battle” theme** (music); **COD zombies–style rounds** with **red Roman numeral** titles; **Discord leaderboards**.
- **Winter begin/end** eclipses are **harder** than summer; **rare “Apocalypse”** thunderstorm eclipse with **charged creepers** (lightning rods help); **Mushroom Island** avoids eclipse.
- **Eclipse plugin** depends on the **calendar plugin** for day progression.

## LWEvents (Fog, Frost, Blizzard, Heatwave, Nether)

- **Fog:** Uses **ProtocolLibrary VIEW_DISTANCE** packet; **Fog/Bog 50/50** in swamps; **Bog** = **Bogged** skeletons and horses.
- **Frost:** Winter nights in **cold biomes**; **slowness + mining fatigue**; **heat sources** (lava, campfire, torches) cure; surface **water freeze/thaw**; **FrostMoveListener** for biome enter/leave. **11pm–6am** water→ice in cold biomes; deserts **12am–4am** (source only). **Coldplay Clocks / Dark Cave** style music; **flash-freeze .wav** or powder snow; plays in frost radius, fades when leaving.
- **Thunder Eclipse:** **Undead cavalry** (zombie horses + riders, gold/diamond gear); stacks with Fog.
- **Blizzard:** **Strays**, fog + snow; **Blizzard Eclipse** = zombies→Strays; **Sinnoh Route 216** day/night style music; Blizzard Eclipse uses eclipse theme.
- **Heatwave:** Hot biomes in summer; crops/animals/water affected; **Husks** by day. **PMD Quicksand Pit** style; oriental metal / Egypt boss.
- **Nether:** Calendar is **off** in Nether (time tracked but no day advance); **Nether sicknesses** in **Soul Sand Valley, Wastes, Basalt Deltas**; **detox** in Nether forests or **Y≥127**; vaccine/timer; **Geiger counter** parrot sound. **Fog variant** Crimson/Warped; **Nether hordes**; **Wither horse (gold)** in extreme fog.

## RoofWitherListener (Devoider / El Diablo)

- **Survival** nether roof: **Devoider** (**300 HP**).
- **Amplified** nether roof: **El Diablo** (**500→600 HP**, **bedrock-breaking blue skulls**, **25% mini-horde**).

## NaturalHordeSpawner and biome modifiers

- **100×100** clusters; **biome mobs**; **jockey chance**.
- **Spring Bloom**, **Autumn Leaf Fall**; **seasonal storms**; **biome modifiers** for weather and effects.

## Source

- Bible sections 3, 5, 7: Eclipse, Fog, Frost, Blizzard, Heatwave, Nether sickness, RoofWitherListener, music references, seasons 21st.
