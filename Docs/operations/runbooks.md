---
title: Runbooks
description: Operational runbooks for common server tasks and incidents.
tags:
  - operations
  - runbooks
status: reference
phase: ongoing
owner: ops
action: none
---
## Future: Ops Playbooks & Runbooks

This page is for **checklists you (or other staff) can follow under pressure** when something goes wrong. It complements the more conceptual future docs by giving concrete “do this, then this” steps.

Start with a few common incidents and expand over time.


### 2. Playbook: Server crash / TPS death

**Symptoms:**

- Players report lag or disconnects.
- Monitoring shows TPS < 10 or server offline.

**Runbook (per backend):**

1. Check monitoring:
   - Confirm which server is affected (`survival-1`, `amplified-1`, `lobby-1` in ServerUPDATE).
   - Note player count before crash, CPU/RAM trends.
2. Inspect logs:
   - Open latest logs for that backend.
   - Look for:
     - Watchdog crash report.
     - Plugin stack traces (Calendar, Portals, Clans, etc.).
3. Contain:
   - If it’s **just Survival/Amplified**, make sure Lobby is healthy so players can reconnect there.
   - Disable any obviously broken experimental features if indicated by logs.
4. Restart:
   - Restart the affected backend.
   - Watch console for MySQL errors or plugin enable failures.
5. Communicate:
   - Post a short message in the appropriate Discord channel:
     - Which server was affected.
     - Whether rollback is expected or not.
6. Follow‑up:
   - If this is a new/crashing bug, create a ticket linking:
     - Crash log.
     - Approximate time and player count.

Add specific branches here as you find common patterns (e.g. “crash when a particular event fires”).


### 3. Playbook: MySQL issues

**Symptoms:**

- In logs: `Communications link failure`, `Access denied`, or “table marked as crashed”.
- Plugins that depend on DB (Portals, Clans, Calendar, Lobby, Discord bot) fail to enable or log errors.

**Runbook:**

1. Identify scope:
   - Is MySQL down or just one table corrupted?
   - Are all servers affected or only some features?
2. If MySQL is **down/not accepting connections**:
   - Check the DB service status.
   - Restart service if safe.
   - If it fails again, check disk space and logs.
3. If specific tables are **corrupted**:
   - Stop or quiet heavy write servers if needed.
   - Run appropriate repair tools (e.g. `aria_chk` / `mysqlcheck`) following the DB doc.
   - If repair fails, consider restoring from backup per the DB DR plan.
4. Once DB is healthy:
   - Restart affected Minecraft servers and Discord bot.
5. Communicate:
   - Inform players if persistent outages occurred.

**See also:** [Database architecture & tuning](database-architecture-and-tuning.md) (DB incident playbook, repair vs restore), [Backups & disaster recovery](backups-and-disaster-recovery.md) (restore procedure, DR scenarios).


### 5. Playbook: Discord bot / verification problems

**Symptoms:**

- `/verify` in Discord doesn’t respond.
- Players verified in Discord can’t use the lobby teleporter.

**Runbook:**

1. Check Discord bot logs:
   - Is the bot online (`Discord bot ready: ...`)?
   - Any DB connection errors?
2. Check DB:
   - Inspect `pending_verification` and `linked_accounts` for the affected username.
3. Test:
   - Manually run `/verify` flow on a test account.
4. Restart:
   - If needed, restart the bot (`npm start`) after resolving DB/config issues.

Add shortcuts here (e.g. SQL queries you regularly use to debug verification issues).

**See also:** [Database architecture & tuning](database-architecture-and-tuning.md) (linked_accounts, pending_verification), [Backups & disaster recovery](backups-and-disaster-recovery.md) (restore if DB corrupted).

---

### 6. Improving playbooks over time

Whenever an incident occurs:

- After it’s resolved, add:
  - What happened.
  - What fixed it.
  - What you wish had been easier or faster.

Gradually, this page should become the **first place you look** when something breaks, and each new issue should make it stronger.

