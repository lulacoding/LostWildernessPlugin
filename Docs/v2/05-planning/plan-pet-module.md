# Plan: Pet Module

**Status:** 📋 Planned  
**Created:** 2026-03-19  
**Source:** #pets channel ideas (Grovyle187)  
**Module:** `rpgcore.pets`

---

## Overview

A comprehensive pet system for all tamed animals (dogs, cats, horses, parrots, foxes, llamas, etc.). Pets are tracked in the database with birth/tame dates, death dates, and persistent data. Each pet has a **Zodiac sign** based on tame date and a hidden **Personality** revealed by the Lord or Redeemer NPC.

---

## Core Features

### 1. Pet Tracking & Database

Every tamed animal is registered in the DB when tamed.

**Tracked data:**
- Pet UUID, owner UUID
- Species (EntityType)
- Name
- Tame date (MC calendar day)
- Death date (MC calendar day, nullable)
- Zodiac sign (derived from tame month)
- Personality (hidden, revealed flag)
- Collar color data

**Tables:**
```sql
CREATE TABLE pet_registry (
    pet_uuid VARCHAR(36) PRIMARY KEY,
    owner_uuid VARCHAR(36) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    name VARCHAR(64),
    tame_day BIGINT NOT NULL,
    tame_mc_date VARCHAR(32) NOT NULL,
    death_day BIGINT,
    death_mc_date VARCHAR(32),
    zodiac_sign VARCHAR(32) NOT NULL,
    personality VARCHAR(64),
    personality_revealed BOOLEAN DEFAULT FALSE,
    collar_custom_color VARCHAR(16),
    created_at BIGINT NOT NULL
);
```

**Commands:**
- `/pets list` — lists all living pets with name, species, zodiac, tame date
- `/pets graveyard` — lists all dead pets with birth/death dates
- `/pets info <name>` — detailed pet info (zodiac, personality if revealed)

---

### 2. Pet Zodiac System

Pets receive a **month zodiac sign** based on the MC calendar month/day they were tamed (same date ranges as player zodiac, month sign only — no year sign).

**Zodiac perks apply to the pet passively while tamed and nearby:**

| Sign | Pet Effect |
|------|-----------|
| **Aries** | +30% sprint speed + fire resistance |
| **Taurus** | Breeding cooldown -50%, auto-graze growth |
| **Gemini** | Can traverse 1.5 block heights, faster following |
| **Cancer** | +40% underwater breathing, stronger protection |
| **Leo** | +25% damage, rare loot drops from kills |
| **Virgo** | Auto-heal over time, faster XP sharing |
| **Libra** | +20% health + damage resistance |
| **Scorpio** | Poison immunity + night vision |
| **Sagittarius** | +35% mob detection range |
| **Capricorn** | +30% fall resistance + wall-climbing |
| **Aquarius** | Rain breathing + lightning resistance |
| **Pisces** | +40% fishing luck (pet helps catch) |
| **Ophiuchus** | Healing aura for nearby pets |

**Zodiac Sync Bonus:**
If the pet's zodiac sign matches the owner's **current active month sign**, both receive a synergy buff (+15% all stats for the duration of the matching month).

---

### 3. Pet Personalities (32 Hidden)

Each pet is secretly assigned one of 32 personalities on tame. Hidden until revealed by:
- **The Lord NPC** — via dialogue at Lord's Plateau
- **The Redeemer Wandering Trader** — if the Easter tradition is completed

