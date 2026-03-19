# Database setup – for you (operator)

A plain guide to what the plugin needs from the database, what **jdbc-url** means, and the exact SQL to run if you use MySQL. You don’t need to change anything if you use the default (H2 file).

---

## What is `jdbc-url`?

**JDBC** = “Java Database Connectivity” – the way Java talks to a database.  
**jdbc-url** = the connection string that says *which* database and *where* it is.

Rough shape:

- **H2 (file):** `jdbc:h2:./path/to/file` → “use the H2 engine and a file on disk”.
- **MySQL / MariaDB:** `jdbc:mysql://HOST:PORT/DATABASE_NAME` → “use MySQL at this host/port and this database”.

The plugin reads `jdbc-url` (and username/password) from `config/db.yml` and opens a connection pool to that database. So when you edit `db.yml`, you’re telling the plugin *where to store data*.

---

## Two ways to run the database

### Option A: H2 (default – no setup)

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

---

### Option B: MySQL / MariaDB (shared across 3 servers)

- **What it is:** A separate database server. All three backends (lobby-1, survival-1, amplified-1) can point to the **same** database so player data is shared.
- **Good for:** When you want one account / one profile no matter which backend they’re on.
- **What you do:**
  1. Install MySQL or MariaDB and create a database (e.g. `lw_player`).
  2. Run the SQL below **once** to create the table(s).
  3. Edit each backend’s `plugins/RPG_Core_V2/config/db.yml` and set `jdbc-url`, `username`, `password` to that database.

Example `db.yml` for MySQL:

```yaml
datasources:
  player:
    jdbc-url: "jdbc:mysql://localhost:3306/lw_player?useSSL=false"
    username: lw
    password: YOUR_PASSWORD_HERE
    maximum-pool-size: 20
```

Replace `localhost:3306` if the DB is on another machine/port; replace `lw_player`, `lw`, and the password with your values.

---

## How to create the MySQL user

You need a database **user** (username + password) that the plugin uses in `db.yml`. Run these in MySQL as an admin (e.g. `root` or in MySQL Workbench).

**1. Create the user and set a password**

```sql
CREATE USER 'lw'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD_HERE';
```

- `lw` = username (must match `username` in `db.yml`).
- `YOUR_PASSWORD_HERE` = the password you’ll put in `db.yml`. Pick a strong one.
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

Then in `jdbc-url` use the DB server’s IP or hostname instead of `localhost`.

---

## Tables PluginV2 uses (and SQL to create them)

The plugin expects **one logical “player” database** (either an H2 file or a MySQL database). Inside that, it uses these tables. With **H2**, the plugin creates them automatically. With **MySQL**, you can create the database and run this SQL yourself so everything is ready.

---

### Phase 1: `player_profiles`

Stores one row per player: UUID and last-seen time.

**MySQL / MariaDB – run this once (e.g. in MySQL Workbench or `mysql` CLI):**

```sql
-- Create the database if you haven’t already
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

That’s all you need for Phase 1. With **H2** the plugin creates this table and reads/writes it. With **MySQL** the plugin can create the table if it has permission; if you created it yourself with the SQL above, the plugin will use it. (Phase 1 save uses H2-style `MERGE`; if you use MySQL and see an error on save, the code may need a small change to use `INSERT ... ON DUPLICATE KEY UPDATE`—we can add that when you switch to MySQL.)

---

### Phase 2 – progression

Stores unlocked milestones and numeric counters (e.g. join count). Same “player” datasource as Phase 1.

**MySQL / MariaDB – run this if you create tables by hand (plugin can also create them):**

```sql
CREATE TABLE IF NOT EXISTS player_achievements (
  player_uuid VARCHAR(36) NOT NULL,
  `key`       VARCHAR(255) NOT NULL,
  unlocked_at BIGINT       NOT NULL,
  PRIMARY KEY (player_uuid, `key`)
);

CREATE TABLE IF NOT EXISTS progression_counters (
  player_uuid  VARCHAR(36)  NOT NULL,
  counter_key  VARCHAR(255) NOT NULL,
  value        BIGINT       NOT NULL,
  PRIMARY KEY (player_uuid, counter_key)
);
```

With **H2** the plugin creates these tables automatically. With **MySQL** the plugin can create them if it has permission.

---

### Later phases (for reference)

- **Skills:** e.g. `player_skills` (player_uuid, skill_id, xp, level).
- **Economy:** e.g. `wallets`, `transaction_log`.
- **Calendar, events, clans, portals, quests:** each will have their own tables.

---

## Summary

| You want… | What to do |
|-----------|------------|
| Easiest (no DB install) | Use default `db.yml` (H2). No SQL, no tables to create. Each backend has its own file. |
| One shared DB for all 3 servers | Use MySQL. Create DB `lw_player`, run the `player_profiles` SQL above once, then set `db.yml` on each backend to that MySQL `jdbc-url`, `username`, `password`. |

**jdbc-url** = “where is the database?” (file path for H2, host/port/database for MySQL).  
**Tables** = start with `player_profiles` only; more will be documented here as we add features.
