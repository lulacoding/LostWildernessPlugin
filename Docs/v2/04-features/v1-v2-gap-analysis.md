# V1 to V2 Gap Analysis – Complete Feature Audit (FINAL)

**Purpose:** Comprehensive top-down analysis of what exists in V1 (implemented + envisioned in Docs) that is missing from V2.

**Date:** 2026-03-18
**Revision:** Final - corrected lobby verification status + updated architecture (lobby as hub, survival as wilderness)
**Scope:** Plugin code, Docs design, and documented vision

---

## Executive Summary

PluginV2 has successfully ported **most core gameplay systems** (calendar, events, portals, clans, bosses, progression, personality, zodiac, classes, **lobby verification**), but **2 critical gaps** and **9+ envisioned features** remain unimplemented.

### Architecture Update: Lobby vs Survival

**NEW DESIGN:**
- **Lobby** = Protected hub with class hall, calendar square, NPCs, onboarding
- **Survival** = True wilderness, no spawn protection, scattered temples/Brotherhood outposts
- **No spawn village at (0,0) in Survival** - that concept moved to Lobby

This is a cleaner separation:
- Safe onboarding/services → Lobby
- Dangerous exploration/discovery → Survival

### Critical Gaps (V1 → V2)

| # | Feature | V1 Status | V2 Status | Priority |
|---|---------|-----------|-----------|----------|
| 1 | **Party System** | ✅ Implemented | ❌ Docs only | 🟠 HIGH |
| 2 | **Admin Commands** | ✅ Implemented | ❌ Missing | 🟡 MEDIUM |
| ~~3~~ | ~~Economy/Wallet~~ | ~~✅ Implemented~~ | ~~❌ Docs only~~ | 🔵 DEFERRED |
| ~~4~~ | ~~Calendar Item~~ | ~~✅ Implemented~~ | ~~⚠️ Partial~~ | 🔵 DEFERRED |
| ~~5~~ | ~~Lobby Verification~~ | ~~✅ Implemented~~ | ✅ **COMPLETE** | ✅ COMPLETE |
| ~~6~~ | ~~Spawn Village (0,0)~~ | ~~📋 Designed~~ | ✅ **MOVED TO LOBBY** | ✅ ARCHITECTURE CHANGE |

**Note:** Economy/Wallet and Calendar Item placement are on the back burner per user request.

### Envisioned Features (Docs → V2)

| # | Feature | Docs Status | V2 Status | Priority | Location |
|---|---------|-------------|-----------|----------|----------|
| 3 | **Lobby Hub Build** | 📋 Designed | ⚠️ Partial | 🟠 HIGH | Lobby world |
| 4 | **NPC/Quest Stack** | 📋 Designed | ❌ Not integrated | 🟠 HIGH | Survival + Lobby |
| 5 | **7-Chapter Storyline** | 📋 Designed | ⚠️ Framework only | 🟠 HIGH | Survival world |
| 6 | **Brotherhood Outposts** | 📋 Designed | ❌ Not built | 🟡 MEDIUM | Survival (scattered) |
| 7 | **Elemental Temples** | 📋 Designed | ⚠️ Tracking only | 🟡 MEDIUM | Survival (scattered) |
| 8 | **Galactic Journey** | 📋 Designed | ❌ Not implemented | 🟢 LOW | Post-game dimensions |
| 9 | **War Enhancements** | 📋 Designed | ⚠️ Basic only | 🟢 LOW | Survival |
| 10 | **Music Triggers** | 📋 Designed | ❌ Not implemented | 🟢 LOW | All servers |
| 11 | **Updatable Maps** | 📋 Designed | ❌ Not implemented | 🟢 LOW | Cross-server |
| 12 | **Creative Server** | 📋 Designed | ❌ Not implemented | 🟢 LOW | Infrastructure |

---

## Part 1: Critical Gaps (V1 Implemented → V2 Missing)

These features **exist in working code** in V1 but are **missing or incomplete** in V2.

---

### 1. Party System 🟠 HIGH

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
- **Code:** ❌ No implementation
- **Parity checklist:** Marked as ❌ in `v2-feature-parity.md`

**Impact:**
- No group play mechanics for dungeon/temple exploration
- Boss fights/events can't check party composition
- Quest objectives can't be party-shared
- Players can't coordinate for Brotherhood/Elemental Temple challenges

