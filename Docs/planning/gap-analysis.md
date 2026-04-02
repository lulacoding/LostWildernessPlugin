---
title: Gap Analysis
description: V1 to V2 feature gap analysis and parity tracking.
tags:
  - planning
  - roadmap
status: reference
phase: ongoing
owner: admin
action: none
---
# Gap analysis

Merged V1/V2 gap documents.


## Executive Summary

PluginV2 has successfully ported **most core gameplay systems** (calendar, events, portals, clans, bosses, progression, personality, zodiac, classes, **lobby verification**), but **2 critical gaps** and **9+ envisioned features** remain unimplemented.

### Architecture Update: Lobby vs Survival

**NEW DESIGN:**
- **Lobby** = Protected hub with class hall, calendar square, NPCs, onboarding
- **Survival** = True wilderness, no spawn protection, scattered temples/Brotherhood outposts
- **No spawn village at (0,0) in Survival** - that concept moved to Lobby

This is a cleaner separation:
- Safe onboarding/services â†’ Lobby
- Dangerous exploration/discovery â†’ Survival

### Critical Gaps (V1 â†’ V2)

| # | Feature | V1 Status | V2 Status | Priority |
|---|---------|-----------|-----------|----------|
| 1 | **Party System** | âœ… Implemented | âŒ Docs only | ðŸŸ  HIGH |
| 2 | **Admin Commands** | âœ… Implemented | âŒ Missing | ðŸŸ¡ MEDIUM |
| ~~3~~ | ~~Economy/Wallet~~ | ~~âœ… Implemented~~ | ~~âŒ Docs only~~ | ðŸ”µ DEFERRED |
| ~~4~~ | ~~Calendar Item~~ | ~~âœ… Implemented~~ | ~~âš ï¸ Partial~~ | ðŸ”µ DEFERRED |
| ~~5~~ | ~~Lobby Verification~~ | ~~âœ… Implemented~~ | âœ… **COMPLETE** | âœ… COMPLETE |
| ~~6~~ | ~~Spawn Village (0,0)~~ | ~~ðŸ“‹ Designed~~ | âœ… **MOVED TO LOBBY** | âœ… ARCHITECTURE CHANGE |

**Note:** Economy/Wallet and Calendar Item placement are on the back burner per user request.

### Envisioned Features (Docs â†’ V2)

| # | Feature | Docs Status | V2 Status | Priority | Location |
|---|---------|-------------|-----------|----------|----------|
| 3 | **Lobby Hub Build** | ðŸ“‹ Designed | âš ï¸ Partial | ðŸŸ  HIGH | Lobby world |
| 4 | **NPC/Quest Stack** | ðŸ“‹ Designed | âŒ Not integrated | ðŸŸ  HIGH | Survival + Lobby |
| 5 | **7-Chapter Storyline** | ðŸ“‹ Designed | âš ï¸ Framework only | ðŸŸ  HIGH | Survival world |
| 6 | **Brotherhood Outposts** | ðŸ“‹ Designed | âŒ Not built | ðŸŸ¡ MEDIUM | Survival (scattered) |
| 7 | **Elemental Temples** | ðŸ“‹ Designed | âš ï¸ Tracking only | ðŸŸ¡ MEDIUM | Survival (scattered) |
| 8 | **Galactic Journey** | ðŸ“‹ Designed | âŒ Not implemented | ðŸŸ¢ LOW | Post-game dimensions |
| 9 | **War Enhancements** | ðŸ“‹ Designed | âš ï¸ Basic only | ðŸŸ¢ LOW | Survival |
| 10 | **Music Triggers** | ðŸ“‹ Designed | âŒ Not implemented | ðŸŸ¢ LOW | All servers |
| 11 | **Updatable Maps** | ðŸ“‹ Designed | âŒ Not implemented | ðŸŸ¢ LOW | Cross-server |
| 12 | **Creative Server** | ðŸ“‹ Designed | âŒ Not implemented | ðŸŸ¢ LOW | Infrastructure |


### 1. Party System ðŸŸ  HIGH

