# Glossary

Terms and abbreviations used in PluginV2 docs and architecture.

| Term | Meaning |
|------|--------|
| **Player spine** | PlayerProfile + PlayerProfileService as the central place all other systems read/write; no domain logic in listeners. |
| **Module** | A feature unit (player, progression, skills, economy, etc.) implementing RpgModule and registered with ModuleManager. |
| **Domain service** | A service class that owns a domain (e.g. ProgressionService, SkillService); used by listeners and other services, never by direct DB access from listeners. |
| **Async-first** | All DB and heavy work off the main thread; listeners only validate and delegate. |
| **ModuleContext** | Object passed to modules in onLoad(); exposes ConfigService, DatabaseProvider, SchedulerService, ServiceRegistry. |
| **Infra** | Infrastructure layer: config, DB, scheduler, optional cache/messaging. |
| **V1** | Current/legacy plugin set (common, survival, amplified, calendar, events, clans, portals, lobby). |
| **V2** | PluginV2 / RPG_Core_V2 – the new single-jar, modular core. |
| **Vertical slice** | One end-to-end feature (e.g. “progression + one milestone + command”) implemented and testable. |
