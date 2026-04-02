---
title: Zodiac Test Guide
description: Full test suite for zodiac signs, spirit animals, and special mechanics.
tags:
  - testing
  - zodiac
status: implemented
phase: phase-1
owner: dev
action: none
---
# Zodiac System Testing Guide

**Document Version**: 1.0
**Last Updated**: 2026-03-17
**Module**: `ZodiacModule`
**Status**: Production Ready


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


### Test S-004: Database Persistence

**Steps**:
1. Note zodiac sign from S-002
2. Disconnect and reconnect
3. Run `/zodiac info` again

**Expected Results**: Same zodiac information displayed (sign not re-rolled)

**Pass Criteria**: ✅ Zodiac persists across reconnects


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


### Test C-002: Sign Perks

**Objective**: Verify a sample of sign perks work correctly.

#### Test: Aries (Speed I)

**Steps**:
1. Join on Aries date (Mar 21 - Apr 19)
2. Run `/effect @s`
3. Observe movement speed

**Expected**: Speed I active, player moves 20% faster

**Pass Criteria**: ✅ Speed effect visible and functional


#### Test: Cancer (Water Breathing)

**Steps**:
1. Join on Cancer date (Jun 21 - Jul 22)
2. Run `/effect @s`
3. Go underwater

**Expected**: Water Breathing active, no drowning

**Pass Criteria**: ✅ No oxygen loss underwater


#### Test: Libra (Evil)

**Steps**:
1. `/reputation test_libra CORRUPTED 500` (negative honor)
2. Run `/zodiac info`
3. Check `/effect @s`

**Expected**:
- Perk: Strength I
- Message: "Evil-aligned Libra"

**Pass Criteria**: ✅ Evil variant active, perk switched


#### Test: Bear (Resistance I below 50% HP)

**Steps**:
1. Player assigned Bear spirit animal
2. Reveal spirit animal
3. Take damage to <50% HP
4. Check `/effect @s`

**Expected**: Resistance I activates when low health

**Pass Criteria**: ✅ Health-conditional effect works


#### `/datejoined` - Other Player

**Input**: `/datejoined test_aries`
**Expected**: `test_aries joined on Year X, Month Y, Day Z (Sign season)`
**Pass**: ✅ Displays other player's date


#### `/zodiac info` - Self

**Input**: `/zodiac info`
**Expected**: Full zodiac info panel with sign, perks, hidden fields
**Pass**: ✅ Displays own zodiac


#### `/zodiac reveal spirit` - Permission Check

**Input**: `/zodiac reveal spirit test_player` (as non-Lord)
**Expected**: `❌ Insufficient permissions. Lord rank required.`
**Pass**: ✅ Permission denied


#### `/zodiac reveal personality` - Success

**Input**: `/zodiac reveal personality test_player` (as Lord)
**Expected**: Similar reveal flow, personality field updated
**Pass**: ✅ Reveal successful


## Phase 3: Comprehensive Tests (2-3 Hours)

### Test Group A: All 13 Zodiac Signs

**Objective**: Systematically test each sign's perks.

For each sign below, perform:
1. Set calendar to sign's date range
2. Join as new player
3. Verify assigned sign matches
4. Check `/effect @s` for expected potion
5. Test perk functionality (if applicable)


#### ZOD-002: Taurus (Resistance I)

**Date Range**: Month 4, Day 20 → Month 5, Day 20
**Perk**: Resistance I (permanent)
**Test**: Take damage, confirm reduced by 20%
**Pass**: ✅ Resistance effect active


#### ZOD-004: Cancer (Water Breathing)

**Date Range**: Month 6, Day 21 → Month 7, Day 22
**Perk**: Water Breathing (permanent)
**Test**: Go underwater, confirm no drowning
**Pass**: ✅ Water breathing active


#### ZOD-006: Virgo (Haste I)

**Date Range**: Month 8, Day 23 → Month 9, Day 22
**Perk**: Haste I (permanent)
**Test**: Mine blocks, confirm 20% faster
**Pass**: ✅ Haste effect active


#### ZOD-008: Libra (Evil - Strength I)

**Date Range**: Month 9, Day 23 → Month 10, Day 22
**Perk**: Strength I (if negative honor)
**Setup**: `/reputation test CORRUPTED 500`
**Test**: Attack mobs, confirm +130% damage
**Pass**: ✅ Strength active for Evil alignment


#### ZOD-010: Scorpio (Evil - Absorption I)

