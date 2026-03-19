# Implementation Status - V1 Plugin (Current System)

> **V2 Rewrite Status:** For the new V2 implementation status, see [v2/06-operations/implementation-status.md](v2/06-operations/implementation-status.md)

This document lists what is **implemented** in the V1 Plugin codebase versus what appears in the Bible (Discord design) but is **not yet implemented**. Use it to see the gap between design and code. Code paths are under `Plugin/` (e.g. `common/`, `portals/`, `servers/survival/`, `servers/amplified/`).

**Legend:** ✅ Implemented · ❌ Not implemented · ⚠️ Partial / stub

---

## 0. Priority task list (execution order)

This section flattens the rest of this file (and the `Docs/future/` plans) into a **practical “do next” list**, roughly in the order you should tackle them to move toward a Hypixel‑scale, 400‑player network.

### 0.1 Core infrastructure & safety (Docs/future/*)

1. **Lock down network & proxy trust**  
   - Implement the network layout and firewall rules in `future/network-and-security.md`.  
   - Ensure: proxy‑only public entry, Bungee/Velocity ↔ backend secrets, backends firewalled from the public internet.

2. **Harden MySQL & backups**  
   - Follow `future/database-architecture-and-tuning.md` and `future/backups-and-disaster-recovery.md`:  
     - Tune InnoDB and pool sizes.  
     - Set up nightly DB + world backups and test a restore.

3. **Performance baseline per backend**  
   - Apply initial Paper/Spigot/Bukkit tuning from `future/paper-performance-and-capacity.md` for lobby/survival/amplified.  
   - Do a small load test and record safe per‑server player counts.

4. **Monitoring, logging, and alerts**  
   - Implement the minimal stack in `future/monitoring-logging-and-alerting.md`: central logs, basic metrics, Discord alerts for crashes/low TPS/DB issues.

5. **Ops playbooks & runbooks**  
   - Use `future/ops-playbooks-and-runbooks.md` as a living document when incidents happen (crashes, DB issues, bot failures) so responses get faster and more consistent.

**0.1 doc/config status:** The future docs have been updated with ServerUPDATE implementation details: network-and-security.md (bind addresses, firewall summary, checklist, BungeeGuard example configs + firewall runbooks); database-architecture-and-tuning.md (Ubuntu/Debian MariaDB drop-in config, placement instructions, index recommendations, validation commands); backups-and-disaster-recovery.md (automated scripts, nightly systemd timer, full restore procedures for DB + world, DR drill checklist); paper-performance-and-capacity.md (config snippets, JVM flags, safe player count table); monitoring-logging-and-alerting.md (Promtail/Prometheus/Alertmanager/Grafana stack, Discord crash alerts, full alert rules table in Docs/ops/lw-alerts.yml); ops-playbooks-and-runbooks.md (See also links to all of the above). ServerUPDATE backends have had performance tuning applied (lobby: view-distance 6, simulation-distance 4, peaceful; survival/amplified: view-distance 8, simulation-distance 6; JVM flags in start.bat) and now bind to localhost via `server-ip=127.0.0.1`. **Backup ops scripts committed:** `Docs/ops/mariadb-backup.sh`, `world-backup.sh`, `prune-backups.sh`, `lw-backup.service`, `lw-backup.timer`, `lw-backup.cnf.example`, `mariadb-99-lostwilderness.cnf`, `backup-setup.md`. Remaining 0.1 work: apply firewall on host, install/enable BungeeGuard (set token + install jars), deploy backup scripts to Linux host + run first backup, run backup/restore drill (fill DR drill table), run load test and fill safe player counts, deploy monitoring stack (install Loki/Promtail/Prometheus/Alertmanager/Grafana, configure Discord webhook, enable spark on backends).

**GitHub repo cleanup (2026-03-11):** Repo prepared for private GitHub upload. Root `.gitignore` added. ~5 000 tracked generated/runtime files removed (node\_modules, server libraries, world data, logs, caches, crash reports, runtime DBs, private keys, ops/ban/user files). `DiscordBot/.env.example` added. All `ServerUPDATE` plugin configs with live MySQL passwords sanitised (`CHANGE_ME` in git; real passwords kept locally via `git update-index --skip-worktree`). See `CHANGELOG.md` for full detail.

**BL-001 (logging) fixed:** Startup log duplication (2x–4x) was caused by each plugin jar adding its own `LWConsoleHandler` to the shared logger. `LWLogger` now adds only one handler per logger (see `Plugin/common/.../logging/LWLogger.java` and `Docs/bug-list.md`).

**BL-002 (portal break) fixed:** Breaking a portal frame no longer causes “Unknown column 'portal_id'” or server freeze. `PortalBreakListener` now uses the real schema (`player_uuid`, `portal_name`, `survival_*`) and runs all DB cleanup asynchronously (see `Plugin/portals/.../PortalBreakListener.java` and `Docs/bug-list.md`).

**Portal transfer — stale mount fix:** Entity transfer data is only written when the player steps on the portal (TeleportListener). On arrival, after reading transfer data we now clear all `portal_transfer_entities` rows for that player so a mount left on the other server can never be re-applied from stale DB data (see `DatabaseManager.getAndClearPortalTransferEntities` and `clearAllPortalTransferEntitiesForPlayer`).

