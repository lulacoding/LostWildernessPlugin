# Lost Wilderness Discord Verify Bot

Bot for lobby verification: players run `/verify` in-game, then `/verify <mc_username>` in Discord. The bot links the account and sets the user's server nickname to their IGN.

## Requirements

- Node.js 18+
- MySQL database (same as the game server: `minecraft` by default)
- Discord Application with Bot token

## Setup

1. **Discord Developer Portal**
   - Create an application at https://discord.com/developers/applications
   - Bot → Reset Token and copy the token
   - OAuth2 → URL Generator: scopes `bot`, `applications.commands`; permissions: Manage Nicknames, Send Messages, Use Application Commands
   - Invite the bot to your server using the generated URL

2. **Environment variables**

   Create a `.env` file (or set in shell):

   - `DISCORD_TOKEN` — Bot token from Developer Portal
   - `DISCORD_CLIENT_ID` — Application ID (same as “Client ID” in OAuth2)
   - `DISCORD_GUILD_ID` — ID of the Discord server where the bot runs
   - `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` — Same as lobby/survival server MySQL
   - `LOBBY_HOST` / `LOBBY_PORT` — Java lobby server (default `127.0.0.1:25568`)
   - `SURVIVAL_HOST` / `SURVIVAL_PORT` — Survival backend (default `127.0.0.1:25566`)
   - `AMPLIFIED_HOST` / `AMPLIFIED_PORT` — Amplified backend (default `127.0.0.1:25567`)

3. **Install and run**

   ```bash
   npm install
   npm run build
   npm start
   ```

   For development: `npm run dev` (uses ts-node).

## Commands

- **/verify &lt;mc_username&gt;** — Run after `/verify` in-game. Links your Discord to that Minecraft username and sets your server nickname to it. Requires a pending verification (from in-game `/verify`).
- **/players** — Shows total online players and per‑server counts for Lobby, Survival, and Amplified using the configured host/port values.

## Database

The bot creates and uses:

- `pending_verification` — `mc_username`, `created_at` (lobby plugin inserts; bot deletes after successful `/verify`)
- `linked_accounts` — `discord_id`, `mc_username` (shared with lobby plugin and CalendarSyncManager)

Use the same MySQL database as the lobby/survival server so the lobby can check `linked_accounts` for verified players.

For full server setup (lobby server, proxy, plugin config), see [Docs/development/lobby-and-discord-bot-setup.md](../Docs/development/lobby-and-discord-bot-setup.md). Player guide: [Docs/player/lobby-and-verification.md](../Docs/player/lobby-and-verification.md).
