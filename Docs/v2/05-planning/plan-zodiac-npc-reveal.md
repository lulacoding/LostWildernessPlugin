# Plan: Lord NPC Reveals (Zodiac + Pets)

**Status:** Planned  
**Created:** 2026-03-19  
**Updated:** 2026-03-19 — Expanded to cover all Lord reveals (Zodiac + Pet personality)
**Modules:** Zodiac, Pets

---

## Overview

The **Lord NPC** is the single in-game character who can reveal hidden information to players. Nothing hidden is revealed via player commands — it all goes through the Lord (or in the case of pets, the Redeemer during Easter).

**Things the Lord reveals:**
1. **Zodiac** — Epochian 2nd sign + Spirit Animal
2. **Personality** — Player's hidden element (via Personality system)
3. **Pet Personality** — A pet's hidden personality trait

**The Redeemer** (Easter-only wandering trader NPC) can also reveal **pet personalities only**, available Palm Sunday → Easter Sunday once per year.

All reveals are implemented as BetonQuest events fired via NPC dialogue — no player-accessible commands.

---

## Lore Context (from #zodiac-system)

> *"Epochians — players who witnessed the first sunrise on Day 0 — are blessed by the cosmos with dual signs, marking them as chosen vessels of celestial power."*
> *"Spirit Animals — each soul is bound to a guardian spirit from the wilderness. These animals guide their bonded companions, granting supernatural abilities even when their true form remains hidden."*

The Lord is the only being with the cosmic sight to read these hidden truths. This makes visits to the Lord meaningful and rare.

---

## Current Behavior (to change)

- `/zodiac reveal <player>` — accessible to any `lw.rank.lord` permission holder
- Feels mechanical, breaks immersion
- No equivalent for pets yet

## Proposed Behavior

- All reveals happen via Lord NPC BetonQuest dialogue
- `/zodiac reveal` restricted to console/admin only (for support purposes)
- New: `/pets reveal` restricted to console/admin only
- Players must physically visit the Lord NPC

---

## Implementation

### Phase 1: Service Layer (No Changes Needed)

Keep existing methods:
- `ZodiacService.revealSecondSign(UUID target, UUID lord)`
- `ZodiacService.revealSpiritAnimal(UUID target, UUID lord)`
- `PetService.revealPersonality(UUID petUuid, UUID revealerUuid)` (new — from Pet module)
- `PersonalityModule` element reveal (future)

All called by BetonQuest events.

### Phase 2: Restrict Commands

**ZodiacCommand.java** — change `reveal` permission to `lw.admin.zodiac.reveal` (console/admin only)
**PetCommand.java** — `reveal` subcommand is console/admin only (`lw.admin.pets.reveal`)

### Phase 3: BetonQuest Integration

#### Conditions

| Condition | Description |
|-----------|-------------|
| `zodiac_has_hidden` | Player has unrevealed 2nd sign OR spirit animal |
| `zodiac_is_epochian` | Player is Epochian (has 2nd sign) |
| `zodiac_spirit_revealed` | Spirit animal already revealed |
| `zodiac_second_revealed` | 2nd sign already revealed |
| `has_pet_with_hidden_personality` | Player has a pet with unrevealed personality |
| `pet_personality_revealed` | All pets already revealed |

#### Events

| Event | Effect |
|-------|--------|
| `zodiac_reveal_spirit` | Reveals spirit animal |
| `zodiac_reveal_second` | Reveals Epochian 2nd sign |
| `zodiac_reveal_all` | Reveals both zodiac secrets |
| `pet_personality_reveal` | Reveals pet personality (most recent untamed, or player selects) |

#### Variables

| Variable | Returns |
|----------|---------|
| `%zodiac_month_sign%` | Player's month sign |
| `%zodiac_year_sign%` | Player's year sign |
| `%zodiac_second_sign%` | 2nd sign (if revealed) else ??? |
| `%zodiac_spirit_animal%` | Spirit animal (if revealed) else ??? |
| `%zodiac_month_symbol%` | Sign emoji |
| `%pet_name%` | Pet's name |
| `%pet_personality%` | Pet's personality (if revealed) else ??? |

### Phase 4: Lord NPC Conversation Flow

