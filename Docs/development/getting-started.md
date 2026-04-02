---
title: Getting Started
description: Developer onboarding and environment setup guide.
tags:
  - development
  - onboarding
status: reference
phase: ongoing
owner: dev
action: none
---
# Getting started

Contributor setup for Lost Wilderness: **PluginV2 (RPG_Core_V2)** build/deploy, **legacy V1** Paper install notes, and **V1 codebase** patterns (`Plugin/`). For V2 architecture see [Architecture overview](../architecture/overview.md) and [V2 architecture plan](../architecture/v2-architecture.md).


## Prerequisites

- JDK 21.
- Your **Server** folder with three backends: **lobby-1**, **survival-1**, **amplified-1** (no need to change the folder structure).
- Paper 1.21+ on each backend (or your target version).


## Deploy to your 3 servers

Copy the same JAR into each backendâ€™s `plugins` folder (you donâ€™t need to change anything else in Server):

| Backend        | Plugins folder (copy JAR here) |
|----------------|---------------------------------|
| **lobby-1**    | `Server\backends\lobby-1\plugins\`   |
| **survival-1** | `Server\backends\survival-1\plugins\` |
| **amplified-1**| `Server\backends\amplified-1\plugins\` |

Example (PowerShell, from repo root):

```powershell
$jar = "PluginV2\build\libs\RPG_Core_V2-2.0.0-SNAPSHOT.jar"
Copy-Item $jar "Server\backends\lobby-1\plugins\"
Copy-Item $jar "Server\backends\survival-1\plugins\"
Copy-Item $jar "Server\backends\amplified-1\plugins\"
```

Or copy the JAR manually into each `Server\backends\<name>\plugins\` folder.


## Test Phase 2 (progression milestones)

With `progression` in `enabled-modules` (default in `core.yml`):

1. **Start survival-1** (or any backend) with the Phase 2 JAR.
2. **Join once** and run `/v2progress`. You should see **milestone:first_join** (and â€œUnlocked milestones: milestone:first_joinâ€ or similar).
3. **Leave and rejoin twice more** (three joins total). Run `/v2progress` again. You should also see **milestone:join_3_times**.
4. **DB check (optional):** With MySQL, query `player_achievements` (two rows for your UUID) and `progression_counters` (one row for `join_count` with value â‰¥ 3). With H2, the plugin creates these tables automatically.


## Config location

- **At runtime:** `plugins/RPG_Core_V2/config/` inside each backend folder.
- **Defaults in JAR:** `PluginV2/src/main/resources/config/`.


## Part 1: Prerequisites

### 1.1 Java (JDK 21)

- **Required:** Java 21 (LTS).
- **Download:** [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64) or pick your OS.
- Set `JAVA_HOME` to the JDK 21 install path.
- **Verify:** `java -version` shows OpenJDK 21.

### 1.2 MySQL or MariaDB

- Required for **Portals**, **Clans**, and DB-backed features.
- **Compatibility:** MySQL 8 or MariaDB 10.x+.

#### Create database and user

```sql
CREATE DATABASE IF NOT EXISTS minecraft
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'minecraft'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';
GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
FLUSH PRIVILEGES;
```

Use the same host, username, password, and database in `configs/common/config.yml`.

### 1.3 Build the plugins

From the project root (e.g. `Lost Wilderness` or plugin repo root):

```powershell
.\gradlew.bat build
```

JARs are written to the project **`plugins/`** folder.


## Part 3: Install plugins

Copy from the project **`plugins/`** folder into the server **`plugins/`** folder:

| JAR | Purpose |
| --- | --- |
| `LostWilderness-Common-*-all.jar` | **Required first** (for Survival/Amplified). Shared config and APIs. |
| `LostWilderness-Survival-*-all.jar` | Main plugin: calendar, events, clans, portals, commands. |
| `LostWilderness-Lobby-*-all.jar` | **Lobby server only.** /verify and teleporter to Survival (verified players). |

Optional: `LostWilderness-Amplified-*-all.jar`, Calendar, Clans, Events, Portals.  
Optional dependency: **ProtocolLib** for Fog event visibility.

**Lobby server:** Use a separate Paper server (e.g. port 25568); copy only **LostWilderness-Lobby** into its `plugins/`. Configure proxy so new players connect to the lobby first. See [Lobby and Discord bot setup](lobby-and-discord-bot-setup.md). **Discord verify bot:** Run the bot in `DiscordBot/` (Node.js); see [DiscordBot/README.md](../../DiscordBot/README.md) and [Lobby and Discord bot setup](lobby-and-discord-bot-setup.md).


## Part 5: Running

```powershell
cd C:\MC-Server
java -Xms1G -Xmx2G -jar paper.jar
```

- **BungeeCord:** If using a proxy, set `bungeecord: true` in each backendâ€™s `config/spigot.yml`.
- **Portals:** Ensure MySQL tables exist (plugin can create them, or see full SQL in the original install guide).


## Section 3 — Plugin development guide (V1 `Plugin/` tree)
# Lost Wilderness â€” Plugin Development Guide

How to develop, extend, and maintain the Lost Wilderness multi-module Minecraft plugin project.


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
        sendMessage(sender, "Hello, world!", "Â§a");
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
Use `sendMessage(sender, message, colorCode)` or `sendMessageStatic(sender, message, colorCode)` for all user-facing output. Use section sign codes (e.g., `Â§6` for gold) instead of deprecated `ChatColor`.


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


## Plugin-to-Plugin Communication
- Use the service registry for cross-module APIs.
- Avoid direct references between feature modules.
- Register services in your main plugin class.


## Best Practices
- Always use the command framework for new commands.
- Centralize command registration in the main plugin.
- Use the service registry for all shared APIs.
- Avoid deprecated Bukkit/Spigot APIs.
- Keep user-facing messages consistent and color-coded.
- Document new features and update this guide as needed.


_Last updated: March 2026_
