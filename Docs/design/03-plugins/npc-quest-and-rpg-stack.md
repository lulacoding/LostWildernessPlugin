# NPC, Quest, And RPG Stack

Part of the [plugins and modules section](README.md). This page captures the recommended external-plugin stack for turning Lost Wilderness into a more MMO-like survival server without rebuilding every system from scratch in Java.

Implementation reality: use [implementation status](../../implementation-status.md) for what is already in code. This page is the recommended direction.

---

## Recommended stack

### Primary recommendation

- **Citizens** for NPC entities, named villagers/humanoids, patrols, stationing, and skin-driven world characters
- **Denizen** for dialogue, cutscenes, scripted interactions, schedules, custom shops, and world-triggered story logic
- **MythicMobs** for custom mobs, bosses, encounter scripting, and future temple/planet guardian fights
- **AuraSkills** for skill progression, player-facing skill menus, and configurable RPG skill XP

### Recommendation based on official documentation

This recommendation is based on the documented capabilities of each plugin:

- **Citizens** documents a large API and feature surface around NPC registries, traits, shops, text, waypoints, GUI hooks, persistence, and pathfinding.
- **Denizen** officially documents two-way integration with Citizens and explicitly positions itself for **questing**, **conversations**, **shops**, and **cutscenes**.
- **MythicMobs** officially documents custom mob skills, AI controls, factions, threat tables, spawners, random spawning rules, levels, and boss scripting.
- **AuraSkills** officially documents built-in skills, configurable menus/rewards/loot, a stable API, and direct compatibility with MythicMobs.

### Why this is the best fit for Lost Wilderness

- Lost Wilderness already has a **custom story progression core** (`Milestone`, `ProgressionService`, Aether gate, personality, cross-server progression).
- The project does **not** need to reinvent generic MMO systems when the differentiator is the **lore, gates, worlds, and chapter progression**.
- Citizens + Denizen handles the most important missing layer: **NPC-driven storytelling**.
- MythicMobs handles the most expensive content layer to build yourself: **custom enemies and boss behaviors**.
- AuraSkills replaces the custom skill grind layer with a maintained plugin that already has menus, XP sources, and balancing tools.

---

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

---

## What stays custom in Lost Wilderness

These systems should remain in the Lost Wilderness codebase even if plugin-based RPG tooling is adopted:

- `ProgressionService` and `player_progression`
- `Milestone` enum and chapter gating
- cross-server Survival <-> Amplified progression persistence
- Aether gate / lore gates / world access rules
- personality assignment and future chapter-based class mapping
- faction reputation if it remains lore-specific
- custom boss-to-story hooks in `BossKillTracker`

### Why

These are the parts that make Lost Wilderness unique. External plugins should provide **content tooling**, while your plugin remains the **source of truth for lore progression**.

---

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

---

## Random villages with NPCs

The best way to make the Survival world feel like an MMO world is:

1. **Generate villages or settlements naturally**
2. **Populate them with Citizens NPCs**
3. **Script them with Denizen**
4. **Bridge them to your milestone and reputation systems**

---

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

---

## Documented integration and caveats

### Denizen + Citizens

Denizen is not just "compatible" with Citizens. The official docs describe a two-way integration:

- Denizen adds commands and tools directly into Citizens
- Denizen provides traits and scripting for Citizens NPCs

Documented examples include:

- `/npc sit`
- `/npc sneak`
- `/npc invisible`
- `/npc mirrorskin`
- traits like `health` and `fishing`

This is why these two plugins should be treated as a pair, not as separate competing options.

### MythicMobs + AuraSkills

AuraSkills documents direct MythicMobs support through the `giveSkillXP` mechanic.

That means a MythicMobs boss can grant AuraSkills XP on death without custom Java just for the basic reward path.

This is a strong reason to avoid overbuilding a parallel custom skill system.

### AuraSkills API and operations

AuraSkills documents:

- a stable Bukkit API
- configurable menus/rewards/loot
- SQL-friendly use on real servers
- an important operational rule: **do not rely on reload**, restart the server instead

Operational notes from the docs that matter:

