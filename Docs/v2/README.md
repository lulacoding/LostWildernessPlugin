# PluginV2 Documentation - V2 Rewrite Implementation Guide

> **Design Bible & V1 System:** This is the V2 rewrite documentation. For design intent, current V1 implementation, operations, and player guides, see the main [Docs/](../README.md) folder.

This folder holds all design, architecture, and implementation docs for the **RPG Core V2** plugin (single-jar, modular rewrite of the Lost Wilderness plugin stack).

## What is PluginV2?

PluginV2 is the new core plugin that will eventually replace the current multi-jar setup (common, survival, amplified, calendar, events, clans, portals, lobby). One jar runs on every server; behaviour is controlled by config and the module system.

- **Architecture:** See repo-root [`V2_ARCHITECTURE_PLAN.md`](../../V2_ARCHITECTURE_PLAN.md) for the full AAA-scale design.
- **Legacy code:** Old plugins under `Plugin/` remain read-only reference; no edits there.

## Doc index

### Quick Start

| Doc | Purpose |
|-----|---------|
| [goals-and-scope.md](01-getting-started/goals-and-scope.md) | Goals, scope, and constraints |
| [architecture-overview.md](01-getting-started/architecture-overview.md) | High-level core → infra → modules |
| [build-and-run.md](01-getting-started/build-and-run.md) | Build and run the V2 jar |
| [migration-from-v1.md](01-getting-started/migration-from-v1.md) | Mapping old plugins to V2 |

### Architecture

| Doc | Purpose |
|-----|---------|
| [module-contracts.md](02-architecture/module-contracts.md) | RpgModule lifecycle, ModuleContext, dependencies |
| [decisions.md](02-architecture/decisions.md) | Architecture decision log |
| [data-model.md](02-architecture/data-model.md) | Main entities and DB mapping |

### Module Documentation

| Doc | Purpose |
|-----|---------|
| [plugin-core.md](03-modules/plugin-core.md) | RPGCorePlugin, ModuleManager, bootstrap |
| [plugin-player.md](03-modules/plugin-player.md) | Player profile and session |
| [plugin-progression.md](03-modules/plugin-progression.md) | Progression / milestones |
| [plugin-skills.md](03-modules/plugin-skills.md) | Skills and XP |
| [plugin-economy.md](03-modules/plugin-economy.md) | Economy and wallets |
| [plugin-calendar.md](03-modules/plugin-calendar.md) | Calendar service |
| [plugin-events.md](03-modules/plugin-events.md) | World events engine |
| [plugin-clans.md](03-modules/plugin-clans.md) | Clans, alliances, war |
| [plugin-portals.md](03-modules/plugin-portals.md) | Portals and cross-server transfer |
| [plugin-quests.md](03-modules/plugin-quests.md) | Quests and storyline |
| [plugin-party.md](03-modules/plugin-party.md) | Party system |
| [plugin-zodiac.md](03-modules/plugin-zodiac.md) | Zodiac signs, spirit animals, and astrological perks |

### Features & Status

| Doc | Purpose |
|-----|---------|
| [feature-inventory.md](04-features/feature-inventory.md) | **Complete feature list** – every module, event, command, config (full detail). Discord paste: [feature-inventory-discord.txt](discord-updates/feature-inventory-discord.txt) |
| **[v2-feature-parity.md](04-features/v2-feature-parity.md)** | **Full remake checklist (Lobby, Survival, Amplified; all current + Docs features)** |
| [events-calendar-gap.md](04-features/events-calendar-gap.md) | V1 vs V2 gap analysis |
| [implementation-status.md](06-operations/implementation-status.md) | What’s done per feature |

### Planning

| Doc | Purpose |
|-----|---------|
| [roadmap.md](05-planning/roadmap.md) | Phase roadmap |
| [timeline.md](05-planning/timeline.md) | Timeline / milestones |
| [phase0-outcomes.md](05-planning/phase0-outcomes.md) | Phase 0 outcomes |
| [phase1-spec.md](05-planning/phase1-spec.md) | Phase 1 concrete spec |
| [phase3-betonquest-milestones.md](05-planning/phase3-betonquest-milestones.md) | Phase 3: BetonQuest integration |
| [phase3-calendar.md](05-planning/phase3-calendar.md) | Phase 3: Calendar details |
| [phase3-events.md](05-planning/phase3-events.md) | Phase 3: Events details |
| [phase3-skills-auraskills.md](05-planning/phase3-skills-auraskills.md) | Phase 3: Skills/AuraSkills |
| [plan-aeternum-features-in-lw.md](05-planning/plan-aeternum-features-in-lw.md) | Aeternum features planning |
| [plan-milestones-categories.md](05-planning/plan-milestones-categories.md) | Milestone categories |

### Operations

| Doc | Purpose |
|-----|---------|
| [config-reference.md](06-operations/config-reference.md) | Config files reference |
| [database-setup.md](06-operations/database-setup.md) | **For you:** what jdbc-url is, H2 vs MySQL, SQL to create tables |
| [testing.md](06-operations/testing.md) | Testing strategy |
| [zodiac-testing-guide.md](06-operations/zodiac-testing-guide.md) | **Comprehensive Zodiac System test guide** – smoke tests, core functionality, all 13 signs, 14 spirit animals, special mechanics, integration, performance, edge cases |
| [deployment.md](06-operations/deployment.md) | Deploy and ops |

### Integration

| Doc | Purpose |
|-----|---------|
| [external-api.md](07-integration/external-api.md) | API surface for other plugins |

### Reference

- [glossary.md](glossary.md) – Terms and abbreviations
- Discord updates archive: [discord-updates/](discord-updates/)

---

## Related Documentation

### Main Documentation Hub

The main [Docs/](../README.md) folder contains:
- **Design bible** - Canonical design intent and vision
- **V1 implementation status** - What's currently live
- **Player handbook** - Join flow, commands, servers
- **Operations & scaling** - Infrastructure, backups, monitoring
- **Project roadmap** - Overall priorities beyond V2 build

### Key References for V2 Development

When implementing V2 features, consult these Docs/ files:

| Docs/ File | What It Provides | When to Use |
| --- | --- | --- |
| [design/README.md](../design/README.md) | Full design bible (vision, plugins, worlds) | Understanding design intent |
| [implementation-status.md](../implementation-status.md) | V1 feature inventory (what exists now) | Verifying V1 behavior to replicate |
| [development/test-guide.md](../development/test-guide.md) | V1 testing procedures | Creating V2 test cases |
| [future/README.md](../future/README.md) | Operations and scaling plans | Architecture decisions |
