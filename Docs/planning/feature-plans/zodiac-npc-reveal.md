---
title: Zodiac NPC Reveal
description: Zodiac sign reveal via NPC interaction plan.
tags:
  - planning
  - zodiac
  - npc
status: planned
phase: phase-2
owner: dev
action: needs-dev
---
# Plan: Lord NPC Reveals (Zodiac + Pets)

**Status:** Planned  
**Created:** 2026-03-19  
**Updated:** 2026-03-19 — Expanded to cover all Lord reveals (Zodiac + Pet personality)
**Modules:** Zodiac, Pets


## Lore Context (from #zodiac-system)

> *"Epochians — players who witnessed the first sunrise on Day 0 — are blessed by the cosmos with dual signs, marking them as chosen vessels of celestial power."*
> *"Spirit Animals — each soul is bound to a guardian spirit from the wilderness. These animals guide their bonded companions, granting supernatural abilities even when their true form remains hidden."*

The Lord is the only being with the cosmic sight to read these hidden truths. This makes visits to the Lord meaningful and rare.


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
