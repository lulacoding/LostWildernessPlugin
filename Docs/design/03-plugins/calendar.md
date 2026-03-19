# Calendar plugin

**Implementation:** See [implementation-status.md](../../implementation-status.md). Implemented: /date, /time, /datejoined, /season, /resetcalendar (Survival), MySQL sync, day-advance. **Not implemented:** /eoc, /calender stats; **partial:** /nextday only via `/lwdebug nextday` (admin).

## Purpose

The Calendar plugin tracks **in-game time** and provides a **calendar system** with **days, months, years, and leap years**. It calculates the current day, month, and year from the **Minecraft day–night cycle** so that one full in-game day advances the calendar by one day. **Leap years** occur every 4 years (366 days); otherwise years are 365 days. The calendar **persists across server restarts** (config file for single-server; MySQL for multi-server). It is intended to support RP, war tracking, museum dates, horse birth/death for tombstones, and “Epoch” history.

From the original plugin description sent in chat: *“The Calendar Plugin is a Minecraft server plugin that tracks in-game time and provides a calendar system with days, months, years, and leap years. It calculates the current day, month, and year based on the Minecraft day-night cycle, ensuring natural day progression. Leap years occur every 4 years, adding an extra day to the last month, making the calendar feel realistic.”*

## Commands

- **/date** — Displays the current in-game year, month, and day. Preferred format from chat: **“1st January 1MC”** and **“Day 66”** style; **Day 0** is **“Epoch”** with subtitle **“A new era begins”**; from Day 1 onwards **1/1/1MC** through **x/x/xMC**.
- **/resetcalendar** — Resets the calendar to **Year 1, Month 1, Day 1**. Restricted to **admins or console**.
- **/nextday** — (Dev/test) Advances the server by one day for testing. **[Partial: only via `/lwdebug nextday` (admin), no standalone /nextday]** *“you can test this with the command /nextday which will send the server ahead aday.”*
- **/time** — In-game time (no title; command only). Used for war declarations, dollar statements, time of birth of horses, journaling. *“50 seconds to an hour with 20 TPS.”*
- **/eoc** — **Days since Epoch** (Day 0). **[Not implemented]**
- **/datejoined** — For player (and console: **/datejoined &lt;username&gt;**).
- **/calender stats** — Shows plugin stats (note: spelling “calender” in command). **[Not implemented]**

## Permissions

- **calendarplugin.reset** — Allows resetting the calendar (default: OP).

## Date and time format (from chat)

- **Grovyle187:** *“Another thing is I'd prefer it to read 1st January 1MC and I would like Day 0 to be "Epoch" and Day 1 onwards 1/1/1MC-x/x/xMC.”*
- **January 1, Year 1:** In F3 day counter terms, the question was whether that is **day 0 or day 1**; the plugin uses **Day 0 = Epoch**.
- **29th February** is **“Leap Day”** in leap years.
- **Month names** and correct **31-/30-day months** were added (e.g. 1.0.1); **28-day February** and **29-day February on leap years** were explicitly requested.

## Persistence (single server)

- Data is stored in a **config file** so that after server restarts the date is preserved. *“also it should store the data in a config file so when server restarts etc the date stays.”*
- **/timeset** (vanilla) must **not** advance or reset the calendar; the calendar only advances when a **full natural day** has passed. *“also time set doesnt effect it so it only progresses to the next day when its gone a whole day natuarlly.”* (A bug was noted: timeset sometimes still affected the count and could reset the calendar to 0; to be fixed.)

## MySQL sync (multi-server)

- **One server is the “leader”** — only that server **writes** the current day to MySQL. The other server(s) **read** and **poll every 30–60 seconds** and update their internal day if it’s out of sync. This avoids a **race condition** when players sleep on different backends.
- **lula.pog** (solution): *“Only one server is allowed to make updates to the calendar day. This avoids a ‘race condition’ where both servers increment the day independently. Pick one server as the ‘leader’ Or set a column like calendar.owner = 'server1' so only one server writes, the other just reads. When a player sleeps on Server 2, before changing the day: Server 2 should read the current day from MySQL. If its internal day matches the DB, it can increment it. Then update the DB. Server 1 checks MySQL every 30–60 seconds and updates its internal day if out-of-sync. This means only one server ‘moves time forward,’ but both read from the same global state. Periodic Polling (All Servers) Every 30s–1min (or longer if strains server ill optimize tho): Each server reads calendar.current_day. If the local day doesn’t match the DB, it updates its internal state.”*
- **Amplified** server: calendar mainly shows **/date**; it takes **about 60 seconds** to sync after a new day. *“calander works on amp server to mainly just date command, but it takes about 60 seconds to sync after a new day is done.”* That was accepted as fine (*“That's fine a minute is a 20th of a day”*).

## MySQL setup (from chat)

- **Database:** **minecraft**
- **Table:** **calendar**

SQL used:

```sql
CREATE DATABASE minecraft;
-- Then after USE minecraft;
CREATE TABLE IF NOT EXISTS calendar (
  id INT PRIMARY KEY DEFAULT 1,
  current_day INT NOT NULL
);
INSERT INTO calendar (id, current_day) VALUES (1, 1);
```

(An earlier variant had `id INT PRIMARY KEY AUTO_INCREMENT`, `current_day`, `previous_day`, `last_updated TIMESTAMP`; the simplified version above was used for the two-server setup.)

- **User:** e.g. **mcuser** with `GRANT ALL PRIVILEGES ON calander.* TO 'mcuser'@'%';` (note: chat sometimes used “calander” in the grant; the table name is **calendar**.)

## Config (from chat)

Example **config.yml** for the plugin:

```yaml
mysql:
  enabled: true
  allow_day_updates: true
  host: localhost
  port: 3306
  database: minecraft
  username: root
  password: password
```

*“make sure mysql and allow_day_updates both true.”*

## Plugin placement

- **Calendar JARs** go in **each backend server’s plugins/ folder only** (Survival and Amplified have their own Calendar plugin JAR — e.g. **SurvivalCalendarPlugin** and **AmplifiedCalendarPlugin**). **Nothing** calendar-related in the **BungeeCord plugins/** folder.

## Known bugs / TODOs (from chat)

- **Config folder:** The plugin did not always create its config folder on first run; workaround was to **manually add the folder** (or use a “folderfix” JAR). *“for now ig till i fix it just add the folder”*, *“should say tho ‘MySQL database sync is disabled.’”*
- **Title:** New-day title sometimes **doesn’t show** (roughly **1/7**); not consistently reproducible; to be fixed.
- **/timeset:** Should not affect day count but sometimes did and could **reset calendar to 0**; to be fixed.
- **Log spam:** MySQL day-update logs were verbose; intention to make them **only show in verbose/debug** modes.

## Versions mentioned

- **calendar-1.0-SNAPSHOT**, **1.0.1**, **1.0.2** (month names, 28/29 Feb, etc.). JARs were named e.g. **calendar-1.0-SNAPSHOT.jar**, **SurvivalCalendarPlugin-1.0-SNAPSHOT.jar**, **AmplifiedCalanderPlugin-1.0-SNAPSHOT.jar** (note spelling “Calander” in one).

## Source

- Direct Messages (Grovyle187): full plugin description, /date /resetcalendar /nextday /time /eoc /datejoined /calender stats, 1st January 1MC, Epoch, leader/polling solution, MySQL CREATE TABLE and config, folder bug, title bug, timeset bug, 60s sync, plugin in server folder only.
