# Zodiac System Testing Guide

**Document Version**: 1.0
**Last Updated**: 2026-03-17
**Module**: `ZodiacModule`
**Status**: Production Ready

---

## Overview

This comprehensive testing guide provides step-by-step procedures to verify all Zodiac Star Sign system functionality in RPGCoreV2. Use this guide for quality assurance, regression testing after updates, bug discovery, and training new testers/admins.

### Testing Philosophy

- **Systematic**: Cover every feature methodically
- **Reproducible**: Anyone can follow these steps
- **Documented**: Record expected vs actual results
- **Efficient**: Start with smoke tests, then go deep

### Document Structure

1. **Quick Smoke Test** (5 minutes) - Basic functionality check
2. **Core Functionality Tests** (30 minutes) - Essential features
3. **Comprehensive Tests** (2-3 hours) - Full validation
4. **Regression Checklist** (15 minutes) - Post-update verification

---

## Prerequisites

### Test Environment Setup

**Required**:
- Server running RPGCoreV2 with ZodiacModule enabled
- Op/admin permissions for test commands
- Access to database (for verification queries)
- At least 3 test player accounts

**Configuration Check**:
```yaml
# config/core.yml
enabled-modules:
  - zodiac  # Must be present
```

**Database Check**:
```sql
-- Verify table exists
SHOW TABLES LIKE 'player_zodiac';

-- Check table structure
DESCRIBE player_zodiac;
```

### Test Accounts

Create these test accounts for comprehensive testing:
- `test_new` - Fresh player, no zodiac yet
- `test_aries` - Aries sign testing
- `test_epochian` - Day 0 (Epochian) testing
- `test_evil` - Negative honor (Evil variant testing)
- `test_lord` - Lord rank (reveal permissions)

### Admin Commands Reference

```
/nextday - Advance calendar by 1 day
/resetcalendar - Reset to Day 0 (Year 1, Month 1, Day 0)
/reputation <player> <faction> <points> - Modify reputation
/clan create <name> - Create test clan
/op <player> - Grant operator permissions
/effect <player> - View active potion effects
```

---

## Phase 1: Quick Smoke Test (5 Minutes)

**Objective**: Verify basic functionality works without major errors.

### Test S-001: Module Loading

**Steps**:
1. Start server with zodiac module enabled
2. Check console for loading messages
3. Verify no errors during startup

**Expected Results**:
```
[INFO] [ZodiacModule] Enabling...
[INFO] [ZodiacModule] Registered ZodiacService
[INFO] [ZodiacModule] Module enabled successfully
```

**Pass Criteria**: ✅ No errors, module loads successfully

---

### Test S-002: First Join Assignment

**Steps**:
1. Join server as `test_new` (never joined before)
2. Wait for join sequence to complete
3. Run `/datejoined`
4. Run `/zodiac info`

**Expected Results**:
```
/datejoined
> You joined on Year 1, Month 3, Day 15 (Aries season)

/zodiac info
> ════════════════════════════════
> ZODIAC: ARIES ♈
> Spirit Animal: [HIDDEN]
> Personality: [Take Quiz]
>
> Active Perks:
> • Speed I (permanent)
> ════════════════════════════════
```

**Pass Criteria**:
- ✅ Zodiac assigned based on current calendar date
- ✅ Commands display correct information
- ✅ Spirit animal hidden until revealed

---

### Test S-003: Effect Application

**Steps**:
1. As Aries player, run `/effect @s`
2. Observe potion effect list

**Expected Results**:
```
Speed I (∞) - Applied by Zodiac System
```

**Pass Criteria**: ✅ Correct potion effect active with infinite duration

---

### Test S-004: Database Persistence

**Steps**:
1. Note zodiac sign from S-002
2. Disconnect and reconnect
3. Run `/zodiac info` again

**Expected Results**: Same zodiac information displayed (sign not re-rolled)

**Pass Criteria**: ✅ Zodiac persists across reconnects

---

### Test S-005: Command Permissions

**Steps**:
1. As non-Lord player, run `/zodiac reveal spirit @s`
2. Run `/datejoined @s`

**Expected Results**:
```
/zodiac reveal spirit @s
> ❌ Insufficient permissions. Lord rank required.

/datejoined @s
> You joined on Year 1, Month 3, Day 15 (Aries season)
```

**Pass Criteria**: ✅ Reveal blocked, info commands work

