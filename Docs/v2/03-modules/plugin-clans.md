# Plugin: Clans

Clans, alliances, and war.

## Purpose

- Clan CRUD, membership, roles, permissions.
- Alliances and relations.
- War declarations, state, outcomes (e.g. head drops, notifications).

## Main classes

| Class | Responsibility |
|-------|----------------|
| `ClanService` | Create/join/leave clan; membership and roles. |
| `AllianceService` | Alliance create/manage/break. |
| `WarService` | Declare war, track state, resolve outcomes. |
| `ClanRepository`, `AllianceRepository`, `WarRepository` | Async persistence. |
| `ClanDisplayAdapter` | UI/listener layer separated from service (e.g. tags, scoreboard). |

## Config

- `config/clans.yml` – Limits, war rules, head drop behaviour, etc.

## DB

- Clan, membership, alliance, and war tables (schema TBD; can mirror V1 concepts with cleaner boundaries).

## Integration

- Boss/event listeners call ClanService/WarService for rewards or notifications; Discord bot or other consumers use the same services.
