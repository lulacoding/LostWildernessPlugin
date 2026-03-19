# Sync and data

## MySQL / HikariCP

- **MySQL** is used for **calendar** and **player_data** (and portal tables). **HikariCP** is the connection pool (standard with many Java plugins).
- **Shared MySQL** for **both** Survival and Amplified **and** the **Discord bot** (e.g. whitelist, link data). So one database serves game backends and bot.
- **Database name:** **minecraft**. Tables include **calendar**, **portals**, **pending_portals** (see [Calendar](03-plugins/calendar.md) and [Portals](03-plugins/portals.md)).

## Calendar sync (high level)

- **One server is the “leader”** (writes the current day to MySQL); the **other server only reads** and **polls every 30–60 seconds** to update its internal day. This avoids a **race condition** where both servers increment the day when players sleep on different backends. See [Calendar sync](06-technical-decisions/calendar-sync.md) for the full logic.

## HuskSync

- **HuskSync** is run **on both** Survival and Amplified for **inventory (and similar) sync** across the BungeeCord network. So when a player switches server, their inventory (and optionally bed spawn, etc.) can follow.

## MySQL Player Data Bridge (Spigot)

- **MySQL Player Data Bridge** (Spigot plugin) was linked and considered for **cross-server player data** (inventory, bed spawn, etc.); it’s **paid** and supports **1.21**. Interest was expressed in **e-chest, shulker, bundle** sync. *“Yeah that would be, even bed spawn is cool as”*, *“I hope that paid plugin also carries over e chest, shulker and bundle data.”*

## Vulcan sync

- **Vulcan** (anticheat) is used **server-side** and is intended to **sync across** the two backends (e.g. so bans/flags follow the player). **Offline inventory checking** was also mentioned in the context of data/anticheat.

## Source

- Direct Messages (Grovyle187): MySQL database minecraft, calendar table, HuskSync on both, MySQL Player Data Bridge link, e-chest/shulker/bundle.
- Bible sections 2 and 3: HikariCP, shared MySQL, calendar leader/polling, Vulcan sync, offline inventory.