---

## Phase 2: Core Functionality Tests (30 Minutes)

### Test C-001: Zodiac Calculation Logic

**Objective**: Verify correct sign assignment based on join date.

#### Test Case: Aries Boundaries

**Steps**:
1. `/resetcalendar` (Day 0)
2. `/nextday` 79 times (Month 3, Day 19)
3. Join as `test_1` → Should be **Pisces**
4. `/nextday` once (Month 3, Day 20)
5. Join as `test_2` → Should be **Aries**

**Expected**:
- test_1 = PISCES (Feb 19 - Mar 20)
- test_2 = ARIES (Mar 21 - Apr 19)

**Pass Criteria**: ✅ Boundary dates assign correct signs

---

#### Test Case: Ophiuchus Window

**Steps**:
1. Reset to Day 0
2. Advance to Month 11, Day 28 (Nov 28)
3. Join as `test_a` → **Scorpio** (80% during window)
4. Advance to Day 29 (Nov 29)
5. Join as `test_b` → **Ophiuchus** (80%) or **Scorpio** (20%)
6. Advance to Day 17 (Dec 17)
7. Join as `test_c` → **Ophiuchus** (80%) or **Scorpio** (20%)
8. Advance to Day 18 (Dec 18)
9. Join as `test_d` → **Sagittarius** (window closed)

**Expected**:
- Nov 28 = Scorpio (100%)
- Nov 29-Dec 17 = Ophiuchus (80%) or Scorpio (20%)
- Dec 18 = Sagittarius (100%)

**Pass Criteria**: ✅ Ophiuchus window logic correct

---

### Test C-002: Sign Perks

**Objective**: Verify a sample of sign perks work correctly.

#### Test: Aries (Speed I)

**Steps**:
1. Join on Aries date (Mar 21 - Apr 19)
2. Run `/effect @s`
3. Observe movement speed

**Expected**: Speed I active, player moves 20% faster

**Pass Criteria**: ✅ Speed effect visible and functional

---

#### Test: Taurus (Resistance I)

**Steps**:
1. Join on Taurus date (Apr 20 - May 20)
2. Run `/effect @s`
3. Take damage from mob

**Expected**: Resistance I active, damage reduced by 20%

**Pass Criteria**: ✅ Resistance effect active

---

#### Test: Cancer (Water Breathing)

**Steps**:
1. Join on Cancer date (Jun 21 - Jul 22)
2. Run `/effect @s`
3. Go underwater

**Expected**: Water Breathing active, no drowning

**Pass Criteria**: ✅ No oxygen loss underwater

---

### Test C-003: Good vs Evil Variants

**Objective**: Verify alignment-based perk variants.

#### Test: Libra (Good)

**Steps**:
1. Join on Libra date (Sep 23 - Oct 22)
2. `/reputation test_libra CELESTIAL 500` (positive honor)
3. Run `/zodiac info`
4. Check `/effect @s`

**Expected**:
- Perk: Regeneration I
- Message: "Good-aligned Libra"

**Pass Criteria**: ✅ Good variant active

---

#### Test: Libra (Evil)

**Steps**:
1. `/reputation test_libra CORRUPTED 500` (negative honor)
2. Run `/zodiac info`
3. Check `/effect @s`

**Expected**:
- Perk: Strength I
- Message: "Evil-aligned Libra"

**Pass Criteria**: ✅ Evil variant active, perk switched

---

### Test C-004: Spirit Animals

**Objective**: Verify spirit animal effects apply.

#### Test: Wolf (Strength I at night)

**Steps**:
1. Player assigned Wolf spirit animal
2. `/zodiac reveal spirit test_player` (as Lord)
3. Wait for nighttime (or `/time set night`)
4. Check `/effect @s`

**Expected**: Strength I active during night (18000-24000 ticks)

**Pass Criteria**: ✅ Night-conditional effect works

---

#### Test: Bear (Resistance I below 50% HP)

**Steps**:
1. Player assigned Bear spirit animal
2. Reveal spirit animal
3. Take damage to <50% HP
4. Check `/effect @s`

**Expected**: Resistance I activates when low health

**Pass Criteria**: ✅ Health-conditional effect works

---

### Test C-005: Commands

**Objective**: Verify all commands work with correct syntax.

#### `/datejoined` - Self

