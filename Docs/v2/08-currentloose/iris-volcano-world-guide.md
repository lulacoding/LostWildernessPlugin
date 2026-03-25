# Iris World Generator — Full Setup Guide
# Lost Wilderness Volcanic World
**Goal:** Generate the entire survival world as volcanic terrain, with a guaranteed volcano crater near spawn containing Professor Craft's lab.

---

## Part 1 — Download & Install Iris

### 1.1 Get the Jar

Search for **"Iris World Generator"** on:
- Hangar (hangar.papermc.io) — official Paper plugin repository
- Modrinth
- GitHub: `VolmitSoftware/Iris`

Download the latest release for **Paper 1.21**. The file will be called something like `Iris-X.X.X.jar`.

### 1.2 Install

Drop the jar into:
```
Server/backends/survival-1/plugins/
```

Start the server once to let Iris generate its folder structure. You'll see:
```
plugins/Iris/
  packs/
  dimensions/
  ...
```

Stop the server again after it loads. Do not generate a world yet.

---

## Part 2 — Create the Pack

Create this entire folder structure manually inside `plugins/Iris/packs/`:

```
plugins/Iris/packs/lost-wilderness/
  pack.json
  dimensions/
    overworld.json
  regions/
    spawn_zone.json
    volcanic_region.json
  biomes/
    spawn_village_plains.json
    volcano_crater.json
    volcano_flats.json
    ash_fields.json
    obsidian_wastes.json
  objects/            ← leave empty for now, lab goes here later
```

---

## Part 3 — Write All the Files

### `pack.json`
The pack manifest. Iris needs this to recognise the pack.

```json
{
  "name": "Lost Wilderness",
  "version": "1.0.0",
  "description": "Volcanic survival world for Lost Wilderness server.",
  "author": "LostWilderness",
  "minIrisVersion": "8.0.0"
}
```

---

### `dimensions/overworld.json`
Top-level world definition. Controls sea level, world height, and which regions are used. The `focus` block guarantees a specific region around spawn.

```json
{
  "name": "Lost Wilderness Overworld",
  "version": 1,
  "seaLevel": 63,
  "dimensionHeight": 384,
  "minHeight": -64,
  "fluid": "WATER",
  "lava": "LAVA",
  "regions": [
    "volcanic_region"
  ],
  "focus": {
    "region": "spawn_zone",
    "radius": 800
  },
  "strongholds": true,
  "dungeons": true,
  "features": []
}
```

**What `focus` does:**
Within 800 blocks of 0,0, Iris uses `spawn_zone` instead of the normal region picker.
Outside 800 blocks, the normal volcanic world gen takes over.

---

### `regions/spawn_zone.json`
Controls the biomes that appear within 800 blocks of spawn.

```json
{
  "name": "Spawn Zone",
  "rarity": 1,
  "biomes": [
    {
      "biome": "spawn_village_plains",
      "rarity": 5
    },
    {
      "biome": "volcano_crater",
      "rarity": 2
    },
    {
      "biome": "ash_fields",
      "rarity": 4
    }
  ]
}
```

**Rarity guide:** Lower number = appears less often. Crater at 2 means it appears less than plains (5) — so most of spawn is buildable flat land with one clear volcano accessible nearby.

---

### `regions/volcanic_region.json`
Controls the rest of the world outside the spawn zone.

```json
{
  "name": "Volcanic Region",
  "rarity": 1,
  "biomes": [
    {
      "biome": "volcano_crater",
      "rarity": 2
    },
    {
      "biome": "volcano_flats",
      "rarity": 7
    },
    {
      "biome": "ash_fields",
      "rarity": 5
    },
    {
      "biome": "obsidian_wastes",
      "rarity": 1
    }
  ]
}
```

This makes `volcano_flats` the dominant world biome. Craters are scattered but rare (rarity 2) so they feel like special landmarks. Obsidian wastes are very rare (rarity 1) — occasional dead zones.

---

### `biomes/spawn_village_plains.json`
Flat, buildable. The Spawn Village is constructed here by hand.

```json
{
  "name": "Spawn Village Plains",
  "terrain": {
    "style": "IRIS_FLAT",
    "exponent": 1.0,
    "multiplier": 0.25,
    "heightFloor": 62,
    "heightCeil": 70
  },
  "surface": [
    { "block": "GRASS_BLOCK", "depth": 1 },
    { "block": "DIRT",        "depth": 4 },
    { "block": "STONE",       "depth": 20 }
  ],
  "ores": [
    { "block": "IRON_ORE",   "size": 8, "per_chunk": 10 },
    { "block": "COAL_ORE",   "size": 16, "per_chunk": 18 }
  ],
  "features": [
    { "type": "GRASS",  "chance": 0.4 },
    { "type": "FLOWER", "chance": 0.05 }
  ],
  "fogColor": "#C8D8E0",
  "skyColor": "#78A9FF"
}
```

