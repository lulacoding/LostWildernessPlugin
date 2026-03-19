# Worlds and seeds

## Survival (main overworld)

- **Seed:** **"stephen holbery"** (or numeric **1473117994** on 1.21.5). The seed is intended to **stay non-public**; **anti–seed-cracker plugins** are planned so the world cannot be reverse-engineered from client-side info.
- **World type:** Standard overworld; **Amplified** and **Large Biomes** were both considered for other worlds. *“I might have to flip a coin on Amplified or Large Biomes, or maybe I'll do both, depends on performance”* (with a link to “Minecraft 1.18 - amplified is back!”).

## Amplified Nether and End

- **Amplified Nether** and **Amplified End** use a **separate seed** from Survival (e.g. **"stephenholbery"**). **Multiverse-Core** is used to create those worlds with custom seeds (e.g. `/mv create world_nether nether -seed …`).

## Multiverse-Core

- **Multiverse-Core (MV5 beta)** is used for **world creation** (e.g. new Nether/End with random or custom seeds).
- **Multiverse Nether Portals** links **Amplified Nether** to **Amplified overworld** (so nether travel works within Amplified).

## Build height (datapacks, not plugins)

- Build height changes are done with **datapacks**, not a plugin. **End** and **Nether** extended **down to y -64**; **Nether roof** kept at ~127 with building above; **up to 512** build height possible (e.g. Amplified).
- **Amplified:** **-128 to 512** discussed; **360** gen height mentioned to reduce plateaus. *“Amplified -128 to 512 (e.g. 360 gen to reduce plateaus).”*
- **Modrinth BuildHeightLimit** and Spigot/Paper world build limit threads were referenced; implementation is datapack-based (Nether floor -64, roof extended, End -64).

## Paper world separation

- **Paper** keeps **Nether** and **End** as **separate worlds** (separate world folders). So the 1.12TB from the previous server was the sum of overworld, nether, and end world data.

## Centralised test server

- A **centralised test server** over **SSH** was suggested for development and testing.

## Source

- Direct Messages (Grovyle187): seed “stephen holbery” / 1473117994, Amplified vs Large Biomes, Multiverse create command, BungeeCord test server.
- Bible sections 2 and 6: anti–seed-cracker, separate Amplified nether/end seed, MV5, build height datapacks.