```yaml
# Lord NPC dialogue options:
# 1. "Read my stars" → zodiac reveal
# 2. "Read my pet's soul" → pet personality reveal  
# 3. "What is my element?" → personality element reveal (post-100% completion)
# 4. Farewell

NPC_options:
  greeting:
    text: "Seeker. What hidden truth do you wish revealed?"
    pointers: ask_zodiac, ask_pet, ask_element, farewell
  
  # === ZODIAC ===
  zodiac_reading:
    text: "Let me peer beyond the veil of stars..."
    conditions: zodiac_has_hidden
    events: play_mystical_sound
    pointers: zodiac_result
  
  zodiac_result_spirit:
    text: "I see it now... The %zodiac_spirit_animal% walks beside you in shadow."
    events: zodiac_reveal_spirit
    pointers: zodiac_epochian_check, farewell_revealed
  
  zodiac_epochian:
    text: "And there is more... A second sign. %zodiac_second_sign% also claims you. You are truly Chosen."
    conditions: zodiac_is_epochian
    events: zodiac_reveal_second
    pointers: farewell_epochian
  
  zodiac_already_known:
    text: "Your stars have already been read. The %zodiac_spirit_animal% guides you still."
    conditions: "!zodiac_has_hidden"
    pointers: farewell

  # === PET ===
  pet_reading:
    text: "Bring your companion closer... I will read its soul."
    conditions: has_pet_with_hidden_personality
    events: play_mystical_sound
    pointers: pet_result
  
  pet_result:
    text: "This creature carries the spirit of the %pet_personality%... guard it well."
    events: pet_personality_reveal
    pointers: farewell_revealed
  
  pet_already_known:
    text: "Your companion's soul is already known to you."
    conditions: "!has_pet_with_hidden_personality"
    pointers: farewell

  # === FAREWELLS ===
  farewell_revealed:
    text: "Go now. Walk in the light of what you have learned."
  farewell_epochian:
    text: "The stars have great plans for you, Chosen. Do not squander their gift."
  farewell:
    text: "May the stars guide your path."

player_options:
  ask_zodiac:
    text: "Read my stars."
    pointers: zodiac_reading, zodiac_already_known
  ask_pet:
    text: "Read my pet's soul."
    pointers: pet_reading, pet_already_known
  ask_element:
    text: "What is my element?" 
    # Gated behind 100% story completion — future implementation
    pointers: element_reveal
  farewell:
    text: "Farewell."
```

### Phase 5: The Redeemer (Easter NPC)

- Citizens wandering trader NPC
- Only active **Palm Sunday → Easter Sunday** (gated via BetonQuest calendar condition)
- Can reveal **pet personalities only** (not zodiac)
- Uses same `pet_personality_reveal` BetonQuest event
- Dialogue has Easter-themed flavor text

---

## Files to Create/Modify

### New Files
- `ZodiacBetonQuestIntegration.java` — conditions, events, variables
- `PetBetonQuestIntegration.java` — pet-specific conditions/events/variables
- `quests/lord/lord-reveals.yml` — BetonQuest conversation
- `quests/redeemer/redeemer-easter.yml` — BetonQuest conversation (Easter)

### Modified Files
- `ZodiacCommand.java` — restrict `reveal` to `lw.admin.zodiac.reveal`
- `ZodiacModule.java` — register BQ integration
- `PetCommand.java` — restrict `reveal` to `lw.admin.pets.reveal`
- `PetModule.java` — register BQ integration

---

## Testing Checklist

- [ ] Lord NPC spawns correctly
- [ ] "Read my stars" dialogue triggers correctly
- [ ] Zodiac spirit animal revealed via Lord
- [ ] Epochian 2nd sign revealed via Lord
- [ ] Already-revealed condition hides option correctly
- [ ] "Read my pet's soul" dialogue triggers correctly
- [ ] Pet personality revealed via Lord
- [ ] `/zodiac reveal` no longer works for regular players
- [ ] Redeemer spawns only during Easter week
- [ ] Redeemer can reveal pet personality
- [ ] Redeemer disappears after Easter Sunday

---

## Related Docs

- [plugin-zodiac.md](../03-modules/plugin-zodiac.md)
- [plan-pet-module.md](plan-pet-module.md)
- [plan-personality-elemental-quest.md](plan-personality-elemental-quest.md)
- [phase3-betonquest-milestones.md](phase3-betonquest-milestones.md)


**File:** `ZodiacCommand.java`

Option A: Remove `reveal` subcommand entirely  
Option B: Change permission to `lw.admin.zodiac.reveal` (console/admin only)

Recommended: **Option B** — keeps admin override for debugging/support.

### Phase 3: BetonQuest Integration

**New File:** `ZodiacBetonQuestIntegration.java`

#### Conditions

| Condition | Description | Usage |
|-----------|-------------|-------|
| `zodiac_has_hidden` | True if player has unrevealed 2nd sign OR spirit animal | Gate dialogue options |
| `zodiac_is_epochian` | True if player is Epochian (has 2nd sign) | Special dialogue branch |
| `zodiac_spirit_revealed` | True if spirit animal already revealed | Prevent re-reveal dialogue |
| `zodiac_second_revealed` | True if 2nd sign already revealed | Prevent re-reveal dialogue |

#### Events

