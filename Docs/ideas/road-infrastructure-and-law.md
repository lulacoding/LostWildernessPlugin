---
title: Road Infrastructure & Law
description: Bitumen roads, NPC traffic enforcement, martial law, and civil war mechanics.
tags:
  - ideas
  - infrastructure
  - clans
  - speculative
status: speculative
phase: post-story
owner: dev
action: needs-design
---
# Road Infrastructure & Law System

Bitumen roads that players build from refined oil byproducts, NPC cops that actually pull you over, clan-defined traffic laws, and a civil war mechanic that turns internal clan conflict into a server event.


## NPC Traffic Law Enforcement

Players running around enforcing traffic laws themselves is not realistic — NPCs handle it.

### NPC Constable Behaviour
- Patrols road networks on a route set by the clan leader
- Performs **random stop checks** on players and vehicles
- If a player is carrying an **illegal item** (set by clan leader): pulls them over, initiates search, confiscates if required
- Issues citations or warnings for traffic violations in clan territory

### Pull-Over Interaction (Chat)
NPC constables communicate via chat with flavour text:
> "Pull over right there, criminal scum."

The system can be tuned per-clan — some clans meme it, some take it seriously. The mechanic is the same.

### Illegal Items System

Clan leaders can designate specific items as **illegal for visitors** (or even for citizens) to carry:
- Examples: end crystals, wither skulls, nuclear devices, unlicensed weapons
- Anyone found carrying an illegal item during a stop check is flagged
- A **license system** allows certain players to carry normally-illegal items (e.g. licensed nuclear transport)

> "No carrying a nuclear bomb unless you've got a license and special permission."


## Civil War & Revolt System

### Revolt Mechanic

When a player or group triggers a revolt against their clan:
1. The revolt action **creates a new clan** from the revolting players
2. An **immediate civil war** is declared between the original clan and the breakaway clan
3. All members of both clans are flagged as combatants in the civil war
4. A **Discord notification** fires automatically:
   > "Clan [Breakaway] has broken off from Clan [Original] and are now at civil war."

### Why This Works

- Gives internal clan conflict a formal, game-sanctioned outlet
- Civil wars are self-contained — they don't drag in unrelated clans unless alliances trigger
- Creates natural drama and narrative that the server broadcasts to the community
- The breakaway clan starts from Tier 12 (citizen rank) and has to rebuild its hierarchy from scratch
- The original clan loses members but keeps its structure and assets

### Escalation Path

A civil war can escalate via alliances:
- Original clan calls in allied clans → allied clans may declare war on the breakaway
- Breakaway seeks protection from a rival clan → rival clan enters conflict
- Ties into the full alliance/IGO system (see [Space travel and multiverse endgame](space-travel-and-multiverse-endgame.md))


## Integration Points

| System | Connection |
| --- | --- |
| NPC automation | Constables are recruited NPCs on patrol routes |
| Clan hierarchy tiers | Martial law converts Tier 9 constables → Tier 11 privates |
| Vehicle system | Traffic laws apply to all vehicles; road blocks are built from vehicle fuel byproduct |
| Alliance system | Civil wars can trigger alliance chain reactions |
| Discord bot | Revolt/civil war notifications fire to Discord automatically |

---

## Open Questions

- How is the illegal items list managed — in-game GUI for clan leader, or a command?
- Does a license for illegal items expire or is it permanent?
- What stops a clan leader from declaring martial law permanently?
- Civil war duration — does it end on a kill count threshold, territory capture, or player negotiation?
- Should the revolt action have a "cooldown" to prevent griefing via repeated revolts?
- Are road blocks vehicle-specific (smoother/faster driving on asphalt vs dirt) or purely cosmetic?
