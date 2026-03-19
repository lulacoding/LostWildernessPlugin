# Migration from V1

Mapping from current (V1) plugins and classes to PluginV2 modules. Fill in as you port.

---

## Plugin → Module

| V1 plugin / area | V2 module | Notes |
|------------------|-----------|--------|
| common (player data, calendar sync, etc.) | player, calendar, progression, … | Split by domain; no single “common” god-object. |
| survival / amplified | Same jar; server-role + enabled-modules in config | No separate JARs; one core jar. |
| calendar | plugin-calendar (CalendarService) | Leader/follower; no CalendarSyncManager monolith. |
| events | plugin-events (EventEngine, WorldEvent) | EventRegistry + EventCascadeRegistry. |
| clans | plugin-clans (ClanService, WarService, etc.) | |
| portals | plugin-portals (PortalService, PortalEngine) | FrameDetector, PortalResolver, TransferCoordinator. |
| lobby | Same jar; lobby server-role, subset of modules | |

---

## Class / concept mapping

*(To be filled as you port. Example:)*

| V1 class / concept | V2 equivalent |
|--------------------|----------------|
| CalendarSyncManager | CalendarService + EventEngine + messaging |
| Milestone (common) | ProgressionService + AchievementKey |
| Wallet / economy in common | EconomyService / WalletService + WalletRepository |
| TeleportListener (portals) | PortalEngine + listeners delegating to PortalService |

---

## Data migration

- Prefer **new V2 tables** (e.g. `player_profiles`, `player_achievements`) and optional one-off scripts to copy from V1 tables if needed.
- Document any schema differences and migration steps here or in implementation-status.md.
