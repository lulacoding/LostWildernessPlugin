---
title: Implementation Status
description: Living per-module implementation status for PluginV2.
tags:
  - roadmap
  - reference
status: reference
phase: ongoing
owner: admin
action: none
---
# Implementation Status - V2 Rewrite

> **V1 Status:** For legacy V1 Plugin implementation status, see [archive/v1-implementation-status.md](../archive/v1-implementation-status.md)

Per-feature status for PluginV2. Update as you implement.

**See also:** [feature-inventory.md](feature-inventory.md) â€“ complete list of every feature with detailed behavior (events, commands, configs, API). Discord-pasteable version: [feature-inventory-discord.txt](../v2/discord-updates/feature-inventory-discord.txt).

**Legend:** âŒ Not started Â· ðŸš§ In progress Â· âœ… Done

**Last Verified:** 2026-04-01

**2026-04-01 â€” Operating Prompt Optimised:** `CLAUDE.md` updated via self-optimisation pipeline (`claude-self-optimiser.md`). Inserted adversarial self-review rule, strict memory write discipline, memory-as-hint verification, output contract, parallel subagent policy, tool discipline, commit hygiene, and strengthened Context7 fallback rule. Failure log created at `Docs/failure-log.md`. Continuous improvement loop defined at `Docs/continuous-improvement.md`.


## Modules


