# Roadmap - V1 Plugin & Project-Level Priorities

> **V2 Build Phases:** For the V2 rewrite build roadmap, see [v2/05-planning/roadmap.md](v2/05-planning/roadmap.md)

This is the prioritized execution plan for Lost Wilderness. It should reflect reality: what matters most next, what can follow after that, and what belongs in longer-term vision rather than immediate execution.

For current feature truth, use [implementation-status.md](implementation-status.md). For infrastructure planning detail, use [future/README.md](future/README.md).

---

## Now

These are the highest-value items to tackle next because they improve reliability, trust, and day-to-day operability.

| Priority | Area | Why It Matters | Where To Work |
| --- | --- | --- | --- |
| 1 | Network and proxy hardening | Protects the server topology and prevents backend exposure | [Network and security](future/network-and-security.md) |
| 2 | Database and backups | Reduces the biggest operational failure risk | [Database architecture and tuning](future/database-architecture-and-tuning.md), [Backups and disaster recovery](future/backups-and-disaster-recovery.md) |
| 3 | Monitoring and alerting | Makes crashes, DB issues, and TPS problems visible early | [Monitoring, logging, and alerting](future/monitoring-logging-and-alerting.md) |
| 4 | Load testing and safe capacity numbers | Replaces guesswork with measured player limits | [Load testing and capacity planning](future/load-testing-and-capacity-planning.md) |
| 5 | Alliance and clan polish | Clans exist, but the broader social/political layer is still incomplete | [Implementation status](implementation-status.md#clans--war), [Clans and war](design/03-plugins/clans-and-war.md) |

---

## Next

These items are the next layer after core stability and operations are under control.

| Area | Goal | Notes |
| --- | --- | --- |
| Lobby and queue ecosystem | Decide what stays external vs enters this repo | Void world, GUI flow, queue logic, and pack acceptance experience |
| Resource-pack delivery | Clean up Java/Bedrock pack delivery and fallback behavior | Especially important for Geyser/Floodgate compatibility |
| War experience polish | Add better war feedback beyond the minimal command layer | Music, warzone radius, UX feedback, clan-facing status |
| Multi-instance routing | Prepare for multiple lobby/survival/amplified instances | Naming, routing, and "return where you left off" behavior |
| Docs quality and support flow | Keep docs aligned with implementation after each major feature | Status, player guides, tests, and changelog should stay synchronized |

---

## Later

These are legitimate goals, but they should not outrank reliability and core system completion.

| Area | Future Direction |
| --- | --- |
| Creative, Devoid, and Galactic Journey | Expand the network into more backends and post-game content |
| Storyline and chapter progression | Turn design/lore into actual gated progression systems |
| Bedrock-specific UX | Better verification, GUI fallback, and pack behavior for cross-play users |
| Audio and presentation polish | Music triggers, branding assets, and richer thematic presentation |
| Release engineering | CI, packaging, release workflow, and more formal deployment tooling |

---

## Out Of Scope For Immediate Execution

These ideas belong in the design bible or long-term R&D, not the near-term implementation queue.

- Experimental GPU offloading ideas
- Very long-horizon tech systems like wireless infrastructure or item cloud concepts
- Fully scripted story campaigns before the core network is operationally stable

---

## Working Rule

When a feature is finished:

1. Update [implementation-status.md](implementation-status.md).
2. Update the relevant player/development/operations guide.
3. Update `CHANGELOG.md`.
4. Remove or downgrade the roadmap item if it is no longer active.