**Input**: `/datejoined`
**Expected**: `You joined on Year X, Month Y, Day Z (Sign season)`
**Pass**: ✅ Displays own join date

---

#### `/datejoined` - Other Player

**Input**: `/datejoined test_aries`
**Expected**: `test_aries joined on Year X, Month Y, Day Z (Sign season)`
**Pass**: ✅ Displays other player's date

---

#### `/datejoined` - Offline Player

**Input**: `/datejoined NonexistentPlayer`
**Expected**: `❌ Player 'NonexistentPlayer' not found or has never joined.`
**Pass**: ✅ Error message for invalid player

---

#### `/zodiac info` - Self

**Input**: `/zodiac info`
**Expected**: Full zodiac info panel with sign, perks, hidden fields
**Pass**: ✅ Displays own zodiac

---

#### `/zodiac info` - Other Player

**Input**: `/zodiac info test_aries`
**Expected**: test_aries's zodiac info panel
**Pass**: ✅ Displays other player's zodiac

---

#### `/zodiac reveal spirit` - Permission Check

**Input**: `/zodiac reveal spirit test_player` (as non-Lord)
**Expected**: `❌ Insufficient permissions. Lord rank required.`
**Pass**: ✅ Permission denied

---

#### `/zodiac reveal spirit` - Success

**Input**: `/zodiac reveal spirit test_player` (as Lord)
**Expected**:
- Message to Lord: "✅ You have revealed test_player's spirit animal: WOLF"
- Message to Player: "🌟 Lord has revealed your spirit animal: WOLF 🐺"
- Spirit animal now visible in `/zodiac info`

**Pass**: ✅ Reveal successful, DB updated

---

#### `/zodiac reveal personality` - Success

**Input**: `/zodiac reveal personality test_player` (as Lord)
**Expected**: Similar reveal flow, personality field updated
**Pass**: ✅ Reveal successful

---

### Test C-006: Database Persistence

**Objective**: Verify data survives server restarts.

**Steps**:
1. Assign zodiac to `test_persist`
2. Record: sign, spirit animal (if revealed), sync status
3. Stop server
4. Check database:
   ```sql
   SELECT * FROM player_zodiac WHERE player_uuid = '<uuid>';
   ```
5. Start server
6. Join as `test_persist`
7. Run `/zodiac info`

**Expected**: All data matches pre-restart state

**Pass Criteria**: ✅ No data loss across restarts

---

## Phase 3: Comprehensive Tests (2-3 Hours)

### Test Group A: All 13 Zodiac Signs

**Objective**: Systematically test each sign's perks.

For each sign below, perform:
1. Set calendar to sign's date range
2. Join as new player
3. Verify assigned sign matches
4. Check `/effect @s` for expected potion
5. Test perk functionality (if applicable)

---

#### ZOD-001: Aries (Speed I)

**Date Range**: Month 3, Day 21 → Month 4, Day 19
**Perk**: Speed I (permanent)
**Test**: Run speed tests, confirm 20% faster movement
**Pass**: ✅ Speed effect active

---

#### ZOD-002: Taurus (Resistance I)

**Date Range**: Month 4, Day 20 → Month 5, Day 20
**Perk**: Resistance I (permanent)
**Test**: Take damage, confirm reduced by 20%
**Pass**: ✅ Resistance effect active

---

#### ZOD-003: Gemini (Luck I)

**Date Range**: Month 5, Day 21 → Month 6, Day 20
**Perk**: Luck I (permanent)
**Test**: Break blocks, confirm better drops
**Pass**: ✅ Luck effect active

---

#### ZOD-004: Cancer (Water Breathing)

**Date Range**: Month 6, Day 21 → Month 7, Day 22
**Perk**: Water Breathing (permanent)
**Test**: Go underwater, confirm no drowning
**Pass**: ✅ Water breathing active

---

#### ZOD-005: Leo (Strength I)

**Date Range**: Month 7, Day 23 → Month 8, Day 22
**Perk**: Strength I (permanent)
**Test**: Attack mobs, confirm +130% damage
**Pass**: ✅ Strength effect active

---

#### ZOD-006: Virgo (Haste I)

**Date Range**: Month 8, Day 23 → Month 9, Day 22
**Perk**: Haste I (permanent)
**Test**: Mine blocks, confirm 20% faster
**Pass**: ✅ Haste effect active

---

#### ZOD-007: Libra (Good - Regeneration I)

