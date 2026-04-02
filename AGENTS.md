# Lost Wilderness – Project context for AI agents

## What this repo is
Lost Wilderness is a **Paper-based Minecraft plugin network**: lobby, survival, amplified, and other backends, with shared MySQL, Bungee/Velocity proxy, and an optional Discord bot.

## Key paths
- **`Plugin/`** – Gradle multi-module plugin (common, survival, amplified, lobby, portals, etc.). Source and resources live here.
- **`Docs/`** – Design bible, implementation status, ops runbooks.
- **`ServerUPDATE/`** – Server layouts, proxy and backend configs. Worlds and logs are not in git.
- **`PluginV2/`** and **[`Docs/architecture/v2-architecture.md`](Docs/architecture/v2-architecture.md)** – modular/core architecture (RPG_Core_V2).

## For large or architectural changes
Read **`Docs/architecture/v2-architecture.md`** and **`Docs/roadmap/implementation-status.md`** for context before making broad changes.

## Conventions
- Follow Cursor rules in **`.cursor/rules/`**.
- After new features: update **`CHANGELOG.md`** and, when relevant, **`Docs/roadmap/implementation-status.md`** (see project-conventions rule).
