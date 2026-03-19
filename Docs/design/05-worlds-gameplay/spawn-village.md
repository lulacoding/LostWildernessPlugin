# Survival Spawn Village (0,0)

Part of the [worlds and gameplay](README.md) section. This page defines the **Survival spawn village at `0,0`** as the first real in-world hub players see after leaving the lobby.

It is a **protected starter hub** in the **Equator belt** (no-season zone) that:

- onboards players into the **calendar and history** identity
- enforces **class selection** before full play begins
- teaches the **no `/home` – roads, lodestones, horses** travel philosophy
- exposes core systems (clans, events, portals) without railroading
- gives subtle, early hints toward **Brotherhood** and long-term storyline

---

## High-level role

- **World:** Survival overworld.
- **Coordinates:** centered on **`(0,0)`** in the overworld.
- **Zone:** within the **Equator** (no-season belt) so `/season` shows \"Equator\" and \"No Season\".
- **Protection:** a **safe, no-build, no-PvP inner square** with controlled exits into normal Survival.
- **Symbolism:** shares `0,0` with **New Year fireworks** (calendar turning points happen above this village).

Spawn village is where players:

- first see **calendar years**, **day count**, and the idea of shared history
- choose their **class** (future PlayerClass / AuraSkills integration)
- learn that **travel is physical**, not warp-based
- get their **first hints** about the Brotherhood and the 7-chapter arc

---

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

---

## NPC roster at spawn (first wave)

Spawn village uses Citizens NPCs scripted with Denizen. The first-wave roles:

### Greeter / Warden of the Gate

- Location: near the Survival arrival point, facing the square.
- Purpose:
  - welcomes new arrivals from the lobby
  - explains that Survival is the **real world**, not a lobby
  - directs players to the **Class Hall** as the first task

### Class Master (or class mentors)

- Location: inside the Class Hall.
- Purpose:
  - runs the **mandatory class selection** flow
  - outlines available classes and what they mean
  - persists the choice to the shared database for cross-server use
- Implementation ideas:
  - a single NPC with dialogue options for classes, **or**
  - one NPC per class clustered in the hall

### Calendar Keeper

- Location: near the calendar monument in the central square.
- Purpose:
  - explains `/date`, `/time`, `/season`, `/datejoined`
  - introduces the idea that **this world has a history and named years**
  - may point at New Year fireworks and the meaning of `0,0`

### Wayfinder / Cartographer

- Location: at the stable/travel yard or by the exit roads.
- Purpose:
  - teaches **no `/home`** and the importance of **roads, lodestones, and maps**
  - explains Equator vs North vs South in simple terms
  - can hint at **long trade routes**, equatorial caravans, and nomad lifestyles

### Event Watcher

- Location: on a balcony or tower overlooking the square.
- Purpose:
  - warns about **eclipse, fog, thunder, blizzards, heatwaves**
  - gives simple rules like: \"If the sky goes dark and time stops, stay together.\"
  - reinforces that **sleep may be blocked** during certain events

### Clan Registrar

- Location: near the inn or a small administration building.
- Purpose:
  - introduces `/clan` and `/alliance`
  - explains that:
    - players can remain **nomads**
    - or form **clans and alliances** and participate in wars
  - points to `[Docs/player/clans.md]`-equivalent in-game help via dialogue

### Quartermaster / Smith

- Location: in the yard or smithy.
- Purpose:
  - gives **starter supplies** (possibly class-specific)
  - hints at future **shop GUI** and economy features
  - can be wired to MythicMobs drops or AuraSkills class bonuses later

### Village Scribe

- Location: in the public shrine or a small office near it.
- Purpose:
  - tells the first **lore hint** about the Brotherhood and truth seekers
  - may change dialogue as the world progresses (via milestones)
  - points curious players towards exploration and scripture, without spoilers

---

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

---

## Lore boundaries at spawn

Spawn is meant to **tease**, not fully reveal, the deeper story.

Allowed at spawn:

- explicit references to:
  - the **calendar** and named years
  - the idea of **Epoch** and early history
  - **truth seekers** and hidden knowledge
- hints that:
  - a **Brotherhood-like group** exists somewhere in the world
  - scripture, verses, and riddles will matter later
  - there are **chapters** to the story beyond simple survival

Not allowed at spawn:

- the exact **location** or structure of the Brotherhood temple/library.\n- explicit directions to Amplified’s desert/mesa **Brotherhood outpost**.\n- full explanations of:\n  - **Devoiders**, **El Diablo**, or the multi-phase boss arcs\n  - **Aquaria**, **Heavenly Tower**, **Gabriel**, **Lord’s Plateau**\n  - **Elemental Temples** and **Galactic Journey** planets\n- any cutscene that spoils mid- or late-game gates before players are ready.

Spawn should feel like **Chapter 1 – The Wilderness** from the storyline docs: orientation, first hints, and identity-building, not a lore dump.

---

## Implementation notes (future work)

- **Protection:** use world spawn-protection + region protection plugin to ensure the inner square cannot be griefed or PvP-killed.\n- **NPCs:** implemented via Citizens + Denizen as per the NPC/quest stack docs.\n- **Classes:** wired into the planned `PlayerClass` + AuraSkills integration in the MMO systems plan.\n- **Milestones:** use existing `Milestone`/`ProgressionService` to track \"class chosen\" or \"spawn onboarding completed\" if needed.\n- **Events:** Event Watcher can react to real event triggers later (e.g. dynamic Denizen dialogue when an eclipse is active).

