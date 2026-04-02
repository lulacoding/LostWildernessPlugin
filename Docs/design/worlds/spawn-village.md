---
title: Spawn Village
description: Spawn village layout and design.
tags:
  - design
  - worlds
  - spawn
status: reference
phase: ongoing
owner: design
action: none
---
# Survival Spawn Village (0,0)

Part of the [worlds and gameplay](README.md) section. This page defines the **Survival spawn village at `0,0`** as the first real in-world hub players see after leaving the lobby.

It is a **protected starter hub** in the **Equator belt** (no-season zone) that:

- onboards players into the **calendar and history** identity
- enforces **class selection** before full play begins
- teaches the **no `/home` – roads, lodestones, horses** travel philosophy
- exposes core systems (clans, events, portals) without railroading
- gives subtle, early hints toward **Brotherhood** and long-term storyline


## Layout concept

At a high level, the spawn village has:

1. **Central calendar square** at or near `0,0`.
2. **Class Hall** on one side of the square.
3. **Starter inn** and **tavern/common room** opposite the Class Hall.
4. **Quartermaster’s yard and blacksmith**.
5. **Stable and travel yard**.
6. **Public shrine / small library teaser**.
7. **Marked exit roads** heading north/south/east/west.

### Central calendar square

- Contains:
  - the **calendar monument** (e.g. lectern or chiselled bookshelves with lore signs)
  - the origin for **New Year fireworks** (highest point above `0,0`)
  - a visible plaque explaining `/date`, `/time`, `/season`, and `/datejoined`
- Acts as the place where:
  - players can see **years advancing**
  - server announcements about wars or major events can be anchored

### Class Hall

- Building dedicated to:
  - **mandatory class selection** (via Citizens + Denizen)
  - future **class-specific quests** and tutorials
- Flow:
  - first-time arrivals are directed here **before** leaving spawn
  - Denizen conversation drives **class choice** and explains the role
  - class is stored in the shared DB (PlayerClass) so it persists to Amplified

### Starter inn / shelter

- A small building that:
  - visually anchors the idea of a **first safe home**
  - can host early NPCs like the Greeter and Calendar Keeper
  - provides a diegetic place for players to read signs and talk to NPCs

### Quartermaster & blacksmith

- Outdoor yard or small workshop that:
  - introduces **starter gear** and repair concepts
  - can later become a Citizens/Denizen-powered **shop** or gear unlocker
  - hints at **future economy** without requiring a full shop system yet

### Stable & travel yard

- Space that:
  - foregrounds **horses, roads, and maps**
  - can include lodestones and signage explaining how to travel without `/home`
  - later can host:
    - a mount rental NPC
    - basic road network maps (north/south/equator)

### Public shrine / library teaser

- A **small, public-facing building** that:
  - contains scripture fragments and “Sam verse” hints
  - references **truth seekers** and a hidden library, without pinning it down
  - can change its lines based on milestones in later phases
- **Not** the actual Brotherhood temple/library (those remain hidden, especially in Amplified).

### Exit roads

- Four marked paths that:
  - point **North** / **South** / **East** / **West**
  - can have signposts like:
    - \"North – winter comes soon\"
    - \"South – summer’s edge\"
    - \"Equator road – no season, shared trade routes\"
  - push players into the Survival map quickly once onboarding is done


## Onboarding flow (from docs to in-game)

### Network-level flow

1. Player joins the **lobby** and verifies via Discord.
2. Player uses the **teleporter** to reach Survival.
3. Player spawns in (or is moved to) the **spawn village at 0,0**.

### In-village flow (first-time players)

1. **Greeter** explains where they are and points them to the **Class Hall**.\n2. Player enters the **Class Hall** and talks to the **Class Master**.\n3. Player selects their **class** via Denizen dialogue.\n4. On successful selection, the Class Master (or Quartermaster):\n   - writes the class to the shared DB\n   - may grant starter kit and/or AuraSkills boosts\n5. Player is now free to tour the village:\n   - talks to the **Calendar Keeper**\n   - talks to the **Wayfinder** about roads and seasons\n   - learns from the **Event Watcher** about world events\n   - hears from the **Clan Registrar** about clans and alliances\n   - reads hints from the **Village Scribe** in the shrine\n6. Player leaves via one of the **exit roads** into normal Survival.

Later, repeat visitors can skip class selection and use the village mainly as:

- a **social hub**
- a **quest and rumor board hub**
- a **place to bring new friends for orientation**


## Implementation notes (future work)

- **Protection:** use world spawn-protection + region protection plugin to ensure the inner square cannot be griefed or PvP-killed.\n- **NPCs:** implemented via Citizens + Denizen as per the NPC/quest stack docs.\n- **Classes:** wired into the planned `PlayerClass` + AuraSkills integration in the MMO systems plan.\n- **Milestones:** use existing `Milestone`/`ProgressionService` to track \"class chosen\" or \"spawn onboarding completed\" if needed.\n- **Events:** Event Watcher can react to real event triggers later (e.g. dynamic Denizen dialogue when an eclipse is active).

