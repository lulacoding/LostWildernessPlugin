---
title: Bedrock Compatibility Notes
description: Living notes on Geyser/Floodgate behaviour and Java-first design decisions.
tags:
  - ideas
  - bedrock
  - speculative
status: reference
phase: ongoing
owner: dev
action: needs-review
---
# Bedrock Compatibility Notes

Living document tracking cross-platform quirks, known Bedrock limitations, and design decisions made for Bedrock support.


## Known Limitations

| Feature | Java | Bedrock | Status |
| --- | --- | --- | --- |
| Banner layers | Up to 16 | First 6 only | Accepted limitation |
| Per-layer banner glow | Full support | Not feasible (texture count explosion) | Compromise — particle/bloom instead |
| Loom GUI extra slot (glow) | Added via plugin | Cannot port via resource pack (Cumulus GUI system) | Bedrock stays vanilla loom |
| Enchanting table interaction | Works | Was broken on early Geyser — now fixed | Fixed |
| DMing Bedrock players | Normal | Name must be wrapped (special format due to full-stop → asterisk change) | Workaround exists |
| Shield + banner | Works | Now vanilla Bedrock feature | Custom patterns should carry over |
| Sword attack cooldown timer | Works | Now integrated via resource pack on crosshair | Fixed in recent Geyser/Bedrock updates |


## Design Philosophy

> **Java is the primary platform.** Bedrock support is welcome but must never degrade the Java experience. Bedrock players accept compromises.

- Do not generate thousands of texture variants to support Bedrock parity — it is not worth the data engineering cost
- Implement Java features cleanly; find the best Bedrock approximation afterwards
- Where a Bedrock compromise is found, document it here


## GUI System

- Java GUIs can be modified via resource pack (inventory title matching)
- Bedrock uses **Cumulus** — a fully separate GUI format; Java GUI tweaks do not carry over
- Any new GUI slots (e.g. loom glow slot) are Java-only


## Ongoing Geyser Notes

- Geyser has improved substantially since initial setup (~5 years ago)
- Combat parity (sword cooldown) is now properly handled
- Bedrock still has the spammy attack style but cooldown display is now in the crosshair
- Monitor future Geyser releases for banner layer support improvements

---

## See Also

- [Glowing banners](glowing-banners.md) — Bedrock compromise design for banner glow
- [NPC automation system](npc-automation-system.md) — future Cumulus GUI considerations
