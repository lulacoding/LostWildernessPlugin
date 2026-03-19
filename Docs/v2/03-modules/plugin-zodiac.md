# Plugin: Zodiac Module

**Package:** `com.lostwilderness.rpgcore.zodiac`

## Overview

The Zodiac module implements a comprehensive astrological system that assigns players zodiac signs based on their join date (MC calendar). It provides passive perks, spirit animals, and special bonuses for alignment (Good/Evil), synchronization events, and Epochian (chosen) players.

## Core Concepts

### Zodiac Signs

Players receive two zodiac signs:
- **Month Sign**: Based on the MC calendar month/day they joined
- **Year Sign**: Based on the MC calendar year they joined

There are **13 zodiac signs** total:
1. Aries ♈ (Mar 21 - Apr 19)
2. Taurus ♉ (Apr 20 - May 20)
3. Gemini ♊ (May 21 - Jun 20)
4. Cancer ♋ (Jun 21 - Jul 22)
5. Leo ♌ (Jul 23 - Aug 22)
6. Virgo ♍ (Aug 23 - Sep 22)
7. Libra ♎ (Sep 23 - Oct 22)
8. Scorpio ♏ (Oct 23 - Nov 21)
9. Sagittarius ♐ (Nov 22 - Dec 21)
10. Capricorn ♑ (Dec 22 - Jan 19)
11. Aquarius ♒ (Jan 20 - Feb 18)
12. Pisces ♓ (Feb 19 - Mar 20)
13. Ophiuchus ⛎ (Nov 29 - Dec 17 OR Epoch Day 0)

### Ophiuchus Special Rules

- **Ophiuchus Window**: Nov 29 - Dec 17
- During this window, players assigned Sagittarius have:
  - 80% chance: Remain Sagittarius
  - 20% chance: Assigned Ophiuchus instead
- **Epoch Day 0**: Players who joined on Day 0 are ALWAYS Ophiuchus (The Chosen/Epochians)

### Epochians (The Chosen)

Players who joined on **Epoch Day 0** have special status:
- Month sign is always OPHIUCHUS
- Receive a **hidden second sign** (randomly assigned from all 13 signs)
- Both signs are always active and provide perks
- Second sign can only be revealed by a **Lord-rank** player using `/zodiac reveal`
- Future enhancement: Signs rotate every 13 MC years

### Spirit Animals

Each player is assigned a random **spirit animal** at join:
- **Hidden by default** until revealed by a Lord-rank player
- Provides passive bonuses while hidden or revealed
- 14 spirit animals total (Wolf, Fox, Bat, Dolphin, Bee, Cat, Rabbit, Parrot, Axolotl, Panda, Turtle, Ocelot, Horse, Iron Golem)
- +20% drop chance from the corresponding mob type

### Good/Evil Alignment

Some zodiac perks have **different variants** based on player alignment:
- Alignment determined by **Honor Score** from ReputationService
- Positive honor (≥0) = Good
- Negative honor (<0) = Evil
- Affects: Libra, Scorpio, Ophiuchus

### Sync Bonus

When a player's **month sign matches current month** AND **year sign matches current year**:
- All zodiac perks are **doubled** (amplifier × 2)
- Additional **Luck I** effect
- Rare occurrence (approximately every 12-13 years per player)

### Clan Leader Year Bonus

When a clan leader's **year sign matches the current year**:
- **All clan members** receive:
  - Luck I
  - +5% XP gain
- Lasts for the entire MC year
- Easter egg mechanic (happens ~once every 13 years per clan)

## Architecture

### Service Layer

**ZodiacService** (interface):
- `assignZodiac(UUID, long epochDay)` - Assign signs on first join
- `getZodiacProfile(UUID)` - Fetch cached profile
- `revealSecondSign(UUID target, UUID lord)` - Reveal Epochian 2nd sign
- `revealSpiritAnimal(UUID target, UUID lord)` - Reveal spirit animal
- `getCurrentMonthSign()` - Current MC month's zodiac
- `getCurrentYearSign()` - Current MC year's zodiac
- `isSignMonthActive(UUID)` - Check if month sign matches current
- `isSignYearActive(UUID)` - Check if year sign matches current
- `isSyncActive(UUID)` - Check if both signs match (sync bonus)
- `isClanLeaderYearBonusActive(UUID)` - Check clan-wide bonus

**ZodiacServiceImpl**:
- Caches profiles in `ConcurrentHashMap` for performance
- Async DB operations via `ZodiacRepository`
- Integrates with CalendarServiceV2, ReputationService, ClanService

### Repository Layer

**ZodiacRepository**:
- Table: `player_zodiac`
- Supports MySQL + H2
- Async operations via `CompletableFuture`

