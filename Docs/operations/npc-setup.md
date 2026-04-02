---
title: NPC Setup
description: Citizens NPC configuration and setup guide.
tags:
  - operations
  - npc
status: reference
phase: ongoing
owner: ops
action: none
---
# NPC Setup Guide — Amos + Thornwell NPCs
> **Status:** Ready to execute once Thornwell coords are scouted in-game.
> **Seed:** `stephenholbery`


## Coords Worksheet *(fill in before starting)*

| NPC | Location | X | Y | Z | Yaw (facing) |
|-----|----------|---|---|---|--------------|
| Amos | 0,0 campfire | 0 | ? | 0 | ? |
| Village Elder | Thornwell — Elder's hall | | | | |
| Blacksmith | Thornwell — forge | | | | |
| Herbalist | Thornwell — garden/stall | | | | |
| Shrine Keeper | Thornwell — temple | | | | |
| Professor Craft | Main lab entrance (nearest volcano) | | | | |


## Step 2 — Update lw_amos compass to real lodestone compass

Current `items.yml` has a plain COMPASS with lore only — it doesn't actually point anywhere. Once you have Thornwell's coords, update it to a real lodestone compass.

Open `Server/backends/survival-1/plugins/BetonQuest/QuestPackages/lw_amos/items.yml` and replace:

```yaml
items:
  thornwell_compass:
    material: COMPASS
    name: "&6Lodestone Compass &7— Thornwell"
    lore:
      - "&7Given to you by a traveller at the edge"
      - "&7of the world. It points toward Thornwell."
      - ""
      - "&8\"Follow it northeast. You'll see the"
      - "&8smoke before you see the walls.\""
```

With (fill in `<X>`, `<Y>`, `<Z>` for Thornwell's coords):

```yaml
items:
  thornwell_compass:
    material: COMPASS
    name: "&6Lodestone Compass &7— Thornwell"
    lore:
      - "&7Given to you by a traveller at the edge"
      - "&7of the world. It points toward Thornwell."
      - ""
      - "&8\"Follow it northeast. You'll see the"
      - "&8smoke before you see the walls.\""
    nbt: '{LodestoneX:<X>,LodestoneY:<Y>,LodestoneZ:<Z>,LodestoneDimension:"minecraft:overworld",LodestoneTracked:1b}'
```

**Also:** Place a Lodestone block at exactly those coords in Thornwell (the compass needs one to lock onto). Run `/bq reload` after the edit.

**Test:** Give yourself the compass via `/bq e <your_name> give_compass` and confirm it spins toward Thornwell.


## Step 4 — Update quest package coords

Each BetonQuest package has location-based conditions or objectives that reference NPC coords. After placing each NPC, check for `location:` entries in their package's `conditions.yml` and `objectives.yml` and update to match real coords.

Packages are at:
```
Server/backends/survival-1/plugins/BetonQuest/QuestPackages/
  lw_elder/
  lw_blacksmith/
  lw_herbalist/
  lw_shrine/
  lw_professor/
```

Run `/bq reload` after each edit.


## Citizens save file

NPCs are saved to:
`Server/backends/survival-1/plugins/Citizens/saves.yml`

Back this up after placing all NPCs.
