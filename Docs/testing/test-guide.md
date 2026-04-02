---
title: Test Guide
description: General testing guide and approach for PluginV2.
tags:
  - testing
status: reference
phase: ongoing
owner: dev
action: none
---
# Test guide

Use this guide to test every major function after the server is installed and running (see [Install and run](install-and-run.md)).


## 1. Permissions quick reference

| Permission | Command / feature |
| ---------- | ----------------- |
| (none) | `/date`, `/time`, `/datejoined`, `/season`, `/eoc`, `/lwhelp` |
| `lw.admin.calendar` | `/resetcalendar`, `/nextday` |
| `lw.admin.config` | `/lwconfig` |
| `lw.admin.plugins` | `/lwplugins` |
| `lw.portals.use` | `/portals` |
| `lw.portals.delete` | `/deleteportals` |
| `lw.clan.use` | `/clan` |
| `lw.clan.alliance` | `/alliance` |
| `lw.clan.war` | `/war` |
| `lw.admin.disc` | `/give-disc` (grant reward discs) |


## 3. Calendar and date

| Command | Expected |
| ------- | -------- |
| `/date` | Current in-game date (e.g. “March 1, 1MC”). |
| `/time` | Current in-game clock time. |
| `/datejoined` | Day you first joined and days ago. |
| `/season` | Calendar day, year, Z, hemisphere, season at your location. |
| `/eoc` | Days since Epoch (Day 0). |
| `/lwdebug getday` | Current calendar day. |
| `/lwdebug setday <n>` | Set day. |
| `/lwdebug nextday` or `/nextday` | Advance by one day (admin). |
| `/lwdebug stats` | Current day, leader/follower, DB status. |


## 5. Clans

- `/clan` or `/clan help` — list subcommands.
- `/clan create <name>`, `/clan invite <player>`, `/clan accept`/`deny`, `/clan list`, `/clan info`, `/clan leave`.
- `/alliance` — create, invite, join, leave, info, list.
- `/war declare <clan>`, `/war ceasefire <clan>`, `/war end <clan>`, `/war status`.
- Confirm clan tag/name in tab or scoreboard for clan members.


## 7. New Year firework display

1. **When it runs:** Only on the **calendar leader** server when the calendar day advances and the new day is **January 1** of a new year (i.e. year increased from previous day). So you need to advance the calendar day-by-day until you cross from Dec 31 to Jan 1, or use `/lwdebug setday` to set a day that is Jan 1 (e.g. day 365 for end of year 1, then `/nextday` to get to day 366 = Jan 1 year 2).
2. **Trigger:** Use `/lwdebug getday` and `/lwdebug setday <n>` so that the **next** `/nextday` lands on Jan 1. Example: if Jan 1 year 2 is day 366, set day to 365, then run `/nextday`; the handler runs for day 366. Or sleep/advance until the server crosses the year boundary.
3. **Where:** Fireworks spawn in the **overworld** at configurable (x, z), default **(0, 0)**. The Y is the highest block at that (x,z) + 1. Ensure overworld is loaded and (0,0) is safe.
4. **Scale:** Normal year = 3 rockets; new decade (year 10, 20, …) = 8; new century (100, 200, …) = 20; new millennium (1000, 2000, …) = 50. Check config `newyear-firework.x` and `newyear-firework.z` (under Survival/Amplified config, not common).
5. **Leader only:** If you run a follower server, fireworks will **not** run there; only the leader advances the day and runs the handler. So test on the leader instance.


## 9. Discord war webhook

1. **Config:** In the server config (e.g. Survival or Amplified `config.yml`), set `discord.war-webhook-url` to a valid Discord channel webhook URL (create one in Channel Settings → Integrations → Webhooks). Leave empty to disable.
2. **Declare war:** As a clan leader, run `/war declare <other clan name>`. A Discord embed should post to the webhook with title "War Declared", clan names, and current in-game date.
3. **Ceasefire:** Run `/war ceasefire <clan name>`. Embed "War Ended" with status ceasefire and date.
4. **End war:** Run `/war end <clan name>`. Embed "War Ended" with status ended and date.
5. **No webhook:** If URL is empty or invalid, no error in-game; webhook is skipped. Check logs for "Discord war webhook" if you see failures.


## 11. Disc rewards

1. **Permission:** `lw.admin.disc` (default OP). Required for `/give-disc`.
2. **Config:** `discs.main-theme` and `discs.get-lost` (material names, e.g. MUSIC_DISC_PIGSTEP, MUSIC_DISC_OTHERSIDE). If missing, defaults are used.
3. **Give to self:** `/give-disc main_theme` — you receive one Main Theme disc (config material, display name "Main Theme"). `/give-disc get_lost` — you receive one Get Lost disc.
4. **Give to another:** `/give-disc main_theme <player>` — that player receives the disc. They get a message if they're online.
5. **Invalid reward:** `/give-disc invalid` — error "Unknown disc. Use main_theme or get_lost."
6. **Inventory full:** If the target's inventory is full, the disc is dropped at their feet. No loss.
7. **Integration:** When 200% advancement completion is implemented, the code should call `DiscRewardService.giveDisc(player, DiscRewardService.REWARD_MAIN_THEME)`; no need to use the command for that case.


## 13. Troubleshooting

| Symptom | Check |
| ------- | ----- |
| **Plugins fail to enable:** `HikariPool` / `Communications link failure` / `Connection refused: getsockopt` | **MySQL is not running or not reachable.** Start MySQL (e.g. `net start MySQL80` on Windows, or your MySQL service). Ensure `mysql.host` and `mysql.port` (default 3306) in each server’s config point to the running instance. Create the database and user (e.g. `mysql.database: minecraft`, `mysql.username` / `mysql.password` with GRANT on that DB). |
| “Unknown command” | Plugin loaded? `/lwplugins`. Correct spelling. |
| “You do not have permission” | OP or grant permission from §1. |
| Calendar not updating | DB/config; `server-name` set; sync interval. |
| Events don’t start | Config sections (e.g. `events.blizzard`); `/lwdebug help`; biome/region. |
| Portals empty or error | MySQL in `configs/common/config.yml`; DB and user exist; table `portal_transfer_entities` exists. |
| Entity not transferred through portal | Check both backends use same DB; check logs for PortalEntitySpawner/savePortalTransferEntities; ensure entity type is in PortalEntitySerializer.isTransferable(). |
| New Year fireworks not firing | Only leader runs it; day must advance to Jan 1 (use setday + nextday); overworld (0,0) must exist. |
| War head never drops | 1/1000 (war) or 1/10000 (else) is random; ensure clans are at war (status active) for 1/1000. |
| Discord war webhook not posting | Config `discord.war-webhook-url` set? URL valid? Check logs for "Discord war webhook". |
| Calendar item does nothing | Item material and display name must match config exactly (calendar-item.material, calendar-item.display-name). |
| /give-disc unknown or no permission | Permission lw.admin.disc; use main_theme or get_lost. |
| Config change not applied | `/lwplugins reload <name>` or full restart. |

For commands and design, see [Player guide](../player/README.md) and [Implementation status](../implementation-status.md).
