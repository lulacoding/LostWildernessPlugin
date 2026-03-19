## Future: Database Architecture & Tuning

Lost Wilderness already leans heavily on MySQL for **calendar, portals, clans, verification, Discord bot, and cross‑server entities**. To handle 400+ players, the database layer must be **boring, fast, and resilient**.

This page describes the **desired end‑state** for DB design and operations.

---

### 1. High‑level design

- **Single logical database** (e.g. `minecraft`) for:
  - Calendar tables
  - Portals and `portal_transfer_entities`
  - Clans, alliances, war tables
  - Verification tables: `pending_verification`, `linked_accounts`
  - Any future global systems (economy, stats)
- All Minecraft JVMs and the Discord bot use **HikariCP (or equivalent)** connection pools.
- One **primary DB host**; later you can add replicas for read‑heavy workloads if necessary.

Design principles:

- Every table has a **clear owner** (which subsystem writes) and clear read patterns.
- All hot queries are **bounded and indexed** (no unindexed full scans on player‑path operations).

---

### 2. Schema and indexing principles

For each table:

1. Identify **primary key** and **most common WHERE clauses**.
2. Ensure those columns are covered by either the primary key or a secondary index.
3. Avoid “SELECT *” in hot paths; prefer explicit columns.

Examples:

- `linked_accounts(discord_id, mc_username)`
  - Primary key: `mc_username` (as already implemented).
  - Add index on `discord_id` if you often look up by Discord ID (e.g. for role sync or stats).

- `pending_verification(mc_username)`
  - Primary key: `mc_username`.
  - Optionally index on `created_at` if you run a cron to clean out stale requests.

- `portal_transfer_entities(player_uuid, created_at, ...)`
  - Composite primary key or unique index on `(player_uuid)` if exactly one pending transfer per player.
  - Index on `created_at` so cleanup jobs can delete old rows efficiently.

Document each table in a small table‑by‑table section in this file as schemas stabilise.

---

### 3. Connection pooling & usage patterns

Target usage:

- Each JVM (Survival, Amplified, Lobby, Discord bot) has **its own Hikari pool** with:
  - Reasonable `maximumPoolSize` (e.g. 10–20 per server, adjust with load tests).
  - Short connection timeouts (e.g. 5s) so misconfigurations fail fast.
- **No synchronous DB calls on the main server thread**:
  - All Bukkit plugin DB access runs on async tasks.
  - Discord bot’s DB access is naturally async via `mysql2/promise`.

Operationally:

- Track pool metrics (active connections, idle, wait time) via logs/metrics.

---

### 4. MySQL/MariaDB server tuning (Ubuntu/Debian + MariaDB)

Production runs on **Ubuntu/Debian with MariaDB**. Windows/XAMPP is used for local dev only — do not apply MariaDB tuning to XAMPP.

#### 4a. Config file placement (Ubuntu/Debian)

MariaDB on Debian/Ubuntu reads drop-in files from `/etc/mysql/mariadb.conf.d/`. The correct place for custom tuning is:

```
/etc/mysql/mariadb.conf.d/99-lostwilderness.cnf
```

Do **not** edit `/etc/mysql/my.cnf` directly — it includes the drop-in directory and will be overwritten by package upgrades.

#### 4b. Reference config (`Docs/ops/mariadb-99-lostwilderness.cnf`)

A ready-to-deploy config drop-in is committed at [`Docs/ops/mariadb-99-lostwilderness.cnf`](../ops/mariadb-99-lostwilderness.cnf). Adjust `innodb_buffer_pool_size` to match your host before copying.

**Guidelines for sizing:**

| Host RAM | `innodb_buffer_pool_size` | `max_connections` |
|----------|--------------------------|-------------------|
| 4 GB     | 1G                       | 100               |
| 8 GB     | 3G                       | 150               |
| 16 GB    | 6G                       | 200               |

Hikari pool usage (current setup): ~10–15 per JVM × 3 backends + bot ≈ 50–60 active connections. Set `max_connections` well above that to leave headroom.

#### 4c. Applying config + validating

```bash
# 1. Copy config into place
sudo cp /path/to/repo/Docs/ops/mariadb-99-lostwilderness.cnf \
    /etc/mysql/mariadb.conf.d/99-lostwilderness.cnf
sudo chmod 644 /etc/mysql/mariadb.conf.d/99-lostwilderness.cnf

# 2. Check for syntax errors
sudo mariadbd --verbose --help 2>&1 | head -50
# (or: sudo mysqld --verbose --help 2>&1 | head -50)

# 3. Restart MariaDB
sudo systemctl restart mariadb

# 4. Verify settings landed
sudo mariadb -u root -p -e "SHOW VARIABLES LIKE 'innodb_buffer_pool_size';"
sudo mariadb -u root -p -e "SHOW VARIABLES LIKE 'max_connections';"
```

