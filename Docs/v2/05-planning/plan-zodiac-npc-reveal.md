# Plan: Zodiac Reveal via Lord NPC

**Status:** Planned  
**Created:** 2026-03-19  
**Module:** Zodiac  

---

## Overview

Replace the player-accessible `/zodiac reveal` command with an immersive NPC interaction. The **Lord NPC** (a mystical character) reveals hidden zodiac information through roleplay dialogue, making it feel like a fortune-telling experience rather than a command execution.

## Current Behavior

- Any player with `lw.rank.lord` permission can run `/zodiac reveal <player>`
- Reveals both the Epochian second sign and spirit animal
- Feels mechanical, breaks immersion

## Proposed Behavior

- Only the **Lord NPC** can reveal hidden zodiac info
- Player initiates conversation with the Lord
- Lord "reads their stars" through dialogue
- Reveal logic runs in background via BetonQuest event
- Player receives mystical RP experience

---

## Implementation

### Phase 1: Service Layer (No Changes)

Keep existing methods in `ZodiacService`:
- `revealSecondSign(UUID target, UUID lord)`
- `revealSpiritAnimal(UUID target, UUID lord)`

These will be called by BetonQuest events instead of the command.

### Phase 2: Remove/Restrict Command

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
