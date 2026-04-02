---
title: Phase 1
description: Phase 1 implementation scope and completion record.
tags:
  - roadmap
  - phase-1
status: implemented
phase: phase-1
owner: dev
action: none
---
# Lost Wilderness — Phase 1 Roadmap
> **Last Updated:** 2026-04-02
> **Goal of Phase 1:** Players can join, complete the personality quiz, explore the world, do quests, kill the Ender Dragon, fight the Father of Ender, and eventually trigger Phase 2 by killing 36 Withers as a clan.


## GATE 0 — First Join Experience

**Done When:** A brand new player can join, complete the personality quiz, enter Survival, walk to Thornwell, and talk to NPCs who give them quests — all without anything breaking.


### Step 2 — Code Tasks

These can be done while Step 1 is in progress. Claude handles these.

- ✅ `[CODE]` **Wither kill counter** — Tracks clan Wither kills toward the Phase 2 trigger (36 kills). Done.
- ✅ `[CODE]` **Intro cutscene on first join** — Plays a story intro the first time a player enters Survival. Done.
- ❌ `[CODE]` **Economy module** *(optional for soft launch)* — Wallet/balance system. Only needed for the Merchant NPC to work fully. Can skip for Day 1 if the Merchant isn't placed yet.


### Step 4 — Update Quest Coordinates

Every BetonQuest package has hardcoded coordinates that point to the old locations. After placing NPCs, update each package with the real Thornwell coordinates.

- ✅ `[QUEST]` **`lw_elder`** — Village Elder tutorial chain (bed → shelter → kill mobs → Nether). **Needs coord update to Thornwell.**
- ✅ `[QUEST]` **`lw_blacksmith`** — Gear quests. **Needs coord update.**
- ✅ `[QUEST]` **`lw_herbalist`** — Potions/farming quests. **Needs coord update.**
- ⚠️ `[QUEST]` **`lw_shrine`** — Zodiac intro. Base package exists but the 13 sign-specific dialogue branches need checking. **Needs coord update.**
- ⚠️ `[QUEST]` **`lw_professor`** — First Contact quest. Foundation exists but full chain needs verification. **Needs coord update.**


## GATE 1 — Chapter 1: The First Flight

**Done When:** A player can kill the Ender Dragon, and the server correctly unlocks their progress flags and opens the Corrupt Portal.


### Step 2 — Code Check

- ✅ `[CODE]` **Ender Dragon kill listener** — Exists. Unlocks `MILESTONE_ENDER_DRAGON` and sets `FLAG_REDEEMER_PENDING`. Needs in-game test.
- ✅ `[CODE]` **Nether entry listener** — Exists. Unlocks `MILESTONE_NETHER_PORTAL` (marks 10% story progress).
- ✅ `[CODE]` **Corrupt Portal gate** — Exists. Portal is blocked before Ender Dragon kill, opens after.
- [ ] `[BUILD]` **Crying Obsidian availability** — Players need Crying Obsidian to build the Corrupt Portal frame. Decide: place an example frame near Thornwell, OR ensure it drops from End chests. This needs to exist before players can progress to Gate 2.


## GATE 2 — Chapter 2: Splitting the Firmament

**Done When:** Players can enter the Amplified world via the Corrupt Portal, kill the Father of Ender, and The Redeemer NPC appears on Easter.


### Step 2 — NPC and Quest

- [ ] `[BUILD]` **Place The Redeemer NPC** — Configure a Citizens NPC that conditionally spawns at 0,0 on Easter after the Ender Dragon is killed. This is triggered automatically by `RedeemerEasterListener.java` — you just need the NPC configured in Citizens.
- [ ] `[QUEST]` **`lw_redeemer` BetonQuest package** — Two dialogue states:
  - *After Ender Dragon kill:* Reveals Corrupt Portal mechanics, sells Crying Obsidian to players with good Celestial reputation. Foreshadowing dialogue about something stirring in the Nether.
  - *After Father of Ender kill:* Reveals Heaven's Gate at Y=602. Tells players to hunt Withers.


## GATE 3 — Phase 1 Complete: The Withering Council

**Done When:** Clan Wither kill counter is live and working, and at 36 kills the Devoid portal becomes accessible.


### Gate 3 Tests

**Test A — Wither kill increments counter**
1. Spawn and kill a standard Wither in Survival (use `/mm mobs spawn Wither` or place Wither skulls manually).
2. Check `/clan info` (or whatever display was implemented).
3. ✅ Pass if: kill count increments by 1.

**Test B — Devoider does NOT count**
1. Spawn and kill a Devoider (the custom Phase 2 Wither variant).
2. Check kill count.
3. ✅ Pass if: kill count stays the same.

**Test C — Amplified Wither kills count too**
1. Kill a standard Wither in the **Amplified world**.
2. Check kill count.
3. ✅ Pass if: count increments (both worlds should count).

**Test D — Devoid portal blocked under 36 kills**
1. Find or build the Devoid portal frame.
2. Try to activate it with fewer than 36 kills.
3. ✅ Pass if: portal does NOT activate.

**Test E — Devoid unlocks at 36 kills**
> Use a test command or temporarily lower the threshold to verify this without killing 36 Withers manually.
1. Reach 36 clan Wither kills.
2. Try to activate the Devoid portal.
3. ✅ Pass if: portal activates and you're teleported to The Devoid. Phase 2 begins.


## Critical Path Summary

```
1. Lock world seed (stephenholbery) ✅
2. Build Thornwell + landmarks + Professor lab  [BUILD]
3. Place all Phase 1 NPCs with correct coordinates  [BUILD]
4. Update BetonQuest package coordinates  [QUEST]
5. Run Gate 0 tests (A through G) — all must pass
         ↓
6. Verify lw_elder leads players toward Ender Dragon  [QUEST]
7. Verify all 13 zodiac sign dialogues work  [QUEST]
8. Confirm Crying Obsidian is obtainable  [BUILD]
9. Run Gate 1 tests (A through D) — all must pass
         ↓
10. Build Father of Ender MythicMobs config  [CODE]
11. Build Heaven's Gate placeholder at Y=602  [BUILD]
12. Configure The Redeemer NPC  [BUILD]
13. Write lw_redeemer BetonQuest package  [QUEST]
14. Run Gate 2 tests (A through E) — all must pass
         ↓
15. Implement Wither kill count display  [CODE]
16. Run Gate 3 tests (A through E) — all must pass
         ↓
Phase 1 complete → Phase 2 begins
```

---

*See also: [PHASE2-ROADMAP.md](PHASE2-ROADMAP.md) · [phase-1-gap-report.md](v2/05-planning/phase-1-gap-report.md)*
