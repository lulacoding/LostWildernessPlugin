---
title: Backup Strategy
description: Database and world backup procedures for disaster recovery.
tags:
  - operations
  - backup
status: reference
phase: ongoing
owner: ops
action: none
---
## Ops: Backup setup (Ubuntu/Debian + MariaDB)

This page covers the full backup setup on the Linux production host. For disaster recovery and restore procedures see [Disaster recovery runbook](#disaster-recovery-runbook-long-form) (below).


### Step 1 â€” Create the credentials file

```bash
sudo cp /path/to/repo/Docs/ops/lw-backup.cnf.example /etc/mysql/lw-backup.cnf
sudo nano /etc/mysql/lw-backup.cnf   # set user + password
sudo chmod 600 /etc/mysql/lw-backup.cnf
sudo chown root:root /etc/mysql/lw-backup.cnf
```


### Step 3 â€” Edit world-backup.sh for your deploy paths

Open `world-backup.sh` and verify:

- `MC_BASE` matches where your backends live (e.g. `/opt/lostwilderness/Server/backends`).
- `BACKENDS` array has correct systemd unit names (e.g. `lw-survival-1`).


### Step 5 â€” Run a manual test

```bash
sudo systemctl start lw-backup.service

# Watch live output
sudo journalctl -u lw-backup.service -f

# Check log files
cat /var/log/lw-backup/mariadb-backup.log
cat /var/log/lw-backup/world-backup.log
cat /var/log/lw-backup/prune-backups.log

# Check files were created
ls -lh /backups/mysql/
ls -lh /backups/worlds/survival-1/
```

A successful run produces files like:
- `/backups/mysql/minecraft-2026-03-11.sql.gz`
- `/backups/worlds/survival-1/worlds-2026-03-11.tar.gz`


### Verify a backup is usable (quick check)

```bash
# Test that the gzip is valid (not truncated)
gzip -t /backups/mysql/minecraft-YYYY-MM-DD.sql.gz && echo "OK"

# Preview first few lines of the dump
zcat /backups/mysql/minecraft-YYYY-MM-DD.sql.gz | head -30

# Test that the world archive is valid
tar -tzf /backups/worlds/survival-1/worlds-YYYY-MM-DD.tar.gz | head -20
```

For a full restore drill see [Disaster recovery runbook](#disaster-recovery-runbook-long-form) below.



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


### 5. Disaster recovery scenarios

1. **Single backend corruption (e.g. Survival world corrupt)**
   - Stop Survival: `sudo systemctl stop lw-survival-1`
   - Restore world backup (see restore procedure below).
   - Run a quick region check, then restart.
   - Announce rollback window in Discord.
   - *Estimate:* 30â€“60 min depending on backup size.

2. **DB corruption or failure**
   - Stop all backends + Discord bot.
   - Restore `minecraft` DB from latest backup (see DB restore procedure below).
   - Restart backends; check logs for plugin enable errors.
   - *Estimate:* 1â€“2 hours including verification.

3. **Full machine loss**
   - Provision new Ubuntu/Debian host.
   - Install Java, MariaDB, pull git repo.
   - Apply MariaDB config drop-in, restore DB + worlds from off-site backups.
   - Update DNS / proxy IP.
   - *Estimate:* 2â€“4 hours.


### 7. Restore procedure â€” world backup

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
| *(fill after first drill)* | DB + survival-1 world | â€” | â€” |
