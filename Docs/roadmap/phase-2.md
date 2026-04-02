---
title: Phase 2
description: Phase 2 planned scope and feature list.
tags:
  - roadmap
  - phase-2
status: planned
phase: phase-2
owner: dev
action: needs-dev
---
# Lost Wilderness — Phase 2 Roadmap
> **Last Updated:** 2026-04-02
> **Phase 2 Ends When:** Clan Devoider kill count reaches 6 → `MILESTONE_DEVOIDER_6` → Phase 3 (El Diablo's Lair) unlocks.
> **Completion Tracking:** 30% (Wither 36) → 40% (Devoider 6)


## GATE 0 — Entry: The Devoid Opens

> **Goal:** A player whose clan has 36 Wither kills can activate the Devoid portal and arrive on the Survival Nether Roof.
>
> **Complete when:** Portal activates, player arrives on Nether Roof, Devoiders are present.

### Code

- ✅ **`PortalInteractListener.java`** — Devoid portal gate (clan Wither count ≥ 36 / solo ≥ 6) already implemented. Needs smoke test.
- ✅ **`WitherKillListener.java`** — Per-clan Wither counter implemented and wired.
- ✅ **`RoofWitherListener.java`** — 6 Withers on Nether Roof → transform into 6 Devoiders. Implemented in BossModule.
- ❌ **`AchievementKey.java`** `[S]` — Add `MILESTONE_DEVOIDER_6` constant.
- ❌ **`ClanRepository.java`** `[S]` — Add `clan_devoider_kills` column. Add `incrementDevoiderKills(clanId)` and `getDevoiderKills(clanId)` async methods. Add DB migration.
- ❌ **`DevoiderKillListener.java`** `[S]` — New listener. On MythicMobs Devoider entity death:
  1. Identify killing player's clan
  2. Increment `clan_devoider_kills` in DB
  3. At count 6: unlock `MILESTONE_DEVOIDER_6`, broadcast title/subtitle to clan, send Kraft instruction message
  4. Solo player (1-person clan): same path, no scaling differences
- ❌ **`StoryModule.java`** `[S]` — Register `DevoiderKillListener` (same pattern as `WitherKillListener`).

### World Build

- ❌ **Survival Nether Roof access** `[S]` — Create a bedrock gap or portal-entry in the Survival Nether ceiling so players can reach the roof. Record exact coords. (Players need a way up that isn't a game exploit.)
- ❌ **Nether Roof atmosphere** `[S]` — Scatter Wither Skeleton skulls and Nether Brick debris across roof surface to signal danger. No major structures needed — Devoiders roam freely.

### NPC / Story

- ❌ **`lw_kraft_phase2.yml`** BetonQuest `[S]` — Kraft field note triggered on first Devoider kill:
  *"Remarkable. These creatures are not Withers — they have been transformed. Something is drawing them to the roof. Follow the energy signature to the Amplified world."*
  Delivered as a book item sent to the player's inventory or via title sequence.

### Ops / Testing

- [ ] Smoke test: Clan at 36 Wither kills → Devoid portal (crying obsidian frame) activates ✓
- [ ] Smoke test: Player passes through portal and arrives on Survival Nether Roof ✓
- [ ] Verify: 6 Devoiders are present and aggressive on the roof ✓
- [ ] Verify: Kraft note delivered on first Devoider encounter ✓


## Phase 2 Summary Checklist

### Critical Path

```
[ ] Add MILESTONE_DEVOIDER_6 to AchievementKey.java
[ ] Add clan_devoider_kills to ClanRepository.java (+ DB migration)
[ ] Implement DevoiderKillListener.java
[ ] Register DevoiderKillListener in StoryModule.java
[ ] Seed Nether Roof bedrock gap (record coords)
[ ] Scatter Nether Roof atmosphere props
[ ] Add PORTAL_EL_DIABLO_LAIR gate to PortalInteractListener.java
[ ] Write lw_kraft_phase2.yml BetonQuest package
[ ] Gate 0 smoke tests pass
      ↓
[ ] Verify Devoider kill tracking end-to-end in DB
[ ] Gate 1 tests pass → Phase 3 begins
```

### Code Still Needed

| File | Gate | Size |
|------|------|------|
| `AchievementKey.java` (add `MILESTONE_DEVOIDER_6`) | Gate 0 | S |
| `ClanRepository.java` (add devoider kill tracking) | Gate 0 | S |
| `DevoiderKillListener.java` (new) | Gate 0 | S |
| `StoryModule.java` (register new listener) | Gate 0 | S |
| `PortalInteractListener.java` (add Lair portal gate) | Gate 1 | S |
| BetonQuest `lw_kraft_phase2.yml` (new) | Gate 0 | S |

### Biggest Time Sinks

1. **Nether Roof access design** — needs in-world decision on how players get up
2. **MythicMobs Devoider entity tag** — verify correct entity type string for kill detection in `DevoiderKillListener`

---

*See also: [PHASE1-ROADMAP.md](PHASE1-ROADMAP.md) · [PHASE3-ROADMAP.md](PHASE3-ROADMAP.md) · [phase2-phase3-design-spec.md](v2/05-planning/phase2-phase3-design-spec.md)*
