---
title: Iris Volcano Guide
description: Iris world volcanic spawn region setup guide.
tags:
  - archive
  - worlds
status: archive
phase: archive
owner: admin
action: none
---
# Iris World Generator — Full Setup Guide
# Lost Wilderness Volcanic World
**Goal:** Generate the entire survival world as volcanic terrain, with a guaranteed volcano crater near spawn containing Professor Craft's lab.


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


## Part 8 — Ongoing: Amplified World (Father of Ender)

The Amplified server (`survival-amplified` or `amplified-1`) uses extreme terrain. Iris also supports this — you'd create a separate pack `lost-wilderness-amplified` with:
- Much higher `heightCeil` values (up to 320)
- Steeper `exponent` values (3.5+)
- More extreme multiplier (2.0+)
- Same biome types but dramatically more vertical

That's a separate setup task for when you're ready to build the Amplified world.


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
