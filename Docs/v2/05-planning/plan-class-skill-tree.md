# Class Skill Tree System - Implementation Plan

**Status**: ✅ IMPLEMENTED (88% complete)
**Last Updated**: 2026-03-19
**Estimated Time**: 25-30 hours
**Target Completion**: DONE (Phase 6 intentionally skipped)

---

## 📋 IMPLEMENTATION PROGRESS

### Phase 1: Core Data Models ✅ 3/3
- [x] `ClassUnlock.java` record
- [x] `ClassUnlockStatus.java` record
- [x] `ClassSkillTreeService.java` interface

### Phase 2: Service Implementation ✅ 2/2
- [x] `ClassSkillTreeServiceImpl.java` (unlock trees + Sanctified Ground tracking)
- [x] `UltimateItemBuilder.java` (item construction specs)

### Phase 3: XP System ✅ 1/1
- [x] `ClassMasteryXpListener.java` (14 event handlers)

### Phase 4: Existing Listener Integration ✅ 2/2
- [x] Modify `ClassAbilityListener.java` (ability upgrades + XP grants)
- [x] Modify `ClassPassiveListener.java` (level gates for all passives)

### Phase 5: Module Wiring ✅ 1/1
- [x] Modify `ClassesModule.java` (service registration + listener injection)

### Phase 6: Commands & UI ❌ 0/2 (SKIPPED - using AuraSkills /skills instead)
- [ ] ~~Modify `ClassCommand.java` (`/class tree` subcommand)~~
- [ ] ~~Add `ClassTreeTabCompleter.java` (tab completion for `/class tree`)~~

### Phase 7: Database ✅ 2/2
- [x] Modify `PlayerClassRepository.java` (Ultimate items table + methods) - DONE IN PHASE 2
- [x] Create migration script `V007__class_ultimate_items.sql`

### Phase 8: Config & Documentation ✅ 3/3
- [x] Generate `skills.yml` for AuraSkills (5 custom mastery skills with sources/abilities)
- [x] Update `config/core.yml` (mastery skill keys + starter XP)
- [x] Create `SKILLS_INSTALLATION.md` installation guide

**Overall Progress**: 14/16 tasks (88%) - Phases 1-5, 7-8 COMPLETE; Phase 6 SKIPPED

---

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

---

## 📝 SPECIFICATIONS & CLARIFICATIONS

### Ultimate Item Specifications

| Class | Item | Material | Base Enchants | Special Mechanics | Cooldown |
|-------|------|----------|---------------|-------------------|----------|
| **TEMPLAR** | Holy Avenger | DIAMOND_SWORD | Sharpness V, Smite X, Unbreaking III | +30% damage to undead, Smite hits 3 targets | N/A |
| **RANGER** | Ranger's Quiver | LEATHER_CHESTPLATE | Protection IV, Unbreaking III | Passively applies Infinity to held bows | N/A |
| **ARTIFICER** | Eternal Hammer | NETHERITE_PICKAXE | Efficiency V, Unbreaking X, Mending | Right-click to repair held item 50% | 5 min |
| **CULTIST** | Mirror Shard | ENDER_PEARL | N/A | Right-click player to swap positions | 5 min |
| **BERSERKER** | Ragnarok Axe | NETHERITE_AXE | Sharpness V, Unbreaking V | Melee kills refresh Speed I for 3s | N/A |

**Trait Enchant Implementation**: Store in PDC using key `lw:class_mastery_enchant` → `"HOLY_AVENGER"` etc.
**Lore Format**: `§5⚔ Class Mastery Item` (line 1), ability description (line 2-3)

### XP Scaling Curve (Levels 1-50)

Target: **15-25 hours of gameplay** for average player to reach Lv50.

| Level Range | Total XP Required | XP per Level | Estimated Time |
|-------------|-------------------|--------------|----------------|
| 1 → 5 | 500 | 100-150 | 1-2 hours |
| 5 → 10 | 2,000 | 300-500 | 3-5 hours |
| 10 → 15 | 4,500 | 700-1000 | 5-8 hours |
| 15 → 20 | 8,000 | 1200-1800 | 8-12 hours |
| 20 → 30 | 18,000 | 1500-2500 | 12-18 hours |
| 30 → 50 | 50,000 | 2000-3000 | 18-25 hours |

**Formula**: `xpRequired(level) = 50 * level^1.8` (rounded to nearest 50)

### Mana Costs for New Abilities

| Ability | Mana Cost | Cooldown | Notes |
|---------|-----------|----------|-------|
| Hunter's Mark (RANGER Lv10) | 20 | 15s | Marks single target |
| Holy Avenger Proc (TEMPLAR Lv50) | 50 | 8s | Chain lightning upgrade |
| Power Slam (ARTIFICER Lv15) | 40 | 10s | Same as base Slam |
| Shadow Step (CULTIST Lv10) | 25 | 6s | Same as base Dash |
| Brutal War Cry (BERSERKER Lv15) | 35 | 12s | Same as base War Cry |