| # | Personality | Effect |
|---|-------------|--------|
| 1 | **Loyal Sentinel** | Barks/howls to warn of nearby hostile mobs |
| 2 | **Shadow Stalker** | Turns invisible in low light for 10s |
| 3 | **Speed Demon** | Sudden 5-second sprint burst |
| 4 | **Treasure Sniffer** | Occasionally digs up buried loot |
| 5 | **Healing Touch** | Slowly heals nearby tamed pets |
| 6 | **Fisher King** | Boosts fishing luck when sitting nearby |
| 7 | **Crop Whisperer** | Speeds up nearby crops by 30% |
| 8 | **Ore Seeker** | Points toward closest ore |
| 9 | **Melody Maker** | Plays random note-block tunes |
| 10 | **Fire Dancer** | Gains fire resistance near fire |
| 11 | **Deep Diver** | +50% underwater breath for owner |
| 12 | **Wall Walker** | Climbs vertical blocks |
| 13 | **Blink** | Short teleport back to owner when too far |
| 14 | **Lucky Charm** | +20% rare drop chance from kills |
| 15 | **Mob Scout** | Detects hidden mobs, visual signal to owner |
| 16 | **Food Forager** | Brings random food items to owner |
| 17 | **Block Buddy** | Picks up and returns loose blocks |
| 18 | **Dance Master** | Performs particle dances on command |
| 19 | **Calm Whisperer** | Calms aggressive mobs within 8 blocks |
| 20 | **Light Beacon** | Glows softly to light up dark areas |
| 21 | **Echo Voice** | Repeats player emotes |
| 22 | **Hoarder** | Stores 1–3 small items in "inventory" |
| 23 | **Arrow Shield** | Blocks one arrow per day for owner |
| 24 | **Mini Explorer** | Fills small map areas automatically |
| 25 | **Dawn Herald** | Notifies owner at sunrise |
| 26 | **Dream Weaver** | Grants random positive effect on sleep |
| 27 | **Storm Rider** | Gains speed/joy in rain |
| 28 | **Star Gazer** | +15% stats under clear night sky |
| 29 | **Chaos Spark** | Prevents one creeper detonation |
| 30 | **Eternal Youth** | Prevents one pet death (pet totem) |
| 31 | **Mimic Master** | Copies owner movements (cosmetic) |
| 32 | **Guardian Angel** | Sacrifices itself to save owner (Totem of Undying effect) |

---

### 4. Clan & Alliance Collar Colors

Dog collars reflect social identity:

- **No clan/alliance:** Default red collar (vanilla)
- **In a clan (no alliance):** Full clan color collar
- **In a clan + alliance:** Half clan color, half alliance color
- **Custom dye applied:** Custom color = 1/3 of collar, rest = clan/alliance colors

Collar color logic:
- Applied via `EntityTameEvent` and updated on clan/alliance changes
- Uses Wolf's `CollarColor` attribute mapped to closest DyeColor
- PDC stores the custom dye choice separately

---

### 5. Golden Taming Items

New craftable items to enhance taming:

| Item | Recipe | Effect |
|------|--------|--------|
| **Golden Bone** | Gold Nuggets + Bone | Tame a wolf with 100% chance OR retame an enemy's wolf during war |
| **Golden Seeds** | Gold Nuggets + Seeds | Tame horses/parrots/chickens guaranteed |
| **Golden Fish** | Gold Nuggets + Raw Cod | Tame cats guaranteed OR retame enemy's cat |

**Retaming mechanics:**
- During an active clan war, golden items can be used on an **enemy's pet** to retame it
- Original owner receives a notification: "Your pet [name] was retamed by [player]!"
- DB updates owner_uuid, retame event logged

---

## Architecture

### Service Layer

**PetService** (interface):
- `registerPet(UUID ownerUuid, Entity pet)` — on tame
- `recordDeath(UUID petUuid)` — on pet death
- `getPets(UUID ownerUuid)` — all living pets
- `getGraveyard(UUID ownerUuid)` — all dead pets
- `getPetProfile(UUID petUuid)` — full pet data
- `revealPersonality(UUID petUuid, UUID revealerUuid)` — Lord NPC or Redeemer NPC only
- `retamePet(UUID petUuid, UUID newOwnerUuid)` — golden item retame

**PetRepository:**
- Async CRUD for `pet_registry` table
- H2/MySQL support

### Listener Layer

- `PetTameListener` — registers pet on tame, assigns zodiac + personality
- `PetDeathListener` — records death in DB
- `PetZodiacEffectListener` — applies zodiac perks (periodic scheduler)
- `PetPersonalityListener` — applies personality effects (event-driven)
- `PetCollarListener` — updates collar on clan/alliance change
- `PetRetameListener` — handles golden item retaming

### Commands