**ServerUPDATE plugin layout (modular):** Backends use **separate** JARs, not a single fat pack. **survival-1** and **amplified-1** each have: `common.jar`, `LostWilderness-Calendar-all.jar`, `LostWilderness-Events-all.jar`, `LostWilderness-Clans-all.jar`, `LostWilderness-Portals-all.jar`, plus `LostWilderness-Survival-all.jar` (survival-1) or `LostWilderness-Amplified-all.jar` (amplified-1). Lobby has `LostWilderness-Lobby-all.jar`. Use this modular set so Calendar, Portals, Events, and Clans load as distinct plugins. Do not add the fat Survival/Amplified JARs on top of the same modules elsewhere or you may get duplicate commands.

### 0.2 Scaling & architecture

6. **Plan multi‑instance servers and routing**  
   - Design naming and routing (`survival-1`, `survival-2`, `lobby-1`, `lobby-2`) following `future/multi-instance-servers-and-routing.md`.  
   - Implement a simple “last server” or least‑loaded routing model at the proxy layer.

7. **Load testing & capacity planning**  
   - Run the scenarios in `future/load-testing-and-capacity-planning.md` to validate safe capacities and update that doc with real numbers.

8. **Auth, anticheat, and trust boundaries**  
   - Choose and configure primary anticheat plugins and document trust boundaries per `future/auth-anticheat-and-trust-boundaries.md`.  
   - Integrate with Discord verification / clan roles where appropriate.

### 0.3 Gameplay & features (from sections 1–8 below)

9. **Alliance / clan polish and war UX**  
   - From §3 Clans & War:  
     - Decide on `/clan reside` semantics and implement if still desired.  
     - Implement war “day subtitle” and/or warzone radius if you want more feedback around wars.

10. **Lobby / queue / resource‑pack ecosystem**  
    - From §3 Lobby / queue / resource pack and §4 Resource packs:  
      - Decide whether to bring VoidWorldGenerator/DeluxeMenus/queue plugin into this repo or keep them external but documented.  
      - If PackServer `/packs/*.zip` remains external, document its URL and integration with DefaultPackListener.

11. **Worlds & gameplay expansion**  
    - From §5 Worlds & gameplay design:  
      - Plan/Create Creative server, Devoid dimension, and other post‑game worlds as separate backends, wired into the proxy + calendar as needed.

12. **Audio/visual polish**  
    - From §7 Audio & visual:  
      - Lobby/warzone/eclipse music triggers.  
      - Logos and promotional assets (art/design work, not plugin code).

13. **Bedrock and cross‑play niceties**  
    - From §4 Resource packs & client assets and §8 Open questions:  
      - Geyser/Floodgate config, Bedrock pack behaviour, lobby GUI fallback for Bedrock.

Use this list as your **top‑level execution guide**; the sections below provide the detailed per‑feature status and links into design docs.

---

## 1. Overview & Vision

Lost Wilderness is a **biblically-inspired “Earth simulator”** Minecraft server: highly customised vanilla, **no-hacks pseudo-anarchy**, designed for long-lived civilisation (economies, wars, history) without heavy mods so anticheat and cross-play can stay strong.

### What “Storyline” Means Here

The storyline is **structured chapters + quests**, but it does not replace the sandbox. Chapters act as **gates** (bosses, portals, quizzes, post-game worlds) while players still choose their own life (clans, war, nomad trade, exploration, building).

- **Core premise**: restore multiversal cosmic peace by defeating **El Diablo** and pushing beyond the “Firmament”.
- **Canon structure**: **7 chapters**, followed by **Leaving the Firmament** and then the **Angel → Galactic Journey** post-game.
- **Lore delivery**: the **Brotherhood** (secret temple/library) is the in-world vehicle for explicit scripture, “Sam verses”, and quest direction. **Epochians** (Day 0 players) are framed as early keepers.
- **Progression tracking**: “**200% completion**” = vanilla advancements + Lost Wilderness custom advancements, rewarding the **Main Theme disc**.
- **World history**: the **calendar** is the internal clock; eclipses, seasons/hemisphere, New Year and century milestones create server-wide beats that make history feel shared.

See also: [Design overview storyline](design/01-overview/storyline.md) and [Storyline chapters (draft)](design/01-overview/storyline-chapters.md).

| Item | Status | Notes |
| --- | --- | --- |
| **Storyline** | ⚠️ | Progression foundations + gates implemented; full quest/NPC storyline flow still pending |
| ↳ 7 chapters, El Diablo, Devoiders | ⚠️ | Milestones + boss wiring + Aether gate exist; chapters/quests still design-only |
| **NPC / quest / RPG stack direction** | ⚠️ | Recommended external stack documented in `Docs/design/03-plugins/npc-quest-and-rpg-stack.md` (Citizens + Denizen + MythicMobs + AuraSkills); integration not implemented yet |
| **Survival spawn village (0,0) design** | ⚠️ | Design doc `Docs/design/05-worlds-gameplay/spawn-village.md` defines a protected Equator hub with class hall, calendar square, NPC roster, and lore boundaries; in-game build and NPC scripting not implemented yet |
| **Brotherhood / Epochians** | ❌ | Lore only; no in-code representation |
| **200% completion & disc reward** | ✅ | DiscRewardService, GiveDiscCommand, and advancement hook (writes COMPLETION_200_PERCENT) |
| ↳ Main Theme disc reward | ✅ | Grants automatically when all non-recipe advancements are complete |
| **New Year / century titles** | ⚠️ | Date title shows date; no dedicated "Happy New Year" title text yet |
| ↳ Firework display on New Year / century | ✅ | NewYearFireworkHandler at (0,0); scale by YEAR/DECADE/CENTURY/MILLENNIUM |
| **Calendar as internal clock** | ✅ | CalendarSyncManager, DayChangeListener |
| ↳ Leader/follower sync, DB persistence | ✅ | CalendarSyncManager, SyncDayChangeListener |
| **Equator / seasons (Z-based)** | ✅ | SeasonCalculator |
| ↳ Z > 500 / Z < -500, equator 500 | ✅ | Hemisphere and equator in SeasonCalculator |
| **Placeable calendar item** | ✅ | CalendarItemListener: right-click with configured item (e.g. Clock named "Calendar") opens book with date/season |
| ↳ 1×1 block, book UI for current date | ⚠️ | Item right-click opens book; placeable 1×1 block still needs texture/datapack |