### Camouflage Implementation Details

**Trigger**: Player stands still in forest/jungle/taiga biome for 3 consecutive seconds.
**Detection**: Scheduled task (runs every 20 ticks) checks if player location delta < 0.2 blocks.
**Break Conditions**: Movement >0.5 blocks, taking damage, using items, attacking.
**Storage**: `Map<UUID, Long> camouflageStillSince` in ClassPassiveListener.

### Phantom Arrow Mechanics

**Trigger**: 20% chance per arrow shot (random on ProjectileLaunchEvent).
**Effect**: Arrow ignores first non-solid block collision (glass, leaves, fences, iron bars).
**Implementation**: Store boolean in arrow's PDC: `lw:phantom_arrow` → `true`. On ProjectileHitEvent, if block is non-solid and PDC flag exists, cancel event and continue flight.

### Sanctified Ground Details

**Cooldown Storage**: In-memory `Map<UUID, Long> sanctifiedGroundCooldowns` in ClassSkillTreeServiceImpl.
**Trigger**: PlayerDeathEvent with TEMPLAR ≥ Lv30, cooldown not active.
**Effect**: Cancel death, set health to 1.0, apply Resistance II for 5s, send title "§6Sanctified Ground Saved You!".
**Cooldown Reset**: 10 minutes (600,000 ms). Lost on server restart (documented behavior).

### Pack Leader Stat Boosts

**Target**: All tamed wolves/cats owned by player (check `Wolf.getOwner()` or `Cat.getOwner()`).
**HP Boost**: +30% max health applied via Attribute modifier on EntityTameEvent.
**Damage Boost**: +15% damage applied via EntityDamageByEntityEvent when tameable is attacker.
**Persistence**: Attribute modifiers persist across restarts.

---

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

---

## 🔗 INTEGRATION POINTS

### With Personality System (Phases 1-6)
- **Naming**: Rename "Ultimate Items" → "Class Mastery Items" to avoid confusion with Personality Ultimate Items
- **PDC Keys**: Use separate namespace: `lw:class_mastery_enchant` vs `lw:trait_enchant`
- **Item Lore**: Different prefix: `§5⚔ Class Mastery Item` vs `§5⚡ Trait Enchant`
- **No Conflicts**: Both systems can grant items independently

### With Reputation System
- **Opportunity**: Grant +10 faction reputation when class mastery levels up (5/10/15/20/30/50)
- **Implementation**: In ClassMasteryXpListener after detecting level-up, call `ReputationService.addReputation(uuid, faction, amount)`

### With Progression System
- **Starter XP**: When player chooses class, grant 150 mastery XP via `ClassService.tryApplyPendingStarterXp()`
- **Milestone Integration**: None required (orthogonal systems)

### With Events System
- **Bonus XP**: World events (Eclipse, Blood Moon, etc.) could grant +50% mastery XP
- **Implementation**: Check `EventService.getActive()` in ClassMasteryXpListener before granting XP, multiply if relevant event active

### With Clans System
- **No Direct Integration**: Class abilities affect clan members (War Cry buff), but no cross-system data dependencies

---

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

---

## 🛠️ IMPLEMENTATION NOTES

### File Creation Order

**Phase 1**: Data models (no dependencies)
1. `ClassUnlock.java`
2. `ClassUnlockStatus.java`
3. `ClassSkillTreeService.java`

**Phase 2**: Service implementation (depends on Phase 1)
4. `ClassSkillTreeServiceImpl.java`
5. `UltimateItemBuilder.java`

**Phase 3**: XP system (depends on ClassService, SkillXpService)
6. `ClassMasteryXpListener.java`

**Phase 4**: Listener modifications (depends on ClassSkillTreeService)
7. `ClassAbilityListener.java` (modify)
8. `ClassPassiveListener.java` (modify)

**Phase 5**: Module wiring (depends on all services)
9. `ClassesModule.java` (modify)

**Phase 6**: Commands (depends on ClassSkillTreeService)
10. `ClassCommand.java` (modify)
11. `ClassTreeTabCompleter.java` (new)

**Phase 7**: Database (can be done in parallel with Phase 1-3)
12. `PlayerClassRepository.java` (modify)
13. `V00X__class_ultimate_items.sql` (new)

**Phase 8**: Config & docs (final step)
14. `skills.yml` fragment
15. `config/core.yml` updates
16. `CHANGELOG.md` + `implementation-status.md`

### Code Style Guidelines

