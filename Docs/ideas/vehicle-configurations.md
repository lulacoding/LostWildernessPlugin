---
title: Vehicle Configurations
description: Vehicle types, truck modular bodies, trailers, 4WD, HUD sensors, and dual control.
tags:
  - ideas
  - vehicles
  - speculative
status: speculative
phase: post-story
owner: dev
action: needs-dev
---
# Vehicle Configurations & Specs

All the physical detail: what you can build, how it works, how it behaves off-road, how the dashboard functions, how you service it, and the dual control / mobile spawn systems.


## Truck Body System

Trucks are a **chassis + body** — you build different bodies for different jobs:

| Body Type | Purpose | Notes |
| --- | --- | --- |
| Flatbed (bear truck) | General cargo | Up to 4 double chests of storage |
| Road salter | Snow/ice road maintenance | Prevents snow accumulation on treated roads |
| Garbage truck | Waste/block clearance | Dual control (see below) |
| Street sweeper | Clear leaf litter / ground blocks | Quality-of-life for megabases |
| Cement mixer | Construction | TBD |
| Tanker | Fuel transport | Bulk liquid |
| Crane body | Lifting operations | Late engineering unlock |

Crane and some specialised bodies are **prestige-locked** — available at later engineering prestige ranks.


## Loadout & Inventory Space

Fitting equipment to a vehicle **consumes physical inventory slots** that represent that space on the vehicle. Example on a Jeep tray:

- Default flatbed: 4 double chests of storage
- Add a winch (front): loses front tray slots
- Add exhaust twin stacks (rear): loses 8 rear inventory slots (4 per side — mimics physical stack placement)
- Add a roof rack: roof area gets inventory slots, fitting gear there removes them
- Add a turret (centre tray): blocks the area around it — nothing can be stored where the turret body sits

Rule: **what's fitted occupies that space**. You cannot stack a turret and full cargo — you have to choose.


## Mobile Spawn Point (Roof Tent)

The full-body Jeep is the **only vehicle that can be a moving spawn point**:

- Fit a **roof tent / awning** to the Jeep
- When parked and tent is deployed, an awning extends and the tent becomes a bed
- Sleeping in the tent sets your spawn point to the **vehicle's location** (not a fixed coordinate)
- On death: plugin checks current vehicle location → respawn there

### Spawn Failsafe
If the vehicle is **destroyed** (landmine, combat, etc.) and you then die:
- Primary spawn (vehicle) is gone
- Falls back to a **secondary spawn point** — your last physical bed set at base
- If neither exists → default respawn (spawn town)

> "If your vehicle broke down and you died, you still spawn at it. But if the vehicle gets blown up and then you die — you didn't go back to town."


## Vehicle HUD & Sensor System

The dashboard is **sensor-based** — you only see data for sensors you've fitted. No sensor = no readout.

### Mandatory Sensors (always present in base schematic)
| Sensor | Display | Notes |
| --- | --- | --- |
| Water temp | Action bar | Cars should overheat even without a temp sensor fitted — you just won't see it coming |
| Fuel level | Action bar | |
| Speedometer | Action bar | |

### Optional Sensors (research/craft)
| Sensor | Notes |
| --- | --- |
| RPM / tachometer | Important for manual transmission feel |
| Oil pressure | Warns before engine damage |
| Oil temperature | |
| Transmission temp | Especially important for towing |
| Boost gauge | Turbo/supercharger builds |
| Tire pressure | Per-tire readout |

### HUD Display System
- **Action bar:** Normal operating data — colour-coded green / amber / red by severity
- **Boss bar:** Critical alerts only — tire pop, engine fault, overheating critical, "please shut off now" warnings
- HUD system works on both **Java and Bedrock**
- Action bar tie-in: dark-side players who take the Mark of the Beast (half-machine upgrade) get an **upgraded HUD** — enhanced crosshairs, better sensor readouts, additional overlays


## Dual Control

Applies to trucks and can be schematic-unlocked for any vehicle:

| Option | Description |
| --- | --- |
| Left-hand drive | Driver on left (American-style) |
| Right-hand drive | Driver on right (Australian-style) |
| Dual control | Either seat can drive |

**Dual control bind:** While in neutral + stationary, a dedicated bind swaps the active driver side. Player must physically walk around to the other door to take the wheel.

Use case in combat:
- Driver hands off to passenger mid-route
- Original driver can now eat, shoot, reload, or apply a heal
- Especially important for military jeeps (no stopping mid-patrol)

> "If I get the schematics for dual control, all my military jeeps will be dual control."


## See Also

- [Vehicle system core](vehicle-system-core.md) — engine research, fuel types, engineer class, transmission
- [Road infrastructure and law](road-infrastructure-and-law.md) — roads, traffic law, NPC police
- [NPC automation system](npc-automation-system.md) — automated vehicles in endgame
