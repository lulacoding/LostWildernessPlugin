# Calendar sync

- **Only one server (leader)** may **write** the current day to MySQL. The other server(s) **read** and **poll every 30–60 seconds**; if the local day doesn’t match **calendar.current_day** in the DB, they update their internal state. This avoids a **race condition** when players sleep on different backends (e.g. Survival vs Amplified).
- **Config file** is used for **single-server** persistence; **MySQL** for **multi-server**.
- **Date format:** “1st January 1MC” style; **Day 0 = Epoch**; **/timeset** must **not** affect day count or reset the calendar (known bug to fix).
- **CalendarPlugin** config: **mysql.enabled**, **mysql.allow_day_updates**, **host**, **port**, **database**, **username**, **password**. Table **calendar** with **id** (e.g. 1), **current_day**.
- Full logic and SQL in [03-plugins/calendar](03-plugins/calendar.md).

## Source

- Direct Messages (Grovyle187) leader/polling solution; Bible section 6; 03-plugins calendar.
