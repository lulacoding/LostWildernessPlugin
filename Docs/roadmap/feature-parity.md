---
title: Feature Parity
description: V1 to V2 feature parity tracking.
tags:
  - roadmap
  - reference
status: reference
phase: ongoing
owner: admin
action: none
---
# V2 feature parity – full remake checklist

PluginV2 is a **full remake** of the current plugin. The target is **three servers: Lobby, Survival, Amplified**. Every feature that exists in the current plugin and in [Docs/implementation-status.md](../../implementation-status.md) (and related design docs) must eventually be **reimplemented in V2**—same behaviour, new code and architecture.

Use this doc as the checklist so nothing is missed when building Phase 2, 3, and 4. Source of truth for “what exists today”: **Docs/implementation-status.md** and **Docs/design/**.


## Parity checklist (from Docs)

Each row is a **feature area** from the current plugin/Docs. V2 must reimplement it in the listed module (or new one). Tick when V2 has equivalent behaviour.

### Calendar (V2: `calendar` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| /date, /time, /datejoined, /season, /resetcalendar, /nextday, /eoc, calendar stats | ✅ | CalendarService + commands; wrapper plugins |
| Leader/follower sync, MySQL persistence, day format (e.g. 1st January 1MC) | ✅ | CalendarRepository (H2/MySQL), async sync; server-role: survival as leader |
| New Day title, /timeset not affecting calendar | ✅ | CalendarDayChangeTitleListener; MC time -> Calendar mapping |
| Placeable calendar item (book UI) | ❌ | Optional; not yet ported |

### Portals (V2: `portals` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| Crying obsidian frame, fire, 1:1 coords, air pocket | ✅ | PortalService frame validation, fillPortal(END_GATEWAY), exit clearance |
| 5 pairs per player, spacing, MySQL (portals, pending_portals) | ✅ | PortalRepository; config/portals.yml max-pairs, frame sizes |
| Bungee/Velocity cross-server, Survival/Amplified targets | ✅ | BungeeMessenger, target-server in config; server-role from core.yml |
| /portals, /deleteportals | ✅ | PortalsCommand, DeletePortalsCommand; lw.portals.use, lw.portals.delete |
| Mount/passengers/entity passthrough, cross-server entity transfer (portal_transfer_entities) | ✅ | PortalEntitySerializer, PortalEntitySpawner; PortalRepository save/getAndClear transfer |

### Lobby (V2: `lobby` wrapper plugin)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| /verify (in-game), pending_verification, linked_accounts | ✅ | Ported to `LW_Lobby_V2`; uses shared `player` database |
| Teleporter to Survival (e.g. END_GATEWAY), connect only if linked | ✅ | `LobbyTeleportListener`; strictly checks verification AND Class Selection |
| Discord verify bot (/verify, /players, nickname) | External | Same as today; plugin only does in-game + DB |

### Events (V2: `events` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| Eclipse, Fog, Blizzard, Frost, Heatwave, Thunder, Paranoia, Easter (title), JungleMonsoon, SeasonalStorm, SpringBloom, AutumnLeafFall | ✅ | All ported; Easter week subtitle; cascades Eclipse→Thunderstorm, Eclipse→Paranoia; [events-calendar-gap.md](events-calendar-gap.md) |
| Event cascades (e.g. Eclipse → Thunderstorm Eclipse, Paranoia) | ✅ | EventCascadeRegistry implemented |
| Sleep blocking, natural horde spawner | ✅ | EventSleepListener; horde logic in some events |
| /lwdebug (start/end event, setday, nextday, stats) | ✅ | Wrapped in /event commands |
| Crash-safe block restore (Blizzard/Frost) | ✅ | BlockRestoreManager implemented |
| Mob buffs on natural spawns (Eclipse/Winter) | ✅ | Implemented in various DailyWorldEvent subclasses |
| Event boss bar, per-player toggle | ✅ | EventBossBarManager implemented |
| TPS guard, event cooldowns | ✅ | Config-driven cooldowns; TPS check in engine |
| Player event preferences, /event toggle, /event togglebar | ❌ | Planned |

### Clans (V2: `clans` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| /clan create, invite, accept, deny, leave, color, promote, demote, enemy, opposition, info, list | ✅ | ClanService, ClanCommand; full logic ported |
| ClanManager, MySQL, BotApiClient (Discord roles) | ✅ | ClanRepository (H2/MySQL); Discord roles external |
| /alliance create, invite, join, leave, info, list | ✅ | AllianceService + AllianceCommand |
| War state, /war declare, ceasefire, end, status | ✅ | WarService + WarCommand |
| Head drops (war), Discord war webhook | ✅ | WarHeadDropListener; Webhook not ported |
| /clan reside, war day subtitle, warzone radius | ❌ | Optional; not yet ported |

### Bosses / progression (V2: `progression` + bosses)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| RoofWither (6 withers, 6 Devoiders), BossAbilityListener, BossDropListener | ✅ | Ported to BossModule; Devoider/Diablo transformations |
| BossArena, ArenaManager, Boundary, ArenaBoundaryListener | ✅ | BossModule; restricted arenas with particles and barriers |
| /arena-test, /diablo-lair | ✅ | Commands ported; lw.admin permission |
| Reputation (factions / honor) | ✅ | ReputationModule (V2); Red Dead style Honor system (-1000 to +1000); Good/Bad factions |
| Milestones (gates for Aether, bosses, storyline) | ✅ | ProgressionService, AchievementKey; categories + pagination menu |
| 200% completion, Main Theme disc reward | ❌ | Progression + item/advancement hook |

### Economy (V2: `economy` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| Wallets, multi-currency (from common) | ❌ | WalletService, WalletRepository |
| Shops, transaction log | ❌ | ShopService if in scope |

### Skills (V2: `skills` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| AuraSkills integration or own XP/levels | ✅ | AuraSkillsBridge, Skills module; optional milestone XP; no own XP storage |

### Storyline / quests (V2: `quests` module)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| 7 chapters, El Diablo, Devoiders, Brotherhood, Epochians | ❌ | Quest definitions, progression gates |
| NPC/quest stack (Citizens, Denizen, etc.) | ❌ | Per Docs design |
| Spawn village (0,0), class hall, calendar square | ❌ | World/build + optional NPCs |

### Other (V2: as needed)

| Current / Docs feature | V2 status | Notes |
|------------------------|-----------|--------|
| /lwhelp, /lwplugins, /lwconfig | ❌ | Core commands |
| Default pack listener, pack status | ❌ | Lobby/resource pack |
| New Year / century titles, firework | ✅ | NewYearFireworkHandler (V2) |
| Reputation (factions) | ❌ | ReputationService if in current design |
| Party | ❌ | Party module |

---

## How to use this

- When starting a **Phase 2** or **Phase 3** slice, pick a row (or group) from the table and implement it in the corresponding V2 module.
- When a feature is done in V2, set **V2 status** to ✅ and add a short note.
- **Do not remove** rows: the goal is full parity with the current plugin and Docs. If something is deliberately dropped, mark it as “N/A (deprecated)” and note why in the Notes column.

Reference: [Docs/implementation-status.md](../../implementation-status.md), [Docs/design/](../../design/), [migration-from-v1.md](../01-getting-started/migration-from-v1.md).
