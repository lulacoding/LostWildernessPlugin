---
title: Matter Engines & Fuel Tiers
description: Fuel tier progression from oil to dark/light matter combustion engines.
tags:
  - ideas
  - technology
  - fuel
  - speculative
status: speculative
phase: post-story
owner: dev
action: needs-design
---
# Matter Engines & Fuel Tier Progression

A technology progression that moves players from basic fossil fuels through nuclear power to alien Zeratari elements, ending with alignment-based matter combustion engines as the ultimate late-game power source.


## Alignment Split (Tier 5 — Zeratari)

At the Zeratari tier, the fuel you can access depends on your **alignment** (good/bad, light/dark):

| Alignment | Zeratari Element | Fuel Unlocked |
| --- | --- | --- |
| Light / Good | Light-aligned element (TBD name) | Light energy fuel; powers light-side vehicles |
| Dark / Bad | Nitro-X (dark-aligned element) | Dark energy fuel; powers dark-side vehicles |

This is where the story and technology trees start to diverge meaningfully.


## Space as a Resource Zone

Once players have matter engines, **space itself becomes a resource zone**:

- Different space zones contain different matter concentrations (dark-heavy, light-heavy, neutral)
- Harvesting matter requires being in the correct zone type
- Space zones randomised from presets (see [Space travel and multiverse endgame](space-travel-and-multiverse-endgame.md))
- Even the **emptiness of space** could yield a harvestable resource block — "void matter" or similar


## Implementation Notes

- Fuel tiers could be implemented as custom items + custom crafting recipes
- Engine type (dark matter / light matter) would be locked by player alignment tag (BetonQuest or custom flag)
- Space matter harvesting could use a custom block + region detection (in space zone type)
- Nuclear processing chain: uranium ore → fuel rod → reactor → power output
- All fuel items need texture pack entries for both Java and Bedrock


## See Also

- [Space travel and multiverse endgame](space-travel-and-multiverse-endgame.md) — space zones where matter is harvested
- [World seeds and chakra progression](world-seeds-chakra-progression.md) — where Zeratari planets sit in the world tier order
- [NPC automation system](npc-automation-system.md) — endgame automated vehicles that run on matter engines