| Event | Description | Effect |
|-------|-------------|--------|
| `zodiac_reveal_spirit` | Reveals spirit animal | Calls `ZodiacService.revealSpiritAnimal()` |
| `zodiac_reveal_second` | Reveals Epochian 2nd sign | Calls `ZodiacService.revealSecondSign()` |
| `zodiac_reveal_all` | Reveals both | Calls both methods |

#### Variables

| Variable | Returns | Example |
|----------|---------|---------|
| `%zodiac_month_sign%` | Player's month sign name | "Aries" |
| `%zodiac_year_sign%` | Player's year sign name | "Leo" |
| `%zodiac_second_sign%` | 2nd sign (if revealed) or "???" | "Ophiuchus" |
| `%zodiac_spirit_animal%` | Spirit animal (if revealed) or "???" | "Wolf" |
| `%zodiac_month_symbol%` | Month sign emoji | "♈" |
| `%zodiac_spirit_emoji%` | Spirit animal emoji | "🐺" |

### Phase 4: Lord NPC Setup

**NPC:** Citizens NPC named "The Lord" (or similar mystical title)  
**Location:** Special shrine/temple area  
**Interaction:** Right-click starts BetonQuest conversation

### Phase 5: Conversation Flow

**File:** `quests/zodiac/lord-reveal.yml` (BetonQuest)

```yaml
conversations:
  lord_zodiac:
    quester: "The Lord"
    first: greeting
    NPC_options:
      greeting:
        text: "Ah, a seeker of celestial truths. What brings you to my sanctum?"
        pointers: ask_reading, ask_spirit, farewell
      
      reading_intro:
        text: "Very well. Let me peer beyond the veil..."
        events: play_mystical_sound
        pointers: reading_result
      
      reading_result:
        text: "I see it now... The %zodiac_spirit_animal% walks beside you in shadow."
        conditions: zodiac_has_hidden
        events: zodiac_reveal_spirit
        pointers: epochian_check, farewell_revealed
      
      epochian_reveal:
        text: "But wait... there is more. A second sign! %zodiac_second_sign% also claims you as their own. You are truly chosen."
        conditions: zodiac_is_epochian
        events: zodiac_reveal_second
        pointers: farewell_epochian
      
      already_revealed:
        text: "Your stars have already been read, child. The %zodiac_spirit_animal% guides you still."
        conditions: "!zodiac_has_hidden"
        pointers: farewell
      
      farewell_revealed:
        text: "Go now, and walk in the light of your constellation."
      
      farewell_epochian:
        text: "The stars have great plans for you, Epochian. Do not squander their gift."
      
      farewell:
        text: "May the stars guide your path."
    
    player_options:
      ask_reading:
        text: "Can you read my stars?"
        pointers: reading_intro, already_revealed
      
      ask_spirit:
        text: "What spirit walks with me?"
        pointers: reading_intro, already_revealed
      
      farewell:
        text: "Farewell."
```

---

## Files to Create/Modify

### New Files
- `src/.../zodiac/betonquest/ZodiacBetonQuestIntegration.java`
- `src/.../zodiac/betonquest/ZodiacConditions.java`
- `src/.../zodiac/betonquest/ZodiacEvents.java`
- `src/.../zodiac/betonquest/ZodiacVariables.java`
- `quests/zodiac/lord-reveal.yml` (BetonQuest conversation)

### Modified Files
- `ZodiacCommand.java` — restrict/remove reveal subcommand
- `ZodiacModule.java` — register BetonQuest integration if BetonQuest present
- `plugin.yml` — update permission description

---

## Testing Checklist

- [ ] Lord NPC spawns correctly at designated location
- [ ] Conversation starts on right-click
- [ ] Conditions correctly gate dialogue options
- [ ] `zodiac_reveal_spirit` event reveals spirit animal
- [ ] `zodiac_reveal_second` event reveals 2nd sign (Epochians only)
- [ ] Variables display correctly in dialogue
- [ ] `/zodiac reveal` no longer works for regular players
- [ ] Admin can still reveal via console or elevated permission
- [ ] Player receives appropriate messages after reveal
- [ ] `/zodiac info` and `/datejoined` show revealed info correctly

---

## Future Enhancements

- **Multiple Lord NPCs** across servers with synced reveals
- **Cost/requirement** to consult the Lord (gold, quest completion, etc.)
- **Cooldown** on re-visiting for different readings
- **Horoscope feature** — Lord gives daily fortune based on current alignments
- **Compatibility reading** — Lord reads compatibility between two players

---

## Dependencies

- BetonQuest plugin
- Citizens plugin (for NPC)
- Zodiac module (existing)
- Calendar module (for date variables)

## Related Docs

- [plugin-zodiac.md](../03-modules/plugin-zodiac.md)
- [phase3-betonquest-milestones.md](phase3-betonquest-milestones.md)
