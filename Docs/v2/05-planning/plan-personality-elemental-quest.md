## TASK CONTEXT

> **Status Update (2026-03-19):** Core service layer is **IMPLEMENTED**. Quiz system and effect listeners are **NOT YET IMPLEMENTED**.
> 
> **✅ Implemented:**
> - PersonalityModule (RpgModule wiring)
> - TraitService / TraitServiceImpl (profile management, tier advancement)
> - TraitRepository (DB CRUD)
> - PersonalityTrait enum (13 traits)
> - Element enum (5 elements)
> - TraitTier enum (4 tiers)
> - HolyEnchant enum (13 enchants)
> - HolyEnchantService
> - CompletionService
> - PlayerTraitProfile record
>
> **❌ Not Implemented:**
> - QuizSessionManager (personality quiz flow)
> - QuizCompletionHandler (scoring → trait assignment)
> - TraitEffectListener (passive ability listeners)
> - TraitCommand (`/trait info/set/revealelement/bless/quiz`)
> - Quiz questions & scoring matrix
> - Integration with `/datejoined` output

You are implementing the **Personality Trait & Elemental Quest system** into the RPGCoreV2 Minecraft plugin (Paper/Java). This is a modular plugin with existing systems for: CalendarServiceV2 (MC date tracking), PlayerProfile (player data), Faction/alignment (Good/Evil via ReputationService), Clan system, EventServiceImpl (world events), and the Zodiac system (rpgcore.zodiac). You will create a new `rpgcore.personality` module that integrates cleanly with all of these.

The system covers:
- A scripted **personality quiz** on first login (after cutscene)
- Assignment of a **primary Personality Trait** (e.g. Mage) with 4 progressive tiers
- A **hidden Element** (Fire, Earth, Wind, Water, Aether) revealed later by the Lord
- Trait tiers unlocking passive abilities + an **Ultimate Trait item** with **Holy Enchant**
- The **Elemental Journey** (post-Heaven, post-story) where players complete 5 Elemental Temples to unlock god-tier passives
- A **300% completion** post-game state with annual Christmas Holy Enchant rewards

---

## TONE CONTEXT
Write production-quality Java code. Follow the existing RpgModule pattern (interface + impl, ServiceRegistry, ModuleContext). Be consistent with existing codebase conventions. Minimal comments only where logic is non-obvious.
a
---

## BACKGROUND DATA, DOCUMENTS & ARCHITECTURE

### Existing Systems to integrate with:
- **PlayerProfile** (`rpgcore.player`): UUID, lastSeenAt. Loaded on AsyncPlayerPreLoginEvent, saved on quit. Cache: ConcurrentHashMap.
- **CalendarServiceV2** (`rpgcore.calendar`): `getCurrentSnapshot()` (LocalDate, dayCount/epoch, season, mcDay), `getPlayerJoinDate(UUID)`. Used to determine story completion milestones.
- **ReputationService** (`rpgcore.reputation`): `getHonorScore(UUID)` → positive = Good, negative = Evil. Faction enum: CELESTIAL/EPOCHIANS (Good), WILDLANDS (Neutral), CORRUPTED/DESTROYERS (Evil). Alignment determines Trait variant (some traits have Good/Evil flavour).
- **ZodiacService** (`rpgcore.zodiac`): `ZodiacProfile` has a `personality` String field to be populated from quiz secondary signal.
- **ClanService** (`rpgcore.clans`): `getPlayersClan(UUID)`, clan leader UUID. Post-game 300% players get boosted event rewards.
- **EventServiceImpl** (`rpgcore.events`): Check for 300% completion flag to apply boosted loot/XP during world events.
- **SurvivalCalendarCommands**: Existing `/datejoined` command to extend with Trait/tier info.
- **Permission node for Lord rank**: `lw.rank.lord` (confirm with existing permission system).

