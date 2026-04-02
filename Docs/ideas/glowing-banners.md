---
title: Glowing Banners
description: Custom banner glow mechanics with Java per-layer effects and Bedrock fallbacks.
tags:
  - ideas
  - banners
  - speculative
status: speculative
phase: phase-3
owner: dev
action: needs-dev
---
# Glowing Banners

Custom banner patterns scattered throughout world structures, with a glowing mechanic that differentiates Java and Bedrock experiences.


## Glowing Mechanic — Java

- **Per-layer glow:** Players can choose which individual layers glow (e.g. only the eyes layer)
- Implemented entirely via Java plugin + data pack + texture pack
- A dedicated "glowing item" (TBD) is used in the **Loom GUI** — requires adding one extra slot to the loom interface
- **De-glow:** Use a **wet sponge** to remove all glow at once — per-layer undo is not supported; a new banner must be crafted if you want to change which layers glow
- Glowing banners on **shields** — per-layer glow applies on the shield as it does on the banner


## Loom GUI Changes

- Java: add one slot for the glowing item
- Bedrock uses the **Cumulus** GUI system — GUI changes cannot be ported via resource pack alone; Bedrock loom stays vanilla
- The glowing item slot is a Java-only addition


## Implementation Notes

- Java stack: data pack (patterns) + texture pack (art) + plugin (event handling for loom slot, glow logic)
- Bedrock stack: resource pack only (bloom colours or particle trigger on right-click)
- Bedrock player detection: check for Geyser prefix on player name → route to Bedrock glow code path
- A Java player creating a glowing banner: Bedrock players nearby see at least some glow effect (even if not per-layer)

---

## Open Questions

- What item triggers the glow? (Glowing ink sac? Custom item?)
- Which structures will contain the custom pattern templates?
- Maximum number of custom patterns to support before texture management becomes unwieldy?
- Should glowing banners emit actual light (Optifine/Iris emissive textures) or purely visual glow?
