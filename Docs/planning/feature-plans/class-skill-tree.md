---
title: Class Skill Tree
description: Five-class skill tree implementation plan and status.
tags:
  - planning
  - classes
status: implemented
phase: phase-1
owner: dev
action: none
---
# Class Skill Tree System - Implementation Plan

**Status**: ✅ IMPLEMENTED (88% complete)
**Last Updated**: 2026-03-19
**Estimated Time**: 25-30 hours
**Target Completion**: DONE (Phase 6 intentionally skipped)


## ⚠️ RISK REGISTER

| ID | Risk | Severity | Probability | Mitigation | Status |
|----|------|----------|-------------|------------|--------|
| R1 | **Performance** - High-frequency passive checks (Camouflage, Bloodlust, Master Crafter) | Medium | High | Use scheduled tasks instead of event listeners where possible; add rate limiting | Open |
| R2 | **XP Balance** - Leveling too slow/fast to reach Lv50 | High | Medium | Playtest target: 15-25 hours for Lv50; adjust XP grants iteratively | Open |
| R3 | **Ultimate Item Loss** - Player loses granted item permanently | Medium | Low | Add `/class recover` command to re-grant if DB flag exists | Open |
| R4 | **AuraSkills Dependency** - Plugin breaks or changes API | Medium | Low | Null-check all AuraSkillsBridge calls; graceful degradation if unavailable | Open |
| R5 | **Sanctified Ground Cooldown** - Lost on restart (10 min cooldown) | Low | High | Acceptable per design; document behavior in command help | Accepted |
| R6 | **Class Switching Edge Case** - Player changes class after receiving Ultimate | Low | Low | Ultimate items table supports multiple classes per player | Mitigated |
| R7 | **Level 50 Detection** - Missed if player offline when leveling | Low | Low | Check level on join AND after XP grant via AuraSkills events | Open |
| R8 | **Naming Conflict** - "Ultimate Items" overlaps with Personality system terminology | Medium | High | Rename to "Class Mastery Items" throughout code/docs | Open |
| R9 | **Existing Player Migration** - No retroactive XP for current players | Medium | High | Create migration script to backfill XP based on current stats | Open |
| R10 | **Passive Stacking** - Multiple passives conflict (e.g., Strength from 2 sources) | Low | Medium | Test all passive combinations; document expected behavior | Open |


## 📐 ARCHITECTURE DECISIONS

### Decision 1: AuraSkills Integration Pattern

**Options Considered**:
1. Listen to AuraSkills `SkillLevelUpEvent` for level 50 detection
2. Poll level after every XP grant
3. Hybrid: Listen to level-up events + fallback poll on join

**Chosen**: Option 3 (Hybrid)
**Rationale**: Most reliable; handles offline leveling and server restarts.
**Implementation**: ClassMasteryXpListener subscribes to `SkillLevelUpEvent` + ClassService.getClass() checks level on PlayerJoinEvent.

### Decision 2: Ultimate Item Duplication Prevention

**Problem**: Player could theoretically level multiple classes to 50 and receive multiple Ultimate items.
**Solution**: `class_ultimate_items` table tracks `(player_uuid, class_name)` pair. One Ultimate per class per player, but allows collection of all 5.
**Edge Case**: If player loses item, `/class recover` command checks DB flag and re-grants if previously given.

### Decision 3: Passive Performance Optimization

**Problem**: Several passives run on high-frequency events (EntityDamageByEntityEvent, PlayerMoveEvent).
**Solutions**:
- **Camouflage**: Scheduled task every 20 ticks instead of PlayerMoveEvent
- **Bloodlust**: No optimization needed (only fires on EntityDeathEvent, max ~10/sec)
- **Master Crafter**: Add 1-second cooldown per player to prevent spam crafting exploits
- **Radiant Shield**: Check blocking state only when player is damaged (already event-driven)

### Decision 4: Cross-Class XP Prevention

**Problem**: XP grants must only apply to players with the correct class.
**Implementation**: Every XP grant in ClassMasteryXpListener:
```java
classService.getClass(uuid).thenAccept(classOpt -> {
    if (classOpt.isPresent() && classOpt.get() == PlayerClass.EXPECTED) {
        skillXpService.grantXp(uuid, "mastery_key", amount);
    }
});
```
**Performance**: Class lookups cached in-memory by ClassService (no DB hit per event).


## 🧪 TESTING PLAN

### Unit Tests (per file)
- [ ] `ClassSkillTreeServiceImpl` - Unlock tree definitions, level gating logic
- [ ] `ClassMasteryXpListener` - XP grant conditions, class ownership checks
- [ ] `UltimateItemBuilder` - Item construction, PDC storage, lore formatting

### Integration Tests
- [ ] Ability upgrades apply at correct levels (Templar Smite → Divine Strike → Ascension)
- [ ] Passives only active when unlocked (test with player at Lv1, 5, 10, 15, 20, 30, 50)
- [ ] XP grants only to correct class (create 2 players with different classes, test cross-XP)
- [ ] Ultimate item grant at Lv50 (simulate level-up event, check inventory + DB)
- [ ] Sanctified Ground cooldown tracking (trigger death twice within 10 min, second should fail)

### Performance Tests
- [ ] Simulate 50 players with different classes, all using abilities simultaneously
- [ ] Measure TPS impact of Camouflage scheduled task (20 tick interval)
- [ ] Stress test Master Crafter with rapid crafting (100 crafts/sec)