### Story Progression Chain (full order):
```
Spawn → Nether → Ender Dragon → Amplified → Father of Ender →
Wither (×36) → The Devoid → Devoider (×6) → Hell → Diablo's Lair →
El Diablo Fight → Neptune → Heavenly Tower → Gabriel & Rainbow Path →
Lord's Plateau → [Element 1 Temple] → [Element 2–5 Temples] →
Credits Cutscene → Post-Game
```

### Completion Tiers:
| % | Covers | Unlocks |
|---|--------|---------|
| 100% | LW Story complete | All Trait tiers, Ultimate items |
| 200% | + All Vanilla & base custom advancements | Prestige cosmetics |
| 300% | + Full Elemental Journey (5 temples) | God-tier passives, Christmas Holy Enchants |

### World Events as throttle:
Existing events (Tornadoes, Eclipses, Blood Moons, Fog, Blizzards, Tsunamis) naturally slow early progression and become XP farms for 300% players. No new events needed — just integrate the 300% flag into EventServiceImpl reward scaling.

---

## DETAILED TASK DESCRIPTION & RULES

### System 1: Personality Quiz

The quiz runs in a **scripted sequence** immediately after the first-login cutscene. Player cannot move freely until quiz is complete. Three question types:

1. **Multiple choice** — Text-based lore questions (e.g. "You find treasure in the forest — do you keep it or share it with your clan?", "Do you prefer PvP or crafting?"). Answers score points toward each Trait category in a scoring matrix.

2. **Crafting challenge** — Player is given a specific set of items and a crafting table. Told to craft ONE item. The item crafted is recorded and scored toward traits (weapon → Combat, tool → Builder, food/potion → Healer, etc.).

3. **Creative selection (final question)** — A GUI opens with all items available. Player picks one item. This is mapped by category to the **secondary personality** signal stored in `ZodiacProfile.personality`. Categories: COMBAT (weapons/armor), BUILDER (blocks/tools), NURTURER (food/potions), ARTIST (decorations/dyes), MYSTIC (enchanting/potions), EXPLORER (maps/compasses/boats).

Highest-scoring Trait = player's primary Trait. Ties broken by ThreadLocalRandom.

### System 2: Personality Traits & Tiers

Tiers advance automatically when the player's **story completion %** crosses thresholds:

| Tier | Threshold | Ability |
|------|-----------|---------|
| APPRENTICE | 0% (quiz assigned) | Title only, no abilities |
| TRAIT | 25% | Tier 1 passive |
| MASTER | 50% | Tier 2 passive |
| ULTIMATE | 100% | Tier 3 passive + Holy Enchant granted + Ultimate Trait Item given |i 

**Full Trait list with abilities:**
have 
**MAGE**
- Apprentice: No ability
- Mage: Splash & lingering potions +50% larger AoE (via PotionSplashEvent radius scaling)
- Master Mage: Potion stacking — same potion re-applied adds duration instead of replacing
- Ultimate Mage: Receives **Mage Bow** (Crossbow, all Vanilla enchants at max + Flame-like custom Trait Enchant). Can be **Blessed** by Lord: doubles all Vanilla enchant levels + adds random Holy Enchant.

**WARRIOR**
- Apprentice: No ability
- Warrior: +10% melee damage (EntityDamageByEntityEvent multiplier)
- Master Warrior: Shield cooldown reduced by 50%
- Ultimate Warrior: Receives **Warlord's Blade** (Sword, max enchants + custom Trait Enchant "Berserker" — damage scales with missing health). Blessable.

**ARCHER**
- Apprentice: No ability
- Archer: +15% bow/crossbow damage
- Master Archer: Arrows pierce 1 additional entity (Piercing I equivalent free)
- Ultimate Archer: Receives **Celestial Bow** (Bow, max enchants + custom "Homing" trait — arrows curve slightly toward last aimed target). Blessable.

