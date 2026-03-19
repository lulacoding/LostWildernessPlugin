# Lost Wilderness — Plugin Development Guide

Part of the [centralised Docs](../README.md). How to develop, extend, and maintain the Lost Wilderness multi-module Minecraft plugin project.

---

## Overview

This document provides a comprehensive guide for developing, extending, and maintaining the LostWilderness multi-module Minecraft plugin project. It covers the architecture, command framework, service APIs, best practices, and migration examples for new and existing contributors.

---

## Table of Contents
1. [Project Structure](#project-structure)
2. [Command Framework](#command-framework)
   - [BaseCommand](#basecommand)
   - [CommandService](#commandservice)
   - [Registering Commands](#registering-commands)
   - [Permissions & Player-Only Commands](#permissions--player-only-commands)
   - [Command Response Formatting](#command-response-formatting)
   - [Migration Example](#migration-example)
3. [Service APIs & Registry](#service-apis--registry)
4. [World and Arena Utilities](#world-and-arena-utilities-phase-2)
5. [Plugin-to-Plugin Communication](#plugin-to-plugin-communication)
6. [Build & Test Automation](#build--test-automation)
7. [Best Practices](#best-practices)
8. [Contributing](#contributing)

---

## Project Structure
- **common/**: Shared code, command framework, service interfaces, utilities, and models.
- **calendar/**, **clans/**, **events/**, **portals/**: Feature modules, each with their own commands and logic.
- **servers/survival/**, **servers/amplified/**: Main plugin entry points, register commands and services.
- **build-and-test.ps1**, **run-test.bat**: Automated build and test scripts.
- **roadmap 2.0.md**: Project roadmap and progress tracking.

---

## Command Framework
### BaseCommand
All plugin commands should extend `BaseCommand` from the `common` module. This provides:
- Standardized permission checks
- Player-only enforcement
- Unified message formatting
- Easy extension for new commands

**Example:**
```java
public class MyCommand extends BaseCommand {
    public MyCommand(JavaPlugin plugin) {
        super(plugin, "my.permission.node", true); // permission, playerOnly
    }
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        sendMessage(sender, "Hello, world!", "§a");
        return true;
    }
}
```

### CommandService
Each feature module should have a `CommandService` (e.g., `CalendarCommandService`) that registers all its commands and provides help output.

**Example:**
```java
public class CalendarCommandService extends AbstractCommandService {
    public CalendarCommandService(JavaPlugin plugin) {
        super(plugin);
    }
    @Override
    protected void registerCommands() {
        registerCommand("date", new DateCommand(plugin), "/date - Show the current date", null);
        // ...
    }
}
```

### Registering Commands
Only the main plugin (e.g., `SurvivalMain`) should register commands with Bukkit. All feature modules register their commands with their own `CommandService`.

**Example:**
```java
CalendarCommandService calendarService = new CalendarCommandService(this);
calendarService.registerAllWithBukkit(this);
```

### Permissions & Player-Only Commands
- Pass a permission node to the `BaseCommand` constructor to require a permission.
- Pass `true` for player-only commands (non-players will be denied).

### Command Response Formatting
Use `sendMessage(sender, message, colorCode)` or `sendMessageStatic(sender, message, colorCode)` for all user-facing output. Use section sign codes (e.g., `§6` for gold) instead of deprecated `ChatColor`.

---

## Migration Example
**Old Command:**
```java
public class OldCommand implements CommandExecutor {
    // ...
}
```
**New Command:**
```java
public class NewCommand extends BaseCommand {
    public NewCommand(JavaPlugin plugin) {
        super(plugin, "my.permission", false);
    }
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        sendMessage(sender, "Command migrated!", "§a");
        return true;
    }
}
```

---

## Service APIs & Registry
- All shared services (calendar, clans, portals, etc.) are defined as interfaces in `common/api/services`.
- Use `EnhancedServiceRegistry.getInstance().registerService()` to register, and `getService()` to retrieve.
- This enables plugin-to-plugin communication without direct dependencies.

## Enhanced Service Registry (Phase 2)

A new `EnhancedServiceRegistry` is available in the `common` module. It supports:
- Thread-safe service registration and retrieval
- Optional shutdown hooks for lifecycle management
- Lifecycle callbacks via `LifecycleAwareService`
- Lazy service instantiation via suppliers
- Metadata & discovery helpers for querying available services

**Usage Example:**
```java
EnhancedServiceRegistry.getInstance().registerService(MyService.class, new MyServiceImpl(), () -> myService.shutdown());
MyService service = EnhancedServiceRegistry.getInstance().getService(MyService.class);
```

Services can be queried using `getServicesByType()` or `getServicesWithAnnotation()` to provide discovery mechanisms between modules.

**Migration Note:**
`ServiceRegistry` now delegates internally to `EnhancedServiceRegistry`. New code should use `EnhancedServiceRegistry` directly.

---

## World and Arena Utilities (Phase 2)

The `common` module provides world boundary helpers and sandboxed boss arena infrastructure for use by boss fights, events, and future features (e.g. Diablo arena in Phase 3).

### Boundary (`com.lula0802.LWEvents.world.Boundary`)

Immutable axis-aligned bounding box in a world. Use it to define regions for arenas or entity containment.

- **Create:** `new Boundary(world, minX, minY, minZ, maxX, maxY, maxZ)` (min/max are normalized automatically).
- **Check containment:** `boundary.contains(location)` or `boundary.contains(entity)`.
- **Clamp a location** to the nearest point inside: `boundary.clamp(location)` (returns null if wrong world).
- **Center:** `boundary.getCenter()`.

### WorldUtils (`com.lula0802.LWEvents.world.WorldUtils`)

- `WorldUtils.isWorldLoaded(String name)` – whether a world is currently loaded.
- `WorldUtils.getWorldNullable(String name)` – get world by name, or null if not loaded.

### EntityControlUtils (`com.lula0802.LWEvents.world.EntityControlUtils`)

Static helpers for entities inside a boundary:

- `getEntitiesIn(Boundary boundary)` – list all entities whose location is inside the boundary.
- `removeEntitiesIn(Boundary boundary, Predicate<Entity> filter)` – remove matching entities (players are never removed).
- `removeAllMobsIn(Boundary boundary)` – remove all living entities except players (e.g. arena reset).
- `spawnInRegion(World world, Boundary boundary, EntityType type, Location preferred)` – spawn at preferred if inside, else random valid point inside.

### BossArena and ArenaManager

- **BossArena** – model with `id`, `displayName`, `Boundary boundary`, and optional `restrictMovement` / `resetOnEmpty`.
- **ArenaManager** – in-memory registry. Register with `EnhancedServiceRegistry` from Survival or Amplified main class. Methods: `register(BossArena)`, `unregister(id)`, `getById(id)`, `getByLocation(Location)`, `getAll()`. Implements `LifecycleAwareService` (clears on disable).

To register an arena with movement restriction (sandbox: players cannot leave the boundary), create a `BossArena` with `restrictMovement = true`. The `ArenaBoundaryListener` (registered by Survival/Amplified) will teleport players back inside when they cross the boundary.

Phase 3 will add arena teleportation (e.g. Multiverse) and boss fight state machine; this infrastructure only provides the boundary, entity control, and arena registry.

---

## Plugin-to-Plugin Communication
- Use the service registry for cross-module APIs.
- Avoid direct references between feature modules.
- Register services in your main plugin class.

---

## Build & Test Automation
- Use `build-and-test.ps1` or `run-test.bat` to build and test all modules.
- Integration tests should use stubs/mocks for Bukkit and plugin APIs.
- All test dependencies are managed via Gradle.

---

## Best Practices
- Always use the command framework for new commands.
- Centralize command registration in the main plugin.
- Use the service registry for all shared APIs.
- Avoid deprecated Bukkit/Spigot APIs.
- Keep user-facing messages consistent and color-coded.
- Document new features and update this guide as needed.

---

## Contributing
- Fork the repository and create a feature branch.
- Follow the code style and best practices outlined here.
- Update documentation and tests for new features.
- Submit a pull request with a clear description of your changes.

---

_Last updated: March 2026_
