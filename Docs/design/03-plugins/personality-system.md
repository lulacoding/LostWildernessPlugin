# Personality Trait & Elemental Quest System

## Overview

The **Personality Trait & Elemental Quest System** is a deep character progression system that assigns players one of 13 personality traits and one of 5 elemental affinities through a lobby quiz, then progresses them through 4 tiers (Apprentice → Ultimate) based on completion percentage (0-300%).

**Key Features:**
- **Personality Quiz** (Lobby): 10-question quiz on first join assigns primary trait + element
- **4 Progression Tiers**: APPRENTICE (0%), TRAIT (25%), MASTER (50%), ULTIMATE (100%)
- **Completion System**: 0-100% (story), 100-200% (advancements), 200-300% (temples)
- **Ultimate Items**: Legendary items with max enchants + custom trait enchants
- **Elemental Journey**: 5 temple quests unlock god-tier elemental passives
- **Post-Game State**: All 5 temples + 300% completion unlocks bonus rewards
- **Holy Enchants**: 13 custom enchantments above vanilla max tier

## Architecture

### Cross-Server Flow

```
1. Player joins Lobby
2. Quiz triggers on first join (no trait assigned yet)
3. Answer 10 questions → Trait + Element assigned to database
4. Portal entry now allowed (quiz completion required)
5. Enter Survival → Profile loaded from database
6. Progression tracked → Tier advances at 25/50/100%
7. Ultimate item granted at 100% completion
```

### Module Structure

**Lobby Plugin** (`PluginV2/lobby-plugin/`):
- `personality/QuizSessionManager.java` - Quiz state machine
- `personality/QuizCompletionHandler.java` - Result processing
- `personality/QuizTriggerListener.java` - First-join detection
- Uses `TraitRepository` from RPGCore (shared database)

**RPGCore Plugin** (`PluginV2/src/main/java/com/lostwilderness/rpgcore/personality/`):
- `PersonalityModule.java` - RpgModule implementation
- `TraitService.java` / `TraitServiceImpl.java` - Core service
- `TraitRepository.java` - Database layer (5 tables)
- `CompletionService.java` - 0-300% calculator
- `HolyEnchantService.java` - Custom enchantment system
- Data models: `PersonalityTrait`, `TraitTier`, `Element`, `HolyEnchant`, `PlayerTraitProfile`

**Survival Plugin** (`PluginV2/survival-plugin/` - Not yet implemented):
- `personality/TraitPassiveListener.java` - Trait ability effects
- `personality/ElementalPassiveListener.java` - Element passive effects
- `personality/HolyEnchantEffectListener.java` - Holy Enchant effects
- `personality/TraitItemListener.java` - Ultimate item mechanics
- `personality/TraitCommand.java` - `/trait` command executor

## 13 Personality Traits

Each trait has a unique progression path with 4 tiers:

| Trait | Symbol | Ultimate Item | Trait Enchant | Description |
|-------|--------|---------------|---------------|-------------|
| **MAGE** | 🔮 | Mage Bow (Crossbow) | MAGE_FOCUS | Master of arcane potions and magic |
| **WARRIOR** | ⚔ | Warlord's Blade | BERSERKER | Frontline fighter, skilled in melee |
| **ARCHER** | 🏹 | Celestial Bow | HOMING | Precise marksman with deadly aim |
| **HEALER** | ✚ | Staff of Covenant | SANCTIFY | Supporter or plague-bringer (alignment) |
| **RANGER** | 🌲 | Ranger's Quiver | INFINITY_LINK | Nature's guardian, friend to beasts |
| **ALCHEMIST** | ⚗ | Flask of Eternity | ETERNAL_BREW | Brewing expert with endless concoctions |
| **SMITH** | 🔨 | Eternal Hammer | MASTER_CRAFT | Craftsman who bends metal to will |
| **SCOUT** | 👁 | Shadowstep Boots | PHANTOM_STEP | Swift and agile, always one step ahead |
| **BERSERKER** | 💢 | Ragnarok Axe | BLOODLUST | Raging warrior who thrives in chaos |
| **SAGE** | 📖 | Tome of Infinite Wisdom | KNOWLEDGE | Scholar seeking knowledge |
| **TAMER** | 🐺 | Ancient Whistle | BEAST_MASTER | Beast master with loyal companions |
| **RUNEKEEPER** | ✨ | Runeblade | RUNIC_OVERLOAD | Enchanter wielding runic power |
| **ILLUSIONIST** | 🎭 | Mirror Shard | ILLUSION | Trickster who bends reality (alignment) |

### Trait Tier Progression

**APPRENTICE (0% completion)**:
- Starting tier after quiz
- No passive abilities yet
- Learning phase