**HEALER**
- Apprentice: No ability
- Healer (Good): AoE Regen I to nearby players every 15s (10 block radius). (Evil): AoE Wither I to nearby enemies every 15s.
- Master Healer: Potions thrown by this player last 50% longer on targets.
- Ultimate Healer: Receives **Staff of the Covenant** (custom item/trident, max enchants + "Sanctify" trait — on hit, cleanses negative effects from allies OR applies them to enemies). Blessable.

**RANGER**
- Apprentice: No ability
- Ranger: +20% speed in forested/jungle biomes
- Master Ranger: Tamed wolves/cats follow more aggressively, +15% tamed mob damage
- Ultimate Ranger: Receives **Ranger's Quiver** (custom item that auto-applies Infinity to any bow held). Blessable.

**ALCHEMIST**
- Apprentice: No ability
- Alchemist: Brewed potions have +25% duration
- Master Alchemist: Can brew 4 potions simultaneously (brewing stand override)
- Ultimate Alchemist: Receives **Flask of Eternity** (custom item, on use grants a random positive potion for 10 min, 30 min cooldown). Blessable.

**SMITH**
- Apprentice: No ability
- Smith: Anvil repair costs -2 levels
- Master Smith: Crafting recipes for gear use 20% fewer materials (recipe override)
- Ultimate Smith: Receives **The Eternal Hammer** (custom item, on use repairs held item by 50%, 5 min cooldown). Blessable.

**SCOUT**
- Apprentice: No ability
- Scout: +15% movement speed permanently
- Master Scout: Sneak speed is equal to walk speed (no slow)
- Ultimate Scout: Receives **Shadowstep Boots** (custom Boots, max enchants + "Phantom Step" trait — short-range blink on double-sneak, 10s cooldown). Blessable.

**BERSERKER**
- Apprentice: No ability
- Berserker: Damage taken below 30% HP triggers Strength I for 5s (10s cooldown)
- Master Berserker: +20% damage when not wearing chestplate
- Ultimate Berserker: Receives **Ragnarok Axe** (Axe, max enchants + "Bloodlust" trait — each kill refreshes a 3s Speed I burst). Blessable.

**SAGE**
- Apprentice: No ability
- Sage: +25% XP gain from all sources
- Master Sage: Enchanting table gives one extra enchant option
- Ultimate Sage: Receives **Tome of Infinite Wisdom** (custom Book item, on use grants 5 levels of XP, 30 min cooldown). Blessable.

**TAMER**
- Apprentice: No ability
- Tamer: +25% chance taming animals succeeds on first attempt
- Master Tamer: Tamed mobs gain +30% max HP
- Ultimate Tamer: Receives **Ancient Whistle** (custom item, summons a personal wolf companion that persists across sessions). Blessable.

**RUNEKEEPER**
- Apprentice: No ability
- Runekeeper: Enchanted items in hand glow with particle effects (cosmetic) + +10% enchant effectiveness
- Master Runekeeper: Enchanting no longer requires Lapis Lazuli
- Ultimate Runekeeper: Receives **Runeblade** (custom Sword, max enchants + "Runic Overload" trait — on critical hit, triggers a random enchant effect from the weapon again). Blessable.

**ILLUSIONIST**
- Apprentice: No ability
- Illusionist (Good): Can cast a harmless decoy (spawn a fake "ghost" version of self for 5s, 2 min cooldown). (Evil): Decoy attracts mob aggro.
- Master Illusionist: Invisibility potions last 3× longer on this player
- Ultimate Illusionist: Receives **Mirror Shard** (custom item, on use briefly swaps position with target player, 5 min cooldown). Blessable.

> Total: 13 Traits to mirror the 13 Zodiac signs thematically. ~64 personality combinations emerge from quiz scoring weights across multiple traits.

### Holy Enchants