---

### `biomes/volcano_crater.json`
The dramatic biome. High terrain, cone shape, lava pool at the top. Professor Craft's lab auto-places here.

```json
{
  "name": "Volcano Crater",
  "terrain": {
    "style": "IRIS_THICK",
    "exponent": 2.8,
    "multiplier": 1.6,
    "heightFloor": 80,
    "heightCeil": 175
  },
  "surface": [
    { "block": "BASALT",     "depth": 1 },
    { "block": "BLACKSTONE", "depth": 5 },
    { "block": "DEEPSLATE",  "depth": 12 },
    { "block": "STONE",      "depth": 30 }
  ],
  "ores": [
    { "block": "IRON_ORE",     "size": 8,  "per_chunk": 14 },
    { "block": "COAL_ORE",     "size": 16, "per_chunk": 20 },
    { "block": "GOLD_ORE",     "size": 4,  "per_chunk": 6  },
    { "block": "COPPER_ORE",   "size": 10, "per_chunk": 16 },
    { "block": "ANCIENT_DEBRIS","size": 1, "per_chunk": 0  }
  ],
  "features": [
    {
      "type": "LAVA_LAKE",
      "minHeight": 145,
      "maxHeight": 175,
      "chance": 0.7
    },
    {
      "type": "BASALT_PILLAR",
      "minHeight": 85,
      "maxHeight": 140,
      "chance": 0.12
    },
    {
      "type": "OBSIDIAN_SPIKE",
      "minHeight": 90,
      "maxHeight": 130,
      "chance": 0.06
    }
  ],
  "objects": [
    {
      "place": ["professor_lab"],
      "chance": 0.003,
      "minHeight": 68,
      "maxHeight": 90,
      "rotate": true
    }
  ],
  "spawnEntities": [
    { "type": "MAGMA_CUBE",  "chance": 0.08 },
    { "type": "BLAZE",       "chance": 0.03 }
  ],
  "fogColor": "#8B3A00",
  "skyColor": "#FF4500"
}
```

**`chance: 0.003` for the lab:** Very low — ensures roughly 1 lab per biome patch, not dozens. If it doesn't place automatically near spawn, you place it manually (see Part 5).

---

### `biomes/volcano_flats.json`
The dominant world biome. Flat volcanic plains between craters. Where players build bases, mine, and fight.

```json
{
  "name": "Volcano Flats",
  "terrain": {
    "style": "IRIS_ROLLING",
    "exponent": 1.5,
    "multiplier": 0.6,
    "heightFloor": 62,
    "heightCeil": 95
  },
  "surface": [
    { "block": "BASALT",      "depth": 1 },
    { "block": "GRAVEL",      "depth": 2 },
    { "block": "BLACKSTONE",  "depth": 4 },
    { "block": "STONE",       "depth": 20 }
  ],
  "ores": [
    { "block": "IRON_ORE",   "size": 8,  "per_chunk": 16 },
    { "block": "COAL_ORE",   "size": 16, "per_chunk": 22 },
    { "block": "COPPER_ORE", "size": 10, "per_chunk": 14 },
    { "block": "GOLD_ORE",   "size": 4,  "per_chunk": 4  }
  ],
  "features": [
    { "type": "BASALT_PILLAR",       "chance": 0.04 },
    { "type": "SMALL_LAVA_POOL",     "chance": 0.02 },
    { "type": "DEAD_BUSH",           "chance": 0.06 }
  ],
  "spawnEntities": [
    { "type": "ZOMBIE",   "chance": 0.15 },
    { "type": "SKELETON", "chance": 0.12 },
    { "type": "CREEPER",  "chance": 0.08 }
  ],
  "fogColor": "#5A3A1A",
  "skyColor": "#CC6622"
}
```

---

### `biomes/ash_fields.json`
Transition biome. Grey, ashy, desolate. Connects craters to the flats.

```json
{
  "name": "Ash Fields",
  "terrain": {
    "style": "IRIS_ROLLING",
    "exponent": 1.3,
    "multiplier": 0.45,
    "heightFloor": 63,
    "heightCeil": 85
  },
  "surface": [
    { "block": "GRAY_CONCRETE_POWDER", "depth": 1 },
    { "block": "GRAVEL",               "depth": 3 },
    { "block": "COBBLESTONE",          "depth": 5 },
    { "block": "STONE",                "depth": 20 }
  ],
  "ores": [
    { "block": "IRON_ORE",   "size": 8,  "per_chunk": 10 },
    { "block": "COAL_ORE",   "size": 16, "per_chunk": 24 }
  ],
  "features": [
    { "type": "GRAVEL_PATCH",           "chance": 0.12 },
    { "type": "SMALL_BASALT_CLUSTER",   "chance": 0.08 }
  ],
  "fogColor": "#777777",
  "skyColor": "#999999"
}
```