**V1 Implementation:**
- **Files:**
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/party/PartyService.java`
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/party/Party.java`
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/commands/party/PartyCommand.java`
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/party/PartyListener.java`
- **Features:**
  - In-memory party management (leader + members)
  - Commands: `/party create`, `/party invite <player>`, `/party accept <leader>`, `/party leave`, `/party list`
  - Max party size configurable (default: 8)
  - Invite system with pending invites
  - Party broadcast messaging
  - Leader-based permissions
- **Database:** None (in-memory only in V1)

**V2 Status:**
- **Docs:** `../03-modules/plugin-party.md` (design spec only)
- **Code:** âŒ No implementation
- **Parity checklist:** Marked as âŒ in `v2-feature-parity.md`

**Impact:**
- No group play mechanics for dungeon/temple exploration
- Boss fights/events can't check party composition
- Quest objectives can't be party-shared
- Players can't coordinate for Brotherhood/Elemental Temple challenges

**Port Requirements:**
1. Create `PartyModule` implementing `RpgModule`
2. Port `PartyService` â†’ V2 with ModuleContext dependency injection
3. Port `Party` model â†’ V2
4. Port `PartyCommand` â†’ V2 command system
5. Port `PartyListener` â†’ V2 event listeners
6. Register in `ModuleManager.MODULES`
7. Add `party.yml` config

**No schema** (in-memory only, unless cross-server party is added later)

**Estimated effort:** ~1 week (straightforward port, no DB changes)


## Part 2: Deferred Features (On Back Burner)

These features exist in V1 but are **deferred** per user request.


### Calendar Item Placement ðŸ”µ DEFERRED

**V1 Implementation:**
- **Files:** `CalendarItemListener.java`
- **Features:** Right-click item opens book GUI with date/season
- **Design:** Placeable 1Ã—1 block (needs texture + datapack)

**V2 Status:**
- **Code:** âŒ Not ported
- **Status:** ðŸ”µ DEFERRED (on back burner)


## Part 4: Envisioned Features (Docs â†’ V2)

These features are **documented in design docs** but **not implemented in V1 or V2**.


### 4. NPC/Quest Stack Integration ðŸŸ  HIGH

**V1 Docs:**
- **File:** `Docs/design/03-plugins/npc-quest-and-rpg-stack.md` (580 lines)
- **Vision:**
  - **Citizens** for NPCs (named villagers, patrols, world characters)
  - **Denizen** for dialogue, cutscenes, scripted interactions, shops
  - **MythicMobs** for custom mobs, bosses, encounter scripting
  - **AuraSkills** for skill progression (already integrated in V2)
  - Random villages/outposts with NPCs (Brotherhood, trade posts, hunter camps)
  - Milestone-aware dialogue (NPCs react to chapter progress)

**V2 Status:**
- **AuraSkills:** âœ… Integrated (`SkillsModule`, `AuraSkillsBridge`)
- **Citizens/Denizen/MythicMobs:** âŒ Not installed or integrated
- **Village/outpost system:** âŒ No world generation or NPC spawning logic

**Impact:**
- Server feels like vanilla Minecraft, not an MMO-style survival world
- No in-world storytelling or quest delivery
- No dynamic content or lore discovery
- Brotherhood/temples have no NPCs or scripted encounters

**Implementation Requirements:**
1. Install external plugins: Citizens, Denizen, MythicMobs
2. Create V2 bridge layer:
   - `NpcService` (register/track NPCs by location)
   - `QuestBridge` (hooks to Denizen with milestone checks)
   - `MobBridge` (MythicMobs integration for temple guardians)
3. Build outpost templates (Brotherhood shrines, hunter camps, ruins)
4. Create NPC roster per location type
5. Script dialogue trees (Denizen) with milestone checks
6. Create custom mobs/bosses (MythicMobs) for temples/encounters
7. Integrate with V2 progression system (milestone writes from scripted quests)

**Estimated effort:** 2-3 weeks (plugin installation + bridge layer) + ongoing content creation


### 6. Brotherhood Outposts (Scattered) ðŸŸ¡ MEDIUM

