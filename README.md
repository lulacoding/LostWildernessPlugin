# Lost Wilderness

Paper-based Minecraft plugin network (lobby, survival, amplified) with shared MySQL and proxy.

## Documentation

- **[Documentation hub](Docs/documentation-hub.md)** — start here (architecture, ops, player handbook, roadmap).
- **[Ideas & brainstorming](Docs/ideas/brainstorming-hub.md)** — long-range Industry Arsenal chapters and related backlogs.
- **[Graph view](Docs/graph.md)** — full doc graph entry (includes Quartz publishing notes).

## Development

- **PluginV2 (active):** `PluginV2/` — Gradle multi-module, `com.lostwilderness.rpgcore`.
- **Legacy Plugin:** `Plugin/` — read-only reference for migration.
- Build: `cd PluginV2 && .\gradlew.bat shadowJar -x test` (Windows).

See [CLAUDE.md](CLAUDE.md) for AI/agent workflow and build commands.