**TRAIT (25% completion)**:
- First passive ability unlocked
- Examples: +10% melee damage (WARRIOR), +15% bow damage (ARCHER), +20% forest speed (RANGER)

**MASTER (50% completion)**:
- Second passive ability unlocked
- Examples: Shield cooldown -50% (WARRIOR), Arrow pierce +1 (ARCHER), Sneak = walk speed (SCOUT)

**ULTIMATE (100% completion)**:
- Ultimate item granted automatically
- Full trait description: "Granted [Item Name] - [special effect]"
- Item has max vanilla enchants + trait enchant

## 5 Elements

Elements are assigned during the quiz but **kept hidden** until revealed by a Lord. Once revealed, players must complete temple quests to activate elemental passives.

| Element | Symbol | Passive (Activated) | Temple Location |
|---------|--------|---------------------|-----------------|
| **FIRE** | 🔥 | Immune to fire and lava damage | TBD (Fire Temple) |
| **EARTH** | 🌍 | Immune to suffocation damage | TBD (Earth Temple) |
| **WIND** | 💨 | Elytra never loses durability | TBD (Wind Temple) |
| **WATER** | 💧 | Permanent underwater breathing | TBD (Water Temple) |
| **AETHER** | ⭐ | ~90% fall damage reduction | TBD (Aether Temple) |

### Element Flow

1. **Quiz Completion**: Element assigned but hidden
2. **Lord Reveal** (`/trait revealelement <player>`): Element shown to player
3. **Temple Quest Complete** (BetonQuest): First temple activates player's element passive
4. **All 5 Temples Complete**: Post-game state (if also 300% completion)

## Completion System (0-300%)

The completion percentage determines tier advancement and post-game status.

### Story Milestones (0-100%)

| Milestone | Completion % |
|-----------|--------------|
| First Join | 5% |
| Nether Portal | 10% |
| Ender Dragon | 20% |
| Wither x36 | 30% |
| Devoider x6 | 40% |
| El Diablo | 50% |
| Heavenly Tower | 70% |
| Lord's Plateau | 100% |

### Vanilla Advancements (100-200%)

Bonus layer: Completing all vanilla advancements (excluding recipes) adds 0-100% bonus.

**Calculation**: `(completed / total) * 100`

### Elemental Temples (200-300%)

Bonus layer: Completing all 5 elemental temples adds 100% bonus.

**Calculation**: 5 temples × 20% = 100%

**Total possible**: 300% (100% story + 100% advancements + 100% temples)

## Ultimate Items

Ultimate items are granted at **100% completion** (ULTIMATE tier).

### Creation Process

1. **Base Item**: Material from trait definition (e.g., CROSSBOW for MAGE)
2. **Display Name**: Gold + Bold + Ultimate item name
3. **Max Enchants**: Vanilla enchants at 2× normal max (unsafe enchanting)
4. **Trait Enchant**: Applied via `HolyEnchantService` (stored in PDC)
5. **Lore**: "Ultimate Item" + tier description

### Example: Mage Bow (MAGE Ultimate)

```yaml
Material: CROSSBOW
Display Name: §6§lMage Bow
Enchantments:
  - Piercing X (max 10)
  - Quick Charge V (max 5)
  - Multishot I
Trait Enchant: MAGE_FOCUS (stored in PDC)
Lore:
  - §5§oUltimate Item
  - §7Granted Mage Bow with max enchants + Mage Focus trait
```

### Blessing Mechanic

Lords can bless Ultimate items using `/trait bless <player>`:

1. **Double Enchant Levels**: All vanilla enchants doubled (up to 2× max)
2. **Add Holy Enchant**: Random applicable Holy Enchant added
3. **Lore Update**: Holy Enchant displayed in gold text

**Blessing Requirements**:
- Item must have a trait enchant (Ultimate item)
- Player must be holding the item
- Lord must have `lw.rank.lord` permission

## 13 Holy Enchants

Holy Enchants are custom enchantments stored in PDC, displayed via lore, and applied through effect listeners.

| Holy Enchant | Description | Applicable Items |
|--------------|-------------|------------------|
| **SOULFIRE** | Burns enemies with divine flame bypassing armor | Sword, Trident |
| **DIVINE_SHIELD** | Grants absorption hearts when blocking | Shield, Chestplate |
| **CELESTIAL_STRIKE** | Calls down lightning on critical hits | Sword, Axe |
| **VOID_PIERCE** | Arrows ignore portion of armor | Bow, Crossbow |
| **NATURES_GRASP** | Roots enemies in place on hit | Bow, Trident |
| **THUNDERCLAP** | Sprint attacks create knockback shockwaves | Boots |
| **SERPENTS_FANG** | Applies stacking poison on melee | Sword |
| **LUNAR_BLESSING** | Regenerates health during nighttime | Helmet |
| **STARFALL** | Arrows rain additional projectiles | Bow, Crossbow |
| **ANCIENT_WARD** | Reduces incoming damage by flat amount | Chestplate, Shield |
| **PHOENIX_FLAME** | Revive once per day with fire immunity | Totem, Chestplate |
| **TITANIC_FORCE** | Bonus damage based on target's max HP | Axe, Mace |
| **ECHO_STEP** | Leaves afterimages that confuse enemies | Boots |

