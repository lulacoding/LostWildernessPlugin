---
title: Space Travel & Multiverse Endgame
description: Post-story star jet travel, resource zones, and galactic empire alliance stacking.
tags:
  - ideas
  - space
  - endgame
  - speculative
status: speculative
phase: post-story
owner: design
action: needs-design
---
# Space Travel & Multiverse Endgame

The ultimate endgame loop: own a star jet, free-roam between all planets and dimensions, stack alliances into a galactic empire, and run a fully automated multiversal civilisation.


## Space Zones

Space is not a single empty void — it is divided into randomised zones generated from **presets**:

| Zone Type | Contents | Notes |
| --- | --- | --- |
| Empty void | Nothing — harvestable void/null resource? | Even emptiness could yield a rare block |
| Asteroid field | Mineable asteroid blocks, rare ores | Mid-tier resource zone |
| Planet cluster | Multiple planet bodies visible/accessible | Exploration hub |
| Dark matter zone | High dark matter concentration | Required for light-side matter engines |
| Light matter zone | High light matter concentration | Required for dark-side matter engines |
| Mixed zone | Both matter types + neutral resources | PvP flashpoint; contested territory |

Zones are randomised per visit or per session from a pool of presets — players never know exactly what they'll encounter.


## Capital City

Every empire-tier clan needs a designated **capital city**:
- Set by the clan creator from their list of bases
- Governors/lords of other bases report to the capital
- The capital is where the clan's top government (Tier 1–3) operates
- Should be the most fortified and developed base


## Amplified Nether/End (Hardware Note)

An amplified copy of the Nether and/or End was floated as an option:
- Server hardware: ~96 threads (48 cores) available on the cluster
- Java thread binding TBD — 48 threads available for world generation
- Not 100% confirmed yet, but not off the table given available compute


## Production / Freelancing Notes

Items that will require external help:

| Item | Approach |
| --- | --- |
| Soundtrack / music | Freelance via Fiverr — no in-house musical talent |
| Java texture/resource pack | May need freelance help for volume of assets |
| Bedrock texture pack | Separate from Java; freelance translation of Java pack to Bedrock format |
| Data pack | Can be AI-written |
| Core plugin per server type | One big core plugin (RPG_Core_V2) + Geyser + Floodgate + anti-cheat |

Two separate texture packs are required — Java and Bedrock are fundamentally different formats and cannot share the same pack.


## See Also

- [Matter engines and fuel tiers](matter-engines-and-fuel-tiers.md) — star jet fuel (dark/light matter)
- [World seeds and chakra progression](world-seeds-chakra-progression.md) — the world tier order that star jets travel between
- [Clan hierarchy tiers](clan-hierarchy-tiers.md) — the governance system that scales up to galactic empire
- [NPC automation system](npc-automation-system.md) — the automated infrastructure that runs the empire
