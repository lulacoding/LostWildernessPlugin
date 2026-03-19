# Bug list

This is the running list of known bugs/regressions found during development/testing.

## BL-001 — Startup logs are duplicated/spammy (2x–4x per message) — **FIXED**

- **Observed**: On server start, many LostWilderness plugin log lines print multiple times (often duplicated, sometimes ~4x).
- **Evidence**: Terminal snippet (survival start) shows repeated lines for the same events, e.g.:
  - `LostWilderness Survival plugin starting up...` repeated
  - `Config loaded.` repeated
  - `All services registered successfully` repeated
  - `Database connection good.` repeated
  - Similar duplication for other plugin startup messages
- **Impact**: Noisy console makes it harder to spot real warnings/errors; increases log size.
- **Cause**: Each plugin jar (Common, Survival, Amplified, etc.) can load its own copy of `LWLogger` (different classloader). The global `Logger.getLogger("LostWilderness-Common")` is shared, so each plugin’s `init()` added another `LWConsoleHandler`, resulting in 2–4× duplicate output.
- **Fix**: `LWLogger` (Plugin/common) now checks by handler class name whether the logger already has an `LWConsoleHandler` before adding one. Only one handler is attached per logger, so startup logs appear once. See `Plugin/common/src/main/java/com/lula0802/LWEvents/logging/LWLogger.java`.
- **Repro** (before fix):
  - Start a backend (e.g. `ServerUPDATE/backends/survival-1/start.bat`)
  - Watch console during plugin enable
- **Notes**:
  - Seen in snippet around `13:47:58`–`13:48:00` in the provided terminal output (`terminals/4.txt`).

## BL-002 — Breaking portal frame causes SQL error and server freeze — **FIXED**

- **Observed**: When a player breaks a portal frame block (e.g. to extend the portal to 7×7 for a ghast pet), LostWilderness-Portals logs repeated SQL errors and the server thread blocks, causing long freezes (10–55+ seconds) and “server has not responded” thread dumps.
- **Evidence** (from `terminals/4.txt` ~13:52:21–13:53:52):
  - `[LostWilderness-Portals] ❌ Cleanup failed: Unknown column 'portal_id' in 'field list'`
  - `PortalBreakListener: Unknown column 'portal_id' in 'field list'` (repeated)
  - Server thread stuck in `PortalBreakListener.onBreak` → `DatabaseManager.getConnection()` → `HikariPool.getConnection()` (TIMED_WAITING on pool)
  - Later: `HikariPool-4 - Connection is not available, request timed out after 30000ms`
- **Impact**: Breaking a portal frame can freeze the server for 30+ seconds; players see lag/disconnect; repeated errors spam logs.
- **Cause**:
  1. **Schema mismatch**: `PortalBreakListener` used column `portal_id`, but `portals` and `pending_portals` use `(player_uuid, portal_name)` as the key (see `DatabaseManager.ensurePortalsTableExists()`).
  2. **Main-thread DB**: Cleanup ran on the server thread; blocking on the pool or slow DB froze the server.
- **Fix**: In `Plugin/portals/.../PortalBreakListener.java`: (a) use actual schema — select/delete by `player_uuid`, `portal_name`, `survival_world`, `survival_x/y/z`; (b) run all DB work in `plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> { ... })` so block break never blocks the server. Listener now takes `(Plugin plugin, DatabaseManager db)` so it can schedule async; both `portals` and `pending_portals` are cleaned when a frame is broken.
- **Repro** (before fix):
  1. Create or use an existing portal (e.g. Survival→Amplified).
  2. Break one or more portal frame blocks (e.g. to resize to 7×7).
  3. Observe console for `Unknown column 'portal_id'` and server freeze / thread dumps.

## BL-003 — /deleteportals says "no portals" when player is maxed out — **FIXED**

- **Observed**: After the player sees "You can only create X portal pairs" (maxed out), `/deleteportals <username>` reports "No portals found" even though they have portals.
- **Cause**: DeletePortalsCommand used `PortalService.deletePlayerPortals(uuid)` when the registry had a PortalService. That service keeps an **in-memory** map that the Portals plugin never fills; the real data is in the **database** (`portals` table). The "maxed out" message comes from `DatabaseManager.getPlayerPortalCount()` (DB). So the command was reporting count from the empty in-memory map.
- **Fix**: DeletePortalsCommand now always uses the **database**: SELECT from `portals` by `player_uuid`, clear portal blocks at both survival and amplified locations, then `DELETE FROM portals` and `DELETE FROM pending_portals` for that player. No PortalService branch. See `Plugin/common/.../commands/DeletePortalsCommand.java`.
- **Repro** (before fix): Create 5 portals (hit max), run `/deleteportals <yourname>` → "No portals found".

