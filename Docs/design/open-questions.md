---
title: Open Questions
description: Aggregated open design questions across all systems.
tags:
  - design
  - open-questions
status: reference
phase: ongoing
owner: admin
action: needs-review
---
# Open questions

Merged design backlog and unknowns.


## cross-platform-and-bedrock

- **Test** Calendar + portals with **Bedrock (Geyser)**.
- **Validate** Bedrock parity for custom UI, particles, advanced features; **test on real Bedrock devices**.
- **/time** and **/date:** Bedrock may need **full namespace** (e.g. **survivalcalendarplugin:time**).
- **Bedrock:** Only own portal? (to confirm).
- **Lobby:** Chest GUI on Bedrock — if it fails, **CLI/tour fallback**.
- **Resource pack:** Seamless pack switch (Mojang screen) not bypassable; **force-resource-packs** can disconnect decliners; **Floodgate/Bedrock bypass** when pack fails; **Autumn:** use **leaf litter** block.
- **Acquisition:** Get **MySQL Player Data Bridge** and **Vulcan**; reimburse lula (PayID).

## Source

- Bible section 8; 04-resource-packs; 03-plugins calendar.



## Apr 2026 new system open questions

Questions arising from the design sessions — full specs in `ideas/`:

### Vehicles
- Is iron sight / scope aim achievable without a mod loader?
- Can steering wheel input be passed natively on Bedrock Win10, or does it need a companion app?
- Tire pressure changes — handling physics effect, or just terrain-type access gate?
- Snow chains: consumed on use or reusable?
- Does the Jeep roof tent deploy automatically when parked, or is it a manual action?

### Clan hierarchy & governance
- How are democratic elections run? (Command vote? GUI?)
- What triggers a clan being "big enough" to unlock tiers of governance?
- Should constitutions be plugin-enforced in any way, or purely cosmetic?
- Civil war duration — kill count threshold, territory capture, or player negotiation?

### NPC automation
- Which Citizens traits (or custom solution) best model NPC hunger/sleep/thirst?
- How is cell tower map data stored — schematic? Region snapshot? Custom block data?
- Should automated vehicles be actual entities or particle-trail + item transport?

### Gunsmithing
- Are guns crafted at a dedicated Gunsmith bench or standard crafting table?
- Should black powder weapons fail in rain (wet powder mechanic)?
- Should alien weapons be alignment-locked like matter engines, or available to all post-story?

### World & alignment
- What are the exact Tier 6 / Crown chakra world types?
- How does food expiry interact with shulker box storage?
- Can players switch alignment and lose access to their current matter engine type?

See full open questions per file in `ideas/`.