---

### `biomes/obsidian_wastes.json`
Rare, dangerous dead zones. Almost unlivable — but rich in obsidian and rare ores.

```json
{
  "name": "Obsidian Wastes",
  "terrain": {
    "style": "IRIS_THICK",
    "exponent": 1.8,
    "multiplier": 0.9,
    "heightFloor": 55,
    "heightCeil": 110
  },
  "surface": [
    { "block": "OBSIDIAN",    "depth": 1 },
    { "block": "BLACKSTONE",  "depth": 6 },
    { "block": "DEEPSLATE",   "depth": 15 }
  ],
  "ores": [
    { "block": "IRON_ORE",      "size": 6,  "per_chunk": 8  },
    { "block": "GOLD_ORE",      "size": 4,  "per_chunk": 8  },
    { "block": "DIAMOND_ORE",   "size": 3,  "per_chunk": 3  },
    { "block": "DEEPSLATE_DIAMOND_ORE", "size": 3, "per_chunk": 4 }
  ],
  "features": [
    { "type": "LAVA_LAKE",       "chance": 0.25 },
    { "type": "OBSIDIAN_SPIKE",  "chance": 0.20 }
  ],
  "spawnEntities": [
    { "type": "WITHER_SKELETON", "chance": 0.06 },
    { "type": "MAGMA_CUBE",      "chance": 0.12 }
  ],
  "fogColor": "#111111",
  "skyColor": "#220000"
}
```

---

## Part 4 — Set Iris as the World Generator

### 4.1 Edit `bukkit.yml`

Located at `Server/backends/survival-1/bukkit.yml`. Add this at the bottom:

```yaml
worlds:
  world:
    generator: Iris:lost-wilderness
```

### 4.2 Delete the Existing World

**Back it up first** if there's anything important:
```
Server/backends/survival-1/world/        ← delete this folder
Server/backends/survival-1/world_nether/ ← delete this (regenerates automatically)
Server/backends/survival-1/world_the_end/ ← delete this
```

### 4.3 Start the Server

Iris will generate the new world using your pack. Watch the console:
```
[Iris] Generating world 'world' with dimension 'lost-wilderness'
```

First boot takes longer than usual as chunks generate. This is normal.

---

## Part 5 — Build and Save Professor Craft's Lab

### 5.1 Find the Nearest Crater

Once the server is running, join and run:
```
/iris biome search volcano_crater
```

This shows the nearest chunks with that biome. Teleport there:
```
/tp <x> ~ <z>
```

Walk around until you find the crater. It should be within ~300-600 blocks of spawn due to the focus zone. If there isn't one close enough, see Part 5.4.

### 5.2 Build the Lab

Build Professor Craft's main lab structure at the crater base or on its slopes. The lab should feel like it was built into the volcanic landscape — rough, functional, slightly chaotic.

**Suggested build elements:**
- Stone/blackstone walls (blends with the biome)
- Brewing stands, cauldrons, bookshelves
- Item frames with sample jars (map items)
- A notice board (sign) outside: "KEEP OUT — Ongoing Research"
- A small tent or lean-to nearby (field lab)
- Lava viewing window (glass pane looking toward the crater)

### 5.3 Save It as an Iris Object

Stand roughly in the centre of the lab. Run:
```
/iris object save professor_lab
```