#### 4d. Slow query logging (enable if you need to find slow queries)

Add to `99-lostwilderness.cnf` temporarily when investigating:

```ini
slow_query_log         = 1
slow_query_log_file    = /var/log/mysql/slow.log
long_query_time        = 1
```

Then: `sudo systemctl restart mariadb`

#### 4e. Recommended indexes (add manually if missing after first run)

Plugins create tables on first start. After the DB has tables, run:

```sql
-- discord_id lookups (bot stats, role sync)
ALTER TABLE linked_accounts ADD INDEX idx_discord_id (discord_id);

-- stale pending_verification cleanup
ALTER TABLE pending_verification ADD INDEX idx_created_at (created_at);

-- portal_transfer_entities cleanup job
ALTER TABLE portal_transfer_entities ADD INDEX idx_created_at (created_at);

-- clan_wars status queries
ALTER TABLE clan_wars ADD INDEX idx_status (status);
```

Run `SHOW INDEX FROM <table>;` first to confirm each index doesn't already exist.

---

### 5. Backups and restore strategy (DB‑specific)

At minimum:

- **Nightly logical backups** via `mysqldump` or `mariadb-dump`:
  - Full dump of `minecraft` DB.
  - Compressed and shipped off‑server (S3/Backblaze/other).
- **Weekly full snapshot** (filesystem snapshot or host‑level backup).

Critically:

- Maintain a **tested restore procedure**:
  - Step‑by‑step instructions to restore `minecraft` into a new instance.
  - A regular drill (e.g. monthly) where you restore to a staging DB and run basic integrity checks.

Add a short **“DB incident playbook”** here:

- Symptoms (e.g. connection refusals, “table marked as crashed”, Aria checksum errors).
- Triage steps (repair vs restore).

**DB incident playbook (short):**

| Symptom | Triage |
|--------|--------|
| Connection refused / "Communications link failure" | Check MySQL service; restart if safe; check disk space and `max_connections`. |
| "Table marked as crashed" / InnoDB errors | Stop heavy writers; run `mysqlcheck --repair` or InnoDB recovery; if repair fails, restore from latest backup. |
| Access denied | Verify credentials in plugin config and Discord bot env; check user host (e.g. `'minecraft'@'localhost'`). |
| Slow queries / pool exhaustion | Check pool size in Hikari config; add indexes (see table-by-table below); scale vertically or add read replica. |

**Restore from backup:** Stop all backends and bot; restore `minecraft` from latest `mysqldump`; point configs at DB; run integrity check; bring services back.

---

### 5b. Table-by-table (schema and indexes)

Lost Wilderness uses a single database `minecraft`. Tables are created by plugins on first run. Recommended indexes (add if missing):

| Table | Owner | Primary / key columns | Recommended index |
|-------|--------|------------------------|--------------------|
| `calendar` | Calendar sync | leader server, current day | As per CalendarSyncManager PK. |
| `player_data` | Calendar | player UUID / day | Index on lookups used by calendar. |
| `portals`, `pending_portals` | Portals | id, player, coords | Index on player and server for list/delete. |
| `portal_transfer_entities` | Portals | player_uuid, portal_name | Index on `created_at` for cleanup of old rows. |
| `pending_verification` | Lobby / bot | mc_username | PK on mc_username; optional index on `created_at` for cleanup. |
| `linked_accounts` | Lobby / bot | mc_username | Index on `discord_id` for lookups by Discord ID. |
| `clans`, `clan_members`, `clan_invites` | Clans | clan id, player UUID | As per ClanRepository; index by clan and by member. |
| `alliances`, `alliance_members`, `alliance_invites`, `alliance_relations` | Clans | alliance id, clan id | As per AllianceRepository. |
| `clan_wars` | Clans | clan pair, status | Index on status and clan ids for war status queries. |
| `boss_kills`, `clan_boss_kills` | Boss/events | player/clan, boss id | As per BossKillRepository. |

Document exact `CREATE TABLE` and `ALTER TABLE` (indexes) here as schemas stabilise; plugins create tables, so align with code in `CalendarSyncManager`, `DatabaseManager`, `VerificationRepository`, `ClanRepository`, etc.

---

### 6. Scaling paths

If/when a single MySQL instance becomes a bottleneck:

- **Vertical scaling** first:
  - More RAM and faster SSDs/NVMe.
- **Read replicas** later:
  - Offload read‑heavy features (e.g. analytics, web dashboards) to replicas.
  - Keep all gameplay‑critical writes to the primary.
- Long term, consider separating **operational data** (players, portals, clans) from **analytics/long‑term metrics** into different stores.

Document which tables/queries become slow as you grow, and update this file with concrete fixes (indexes, schema changes, or moved workloads).