**Port Requirements:**
1. Create `PartyModule` implementing `RpgModule`
2. Port `PartyService` → V2 with ModuleContext dependency injection
3. Port `Party` model → V2
4. Port `PartyCommand` → V2 command system
5. Port `PartyListener` → V2 event listeners
6. Register in `ModuleManager.MODULES`
7. Add `party.yml` config

**No schema** (in-memory only, unless cross-server party is added later)

**Estimated effort:** ~1 week (straightforward port, no DB changes)

---

### 2. Admin Commands 🟡 MEDIUM

**V1 Implementation:**
- **Files:**
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/commands/LWConfigCommand.java`
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/commands/LWHelpCommand.java`
  - `Plugin/common/src/main/java/com/lula0802/LWEvents/commands/LWPluginsCommand.java`
- **Features:**
  - `/lwconfig reload` – Reload Common plugin config (permission: `lw.admin.config`)
  - `/lwhelp` – Show all LW commands with descriptions
  - `/lwplugins` – Show loaded LW plugin versions and status

**V2 Status:**
- **Docs:** Mentioned in `v2-feature-parity.md` (Other section)
- **Code:** ❌ Not implemented
- **Parity checklist:** Marked as ❌

**Impact:**
- No centralized help system for players/admins
- No config reload without full restart (less dev-friendly)
- No module status overview command

**Port Requirements:**
1. Create `core` module admin commands:
   - `/lwconfig reload` → reload `core.yml` and module configs
   - `/lwhelp` → list all V2 commands by module
   - `/lwplugins` → list loaded V2 modules and versions
2. Add to `RPGCorePlugin` or `CoreModule`
3. Register in `plugin.yml` with permissions

**Estimated effort:** ~2-3 days (simple commands)

---

## Part 2: Deferred Features (On Back Burner)

These features exist in V1 but are **deferred** per user request.

---

### Economy/Wallet System 🔵 DEFERRED

**V1 Implementation:**
- **Files:** `WalletService.java`, `WalletRepository.java`, `WalletCacheListener.java`
- **Features:** Multi-currency, transactions, async persistence
- **Database:** `player_wallets`, `player_transactions`, `shop_items`

**V2 Status:**
- **Docs:** `../03-modules/plugin-economy.md` (design spec)
- **Code:** ❌ Not implemented
- **Status:** 🔵 DEFERRED (on back burner)

---

### Calendar Item Placement 🔵 DEFERRED

**V1 Implementation:**
- **Files:** `CalendarItemListener.java`
- **Features:** Right-click item opens book GUI with date/season
- **Design:** Placeable 1×1 block (needs texture + datapack)

**V2 Status:**
- **Code:** ❌ Not ported
- **Status:** 🔵 DEFERRED (on back burner)

---

## Part 3: Architecture Change - Lobby as Hub

**OLD DESIGN (Docs):**
- Spawn Village at (0,0) in Survival
- Protected hub with class hall, calendar square, marketplace
- Central NPCs (trainers, traders, scribes)

**NEW DESIGN (User Clarification):**
- **Lobby** = Hub with class hall, calendar square, NPCs, onboarding
- **Survival (0,0)** = No special protection, wilderness spawn
- **Temples/Brotherhood** = Scattered across Survival world (discovered naturally)

**Impact on V2:**
- Lobby needs more features (class hall, calendar displays, NPC roster)
- Survival is simpler (no spawn protection needed at 0,0)
- Content discovery is exploration-driven (not hub-radiating)

**What Lobby Needs:**

| Feature | V2 Status | Priority |
|---------|-----------|----------|
| Personality quiz system | ✅ Implemented | ✅ COMPLETE |
| Verification system | ✅ Implemented | ✅ COMPLETE |
| Class selection NPCs | ❌ Needs Citizens/Denizen | 🟠 HIGH |
| Calendar display (decorative) | ❌ Not built | 🟡 MEDIUM |
| Lore boards/books | ❌ Not built | 🟡 MEDIUM |
| Teleporter to Survival | ✅ Implemented | ✅ COMPLETE |
| Spawn protection | ✅ LobbyProtectionListener | ✅ COMPLETE |

---

## Part 4: Envisioned Features (Docs → V2)

These features are **documented in design docs** but **not implemented in V1 or V2**.

---

### 3. Lobby Hub Build 🟠 HIGH

**Vision:**
- **Class hall** with 5 trainer NPCs (Celestial Templar, Wildland Ranger, Redeemed Artificer, Corrupted Cultist, Destroyer Berserker)
- **Calendar square** with clock tower, decorative calendar displays
- **Lore library** with scripture hints, "Sam verses", Brotherhood teasers
- **Teleporter area** with portal to Survival (already implemented)
- **Safe zone** with spawn protection (already implemented)

