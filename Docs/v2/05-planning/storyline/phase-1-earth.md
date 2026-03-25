# Phase 1 — Earth (The Survival World)
**Story Arc:** The First Steps
**Server:** Survival 1 (Volcano Biomes / Anarchy SMP)
**Completion Range:** 0% → ~25%

---

## 🌍 World Overview

Players spawn at **0,0** in a purist anarchy-style SMP built in volcano biomes. There is no hand-holding — the world is raw, dangerous, and open. However, woven into the landscape are NPCs, quest hubs, and a living lore system that rewards exploration and engagement.

Before the player even spawns for the first time, two things happen:
1. A **personality quiz** assigns one of 13 traits (Warrior, Mage, Ranger, Healer, Alchemist, etc.)
2. A **cutscene plays** explaining the lore — El Diablo's arrival, the splitting of the dragons, and the Lord's call for help from the real world

---

## 🏠 The Spawn Village (0,0) — The Quest Hub

The **Spawn Village** sits at 0,0 and acts as the Phase 1 main hub — like Stormwind in WoW. It is built, lived-in, and populated with NPCs who give out quests, sell items, and track your progress.

### Key NPCs at Spawn:

**1. The Village Elder**
- First NPC players speak to
- Delivers the opening lore monologue
- Assigns the first "Tutorial Chain" of quests:
  - Craft a bed
  - Build a shelter
  - Kill 10 hostile mobs
  - Visit the Nether
- Rewards: Starter Kit, first XP grant, first Reputation tick with Wildlands

**2. The Blacksmith**
- Sells crafting recipes and early gear
- Has a quest chain around mining and smithing
- Quest rewards tied to your **Class/Trait** — a Warrior gets armor upgrades, a Ranger gets bow upgrades, a Mage gets enchanting bonuses
- Teaches the basics of AuraSkills progression

**3. The Herbalist**
- Alchemist-adjacent NPC — sells potions, gives farming quests
- Quest chain: collect specific plants, brew specific potions
- Unlocks early access to unique recipes for Alchemist trait players

**4. The Merchant**
- Economy and trading quests
- Sends players to trade with villagers (reputation boost)
- Acts as a preview for the future Economy module

**5. The Shrine Keeper**
- Tracks your **Zodiac sign** and explains the system
- Has 13 different dialogue options based on which sign you rolled
- Gives a small passive buff item tied to your sign at the start

---

## 🏛️ The Class Hall

Just outside the Spawn Village sits the **Class Hall** — a large structured building with 13 rooms, one for each personality trait.

### What happens here:
- When players first arrive, they are directed to their **assigned trait room**
- Each room has a **Class Master NPC** (e.g., The Battle Master for Warriors, The Arcane Master for Mages)
- The Class Master gives the first **Trait Quest Chain** — a series of achievements and tasks that teach the player their passive abilities as they tier up:
  - **Apprentice (0%)** — basic passive unlocked (e.g., Warrior: +5% melee damage)
  - **Journeyman (25%)** — second passive unlocked
  - **Expert (50%)** — Master ability unlocked
  - **Master (75%)** — Ultimate item recipe revealed
  - **Ultimate (100%)** — Full prestige, class complete, return to the Lord
- Completing the first full trait prestige is **required** to progress beyond Lord's Plateau later

### Class Hall also contains:
- A **Party Board** — where players can advertise or join parties for boss fights
- A **Clan Board** — lists active clans, wars, and alliances
- A **World Board** — shows the current Calendar event and any active Eclipses/Seasons

---

## 🔬 Professor Craft (Roaming Side Quest NPC)

**Personality:** Think Professor E. Gadd from Super Mario — a whimsical, eccentric goofball who is somehow also one of the most intelligent people in the world. Old, scruffy, mushroom-obsessed. Somewhere between a mad scientist and a naturalist. His quests focus on **exploration, learning crafting recipes, brewing recipes, and discovering new machines** — things that can be done solo or as a team.

**Appearance:** An elderly professor figure — old, weathered, with wild white hair. Vaguely resembles the mushroom guy from Joe Rogan. If you ask him about the mushroom islands, he goes quiet and looks over his shoulder. He knows too much.

### Where to find him:
- **Main Lab** — generates in the closest Volcano biome to Spawn. A proper built structure — discoverable location, finding it for the first time is an achievement unlock
- **Field Labs** — Professor Craft also sets up **lab tents** in various locations across the world as he roams and researches
- **Roaming** — he wanders the volcano biome region between his locations. Dialogue differs depending on where you find him: frantic and excited in the field, more organised in his lab

### His quest chain:
1. **First Contact** — Find Professor Craft's main lab in the volcano biome. He's studying strange energy corrupting the geology. He gives you your first set of crafting/brewing blueprints as an intro reward
2. **The Samples** — Collect lava from 3 different volcano craters. He analyses them and detects Diablo's corruption spreading up from the Nether
3. **The Anomaly** — He sends you to investigate a corrupted mushroom island. *He goes noticeably quiet when he mentions the mushroom islands. He knows something is very wrong there.* The mushrooms have been turned by Diablo's corrupted signals — which El Diablo used to send a Trojan Horse signal to the Gods of the Overworld
4. **The Report** — Return his findings. He writes a full research report that forms the lore basis for what El Diablo has been doing. Rewards unique crafting/machine blueprints unavailable anywhere else

