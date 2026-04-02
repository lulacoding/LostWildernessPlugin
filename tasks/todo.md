# Task: Gate 0 Code — Unblocked Starters — 2026-04-01

## Context
Seed not yet confirmed → world builds blocked. These three tasks are pure code, no world coords needed. Complete them while waiting on seed lock.

Reference: `Docs/PHASE1-ROADMAP.md` → Gate 0 Code section.

---

## Plan

### Step 1 — WitherKillListener.java `[S]`
New listener in `PluginV2/src/main/java/com/lostwilderness/rpgcore/`.
- Detects standard Wither kills (entity type = WITHER, **not** MythicMobs Devoider — check MM API)
- Increments per-clan kill counter in DB (new `wither_kills` column or table — check ClanRepository schema first)
- At clan count ≥ 36, unlocks Devoid access (set flag or call portal gate — check PortalInteractListener pattern)
- Solo players without a clan: count threshold = 6
- Counts kills in both Survival **and** Amplified worlds
- All DB calls async — no `.join()` on main thread
- Register in `RPGCorePlugin.java` + `plugin.yml`

### Step 2 — Intro Cutscene `[S]`
`StoryIntroListener.java` already exists and sets `FLAG_INTRO_SEEN`.
- Write Denizen script (or title/subtitle/sound sequence) triggered when `FLAG_INTRO_SEEN` is not set on first Survival join
- Content: El Diablo's arrival lore + The Lord's call (see `Docs/PHASE1-ROADMAP.md` Gate 0 → Intro Cutscene)
- Trigger: hook into `StoryIntroListener` — find it, check existing hook point, add sequence call

### Step 3 — Amos Compass Dialogue `[S]`
Citizens NPC dialogue — can be drafted now, placed at coords later.
- Simple Citizens dialogue: on interact → give Lodestone Compass pointed at Thornwell
- Draft the Citizens `npc.yml` / conversation YAML now
- Mark the coords field as `TODO: fill after seed confirmed`

---

## Steps
- [x] Read `ClanRepository.java` — understand existing schema before adding wither kill counter
- [x] Read `PortalInteractListener.java` — understand gate check pattern for Devoid unlock
- [x] Read `StoryIntroListener.java` — find existing hook point for cutscene trigger
- [x] Implement `WitherKillListener.java`
- [x] Register in `RPGCorePlugin.java` + `plugin.yml`
- [x] Compile: `cd PluginV2 && ./gradlew.bat compileJava -x test`
- [x] Write intro cutscene sequence (titles/subtitles/sound or Denizen script)
- [x] Draft Amos compass Citizens dialogue YAML
- [x] Build + deploy: `PluginV2/build-and-copy-to-server.bat`
- [x] Update `CHANGELOG.md`
- [x] Update `Docs/PHASE1-ROADMAP.md` — tick off completed items

---

## Verification
- WitherKillListener: spawn a Wither in creative, kill it, check DB `wither_kills` increments for the clan
- Intro cutscene: join Survival on a fresh account (or clear flag in DB), confirm sequence plays
- Amos dialogue: place NPC manually at any temp location, confirm compass is given on interact

---

## Risks
- MythicMobs Devoider exclusion: verify MM entity check method via Context7 before implementing
- Wither kill DB schema: might need a migration — check if `clan_data` table can hold this or needs a new table
- StoryIntroListener hook: if it fires before world is loaded, cutscene timing may be wrong

---

## Review
All 3 Gate 0 code tasks completed — 2026-04-01.
- WitherKillListener implemented (clan threshold 36, solo threshold 6, async DB)
- Intro cutscene hooked into StoryIntroListener
- Amos compass Citizens dialogue YAML drafted (coords TBD after seed confirmed)