---

## 2. Server infrastructure

| Item | Status | Notes |
| --- | --- | --- |
| **Paper, BungeeCord, Velocity, VPS** | ⚠️ | Plugin code assumes proxy/backend setup; not in repo |
| ↳ Paper 1.21 backend | ✅ | Built and tested against Paper API |
| ↳ BungeeCord/Velocity proxy | ⚠️ | Portals use BungeeCord messenger; proxy itself external |
| **Ubuntu, hardware, RAID, backup** | ⚠️ | Ops; not in repo |
| **MySQL (calendar, portals, clans)** | ✅ | CalendarSyncManager, DatabaseManager, ClanManager |
| ↳ Calendar table, leader sync | ✅ | CalendarSyncManager |
| ↳ Portals, pending_portals, portal_transfer_entities | ✅ | DatabaseManager (entity data for cross-server transfer) |
| ↳ Clans, alliances, clan_wars tables | ✅ | ClanRepository, AllianceRepository, WarRepository |
| **HuskSync** | ❌ | External plugin; not in repo |
| **Lobby server** | ✅ | Paper backend at Server/servers/lobby/ (port 25568); proxy default so new players join lobby first |
| ↳ Lobby plugin (LostWilderness-Lobby) | ✅ | /verify, teleporter (END_GATEWAY or config) to Survival; VerificationRepository (pending_verification, linked_accounts); BungeeCord connect |
| **Discord verify bot** | ✅ | DiscordBot/ (Node.js + TS); slash /verify &lt;mc_username&gt;, /players (per‑server + total player counts); sets nickname to IGN; same MySQL as lobby |
| **Legacy LinkApproval flow** | ❌ | Removed in favor of the single lobby `/verify` + Discord `/verify &lt;mc_username&gt;` flow |
| **Domains, launch** | ⚠️ | Ops; not in repo |

---

## 3. Plugins & modules

### Calendar

| Item | Status | Notes |
| --- | --- | --- |
| **Commands** | | |
| ↳ /date | ✅ | DateCommand; current in-game date |
| ↳ /time | ✅ | TimeCommand; in-game clock time |
| ↳ /datejoined | ✅ | DateJoinedCommand; join day and days ago |
| ↳ /season | ✅ | SeasonCommand; day, year, Z, hemisphere, season at location |
| ↳ /resetcalendar | ✅ | Survival only; ResetCalendarCommand |
| ↳ /nextday | ✅ | NextdayCommand (lw.admin.calendar); also /lwdebug nextday |
| ↳ /eoc (days since Epoch) | ✅ | EocCommand; Survival and Amplified |
| ↳ /calender stats | ✅ | Via /lwdebug stats (current day, leader/follower, DB status) |
| **Date logic** | | |
| ↳ Format "1st January 1MC", Epoch (Day 0) | ✅ | DateCalculator, configurable format |
| ↳ MySQL sync (leader + polling 30–60s) | ✅ | CalendarSyncManager, SyncDayChangeListener |
| ↳ Config persistence, single-server | ✅ | CalendarServiceImpl, config.yml |
| **Bug fixes / robustness** | | |
| ↳ Config folder on first run | ✅ | ConfigHelper.getConfig() creates data folder if missing |
| ↳ /timeset affecting calendar | ✅ | DayChangeListener advances only when mcDay > lastMcDay |
| ↳ New day title (missing 1/7) | ✅ | Per-player title in try/catch so one failure doesn't skip others |

### Portals

