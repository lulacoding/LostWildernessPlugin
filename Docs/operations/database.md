---
title: Database Operations
description: MySQL setup, tuning, and maintenance procedures.
tags:
  - operations
  - database
status: reference
phase: ongoing
owner: ops
action: none
---
# Database setup â€“ for you (operator)

A plain guide to what the plugin needs from the database, what **jdbc-url** means, and the exact SQL to run if you use MySQL. You donâ€™t need to change anything if you use the default (H2 file).


## Two ways to run the database

### Option A: H2 (default â€“ no setup)

- **What it is:** A small database that stores everything in a **file** on the same machine as the server (no separate MySQL install).
- **Where the file is:** Inside the plugin folder, e.g. `plugins/RPG_Core_V2/player.mv.db` (path comes from the jdbc-url in `db.yml`).
- **Good for:** Single server, dev, or testing. Each backend has its **own** file (lobby-1, survival-1, amplified-1 do **not** share data).
- **What you do:** Nothing. Leave `db.yml` as shipped; the plugin creates the file and the table the first time it runs.

Default in `db.yml`:

```yaml
datasources:
  player:
    jdbc-url: "jdbc:h2:./plugins/RPG_Core_V2/player;AUTO_SERVER=TRUE"
    username: ""
    password: ""
    maximum-pool-size: 10
```

No SQL to run; no MySQL; no tables to create by hand.


## How to create the MySQL user

You need a database **user** (username + password) that the plugin uses in `db.yml`. Run these in MySQL as an admin (e.g. `root` or in MySQL Workbench).

**1. Create the user and set a password**

```sql
CREATE USER 'lw'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD_HERE';
```

- `lw` = username (must match `username` in `db.yml`).
- `YOUR_PASSWORD_HERE` = the password youâ€™ll put in `db.yml`. Pick a strong one.
- `'lw'@'localhost'` = user `lw` only when connecting from the same machine. If the game server and MySQL are on **different** machines, use `'lw'@'%'` so the user can connect from any host (less secure; use a firewall and strong password).

**2. Grant rights on the database**

```sql
CREATE DATABASE IF NOT EXISTS lw_player
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON lw_player.* TO 'lw'@'localhost';
FLUSH PRIVILEGES;
```

- `lw_player` = database name (must match the one in `jdbc-url`).
- `lw_player.*` = all tables in that database. The plugin can then create and use tables there.

**3. Use the same user/password in `db.yml`**

```yaml
username: lw
password: YOUR_PASSWORD_HERE
```

**If the server and MySQL are on different machines**, create the user for remote access:

```sql
CREATE USER 'lw'@'%' IDENTIFIED BY 'YOUR_PASSWORD_HERE';
GRANT ALL PRIVILEGES ON lw_player.* TO 'lw'@'%';
FLUSH PRIVILEGES;
```

Then in `jdbc-url` use the DB serverâ€™s IP or hostname instead of `localhost`.


### Phase 1: `player_profiles`

Stores one row per player: UUID and last-seen time.

**MySQL / MariaDB â€“ run this once (e.g. in MySQL Workbench or `mysql` CLI):**

```sql
-- Create the database if you havenâ€™t already
CREATE DATABASE IF NOT EXISTS lw_player
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE lw_player;

-- Table used by the player module (Phase 1)
CREATE TABLE IF NOT EXISTS player_profiles (
  uuid       VARCHAR(36)  NOT NULL PRIMARY KEY,
  last_seen_at BIGINT      NOT NULL
);
```

- **uuid:** Minecraft player UUID (e.g. `a1b2c3d4-e5f6-7890-abcd-ef1234567890`).
- **last_seen_at:** Time in milliseconds since Unix epoch (when they last quit or were saved).

Thatâ€™s all you need for Phase 1. With **H2** the plugin creates this table and reads/writes it. With **MySQL** the plugin can create the table if it has permission; if you created it yourself with the SQL above, the plugin will use it. (Phase 1 save uses H2-style `MERGE`; if you use MySQL and see an error on save, the code may need a small change to use `INSERT ... ON DUPLICATE KEY UPDATE`â€”we can add that when you switch to MySQL.)


### Later phases (for reference)

- **Skills:** e.g. `player_skills` (player_uuid, skill_id, xp, level).
- **Economy:** e.g. `wallets`, `transaction_log`.
- **Calendar, events, clans, portals, quests:** each will have their own tables.


## Future: scaling and tuning (long-form)

## Future: Database Architecture & Tuning

Lost Wilderness already leans heavily on MySQL for **calendar, portals, clans, verification, Discord bot, and crossâ€‘server entities**. To handle 400+ players, the database layer must be **boring, fast, and resilient**.

This page describes the **desired endâ€‘state** for DB design and operations.


### 2. Schema and indexing principles

For each table:

1. Identify **primary key** and **most common WHERE clauses**.
2. Ensure those columns are covered by either the primary key or a secondary index.
3. Avoid â€œSELECT *â€ in hot paths; prefer explicit columns.

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

Document each table in a small tableâ€‘byâ€‘table section in this file as schemas stabilise.


### 4. MySQL/MariaDB server tuning (Ubuntu/Debian + MariaDB)

Production runs on **Ubuntu/Debian with MariaDB**. Windows/XAMPP is used for local dev only â€” do not apply MariaDB tuning to XAMPP.

#### 4a. Config file placement (Ubuntu/Debian)

MariaDB on Debian/Ubuntu reads drop-in files from `/etc/mysql/mariadb.conf.d/`. The correct place for custom tuning is:

```
/etc/mysql/mariadb.conf.d/99-lostwilderness.cnf
```

Do **not** edit `/etc/mysql/my.cnf` directly â€” it includes the drop-in directory and will be overwritten by package upgrades.

#### 4b. Reference config (`Docs/ops/mariadb-99-lostwilderness.cnf`)

A ready-to-deploy config drop-in is committed at [`Docs/ops/mariadb-99-lostwilderness.cnf`](../ops/mariadb-99-lostwilderness.cnf). Adjust `innodb_buffer_pool_size` to match your host before copying.

**Guidelines for sizing:**

| Host RAM | `innodb_buffer_pool_size` | `max_connections` |
|----------|--------------------------|-------------------|
| 4 GB     | 1G                       | 100               |
| 8 GB     | 3G                       | 150               |
| 16 GB    | 6G                       | 200               |

Hikari pool usage (current setup): ~10â€“15 per JVM Ã— 3 backends + bot â‰ˆ 50â€“60 active connections. Set `max_connections` well above that to leave headroom.

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
  - Offload readâ€‘heavy features (e.g. analytics, web dashboards) to replicas.
  - Keep all gameplayâ€‘critical writes to the primary.
- Long term, consider separating **operational data** (players, portals, clans) from **analytics/longâ€‘term metrics** into different stores.

Document which tables/queries become slow as you grow, and update this file with concrete fixes (indexes, schema changes, or moved workloads).

