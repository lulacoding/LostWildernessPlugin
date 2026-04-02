---
title: NPC Automation System
description: Recruitable NPCs for mundane tasks and AI-automated endgame infrastructure.
tags:
  - ideas
  - npc
  - speculative
status: speculative
phase: phase-3
owner: dev
action: needs-dev
---
# NPC Automation System

A two-phase system: early-game **recruitable NPCs** that handle mundane base tasks, scaling with real player count; late-game **AI-automated vehicles and infrastructure** unlocked after completing the main story.


## Phase 2 — AI-Automated Vehicles & Infrastructure (Post-Story Endgame)

Unlocked after completing the main story arc. Requires:
- **Cell tower network** built and active at the base
- **Redstone computer** components (in-game tech progression)
- **Alien tech** obtained through story progression

### How It Works

1. Cell towers cover a mapped area of the base
2. First activation: the system **maps all roads and paths** within cell range (one-time per base)
3. This map data is shared across all automated units — map once, use everywhere
4. Automated units operate only within the cell tower range

### Automated Unit Types

| Unit | Role | Notes |
| --- | --- | --- |
| Auto-driver vehicle | Transport goods between points | Range-limited to cell coverage |
| Robocop | Patrol, report, enforce base rules | Uses same map data as all others |
| Auto-mailman | Item delivery within base | Replaces NPC mailman role at scale |
| Auto-sanitation | Clear dropped items, manage waste bins | Cosmetic but satisfying |

### Per-Clan Vehicle Cap

- Automated vehicles are **resource-intensive** (AI pathfinding is expensive)
- Hard cap on simultaneous automated vehicles per clan (exact number TBD — suggest 3–5 at launch)
- All units share the same underlying map data, so the cost is the pathfinding, not the mapping

### Player Role Shift (Endgame Fantasy)

> Before automation: players ARE the constables, mailmen, and garbos.
> After automation: NPCs and AI handle private/constable rank. Players become superintendents, commissioners, generals.

The fantasy is that the megabase eventually runs itself at the routine level, freeing players to focus on high strategy, war, politics, and story content.


## Integration with Clan Governance

See [Clan governance system](clan-governance-system.md):
- NPCs fill constable/private/logistics ranks in large clans
- Players promoted to superintendent/commissioner/general as NPCs handle lower ranks
- NPC cap tied to clan's real-player count (enforced in `ClansModule`)


## Open Questions

- Which Citizens traits (or custom solution) best model hunger/sleep/thirst?
- How does the cell tower "map" data get stored — schematic? Region snapshot? Custom block data?
- Should automated vehicles be actual entities (Minecart/custom vehicle) or just particle-trail + item transport?
- At what story milestone is Phase 2 unlocked — final boss? Post-credits?
- How is the NPC cap enforced when a clan's player count drops below a threshold?