**Date Range**: Month 9, Day 23 → Month 10, Day 22
**Perk**: Regeneration I (if positive honor)
**Setup**: `/reputation test CELESTIAL 500`
**Test**: Wait 2.5s, confirm health regeneration
**Pass**: ✅ Regen active for Good alignment

---

#### ZOD-008: Libra (Evil - Strength I)

**Date Range**: Month 9, Day 23 → Month 10, Day 22
**Perk**: Strength I (if negative honor)
**Setup**: `/reputation test CORRUPTED 500`
**Test**: Attack mobs, confirm +130% damage
**Pass**: ✅ Strength active for Evil alignment

---

#### ZOD-009: Scorpio (Good - Invisibility)

**Date Range**: Month 10, Day 23 → Month 11, Day 21
**Perk**: Invisibility at night (if positive honor)
**Setup**: Positive honor + nighttime
**Test**: Become invisible during night
**Pass**: ✅ Invisibility active (Good, night)

---

#### ZOD-010: Scorpio (Evil - Absorption I)

**Date Range**: Month 10, Day 23 → Month 11, Day 21
**Perk**: Absorption I on kill (if negative honor)
**Setup**: Negative honor
**Test**: Kill mob, gain absorption hearts
**Pass**: ✅ Absorption on kill (Evil)

---

#### ZOD-011: Sagittarius (Jump Boost I)

**Date Range**: Month 11, Day 22 → Month 12, Day 21
**Perk**: Jump Boost I (permanent)
**Test**: Jump height increased
**Pass**: ✅ Jump boost active

---

#### ZOD-012: Capricorn (Health Boost I)

**Date Range**: Month 12, Day 22 → Month 1, Day 19
**Perk**: Health Boost I (permanent)
**Test**: Max health = 12 hearts (24 HP)
**Pass**: ✅ Extra 2 hearts visible

---

#### ZOD-013: Aquarius (Fire Resistance)

**Date Range**: Month 1, Day 20 → Month 2, Day 18
**Perk**: Fire Resistance (permanent)
**Test**: Stand in fire/lava, take no damage
**Pass**: ✅ Fire immunity active

---

#### ZOD-014: Pisces (Night Vision)

**Date Range**: Month 2, Day 19 → Month 3, Day 20
**Perk**: Night Vision (permanent)
**Test**: Go into dark cave, see clearly
**Pass**: ✅ Night vision active

---

#### ZOD-015: Ophiuchus (Good - Regeneration I)

**Date Range**: Special assignment (80% during Nov 29-Dec 17)
**Perk**: Regeneration I (if positive honor)
**Setup**: Positive honor
**Test**: Health regeneration over time
**Pass**: ✅ Regen active (Good)

---

#### ZOD-016: Ophiuchus (Evil - Wither Effect)

**Date Range**: Special assignment (80% during Nov 29-Dec 17)
**Perk**: Wither I on melee hits (if negative honor)
**Setup**: Negative honor
**Test**: Hit mob, apply Wither effect
**Pass**: ✅ Wither inflicted (Evil)

---

### Test Group B: All 14 Spirit Animals

**Objective**: Verify each spirit animal's passive effect and mob drop bonus.

For each animal below, perform:
1. Assign to test player (via random or database edit)
2. Reveal spirit animal as Lord
3. Test passive effect trigger condition
4. Kill corresponding mob, verify +20% drop chance
5. Confirm effect applies while hidden (passive only)

---

#### SPI-001: Wolf

**Effect**: Strength I at night (18000-24000 ticks)
**Mob Bonus**: +20% drops from Wolves
**Test Steps**:
1. `/time set night`
2. Check `/effect @s` → Strength I visible
3. Kill 10 wolves, count drops
4. Compare to baseline (expect ~20% more)

**Pass**: ✅ Night strength + wolf drops

---

#### SPI-002: Bear

**Effect**: Resistance I when HP < 50%
**Mob Bonus**: +20% drops from Polar Bears
**Test Steps**:
1. Take damage to low health
2. Check `/effect @s` → Resistance I visible
3. Kill polar bears, verify drop bonus

**Pass**: ✅ Low HP resistance + bear drops

---

#### SPI-003: Fox

**Effect**: Speed I in Forest/Taiga biomes
**Mob Bonus**: +20% drops from Foxes
**Test Steps**:
1. Go to forest biome
2. Check `/effect @s` → Speed I visible
3. Leave forest → Speed disappears
4. Kill foxes, verify drops