| Item | Status | Notes |
| --- | --- | --- |
| **Frame & activation** | | |
| ↳ Crying Obsidian frame, fire | ✅ | PortalListener, TeleportListener, portal logic |
| ↳ 1:1 coords (no 8:1) | ✅ | Portal logic; Paper config separate |
| ↳ Air pocket when frame encased | ✅ | Portal creation logic |
| **Limits & storage** | | |
| ↳ 5 pairs per player, minimum spacing | ✅ | Config-driven (portal limit) |
| ↳ MySQL tables (portals, pending_portals) | ✅ | DatabaseManager, portal storage |
| **Cross-server** | | |
| ↳ Bungee Messenger | ✅ | Used for cross-server teleport; `target-server` in config must match proxy server names (e.g. `amplified-1`, `survival-1`) |
| ↳ Survival/Amplified backends | ⚠️ | Single `portals` module; both server types use same common + portals |
| **Commands** | | |
| ↳ /portals | ✅ | PortalsCommand; list linked portals |
| ↳ /deleteportals | ✅ | DeletePortalsCommand |
| **Entity passthrough** | | |
| ↳ Mount (e.g. horse) with player | ✅ | PortalTeleportHelper (same-server); cross-server via portal_transfer_entities |
| ↳ Passengers (e.g. boat with player) | ✅ | Root vehicle teleported; cross-server serialized and respawned on other server |
| ↳ Happy Ghasts / following mobs | ⚠️ | Only transferable types (horse, boat, pig, strider, minecarts); add more in PortalEntitySerializer if needed |
| ↳ Cross-server entity transfer | ✅ | PortalEntitySerializer, PortalEntitySpawner, DB table portal_transfer_entities; save on source, spawn on join at destination |

### Lobby

| Item | Status | Notes |
| --- | --- | --- |
| **Lobby plugin** | ✅ | Plugin/lobby; LostWilderness-Lobby-all.jar |
| ↳ /verify (in-game) | ✅ | VerifyCommand; inserts pending_verification; tells player to run /verify in Discord |
| ↳ Teleporter to Survival | ✅ | LobbyTeleportListener; step on block (default END_GATEWAY); BungeeCord connect only if linked_accounts has player |
| ↳ pending_verification, linked_accounts | ✅ | VerificationRepository; same MySQL as Discord bot |
| **Discord verify bot** | ✅ | DiscordBot/; /verify &lt;mc_username&gt;, /players; set nickname to IGN |

### LWEvents (Eclipse, Fog, Frost, Blizzard, Heatwave, etc.)

| Item | Status | Notes |
| --- | --- | --- |
| **Events** | | |
| ↳ EclipseEvent | ✅ | Chance + interval; sleep blocked; resource pack |
| ↳ FogEvent | ✅ | ProtocolLib view distance; sleep blocked |
| ↳ BlizzardEvent | ✅ | BlizzardMoveListener, Strays, freeze |
| ↳ FrostEvent | ✅ | FrostMoveListener, cold biomes, heat sources |
| ↳ SummerHeatwaveEvent | ✅ | Husks, drying crops/farmland |
| ↳ ThunderEvent | ✅ | Thunderstorm, zombie horses on eclipse day |
| ↳ ParanoiaEvent | ✅ | Eclipse atmosphere: randomized horror sounds; starts via Eclipse cascade |
| ↳ EasterWeekEvent | ✅ | Easter week titles (Gregorian Easter) |
| ↳ JungleMonsoonEvent | ✅ | In events impl |
| ↳ SeasonalStormEvent | ✅ | In events impl |
| ↳ SpringBloomEvent, AutumnLeafFallEvent | ✅ | In events impl |
| **Support** | | |
| ↳ NaturalHordeSpawnerTask | ✅ | Horde spawner |
| ↳ Sleep blocking (eclipse, thunder, fog) | ✅ | SleepBlocker |
| ↳ /lwdebug (start/end per event, setday, nextday, stats) | ✅ | LWDebugCommand |
| ↳ Event cascades | ✅ | EventCascadeRegistry + EventTrigger; Eclipse triggers child events (Thunderstorm Eclipse, Paranoia) |
| ↳ Crash-safe block restore | ✅ | BlockRestoreManager; Blizzard/Frost persist block edits to block_restore.yml and restore on startup |
| ↳ Mob buffs on natural spawns | ✅ | EventMobBuffsListener; buffs NATURAL spawns during Eclipse/Winter (configurable) |
| ↳ Event boss bar | ✅ | EventBossBarManager shows active events (isActive() detection) with per-player toggle |
| ↳ TPS guard | ✅ | events.pause-if-tps-below skips event dispatch when TPS is below threshold |
| ↳ Event cooldowns | ✅ | events.cooldowns.* day-based cooldowns to prevent back-to-back repeats |
| ↳ Player event preferences | ✅ | MySQL player_event_prefs + /event toggle + /event togglebar |

### Clans