**V1 Docs:**
- **Files:**
  - `Docs/design/01-overview/storyline-chapters.md` (mentions Brotherhood)
  - `Docs/design/03-plugins/npc-quest-and-rpg-stack.md` (Brotherhood outposts)
- **Vision:**
  - **Main temple:** Desert/mesa biome in Amplified (large structure)
  - **Outposts:** Scattered shrines across Survival/Amplified biomes
  - **Epochians:** Day 0 players framed as early keepers
  - **Scripture:** "Sam verses" as riddles, keys, warnings (explicit biblical tone)
  - **Lore delivery:** Bookshelves, signs, NPC dialogue, hidden clues

**V2 Status:**
- **Progression tracking:** âœ… `player_profile.join_day` (tracks Day 0 players)
- **Lore system:** âŒ No scripture, no bookshelves, no Brotherhood NPCs
- **Temples/outposts:** âŒ Not built

**New Architecture (Scattered Discovery):**
- **Main temple:** Major landmark in Amplified desert (Chapter 3 destination)
- **Small shrines:** Hidden in Survival biomes (discovered via exploration)
- **No spawn hub:** Brotherhood is not at (0,0), it's a journey

**Impact:**
- Biblical theme is invisible in-game
- No lore discovery mechanics
- Day 0 prestige is not recognized in-world
- Chapter 3 has no physical location

**Implementation Requirements:**
1. Build main Brotherhood temple (Amplified desert) - large structure
2. Build 5-10 small shrines (scattered across Survival biomes)
3. Create scripture system (books with "Sam verses")
4. Add NPC dialogue referencing Day 0 players (Epochians)
5. Create hidden clues/riddles for chapter progression
6. Integrate with progression system (scripture unlocks milestones)
7. Add compass/map hints pointing to temple (optional)

**Estimated effort:** 2-3 weeks (main temple) + 1 week (small shrines) + 1 week (scripture/NPCs)


### 8. Galactic Journey (Post-game) ðŸŸ¢ LOW

**V1 Docs:**
- **File:** `Docs/design/01-overview/storyline-chapters.md` (Post-Game section)
- **Vision:**
  - **Unlock:** Angelhood (all 5 elements complete)
  - **Structure:** Multiverse "planets" with lore, boss, custom items, Planet Enchants
  - **Order:** Venus â†’ Mercury â†’ Moon â†’ Mars â†’ Jupiter â†’ Saturn â†’ Uranus â†’ Neptune â†’ Pluto
  - **Each planet:** Boss fight, unique items, lore progression

**V2 Status:**
- **Tracking:** âŒ No planet progression tracking
- **Worlds:** âŒ Not built
- **Bosses:** âŒ Not implemented

**New Architecture:**
- Separate dimensions (like Nether/End) accessed via special portals
- Or: scattered "starport" locations in Survival (less immersive)

**Impact:**
- Post-game content is nonexistent
- Angel status feels like an endpoint, not a gateway

**Implementation Requirements:**
1. Build 9 planet dimensions (or schematic-based areas)
2. Create planet bosses (MythicMobs)
3. Add Planet Enchants (custom PDC-based enchants)
4. Create planet-specific loot
5. Add progression tracking (`player_planet_completions` table)
6. Script starport/teleporter system (unlock after Angel)

**Estimated effort:** 2-3 months (9 planets is a lot of content)


### 10. Music Triggers (Lobby/Warzone/Eclipse) ðŸŸ¢ LOW

**V1 Docs:**
- **Files:**
  - `Docs/design/07-audio-visual/lobby-and-warzone-music.md`
  - `Docs/design/07-audio-visual/eclipse-theme.md`
- **Vision:**
  - **Lobby music:** Ambient theme on join
  - **Warzone music:** Combat theme during war
  - **Eclipse music:** 5-act theme (foreboding â†’ climax â†’ aftermath)
  - **Trigger system:** NoteBlock API or resource pack with sound events

**V2 Status:**
- **Music system:** âŒ No in-game music triggers
- **Resource packs:** âœ… Eclipse pack exists (for visuals)
- **Audio assets:** âŒ No custom music files