- **Immutable records**: Use for ClassUnlock, ClassUnlockStatus
- **Null safety**: Check `Optional.isPresent()` before accessing, never assume non-null
- **Async patterns**: All DB calls return `CompletableFuture<T>`, use `.thenAccept()` chains
- **Logging**: Use `plugin.getLogger().info()` for level-ups, `.warning()` for XP grant failures
- **Magic numbers**: Extract constants (e.g., `SANCTIFIED_GROUND_COOLDOWN_MS = 600_000L`)

### Performance Budgets

| Component | Max Execution Time | Frequency | Notes |
|-----------|-------------------|-----------|-------|
| `ClassMasteryXpListener.onEntityDeath()` | <5ms | ~100/min | Critical path |
| `ClassPassiveListener.onEntityDamage()` | <2ms | ~500/min | Very high frequency |
| `ClassSkillTreeService.isUnlocked()` | <1ms | ~1000/min | Cache aggressively |
| `ClassCommand.handleTree()` | <50ms | ~10/min | User-initiated, tolerate lag |
| Camouflage scheduled task | <10ms | 1/sec | Background task |

---

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

---

## 🎯 UNLOCK TREES SPECIFICATION

### One Custom AuraSkills Skill per Class

| Class | Skill Key | Primary XP Sources |
|-------|-----------|-------------------|
| CELESTIAL_TEMPLAR | `templar_mastery` | Killing undead mobs (20 XP), using Smite (10 XP) |
| WILDLAND_RANGER | `ranger_mastery` | Killing with bow/crossbow (25 XP), taming (50 XP) |
| REDEEMED_ARTIFICER | `artificer_mastery` | Crafting items (5 XP), anvil repairs (15 XP) |
| CORRUPTED_CULTIST | `cultist_mastery` | PvP kills (40 XP), using Dash (10 XP), lifesteal procs (8 XP) |
| DESTROYER_BERSERKER | `berserker_mastery` | Axe kills (20 XP), using War Cry (10 XP), taking damage at low HP (5 XP) |

### CELESTIAL_TEMPLAR — `templar_mastery`

| Level | Unlock | Type | Description |
|-------|--------|------|-------------|
| 1 | **Smite** (existing) | Active | Lightning AoE, 8s cd, 30 mana, requires sword |
| 5 | **Holy Aura** | Passive | Regen I for 5s after killing undead mob |
| 10 | **Blessed Armor** | Passive | Resistance I while wearing full armor (4 pieces) |
| 15 | **Divine Strike** | Active Upgrade | Smite now hits up to 2 targets (closest second enemy in 6 blocks) |
| 20 | **Radiant Shield** | Passive | Blocking reflects 10% damage back to attacker |
| 30 | **Sanctified Ground** | Passive | On death: survive once with 1 heart (10 min cooldown, in-memory) |
| 50 | **Ascension** | Active Upgrade | Smite becomes chain lightning: hits 3 enemies, mana cost 50, cd 8s |

### WILDLAND_RANGER — `ranger_mastery`

| Level | Unlock | Type | Description |
|-------|--------|------|-------------|
| 1 | **Swift Feet** (existing) | Passive | Speed I in forest/jungle/taiga/dark_forest biomes |
| 5 | **Eagle Eye** | Passive | Arrow damage +15% (multiply final damage by 1.15) |
| 10 | **Hunter's Mark** | Active (NEW) | Right-click + sneak with bow: mark mob, +25% damage to marked for 10s, 15s cd, 20 mana |
| 15 | **Camouflage** | Passive | Standing still for 3s in forest biomes grants Invisibility (breaks on move >0.5 blocks) |
| 20 | **Pack Leader** | Passive | Tamed wolves/cats gain +30% HP and +15% damage (attribute modifiers) |
| 30 | **Phantom Arrow** | Passive | Arrows pass through one non-solid block 20% of the time (glass, leaves, fences) |
| 50 | **Ranger's Quiver** | Ultimate Item | Leather chestplate that passively applies Infinity to any held bow (check on item held event) |

### REDEEMED_ARTIFICER — `artificer_mastery`

| Level | Unlock | Type | Description |
|-------|--------|------|-------------|
| 1 | **Slam** (existing) | Active | Knockback + Slowness II, 10s cd, 40 mana, requires pickaxe/shovel |
| 5 | **Efficient Repairs** | Passive | Anvil repair costs -2 levels (modify PrepareAnvilEvent) |
| 10 | **Reinforced Tools** | Passive | Tools take 20% less durability damage (PlayerItemDamageEvent, check tool types) |
| 15 | **Power Slam** | Active Upgrade | Slam radius increases from 4.0 to 6.0, Slowness III instead of II |
| 20 | **Master Crafter** | Passive | 15% chance of crafting an extra item (CraftItemEvent, 1s cooldown per player) |
| 30 | **Arcane Forge** | Passive | Enchanting table gives +1 extra option, no lapis needed (EnchantItemEvent) |
| 50 | **The Eternal Hammer** | Ultimate Item | Netherite pickaxe, right-click to repair held item by 50%, 5 min cooldown |

