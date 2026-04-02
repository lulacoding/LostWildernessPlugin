# NPC Setup Guide — Amos + Thornwell NPCs
> **Status:** Ready to execute once Thornwell coords are scouted in-game.
> **Seed:** `stephenholbery`

---

## Prerequisites

1. Server running with Citizens plugin loaded
2. BetonQuest plugin loaded
3. You are OP in-game
4. Thornwell coords scouted (fill in below before starting)

---

## Coords Worksheet *(fill in before starting)*

| NPC | Location | X | Y | Z | Yaw (facing) |
|-----|----------|---|---|---|--------------|
| Amos | 0,0 campfire | 0 | ? | 0 | ? |
| Village Elder | Thornwell — Elder's hall | | | | |
| Blacksmith | Thornwell — forge | | | | |
| Herbalist | Thornwell — garden/stall | | | | |
| Shrine Keeper | Thornwell — temple | | | | |
| Professor Craft | Main lab entrance (nearest volcano) | | | | |

---

## Step 1 — Create Amos NPC (0,0 campfire)

Stand at the campfire location, then run:

```
/npc create Amos --type PLAYER
/npc skin stephenholbery
/npc look
/npc save
```

Get the NPC's ID (shown in chat or run `/npc list`). Then link to BetonQuest:

```
/npc select <id>
/npc edit bqconversation lw_amos.Amos
```

**Verify:** Right-click the NPC → conversation opens → he offers the compass.

The `lw_amos` package already exists at:
`Server/backends/survival-1/plugins/BetonQuest/QuestPackages/lw_amos/`

**Update the compass target coords** in `lw_amos/events.yml` — find the `give_compass` event and set the Lodestone target to Thornwell's actual X/Z.

---

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

---

## Step 3 — Create Thornwell NPCs

For each NPC, stand at their spot and repeat:

```
/npc create <Name> --type PLAYER
/npc look
/npc save
/npc select <id>
/npc edit bqconversation <package>.<ConversationName>
```

| NPC | Package | Conversation name |
|-----|---------|-------------------|
| Village Elder | `lw_elder` | `Elder` |
| Blacksmith | `lw_blacksmith` | `Blacksmith` |
| Herbalist | `lw_herbalist` | `Herbalist` |
| Shrine Keeper | `lw_shrine` | `ShrineKeeper` |
| Professor Craft | `lw_professor` | `ProfessorCraft` |

---

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

---

## Step 5 — Smoke test

For each NPC:
- [ ] Right-click → conversation opens
- [ ] First quest objective given correctly
- [ ] No BetonQuest errors in console

For Amos specifically:
- [ ] Compass given on interact
- [ ] Compass points to Thornwell

---

## Citizens save file

NPCs are saved to:
`Server/backends/survival-1/plugins/Citizens/saves.yml`

Back this up after placing all NPCs.
