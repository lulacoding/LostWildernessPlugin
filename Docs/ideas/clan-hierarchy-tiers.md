---
title: Clan Hierarchy Tiers
description: Numbered tier system (0-13+) with military branches and empire governance.
tags:
  - ideas
  - clans
  - speculative
status: speculative
phase: phase-3
owner: design
action: needs-design
---
# Clan Hierarchy Tiers

A numbered tier system for clan roles, scaling from basic NPC labour at the bottom to clan creator at the top. The hierarchy is entirely **optional and player-driven** — you can sit at the bottom and never engage with it, or build an empire with a constitution, generals, and governors.


## Military Branch Specialisations (Tier 5)

Generals choose a branch when promoted:

- **Air Force** — aerial vehicles, air superiority, bombing runs
- **Space Force** — star jets, space exploration, orbital assets
- **Cyber Force** — redstone/tech warfare, network attacks, signal jamming

At Tier 4, the **General of the Armies** commands all branches.


## Multi-base Empire (Tier 3 Governors)

When a clan controls **multiple bases**, it becomes an empire in function. Tier 3 unlocks:
- **Governors** — each base gets an assigned governor/lord who manages internal policy
- **Capital city** — one base is designated the capital; clan creator or co-leader governs from there
- This mirrors a real federal/imperial structure: local governors report up to the central government


## The Voluntary Principle

> "Just because the hierarchy exists doesn't mean you need to enforce it."

The tier system is **player-driven**, not mandatory:
- Want to just fight wars and explore? Sit at Tier 12 as a citizen and do exactly that
- Government building, law enforcement, and military command are each their own **skill sets**
- There is something for every play style — solo explorer, soldier, politician, judge, builder
- The server doesn't force anyone up the ladder; it just makes the ladder available

---

## Scope / Production Note

The full hierarchy requires:
- Tier/role data stored in the clan database (`ClanRepository`)
- Chat title/prefix formatting per tier (`ClanService`)
- Permission nodes or role-gate checks for tier-locked commands
- Election/voting system for democratic government types (future module)
- Governor assignment system tied to WorldGuard regions or custom base regions

See also: [Clan governance system](clan-governance-system.md) for the broader governance design, [NPC automation system](npc-automation-system.md) for how NPCs fill Tier 13.
