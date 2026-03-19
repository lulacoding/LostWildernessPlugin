# Test guide

Use this guide to test every major function after the server is installed and running (see [Install and run](install-and-run.md)).

---

## Before you start

- Server running Paper 1.21.11 with **LostWilderness-Common** and **LostWilderness-Survival** (and any other plugins you want to test).
- You have **OP** or the permissions below.
- **MySQL must be running** for Survival, Portals, Clans, Calendar, and Amplified. If you see `Communications link failure` / `Connection refused` and plugins fail to enable, start MySQL and ensure the DB and user exist (see §14 Troubleshooting).
- For **Portals:** MySQL configured in the Common config (each server’s `plugins/LostWilderness-Common/config.yml` or bundled `config.yml` with `mysql.host`, `mysql.port`, `mysql.database`, `mysql.username`, `mysql.password`).
- For **Link approval:** optional; commands work without a real Discord bot.

---

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

---

## 2. Plugin health and config

- `/lwplugins` or `/lwplugins status` — list loaded plugins and status.
- `/lwplugins health` — load errors summary.
- `/lwplugins info LostWilderness-Survival` — version, main class, dependencies, commands.
- `/lwplugins reload LostWilderness-Survival` — reload after config changes.
- `/lwconfig list` or `/lwconfig reload` (with `lw.admin.config`) — config system check.

---

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

---

## 4. World events (debug)

- `/lwdebug help` — list events and actions.
- For each event: `/lwdebug <event> start` and `/lwdebug <event> end` (e.g. blizzard, frost, spring, autumn, heatwave, storm, monsoon, thunder, fog, eclipse, easter).
- Blizzard/Frost: `/lwdebug blizzard frozen`, `/lwdebug blizzard list` (and same for frost).
- With Eclipse/Thunder/Fog active: sleep should be blocked.

---

## 5. Clans

- `/clan` or `/clan help` — list subcommands.
- `/clan create <name>`, `/clan invite <player>`, `/clan accept`/`deny`, `/clan list`, `/clan info`, `/clan leave`.
- `/alliance` — create, invite, join, leave, info, list.
- `/war declare <clan>`, `/war ceasefire <clan>`, `/war end <clan>`, `/war status`.
- Confirm clan tag/name in tab or scoreboard for clan members.

---

## 6. Portals

- **Permission:** `lw.portals.use`.
- `/portals` — list linked portals.
- Create/link portals in-game per server rules; run `/portals` again to confirm.
- `/deleteportals <username>` (with `lw.portals.delete`) — remove all portals for that player.
- With BungeeCord: test cross-server teleport and that mount/passengers go through on same-server teleport.

### 6.1 Cross-server entity transfer (detail)

1. **Setup:** Two backends (Survival + Amplified) behind BungeeCord, shared MySQL with `portals`, `pending_portals`, and `portal_transfer_entities` tables.
2. **First-time cross with horse:** On Survival, tame a horse, saddle it, ride it. Build and light a Crying Obsidian portal, then ride **into** the portal (step onto the End Gateway blocks). You should be sent to Amplified. After joining Amplified, you should be teleported to the exit location **and** your horse should spawn and you should be on it. If the horse is missing, check server logs for PortalEntitySpawner/PortalEntitySerializer and DB for `portal_transfer_entities` (row should be consumed after spawn).
3. **First-time cross with boat:** Enter a boat, paddle into the portal. On the other server you should appear in a boat at the exit.
4. **Re-use (permanent portal) with mount:** Link a portal (first cross), then return. Use the same portal again while on a horse/boat. Entity data is saved; on the other server there is no pending row so the player joins at spawn—entity transfer for re-use is currently applied when a pending portal is processed (first-time). So for re-use, test without a mount or expect to arrive at spawn without the mount unless you add logic to teleport+spawn on join for re-use.
5. **Same-server:** On one backend, ride a horse into an already-linked portal that teleports you to another location on the **same** server (return portal same world). Horse and player should both teleport (PortalTeleportHelper).
6. **DB check:** After a first-time cross with a vehicle, query `SELECT * FROM portal_transfer_entities;` — it should be empty (consumed) or have one row before the spawn runs. No duplicate spawns.

---

## 7. New Year firework display

1. **When it runs:** Only on the **calendar leader** server when the calendar day advances and the new day is **January 1** of a new year (i.e. year increased from previous day). So you need to advance the calendar day-by-day until you cross from Dec 31 to Jan 1, or use `/lwdebug setday` to set a day that is Jan 1 (e.g. day 365 for end of year 1, then `/nextday` to get to day 366 = Jan 1 year 2).
2. **Trigger:** Use `/lwdebug getday` and `/lwdebug setday <n>` so that the **next** `/nextday` lands on Jan 1. Example: if Jan 1 year 2 is day 366, set day to 365, then run `/nextday`; the handler runs for day 366. Or sleep/advance until the server crosses the year boundary.
3. **Where:** Fireworks spawn in the **overworld** at configurable (x, z), default **(0, 0)**. The Y is the highest block at that (x,z) + 1. Ensure overworld is loaded and (0,0) is safe.
4. **Scale:** Normal year = 3 rockets; new decade (year 10, 20, …) = 8; new century (100, 200, …) = 20; new millennium (1000, 2000, …) = 50. Check config `newyear-firework.x` and `newyear-firework.z` (under Survival/Amplified config, not common).
5. **Leader only:** If you run a follower server, fireworks will **not** run there; only the leader advances the day and runs the handler. So test on the leader instance.