### Edge Case Tests
- [ ] Player switches class after receiving Ultimate item (should allow, track separately)
- [ ] Player loses Ultimate item (test `/class recover` command)
- [ ] Server restart during Sanctified Ground cooldown (cooldown resets, documented)
- [ ] AuraSkills plugin disabled mid-session (graceful fallback, no crashes)

### User Acceptance Tests
- [ ] New player: Level from 1→5 feels achievable (1-2 hours)
- [ ] Mid-game: Level from 10→20 progression curve feels balanced
- [ ] End-game: Level 50 feels rewarding, Ultimate item is exciting
- [ ] `/class tree` output is clear and motivating


## 📚 ORIGINAL TASK CONTEXT

### System Overview

Extending the **rpgcore.classes** module in RPGCoreV2 to add a **linear class skill tree** system. Each of the 5 PlayerClasses gets a dedicated **AuraSkills custom skill** (e.g. `templar_mastery`). As players level that skill by doing class-relevant actions, they unlock class abilities in a fixed linear order — no branching.

**Current State**: All classes have one active ability at level 1 with no progression.
**Target State**: 7 unlock levels per class (1/5/10/15/20/30/50) with abilities, passives, and Ultimate items.

### Existing Classes Module (DO NOT BREAK)

**PlayerClass enum** — 5 values:
```java
CELESTIAL_TEMPLAR(Faction.CELESTIAL)
WILDLAND_RANGER(Faction.WILDLANDS)
REDEEMED_ARTIFICER(Faction.CELESTIAL)
CORRUPTED_CULTIST(Faction.CORRUPTED)
DESTROYER_BERSERKER(Faction.DESTROYERS)
```

**ClassAbilityListener** — current active abilities (right-click + sneak):
- `CELESTIAL_TEMPLAR`: Smite — lightning AoE, 8s cooldown, 30 mana, requires sword
- `REDEEMED_ARTIFICER`: Slam — knockback+slowness, 10s cooldown, 40 mana, requires pickaxe/shovel
- `CORRUPTED_CULTIST`: Dash — teleport 8 blocks forward, 6s cooldown, 25 mana, empty hand
- `DESTROYER_BERSERKER`: War Cry — Resistance II to allies + knockback to mobs, 12s cooldown, 35 mana, requires axe
- `WILDLAND_RANGER`: No active ability yet

**ClassPassiveListener** — current passives (always active, no level gate):
- `CELESTIAL_TEMPLAR`: +20% damage vs undead, -50% wither damage taken
- `WILDLAND_RANGER`: Speed I in forest/taiga/jungle/dark forest biomes
- `CORRUPTED_CULTIST`: 5% lifesteal on hit (1 heart heal)
- `DESTROYER_BERSERKER`: Strength I when health ≤ 30%
- `REDEEMED_ARTIFICER`: No passive yet

**ClassService** key methods:
```java
CompletableFuture<Optional<PlayerClass>> getClass(UUID)
CompletableFuture<Void> setClass(UUID, PlayerClass)
CompletableFuture<Boolean> tryApplyPendingStarterXp(UUID)
```

**PlayerClassRepository** tables:
- `player_classes`: player_uuid (PK), class_name, chosen_at
- `class_starter_xp_grants`: player_uuid (PK), class_name, enqueued_at, claimed_at

**AuraSkillsBridge** key methods:
```java
boolean addSkillXp(UUID playerUuid, String skillKey, double amount)
int getSkillLevel(UUID playerUuid, String skillKey)
double getMana(UUID playerUuid)
boolean consumeMana(UUID playerUuid, double amount)
```

**SkillXpService** key methods:
```java
boolean grantXp(UUID playerUuid, String skillKey, double amount)
boolean grantXpBatch(UUID playerUuid, Map<String, Double> grants)
```


## 🔧 IMPLEMENTATION RULES

### **RULE 0: ALWAYS UPDATE DOCUMENTATION** 🚨
- **After EVERY file created/modified**, immediately update:
  - Plan document progress checkboxes (✅/⬜)
  - Overall progress percentage
  - `CHANGELOG.md` with changes
  - `implementation-status.md` with feature status
- **This is NON-NEGOTIABLE** - Documentation is as important as code
- Update docs BEFORE moving to next task, not at end of phase

### Code Rules
1. **Gate all non-level-1 abilities/passives** behind `ClassSkillTreeService.isUnlocked()` checks
2. **All DB operations must be async** (CompletableFuture), following PlayerClassRepository pattern
3. **Load ClassSkillTreeService** via ServiceRegistry in ClassesModule.onLoad()
4. **Do NOT break existing behavior** — wrap and extend ClassAbilityListener/ClassPassiveListener only
5. **ClassMasteryXpListener** must check class ownership before granting XP (avoid cross-class leaking)
6. **Sanctified Ground cooldown** stored in-memory only (lost on restart — acceptable, documented)
7. **skillTreeService** injected into listeners via constructor (update ClassesModule to pass it)
8. **Mastery XP from abilities** only granted if ability actually fires (after mana/cooldown checks pass)
9. **Null-check AuraSkillsBridge** — graceful degradation if AuraSkills not loaded
10. **Performance**: Avoid high-frequency event spam; use scheduled tasks for Camouflage, rate-limit Master Crafter


## 🚀 NEXT STEPS

1. Review this updated plan for completeness
2. Begin Phase 1 implementation (data models)
3. Update progress checkboxes as tasks complete
4. Log any issues/blockers in Risk Register
5. Update `CHANGELOG.md` after each phase completion

**Ready to begin implementation?** Start with `ClassUnlock.java` record.