**Impact:**
- Server feels quiet (no atmospheric audio)
- Events lack immersion (eclipse has visuals but no audio)

**Implementation Requirements:**
1. Compose/acquire music tracks (lobby, warzone, eclipse)
2. Add to resource pack (or use external music plugin)
3. Create trigger system:
   - Lobby: play on join (LobbyJoinListener)
   - Warzone: play when entering war zone (boundary detection)
   - Eclipse: play during event (EclipseEvent.onStart())
4. Add config options (enable/disable music, volume)

**Estimated effort:** 1-2 weeks (depends on music acquisition)


### 12. Creative Server ðŸŸ¢ LOW

**V1 Docs:**
- **File:** `Docs/design/05-worlds-gameplay/lobby-and-creative.md`
- **Vision:**
  - Separate Creative server for builders
  - Plot system (WorldEdit/FastAsyncWorldEdit/PlotSquared)
  - Superflat world
  - Linked to proxy (accessible from lobby)

**V2 Status:**
- **Creative server:** âŒ Not configured
- **V2 support:** âš ï¸ V2 plugin could run on Creative (config-driven)

**Impact:**
- No creative space for builders
- Can't prototype builds before placing in Survival/Amplified

**Implementation Requirements:**
1. Set up Creative backend server (Paper)
2. Install plot plugin (PlotSquared or similar)
3. Add to proxy config (e.g., `creative-1`)
4. Add V2 plugin with minimal modules (`player`, `calendar` as follower)
5. Add teleporter from lobby to Creative (optional)

**Estimated effort:** 1-2 days (infrastructure setup)


## Part 6: Lobby vs Survival Architecture

**Design Principle:** Clean separation between **safe hub** (Lobby) and **dangerous wilderness** (Survival).

### Lobby (Safe Hub)

| Feature | V2 Status | Purpose |
|---------|-----------|---------|
| Personality quiz | âœ… Implemented | Assign trait + element on first join |
| Verification system | âœ… Implemented | Discord link requirement |
| Class hall | âŒ Needs build + NPCs | 5 trainer NPCs for class selection |
| Calendar square | âŒ Needs build | Decorative displays, lore boards |
| Lore library | âŒ Needs build | Scripture hints, server rules, chapter teasers |
| Teleporter to Survival | âœ… Implemented | Portal entry with verification + class check |
| Spawn protection | âœ… Implemented | Movement lock, damage immunity |
| Greeter intro | âœ… Implemented | Camera focus, BetonQuest event trigger |

**What Lobby Needs:**
- World build (class hall, calendar square, lore library)
- Citizens/Denizen integration (5 class trainer NPCs)
- Decorative elements (banners, item frames, custom textures)

### Survival (Wilderness)

| Feature | V2 Status | Purpose |
|---------|-----------|---------|
| Spawn at (0,0) | âœ… Default | No special protection, just wilderness |
| New Year fireworks | âœ… Implemented | (0,0) celebration display |
| Brotherhood outposts | âŒ Needs build | Scattered discovery (main temple + shrines) |
| Elemental temples | âŒ Needs build | Scattered discovery (5 temples) |
| Random villages/NPCs | âŒ Needs NPC stack | Hunter camps, trade posts, ruins |
| Boss arenas | âœ… Implemented | Devoiders, El Diablo, Aether gate |

**What Survival Needs:**
- Brotherhood main temple (Amplified desert)
- 5 elemental temples (scattered biomes)
- Random NPC outposts (discovered via exploration)
- No centralized spawn hub (true wilderness feel)


## Conclusion (FINAL)

**V2 is production-ready for core gameplay** (calendar, events, portals, clans, bosses, progression, personality, zodiac, classes, **lobby verification with personality quiz**) but **missing party system and admin commands** and **all narrative content** (lobby hub build, NPC/quest stack, 7-chapter storyline, Brotherhood outposts, elemental temples).

**Architecture is now cleaner:**
- **Lobby** = Safe hub with onboarding, class selection, lore library
- **Survival** = True wilderness with scattered discoveries (no spawn hub)

