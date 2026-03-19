# Plugin: Core

Bootstrap and module management. No gameplay logic.

## Purpose

- Start the plugin and load config.
- Create infra services (config, DB, scheduler, messaging).
- Build `ModuleContext` and `ModuleManager`.
- Load and enable modules in dependency order; on disable, shut them down and close infra.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `RPGCorePlugin` | Paper entrypoint. `onLoad` / `onEnable` / `onDisable`; wires infra and ModuleManager. |
| `ModuleManager` | Holds registered modules; dependency order; `loadAll()`, `enableAll()`, `disableAll()`. |
| `RpgModule` | Interface: `onLoad(ModuleContext)`, `onEnable()`, `onDisable()`. |
| `ModuleContext` | Exposes ConfigService, DatabaseProvider, SchedulerService, ServiceRegistry (and optional messaging). |
| `ModuleDescriptor` | Name, dependencies, ordering for each module. |

## Config

- `config/core.yml` – Server role, enabled modules list, environment (dev/stage/prod).

## Lifecycle

1. `onLoad()` – Read base config (environment, server role).
2. `onEnable()` – Create ConfigService, DatabaseProvider, SchedulerService, (optional) ClusterMessagingService → build ModuleContext → create ModuleManager → register modules from config → `loadAll()` → `enableAll()`.
3. `onDisable()` – `moduleManager.disableAll()` → close DB pools, messaging, etc.
