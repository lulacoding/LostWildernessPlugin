---
title: Phase 1 In-Game Setup Guide
description: Step-by-step in-game setup guide for Phase 1.
tags:
  - archive
  - roadmap
status: archive
phase: archive
owner: admin
action: none
---
# Phase 1 — In-Game Setup Guide
**Use this when:** The plugin is deployed, the Spawn Village is built, and you're ready to wire everything together.


## Step 2 — Place Citizens NPCs

Open Citizens commands in-game (you must be OP). Place each NPC at its intended location in the Spawn Village.

### Creating NPCs

```
/npc create "Village Elder" --type PLAYER
/npc create "Shrine Keeper" --type PLAYER
/npc create "Blacksmith" --type PLAYER
/npc create "Herbalist" --type PLAYER
/npc create "Merchant" --type PLAYER
```

For Professor Craft (at the volcano lab):
```
/npc create "Professor Craft" --type PLAYER
```

### After creating each NPC

Run `/npc list` to see the assigned IDs. Note each ID number — you'll need them in Step 3.

Skin each NPC:
```
/npc skin <skin_name>
```

Make them look at the nearest player:
```
/npc lookclose
```

Make them not move:
```
/npc waypoints
```
(then just don't add any waypoints)


## Step 4 — Set Professor Craft's Lab Location

Once the lab is built in the volcano biome:

1. Stand at the lab entrance and run `/coords` or note your X Y Z
2. Open `lw_professor/package.yml`
3. Find the line:
   ```yaml
   find_lab: "location 0;64;0;world;32 events:first_contact_complete"
   ```
4. Replace `0;64;0;world;32` with your actual coordinates:
   ```yaml
   find_lab: "location <X>;<Y>;<Z>;world;<radius> events:first_contact_complete"
   ```
   Use a radius of `32` (32 blocks). Example:
   ```yaml
   find_lab: "location -1240;75;884;world;32 events:first_contact_complete"
   ```
5. Run `/bq reload`


## Step 6 — Test the Village Elder Quest Chain

Right-click the Village Elder NPC. Confirm:
- [ ] First dialogue appears (intro_new branch)
- [ ] Starter kit given on first talk (iron gear + 16 bread)
- [ ] Craft a bed → Quest Complete: Home and Hearth (200 XP)
- [ ] Kill 10 mobs → Quest Complete: Proving Your Worth (400 XP)
- [ ] Enter the Nether → Quest Complete: The First Rift (800 XP + Elder's Blessing)
- [ ] Elder's final dialogue plays after all 3 done


## Step 8 — Test Story Gates

**Corrupt Portal Gate:**
- Try to light a Crying Obsidian frame with Flint & Steel without killing the Ender Dragon
- Expected: "The portal hums but will not open. Something is missing..."
- Kill the Ender Dragon → gate should open

**Nether Milestone:**
- Enter the Nether with a fresh account
- Check console for: `[story] <name> entered the Nether — MILESTONE_NETHER_PORTAL unlocked.`
- Check with `/v2progress` that the milestone is registered

**Ender Dragon Kill:**
- Kill the Ender Dragon
- Expected:
  - Server-wide broadcast: "⚡ [name] has slain the Ender Dragon!"
  - Dragon death sound for all online players
  - 3-second delay, then title for involved players: "Dragon Slain / The Redeemer will come on the next Easter..."
  - Celestial +300, Epochian +150 rep for involved players

**Wither Kill Counter (Clan):**
- Be in a clan
- Kill 6 standard Withers (not Devoiders, not El Diablo)
- Expected: Server broadcast "⚔ [Clan Name] has broken the Withering Council's threshold! The Devoid stirs..."
- MILESTONE_WITHER_6 unlocked for present players

**The Redeemer Easter Trigger:**
To test without waiting for Easter, use the calendar admin command to advance to Easter Sunday:
```
/calendar nextday
```
(repeat until you hit Easter Sunday, or use `/calendar set <date>` if available)

Expected for players with MILESTONE_ENDER_DRAGON AND positive Celestial rep:
- Gold title: "The Redeemer Arrives / Return to spawn. He awaits."
- Chat messages about the Redeemer
- FLAG_REDEEMER_PENDING set in their progression


## Quick Reference — BetonQuest Commands

| Command | What it does |
|---|---|
| `/bq reload` | Reload all quest packages (after editing YAML) |
| `/bq event <player> <package>.<event>` | Fire a BetonQuest event manually |
| `/bq condition <player> <package>.<condition>` | Check if condition is true |
| `/bq objective <player> list` | List active objectives |
| `/bq tag <player> list` | List all tags a player has |
| `/bq purge <player>` | Reset ALL BetonQuest data for a player |
| `/bq debug` | Enable debug logging |


## NPC ID Tracking (fill in as you place them)

| NPC | Citizens ID | Location | Wired to BQ? |
|---|---|---|---|
| Greeter (existing) | 0 | Spawn portal area | ✅ |
| Village Elder | ??? | Spawn Village centre | ⬜ |
| Shrine Keeper | ??? | Shrine building | ⬜ |
| Blacksmith | ??? | Forge building | ⬜ |
| Herbalist | ??? | Garden building | ⬜ |
| Merchant | ??? | Market building | ⬜ (placeholder only) |
| Professor Craft | ??? | Volcano lab | ⬜ |
| The Redeemer | ??? | 0,0 area (post-Easter) | ⬜ (script pending) |