**Recommended next steps:**
1. Port **Party System** (1 week) - needed for group temple exploration
2. Port **Admin Commands** (2-3 days) - QoL for devs/admins
3. Build **Lobby Hub** (2-3 weeks) - class hall, calendar square, lore library
4. Install **NPC/Quest stack** (2-3 weeks) - Citizens, Denizen, MythicMobs
5. Build **Brotherhood Outposts** (4-5 weeks) - main temple + shrines
6. Script **7-Chapter Storyline** (3-6 months) - quest chains, boss gates
7. Build **Elemental Temples** (5-6 weeks) - 5 scattered temples + guardians

**Total effort estimate:**
- **Critical gaps (1-2):** 1-2 weeks
- **Foundation (3-4):** 4-6 weeks
- **Content layer (5-7):** 4-8 months
- **Post-game/polish (8-12):** 3-6 months

**Final note:** V2 is architecturally superior to V1 (modular, async-first, single-jar) and has successfully ported the lobby verification system with personality quiz. The main gaps are party system (for group play), lobby hub build (for onboarding), and the entire NPC/quest/storyline content layer. The new architecture (Lobby as hub, Survival as wilderness) is cleaner and more immersive than a centralized spawn village.



## 1. Calendar / special-day behaviour

| Feature | Old plugin | PluginV2 | Notes |
|--------|------------|---------|--------|
| Day-change title | "March 1, 1MC" | âœ… Same | CalendarDayChangeTitleListener |
| Day-change subtitle (season) | Season message or "Day X" | âœ… Same | buildSubtitle() |
| **Easter week subtitle** | Palm Sunday â†’ Easter Sunday titles (e.g. "Â§dðŸŒ¿ Palm Sundayâ€¦", "Â§aâ˜€ He is risen! Happy Easter Sunday! â˜€") | âŒ Missing | EasterWeekEvent.getTitleForDay(day); used in DayChangeListener before season subtitle |
| New Year fireworks | At (0,0), scale by decade/century/millennium | âœ… Same | NewYearFireworkHandler |
| Placeable calendar item (book UI) | Right-click with item opens date/season book | âŒ Missing | CalendarItemListener (optional) |

**Easter in old plugin:** Not a â€œworld eventâ€ that starts/ends. It only provides **subtitle text** for the 8 days (Palm Sunday through Easter Sunday) based on MC calendar year and a Gregorian Easter calculation. So in V2 you only need to add Easter week **subtitle** logic to the day-change title listener (and optionally a small Easter-date helper).


## 3. Cascades (old plugin)

| Cascade | Old plugin | PluginV2 |
|---------|------------|----------|
| Eclipse â†’ Thunderstorm (triggerEclipseStorm) | âœ… | âŒ No ThunderEvent yet |
| Eclipse â†’ Paranoia | âœ… | âŒ No ParanoiaEvent yet |

EventCascadeRegistry exists in V2; once Thunder and Paranoia exist, register them in EventsModule.


## 5. Summary: status

**Calendar / titles**

- **Easter week subtitles** âœ… (EasterWeekHelper + CalendarDayChangeTitleListener).

**World events (all ported)**

- Thunder âœ… (base chance + Eclipse â†’ Thunderstorm cascade).
- Blizzard âœ… (BlockRestoreManager).
- Frost âœ… (BlockRestoreManager, winter-only, heat-source check).
- Fog âœ….
- Summer heatwave âœ… (summer-only).
- Spring bloom âœ… (March 1), Autumn leaf fall âœ… (autumn), Seasonal storm âœ… (autumn/winter months), Jungle monsoon âœ… (summer).
- Paranoia âœ… (cascade from Eclipse, horror sounds).

**Supporting**

- BlockRestoreManager âœ… (in-memory restore for Blizzard/Frost).
- Optional (not yet): crash-safe persist/restore on startup; mob buffs; biome-aware horde + jockey; player event preferences + /event toggle, /event togglebar.

**Optional calendar**

- Placeable calendar item (book UI) â€” not implemented.
