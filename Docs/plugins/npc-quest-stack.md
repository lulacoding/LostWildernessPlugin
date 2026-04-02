---
title: NPC & Quest Stack
description: Citizens, BetonQuest, and NPC quest integration setup.
tags:
  - plugins
  - npc
  - quests
status: planned
phase: phase-2
owner: dev
action: needs-dev
---
# NPC, Quest, And RPG Stack

Part of the [plugins and modules section](README.md). This page captures the recommended external-plugin stack for turning Lost Wilderness into a more MMO-like survival server without rebuilding every system from scratch in Java.

Implementation reality: use [implementation status](../../implementation-status.md) for what is already in code. This page is the recommended direction.


## Recommended alternatives

### If you want easier quest setup over scripting depth

- **Citizens**
- **BeautyQuests**
- **MythicMobs**
- **AuraSkills**

This is the easiest admin workflow, but less expressive than Denizen for lore-heavy storytelling.

### If you want a stronger quest framework than BeautyQuests

- **Citizens**
- **BetonQuest**
- **MythicMobs**
- **AuraSkills**

This is the best alternative to Denizen if you want a more formal quest engine with branching progression.

### Plugins not recommended as the primary foundation

- **MMOCore**: too invasive unless Lost Wilderness becomes a full prefab MMO framework
- **ValhallaMMO**: interesting, but more of a total RPG overhaul than a layered lore/survival MMO
- **ZNPCs** and other Citizens replacements: not as proven or flexible for this project
- **EliteMobs**: usable, but MythicMobs is a better fit for custom lore bosses


## What can be replaced or treated as temporary

If the recommended plugin stack is adopted, the following custom systems become optional or temporary:

### Likely replace

- custom skill system (`skills/*`, `player_skills`) if AuraSkills becomes the source of truth
- future custom NPC dialogue engine work
- future custom quest engine work
- future custom NPC shop engine work

### Maybe keep

- custom wallet/economy if Lost Wilderness wants a lore-specific currency
- custom party system if tighter boss/progression integration is more important than using a general party plugin
- custom reputation system if factions remain strongly tied to story and world-state

### Rule of thumb

- Use external plugins for **authoring and player-facing RPG systems**
- Use custom code for **progression, persistence, lore gating, and cross-server rules**

### Official-doc-based refinement

- keep **Citizens** as the NPC base layer
- keep **Denizen** as the documented quest/conversation/cutscene layer
- prefer **AuraSkills** over a custom skill tree if it becomes the source of truth, because it already documents menus, rewards, loot, API access, and SQL-friendly server use
- prefer **MythicMobs** over a custom combat-content engine because it already documents mob skills, factions, threat tables, AI controls, spawners, and boss scripting


## What each documented plugin should own

### Citizens

Use Citizens as the **NPC entity layer**:

- named NPCs in villages, outposts, shrines, towers, and settlements
- stationing, pathing, looking at players, and static placement
- built-in text/shop/waypoint traits where useful
- persistent NPC identities and editable roles

The Citizens API/docs make it clear that this is the correct base layer for server-side NPC entities.

### Denizen

Use Denizen as the **interaction and scripting layer**:

- quest conversations
- branching dialogue
- shop access through conversations or scripted GUIs
- milestone-aware dialogue
- scripted NPC behaviors and world interactions
- cutscenes using walking, rotation, and scripted scenes

The Denizen Citizens guide explicitly highlights:

- **questing**
- **conversations**
- **shops**
- **cutscenes**

That is an almost exact match for what Lost Wilderness needs from an MMORPG layer.

### MythicMobs

Use MythicMobs as the **combat and encounter layer**:

- minibosses near villages and ruins
- corrupted mobs and regional elites
- temple guardians
- custom spawners and location-based encounters
- faction-tagged enemies
- scalable bosses and scripted combat phases

The official docs that matter most for Lost Wilderness are:

- **mob skills**
- **threat tables**
- **mob factions**
- **AI controls**
- **spawners**
- **spawning control**

### AuraSkills

Use AuraSkills as the **player skill progression layer**:

- skill XP and level progression
- menus like `/skills` and `/stats`
- skill rewards and progression tuning
- loot/reward configuration
- API-based hooks from your custom plugin

AuraSkills already documents a stable Bukkit API and built-in commands/menus, so it should be treated as the source of truth for general skill progression if adopted.


## Good village themes for Survival

- **Starter trade villages**: bread, tools, directions, beginner quests
- **Brotherhood outposts**: scripture hints, chapter lore, milestone-aware dialogue
- **Hunter camps**: combat and mob-hunting tasks
- **Pilgrim shrines**: small lore locations with low-density NPC populations
- **Fishing hamlets**: lead-ins for Aquaria and ocean progression
- **Mountain monasteries**: skill trainers, heavenly/firmament foreshadowing
- **Ruined settlements**: lore-rich but hostile, ideal for MythicMob encounters


## Suggested implementation order

1. Pick the external stack: `Citizens + Denizen + MythicMobs + AuraSkills`
2. Freeze or remove plans for a full custom skill and quest engine
3. Build a thin **bridge layer** in the Lost Wilderness plugin:
   - milestone writes
   - quest completion hooks
   - reputation hooks
   - world-village registration
4. Prototype one random village template
5. Add 2-3 Citizens NPCs with Denizen dialogue
6. Connect one NPC to a real milestone or reputation check
7. Expand into biome-specific villages and settlement categories


## First playable prototype

If you want the single best "build this first" target, make this:

### Prototype: Brotherhood desert outpost

#### Components

- one small desert outpost structure generated in Survival
- three Citizens NPCs:
  - **scribe**
  - **merchant**
  - **watchman**
- Denizen dialogue for all three
- one milestone check:
  - if player has `DEVOIDER_1`, new dialogue unlocks
- one reputation outcome:
  - helping the outpost gives `EPOCHIANS` reputation
- one MythicMobs miniboss nearby:
  - corrupted raider / corrupted beast
- one village board quest:
  - "clear the nearby corruption"

#### Why this is the best prototype

- it fits the lore
- it tests NPCs, dialogue, reputation, mobs, and exploration in one package
- it does not depend on finishing the whole chapter system
- it can later scale into Brotherhood networks, rumor chains, and chapter clues

---

## Practical decision summary

If the goal is:

- **deep lore and custom interactions** -> choose `Citizens + Denizen`
- **easy quest authoring** -> choose `BeautyQuests`
- **formal quest system with stronger branching** -> choose `BetonQuest`
- **custom mobs and bosses** -> choose `MythicMobs`
- **skill progression without custom maintenance burden** -> choose `AuraSkills`

For Lost Wilderness specifically, the best overall match remains:

- **Citizens**
- **Denizen**
- **MythicMobs**
- **AuraSkills**