| Item | Status | Notes |
| --- | --- | --- |
| **Clan commands** | | |
| ↳ /clan create | ✅ | ClanCommand |
| ↳ /clan invite, accept, deny | ✅ | ClanCommand |
| ↳ /clan leave | ✅ | ClanCommand |
| ↳ /clan color | ✅ | ClanCommand |
| ↳ /clan promote, demote | ✅ | ClanCommand |
| ↳ /clan enemy, opposition | ✅ | ClanCommand |
| ↳ /clan info [name] | ✅ | ClanCommand |
| ↳ /clan list | ✅ | ClanCommand; ClanRepository.getAllClans(), ClanManager.getAllClans() |
| ↳ /clan reside | ❌ | Not in code; design meaning (e.g. set clan home) TBD |
| **Clan backend** | | |
| ↳ ClanManager, MySQL | ✅ | ClanManager, ClanRepository |
| ↳ BotApiClient (Discord roles) | ✅ | ClanDisplayListener; role sync when config present |
| **Alliance** | | |
| ↳ /alliance create | ✅ | AllianceCommand; AllianceRepository, alliances table |
| ↳ /alliance invite | ✅ | alliance_invites table; leader/admin can invite |
| ↳ /alliance join | ✅ | Accept pending invite; add to alliance_members |
| ↳ /alliance leave | ✅ | Remove clan from alliance; disband if empty |
| ↳ /alliance info [name] | ✅ | List clans in alliance and ranks |
| ↳ /alliance list | ✅ | List all alliances and clan counts |
| **War** | | |
| ↳ War state (which clan pairs, status) | ✅ | WarRepository, clan_wars table (clan_id_1, clan_id_2, status, started_at) |
| ↳ /war declare (clan) | ✅ | WarCommand; permission lw.clan.war |
| ↳ /war ceasefire (clan) | ✅ | WarCommand; set status to ceasefire |
| ↳ /war end (clan) | ✅ | WarCommand; set status to ended |
| ↳ /war status | ✅ | List current clan's wars and status |
| ↳ War day subtitle (when war starts) | ❌ | Not in code |
| ↳ War music | ❌ | Not in code |
| ↳ Head drops (war) | ✅ | WarHeadDropListener: 1/1000 in war, 1/10000 otherwise; victim head dropped at death location |
| ↳ Discord war webhook | ✅ | DiscordWarNotifier; webhook on declare/ceasefire/end (config discord.war-webhook-url) |
| ↳ Warzone radius / PvP rules | ❌ | Not in code |
| **Other** | | |
| ↳ /nomad | ❌ | Not in code |
| ↳ Devoider/El Diablo kill counts per clan | ✅ | BossKillRepository & BossKillTracker (V2) |
| ↳ Reputation system | ✅ | ReputationModule (V2) |

### RoofWither / boss

| Item | Status | Notes |
| --- | --- | --- |
| **Devoider / El Diablo** | | |
| ↳ RoofWitherListener (6 withers, 6 Devoiders) | ✅ | BossModule (V2); transforms Withers on roof; arena barriers |
| ↳ BossAbilityListener, BossDropListener | ✅ | Ported to BossModule (V2) |
| **Arena & boundaries** | | |
| ↳ BossArena, ArenaManager, Boundary | ✅ | BossModule (V2) models and service |
| ↳ ArenaBoundaryListener (restrict movement) | ✅ | Enforced sandbox with particles |
| **Commands** | | |
| ↳ /arena-test | ✅ | ArenaTestCommand (V2) |
| ↳ /diablo-lair | ✅ | DiabloLairCommand (V2) |
| **Reputation / Honor** | ✅ | ReputationModule (V2); Red Dead style Honor (-1000 to +1000); Good/Bad factions |

### Lobby / queue / resource pack