**Why it matters:** Gabriel at the Heavenly Tower **checks if you've completed the Professor Craft quest chain** as one of the "worthiness" requirements for Phase 3.

**Reward:** Unique advancement `[The Scientific Method]` + exclusive crafting/machine recipes + XP grant

---

## ⚔️ AuraSkills Progression (Runs Passively)

The **AuraSkills** system is bridged into PluginV2 and runs from the moment a player joins. As players play normally, they level up skills:

- Mining, Farming, Fighting, Archery, Fishing, etc.
- XP is granted from kills, crafting, gathering
- Your **Trait** amplifies certain skill gains (Warriors level combat faster, Rangers level archery faster)
- Skill levels unlock perks that complement your trait passives

This is not a separate quest — it's ambient progression that rewards playing the game naturally.

---

## 🏰 Clans (Social Layer — Active from Day 1)

Clans are organic. No one is forced into one, but the game heavily rewards being in one:

- `/clan create <name> <#hex>` — any player can become a clan leader
- Clan wars drop **player heads** from PvP kills
- Alliance colors show in name prefix
- **Clan Reputation** is the average of all members' honor scores — a clan's alignment (Holy / Mixed / Evil) affects what story content they can access together
- The Withering Council (36 Withers + 6 Devoiders) **requires clan-based kill tracking** — solo players can form a 1-person clan and progress, but the fights are balanced for groups

---

## ⚖️ Reputation System (Runs Passively)

From the moment of first spawn, every action shifts your **Honor Score** (-1000 to +1000):

| Action | Effect |
|---|---|
| Killing hostile mobs | +Wildlands |
| Trading with villagers | +Celestial |
| Killing players in PvP | -Celestial, +Destroyers |
| Curing zombie villagers | +100 Celestial |
| Defeating Devoider/El Diablo | +Celestial, +Epochian |
| Killing peaceful creatures | -Honor |
| Right-clicking Wither with Wither Rose | -150 Celestial, +100 Corrupted (Pledge to Darkness) |

Your **title** updates dynamically: Hero → Noble → Honorable → Neutral → Dishonorable → Villain → Outlaw

---

## 🐉 THE KEY MILESTONE: Kill the Ender Dragon

**Completion:** 20%
**Server:** End dimension (accessible from Survival 1)

### What triggers:
- Standard vanilla progression — find stronghold, activate portal, kill the Ender Dragon
- The kill is **tracked per player and per clan** in the plugin database

### What happens next:
On the **next Easter** calendar event after the kill:
- **The Redeemer** appears at 0,0 near the Spawn Village
- He reveals the existence of the **Corrupt Portal** (Crying Obsidian frame)
- He begins selling **Crying Obsidian** to the players who killed the dragon
- Players can now build and activate the Corrupt Portal to access **Amplified** (Split Lands 1)

### The Redeemer NPC:
- First named NPC with extended dialogue players encounter post-dragon
- Explains the lore of the Corrupt Portal — Diablo corrupted the portal network when he took over the Nether
- Foreshadows the Withering Council: *"The Nether is more dangerous than it appears. Something old is stirring..."*

---

## 🏔️ The Amplified World (Still Phase 1)

**Server:** Amplified (Split Lands 1) — extreme terrain, towering mountains, deep ravines

Once in Amplified, players:
1. Explore and establish a second base
2. Kill the **Father of Ender** (amplified End dimension boss — a buffed male Ender Dragon)
3. Continue grinding **Wither kills** (these count toward the 36 total across both worlds)

### After Father of Ender dies:
- The Redeemer reappears on the next Easter
- Reveals **Heaven's Gate** — an Aether portal whose top block must be placed at **Y=602** in the Amplified Overworld
- Players can build it but cannot use it yet — they must complete the Withering Council first
- This is the physical preview of Phase 3, visible but locked

---

## 🌑 The Evil Path Branch Point (Available in Phase 1)

At any point in Phase 1, a player can choose to **Pledge to the Darkness**:
- Right-click a Wither with a **Wither Rose**
- This instantly tanks Celestial rep and grants massive Corrupted standing
- The player becomes "Condemned" — no Easter or Christmas bonuses
- They can still play normally, but are now on **Story B (Evil Path)**
- They can repent at any time before entering Diablo's Lair by defeating El Diablo

---

## 📊 Phase 1 Completion Milestones

| Milestone | Completion % |
|---|---|
| First Nether entry | 10% |
| Ender Dragon killed | 20% |
| Father of Ender killed | ~23% (contributes toward Wither count) |
| Professor Craft chain complete | Achievement only |
| Class Hall first trait quest started | Character development |

---

## ✅ Phase 1 Ends When:

The clan has killed both dragons AND has begun accumulating Wither kills toward the 36 required. The Corrupt Portal to Amplified is open, Heaven's Gate is built but locked, and the Nether Roof is beginning to stir.

**Phase 2 begins the moment The Devoid becomes accessible** — triggered when the clan's Wither kill count hits 6.