- block-replacement checks should be configured correctly to avoid duplicate XP
- action bar conflicts can be coordinated with ProtocolLib
- uninstalling/resetting health has a documented process, meaning health/stat systems should be treated carefully in production

### MythicMobs operational notes

MythicMobs docs/FAQ note several practical concerns:

- bosses above 2000 HP may require raising `max-health` in `spigot.yml`
- complex mobs should use cooldown/randomization patterns rather than firing every skill at once
- passive mobs should often be disguised hostile mobs if they need to attack

These constraints should influence how Lost Wilderness designs chapter bosses and guardian encounters.

### Recommended architecture

#### Layer 1: world generation

Use one of:

- custom datapacks / custom structure packs
- a worldgen or structure plugin
- schematic-based structure spawning

The goal is not to make Citizens generate villages. The goal is to let the world generator place settlements, then let Citizens populate them.

#### Layer 2: NPC markers

Every generated village template should contain hidden markers for NPC spawn points, for example:

- armor stands named `npc_blacksmith`
- structure markers under the floor
- designated coordinates relative to a village center

When your plugin detects a newly generated village, it converts those markers into NPCs.

#### Layer 3: persistent registration

The Lost Wilderness plugin should store:

- village ID
- world
- chunk or bounding box
- template type
- NPC roles already spawned

This prevents duplicates when chunks reload and makes villages persistent.

#### Layer 4: scripted behavior

Denizen should control:

- dialogue
- schedules
- shops
- local rumors
- quest starts and completions
- milestone-sensitive dialogue

Your plugin should control:

- chapter gating
- progression writes
- faction/reputation hooks
- cross-server persistence

---

## Good village themes for Survival

- **Starter trade villages**: bread, tools, directions, beginner quests
- **Brotherhood outposts**: scripture hints, chapter lore, milestone-aware dialogue
- **Hunter camps**: combat and mob-hunting tasks
- **Pilgrim shrines**: small lore locations with low-density NPC populations
- **Fishing hamlets**: lead-ins for Aquaria and ocean progression
- **Mountain monasteries**: skill trainers, heavenly/firmament foreshadowing
- **Ruined settlements**: lore-rich but hostile, ideal for MythicMob encounters

---

## More MMO-like ideas for Lost Wilderness

These ideas fit the project without turning it into a generic theme-park MMO.

### World and exploration ideas

- rare hidden villages that only generate in remote latitudes
- weather-reactive towns (snow villages behave differently during Blizzard/Frost)
- regional caravan routes with guards and traders
- ruined pilgrimage roads connecting temples, shrines, and Brotherhood locations
- floating sky outposts that only unlock post-El-Diablo

### NPC role ideas

- wandering scripture readers
- innkeepers that tell rumors based on real world-state
- blacksmiths who unlock stock based on chapter milestones
- hidden Brotherhood librarians
- false prophets / corrupted informants in dangerous biomes
- faction envoys that react to reputation

### Quest ideas

- weekly expedition boards in villages
- seasonal quest chains tied to eclipse / New Year / winter
- chain quests that send players from Survival villages to Amplified destinations
- scripture scavenger hunts using bookshelves, signs, and clues
- repeatable bounty boards for clan and party play

### Group-content ideas

- regional minibosses guarded by MythicMobs packs
- party-scaled village defense events
- seasonal invasions attacking remote settlements
- Brotherhood trials in hidden underground sanctuaries
- instanced or pseudo-instanced temple boss rooms tied to milestones

### Social/economy ideas

- village reputation tracks separate from global factions
- supply-and-demand traveling merchants
- tax or tithing systems for lore-rich religious settlements
- village boards listing real player accomplishments
- local currencies or relics that convert into global wealth later

### Story-delivery ideas

- NPC dialogue changes when major bosses die
- traveling preachers hint at the next chapter
- Brotherhood scribes update clues after milestones
- world rumors point to Aquaria, Heavenly Tower, or Elemental Temples
- hidden NPCs only appear after a story trigger, a season, or a celestial event

---

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

---

## Ranked action plan