| Item | Status | Notes |
| --- | --- | --- |
| DefaultPackListener (force pack) | ✅ | DefaultPackListener |
| PackStatusListener | ✅ | PackStatusListener |
| VoidWorldGenerator, DeluxeMenus, queue plugin | ❌ | Not in Plugin repo (separate lobby server) |
| PackServer /packs/*.zip | ❌ | Not in code; config-driven pack URL |

### Personality & Elemental Quest System (PluginV2)

| Item | Status | Notes |
| --- | --- | --- |
| **Personality Quiz (Lobby)** | ✅ | 10-question quiz on first join; assigns trait + element (hidden until revealed) |
| ↳ QuizSessionManager, QuizCompletionHandler | ✅ | Quiz flow with movement freeze (SLOWNESS + JUMP_BOOST); chat input; tie-breaking |
| ↳ QuizTriggerListener | ✅ | Auto-starts on first join; checks quiz completion before portal entry |
| ↳ 13 Personality Traits | ✅ | MAGE, WARRIOR, ARCHER, HEALER, RANGER, ALCHEMIST, SMITH, SCOUT, BERSERKER, SAGE, TAMER, RUNEKEEPER, ILLUSIONIST |
| ↳ 5 Elements | ✅ | FIRE, EARTH, WIND, WATER, AETHER; each with god-tier passive after temple completion |
| **Trait Progression (4 Tiers)** | ✅ | APPRENTICE (0%), TRAIT (25%), MASTER (50%), ULTIMATE (100%) |
| ↳ CompletionService (0-300%) | ✅ | Story milestones (0-100%), vanilla advancements (100-200%), elemental temples (200-300%) |
| ↳ Auto tier advancement | ✅ | TraitService.checkAndAdvanceTier() on milestone unlock; sends title + message |
| ↳ Ultimate item grant at 100% | ✅ | TraitService.grantUltimateItem(); creates item with max enchants + trait enchant |
| **Element System** | ⚠️ | Element assigned in quiz (hidden); revealed by Lord command; activated after first temple |
| ↳ Element reveal by Lords | ✅ | TraitService.revealElement(); sends reveal message |
| ↳ Temple completion tracking | ✅ | TraitService.completeTemple(); 5 temples (FIRE, EARTH, WIND, WATER, AETHER) |
| ↳ Post-game state (all 5 temples + 300%) | ✅ | TraitService.isPostGame(); unlocks +50% event rewards + Christmas enchants |
| ↳ Elemental passives (god-tier) | ✅ | ElementalPassiveListener: FIRE (fire immunity), EARTH (suffocation immunity), WIND (elytra durability), WATER (underwater breathing), AETHER (fall damage reduction) |
| **Trait Passive Abilities** | ⚠️ | TraitPassiveListener: Core abilities implemented (MAGE potion AoE, WARRIOR melee damage, ARCHER projectile damage, HEALER potion duration, RANGER forest speed, BERSERKER low HP strength, SAGE XP boost); advanced mechanics TODOed |
| **Ultimate Items** | ⚠️ | TraitItemListener: Homing arrows (MAGE/ARCHER), Shadowstep blink (SCOUT), Eternal Hammer repair (SMITH), Tome XP (SAGE), Flask random potion (ALCHEMIST) implemented; others TODOed |
| ↳ Custom items with max enchants | ✅ | TraitServiceImpl.createUltimateItem(); includes trait enchant via PDC |
| ↳ Holy Enchant system (PDC-based) | ✅ | HolyEnchantService; trait + holy enchants stored in PersistentDataContainer |
| ↳ Blessing mechanic (Lord command) | ✅ | TraitService.blessItem(); doubles enchant levels + adds random Holy Enchant |
| ↳ 13 Holy Enchants | ⚠️ | HolyEnchantEffectListener: SOULFIRE (fire damage), DIVINE_SHIELD (absorption), CELESTIAL_STRIKE (lightning), VOID_PIERCE (armor pen), NATURES_GRASP (root), THUNDERCLAP (knockback), SERPENTS_FANG (poison), ANCIENT_WARD (damage reduction), TITANIC_FORCE (% HP damage) implemented; 4 TODOed |
| ↳ Ultimate item special mechanics | ⚠️ | See Ultimate Items row above |
| **Christmas Mechanic** | ✅ | TraitService.grantChristmasEnchants(); 9 random Holy Enchants to 300% players on Dec 25 |
| **Database Tables** | ✅ | player_traits, player_temples, player_quiz_answers, player_holy_enchants, player_christmas_claims |
| **Commands** | ✅ | TraitCommand with 6 subcommands: info [player], revealelement <player> (Lord), bless <player> (Lord), temple <element> (Admin), set <trait> (Admin), quiz (Admin stub); TraitTabCompleter; registered in SurvivalV2Plugin |
| **Integration Hooks** | ✅ | ProgressionJoinListener.checkTierAdvancement() after milestone unlocks; /datejoined displays trait/element/completion; EventServiceImpl.getRewardMultiplier() for post-game bonus (1.5x) |

### Class Skill Tree System (PluginV2)

| Item | Status | Notes |
| --- | --- | --- |
| **Core Data Models (Phase 1)** | ✅ | ClassUnlock record (level, name, description, type), ClassUnlockStatus (unlock + locked status + helper methods), ClassSkillTreeService interface (10 methods) |
| **Service Implementation (Phase 2)** | ✅ | ClassSkillTreeServiceImpl with 5 class unlock trees (7 levels each: 1/5/10/15/20/30/50), AuraSkills integration for mastery levels, Sanctified Ground cooldown tracking (10 min, in-memory), Ultimate item granting (level 50) |
| ↳ 5 PlayerClasses | ✅ | CELESTIAL_TEMPLAR (templar_mastery), WILDLAND_RANGER (ranger_mastery), REDEEMED_ARTIFICER (artificer_mastery), CORRUPTED_CULTIST (cultist_mastery), DESTROYER_BERSERKER (berserker_mastery) |
| ↳ 35 Total Unlocks | ✅ | 7 per class: abilities (active), passives, upgrades, Ultimate items at level 50 |
| ↳ Class Mastery Items | ✅ | UltimateItemBuilder: Holy Avenger (diamond sword), Ranger's Quiver (leather chestplate), Eternal Hammer (netherite pickaxe), Mirror Shard (ender pearl), Ragnarok Axe (netherite axe); PDC-stored (`lw:class_mastery_enchant`) |
| ↳ Database (class_ultimate_items) | ✅ | PlayerClassRepository extended with hasUltimateItem(), markUltimateItemGiven(); tracks (player_uuid, class_name, granted_at) |
| **XP System (Phase 3)** | ✅ | ClassMasteryXpListener with 7 event handlers (EntityDeath, PlayerDeath, EntityTame, CraftItem, PrepareAnvil, EntityDamage); async class checks via grantXpIfClass(); public hooks for ability/lifesteal XP |
| **Listener Integration (Phase 4)** | ✅ | ClassAbilityListener: level-scaled abilities (Smite 1/2/3 targets, Slam radius/slowness, Dash range, War Cry Strength buff), XP grants after activation; ClassPassiveListener: level gates (Lifesteal Lv5→Lv30 Soul Drain, Berserker's Rage Lv5), XP on lifesteal proc |
| **Module Wiring (Phase 5)** | ✅ | ClassesModule: ClassSkillTreeServiceImpl created in onLoad(), ClassMasteryXpListener created in onEnable(), dependencies injected to listeners, all registered as event handlers |
| **Commands (Phase 6)** | ❌ | SKIPPED - Using AuraSkills /skills GUI instead of separate /class tree command |
| **Database Migrations (Phase 7)** | ✅ | V007__class_ultimate_items.sql migration script created; table with player_uuid, class_name, granted_at + indexes |
| **Config & Docs (Phase 8)** | ✅ | skills.yml created for AuraSkills (5 custom mastery skills with sources/abilities); config/core.yml updated (starter XP + mastery-skills mapping); SKILLS_INSTALLATION.md guide created |
| **AuraSkills Integration** | ✅ | 5 custom skills appear in /skills GUI (templar_mastery, ranger_mastery, artificer_mastery, cultist_mastery, berserker_mastery); XP auto-tracked by AuraSkills + custom listeners; 7 abilities per skill displayed at levels 1/5/10/15/20/30/50 |
| **Design Docs** | ✅ | v2/05-planning/plan-class-skill-tree.md (complete 515-line plan with specs, risks, testing, XP curves); SKILLS_INSTALLATION.md (installation + customization guide) |
| **Overall Progress** | ✅ | 88% (14/16 tasks); Phases 1-5, 7-8 COMPLETE; Phase 6 SKIPPED; System production-ready with AuraSkills integration |

### Party System (PluginV2)

| Item | Status | Notes |
| --- | --- | --- |
| **Core Party Module (Phase 1)** | ✅ | Party model with id, leader, name, members, invites, maxSize, createdAt, color; mutation/query methods |
| ↳ PartyRepository | ✅ | Async DB operations via "player" datasource; 3 tables (party, party_member, party_invite); MySQL + H2 support; cascade delete; indexes on player lookups |
| ↳ PartyService interface | ✅ | 14 methods: createParty, disbandParty, invitePlayer, acceptInvite, leaveParty, kickMember, getParty, getPartyMembers, isInParty, areInSameParty, getPlayerInvites, display methods (stubs for Phase 3) |
| ↳ PartyServiceImpl | ✅ | In-memory cache (ConcurrentHashMap); sync wrappers over async repo (10s timeout); auto-creates party on first invite; leader leaving = disband; kick validation; online player notifications |
| ↳ PartyCommand | ✅ | /party with 7 subcommands: create [name], invite <player>, accept <player>, leave, disband, info, kick <player>; tab completion (online players for invite, pending inviters for accept, party members for kick) |
| ↳ PartyModule | ✅ | Standard module pattern; player datasource validation; async DB executor; table creation; service registry; command registration (JavaPlugin cast); registered in RPGCorePlugin |
| ↳ party.yml config | ✅ | max-size: 6, color: #00FF00, display settings (prefix, glow), combat settings (friendly fire, party buff damage multiplier) |
| ↳ plugin.yml + core.yml | ✅ | /party command registered with lw.party.use permission; "party" module added to enabled-modules list |
| **Cross-Server Sync (Phase 2)** | ✅ | Cluster messaging integration (party/sync, party/disband channels); JSON serialization/deserialization (manual, no external libs); PartySessionListener (PlayerQuitEvent → leave, PlayerJoinEvent → reload from DB); portal transfers preserve party; database polling fallback (5s interval) for stubbed ClusterMessagingService; partiesEqual() change detection |
| **Visual Display & Combat (Phase 3)** | ✅ | PartyDisplayListener (scoreboard teams with [Party] prefix, party color, glow effect, cross-player visibility); PartyFriendlyFireListener (EntityDamageByEntityEvent HIGH priority, direct + projectile damage cancellation); PartyBuffListener (damage boost formula: 1.0 + 0.05 * (size - 1), applies to attacks); PartyServiceImpl display delegation via reflection; all features config-controlled |
| **Service Integration (Phase 4)** | ✅ | BossKillTracker party credit (all members within 64 blocks); PortalEnterListener party notification (formatted messages to all members); EventServiceImpl.meetsPartyRequirement() + applyBuffToParty(); EventService interface updated; ProgressionService.unlockForParty() + incrementCounterForParty() for shared objectives; all integrations use fully qualified class names |
| **Module Registration & Testing (Phase 5)** | ✅ | PartyModule verified registered in RPGCorePlugin (lines 11, 130-132); comprehensive testing documentation created (PARTY_PHASE_5_COMPLETE.md): Phase 1 core ops, Phase 2 cross-server sync, Phase 3 visual & combat, Phase 4 service integration, 10 edge cases, performance guidelines, regression protocol, known issues, deployment checklist; all 10 success criteria verified |
| **Overall Progress** | ✅ | 100% (Phases 1-5/5 COMPLETE); All party features functional and tested. Cross-server support verified. Service integrations deployed. Comprehensive testing documentation created. Build status: SUCCESSFUL |

### Other

| Item | Status | Notes |
| --- | --- | --- |
| LWHelpCommand, LWPluginsCommand, LWConfigCommand | ✅ | /lwhelp, /lwplugins, /lwconfig |
| Multichat, Clearlagg, Multiverse, Harbor | ❌ | External plugins |
| Geyser, Floodgate, BungeeGuard | ❌ | Infrastructure / external |
| Datapacks (build height, advancements) | ⚠️ | Not in Plugin repo; separate assets |
| Difficulty Hard | ⚠️ | Server config |

---

## 4. Resource packs & client assets

| Item | Status | Notes |
| --- | --- | --- |
| **Bedrock / Geyser** | | |
| ↳ GeyserPackConverter, Bedrock manifest | ❌ | External tool / docs in Bible |
| ↳ Geyser config (resource-packs) | ❌ | Server config |
| **Packs** | | |
| ↳ Default pack (force on join) | ✅ | DefaultPackListener; config-driven URL |
| ↳ Eclipse red moon pack (force during event) | ⚠️ | Events config; pack URL in config |
| ↳ PackServer /packs/*.zip serving | ❌ | Config-driven pack URL; no built-in pack server in code |
| **Assets** | | |
| ↳ Logo, 64×64 icon, Discord assets | ❌ | Art/design; not in repo |
| ↳ Vertical slabs | ❌ | Not in plugin code |

---

## 5. Worlds & gameplay design

| Item | Status | Notes |
| --- | --- | --- |
| **Lobby** | ❌ | Separate server; not in Plugin repo |
| ↳ Void world, queue | ❌ | Not in Plugin repo |
| ↳ Chest GUI / DeluxeMenus | ❌ | Not in Plugin repo |
| **Creative** | ❌ | Not in Plugin repo |
| ↳ Plots, superflat | ❌ | Not in Plugin repo |
| **Survival** | ✅ | SurvivalMain registers calendar, events, clans, alliances, war, portals, commands |
| ↳ Calendar, events, clans, portals | ✅ | All wired in SurvivalMain |
| **Amplified** | ✅ | AmplifiedMain; same as Survival except no /resetcalendar |
| **Devoid dimension, Galactic Journey, Leaving the Firmament** | ❌ | Design only; no world/quest logic in code |
| **War (world/gameplay)** | | See also §3 Clans → War for commands and state. |
| ↳ War state & persistence (clan pairs, status) | ✅ | WarRepository, clan_wars table |
| ↳ /war declare, ceasefire, end, status | ✅ | WarCommand; lw.clan.war |
| ↳ War day subtitle (when war starts) | ❌ | Not in code |
| ↳ War music (lobby/warzone themes) | ❌ | No in-game music triggers in plugin |
| ↳ Head drops (war kills) | ✅ | WarHeadDropListener (1/1000 war, 1/10000 else) |
| ↳ Warzone radius / PvP rules | ❌ | Not in code |
| **Maps (updatable, transfer)** | ❌ | Not in code |
| **Hemisphere (Z), equator 500** | ✅ | SeasonCalculator; Z-based seasons and equator |

---

## 6. Technical decisions

| Item | Status | Notes |
| --- | --- | --- |
| Calendar leader + polling | ✅ | CalendarSyncManager, SyncDayChangeListener |
| Portal 1:1 coords, no 8:1 | ✅ | Portal logic; Paper config separate |
| Plugin placement (backend only, Bungee messenger) | ✅ | Portals/calendar in Survival/Amplified |
| Roadmap and centralised docs | ✅ | Docs/roadmap.md; all docs in Docs/ (see Docs/README.md) |

---

## 7. Audio & visual

| Item | Status | Notes |
| --- | --- | --- |
| **Music** | | |
| ↳ Lobby music theme | ❌ | No in-game music triggers in plugin |
| ↳ Warzone music theme | ❌ | Not in code |
| ↳ Eclipse music theme | ❌ | Resource pack / external; no plugin trigger |
| ↳ Eclipse 5-act theme (audio) | ❌ | Design only |
| **Discs & rewards** | | |
| ↳ Custom discs (Get Lost, per dimension) | ✅ | DiscRewardService; config discs.get-lost, discs.main-theme; /give-disc (lw.admin.disc) |
| ↳ 200% completion Main Theme disc | ✅ | giveDisc(REWARD_MAIN_THEME); call from advancement logic when 200% implemented |
| **Assets** | | |
| ↳ Logo, 64×64 icon | ❌ | Art/design; not in repo |
| ↳ Discord/advertising assets | ❌ | Art/design |

---

## 8. Open questions / TODO (from Bible)

| Item | Status | Notes |
| --- | --- | --- |
| **Calendar** | | |
| ↳ Config folder on first run | ✅ | ConfigHelper.getConfig() creates data folder |
| ↳ /timeset affecting calendar | ✅ | DayChangeListener only advances when mcDay > lastMcDay |
| ↳ New day title (missing 1/7) | ✅ | Per-player title in try/catch |
| **Portal** | | |
| ↳ Entity passthrough (mount, passengers) | ✅ | PortalTeleportHelper (same-server); cross-server via portal_transfer_entities |
| ↳ Cross-server entity transfer | ✅ | Serialize vehicle on source, store in DB, spawn on destination when player joins |
| **Bedrock** | | |
| ↳ /time /date namespace, lobby GUI fallback | ⚠️ | Geyser/Bedrock; not in plugin |
| **Other** | | |
| ↳ Build height, lobby datapack 77 | ❌ | Datapack / other repo |
| ↳ Vulcan, Clearlagg verify | ❌ | External |
| ↳ Release, future ideas (wind, tides, Thirst) | ❌ | Design / future |

---

## Documentation (this Docs folder)

- **Docs home:** [README.md](README.md) — Main documentation hub for players, developers, operators, design, and status pages.
- **Player handbook:** [player/README.md](player/README.md) — Join flow, servers, events, portals, clans, and verification.
- **Development:** [development/README.md](development/README.md) — Setup, architecture, testing, and contributor references.
- **Testing:** [development/test-guide.md](development/test-guide.md) — How to test every feature (calendar, events, clans, war, portals, cross-server entities, New Year fireworks, head drops, Discord webhook, calendar item, discs).
- **Operations & scaling:** [future/README.md](future/README.md) — Infrastructure, performance, security, backups, monitoring, and runbooks.
- **Design bible:** [design/README.md](design/README.md) — Full architecture and design intent from Discord and planning notes.
