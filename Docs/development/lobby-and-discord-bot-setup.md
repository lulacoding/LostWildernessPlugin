# Lobby and Discord bot setup

This guide covers configuring the **lobby server**, **Lobby plugin**, and **Discord verify bot** so that players join the lobby first and must verify via Discord before using the teleporter to Survival.

---

## 1. Lobby server (Paper)

- **Location:** `Server/servers/lobby/`
- **Port:** 25568 (must not conflict with proxy on 25565 or other backends).

### server.properties

- `server-port=25568`
- `query.port=25568` (or leave query disabled)
- `online-mode=false` (proxy does authentication)
- `motd=Lost Wilderness Lobby` (optional)
- Same proxy-style settings as survival/amplified (e.g. `prevent-proxy-connections=false`).

### Plugins

- Copy **LostWilderness-Lobby-all.jar** from `Plugin/lobby/build/libs/` (or from `Plugin/plugins/` after a full build) into `Server/servers/lobby/plugins/`.
- No other Lost Wilderness plugins are required on the lobby; the lobby plugin uses its own config and MySQL.

### Config (after first run)

- `plugins/LostWilderness-Lobby/config.yml`:
  - **target-server:** `survival` (must match the proxy server name).
  - **mysql:** Same as your other servers (host, port, database, username, password).
  - **teleporter-block:** `END_GATEWAY` (default). Players step on this block to go to Survival when verified. You can use another block type (e.g. `STONE_PRESSURE_PLATE`) if you prefer.

### World

- Place at least one **END_GATEWAY** block (or your chosen teleporter block) where you want the teleporter. Set spawn so new players land near it.

---

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

---

## 3. Discord verify bot

- **Location:** `DiscordBot/`
- **Stack:** Node.js 18+, TypeScript, discord.js v14, express, mysql2.

### Environment variables

- **DISCORD_TOKEN** — Bot token from [Discord Developer Portal](https://discord.com/developers/applications) → your app → Bot → Reset Token.
- **DISCORD_CLIENT_ID** — Application ID (same as “Client ID” in OAuth2).
- **DISCORD_GUILD_ID** — ID of the Discord server where the bot is installed.
- **MYSQL_HOST**, **MYSQL_PORT**, **MYSQL_DATABASE**, **MYSQL_USER**, **MYSQL_PASSWORD** — Same database as the lobby and survival server (e.g. `minecraft`).
- **LOBBY_HOST / LOBBY_PORT** — Java lobby backend, default `127.0.0.1:25568`.
- **SURVIVAL_HOST / SURVIVAL_PORT** — Survival backend, default `127.0.0.1:25566`.
- **AMPLIFIED_HOST / AMPLIFIED_PORT** — Amplified backend, default `127.0.0.1:25567`.

### Invite the bot

- In Developer Portal → OAuth2 → URL Generator: scopes **bot**, **applications.commands**; permissions **Manage Nicknames**, **Send Messages**, **Use Application Commands**.
- Open the generated URL and add the bot to your Discord server.

### Build and run

```bash
cd DiscordBot
npm install
npm run build
npm start
```

For development: `npm run dev` (uses ts-node).

### Behaviour

- **Slash command /verify &lt;mc_username&gt;:** Checks `pending_verification` (created when the player runs `/verify` in-game). If present, links the Discord user to that Minecraft username in `linked_accounts`, sets the member’s server nickname to the IGN, and removes the pending row.
- **Slash command /players:** Queries the Java servers (lobby, survival, amplified) by host/port and shows total and per‑server player counts in Discord.
The bot creates the tables `pending_verification` and `linked_accounts` on first run if they do not exist. Use the **same MySQL database** as the lobby so the Lobby plugin can check `linked_accounts` for verified players.

---

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
