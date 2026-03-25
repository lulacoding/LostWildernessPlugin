---
name: new-module
description: Scaffold a new RPG domain module in RPG_Core_V2 following the existing RpgModule pattern. Args: module name in lowercase (e.g. "economy", "nether-events").
---

Scaffold a new RPG module named "{args}".

## Steps

1. **Read an existing similar module first** to understand the exact pattern.
   Good references: `party/PartyModule.java`, `reputation/ReputationModule.java`

2. **Create these files** under `PluginV2/src/main/java/com/lostwilderness/rpgcore/{args}/`:
   - `{PascalCase}Module.java` — implements `RpgModule` with `onLoad()`, `onEnable()`, `onDisable()`
   - `{PascalCase}Service.java` — business logic, registered in ServiceRegistry
   - `{PascalCase}Repository.java` — DB access using HikariCP DataSource, async CompletableFuture pattern

3. **Register** in `RPGCorePlugin.java`:
   - Add to the module list in `onEnable()` following the existing pattern
   - Guard with `enabled.contains("{args}")` check

4. **Add to config**:
   - `plugin.yml`: no change needed unless new commands
   - `Server/backends/survival-1/plugins/RPG_Core_V2/config/core.yml`: add `- {args}` under `enabled-modules`
   - `PluginV2/src/main/resources/config/core.yml`: add `- {args}` under `enabled-modules`

5. **Key conventions to follow**:
   - Never call `.join()` on CompletableFuture on the main thread
   - PDC keys use `"lw:"` namespace prefix
   - Listeners must clean up player state on `PlayerQuitEvent`
   - Services injected via `ServiceRegistry`, not static singletons

6. **Build** to verify: `cd PluginV2 && ./gradlew.bat :compileJava -x test`
