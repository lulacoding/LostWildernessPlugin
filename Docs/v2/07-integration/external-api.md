# External API

Stable API surface for other plugins or services (e.g. Discord bot, web dashboard). Internal-only APIs are not listed here.

---

## Status

*(To be defined when PluginV2 exposes a public API.)*

- **Other Paper plugins:** Optional dependency on RPG_Core_V2; get services via a dedicated API class (e.g. `RPGCoreAPI.getPlayerProfileService()`) rather than internal classes.
- **Non-Minecraft consumers:** Optional HTTP/gRPC layer or shared DB; document endpoints and contracts here.

---

## Rules

- Only types and methods explicitly marked as “public API” are stable; internal packages can change without notice.
- Prefer interfaces for API contracts so implementations can evolve.