### Holy Enchant Sources

1. **Lord Blessing**: Random Holy Enchant added when blessing Ultimate item
2. **Christmas Event**: 9 random Holy Enchants granted to 300% players on Dec 25

## Database Schema

### player_traits

Primary trait and progression state.

```sql
CREATE TABLE player_traits (
    player_uuid VARCHAR(36) PRIMARY KEY,
    primary_trait VARCHAR(50) NOT NULL,
    primary_tier VARCHAR(20) NOT NULL,
    element VARCHAR(20) NOT NULL,
    element_revealed BOOLEAN NOT NULL DEFAULT FALSE,
    element_activated BOOLEAN NOT NULL DEFAULT FALSE,
    is_postgame BOOLEAN NOT NULL DEFAULT FALSE,
    assigned_at BIGINT NOT NULL,
    last_tier_advance BIGINT
);
```

### player_temples

Temple completion tracking.

```sql
CREATE TABLE player_temples (
    player_uuid VARCHAR(36) NOT NULL,
    element VARCHAR(20) NOT NULL,
    completed_at BIGINT NOT NULL,
    PRIMARY KEY (player_uuid, element)
);
```

### player_quiz_answers

Quiz analytics (debugging/analysis).

```sql
CREATE TABLE player_quiz_answers (
    player_uuid VARCHAR(36) NOT NULL,
    question_number INT NOT NULL,
    answer TEXT NOT NULL,
    answered_at BIGINT NOT NULL,
    PRIMARY KEY (player_uuid, question_number)
);
```

### player_holy_enchants

Holy Enchant grants tracking.

```sql
CREATE TABLE player_holy_enchants (
    player_uuid VARCHAR(36) NOT NULL,
    holy_enchant VARCHAR(50) NOT NULL,
    granted_at BIGINT NOT NULL,
    source VARCHAR(20) NOT NULL,  -- 'ULTIMATE', 'LORD_BLESSING', 'CHRISTMAS'
    PRIMARY KEY (player_uuid, holy_enchant)
);
```

### player_christmas_claims

Christmas event claim tracking.

```sql
CREATE TABLE player_christmas_claims (
    player_uuid VARCHAR(36) PRIMARY KEY,
    last_claim_year INT NOT NULL,
    enchants_claimed INT NOT NULL
);
```

## Commands (Not Yet Implemented)

### /trait info [player]

Display trait, tier, element status, and completion %.

**Example output**:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Your Trait Profile
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Primary Trait: §d🔮 Mage (Master Tier)
Completion: §a52% §7(Master tier achieved)

Element: §c🔥 Fire §a(Activated)
Passive: Immune to fire and lava damage

Temples: §a1§7/5 complete
  §a✓ §cFire Temple
  §7✗ Earth Temple
  §7✗ Wind Temple
  §7✗ Water Temple
  §7✗ Aether Temple

Holy Enchants: §63 §7granted
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### /trait revealelement <player>

**Permission**: `lw.rank.lord`

Reveals player's element. Sends message to player with element info.

### /trait bless <player>

**Permission**: `lw.rank.lord`

Blesses player's held Ultimate item:
- Doubles all vanilla enchant levels
- Adds random applicable Holy Enchant
- Updates lore

### /trait temple <element>

**Permission**: `op` (admin only)

Manually mark temple as complete (for testing/fixing).

### /trait set <player> <trait>

**Permission**: `op` (admin only)

Force trait assignment (bypasses quiz).

### /trait quiz <player>

**Permission**: `op` (admin only)

Reset quiz completion flag (allows re-taking quiz).

## Integration Points

### Progression System

**ProgressionJoinListener hook** (Phase 6):
```java
// After milestone unlock
TraitService traitService = serviceRegistry.get(TraitService.class);
if (traitService != null) {
    traitService.checkAndAdvanceTier(player.getUniqueId());
}
```

This auto-advances tiers when completion % crosses 25/50/100%.

### Calendar Commands