**Pass**: ✅ Biome speed + fox drops

---

#### SPI-004: Rabbit

**Effect**: Jump Boost II (permanent)
**Mob Bonus**: +20% drops from Rabbits
**Test Steps**:
1. Check jump height (should be very high)
2. Kill rabbits, verify drops

**Pass**: ✅ Permanent jump boost + rabbit drops

---

#### SPI-005: Owl

**Effect**: Night Vision (permanent)
**Mob Bonus**: +20% drops from Phantoms
**Test Steps**:
1. Enter dark area, verify visibility
2. Don't sleep for 3+ nights, kill phantoms

**Pass**: ✅ Night vision + phantom drops

---

#### SPI-006: Deer

**Effect**: Speed I in Plains biomes
**Mob Bonus**: +20% drops from Llamas (closest equivalent)
**Test Steps**:
1. Go to plains biome
2. Check `/effect @s` → Speed I visible
3. Kill llamas, verify drops

**Pass**: ✅ Plains speed + llama drops

---

#### SPI-007: Hawk

**Effect**: Slow Falling (permanent)
**Mob Bonus**: +20% drops from Parrots
**Test Steps**:
1. Jump from high place, fall slowly
2. Kill parrots (rare), verify drops

**Pass**: ✅ Slow falling + parrot drops

---

#### SPI-008: Snake

**Effect**: Poison Resistance (permanent)
**Mob Bonus**: +20% drops from Cave Spiders
**Test Steps**:
1. Get hit by cave spider, no poison effect
2. Kill cave spiders, verify drops

**Pass**: ✅ Poison immunity + spider drops

---

#### SPI-009: Turtle

**Effect**: Resistance I in water
**Mob Bonus**: +20% drops from Turtles
**Test Steps**:
1. Go underwater, check `/effect @s`
2. Kill turtles, verify drops (scutes)

**Pass**: ✅ Water resistance + turtle drops

---

#### SPI-010: Dolphin

**Effect**: Dolphin's Grace (permanent)
**Mob Bonus**: +20% drops from Dolphins
**Test Steps**:
1. Swim underwater, verify fast swimming
2. Kill dolphins (if necessary), verify drops

**Pass**: ✅ Swimming speed + dolphin drops

---

#### SPI-011: Cat

**Effect**: Saturation I (permanent - very slow hunger)
**Mob Bonus**: +20% drops from Cats
**Test Steps**:
1. Observe hunger depletion rate (very slow)
2. Kill cats, verify drops

**Pass**: ✅ Slow hunger + cat drops

---

#### SPI-012: Horse

**Effect**: Speed I (permanent)
**Mob Bonus**: +20% drops from Horses
**Test Steps**:
1. Observe movement speed increase
2. Kill horses, verify drops

**Pass**: ✅ Permanent speed + horse drops

---

#### SPI-013: Raven

**Effect**: Luck I (permanent)
**Mob Bonus**: +20% drops from Bats (closest equivalent)
**Test Steps**:
1. Break blocks, mine ores → better drops
2. Kill bats, verify drops

**Pass**: ✅ Permanent luck + bat drops

---

#### SPI-014: Dragon

**Effect**: Fire Resistance (permanent)
**Mob Bonus**: +20% drops from Endermen (mystic equivalent)
**Test Steps**:
1. Stand in fire/lava, take no damage
2. Kill endermen, verify drops (pearls)

**Pass**: ✅ Fire immunity + endermen drops

---

### Test Group C: Special Mechanics

#### Test: Epochian (Day 0) Assignment

**Test ID**: EPOCH-001
**Objective**: Verify Day 0 players become Epochians with two signs.

**Setup**:
1. `/resetcalendar` (Day 0)
2. Join as `test_epochian`

**Expected**:
- Month sign = OPHIUCHUS
- Year sign = Random (1-13)
- Both signs active simultaneously
- Spirit animal hidden
- Second sign hidden until revealed

**Verification**:
```sql
SELECT * FROM player_zodiac WHERE player_uuid = '<uuid>';
-- is_epochian = true
-- second_sign != null
-- second_sign_revealed = false
```

**Pass Criteria**:
- ✅ OPHIUCHUS assigned as primary
- ✅ Second sign assigned randomly
- ✅ Both perks active
- ✅ Hidden until Lord reveals

