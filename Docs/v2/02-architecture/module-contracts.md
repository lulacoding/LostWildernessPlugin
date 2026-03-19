# Module contracts

How modules are defined, loaded, and how they get access to infra and other services.

## RpgModule interface

- `void onLoad(ModuleContext ctx)` – Register services, load configs. No listeners yet.
- `void onEnable()` – Register listeners and commands.
- `void onDisable()` – Cleanup (unregister listeners, flush caches, etc.).

## ModuleContext

Exposes only what modules need:

- `ConfigService getConfigService()`
- `DatabaseProvider getDatabaseProvider()`
- `SchedulerService getScheduler()`
- `ClusterMessagingService getMessaging()` (optional)
- `ServiceRegistry getServiceRegistry()` – Register and obtain domain services (e.g. `PlayerProfileService`, `ProgressionService`).

## ModuleDescriptor

- **Name** – e.g. `player`, `progression`, `skills`.
- **Dependencies** – List of module names that must be loaded/enabled first.
- **Ordering** – Used by `ModuleManager` to build a dependency-safe load/enable/disable order.

## Dependency rules

- **PlayerModule** – No dependencies. Load first.
- **ProgressionModule, SkillsModule, EconomyModule** – Depend on `PlayerModule`.
- **QuestsModule** – Depends on `PlayerModule`, `ProgressionModule`, `EconomyModule` (and possibly others).
- Other modules follow the same pattern: depend only on what they use.

## Registration

- Modules are registered with `ModuleManager` based on server role and config (e.g. `enabled-modules` in `core.yml`).
- `ModuleManager` builds a dependency graph and runs `loadAll()` then `enableAll()` in dependency order; on shutdown, `disableAll()` in reverse order.
