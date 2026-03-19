# Phase 1 spec

Concrete spec for the first implementation slice: skeleton plugin, infra, module system, and player spine.

---

## paper-plugin.yml

```yaml
name: RPG_Core_V2
version: ${version}
main: com.lostwilderness.rpgcore.core.RPGCorePlugin
api-version: '1.21'
```

(Add `authors`, `description`, `website` as needed. No commands in Phase 1 unless you add a minimal `/v2profile` for testing.)

---

## Class list (minimal)

**core/**  
- `RPGCorePlugin` – onLoad / onEnable / onDisable; create infra, ModuleContext, ModuleManager; load/enable/disable modules.  
- `ModuleManager` – register modules, dependency order, loadAll(), enableAll(), disableAll().  
- `RpgModule` – interface: onLoad(ModuleContext), onEnable(), onDisable().  
- `ModuleContext` – getConfigService(), getDatabaseProvider(), getScheduler(), getServiceRegistry().

**infra/**  
- `ConfigService` – read core.yml, db.yml (and optionally player.yml).  
- `DatabaseProvider` – Hikari pools from config.  
- `SqlExecutor` – async query/update returning CompletableFuture.  
- `SchedulerService` – runSync, runAsync, runSyncDelayed, runAsyncDelayed (and optionally repeating).  
- `ClusterMessagingService` – interface + stub impl (no-op or log-only).

**player/**  
- `PlayerProfile` – uuid, lastSeenAt (minimal for Phase 1).  
- `PlayerProfileService` – cache Map<UUID, PlayerProfile>; loadProfileAsync(UUID), getProfile(UUID), saveProfileAsync(UUID).  
- `PlayerSessionManager` – optional; track online set and callbacks.  
- `player/repo/PlayerProfileRepository` – findById(UUID), save(PlayerProfile) via SqlExecutor.  
- `player/listener/PlayerProfilePreloadListener` – AsyncPlayerPreLoginEvent → loadProfileAsync.  
- `player/listener/PlayerSessionListener` – PlayerJoinEvent (ensure profile in cache), PlayerQuitEvent (saveProfileAsync + evict).

**Module:**  
- `PlayerModule` – implements RpgModule; registers PlayerProfileService, listeners; depends on nothing.

---

## Method signatures (minimal)

**RPGCorePlugin**  
- `void onLoad()` – load base config (env, server role).  
- `void onEnable()` – build ConfigService, DatabaseProvider, SchedulerService, ModuleContext, ModuleManager; register PlayerModule; loadAll(); enableAll().  
- `void onDisable()` – disableAll(); close DB/messaging.

**ModuleContext**  
- `ConfigService getConfigService()`  
- `DatabaseProvider getDatabaseProvider()`  
- `SchedulerService getScheduler()`  
- `ServiceRegistry getServiceRegistry()`

**PlayerProfileService**  
- `CompletableFuture<PlayerProfile> loadProfileAsync(UUID)`  
- `Optional<PlayerProfile> getProfile(UUID)`  
- `void saveProfileAsync(UUID)`  
- `void saveAllDirtyAsync()` (optional for Phase 1)

**PlayerProfileRepository**  
- `CompletableFuture<Optional<PlayerProfile>> findById(UUID)`  
- `CompletableFuture<Void> save(PlayerProfile)`

**SchedulerService**  
- `void runSync(Runnable)`  
- `void runAsync(Runnable)`  
- `void runSyncDelayed(Runnable, long ticks)`  
- `void runAsyncDelayed(Runnable, long ticks)`

---

## Config files (Phase 1)

- **config/core.yml** – server-role, enabled-modules: [player].  
- **config/db.yml** – datasources (e.g. player: jdbcUrl, username, password, maximumPoolSize).  
- **config/player.yml** – optional; cache/save settings.

---

## DB (Phase 1)

One table sufficient for Phase 1:

- **player_profiles** – id (UUID), last_seen_at (timestamp); optional columns for future (e.g. username) can be added when needed.
