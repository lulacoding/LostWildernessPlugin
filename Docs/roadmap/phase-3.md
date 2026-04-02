---
title: Phase 3
description: Phase 3 long-range feature planning.
tags:
  - roadmap
  - phase-3
status: planned
phase: phase-3
owner: dev
action: needs-dev
---
# Lost Wilderness — Phase 3 Roadmap
> **Last Updated:** 2026-04-02
> **Phase 3 Ends When:** El Diablo defeated (Good path) → `MILESTONE_EL_DIABLO` → Aether Portal buildable → Phase 4 (Heavenly Trials) begins.
> **Completion Tracking:** 40% (Devoider 6) → 50% (El Diablo)


## GATE 0 — Entry: Reach El Diablo's Lair

> **Goal:** A player with `MILESTONE_DEVOIDER_6` can travel to the Amplified Nether Roof, enter El Diablo's Lair, find the Wither Rose altar, and receive Kraft's warning.
>
> **Complete when:** Lair is accessible, altar is present and functional, Kraft note delivered.

### World Build *(blocking everything in this gate — do first)*

- ❌ **El Diablo's Lair — Bedrock Castle** `[L]` — Pre-built structure on the Amplified Nether Roof. Build via WorldEdit schematic. Layout:
  - **Entrance hall** — corridor leading to Wither Rose altar (PDC-tagged block)
  - **Dungeon levels** — multi-floor: traps (tripwires, pistons, lava drips), Wither Skeleton spawners, branching corridors
  - **Giant Wither Skeleton chamber** — mid-dungeon open room (Phase 2 miniboss arena)
  - **Boss arena** — large central chamber; obsidian cage structure centred (3–4 breakable segments); Wither Roses decorating walls and floor
  - Note exact coords: altar position, arena boss spawn point, obsidian cage block positions (needed for schematic restore)
- ❌ **Amplified Nether Roof access** `[S]` — Same approach as Phase 2 Survival roof. Seed a bedrock gap or portal entrance on the Amplified Nether ceiling. Record coords. Place crying obsidian portal frame on roof surface (entry gated by `MILESTONE_DEVOIDER_6`).

### Code

- ✅ **`PortalInteractListener.java`** — Lair entry portal type `PORTAL_EL_DIABLO_LAIR` added in Phase 2 Gate 1. Verify it's wired correctly to Amplified server.
- ❌ **`AchievementKey.java`** `[S]` — Add `MILESTONE_EL_DIABLO` and `FLAG_EVIL_PATH` constants.
- ❌ **`WitherRoseAltarListener.java`** `[S]` — New listener. On player right-clicking PDC-tagged altar block with a Wither Rose in hand:
  1. Set `FLAG_EVIL_PATH` on player (via ProgressionService)
  2. Apply -150 Celestial reputation, +100 Corrupted reputation (via ReputationService)
  3. Send title: *"You have pledged yourself to darkness."*
  4. Teleport player to Moon entry point (stub: send message *"The Moon awaits... (coming in Phase 4)"* until Phase 4 is built)
  5. Consume the Wither Rose from player's hand
- ❌ **`StoryModule.java`** `[S]` — Register `WitherRoseAltarListener`.

### NPC / Story

- ❌ **`lw_kraft_phase3.yml`** BetonQuest `[S]` — Kraft field note on Lair entry (triggered by entering a WorldGuard region or stepping on a pressure plate at the entrance):
  *"Be careful. The castle absorbs Nether energy. Don't let it consume you."*

### Ops / Testing

- [ ] Smoke test: Player with `MILESTONE_DEVOIDER_6` → Amplified Nether Roof portal activates ✓
- [ ] Smoke test: Lair structure loads correctly — no floating/missing blocks ✓
- [ ] Smoke test: Wither Rose placed on altar → `FLAG_EVIL_PATH` set, rep changes apply, teleport stub fires ✓
- [ ] Verify: Evil path player sees correct title + correct rep delta ✓
- [ ] Verify: Kraft note delivered on entry ✓


## GATE 2 — Aquaria Reveal: The Aether Portal

> **Goal:** Players with `MILESTONE_EL_DIABLO` can build a glowstone frame, right-click with a water bucket, and activate the Aether Portal to Aquaria.
>
> **Complete when:** Aether Portal activates and transports player to Aquaria (or stub destination).

### Code

- ❌ **`PortalInteractListener.java`** `[S]` — Add new portal type `PORTAL_TYPE_AETHER`:
  - Detect: glowstone frame (same frame-detection logic as crying obsidian, swap material to `GLOWSTONE`) + player right-clicks with `WATER_BUCKET`
  - Gate: player must have `MILESTONE_EL_DIABLO`
  - Action: consume water bucket → teleport player to Aquaria server/world
  - Aquaria stub: until Phase 4 world is built, send message *"Aquaria awaits... (coming soon)"* and do not teleport

### World Build

- ❌ **Aether Portal example frame** `[S]` — *(Optional but recommended)* Pre-built glowstone frame near Thornwell or at a discoverable above-ground location. Acts as a discoverable hint. No function until `MILESTONE_EL_DIABLO` is met.

### Ops / Testing

- [ ] Test: Glowstone frame + water bucket right-click → portal activates for player with `MILESTONE_EL_DIABLO` ✓
- [ ] Test: Portal **blocked** (sends error message) for player without `MILESTONE_EL_DIABLO` ✓
- [ ] Test: Portal teleports to Aquaria stub destination ✓


*See also: [PHASE2-ROADMAP.md](PHASE2-ROADMAP.md) · [PHASE1-ROADMAP.md](PHASE1-ROADMAP.md) · [phase2-phase3-design-spec.md](v2/05-planning/phase2-phase3-design-spec.md)*
