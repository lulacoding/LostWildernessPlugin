# Discord bot

## Verify bot (Lost Wilderness)

A **Discord verify bot** is implemented in `DiscordBot/` (Node.js + TypeScript, discord.js v14).

- **Slash command /verify &lt;mc_username&gt;:** Used after the player runs **/verify** in the lobby. The bot checks `pending_verification` for that Minecraft username; if present, it adds the link to `linked_accounts`, sets the member’s **server nickname** to the IGN, and removes the pending row. If there is no pending verification, the bot tells the user to run **/verify** in-game first.
- **Slash command /players:** Queries the Java lobby/survival/amplified backends by host/port and reports total players plus per‑server counts in Discord.
- **Database:** Same MySQL as the game server. The bot creates `pending_verification` and `linked_accounts` if they do not exist. Lobby plugin reads `linked_accounts` to allow verified players to use the teleporter.

Setup: set `DISCORD_TOKEN`, `DISCORD_CLIENT_ID`, `DISCORD_GUILD_ID`, MySQL env, and optional lobby/survival/amplified host/port; invite the bot with **Manage Nicknames** and **applications.commands**; run `npm install && npm run build && npm start`. See [DiscordBot/README.md](../../DiscordBot/README.md) and [Lobby and Discord bot setup](../../development/lobby-and-discord-bot-setup.md).

---

## Linking flow

- Players verify by running **/verify** in the lobby, then **/verify &lt;mc_username&gt;** in Discord.
- The Discord bot writes to `linked_accounts`, and the lobby teleporter checks that table before sending the player to Survival.
- **Bedrock:** Bedrock-specific verification and naming rules are still design questions, not an implemented flow in this repo.

## Whitelist and clan roles

- **Whitelistbot** (by **lulacoding**) is used for whitelist management.
- The bot **creates clan roles and channels** when clans are created in-game (via the clan plugin). So each clan gets a Discord role and optionally a channel; **ally chats** and **alliance** level are mirrored at Discord level.
- **Discord webhooks** are used for **server-wide in-game chat** and **logs** (e.g. to an admin channel).

## Skipping Bedrock in name checks

- For anticheat/name checks, **anyone with a name beginning with \*** (the Bedrock prefix) is **skipped** so Bedrock players are not falsely flagged. *“Also skipping anyone with a name beginning with * which is the symbol for bedrock players.”*

## Source

- Direct Messages (Grovyle187): early Discord linking ideas, Bedrock .Lobsta2 naming, whitelistbot (lulacoding).
- Bible section 2: roles, nickname = MC name, clan roles and channels, webhook logs.
