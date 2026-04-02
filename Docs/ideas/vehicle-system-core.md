---
title: Vehicle System - Core
description: Research-gated vehicle progression, engine upgrades, fuel research, and transmission modes.
tags:
  - ideas
  - vehicles
  - speculative
status: speculative
phase: post-story
owner: dev
action: needs-dev
---
# Vehicle System — Core

The vehicle system is built around **research gates and player specialisation** — not time or grinding. The best vehicles take actual engineering knowledge, rare components, and specific personality builds to unlock. No one gets a turbo on day one.


## Starting Vehicles

Every player starts with access to schematics for:
- A **basic 4-door 5-seater sedan**
- A **basic 5-seater Jeep**
- A **basic trailer chassis**

Both start with a **pushrod V8 carburettor** engine (cast iron block). Deliberately primitive. No electronics.


## Fuel Type System

### Engineer Sub-Type (Random Assignment)

When a player chooses **Engineer** as their personality/class, they are **randomly assigned** a fuel type:

| Type | Notes |
| --- | --- |
| Petrol | Standard; carburettor → EFI path |
| Diesel | Higher torque; specific diesel engine research tree |
| LPG | Alternative; cleaner but less common fuel source |

This means everyone starts needing to refine fuel **differently** — no single resource (crude oil) becomes the only bottleneck. Diesel engineers fight over diesel. Petrol engineers fight over crude. LPG engineers fight over gas.

### Fuel Research Chain (Petrol example)
1. Research fuel types (first unlock)
2. Leaded fuel → causes engine knock (ping) at high RPM
3. Unleaded fuel → clean burn
4. Octane ratings → performance tuning


## Research Kinematics (Randomised Unlocks)

Not every upgrade is straight research — some are randomised rewards. This means:
- Two players researching the same thing may end up with different unlocks
- You might get a winch while someone else gets diff locks
- Scarcity of specific unlocks creates an engineering economy

Examples of randomised kinematics:
- Diff locks
- Snorkels
- Winches
- Manual vs automatic transmission
- Range selectors (2H/4H/4L)
- Limited slip differential


## Input Detection System (Lobby Setup)

Before joining the server, players complete a **hardware profile** in the lobby that determines what control modes they can access:

| Question | Options | Unlocks |
| --- | --- | --- |
| Keyboard type | Has numpad / No numpad | H-pattern gearbox access |
| Platform | Java / Bedrock-keyboard / Bedrock-controller / Touchscreen | Transmission mode tier |
| Mouse extra buttons? | Yes (how many) / No | Sequential shift on mouse buttons |
| Controller type | Standard / SCUF / Pro | Clutch + sequential for console players |
| Steering wheel? | Yes / No | Wheel binding profiles |

This data is stored per-player and used to determine what transmission modes their vehicles support. Every button available across all devices will be used for something — some actions on controllers will require multi-button combinations.

### Per-Platform Transmission Access

| Platform | Available Modes |
| --- | --- |
| Java + numpad | All modes including H-pattern clutch |
| Java no numpad | Sequential + clutch, auto-clutch sequential, automatic |
| Bedrock + numpad (Win10) | Potentially H-pattern + clutch — same as Java |
| Bedrock + SCUF/Pro controller | Auto-clutch sequential (manual option) + automatic |
| Bedrock standard controller | Automatic only (possibly auto-clutch sequential) |
| Touchscreen | Automatic only |


## Engineer as a Career

> "You can actually get like a resume — your engineer type allows for such and such."

- Engineers are **sought after between wars** — victors recruit enemy engineers
- The only engineer on the server who can build turbos has real market power
- Engineering skill cannot be bought with playtime, mining, or crafting — it's RNG class assignment + research investment
- This parallels Operation Paperclip (WW2 Nazi engineers recruited post-war) — that dynamic will emerge organically

### Personality Test Extension

The existing personality test needs to be extended to properly distinguish **Engineer** from **Mechanic**:
- Both are technical roles but different in scope
- Engineer: design, research, manufacture
- Mechanic: maintain, repair, service
- At a 50/50 result, a final tiebreaker question phrases the distinction clearly
- Points-weighted system determines the sub-type


## See Also

- [Vehicle configurations](vehicle-configurations.md) — physical builds, truck bodies, trailers, 4WD, dual control, sensors
- [Matter engines and fuel tiers](matter-engines-and-fuel-tiers.md) — endgame dark/light matter engines
- [Road infrastructure and law](road-infrastructure-and-law.md) — roads, NPC police, traffic laws, civil war
