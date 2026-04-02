---
title: Lobby & Discord Bot Setup
description: Setup guide for the lobby server and optional Discord bot.
tags:
  - development
  - discord
  - lobby
status: reference
phase: ongoing
owner: dev
action: none
---
# Lobby and Discord bot setup

This guide covers configuring the **lobby server**, **Lobby plugin**, and **Discord verify bot** so that players join the lobby first and must verify via Discord before using the teleporter to Survival.


## 2. Proxy (Velocity / BungeeCord-style)

In `Server/proxy/config.yml`:

1. **Register the lobby server** under `servers:`:
   ```yaml
   lobby:
     motd: Lost Wilderness Lobby
     address: 127.0.0.1:25568
     restricted: false
   ```

2. **Make lobby the default** by putting it first in `priorities:`:
   ```yaml
   priorities:
     - lobby
     - survival
     - amplified
   ```
   With `force_default_server: true`, new connections go to the lobby.

Restart the proxy after changes.


## 4. Database (MySQL)

- Use one database (e.g. `minecraft`) for:
  - Lobby plugin (reads/writes `pending_verification`, reads `linked_accounts`).
  - Discord bot (reads/writes `pending_verification` and `linked_accounts`).
  - Survival/Amplified (CalendarSyncManager can read `linked_accounts` for Discord ID by MC username).

Tables:

- **pending_verification:** `mc_username` (PK), `created_at`. Lobby plugin inserts; Discord bot deletes after successful `/verify`.
- **linked_accounts:** `discord_id`, `mc_username` (PK). Discord bot writes; lobby (and optionally other plugins) read.

---

## 5. Quick checklist

| Step | Action |
|------|--------|
| 1 | Lobby `server.properties`: port 25568, `online-mode=false`. |
| 2 | Proxy: add `lobby` server (127.0.0.1:25568), put `lobby` first in `priorities`. |
| 3 | Copy `LostWilderness-Lobby-all.jar` to `Server/servers/lobby/plugins/`. |
| 4 | Configure `plugins/LostWilderness-Lobby/config.yml` (target-server, mysql, teleporter-block). |
| 5 | Place teleporter block (e.g. END_GATEWAY) in lobby world. |
| 6 | Create Discord application, invite bot (Manage Nicknames, applications.commands), set `DISCORD_TOKEN` and `DISCORD_CLIENT_ID`. |
| 7 | Set MySQL env for the bot; run `npm install && npm run build && npm start` in `DiscordBot/`. |
| 8 | Restart lobby server and proxy. |

See also: [DiscordBot/README.md](../../DiscordBot/README.md), [Lobby and verification (player)](../player/lobby-and-verification.md).