Iris will ask you to select the bounds (similar to WorldEdit's `//pos1` and `//pos2`). Select corners that encompass the whole structure.

The saved object appears at:
```
plugins/Iris/packs/lost-wilderness/objects/professor_lab.iob
```

**This means:** On future world generations, the lab will auto-place in volcano crater biomes at the coords defined in `volcano_crater.json`.

### 5.4 If No Crater Spawned Near Spawn

This can happen with unlucky world seeds. Options:

**Option A — Reroll the seed**
Delete the world folder and restart. Iris picks a new seed. Repeat until a crater lands within comfortable range of spawn.

**Option B — Force a specific seed**
In `server.properties`, set:
```properties
level-seed=<your_seed>
```
Use a seed finder tool or just test seeds manually until one puts a crater 300-600 blocks from spawn.

**Option C — Place the lab manually and skip object saving**
Just build the lab, note the coordinates, and update the BetonQuest objective:
```yaml
# lw_professor/package.yml
find_lab: "location <X>;<Y>;<Z>;world;32 events:first_contact_complete"
```
The lab won't auto-place in future world gen, but the one near spawn works perfectly.

### 5.5 Note the Lab Coordinates

Once the lab is placed (auto or manual), write down the coordinates. Update the BetonQuest script:

Open `Server/backends/survival-1/plugins/BetonQuest/QuestPackages/lw_professor/package.yml`

Find:
```yaml
find_lab: "location 0;64;0;world;32 events:first_contact_complete"
```

Replace with your actual coords:
```yaml
find_lab: "location -847;74;312;world;32 events:first_contact_complete"
```

Reload BetonQuest:
```
/bq reload
```

---

## Part 6 — Verify Everything Works

### Checklist

**World generation:**
- [ ] Spawn area (within 800 blocks) has flat plains + at least one crater
- [ ] Terrain beyond 800 blocks is volcanic (flats, craters, ash fields)
- [ ] Lava pools visible at crater tops
- [ ] Ash fields visible as grey transition zones
- [ ] Obsidian wastes exist somewhere in the world (may need to travel far)

**Iris biome check:**
Stand in each biome and run:
```
/iris biome
```
Confirms which biome you're standing in.

**Iris world info:**
```
/iris world
```
Shows the active dimension and generator pack.

**Lab placement:**
- [ ] Professor Craft's lab is within ~600 blocks of spawn
- [ ] Lab is at the base/slope of a volcano crater (not floating, not underground)
- [ ] BetonQuest objective coordinates updated

**Professor Craft quest:**
- [ ] Walking within 32 blocks of the lab triggers the `find_lab` objective completion
- [ ] First Contact conversation fires
- [ ] Achievement message appears in chat

---

## Part 7 — Tuning the World

After testing, you may want to adjust how the world feels. All changes require world regeneration (delete world folder, restart) unless you're just changing mob spawns or colors.

### Crater frequency
In `regions/volcanic_region.json`, lower the crater rarity number = more craters:
```json
{ "biome": "volcano_crater", "rarity": 1 }  // very common
{ "biome": "volcano_crater", "rarity": 3 }  // rarer
```

### Crater height
In `biomes/volcano_crater.json`, adjust `heightCeil` to make taller or shorter volcanoes:
```json
"heightCeil": 200  // taller, more dramatic
"heightCeil": 130  // smaller, less extreme
```

### Spawn zone size
In `dimensions/overworld.json`, adjust the focus radius:
```json
"radius": 600   // tighter spawn area
"radius": 1200  // more area around spawn uses spawn_zone biomes
```

### Ore richness
Each biome's `ores` array controls per-chunk ore count. Increase `per_chunk` values for richer mining.

---

## Part 8 — Ongoing: Amplified World (Father of Ender)

The Amplified server (`survival-amplified` or `amplified-1`) uses extreme terrain. Iris also supports this — you'd create a separate pack `lost-wilderness-amplified` with:
- Much higher `heightCeil` values (up to 320)
- Steeper `exponent` values (3.5+)
- More extreme multiplier (2.0+)
- Same biome types but dramatically more vertical

That's a separate setup task for when you're ready to build the Amplified world.

---

## Quick Reference

| Command | What it does |
|---|---|
| `/iris biome` | Show biome you're standing in |
| `/iris biome search <name>` | Find nearest chunk with that biome |
| `/iris world` | Show active world/dimension info |
| `/iris object save <name>` | Save a structure as an Iris object |
| `/iris reload` | Reload pack files without restart |
| `/iris create <world> <pack>` | Create a new world with a pack |
| `/iris studio` | Opens visual pack editor (optional) |

---

## File Locations Summary

| File | Path |
|---|---|
| Pack manifest | `plugins/Iris/packs/lost-wilderness/pack.json` |
| Dimension | `plugins/Iris/packs/lost-wilderness/dimensions/overworld.json` |
| Spawn zone region | `plugins/Iris/packs/lost-wilderness/regions/spawn_zone.json` |
| Volcanic region | `plugins/Iris/packs/lost-wilderness/regions/volcanic_region.json` |
| Spawn plains biome | `plugins/Iris/packs/lost-wilderness/biomes/spawn_village_plains.json` |
| Volcano crater biome | `plugins/Iris/packs/lost-wilderness/biomes/volcano_crater.json` |
| Volcano flats biome | `plugins/Iris/packs/lost-wilderness/biomes/volcano_flats.json` |
| Ash fields biome | `plugins/Iris/packs/lost-wilderness/biomes/ash_fields.json` |
| Obsidian wastes biome | `plugins/Iris/packs/lost-wilderness/biomes/obsidian_wastes.json` |
| Professor lab object | `plugins/Iris/packs/lost-wilderness/objects/professor_lab.iob` |
| BetonQuest prof script | `plugins/BetonQuest/QuestPackages/lw_professor/package.yml` |
