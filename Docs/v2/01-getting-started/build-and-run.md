# Build and run

How to build the PluginV2 (RPG_Core_V2) jar and run it on your Server backends.

---

## Prerequisites

- JDK 21.
- Your **Server** folder with three backends: **lobby-1**, **survival-1**, **amplified-1** (no need to change the folder structure).
- Paper 1.21+ on each backend (or your target version).

---

## Build

From the repo root or from `PluginV2`:

```bat
cd PluginV2
.\gradlew.bat build
```

- **Output JAR:** `PluginV2\build\libs\RPG_Core_V2-2.0.0-SNAPSHOT.jar`

---

## Deploy to your 3 servers

Copy the same JAR into each backend’s `plugins` folder (you don’t need to change anything else in Server):

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

---

## Test Phase 1 (profile load/save)

1. **Start one backend** (e.g. survival-1). You can start from your usual script or run the Paper jar in `Server\backends\survival-1\`.
2. **First run:** The plugin creates `plugins/RPG_Core_V2/config/` and writes default `core.yml` and `db.yml`. The default DB is **H2 file-based** (`./plugins/RPG_Core_V2/player`), so no MySQL is required for this test.
3. **Check console:** You should see `RPG_Core_V2 enabled.` and no errors.
4. **Join the server** (direct connect to the backend port, or via proxy if you use it).
5. **Leave the server.**
6. **Check logs:** In `Server\backends\survival-1\logs\latest.log` (or console), confirm there are no stack traces. Profile load happens on pre-login; save happens on quit. You can add temporary log lines in `PlayerProfilePreloadListener` / `PlayerSessionListener` if you want visible “profile loaded” / “profile saved” messages.
7. **Optional:** Start lobby-1 and amplified-1 the same way; same JAR works. Set `server-role` in `plugins/RPG_Core_V2/config/core.yml` per backend later (lobby vs survival vs amplified) when you add role-specific behaviour.

---

## Test Phase 2 (progression milestones)

With `progression` in `enabled-modules` (default in `core.yml`):

1. **Start survival-1** (or any backend) with the Phase 2 JAR.
2. **Join once** and run `/v2progress`. You should see **milestone:first_join** (and “Unlocked milestones: milestone:first_join” or similar).
3. **Leave and rejoin twice more** (three joins total). Run `/v2progress` again. You should also see **milestone:join_3_times**.
4. **DB check (optional):** With MySQL, query `player_achievements` (two rows for your UUID) and `progression_counters` (one row for `join_count` with value ≥ 3). With H2, the plugin creates these tables automatically.

---

## Config per server (optional for Phase 1)

After first run, each backend has:

- `Server\backends\<name>\plugins\RPG_Core_V2\config\core.yml` – set `server-role: lobby` on lobby-1, `survival` on survival-1, `amplified` on amplified-1 when you care about role.
- `Server\backends\<name>\plugins\RPG_Core_V2\config\db.yml` – default is H2. For a shared MySQL later, point `jdbc-url` (and username/password) to your DB and ensure the `player_profiles` table exists (plugin creates it for H2; for MySQL you may need to run the same DDL once).

---

## Config location

- **At runtime:** `plugins/RPG_Core_V2/config/` inside each backend folder.
- **Defaults in JAR:** `PluginV2/src/main/resources/config/`.
