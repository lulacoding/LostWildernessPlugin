---
title: Pet Module Plan
description: Planned pet companion module implementation.
tags:
  - planning
  - pets
status: planned
phase: phase-3
owner: dev
action: needs-dev
---
# Plan: Pet Module - Implementation Plan

**Status:** 📋 Ready for Implementation
**Created:** 2026-03-19
**Based On:** [plan-pet-module.md](plan-pet-module.md)
**Module:** `rpgcore.pets`


## Phase 1: Core Infrastructure (MVP)

### 1.1 Database Schema ✅

```sql
CREATE TABLE pet_registry (
    pet_uuid VARCHAR(36) PRIMARY KEY,
    owner_uuid VARCHAR(36) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    custom_name VARCHAR(64),
    tame_day BIGINT NOT NULL,
    tame_mc_date VARCHAR(32) NOT NULL,
    death_day BIGINT,
    death_mc_date VARCHAR(32),
    zodiac_sign VARCHAR(32) NOT NULL,
    personality VARCHAR(64) NOT NULL,
    personality_revealed BOOLEAN DEFAULT FALSE,
    collar_priority VARCHAR(16) DEFAULT 'DEFAULT',
    is_lost BOOLEAN DEFAULT FALSE,
    last_seen_location VARCHAR(128),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,

    INDEX idx_owner (owner_uuid),
    INDEX idx_owner_alive (owner_uuid, death_day),
    CONSTRAINT fk_owner FOREIGN KEY (owner_uuid) REFERENCES player_profiles(uuid)
);
```

**collar_priority values:** `CLAN`, `ALLIANCE`, `CUSTOM`, `DEFAULT`

### 1.2 Service Layer

**Files to Create:**
- `PetModule.java` - RpgModule implementation
- `PetService.java` - Interface
- `PetServiceImpl.java` - Implementation
- `PetRepository.java` - Async DB operations
- `PetProfile.java` - Data record

**Core Methods:**
```java
CompletableFuture<PetProfile> registerPet(UUID ownerUuid, Entity entity);
CompletableFuture<Void> recordDeath(UUID petUuid);
CompletableFuture<List<PetProfile>> getActivePets(UUID ownerUuid);
CompletableFuture<List<PetProfile>> getGraveyard(UUID ownerUuid);
CompletableFuture<PetProfile> getPetProfile(UUID petUuid);
CompletableFuture<Boolean> canTameMore(UUID ownerUuid); // Dynamic limit check
CompletableFuture<Void> revealPersonality(UUID petUuid, UUID revealerUuid);
CompletableFuture<Void> retamePet(UUID petUuid, UUID newOwnerUuid);
CompletableFuture<Void> reconcilePet(UUID ownerUuid, String name, Entity entity);
```

### 1.3 Data Models

**PetProfile.java:**
```java
public record PetProfile(
    UUID petUuid,
    UUID ownerUuid,
    EntityType entityType,
    String customName,
    long tameDay,
    String tameMcDate,
    Long deathDay,
    String deathMcDate,
    PetZodiacSign zodiacSign,
    PetPersonality personality,
    boolean personalityRevealed,
    CollarPriority collarPriority,
    boolean isLost,
    String lastSeenLocation,
    long createdAt,
    long updatedAt
) {}
```

**PetZodiacSign.java** - Enum (reuse existing ZodiacSign or create pet-specific)

**PetPersonality.java** - Enum (32 personalities)

**CollarPriority.java** - Enum: `DEFAULT`, `CLAN`, `ALLIANCE`, `CUSTOM`

### 1.4 Basic Listeners

**PetTameListener.java:**
- Listen: `EntityTameEvent`
- Check: `canTameMore()` (Uses the dynamic limit from the TAMER trait if active)
- Cancel if limit reached with message
- Register pet in DB with zodiac + random personality
- Apply initial collar color
- Store custom name in PDC: `lw:pet_registered`

**PetDeathListener.java:**
- Listen: `EntityDeathEvent`
- Check: Is tamed + has `lw:pet_registered` PDC
- Record death date in DB
- Send owner notification (if online)

**Progress:** ⬜ Module structure ⬜ Database ⬜ Service layer ⬜ Listeners


## Phase 3: Zodiac Effects (Passive)

### 3.1 Zodiac Effect Scheduler

**PetZodiacEffectTask.java** - Runs every **5 seconds**

**Logic:**
1. Get all online players.
2. For each player: get active pets from cache.
3. For each pet: check if entity exists + within **64 blocks** + same world.
4. **Active Limit:** Sort by distance and only apply effects to the **top 3 closest pets**.
5. Apply zodiac passive effect.
6. Check zodiac sync bonus (owner + pet same sign).

### 3.2 Zodiac Effect Implementation

**Simple Passive Effects (Phase 3):**
- Aries: Speed + Fire Resistance
- Cancer: Water Breathing
- Libra: Resistance
- Scorpio: Poison Immunity + Night Vision
- Virgo: Regeneration
- Aquarius: Lightning Resistance

**Complex Effects (Phase 4):**
- Taurus: Breeding cooldown modification
- Leo: Damage boost (requires attribute modification)
- Sagittarius: Mob detection (particles)
- Capricorn: Fall resistance + wall climbing
- Pisces: Fishing luck
- Ophiuchus: Healing aura


## Phase 5: Collar Colors & Social Integration

### 5.1 Collar Priority System

**Priority Order:**
1. **Clan color** (if player in clan)
2. **Alliance color** (if no clan, but clan has alliance)
3. **Custom dye** (if no clan/alliance, player dyed collar)
4. **Default red** (vanilla)


## Phase 7: BetonQuest Integration

- Conditions: `has_pet_with_hidden_personality`, `pet_count`
- Events: `pet_personality_reveal`
- Variables: `%pet_name%`, `%pet_personality%`, `%pet_zodiac%`


## Testing Checklist

- [ ] Taming beyond base limit works (blocked if not Tamer)
- [ ] Tamer trait correctly expands limit at each tier (5/7/12/15)
- [ ] Pet Zodiac perks apply within 32 blocks
- [ ] Guardian Angel saves owner from death
- [ ] Golden Bone retames enemy wolf during active war