**Schema**:
```sql
CREATE TABLE IF NOT EXISTS player_zodiac (
    player_uuid VARCHAR(36) PRIMARY KEY,
    month_sign VARCHAR(32) NOT NULL,
    year_sign VARCHAR(32) NOT NULL,
    is_epochian BOOLEAN NOT NULL DEFAULT FALSE,
    second_sign VARCHAR(32),
    second_sign_revealed BOOLEAN NOT NULL DEFAULT FALSE,
    spirit_animal VARCHAR(32) NOT NULL,
    spirit_animal_revealed BOOLEAN NOT NULL DEFAULT FALSE,
    personality VARCHAR(255),
    created_at BIGINT NOT NULL
)
```

### Listener Layer

**ZodiacJoinListener**:
- Triggers zodiac assignment on first join
- Loads profile into cache on subsequent joins

**ZodiacEffectListener**:
- Periodic scheduler (every 1 second) applies zodiac perks to online players
- Event listeners for:
  - Entity death (Leo looting bonus, spirit animal drops)
  - Lightning damage (Aquarius immunity)
  - Entity targeting (Cat phantom immunity, Ocelot creeper neutrality)
  - Sneak toggle (Ophiuchus Serpent Form)

## Zodiac Perks by Sign

### Active Perks (Potion Effects)

| Sign | Good/Neutral Perks | Evil Variant |
|------|-------------------|--------------|
| **Aries** | Speed I + Fire Resistance | Speed I only (if Fire Resist already present) |
| **Taurus** | Haste I + Crop growth +20% radius | Same |
| **Gemini** | Jump Boost I + 20% Fortune drops | Same |
| **Cancer** | Dolphin's Grace in water + Shield durability 50% slower | Same |
| **Leo** | Strength I + Looting drops + Anvil cost -1 | Same |
| **Virgo** | Mending +30%, XP +20%, No lapis enchanting | Same |
| **Libra** | Better villager trades +15%, longer Hero of Village | Cheaper prices -15%, Bad Omen removal |
| **Scorpio** | Regen I AoE (10 block radius) + XP +25% | Poison Resistance + Night Vision |
| **Sagittarius** | Power I on bows/crossbows, Protection I armor | Same |
| **Capricorn** | Slow Falling + Step Assist 1.5 blocks | Same |
| **Aquarius** | Regen in rain + Lightning immunity + Speed in water | Same |
| **Pisces** | Luck I (fishing) + Dolphin's Grace passive | Same |
| **Ophiuchus** | Healing Aura (Regen I to allies every 10s) | Serpent Form (Sneak: Invisibility + Silence + +10% break speed) |

### Spirit Animal Effects

| Animal | Effect |
|--------|--------|
| **Wolf** | +5% melee damage near wolves |
| **Fox** | +10% speed at night |
| **Bat** | Regeneration I in darkness (light ≤4) |
| **Dolphin** | Enhanced Dolphin's Grace near water |
| **Bee** | Crops grow faster in 5-block radius |
| **Cat** | Phantoms never target you |
| **Rabbit** | Jump Boost I when health <30% |
| **Parrot** | Hostile mob detection (louder footsteps) |
| **Axolotl** | Regeneration after combat |
| **Panda** | Slowness immunity |
| **Turtle** | Resistance I when stationary 3+ seconds |
| **Ocelot** | Creepers remain neutral |
| **Horse** | Speed boost in Plains/Savanna biomes |
| **Iron Golem** | Increased knockback resistance |

All spirit animals also grant **+20% drop chance** from their corresponding mob type.

## Commands

### `/zodiac info [player]`
View zodiac signs for yourself or another player.
- Shows: Month sign, year sign, Epochian status
- Respects hidden status (2nd sign, spirit animal)
- Shows active bonuses (month active, year active, sync bonus)

### `/zodiac reveal <player>`
**Permission:** `lw.rank.lord`

Reveal a player's hidden zodiac information:
- Epochian second sign
- Spirit animal

Both are revealed simultaneously. Sends messages to both the Lord and target player.

### `/zodiac clanbonus`
Check if your clan's leader has the year bonus active.
- Shows leader name
- Indicates if bonus is currently active
- Lists benefits (Luck I + 5% XP)

### `/datejoined` (extended)
**Location:** `survival-plugin/SurvivalCalendarCommands`

Extended to show:
- Join date (existing)
- Month sign + symbol
- Year sign + symbol
- Epochian status + 2nd sign (hidden/revealed)
- Spirit animal (hidden/revealed)

## Dependencies

### Required Modules
- `player` - For PlayerProfile and join date tracking
- `calendar` - For MC date/time and join date lookup
- `reputation` - For Good/Evil alignment (honor score)
- `clans` - For clan leader bonus checks