**Date Range**: Month 10, Day 23 → Month 11, Day 21
**Perk**: Absorption I on kill (if negative honor)
**Setup**: Negative honor
**Test**: Kill mob, gain absorption hearts
**Pass**: ✅ Absorption on kill (Evil)


#### ZOD-012: Capricorn (Health Boost I)

**Date Range**: Month 12, Day 22 → Month 1, Day 19
**Perk**: Health Boost I (permanent)
**Test**: Max health = 12 hearts (24 HP)
**Pass**: ✅ Extra 2 hearts visible


#### ZOD-014: Pisces (Night Vision)

**Date Range**: Month 2, Day 19 → Month 3, Day 20
**Perk**: Night Vision (permanent)
**Test**: Go into dark cave, see clearly
**Pass**: ✅ Night vision active


#### ZOD-016: Ophiuchus (Evil - Wither Effect)

**Date Range**: Special assignment (80% during Nov 29-Dec 17)
**Perk**: Wither I on melee hits (if negative honor)
**Setup**: Negative honor
**Test**: Hit mob, apply Wither effect
**Pass**: ✅ Wither inflicted (Evil)


#### SPI-001: Wolf

**Effect**: Strength I at night (18000-24000 ticks)
**Mob Bonus**: +20% drops from Wolves
**Test Steps**:
1. `/time set night`
2. Check `/effect @s` → Strength I visible
3. Kill 10 wolves, count drops
4. Compare to baseline (expect ~20% more)

**Pass**: ✅ Night strength + wolf drops


#### SPI-003: Fox

**Effect**: Speed I in Forest/Taiga biomes
**Mob Bonus**: +20% drops from Foxes
**Test Steps**:
1. Go to forest biome
2. Check `/effect @s` → Speed I visible
3. Leave forest → Speed disappears
4. Kill foxes, verify drops

**Pass**: ✅ Biome speed + fox drops


#### SPI-005: Owl

**Effect**: Night Vision (permanent)
**Mob Bonus**: +20% drops from Phantoms
**Test Steps**:
1. Enter dark area, verify visibility
2. Don't sleep for 3+ nights, kill phantoms

**Pass**: ✅ Night vision + phantom drops


#### SPI-007: Hawk

**Effect**: Slow Falling (permanent)
**Mob Bonus**: +20% drops from Parrots
**Test Steps**:
1. Jump from high place, fall slowly
2. Kill parrots (rare), verify drops

**Pass**: ✅ Slow falling + parrot drops


#### SPI-009: Turtle

**Effect**: Resistance I in water
**Mob Bonus**: +20% drops from Turtles
**Test Steps**:
1. Go underwater, check `/effect @s`
2. Kill turtles, verify drops (scutes)

**Pass**: ✅ Water resistance + turtle drops


#### SPI-011: Cat

**Effect**: Saturation I (permanent - very slow hunger)
**Mob Bonus**: +20% drops from Cats
**Test Steps**:
1. Observe hunger depletion rate (very slow)
2. Kill cats, verify drops

**Pass**: ✅ Slow hunger + cat drops


#### SPI-013: Raven

**Effect**: Luck I (permanent)
**Mob Bonus**: +20% drops from Bats (closest equivalent)
**Test Steps**:
1. Break blocks, mine ores → better drops
2. Kill bats, verify drops

**Pass**: ✅ Permanent luck + bat drops


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


#### Test: Effect Refresh Rate

**Test ID**: PERF-002
**Objective**: Verify effects apply within 1 second of trigger.

**Setup**:
1. Scorpio (Evil) player kills mob
2. Measure time until Absorption I appears

**Expected**: Effect applies within 1 tick (0.05s) to 1 second

**Pass Criteria**: ✅ Instant effect application


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


#### Test: Rapid Reconnects

**Test ID**: EDGE-003
**Objective**: Verify no duplicate assignments on fast reconnects.

**Steps**:
1. Join and disconnect 10 times rapidly (within 1 second each)
2. Check database for duplicate entries

**Expected**: Only 1 profile exists per UUID

**Pass**: ✅ Upsert prevents duplicates


#### Test: Year 13 → Year 1 Rollover

**Test ID**: EDGE-005
**Objective**: Verify year sign cycles after Year 13.

**Setup**:
1. Player joins in Year 13, Month 1 → Capricorn year sign
2. Calendar advances to Year 14, Month 1
3. Check year sign calculation

**Expected**: Year 14 % 13 = Year 1 → Aries year sign

**Pass**: ✅ Year cycling works


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
