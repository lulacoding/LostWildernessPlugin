## Future: Backups & Disaster Recovery

This page defines how you **avoid losing the world**, configs, or player data, and what to do when something goes wrong anyway.

The goal is to be able to answer, at any time:

- "If the machine died right now, what would we lose?"
- "How long would it take to be back online?"

---

### 1. What must be backed up

Critical data:

- **Worlds**:
  - Survival, Amplified, Nether/End, any future worlds (Creative, Devoid, etc.).
- **Database**:
  - `minecraft` MySQL/MariaDB database (calendar, clans, portals, verification, etc.).
- **Configs and plugins**:
  - `plugins/` folders on each backend.
  - Central `Configs`/`configs/common` directory if used.
- **Docs and design**:
  - Current Git repo (including `Docs/` and plugin source).

Less critical but nice to have:

- Logs (for forensics).
- Old world snapshots or archived seasons.

---

### 2. Backup strategy overview

**Production: Ubuntu/Debian + MariaDB.** Automated via systemd timer at 03:00 daily (see `Docs/ops/backup-setup.md`).

- **Nightly**:
  - Logical DB backup via `mariadb-backup.sh`.
  - World archives per backend via `world-backup.sh`.
  - Retention pruning via `prune-backups.sh`.
- **Weekly**:
  - Full image / snapshot of the server (if supported by host).
  - Extra copy of configs and plugin jars.
- **Off-site** (future):
  - Push `/backups/` to S3/Backblaze or rsync to another host.

**Retention:** 7 nightly + 4 weekly + 3 monthly. `prune-backups.sh` enforces this automatically each night.

---

### 3. Database backup details

**Automated scripts (Ubuntu/Debian):** see [`Docs/ops/backup-setup.md`](../ops/backup-setup.md) for the complete install guide and [`Docs/ops/mariadb-backup.sh`](../ops/mariadb-backup.sh) for the script.

Setup summary:

```bash
# 1. Create credentials file (once)
sudo cp Docs/ops/lw-backup.cnf.example /etc/mysql/lw-backup.cnf
# edit user/password, then:
sudo chmod 600 /etc/mysql/lw-backup.cnf

# 2. Manual test
sudo bash Docs/ops/mariadb-backup.sh

# 3. Check output
ls -lh /backups/mysql/
gzip -t /backups/mysql/minecraft-$(date +%F).sql.gz && echo "OK"
```

Key points:

- Uses `--single-transaction` so no table locks during dump.
- Output: `/backups/mysql/minecraft-YYYY-MM-DD.sql.gz`
- Logs to `/var/log/lw-backup/mariadb-backup.log`

---

### 4. World backup details

**Automated script:** [`Docs/ops/world-backup.sh`](../ops/world-backup.sh)

The script:

1. Stops each backend via its systemd unit.
2. Archives `world`, `world_nether`, `world_the_end` into `/backups/worlds/<backend>/worlds-YYYY-MM-DD.tar.gz`.
3. Restarts the backend.

Edit `MC_BASE` and `BACKENDS` in the script to match your deploy paths and unit names before use.

```bash
# Manual test (stops + restarts servers — only run during a maintenance window)
sudo bash Docs/ops/world-backup.sh

# Verify archive
tar -tzf /backups/worlds/survival-1/worlds-$(date +%F).tar.gz | head -20
```

---

### 5. Disaster recovery scenarios

1. **Single backend corruption (e.g. Survival world corrupt)**
   - Stop Survival: `sudo systemctl stop lw-survival-1`
   - Restore world backup (see restore procedure below).
   - Run a quick region check, then restart.
   - Announce rollback window in Discord.
   - *Estimate:* 30–60 min depending on backup size.

2. **DB corruption or failure**
   - Stop all backends + Discord bot.
   - Restore `minecraft` DB from latest backup (see DB restore procedure below).
   - Restart backends; check logs for plugin enable errors.
   - *Estimate:* 1–2 hours including verification.