**V2 Status:**
- **Base system:** ✅ Verification, quiz, teleporter, spawn protection
- **World build:** ❌ Lobby world needs physical structures
- **NPCs:** ❌ Needs Citizens/Denizen integration
- **Decorative elements:** ❌ Calendar displays, lore books, class banners

**Impact:**
- Lobby feels bare (just a teleporter + quiz system)
- No visual identity or world-building
- Class system feels abstract (no in-world representation)

**Implementation Requirements:**
1. Build lobby world structures:
   - Class hall with 5 training rooms/areas
   - Calendar square with decorative clock tower
   - Lore library with bookshelves
   - Teleporter plaza (already functional, just needs decoration)
2. Install Citizens + Denizen
3. Create 5 class trainer NPCs with dialogue
4. Script class selection flow (dialogue → database write)
5. Add lore books (scripture hints, server rules, chapter teasers)
6. Add decorative calendar displays (item frames, maps, custom textures)

**Estimated effort:** 1-2 weeks (world build) + 1 week (NPC scripting)

---

### 4. NPC/Quest Stack Integration 🟠 HIGH

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
- **AuraSkills:** ✅ Integrated (`SkillsModule`, `AuraSkillsBridge`)
- **Citizens/Denizen/MythicMobs:** ❌ Not installed or integrated
- **Village/outpost system:** ❌ No world generation or NPC spawning logic

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

---

### 5. 7-Chapter Storyline System 🟠 HIGH