### CORRUPTED_CULTIST — `cultist_mastery`

| Level | Unlock | Type | Description |
|-------|--------|------|-------------|
| 1 | **Dash** (existing) | Active | Teleport 8 blocks forward, 6s cd, 25 mana, empty hand |
| 5 | **Lifesteal** (existing) | Passive | 5% proc chance on melee hit, heal 1 heart |
| 10 | **Shadow Step** | Active Upgrade | Dash range increases to 12 blocks, leaves SMOKE_LARGE particle trail |
| 15 | **Venom Strike** | Passive | Melee hits have 15% chance to apply Poison I for 4s |
| 20 | **Dark Pact** | Passive | When health drops below 20%, gain Strength I + Speed I for 6s (30s per-player cooldown) |
| 30 | **Soul Drain** | Passive Upgrade | Lifesteal proc rate increases to 15%, heals 1.5 hearts |
| 50 | **Mirror Shard** | Ultimate Item | Ender Pearl, right-click player to swap positions, 5 min cooldown |

### DESTROYER_BERSERKER — `berserker_mastery`

| Level | Unlock | Type | Description |
|-------|--------|------|-------------|
| 1 | **War Cry** (existing) | Active | Resistance II to allies + mob knockback, 12s cd, 35 mana, requires axe |
| 5 | **Berserker's Rage** (existing) | Passive | Strength I when health ≤ 30% |
| 10 | **Iron Skin** | Passive | Permanent Resistance I when holding an axe (PlayerItemHeldEvent) |
| 15 | **Brutal War Cry** | Active Upgrade | War Cry now also grants Strength I to allied players for 5s |
| 20 | **Unstoppable** | Passive | Immunity to knockback from mobs (not players) - cancel EntityDamageByEntityEvent velocity |
| 30 | **Bloodlust** | Passive | Each melee kill refreshes a 3s Speed I burst (apply on EntityDeathEvent) |
| 50 | **Ragnarok Axe** | Ultimate Item | Netherite axe with Sharpness V, Unbreaking V, Bloodlust proc triggers Speed I on kill |

---

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

---

## 📄 CODE EXAMPLES

### Example: Level-gated passive in ClassPassiveListener
```java
@EventHandler
public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player p)) return;
    PlayerClass pc = cachedClass(p.getUniqueId());
    if (pc != PlayerClass.CELESTIAL_TEMPLAR) return;

    // Level 1 passive — always active after class chosen
    if (isUndead(event.getEntity())) {
        event.setDamage(event.getDamage() * 1.20);
    }

    // Level 20 passive — Radiant Shield
    if (skillTreeService.isUnlocked(p.getUniqueId(), pc, 20)) {
        if (p.isBlocking()) {
            double reflect = event.getDamage() * 0.10;
            event.getEntity().damage(reflect, p);
        }
    }
}
```

### Example: Ability upgrade in ClassAbilityListener
```java
private void handleTemplarSmite(Player player) {
    if (!consumeMana(player, 30)) return;
    setCooldown(player, 8000);
    int level = skillTreeService.getMasteryLevel(player.getUniqueId(), PlayerClass.CELESTIAL_TEMPLAR);
    int maxTargets = level >= 50 ? 3 : (level >= 15 ? 2 : 1);
    // strike up to maxTargets nearest entities
    ...
    // Grant mastery XP for using the ability
    if (skillXpService != null) {
        skillXpService.grantXp(player.getUniqueId(), "templar_mastery", 10.0);
    }
}
```

### Example: /class tree output
```java
List<ClassUnlockStatus> statuses = skillTreeService.getAllUnlocks(uuid, pc);
int currentLevel = skillTreeService.getMasteryLevel(uuid, pc);
sender.sendMessage("§6§l⚔ " + pc.name() + " Mastery §7(Level " + currentLevel + ")");
for (ClassUnlockStatus s : statuses) {
    String prefix = s.unlocked() ? "§a✔" : (currentLevel >= s.unlock().requiredLevel() - 10 ? "§c✘" : "§8✘");
    String gap = s.unlocked() ? "" : " §7(" + (s.unlock().requiredLevel() - currentLevel) + " levels away)";
    sender.sendMessage(prefix + " [Lv." + s.unlock().requiredLevel() + "] " + s.unlock().abilityName() + " — " + s.unlock().description() + gap);
}
```

---

## 🚀 NEXT STEPS

1. Review this updated plan for completeness
2. Begin Phase 1 implementation (data models)
3. Update progress checkboxes as tasks complete
4. Log any issues/blockers in Risk Register
5. Update `CHANGELOG.md` after each phase completion

**Ready to begin implementation?** Start with `ClassUnlock.java` record.