---

## 8. War: player head drop

1. **Setup:** Two clans with **active** war. Clan A and Clan B: `/war declare` from one leader against the other; `/war status` shows active.
2. **In-war kill:** As a member of Clan A, kill a member of Clan B (or vice versa) in PvP. On each kill the plugin rolls: **1/1000** chance to drop the **victim's player head** at the death location. So you may need many kills to see one (or temporarily lower the chance in code for testing). When it drops, the head item should have the victim's skin and a name like "PlayerName's Head".
3. **Non-war kill:** Kill a player who is not in a clan at war with yours (or same clan, or no clan). Roll is **1/10000** for a head drop. Again, low chance; test with many kills or a code tweak.
4. **No head when not at war:** If clans are not at war (or one has no clan), the 1/10000 roll applies. If you disable war (ceasefire/end), the next kill should use 1/10000.
5. **Location:** Head drops at `victim.getLocation()`; dropped on a 1-tick delay so it appears in the world. Check that the item exists and is a player head with correct owner.

---

## 9. Discord war webhook

1. **Config:** In the server config (e.g. Survival or Amplified `config.yml`), set `discord.war-webhook-url` to a valid Discord channel webhook URL (create one in Channel Settings → Integrations → Webhooks). Leave empty to disable.
2. **Declare war:** As a clan leader, run `/war declare <other clan name>`. A Discord embed should post to the webhook with title "War Declared", clan names, and current in-game date.
3. **Ceasefire:** Run `/war ceasefire <clan name>`. Embed "War Ended" with status ceasefire and date.
4. **End war:** Run `/war end <clan name>`. Embed "War Ended" with status ended and date.
5. **No webhook:** If URL is empty or invalid, no error in-game; webhook is skipped. Check logs for "Discord war webhook" if you see failures.

---

## 10. Calendar item (right-click book)

1. **Config:** `calendar-item.material` (default CLOCK), `calendar-item.display-name` (default "Calendar"). The item in hand must match **both** material and display name (exact string).
2. **Get the item:** Give yourself a clock and name it: `/give @p clock{display:{Name:'{"text":"Calendar"}'}}` (or use a sign/anvil to name it "Calendar"). Or change config to a different material/name.
3. **Right-click:** Hold the calendar item and right-click air or a block. A **written book** should open with title "Calendar", one page showing today's date (e.g. "March 1, 1MC"), year, season, and day number. No inventory GUI; the book view is the calendar.
4. **Wrong item:** If the item is not the configured material or name, nothing happens. If the name has color codes or extra spaces, it may not match.
5. **Season:** The book shows the season at the **player's current Z** (hemisphere). Move north/south and open again to see season change if you cross a season boundary.

---

## 11. Disc rewards

1. **Permission:** `lw.admin.disc` (default OP). Required for `/give-disc`.
2. **Config:** `discs.main-theme` and `discs.get-lost` (material names, e.g. MUSIC_DISC_PIGSTEP, MUSIC_DISC_OTHERSIDE). If missing, defaults are used.
3. **Give to self:** `/give-disc main_theme` — you receive one Main Theme disc (config material, display name "Main Theme"). `/give-disc get_lost` — you receive one Get Lost disc.
4. **Give to another:** `/give-disc main_theme <player>` — that player receives the disc. They get a message if they're online.
5. **Invalid reward:** `/give-disc invalid` — error "Unknown disc. Use main_theme or get_lost."
6. **Inventory full:** If the target's inventory is full, the disc is dropped at their feet. No loss.
7. **Integration:** When 200% advancement completion is implemented, the code should call `DiscRewardService.giveDisc(player, DiscRewardService.REWARD_MAIN_THEME)`; no need to use the command for that case.

---

## 12. Regression checklist

- [ ] Server starts with no red errors; Common and Survival load.
- [ ] `/lwplugins` and `/lwplugins health` run without errors.
- [ ] `/date`, `/time`, `/datejoined`, `/season`, `/eoc` return correct data.
- [ ] `/lwdebug getday`, `setday`, `nextday` work; `/date` updates.
- [ ] At least one event: start → verify effect → end → verify cleanup.
- [ ] `/lwhelp` shows expected sections and service status.
- [ ] `/clan help` and create/invite/list work; `/alliance` and `/war` work.
- [ ] `/war declare`, `/war ceasefire`, `/war end`, `/war status` work; Discord webhook posts if configured.
- [ ] War head drop: two clans at war, PvP kill; eventually a victim head can drop (1/1000). Non-war kill uses 1/10000.
- [ ] `/portals` runs; create portal and see it in `/portals`. Cross-server with horse/boat: entity transfers to other server.
- [ ] New Year fireworks: advance calendar to Jan 1 (leader server); fireworks at (0,0) overworld. Scale increases for decade/century/millennium.
- [ ] Calendar item: right-click with configured item (e.g. clock named "Calendar"); book opens with date/season.
- [ ] `/give-disc main_theme` and `/give-disc get_lost [player]` give the configured discs (lw.admin.disc).
- [ ] Lobby verification: in lobby run `/verify`, then in Discord run `/verify <name>`; player can use the teleporter after linking.
- [ ] Config change + reload: behavior reflects new config.

---

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
