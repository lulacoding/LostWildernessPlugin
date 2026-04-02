---
title: Backup Setup
description: Step-by-step backup configuration and scheduling guide.
tags:
  - operations
  - backup
status: reference
phase: ongoing
owner: ops
action: none
---
## Ops: Backup setup (Ubuntu/Debian + MariaDB)

This page covers the full backup setup on the Linux production host. For disaster recovery and restore procedures see [`Docs/future/backups-and-disaster-recovery.md`](../future/backups-and-disaster-recovery.md).


### Step 1 — Create the credentials file

```bash
sudo cp /path/to/repo/Docs/ops/lw-backup.cnf.example /etc/mysql/lw-backup.cnf
sudo nano /etc/mysql/lw-backup.cnf   # set user + password
sudo chmod 600 /etc/mysql/lw-backup.cnf
sudo chown root:root /etc/mysql/lw-backup.cnf
```


### Step 3 — Edit world-backup.sh for your deploy paths

Open `world-backup.sh` and verify:

- `MC_BASE` matches where your backends live (e.g. `/opt/lostwilderness/Server/backends`).
- `BACKENDS` array has correct systemd unit names (e.g. `lw-survival-1`).


### Step 5 — Run a manual test

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

For a full restore drill see [`Docs/future/backups-and-disaster-recovery.md`](../future/backups-and-disaster-recovery.md).
