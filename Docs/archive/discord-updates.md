---
title: Discord Updates Archive
description: Archived Discord update posts and announcements.
tags:
  - archive
  - discord
status: archive
phase: archive
owner: admin
action: none
---
# Discord Updates Archive

This directory contains Discord-formatted versions of documentation for posting to server channels. These files are **not duplicates** of the main documentation; they are specifically formatted for Discord's message character limits and markdown support.

## Files

- `biome-painting-update-discord.txt` - Seasonal biome painting (Aeternum-style)
- `boss-arena-update-discord.txt` - Boss transformation and arena system
- `calendar-update-discord.txt` - Calendar system update (message parts)
- `classes-update-discord.txt` - Class skill tree system (5 classes, AuraSkills integration)
- `clans-update-discord.txt` - Clans, war, alliances update
- `events-update-discord.txt` - Dynamic world events system (20+ events)
- `feature-inventory-discord.txt` - Complete feature inventory (Discord format)
- `lobby-update-discord.txt` - Lobby verification and teleporter
- `milestones-update-discord.txt` - Progression and milestones
- `personality-update-discord.txt` - Personality traits and elemental quest system
- `player-profile-update-discord.txt` - Player profile and session tracking
- `portals-update-discord.txt` - Portal system update
- `reputation-update-discord.txt` - Reputation system update
- `skills-auraskills-integration-discord.txt` - AuraSkills integration bridge
- `zodiac-module-announcement.txt` - Zodiac system announcement

## Usage

Copy/paste each PART marker section into separate Discord messages. These use Discord markdown (`**bold**`, `code`, bullet lists).

---

## 📝 Update Format Guide

### When Adding New Features

Each feature announcement file follows this structure:

**Initial Announcement (with PART separators for 2000-char Discord limit):**

```
**PluginV2 – Feature Name**

─────── PART 1 ───────

**Overview**
- Brief description
- Key features

**Feature Details**
1. **Feature One**
   - Details

─────── PART 2 ───────

**Status:** ✅ COMPLETE / 🚧 IN PROGRESS

**Not Yet Implemented:**
- Missing items
```

### When Updating Existing Features

**Append to the END of the file** (newest updates last):

```
═══════════════════════════════════════════════

**📅 UPDATE – YYYY-MM-DD (Time)**

**✅ What Was Added/Fixed**

**Feature Name** 🔨
- ✅ **Specific update:** Description
  - Technical details
  - In-game message examples: §a✓ Success message

**Technical Details:**
- Implementation notes
- Files changed
- Build status

**Module Status Update:**
- Old status → **New status** 🎉

**Remaining Work:**
- Outstanding tasks
```

### Format Guidelines

1. **Date Format:** `YYYY-MM-DD (Time of day)` e.g. `2026-03-19 (Evening)`
2. **Separator:** Use `═══════════════════════════════════════════════` (80 equals signs)
3. **Status Icons:**
   - ✅ Complete
   - 🚧 In progress
   - ❌ Not started
   - ⚠️ Limitation/warning
4. **Emojis:** Use relevant emojis (🔨🧪👁🐺🎉📅)
5. **Messages:** Show in-game messages with color codes: `§a✓ Message text`
6. **Chronological:** Newest updates at bottom

### Character Limits

- Discord: **2000 characters per message**
- Use `─────── PART X ───────` for initial announcements
- Update sections can be longer (paste as multiple messages)

### Example Workflow

When implementing a feature:

1. ✅ Write code + compile
2. ✅ Update `CHANGELOG.md` (technical details)
3. ✅ Update `implementation-status.md` (module status)
4. ✅ **Append to discord-updates/*.txt** (user announcement)
5. ✅ Test copy-paste to Discord

## Source Documentation

For complete documentation, see the main docs directory:
- Full feature list: [../roadmap/feature-inventory.md](../roadmap/feature-inventory.md)
- Implementation status: [../roadmap/implementation-status.md](../roadmap/implementation-status.md)
