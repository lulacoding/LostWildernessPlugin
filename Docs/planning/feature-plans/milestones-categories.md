---
title: Milestones & Categories
description: Progression milestone categories and paged reward menu plan.
tags:
  - planning
  - progression
status: implemented
phase: phase-1
owner: dev
action: none
---
# Milestones: categories and pagination

> **Status Update (2026-03-19):** Core implementation is **COMPLETE**. MilestonesMenu, ProgressionJoinListener, ProgressionDeathListener, BetonQuestBridge all exist. Verify counter tracking is working correctly.
>
> **✅ Implemented:**
> - MilestonesMenu with categories and pagination
> - `/rewards` and `/milestones` commands
> - ProgressionJoinListener (join count, calendar/clan integration)
> - ProgressionDeathListener (death count)
> - BetonQuestBridge (fires BQ events on milestone unlock/claim)
> - AchievementKey with all milestone keys
> - V2ClaimCommand with rewards
>
> **⚠️ Needs Verification:**
> - `playtime_seconds` counter tracking on quit
> - Season milestone unlocks (depends on calendar integration)
> - Clan milestone unlock timing

## Overview

The `/milestones` (and `/rewards`) GUI is a built-in Bukkit inventory. It is organized by **categories**; each category opens a **paged** list of milestones so everything fits.

## Categories

| Category          | Description                    | Milestones |
|------------------|--------------------------------|------------|
| **Join & Loyalty** | Logins / return visits         | First Join, Join 3×, 10×, 25×, 50×, 100× |
| **Playtime**     | Total time online              | 1 hour, 5 hours, 24 hours, 100 hours |
| **Seasons**      | First time in a season / New Year | First Spring, Summer, Autumn, Winter; New Year login |
| **Events**      | Event participation (future)   | (placeholder: e.g. Survived Eclipse, Festival) |
| **Deaths**      | Death count (light-hearted)     | First Death, 10 Deaths |
| **Clans**       | Social                         | Joined a clan |

## Menu flow

1. **Main menu** (title: `Milestones`): one row of category icons (e.g. 6 items in slots 10–15). Click a category → open that category’s inventory.
2. **Category menu** (title: `Milestones > <Category>`): up to 7 milestone buttons per page in slots 10–16; slot 0 = Previous page, slot 4 = Back to main, slot 8 = Next page. If only one page, Prev/Next can be omitted or shown disabled.
3. **Clicking a milestone**: runs `/v2claim <key>` and closes the menu (same as today).

## Data model

- **Unlock keys** (`player_achievements.key`): `milestone:first_join`, `milestone:join_3_times`, … `milestone:join_100_times`, `milestone:playtime_1h`, `milestone:playtime_5h`, `milestone:playtime_24h`, `milestone:playtime_100h`, `milestone:first_season_spring`, `milestone:first_season_summer`, `milestone:first_season_autumn`, `milestone:first_season_winter`, `milestone:new_year_login`, `milestone:first_death`, `milestone:deaths_10`, `milestone:joined_clan`.
- **Claimed keys**: `claimed:first_join`, … same suffix as unlock key.
- **Counters** (`progression_counters`): `join_count` (existing), `playtime_seconds`, `death_count`.

## Unlock logic

- **Join & Loyalty**: On `PlayerJoinEvent`, increment `join_count`; unlock `milestone:join_N_times` when `join_count >= N` (N = 3, 10, 25, 50, 100). First join already handled.
- **Playtime**: On `PlayerQuitEvent`, add session length (seconds) to `playtime_seconds`; then unlock `milestone:playtime_1h` when `>= 3600`, `playtime_5h` when `>= 18000`, `playtime_24h` when `>= 86400`, `playtime_100h` when `>= 360000`. Session start stored in memory (e.g. `Map<UUID, Long>`) on join.
- **Seasons**: On `PlayerJoinEvent`, read `CalendarServiceV2.getCurrentSnapshot().season()` and date; if March 1 (New Year in LW), unlock `milestone:new_year_login`; unlock `milestone:first_season_spring` if season is SPRING and not yet unlocked, same for SUMMER, AUTUMN, WINTER. Progression module optionally depends on calendar; if calendar not present, skip season milestones.
- **Deaths**: On `PlayerDeathEvent`, increment `death_count`; unlock `milestone:first_death` when `>= 1`, `milestone:deaths_10` when `>= 10`.
- **Clans**: On `PlayerJoinEvent`, if `ClanService.getClanOfPlayer(uuid) != null` and not yet unlocked, unlock `milestone:joined_clan`. (Alternatively fire when they accept invite; join-check is simpler and eventually consistent.)

## Rewards (V2ClaimCommand)

Each milestone maps to a `MilestoneReward`: same AuraSkills XP pattern (skill key + amount). Suggested defaults: Join 25 = 50 Fighting, Join 50 = 75, Join 100 = 150; Playtime 1h = 25, 5h = 50, 24h = 100, 100h = 250; Seasons = 30 each; New Year = 100; Deaths = 10 / 25 (joke); Joined clan = 50. Events can be added later with similar rewards.

## Implementation steps

1. **Docs**: This plan (done).
2. **Repository**: Add `addToCounter(uuid, key, delta)` and `setCounter(uuid, key, value)` for playtime (and any future counters).
3. **AchievementKey**: Add all new milestone/claimed keys and counter keys.
4. **ProgressionJoinListener**: Extend join logic for join 25/50/100; add optional CalendarService + ClanService; on join run season and New Year checks, clan check. Add playtime session start (map).
5. **ProgressionQuitListener** (or same listener): On quit, add session seconds to `playtime_seconds`, then check playtime milestones and unlock.
6. **ProgressionDeathListener**: On death, increment `death_count`, unlock first_death and deaths_10.
7. **V2ClaimCommand**: Extend `REWARDS` map with all new claim keys and rewards; update usage message.
8. **MilestonesMenu**: Replace single screen with main menu (category buttons); on category click open category inventory. Category inventory: 7 slots per page (10–16), prev (0), back (4), next (8); title `Milestones > <Category>` or `Milestones > <Category> (2)` for page 2. Central list of categories and per-category list of milestone entries (claim key, display name, lore, material). On click: if milestone slot → v2claim and close; if prev/next → reopen same category different page; if back → reopen main.
9. **BetonQuestBridge**: Optionally add events for new milestones (or leave only original three); document that new milestones do not require BQ tags unless conditions are needed.
10. **CHANGELOG / implementation-status**: Note categories, pages, and new milestone types.

## Events category (later)

When event participation tracking exists (e.g. “was online during Eclipse”, “fished during Fishing Festival”), add milestones and event-unlock hooks; same menu category, just more entries.