---

#### Test: Ophiuchus 80/20 Split

**Test ID**: OPH-001
**Objective**: Verify Ophiuchus has 80% chance during window.

**Setup**:
1. Reset calendar
2. Advance to Nov 29 (start of window)
3. Create 100 test accounts (or script DB inserts)
4. Join all during Nov 29-Dec 17 window

**Expected**: ~80 Ophiuchus, ~20 Scorpio (allow ±10% variance)

**Statistical Verification**:
```sql
SELECT
  month_sign,
  COUNT(*) as count,
  (COUNT(*) * 100.0 / (SELECT COUNT(*) FROM player_zodiac)) as percentage
FROM player_zodiac
WHERE join_month = 11 AND join_day BETWEEN 29 AND 31
   OR join_month = 12 AND join_day BETWEEN 1 AND 17
GROUP BY month_sign;
```

**Pass Criteria**: ✅ Ophiuchus ~80%, Scorpio ~20%

---

#### Test: Sync Bonus

**Test ID**: SYNC-001
**Objective**: Verify sync bonus doubles perks and adds Luck I.

**Setup**:
1. Player joins in Month 3 (Aries month)
2. Player is Year 1 (Aries year)
3. Calendar is Month 3 (current month matches)
4. Verify sync status

**Expected**:
- Month sign = Aries (Speed I)
- Year sign = Aries
- **Sync bonus**: Speed II + Luck I (doubled effect)

**Verification**:
```bash
/effect @s
# Should show:
# Speed II (∞) - Zodiac Sync Bonus
# Luck I (∞) - Zodiac Sync Bonus
```

**Pass Criteria**:
- ✅ Sync detected (month == year sign)
- ✅ Perks doubled (Speed I → Speed II)
- ✅ Luck I added
- ✅ "✨ SYNC BONUS" displayed in `/zodiac info`

---

#### Test: Clan Leader Bonus

**Test ID**: CLAN-001
**Objective**: Verify clan leader's year sign buffs all members.

**Setup**:
1. Create clan: `/clan create TestClan`
2. Leader = Aries year sign
3. Invite 3 members (different year signs)
4. Advance calendar to Month 3 (Aries month)