- `/pets list` — show all living pets
- `/pets graveyard` — show dead pets
- `/pets info <name>` — pet details
- `/pets rename <old> <new>` — rename a pet

---

## Integration Points

| System | Integration |
|--------|------------|
| **Zodiac** | Reuse ZodiacSign enum + date calculation logic |
| **Calendar** | `CalendarServiceV2.getCurrentSnapshot()` for tame date |
| **Clans** | `ClanService` for collar color updates on clan join/leave |
| **War** | `WarService.isAtWar()` to gate golden taming retame |
| **BetonQuest** | Two NPCs trigger `pet_personality_reveal` event:<br>• **The Lord** — available year-round at Lord's Plateau<br>• **The Redeemer** — Citizens NPC wandering trader, spawns only during Easter period (Palm Sunday → Easter Sunday); uses BetonQuest date condition to gate appearance |

---

## Files to Create

### New Files
- `PetModule.java` — RpgModule wiring
- `PetService.java` — interface
- `PetServiceImpl.java` — implementation
- `PetRepository.java` — DB CRUD
- `PetProfile.java` — data record
- `PetZodiacSign.java` — enum (or reuse ZodiacSign)
- `PetPersonality.java` — enum (32 personalities)
- `PetTameListener.java`
- `PetDeathListener.java`
- `PetZodiacEffectListener.java`
- `PetPersonalityListener.java`
- `PetCollarListener.java`
- `PetRetameListener.java`
- `PetCommand.java`
- `GoldenTamingItems.java` — item definitions + recipe registration
- `config/pets.yml`

### DB Migration
- `V00X__pet_registry.sql`

---

## Config (`config/pets.yml`)

```yaml
pets:
  enabled: true
  max-pets-per-player: 20
  graveyard-keep-days: 365  # MC days
  
  zodiac:
    enabled: true
    sync-bonus-multiplier: 1.15
  
  personality:
    enabled: true
    # reveal-command: only via NPC (Lord/Redeemer)
  
  collar:
    clan-colors-enabled: true
  
  golden-taming:
    enabled: true
    retame-requires-war: true
    
  golden-bone:
    recipe:
      - "GGG"
      - "GBG"
      - "GGG"
    # G = GOLD_NUGGET, B = BONE
  
  golden-seeds:
    recipe:
      - "GGG"
      - "GSG"
      - "GGG"
    # G = GOLD_NUGGET, S = WHEAT_SEEDS
  
  golden-fish:
    recipe:
      - "GGG"
      - "GFG"
      - "GGG"
    # G = GOLD_NUGGET, F = COD
```

---

## Testing Checklist

- [ ] Taming a wolf registers it in DB with correct zodiac
- [ ] `/pets list` shows all living pets
- [ ] `/pets graveyard` shows pets that have died
- [ ] Zodiac perks apply correctly (e.g. Leo wolf does extra damage)
- [ ] Zodiac sync bonus activates when owner and pet share sign
- [ ] Personality assigned on tame (verify DB, hidden initially)
- [ ] Lord NPC reveal updates `personality_revealed = true`
- [ ] Collar updates when player joins/leaves clan
- [ ] Collar updates when clan joins/leaves alliance
- [ ] Custom dye persists correctly in PDC
- [ ] Golden Bone can retame enemy wolf during war
- [ ] Retame notification sent to original owner
- [ ] Guardian Angel personality triggers correctly (saves owner once)
- [ ] Eternal Youth personality prevents one pet death

---

## Future Enhancements

- **Pet levels** — pets gain XP from kills, level up to unlock better zodiac perks
- **Pet naming ceremony** — special item/ritual to name a pet, name persists in DB
- **Pet inventory** (Hoarder personality) — small GUI for items pet has collected
- **Cross-server pet tracking** — pets registered across Survival + Amplified
- **Pet breeding records** — track lineage, inherited traits from parents

---

## Related Docs

- [plugin-zodiac.md](../03-modules/plugin-zodiac.md)
- [plan-zodiac-npc-reveal.md](plan-zodiac-npc-reveal.md)
- [plugin-clans.md](../03-modules/plugin-clans.md)
