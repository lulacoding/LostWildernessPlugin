# Lost Wilderness Documentation - Main Hub (V1 + Design)

> **Note:** This documentation covers the V1 Plugin system (current live implementation). For the V2 rewrite (in development), see [v2/](v2/README.md).

The `Docs/` folder is the documentation hub for Lost Wilderness: player handbook, project status, contributor docs, operations plans, and the long-form design bible.

---

## Start Here

| If You Want To... | Start Here | What You Will Get |
| --- | --- | --- |
| See what is live right now | [Implementation status](implementation-status.md) | Current codebase reality, feature status, and gaps vs design |
| See what should happen next | [Roadmap](roadmap.md) | Prioritized now/next/later work across gameplay, platform, and polish |
| Learn how the server works as a player | [Player handbook](player/README.md) | Join flow, commands, servers, portals, events, clans, and verification |
| Work on the codebase | [Development docs](development/README.md) | Setup, local run, architecture, testing, and contributor references |
| Plan infrastructure and scaling | [Operations & scaling](future/README.md) | Network, database, monitoring, backups, load testing, and runbooks |
| Read the full product/design bible | [Design bible](design/README.md) | Vision, infrastructure intent, plugins, worlds, packs, audio, and open questions |

---

## Documentation Areas

| Area | Purpose | Audience |
| --- | --- | --- |
| [`player/`](player/README.md) | Clean player-facing handbook | Players, moderators, support |
| [`development/`](development/README.md) | Contributor and codebase documentation | Developers |
| [`future/`](future/README.md) | Operations, infrastructure, and scaling plans | Operators, infra, future planning |
| [`design/`](design/README.md) | Canonical concept and long-form design intent | Designers, product, lore/planning |
| [`implementation-status.md`](implementation-status.md) | Single source of truth for what is implemented | Everyone |
| [`roadmap.md`](roadmap.md) | Prioritized execution plan | Project owner, developers |
| [`bug-list.md`](bug-list.md) | Tracked bugs and fix notes | QA, debugging, release notes |
| [`archive/`](archive/README.md) | Older notes, publishing helpers, and non-primary reference material | Internal reference only |

---

## Recommended Reading Paths

### New player or staff helper

1. [Player handbook](player/README.md)
2. [Lobby and verification](player/lobby-and-verification.md)
3. [Survival](player/survival.md) and [Amplified](player/amplified.md)
4. [Portals](player/portals.md) and [Clans](player/clans.md)

### New developer

1. [Development docs](development/README.md)
2. [Install and run](development/install-and-run.md)
3. [Plugin architecture guide](development/dev-guide.md)
4. [Test guide](development/test-guide.md)
5. [Implementation status](implementation-status.md)

### Project planning and product direction

1. [Implementation status](implementation-status.md)
2. [Roadmap](roadmap.md)
3. [Operations & scaling](future/README.md)
4. [Design bible](design/README.md)

---

## Documentation Rules

- Use [style-guide.md](style-guide.md) when adding or editing pages.
- Keep player docs player-facing, development docs contributor-facing, and design docs concept-facing.
- Put historical notes, publishing helpers, and temporary reference material in [`archive/`](archive/README.md), not in the main handbook flows.
- Update `CHANGELOG.md`, `Docs/implementation-status.md`, and any relevant guide pages when major features or workflows change.

---

## Documentation Map

Lost Wilderness has two documentation sets:

| Documentation Area | Purpose | Location |
| --- | --- | --- |
| **Main Hub** (this folder) | V1 system, design bible, operations, player guides | [Docs/README.md](README.md) |
| **V2 Rewrite** | V2 implementation guide, module architecture, build phases | [v2/](v2/README.md) |

### Directory Structure

- **design/** - Design bible (8 subsections: overview, infrastructure, plugins, packs, worlds, technical, audio, open questions)
- **development/** - V1 development guides (setup, architecture, testing)
- **player/** - Player handbook (join flow, commands, features)
- **ops/** - Operational configs (monitoring, backups, systemd services)
- **future/** - Operations planning (scaling, database, monitoring)
- **v2/** - V2 rewrite implementation guide (modules, phases, features)
- **archive/** - Historical reference

### V1 vs V2 Documentation

- **V1 Documentation** (Docs/ root folders): Current live system, design intent, operations
- **V2 Documentation** (Docs/v2/): Implementation guide for rewrite (in development)

### Key Cross-References

| Document | Purpose | Complementary Doc |
| --- | --- | --- |
| [roadmap.md](roadmap.md) | Project-level priorities | [v2/05-planning/roadmap.md](v2/05-planning/roadmap.md) (V2 build phases) |
| [implementation-status.md](implementation-status.md) | V1 current state (38KB) | [v2/06-operations/implementation-status.md](v2/06-operations/implementation-status.md) (V2 status) |
| [design/](design/README.md) | Design intent | [v2/04-features/v2-feature-parity.md](v2/04-features/v2-feature-parity.md) (V2 checklist) |