- One Holy Enchant is granted per Ultimate Trait (5 total if all 5 Traits are Ultimated).
- Holy Enchants are **custom enchantments** above vanilla max tier with unique effects.
- Each Holy Enchant is specific to a Trait's Ultimate item.
- Only **one of each Holy Enchant** per player — no duplicates.
- **Christmas drop**: On December 25 (real date), players with 300% completion receive **9 Holy Enchants** randomly selected from the full pool, excluding any already held.
- Holy Enchant pool examples: SOULFIRE, DIVINE_SHIELD, CELESTIAL_STRIKE, VOID_PIERCE, NATURES_GRASP, THUNDERCLAP, SERPENTS_FANG, LUNAR_BLESSING, STARFALL, ANCIENT_WARD, PHOENIX_FLAME, TITANIC_FORCE, ECHO_STEP.

### Blessing Mechanic

- Lord-rank player (`lw.rank.lord`) can **Bless** any player's Ultimate Trait item.
- Blessing: Doubles all Vanilla enchant levels (up to hard cap) + adds one random Holy Enchant from the pool (not already on the item).
- Tracked in `player_traits` table per item.

### System 3: Element Assignment & Reveal

Each player is silently assigned **one of 5 Elements** from quiz scores. The Element is **not revealed** until the Lord runs `/trait revealelement <player>`. Before reveal: Element has NO passive effect.

| Element | Passive (after Temple quest complete) |
|---------|--------------------------------------|
| FIRE | Immune to fire and lava damage |
| WIND | Elytra never loses durability |
| EARTH | Immune to suffocation damage |
| WATER | Permanent underwater breathing |
| AETHER | ~90% fall damage reduction |

Elements are only **activated** after:
1. All assigned Traits are ULTIMATE tier.
2. Lord has revealed the Element.
3. Corresponding Elemental Temple quest is complete (BetonQuest-driven).

### System 4: Elemental Journey Flow

After Lord's Plateau in the story, the Lord reveals Element 1. Player completes Temple 1 → returns to Lord → Lord reveals Element 2. Repeat for all 5.

After all 5 complete: Credits cutscene plays. Player is **post-game**:
- All 5 elemental passives active simultaneously. make
- Flag `is_postgame = true` stored in DB.
- EventServiceImpl checks this flag: post-game players get +50% drops/XP from world events.
- `/datejoined` shows "⭐ Post-Game God" status.

### Trait Progression Tracking

Completion % comes from BetonQuest milestone tracking (existing `rpgcore.progression` module). The `TraitService` subscribes to completion % updates and auto-advances tiers.

Tier check method: `TraitServiceImpl.checkAndAdvanceTier(UUID)` — called:
- On PlayerJoinEvent (catch up any offline progression)
- When BetonQuest fires a milestone event (hook into existing milestone listener)

---

## EXAMPLES

### Example: Quiz scoring matrix (simplified)
```java
// Question: "You find treasure. Do you: A) Keep it, B) Share with clan, C) Use it for crafting, D) Ignore it"
// Answer A → BERSERKER+2, SCOUT+1
// Answer B → HEALER+2, SAGE+1, WARRIOR+1
// Answer C → SMITH+2, ALCHEMIST+1
// Answer D → ILLUSIONIST+2, RANGER+1
```

### Example: Tier advance on milestone
```java
// Player crosses 25% story completion
traitService.checkAndAdvanceTier(player.getUniqueId());
// Apprentice Mage → Mage: send title + action bar message + grant Tier 1 ability
player.sendTitle("§6§lMage", "§eYour arcane studies begin to bear fruit.", 10, 60, 20);
```

### Example: Mage AoE expansion
```java
@EventHandler
public void onPotionSplash(PotionSplashEvent event) {
    if (!(event.getPotion().getShooter() instanceof Player p)) return;
    TraitProfile profile = traitService.getProfile(p.getUniqueId());
    if (profile != null && profile.activeTrait() == PersonalityTrait.MAGE
            && profile.activeTier().ordinal() >= TraitTier.TRAIT.ordinal()) {
        // Increase splash radius by re-applying effects to players in extended radius
        // Default splash radius ~4 blocks → extend to 6 blocks
    }
}
```