3. **Full machine loss**
   - Provision new Ubuntu/Debian host.
   - Install Java, MariaDB, pull git repo.
   - Apply MariaDB config drop-in, restore DB + worlds from off-site backups.
   - Update DNS / proxy IP.
   - *Estimate:* 2–4 hours.

---

### 6. Restore procedure — MariaDB `minecraft` DB

Run these steps during a maintenance window (all backends and Discord bot must be stopped first).

```bash
# 1. Stop all services
sudo systemctl stop lw-proxy lw-lobby-1 lw-survival-1 lw-amplified-1
# also stop the Discord bot process

# 2. Drop and recreate the database (or use a test DB name for drills)
sudo mariadb -u root -p <<'SQL'
DROP DATABASE IF EXISTS minecraft;
CREATE DATABASE minecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SQL

# 3. Restore from backup
BACKUP_FILE="/backups/mysql/minecraft-YYYY-MM-DD.sql.gz"
zcat "${BACKUP_FILE}" | sudo mariadb -u root -p minecraft

# 4. Quick integrity check
sudo mariadb -u root -p minecraft <<'SQL'
SELECT 'calendar' AS tbl, COUNT(*) FROM calendar
UNION ALL SELECT 'linked_accounts', COUNT(*) FROM linked_accounts
UNION ALL SELECT 'portals', COUNT(*) FROM portals
UNION ALL SELECT 'clans', COUNT(*) FROM clans;
SQL

# 5. Restart services
sudo systemctl start lw-lobby-1
# wait ~10s, check lobby logs
sudo journalctl -u lw-lobby-1 --since "1 minute ago"
sudo systemctl start lw-survival-1 lw-amplified-1
sudo systemctl start lw-proxy
# restart Discord bot

# 6. Test: join lobby, run /date, /portals, /clan list
```

If the DB host changed: update `mysql.host` in each plugin's config and `MYSQL_*` in the Discord bot `.env`, then restart.

---

### 7. Restore procedure — world backup

```bash
BACKEND="survival-1"
BACKUP_FILE="/backups/worlds/${BACKEND}/worlds-YYYY-MM-DD.tar.gz"
BACKEND_DIR="/opt/lostwilderness/Server/backends/${BACKEND}"

# 1. Stop backend
sudo systemctl stop lw-${BACKEND}

# 2. Move current world aside (keep as fallback for 24h)
sudo mv "${BACKEND_DIR}/world"        "${BACKEND_DIR}/world.broken"
sudo mv "${BACKEND_DIR}/world_nether" "${BACKEND_DIR}/world_nether.broken" 2>/dev/null || true
sudo mv "${BACKEND_DIR}/world_the_end" "${BACKEND_DIR}/world_the_end.broken" 2>/dev/null || true

# 3. Extract backup
sudo tar -xzf "${BACKUP_FILE}" -C "${BACKEND_DIR}"

# 4. Start backend, check logs
sudo systemctl start lw-${BACKEND}
sudo journalctl -u lw-${BACKEND} --since "1 minute ago" -f
```

---

### 8. Regular DR drills

Schedule quarterly (minimum):

**Checklist:**

- [ ] Stop all backends and bot in a maintenance window (or use a staging clone).
- [ ] Restore the most recent DB backup into a test database (`minecraft_drill`).
- [ ] Verify row counts match expectations (see step 4 above).
- [ ] Restore survival-1 world backup into a temp directory; confirm files extracted cleanly.
- [ ] Start lobby-1 pointed at `minecraft_drill`; confirm plugins enable without errors.
- [ ] Join lobby-1 as a test account; run `/date`, `/portals`, `/clan list`.
- [ ] Record the outcome in the table below.
- [ ] Clean up drill database and temp world.

**DR drill results:**

| Date | Scope | Outcome | Issues / follow-up |
|------|-------|---------|-------------------|
| *(fill after first drill)* | DB + survival-1 world | — | — |
