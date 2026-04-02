---
title: Clan Governance System
description: Player-defined clan government types, courts, and internal law enforcement.
tags:
  - ideas
  - clans
  - governance
  - speculative
status: speculative
phase: phase-3
owner: design
action: needs-design
---
# Clan Governance System

An expansion of the existing clan system that allows large clans to develop complex, player-defined political and governmental structures. Small clans remain simple; mega-clans can grow into full empires with constitutions, courts, and military hierarchies.


## Government Types

Players choose their government type when founding or restructuring a clan:

| Type | Leader Title | Notes |
| --- | --- | --- |
| Empire | Emperor / Empress | Absolute rule, no elections |
| Kingdom | King / Queen | Hereditary or appointed |
| Democracy | President | Elected positions |
| Dictatorship | Supreme Leader / General | Military rule |
| Republic | Chancellor / Consul | Senate-style council |
| Custom | Player-defined | Any title, any structure |


## Clan Role Hierarchy (Example — Empire)

```
Emperor / Empress
  └── Prime Minister (second-in-command, administration)
       └── General (military branch head)
            └── Lieutenant (field commanders)
  └── Chief Magistrate (judiciary branch)
       └── Judges / Lawyers (represent in alliance courts)
  └── Police Commissioner
       └── Superintendent → Inspector → Constable
```

All roles are configurable per clan. The clan leader sets the structure; the database stores the hierarchy.


## Alliance Courts

Large clans or alliances can designate lawyers and magistrates to represent them in:
- Inter-clan dispute resolution
- Alliance treaty negotiations
- War crime / raid rule adjudication

This is fully player-run — the plugin provides the roles and titles, players conduct the proceedings.


## Constitutions

Large clans can write a constitution as a text document stored in the database and viewable in-game. This is purely cosmetic/flavour — no plugin enforcement, but it establishes clan law that members acknowledge on joining.


## Implementation Notes

- Clan roles/titles stored in database alongside existing clan data (`ClanService` / `ClanRepository`)
- Honor/title display in chat via `ClanService` formatting
- Police role triggers event listeners for base monitoring (WorldGuard region events?)
- Alliance court system may require a separate `AllianceModule` or extension of `ClansModule`

---

## Open Questions

- How are elections run for democratic government types? (Command vote? GUI?)
- Should constitutions be enforced in any way by the plugin, or purely cosmetic?
- What triggers a clan being "big enough" to unlock tiers of governance?
- Alliance court system — separate module or part of clans?
- Should clan government type affect war rules or just titles?