This is the recommended execution order if the goal is to move from "interesting codebase" to "first playable MMO-style survival experience" as fast as possible without painting the project into a corner.

### Rank 1: lock the stack and stop duplicate work

#### Do now

1. Decide the primary stack is `Citizens + Denizen + MythicMobs + AuraSkills`
2. Freeze further custom work on:
   - custom skill system expansion
   - custom quest engine
   - custom NPC dialogue engine
   - custom NPC shop engine
3. Keep custom work only for:
   - milestones
   - chapter gates
   - cross-server progression
   - personality
   - faction reputation

#### Why this is first

Without this decision, the project risks building the same feature twice: once in Java, then again in external plugins.

### Rank 2: install and validate the plugin stack on staging

#### Do next

1. Install **Citizens**
2. Install **Denizen**
3. Install **MythicMobs**
4. Install **AuraSkills**
5. Start a test server with all four together
6. Verify:
   - plugins boot cleanly
   - no version conflicts on Paper
   - Citizens NPCs can spawn
   - Denizen scripts load
   - MythicMobs mobs spawn
   - AuraSkills menus and XP work
   - AuraSkills is tested with full restarts instead of reload-based workflows
   - MythicMobs boss health requirements are compatible with `spigot.yml` `max-health`

#### Why this is second

MythicMobs and Citizens are the most likely to reveal version-specific friction. It is cheaper to prove compatibility before more design work.

### Rank 3: build the bridge layer in the Lost Wilderness plugin

#### Do after plugin install

Create a small integration layer in the custom plugin for:

1. milestone writes from scripted quests or dialogue
2. reputation writes from scripted outcomes
3. optional AuraSkills XP grants from custom story events
4. village registration / spawned-NPC persistence helpers
5. hooks for boss deaths and story gates

#### Why this is third

This preserves what makes Lost Wilderness unique while letting external plugins do the content work.

### Rank 4: create the first vertical slice

Build one complete playable content slice before expanding:

1. one generated village template
2. one blacksmith NPC
3. one scripture/lore NPC
4. one rumor/guide NPC
5. one Denizen dialogue chain
6. one simple reward or milestone check
7. one MythicMobs miniboss nearby

#### Success criteria

- player discovers a village naturally
- NPCs are already there
- one NPC gives real dialogue
- one NPC references existing milestones
- one fight or encounter exists nearby
- progression persists between Survival and Amplified

#### Why this is fourth

It gives you a testable MMORPG feel without committing to a giant system rollout.

### Rank 5: convert exploration into content loops

Once the first slice works, expand into repeatable gameplay loops:

1. village board quests
2. rumor-driven exploration
3. miniboss hunts
4. settlement defense events
5. faction-based rewards
6. caravan or trader encounters

#### Why this matters

MMO feel comes from loops, not just isolated NPCs.

### Rank 6: connect story chapters to the world

Use the plugin stack to make the storyline visible in-world:

1. Brotherhood outposts react to chapter progress
2. rumors point players toward Aquaria and Heavenly Tower
3. milestone-gated NPCs appear after Devoider / El Diablo
4. faction envoys unlock new lines and rewards
5. post-El-Diablo sky or temple content starts to show up

#### Why this comes later

Do not try to script the whole storyline before the NPC/content pipeline is proven on one slice.

### Rank 7: expand by biome and server role

After the first slice and world-story loop are working:

1. plains starter villages
2. desert Brotherhood settlements
3. snowy weather-reactive towns
4. mountain monasteries
5. fishing hamlets
6. ruined corrupted settlements
7. Amplified-exclusive settlements that feel like another dimension

#### Why this is later

Biome expansion is content multiplication. It should happen after the base process is stable.

### Rank 8: only then decide what custom systems to retire

After several weeks of testing the external stack:

1. decide whether `AuraSkills` fully replaces custom skills
2. decide whether your custom wallet survives or Vault economy is better
3. decide whether custom party remains worth keeping
4. decide whether custom reputation should stay bespoke or be partially delegated

#### Why this is last

Do not delete custom systems before the replacement stack proves itself in practice.

---

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