**V1 Docs:**
- **File:** `Docs/design/01-overview/storyline-chapters.md` (105 lines)
- **Vision:**
  - **Chapter 1:** The Wilderness (spawn, epoch, first steps) - *scattered tutorial NPCs*
  - **Chapter 2:** Signs and Seasons (history, calendar events as omens)
  - **Chapter 3:** The Brotherhood (library, hidden temple, scripture) - *desert outpost*
  - **Chapter 4:** The Wither's Shadow (Devoiders, 6 withers each)
  - **Chapter 5:** El Diablo (36 withers → 3-phase boss fight)
  - **Chapter 6:** Leaving the Firmament (Aether portal at y602, Aquaria, Neptune, Heavenly Tower, Gabriel quiz, Rainbow Path, Lord's Plateau)
  - **Chapter 7:** The Covenant (Personality, Elements, Angelhood)
  - **Post-game:** Galactic Journey (9 planets with bosses/enchants)

**V2 Status:**
- **Progression framework:** ✅ (`ProgressionService`, `AchievementKey`, milestones)
- **Boss gates:** ⚠️ Devoiders/El Diablo exist, Aether gate exists
- **Chapter quests:** ❌ No quest definitions or scripted flow
- **NPC delivery:** ❌ No Brotherhood NPCs or scripture system
- **Aquaria/Heavenly Tower/Rainbow Path:** ❌ Not built

**New Architecture (Survival as Wilderness):**
- **Chapter 1-3:** Tutorial NPCs scattered in Survival (not centralized)
- **Brotherhood:** Hidden desert outpost (discovered via exploration)
- **Chapter 4-5:** Boss arenas (already exist in V2)
- **Chapter 6:** Vertical progression (Aether → Aquaria → Tower)
- **Chapter 7:** Element reveal + temples (scattered across world)

**Impact:**
- Players have bosses but no narrative context
- No guided progression or story beats
- Server feels like a tech demo, not a living world
- Brotherhood is invisible (no in-world presence)

**Implementation Requirements:**
1. Define quest objectives per chapter (Denizen or BetonQuest)
2. Build Brotherhood temple/library (Amplified desert outpost)
3. Create scripture/clues system ("Sam verses" in books)
4. Build Aquaria (underwater glass cathedral)
5. Build Heavenly Tower (3000 blocks: parkour/guards/spiral)
6. Create Gabriel quiz (quiz system or Denizen script)
7. Build Rainbow Path and Lord's Plateau (world build)
8. Script chapter gates (milestone checks before progression)
9. Scatter tutorial NPCs in Survival (no central hub)

**Estimated effort:** 3-6 months (quest scripting + world builds + boss tuning)

---

### 6. Brotherhood Outposts (Scattered) 🟡 MEDIUM

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
- **Progression tracking:** ✅ `player_profile.join_day` (tracks Day 0 players)
- **Lore system:** ❌ No scripture, no bookshelves, no Brotherhood NPCs
- **Temples/outposts:** ❌ Not built

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

---

### 7. Elemental Temples (Scattered) 🟡 MEDIUM

**V1 Docs:**
- **File:** `Docs/design/01-overview/storyline-chapters.md` (Chapter 7, Elemental Temples)
- **Vision:**
  - **5 temples:** FIRE, EARTH, WIND, WATER, AETHER
  - **Each temple:** quests, quizzes, minibosses, loot, enchants, advancements
  - **Guardian battle:** Final boss per element
  - **Completion:** All 5 temples → **Angel** status
  - **Post-game:** 200-300% completion via temple achievements

**V2 Status:**
- **Tracking:** ✅ `TraitService.completeTemple()`, `player_temples` table
- **Element system:** ✅ Elements assigned in quiz, revealed by Lord command
- **Temples:** ❌ Not built (no worlds, no quests, no bosses)
- **Guardians:** ❌ Not implemented (would use MythicMobs)

**New Architecture (Scattered Discovery):**
- **Temples scattered across Survival world** (not separate dimensions)
- **Biome-appropriate locations:**
  - FIRE: Nether portal area or volcanic biome
  - EARTH: Deep underground cave system
  - WIND: Mountain peak or sky island
  - WATER: Ocean monument or underwater trench
  - AETHER: Extreme heights (y > 200) or special structure
- **Discovered via exploration** (no quest markers, just world clues)

**Impact:**
- 200-300% completion is invisible (no content to complete)
- Post-game feels empty (no elemental challenges)
- Angel status has no gameplay manifestation
- Element reveal feels anticlimactic (no temples to unlock)

**Implementation Requirements:**
1. Build 5 temples in Survival world:
   - FIRE: Nether portal area (volcanic theme)
   - EARTH: Deep cave system (crystal/ore theme)
   - WIND: Mountain peak (sky island theme)
   - WATER: Ocean depth (underwater ruins theme)
   - AETHER: High altitude (heavenly theme)
2. Create quest chains per temple (Denizen or BetonQuest)
3. Create guardian bosses (MythicMobs): Fire Titan, Earth Golem, Wind Djinn, Water Leviathan, Aether Seraph
4. Add temple-specific loot/enchants
5. Create advancements for temple completion
6. Script Angel ascension event (when all 5 complete)

**Estimated effort:** 3-4 weeks (temple builds) + 2 weeks (quest/boss scripting)

---

### 8. Galactic Journey (Post-game) 🟢 LOW

**V1 Docs:**
- **File:** `Docs/design/01-overview/storyline-chapters.md` (Post-Game section)
- **Vision:**
  - **Unlock:** Angelhood (all 5 elements complete)
  - **Structure:** Multiverse "planets" with lore, boss, custom items, Planet Enchants
  - **Order:** Venus → Mercury → Moon → Mars → Jupiter → Saturn → Uranus → Neptune → Pluto
  - **Each planet:** Boss fight, unique items, lore progression

**V2 Status:**
- **Tracking:** ❌ No planet progression tracking
- **Worlds:** ❌ Not built
- **Bosses:** ❌ Not implemented

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

---

### 9. War System Enhancements 🟢 LOW

**V1 Docs:**
- **File:** `Docs/implementation-status.md` (Section 5, War)
- **Vision:**
  - `/clan reside` – Set clan home (semantics TBD)
  - **War day subtitle** – Display "War declared!" when war starts
  - **Warzone radius** – PvP rules near war zones
  - **War music** – Music triggers during combat (lobby/warzone themes)

**V2 Status:**
- **Basic war:** ✅ `/war declare`, `/war ceasefire`, `/war end`, `/war status`
- **War persistence:** ✅ `clan_wars` table, WarRepository
- **Head drops:** ✅ 1/1000 in war, 1/10000 otherwise
- **Discord webhook:** ⚠️ In V1, needs port to V2
- **Enhancements:** ❌ Not implemented

**Impact:**
- War feels basic (just a status flag)
- No visual/audio feedback for war events
- No territory mechanics

**Implementation Requirements:**
1. Add `/clan reside` command (define semantics: set home, claim territory, etc.)
2. Add war day subtitle (title/subtitle when war is declared)
3. Add warzone radius system (WorldGuard regions or custom boundary logic)
4. Add music triggers (resource pack + NoteBlock API or external plugin)
5. Port Discord webhook to V2 (already in V1, needs migration)

**Estimated effort:** 1-2 weeks (enhancements only)

---

### 10. Music Triggers (Lobby/Warzone/Eclipse) 🟢 LOW

**V1 Docs:**
- **Files:**
  - `Docs/design/07-audio-visual/lobby-and-warzone-music.md`
  - `Docs/design/07-audio-visual/eclipse-theme.md`
- **Vision:**
  - **Lobby music:** Ambient theme on join
  - **Warzone music:** Combat theme during war
  - **Eclipse music:** 5-act theme (foreboding → climax → aftermath)
  - **Trigger system:** NoteBlock API or resource pack with sound events

**V2 Status:**
- **Music system:** ❌ No in-game music triggers
- **Resource packs:** ✅ Eclipse pack exists (for visuals)
- **Audio assets:** ❌ No custom music files

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

---

### 11. Updatable Maps 🟢 LOW

**V1 Docs:**
- **File:** `Docs/implementation-status.md` (Section 5, Maps)
- **Vision:**
  - Updatable maps that sync across servers (Survival/Amplified)
  - Cross-server map transfer (like entity transfer for portals)

**V2 Status:**
- **Maps:** ❌ No map system

**Impact:**
- Players can't share maps between Survival/Amplified
- Cartography is limited to single-server

**Implementation Requirements:**
1. Create map sync system (similar to portal entity transfer)
2. Serialize map data to DB (`portal_transfer_maps` table)
3. Add listener for map creation/update
4. Deserialize and restore map on destination server
5. Add config option (enable/disable map sync)

**Estimated effort:** 1 week (small feature)

---

### 12. Creative Server 🟢 LOW

**V1 Docs:**
- **File:** `Docs/design/05-worlds-gameplay/lobby-and-creative.md`
- **Vision:**
  - Separate Creative server for builders
  - Plot system (WorldEdit/FastAsyncWorldEdit/PlotSquared)
  - Superflat world
  - Linked to proxy (accessible from lobby)

**V2 Status:**
- **Creative server:** ❌ Not configured
- **V2 support:** ⚠️ V2 plugin could run on Creative (config-driven)

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

---

## Part 5: Priority Roadmap (FINAL)

Recommended implementation order based on impact and dependencies.

### Phase 1: Critical Gaps (1-2 weeks)

1. **Party System** 🟠
   - **Why first:** Needed for group content (temple exploration, boss fights)
   - **Effort:** ~1 week (port existing code)
   - **Files to port:** PartyService, Party, PartyCommand, PartyListener
   - **Dependencies:** None

2. **Admin Commands** 🟡
   - **Why second:** QoL for admins/devs (config reload, help system)
   - **Effort:** ~2-3 days (create new commands)
   - **Files to create:** LWConfigCommand, LWHelpCommand, LWPluginsCommand
   - **Dependencies:** None

### Phase 2: Foundation (2-3 weeks)

3. **Lobby Hub Build** 🟠
   - **Why third:** Onboarding experience, visual identity
   - **Effort:** ~2-3 weeks (world build + NPC integration)
   - **What to build:** Class hall, calendar square, lore library, decorative elements
   - **Dependencies:** Citizens/Denizen installation

4. **NPC/Quest Stack Integration** 🟠
   - **Why fourth:** Foundation for all story content
   - **Effort:** ~2-3 weeks (plugin installation + bridge layer)
   - **What to install:** Citizens, Denizen, MythicMobs
   - **Dependencies:** None (but required for #3, #5, #6)

### Phase 3: Content Layer (3-6 months)

5. **Brotherhood Outposts** 🟡
   - **Why fifth:** Chapter 3 storyline destination
   - **Effort:** ~4-5 weeks (main temple + shrines + scripture)
   - **What to build:** Main temple (Amplified desert), 5-10 small shrines (scattered)
   - **Dependencies:** NPC/Quest stack

6. **7-Chapter Storyline** 🟠
   - **Why sixth:** Main narrative progression
   - **Effort:** ~3-6 months (quest scripting, world builds, boss tuning)
   - **What to script:** Chapter quests, milestone gates, NPC dialogue
   - **Dependencies:** NPC/Quest stack, Brotherhood outposts

7. **Elemental Temples** 🟡
   - **Why seventh:** Post-game progression (200-300%)
   - **Effort:** ~5-6 weeks (5 temples + guardians + quests)
   - **What to build:** 5 temples scattered in Survival, guardian bosses
   - **Dependencies:** NPC/Quest stack, MythicMobs

### Phase 4: Post-game/Polish (3-6 months)

8. **Galactic Journey** 🟢
9. **War Enhancements** 🟢
10. **Music Triggers** 🟢
11. **Updatable Maps** 🟢
12. **Creative Server** 🟢

### Deferred (Back Burner)

- **Economy/Wallet System** 🔵
- **Calendar Item Placement** 🔵

---

## Part 6: Lobby vs Survival Architecture

**Design Principle:** Clean separation between **safe hub** (Lobby) and **dangerous wilderness** (Survival).

### Lobby (Safe Hub)

| Feature | V2 Status | Purpose |
|---------|-----------|---------|
| Personality quiz | ✅ Implemented | Assign trait + element on first join |
| Verification system | ✅ Implemented | Discord link requirement |
| Class hall | ❌ Needs build + NPCs | 5 trainer NPCs for class selection |
| Calendar square | ❌ Needs build | Decorative displays, lore boards |
| Lore library | ❌ Needs build | Scripture hints, server rules, chapter teasers |
| Teleporter to Survival | ✅ Implemented | Portal entry with verification + class check |
| Spawn protection | ✅ Implemented | Movement lock, damage immunity |
| Greeter intro | ✅ Implemented | Camera focus, BetonQuest event trigger |

**What Lobby Needs:**
- World build (class hall, calendar square, lore library)
- Citizens/Denizen integration (5 class trainer NPCs)
- Decorative elements (banners, item frames, custom textures)

### Survival (Wilderness)

| Feature | V2 Status | Purpose |
|---------|-----------|---------|
| Spawn at (0,0) | ✅ Default | No special protection, just wilderness |
| New Year fireworks | ✅ Implemented | (0,0) celebration display |
| Brotherhood outposts | ❌ Needs build | Scattered discovery (main temple + shrines) |
| Elemental temples | ❌ Needs build | Scattered discovery (5 temples) |
| Random villages/NPCs | ❌ Needs NPC stack | Hunter camps, trade posts, ruins |
| Boss arenas | ✅ Implemented | Devoiders, El Diablo, Aether gate |

**What Survival Needs:**
- Brotherhood main temple (Amplified desert)
- 5 elemental temples (scattered biomes)
- Random NPC outposts (discovered via exploration)
- No centralized spawn hub (true wilderness feel)

---

## Part 7: Summary Table (FINAL)

| Feature | V1 Status | V2 Status | Effort | Priority | Location |
|---------|-----------|-----------|--------|----------|----------|
| ~~Lobby Verification~~ | ~~✅~~ | ✅ **COMPLETE** | ✅ | ✅ | Lobby |
| Party System | ✅ Implemented | ❌ Missing | 1 week | 🟠 HIGH | All servers |
| Admin Commands | ✅ Implemented | ❌ Missing | 2-3 days | 🟡 MEDIUM | All servers |
| Lobby Hub Build | 📋 Designed | ⚠️ Partial | 2-3 weeks | 🟠 HIGH | Lobby |
| NPC/Quest Stack | 📋 Designed | ❌ Missing | 2-3 weeks | 🟠 HIGH | All servers |
| 7-Chapter Storyline | 📋 Designed | ⚠️ Framework | 3-6 months | 🟠 HIGH | Survival |
| Brotherhood Outposts | 📋 Designed | ❌ Missing | 4-5 weeks | 🟡 MEDIUM | Survival (scattered) |
| Elemental Temples | 📋 Designed | ⚠️ Tracking | 5-6 weeks | 🟡 MEDIUM | Survival (scattered) |
| Galactic Journey | 📋 Designed | ❌ Missing | 2-3 months | 🟢 LOW | Post-game dimensions |
| War Enhancements | 📋 Designed | ⚠️ Basic | 1-2 weeks | 🟢 LOW | Survival |
| Music Triggers | 📋 Designed | ❌ Missing | 1-2 weeks | 🟢 LOW | All servers |
| Updatable Maps | 📋 Designed | ❌ Missing | 1 week | 🟢 LOW | Cross-server |
| Creative Server | 📋 Designed | ❌ Missing | 1-2 days | 🟢 LOW | Infrastructure |
| Economy/Wallet | ✅ Implemented | ❌ Missing | - | 🔵 DEFERRED | - |
| Calendar Item | ✅ Implemented | ⚠️ Partial | - | 🔵 DEFERRED | - |

---

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