### Service Integration

```java
// CalendarServiceV2
LocalDate getPlayerJoinDate(UUID);
CalendarSnapshot getCurrentSnapshot(); // Returns date, dayCount, season, mcDay

// ReputationService
CompletableFuture<Integer> getHonorScore(UUID); // -1000 to +1000

// ClanService
UUID getClanOfPlayer(UUID);
List<Map.Entry<UUID, String>> getClanMembersWithRanks(UUID clanId);
```

## Configuration

**Enable in `config/core.yml`:**
```yaml
enabled-modules:
  - zodiac
```

**Permissions:**
```yaml
lw.rank.lord:
  description: Allows revealing hidden zodiac signs and spirit animals
  default: op

lw.zodiac.use:
  description: Use /zodiac info command
  default: true
```

## Data Flow

### First Join
1. Player joins server
2. `ZodiacJoinListener.onJoin()` detects no profile
3. Gets join date from CalendarServiceV2
4. `ZodiacService.assignZodiac()` calculates:
   - Month sign from MC month/day
   - Year sign from MC year
   - Epochian check (epochDay == 0)
   - Random spirit animal
   - If Epochian: Random 2nd sign
5. Save to DB, cache in memory
6. Profile available immediately for effects

### Effect Application (Every Second)
1. `ZodiacEffectListener` scheduler ticks
2. For each online player:
   - Load profile from cache
   - Check Good/Evil alignment (ReputationService)
   - Apply month sign perks
   - If year sign active: Apply year sign perks
   - If sync active: Double perks + Luck I
   - Apply spirit animal effects
   - Apply Epochian 2nd sign perks

### Lord Reveal
1. Lord runs `/zodiac reveal <player>`
2. Permission check: `lw.rank.lord`
3. Update DB: `second_sign_revealed = true`, `spirit_animal_revealed = true`
4. Update cache
5. Notify both Lord and target player

## Future Enhancements

### Phase 2 (Not Yet Implemented)
- **Personality Quiz**: Set `personality` field via in-game quiz
- **Epochian Sign Rotation**: Auto-rotate signs every 13 MC years (156 months)
- **Zodiac Quests**: BetonQuest integration for sign-specific storylines
- **Compatibility System**: Check sign compatibility between players
- **Horoscope Messages**: Daily fortune messages based on alignments
- **Advanced Spirit Animal Mechanics**: More complex conditional effects

## Testing

### Manual Testing Checklist
1. **First Join**: Verify zodiac assignment, check DB row
2. **Effect Application**: Join with specific sign, verify passive effects
3. **Good/Evil Variants**: Change honor score, verify Libra/Scorpio/Ophiuchus variants
4. **Sync Bonus**: Use `/nextday` to advance to matching date, verify doubled effects
5. **Lord Reveal**: Test reveal command, verify messages and `/datejoined` output
6. **Clan Bonus**: Create clan, advance to matching year, verify clan-wide buff
7. **Ophiuchus Special**: Test Epoch Day 0 (always Ophiuchus), test Nov 29-Dec 17 (80/20 split)
8. **Spirit Animals**: Test each effect (Fox night speed, Cat phantom immunity, etc.)

## Performance Considerations

- **Caching**: All profiles cached in `ConcurrentHashMap` for instant access
- **Periodic Scheduler**: Runs every 1 second (20 ticks) - acceptable for potion effects
- **Async DB**: All database operations are async to prevent main thread blocking
- **Effect Refresh**: Potion effects applied with 5-second duration, refreshed every second
- **No Per-Tick Operations**: Only periodic checks (1 Hz), not per-tick (20 Hz)

## Integration Notes

### Extending for New Signs
To add a new zodiac sign:
1. Add to `ZodiacSign` enum with date range
2. Add case to `ZodiacEffectListener.applySignPerks()`
3. Update documentation

### Extending for New Spirit Animals
To add a new spirit animal:
1. Add to `SpiritAnimal` enum with EntityType
2. Add case to `ZodiacEffectListener.applySpiritAnimalEffects()`
3. Update documentation

### Event Hooks
Other modules can listen for zodiac events:
- Player profile loaded (use `ZodiacService.getZodiacProfile()`)
- Sync bonus active (use `ZodiacService.isSyncActive()`)
- Clan leader bonus active (use `ZodiacService.isClanLeaderYearBonusActive()`)

## See Also

- [Calendar Module](plugin-calendar.md) - MC date/time system
- [Reputation Module](../03-modules/plugin-reputation.md) - Honor system
- [Clans Module](plugin-clans.md) - Clan system
- [Data Model](../02-architecture/data-model.md) - Database schema