**Expected**:
- Leader gets personal perks
- **All 3 clan members get Speed I** (leader's year bonus)
- Members see "Clan Year Bonus" in `/zodiac info`

**Verification**:
```bash
# As each clan member:
/effect @s
# Should show: Speed I (∞) - Clan Year Bonus (from Leader)
```

**Pass Criteria**:
- ✅ Leader's year sign detected
- ✅ Clan-wide buff applied to all members
- ✅ Members see bonus in info panel
- ✅ Bonus updates if leader changes

---

### Test Group D: Integration Tests

#### Test: Calendar Integration

**Test ID**: INT-CAL-001
**Objective**: Verify zodiac syncs with calendar date changes.

**Steps**:
1. Player joins in Month 3 (Aries)
2. Calendar advances to Month 4 (Taurus)
3. Check if year sign bonus updates

**Expected**: Month sign stays Aries (frozen), but year bonus follows current calendar month

**Pass**: ✅ Calendar integration works

---

#### Test: Reputation Integration

**Test ID**: INT-REP-001
**Objective**: Verify Good/Evil variants switch dynamically.

**Steps**:
1. Libra player with positive honor → Regeneration I
2. Change to negative honor: `/reputation test CORRUPTED 500`
3. Check effects update within 1 second

**Expected**: Regeneration I removed, Strength I applied

**Pass**: ✅ Real-time alignment switching

---

#### Test: Clan Integration

**Test ID**: INT-CLAN-001
**Objective**: Verify clan system recognizes leader changes.

**Steps**:
1. Clan leader = Aries year sign
2. Members get Speed I
3. Transfer leadership to Taurus player
4. Members should get Resistance I instead

**Expected**: Clan bonus updates to new leader's sign

**Pass**: ✅ Leadership change updates bonus

---

### Test Group E: Performance Tests

#### Test: Concurrent Load

**Test ID**: PERF-001
**Objective**: Verify no lag with multiple players online.

**Setup**:
1. 10+ players online simultaneously
2. All have different zodiac signs
3. Observe TPS for 5 minutes

**Expected**: TPS stays above 19.5, no lag spikes

**Monitoring**:
```bash
/tps  # Check server TPS
/timings report  # Check plugin impact
```

**Pass Criteria**: ✅ No performance degradation

---

#### Test: Effect Refresh Rate

**Test ID**: PERF-002
**Objective**: Verify effects apply within 1 second of trigger.

**Setup**:
1. Scorpio (Evil) player kills mob
2. Measure time until Absorption I appears

**Expected**: Effect applies within 1 tick (0.05s) to 1 second

**Pass Criteria**: ✅ Instant effect application

---

#### Test: Cache Hit Rate

**Test ID**: PERF-003
**Objective**: Verify DB queries minimized via caching.

**Setup**:
1. Enable query logging in MySQL
2. Player joins (1 DB load expected)
3. Player plays for 10 minutes
4. Check query count

**Expected**: Only 1-2 queries total (load + potential save)

**Pass Criteria**: ✅ No repeated queries per tick

---

### Test Group F: Edge Cases

#### Test: Boundary Dates

**Test ID**: EDGE-001
**Objective**: Verify sign transitions at exact boundary moments.

**Critical Dates**:
- Mar 20 23:59 → Pisces
- Mar 21 00:00 → Aries
- Nov 28 23:59 → Scorpio
- Nov 29 00:00 → Ophiuchus/Scorpio (80/20)

**Test**: Join at exact boundary, verify correct assignment

**Pass**: ✅ No off-by-one errors

---

#### Test: Null/Missing Data

**Test ID**: EDGE-002
**Objective**: Verify graceful handling of corrupted data.

**Scenarios**:
1. Player UUID is null → Skip gracefully
2. Join date invalid → Default to current date
3. Database connection lost → Cached data used
4. Profile not found → Create new profile

**Pass**: ✅ No crashes, errors logged

---

#### Test: Rapid Reconnects

**Test ID**: EDGE-003
**Objective**: Verify no duplicate assignments on fast reconnects.

**Steps**:
1. Join and disconnect 10 times rapidly (within 1 second each)
2. Check database for duplicate entries

**Expected**: Only 1 profile exists per UUID

**Pass**: ✅ Upsert prevents duplicates

---

#### Test: Honor Threshold Edge

**Test ID**: EDGE-004
**Objective**: Verify honor = 0 is handled correctly.

**Setup**:
1. Libra player with honor = 0 (exactly neutral)
2. Check which variant applies

**Expected**: Should default to Good variant (honor >= 0)

**Pass**: ✅ Zero honor defaults correctly

---

#### Test: Year 13 → Year 1 Rollover

**Test ID**: EDGE-005
**Objective**: Verify year sign cycles after Year 13.

**Setup**:
1. Player joins in Year 13, Month 1 → Capricorn year sign
2. Calendar advances to Year 14, Month 1
3. Check year sign calculation

**Expected**: Year 14 % 13 = Year 1 → Aries year sign

**Pass**: ✅ Year cycling works

---

## Phase 4: Regression Testing (15 Minutes)

**Use this checklist after ANY update to verify existing functionality still works.**

### Regression Checklist

- [ ] **Module loads** without errors
- [ ] **First join** assigns zodiac correctly
- [ ] **3 random signs** tested - perks active
- [ ] **1 Good/Evil variant** tested - switches correctly
- [ ] **1 spirit animal** tested - effect applies
- [ ] **Epochian** tested - 2 signs active (if Day 0 reachable)
- [ ] **Sync bonus** tested - doubled perks + Luck I
- [ ] **Clan leader bonus** tested - clan-wide buff applies
- [ ] **All commands** tested - no errors
- [ ] **Reveal system** tested - Lord can reveal, DB updates
- [ ] **Database persistence** tested - reconnect preserves data
- [ ] **Performance** checked - no TPS drops
- [ ] **Integration** checked - calendar/reputation/clans work

**Time Estimate**: 15 minutes
**Frequency**: After every code change to ZodiacModule

---

## Database Verification Queries

### Check All Profiles

```sql
SELECT
  player_uuid,
  month_sign,
  year_sign,
  is_epochian,
  second_sign,
  spirit_animal,
  second_sign_revealed,
  spirit_animal_revealed,
  join_year,
  join_month,
  join_day
FROM player_zodiac
ORDER BY join_year, join_month, join_day;
```

### Count Signs Distribution

```sql
SELECT
  month_sign,
  COUNT(*) as count
FROM player_zodiac
GROUP BY month_sign
ORDER BY count DESC;
```

### Find Epochians

```sql
SELECT * FROM player_zodiac WHERE is_epochian = true;
```

### Find Revealed Players

```sql
SELECT
  player_uuid,
  spirit_animal,
  spirit_animal_revealed
FROM player_zodiac
WHERE spirit_animal_revealed = true;
```

### Sync Candidates

```sql
SELECT * FROM player_zodiac WHERE month_sign = year_sign;
```

---

## Known Issues & Limitations

### Current Limitations

1. **Personality Quiz**: Placeholder - quiz system not implemented yet
2. **Epochian Rotation**: Second sign doesn't rotate over time (future feature)
3. **Visual Effects**: No particle effects for perks (planned)
4. **Sound Effects**: No audio cues for sync/clan bonuses (planned)

### Known Bugs

- None currently documented

---

## Test Data Cleanup

After testing, clean up test data:

```sql
-- Remove all test accounts
DELETE FROM player_zodiac WHERE player_uuid IN (
  SELECT uuid FROM players WHERE name LIKE 'test_%'
);

-- Or reset entire table (BE CAREFUL IN PRODUCTION)
TRUNCATE TABLE player_zodiac;
```

---

## Appendix: Quick Reference

### Sign Date Ranges

| Sign | Start | End | Perk |
|------|-------|-----|------|
| Capricorn | Dec 22 | Jan 19 | Health Boost I |
| Aquarius | Jan 20 | Feb 18 | Fire Resistance |
| Pisces | Feb 19 | Mar 20 | Night Vision |
| Aries | Mar 21 | Apr 19 | Speed I |
| Taurus | Apr 20 | May 20 | Resistance I |
| Gemini | May 21 | Jun 20 | Luck I |
| Cancer | Jun 21 | Jul 22 | Water Breathing |
| Leo | Jul 23 | Aug 22 | Strength I |
| Virgo | Aug 23 | Sep 22 | Haste I |
| Libra | Sep 23 | Oct 22 | Regen I (Good) / Str I (Evil) |
| Scorpio | Oct 23 | Nov 21 | Invis (Good) / Absorb (Evil) |
| **Ophiuchus** | **Nov 29** | **Dec 17** | Regen I (Good) / Wither (Evil) |
| Sagittarius | Nov 22 | Dec 21 | Jump Boost I |

### Spirit Animals

| Animal | Effect | Condition |
|--------|--------|-----------|
| Wolf | Strength I | Night |
| Bear | Resistance I | HP < 50% |
| Fox | Speed I | Forest/Taiga |
| Rabbit | Jump Boost II | Permanent |
| Owl | Night Vision | Permanent |
| Deer | Speed I | Plains |
| Hawk | Slow Falling | Permanent |
| Snake | Poison Resistance | Permanent |
| Turtle | Resistance I | In water |
| Dolphin | Dolphin's Grace | Permanent |
| Cat | Saturation I | Permanent |
| Horse | Speed I | Permanent |
| Raven | Luck I | Permanent |
| Dragon | Fire Resistance | Permanent |

### Command Syntax

```
/datejoined [player]
/zodiac info [player]
/zodiac reveal spirit <player>
/zodiac reveal personality <player>
```

---

## Test Report Template

Use this template to document test results:

```
Test Date: YYYY-MM-DD
Tester: [Name]
Server Version: [Paper 1.21.1]
Plugin Version: RPGCoreV2 [commit hash]

Phase 1: Smoke Test
[ ] S-001: Module Loading - PASS/FAIL
[ ] S-002: First Join Assignment - PASS/FAIL
[ ] S-003: Effect Application - PASS/FAIL
[ ] S-004: Database Persistence - PASS/FAIL
[ ] S-005: Command Permissions - PASS/FAIL

Phase 2: Core Functionality
[...continue for all test cases...]

Issues Found:
1. [Issue description, test ID, severity]
2. [...]

Overall Result: PASS / FAIL
Notes: [Any additional observations]
```

---

## Support & Reporting

**Bug Reports**: Create issue with test ID and steps to reproduce
**Test Failures**: Include server logs, database dump, and test report
**Questions**: Refer to `../03-modules/plugin-zodiac.md` for implementation details

**Document Maintainer**: AI Assistant
**Review Frequency**: After each zodiac module update