### Example: Christmas Holy Enchant drop
```java
// Scheduler checks real date on server startup and daily
LocalDate today = LocalDate.now();
if (today.getMonthValue() == 12 && today.getDayOfMonth() == 25) {
    for (Player p : Bukkit.getOnlinePlayers()) {
        if (completionService.getCompletionPercent(p.getUniqueId()) >= 300) {
            holyEnchantService.grantChristmasEnchants(p.getUniqueId(), 9);
        }
    }
}
```

### Example: /datejoined extended output (post-game player)
```
You first joined on: 15/3/5 MC
Your Zodiac Sign: Leo ♌  |  Year Sign: Sagittarius ♐
Active Trait: §6Ultimate Mage  |  Element: §cFire (Unlocked)
Completion: §a300% §7— ⭐ Post-Game God
```

---

## IMMEDIATE TASK

Implement the full `rpgcore.personality` module. Build in this order:

1. **`PersonalityTrait.java`** enum — 13 values, each with tier ability descriptions, Ultimate item definition, Holy Enchant assignment.
2. **`Element.java`** enum — 5 values with passive description and unlock requirement.
3. **`TraitTier.java`** enum — APPRENTICE, TRAIT, MASTER, ULTIMATE with completion % thresholds.
4. **`HolyEnchant.java`** enum — full pool of ~13 Holy Enchants with name, effect description, applicable item types.
5. **`PlayerTraitProfile.java`** record — trait slots (up to 5), tiers per slot, active trait index, element slots (up to 5), element revealed/quest-complete flags, is_postgame flag, is_blessed per item.
6. **`TraitRepository.java`** — async DB CRUD for `player_traits`, `player_elements`, `player_quiz_answers` tables (H2/MySQL, follow existing repository pattern).
7. **`TraitService.java`** interface + **`TraitServiceImpl.java`** — assign on quiz, advance tiers, reveal element, reveal second trait (Lord only), grant Christmas enchants, check post-game status.
8. **`QuizSessionManager.java`** — in-memory state machine per UUID: tracks current question index, answers, scores. Questions defined as static list of `QuizQuestion` records.
9. **`QuizCompletionHandler.java`** — scores answers → assigns Trait + Element → calls TraitService → notifies ZodiacService of secondary personality.
10. **`TraitEffectListener.java`** — Bukkit listeners applying all tier passive effects (PotionSplashEvent, EntityDamageByEntityEvent, PlayerMoveEvent, etc.). Check `traitService.getProfile(uuid)` for active trait and tier.
11. **`TraitCommand.java`** — `/trait info [player]`, `/trait set <trait>`, `/trait revealelement <player>` (Lord only), `/trait bless <player>` (Lord only), `/trait quiz` (admin restart quiz).
12. **`PersonalityModule.java`** — `RpgModule` wiring everything together, loads on survival-plugin.

Place all files in: `PluginV2/src/main/java/rpgcore/personality/`

Also extend:
- `SurvivalCalendarCommands` → `/datejoined` shows active Trait, tier, Element status, completion %.
- `EventServiceImpl` → check `traitService.isPostGame(uuid)` for +50% event rewards.

---

## THINK STEP BY STEP

Before writing each class, check:
1. Which existing services does it depend on? Inject via `ServiceRegistry`.
2. Does it follow the `RpgModule` / `RpgService` pattern of existing modules (CalendarModule, ClanModule, ReputationModule)?
3. Are all DB operations async (CompletableFuture, like PlayerProfileRepository)?
4. Is state loaded on `AsyncPlayerPreLoginEvent` and unloaded on `PlayerQuitEvent`?
5. Does it respect Good/Evil alignment from `ReputationService` for trait variants?

---

## OUTPUT FORMAT

Output each Java file completely with full package declarations and imports. After all files, provide:
1. A summary table of all files created and their purpose.
2. Any SQL migration scripts for the new tables.
3. Integration steps: what to add to the survival-plugin module loader list and any config keys to add to `core.yml`.