| Module                                     | Status | Notes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| ------------------------------------------ | ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Player (profile, service, repo, listeners) | âœ…      | Pre-login load, join/quit save; H2/MySQL. **Files:** PlayerModule.java, PlayerProfile.java, PlayerProfileRepository.java, PlayerService.java, PlayerJoinListener.java, PlayerQuitListener.java |
| Progression                                | âœ…      | Milestones: **categories** (Join & Loyalty, Playtime, Seasons, Events, Deaths, Clans) and **pages** (7 per page). `/rewards` and `/milestones` open main menu â†’ category â†’ paged list; click runs `/v2claim`. Join 3/10/25/50/100, playtime 1h/5h/24h/100h, first season + New Year, first death + 10 deaths, joined clan. Counters: join_count, playtime_seconds, death_count. `ProgressionJoinListener` (join + quit playtime + optional calendar/clan), `ProgressionDeathListener`. BetonQuest only for tags via `BetonQuestBridge`. **Files:** ProgressionModule.java, ProgressionService.java, ProgressionRepository.java, AchievementKey.java, MilestonesMenu.java, BetonQuestBridge.java, V2ProgressCommand.java, V2ClaimCommand.java, RewardsCommand.java. Plan: [plan-milestones-categories.md](../planning/feature-plans/milestones-categories.md). |
| Skills                                     | âœ…      | AuraSkills bridge (addSkillXp, getSkillLevel); XP only via /v2claim menu (no auto-grant on join); /v2skills debug. **Files:** SkillsModule.java, AuraSkillsBridge.java, SkillXpService.java, SkillsDebugCommand.java |
| Server wrappers                            | âœ…      | LW-Survival-V2 and LW-Amplified-V2 thin plugins depending on RPG_Core_V2 (backend-specific glue). **Locations:** survival-plugin/, amplified-plugin/, lobby-plugin/ |
| Calendar                                   | âœ…      | CalendarModule, CalendarServiceV2; **leader/follower:** only `server-role: survival` advances from MC time; other roles poll DB and fire CalendarDayAdvancedEvent when day increases; day-change title + subtitle (CalendarDayChangeTitleListener), Easter week subtitles, New Year fireworks; /date,/time,/season,/eoc,/datejoined,/nextday,/resetcalendar via wrappers; CalendarDayAdvancedEvent on day advance. **Files:** CalendarModule.java, CalendarServiceV2Impl.java, CalendarRepository.java, CalendarDayAdvancedEvent.java, EasterWeekHelper.java, SeasonCalculator.java, DateCalculator.java, TimeCalculator.java |
| Economy                                    | âŒ      | **NOT STARTED** - No module exists in codebase |
| Events                                     | âœ…      | EventsModule: all daily events; per-world dispatch; world-day-offsets; cooldowns; BlockRestoreManager; BiomeGroups; cascades; boss bar; BiomeBackupStore; BiomePainter; /season biomes. **All AeternumSeasons features implemented:** SeasonalWeatherListener (auto weather by season), SeasonalCropsListener (growth by season), WildlifeMigrationListener (spawn weights by season). **Events:** Eclipse, Thunder, Fog, Blizzard, Frost, SummerHeatwave, Paranoia, EasterWeek, JungleMonsoon, SeasonalStorm, SpringBloom, AutumnLeafFall, NewYear, Holiday, BloodMoon, MagicStorm, Tornado, FishingFestival, MiningBlessing, RestfulSleep, Festival (PDC on villagers/traders). **Files:** EventsModule.java, EventService.java, DailyWorldEvent.java, SeasonalEvent.java, 22 event implementations in impl/, BlockRestoreManager.java, BiomePainter.java, BiomeBackupStore.java, SeasonalWeatherListener.java, SeasonalCropsListener.java, WildlifeMigrationListener.java. See feature-inventory. |
| Clans                                      | âœ…      | ClansModule: ClanService, WarService, AllianceService; /clan (create, invite, accept, deny, leave, color, promote, demote, enemy, opposition, info, list); /war declare \<clan\>, ceasefire [clan], status (leaders); /alliance invite, accept, leave, info, list (leaders); WarHeadDropListener (drop victim head when killing enemy in war); ClanDisplayListener (scoreboard prefix). Tables: clans, clan_members, clan_invites, clan_relations, clan_wars, alliance_invites. Discord bot not ported (optional). **Files:** ClansModule.java, ClanService.java, ClanServiceImpl.java, WarService.java, WarServiceImpl.java, AllianceService.java, AllianceServiceImpl.java, ClanRepository.java, AllianceRepository.java, WarRepository.java, ClanCommand.java, WarCommand.java, AllianceCommand.java, WarHeadDropListener.java, ClanDisplayListener.java |
| Portals                                    | âœ…      | PortalsModule: crying obsidian frame + END_GATEWAY, flint & steel light, 1:1 coords, BungeeCord Connect, entity transfer (PortalEntitySerializer/Spawner). PortalRepository (portals, pending_portals, portal_transfer_entities). /portals, /deleteportals. PortalBreakListener async delete. config/portals.yml; server-role for target-server. **Files:** PortalsModule.java, PortalService.java, PortalRepository.java, PortalsConfig.java, PortalEntitySerializer.java, PortalEntitySpawner.java, PortalTeleportHelper.java, BungeeMessenger.java, PortalsCommand.java, DeletePortalsCommand.java, PortalInteractListener.java, PortalBreakListener.java, PortalJoinListener.java |
| Zodiac                                     | âœ…      | ZodiacModule: 13 zodiac signs (Aries-Ophiuchus) assigned on join based on MC calendar date (month+year); 14 spirit animals; Good/Evil perk variants (Libra, Scorpio, Ophiuchus); Sync bonus (month+year match = double perks + Luck I); Epochian special status (Day 0 = hidden 2nd sign); Clan leader year bonus; /zodiac info/reveal/clanbonus; /datejoined extended; ZodiacEffectListener periodic effects; ZodiacRepository (player_zodiac table). **Files:** ZodiacModule.java, ZodiacService.java, ZodiacServiceImpl.java, ZodiacRepository.java, ZodiacSign.java, SpiritAnimal.java, ZodiacProfile.java, ZodiacCommand.java, ZodiacJoinListener.java, ZodiacEffectListener.java. **Testing guide**: [zodiac test guide](../testing/zodiac-test.md) |
| Quests                                     | âŒ      | **NOT STARTED** - No module exists in codebase. Plan to use BetonQuest external plugin. |
| Party                                      | âœ…      | **COMPLETE** - PartyModule: PartyService, PartyRepository; cross-server via cluster messaging (`party/sync`, `party/disband`); DB polling fallback. `/party create/invite/accept/leave/disband/info/kick`. **Listeners:** PartySessionListener (join/quit), PartyDisplayListener (visual effects, glow, scoreboard), PartyFriendlyFireListener (damage protection), PartyBuffListener (party damage boost). Config: `config/party.yml`. Max size configurable (default 6). **Service integration:** BossKillTracker party credit, PortalEnterListener party notification, EventService party requirements/buffs, ProgressionService shared objectives. **Files:** PartyModule.java (6.5KB), Party.java (3.3KB), PartyService.java (2.2KB), PartyServiceImpl.java (21KB), PartyRepository.java (9.2KB), PartyCommand.java (11KB), 4 listeners in listener/. Tables: parties, party_members, party_invites. **Status:** All 5 phases complete (100%). |
| Classes                                    | âœ…      | **COMPLETE** - ClassesModule: 5 player classes (Templar, Ranger, Artificer, Cultist, Berserker); ClassService, PlayerClassRepository; ClassAbilityListener (active abilities), ClassPassiveListener (passive effects); AuraSkills mastery integration; ClassSkillTreeService with 7 unlock levels per class (1/5/10/15/20/30/50); Ultimate items at Lv50; ClassMasteryXpListener for XP grants. `/class` command + ClassSelectionMenu. **Files:** ClassesModule.java (7.3KB), PlayerClass.java, ClassService.java, ClassSkillTreeService.java, ClassSkillTreeServiceImpl.java (8.8KB), ClassSelectionMenu.java, UltimateItemBuilder.java, PlayerClassRepository.java, ClassCommand.java, AuraSkillsIntegration.java, ClassAbilityListener.java (11KB), ClassPassiveListener.java (6.7KB), ClassMasteryXpListener.java (8.6KB), ClassStarterXpJoinListener.java. Tables: player_classes, class_starter_xp_grants, class_ultimate_items. **Status:** All 8 phases complete (88% of original plan, Phase 6 skipped - using AuraSkills GUI). |
| Personality                                | âœ…      | **100% COMPLETE** - PersonalityModule: TraitService, TraitRepository, CompletionService, HolyEnchantService. 13 PersonalityTraits, 5 Elements, 4 TraitTiers, 13 HolyEnchants. **Implemented:** Core data models, service layer, database tables, PersonalityModule registration, QuizSessionManager (quiz flow), TraitPassiveListener (13/13 traits, RANGER Master damage boost), TraitCommand (`/trait` command with 6 subcommands), ElementalPassiveListener (5 god-tier element passives), TraitItemListener (13/13 ultimate items including Warlord's Blade, Ranger's Quiver, Ragnarok Axe, Ancient Whistle, Mirror Shard, Staff of the Covenant, Runeblade, Philosopher's Stone), HolyEnchantEffectListener (13/13 enchants including STARFALL, PHOENIX_FLAME, ECHO_STEP, LUNAR_BLESSING), scheduled tasks (HEALER AoE, LUNAR_BLESSING). **Files:** PersonalityModule.java, PersonalityTrait.java, Element.java, TraitTier.java, HolyEnchant.java, PlayerTraitProfile.java, TraitService.java, TraitServiceImpl.java, TraitRepository.java, HolyEnchantService.java, CompletionService.java, QuizSessionManager.java, QuizCompletionHandler.java, TraitPassiveListener.java (500+ lines), TraitCommand.java, ElementalPassiveListener.java, HolyEnchantEffectListener.java (450+ lines), TraitItemListener.java (700+ lines), SurvivalV2Plugin.java (scheduled tasks). **Testing Guide:** [personality test guide](../testing/personality-test.md). **Final Summary:** [PERSONALITY_FINAL_IMPLEMENTATION.md](PERSONALITY_FINAL_IMPLEMENTATION.md). **Status:** Ready for production testing. |
| Boss                                       | âœ…      | BossModule: RoofWitherListener (6 withers â†’ 6 Devoiders transformation on nether roof), BossAbilityListener (custom abilities), BossDropListener (loot tables), ArenaManager (boundary enforcement), BossArenaBarriers (arena construction), BossKillRepository (clan kill tracking). **Commands:** /arena-test, /diablo-lair. **Files:** BossModule.java, BossService.java, BossArena.java, ArenaManager.java, BossArenaBarriers.java, Boundary.java, DiabloLairGenerator.java, BossKillRepository.java, RoofWitherListener.java, BossAbilityListener.java, BossDropListener.java, ArenaBoundaryListener.java, ArenaTestCommand.java, DiabloLairCommand.java. **Integration:** ReputationModule for Good/Evil factions. |
| Reputation                                 | âœ…      | ReputationModule: Honor system (-1000 to +1000), Good/Bad factions, ReputationService, ReputationRepository. `/reputation` command. **Files:** ReputationModule.java, ReputationService.java, ReputationRepository.java, Faction.java, ReputationListener.java, ReputationCommand.java. Table: player_reputation. **Integration:** Boss kills affect reputation. |



## DB / config


| Item                                  | Status | Notes                                               |
| ------------------------------------- | ------ | --------------------------------------------------- |
| core.yml, db.yml                      | âœ…      | Defaults in jar; config in data folder              |
| player_profiles table (or equivalent) | âœ…      | Created by PlayerProfileRepository                  |
| Per-domain tables                     | âœ…      | **21 tables verified:** player_achievements, progression_counters, player_zodiac, clans, clan_members, clan_invites, clan_relations, clan_wars, alliance_invites, portals, pending_portals, portal_transfer_entities, calendar_state, calendar_player_join, parties, party_members, party_invites, player_classes, class_starter_xp_grants, class_ultimate_items, player_traits, player_elements, player_temples, player_quiz_answers, player_holy_enchants, player_christmas_claims, player_reputation, boss_kill_tracking |



## Module File Counts (Code Audit 2026-03-19)

| Module | Java Files | Status |
|--------|-----------|--------|
| boss | 15 | âœ… Complete |
| calendar | 9 | âœ… Complete |
| clans | 15 | âœ… Complete |
| classes | 16 | âœ… Complete |
| core | 6 | âœ… Complete |
| events | 38 | âœ… Complete (~90% - Nether events missing) |
| infra | 6 | âœ… Complete |
| party | 10 | âœ… Complete |
| personality | 11 | âœ… Complete (13 traits, 20/20 trait passives, 5 elements, 13 enchants, 13 items, 2 scheduled tasks) |
| player | 6 | âœ… Complete |
| portals | 14 | âœ… Complete |
| progression | 11 | âœ… Complete |
| reputation | 6 | âœ… Complete |
| skills | 4 | âœ… Complete |
| world | 3 | âœ… Complete (BiomePainter, BiomeBackupStore) |
| zodiac | 10 | âœ… Complete |
| **economy** | **0** | âŒ **Module does not exist** |
| **quests** | **0** | âŒ **Module does not exist** |


## Related Planning Documents

- [phase1-spec.md](../archive/phase1-spec.md) - Phase 1 implementation (COMPLETE)
- [plan-class-skill-tree.md](../planning/feature-plans/class-skill-tree.md) - Classes implementation (COMPLETE)
- [personality plans](../planning/feature-plans/personality.md) - Personality implementation (50% COMPLETE)
- [plan-milestones-categories.md](../planning/feature-plans/milestones-categories.md) - Progression (COMPLETE)
- [phase3-betonquest-milestones.md](../planning/feature-plans/betonquest-milestones.md) - BetonQuest integration (COMPLETE)
- [plan-aeternum-features-in-lw.md](../planning/feature-plans/aeternum-features.md) - AeternumSeasons features (~90% COMPLETE)
- [phase3-events.md](../planning/feature-plans/phase3-events.md) - Events roadmap (mostly COMPLETE, Nether events pending)
- [plan-zodiac-npc-reveal.md](../planning/feature-plans/zodiac-npc-reveal.md) - Zodiac NPC reveal (PLANNED, not started)