**Extend /datejoined** (Phase 6):
```
You first joined on: 15/3/5 MC
Your Zodiac Sign: Leo ♌  |  Year Sign: Sagittarius ♐
Active Trait: §6Ultimate Mage (Tier 4/4)  |  Element: §cFire (Unlocked)
Story Completion: §a150% §7(Master tier achieved)
⭐ Post-Game God (All 5 elements active)
```

### Event Rewards

**Post-game bonus** (Phase 6):
```java
if (traitService.isPostGame(uuid)) {
    rewardMultiplier *= 1.5;  // +50% drops/XP
}
```

### BetonQuest Events

Temple completion triggers:
```yaml
# Fire temple complete
events:
  fire_temple_complete: "trait temple FIRE %player%"
```

## Implementation Status

### ✅ Phase 1: Core Data Models (Complete)
- TraitTier.java
- Element.java
- HolyEnchant.java
- PersonalityTrait.java
- PlayerTraitProfile.java
- TraitRepository.java

### ✅ Phase 2: Quiz System (Complete)
- QuizQuestion.java
- QuizSession.java
- QuizSessionManager.java (10 questions)
- QuizCompletionHandler.java
- QuizTriggerListener.java
- LobbyV2Plugin.java integration

### ✅ Phase 3: Trait Service & Progression (Complete)
- TraitService.java / TraitServiceImpl.java
- CompletionService.java (0-300% calculator)
- HolyEnchantService.java (PDC-based enchants)
- PersonalityModule.java
- RPGCorePlugin.java registration
- AchievementKey.java milestone constants

### ❌ Phase 4: Effect Listeners (Not Started)
- TraitPassiveListener.java (39 passive abilities)
- ElementalPassiveListener.java (5 elemental passives)
- HolyEnchantEffectListener.java (13 enchant effects)
- TraitItemListener.java (Ultimate item mechanics)

### ❌ Phase 5: Commands (Not Started)
- TraitCommand.java (`/trait` subcommands)
- TraitTabCompleter.java

### ❌ Phase 6: Integration & Extensions (Not Started)
- ProgressionJoinListener hook (tier advancement)
- SurvivalCalendarCommands extension (`/datejoined`)
- EventServiceImpl post-game bonus

## Testing Checklist

### Quiz Flow
- [ ] New player joins lobby → quiz auto-starts after 3 seconds
- [ ] Player frozen in place (cannot move/jump)
- [ ] All 10 questions display correctly
- [ ] Chat input captured (no broadcast)
- [ ] Quiz completes → trait + element assigned
- [ ] Database row created in `player_traits`
- [ ] Portal entry blocked before quiz, allowed after

### Trait Progression
- [ ] Player reaches 25% → tier advances to TRAIT
- [ ] Player reaches 50% → tier advances to MASTER
- [ ] Player reaches 100% → tier advances to ULTIMATE
- [ ] Ultimate item granted at 100%
- [ ] Advancement messages display (title + chat)

### Element System
- [ ] Element hidden after quiz completion
- [ ] Lord `/trait revealelement` reveals element
- [ ] Temple quest complete → element activated
- [ ] First temple activates player's element passive
- [ ] All 5 temples + 300% → post-game state

### Holy Enchants
- [ ] Lord `/trait bless` doubles enchants + adds Holy
- [ ] Lore updated with Holy Enchant name
- [ ] Christmas event grants 9 Holy Enchants to 300% players

### Database Persistence
- [ ] Quiz answers saved to `player_quiz_answers`
- [ ] Trait/tier persists across restarts
- [ ] Temple completions persist
- [ ] Holy Enchants tracked in `player_holy_enchants`

## Future Enhancements

### Alignment System
- Track Good/Evil alignment (Red Dead style Honor system)
- HEALER and ILLUSIONIST passives vary by alignment
- Reputation system integration

### Trait Respec
- `/trait respec` command (high cost)
- Resets trait but keeps element
- Requires admin approval or rare item

### Trait Synergies
- Party bonuses for complementary traits
- Example: HEALER + WARRIOR in same party → both gain +10% damage

### Custom Trait Enchant Effects
- MAGE_FOCUS: Potion projectiles have +100% AoE
- BERSERKER: Damage scales with missing health
- HOMING: Arrows curve toward nearest target
- INFINITY_LINK: Held bows gain Infinity automatically

### Elemental Combos
- Players with different elements deal bonus damage together
- Example: FIRE + WATER → Steam explosion AOE

## References

- Plan document: `C:\Users\cthvh\.claude\plans\encapsulated-floating-metcalfe.md`
- Implementation status: `Docs/implementation-status.md`
- Progression system: `PluginV2/src/main/java/com/lostwilderness/rpgcore/progression/`
- Calendar system: `PluginV2/src/main/java/com/lostwilderness/rpgcore/calendar/`
