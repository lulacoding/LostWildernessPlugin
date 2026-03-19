# Lobby and queue

**Implementation:** VoidWorldGenerator, DeluxeMenus, custom queue plugin, and PackServer are **not** in the Plugin repo (separate lobby server). **DefaultPackListener** and **PackStatusListener** are implemented in the codebase.

---

## Current implementation (2026)

A **lobby server** and **Lobby plugin** are implemented so that new players join the lobby first (proxy default), then verify via Discord and use a teleporter to Survival.

- **Lobby server:** Paper backend at `Server/servers/lobby/`, port **25568**. Proxy sends new connections to the lobby (lobby first in `priorities`).
- **LostWilderness-Lobby plugin** (`Plugin/lobby`):  
  - **/verify** — Creates a pending verification; player then runs **/verify &lt;mc_username&gt;** in Discord (see [Discord bot](../../02-infrastructure/discord-bot.md)).  
  - **Teleporter** — One block type (default END_GATEWAY); stepping on it sends the player to the Survival server via BungeeCord **only if** they are in `linked_accounts` (verified).  
  - Shared MySQL: `pending_verification`, `linked_accounts`. Config: `target-server`, `mysql`, `teleporter-block`.
- **Discord verify bot** (`DiscordBot/`): Slash **/verify &lt;mc_username&gt;** (verification) and **/players** (aggregated player counts for lobby/survival/amplified); links account, sets server nickname to IGN. Same DB as lobby.

Player flow: join → lobby → **/verify** in-game → **/verify IGN** in Discord → step on teleporter → Survival. See [Lobby and verification](../../../player/lobby-and-verification.md) and [Lobby and Discord bot setup](../../../development/lobby-and-discord-bot-setup.md).

---

## Lobby server (design / future)

- **Void/empty** world (End-style void); **VoidWorldGenerator**.
- **Spectator** mode; **chest GUI menus** (e.g. **DeluxeMenus**); **port 25568**.
- **Link Discord**, **clan/ally list**, **queue** (custom queue plugin using **Discord + MySQL**).
- **Accept resource pack** before **Play**; **client check** (Vulcan/Negativity).
- **Play** = go to **Survival** or **“where you left off.”**
- **Bedrock:** If chest GUI fails, **CLI lobby** or tour fallback.
- **Staff priority** on queue (login doesn’t count toward cap).
- **Creative-while-queued** option.

## PackServer and DefaultPackListener

- **PackServer** serves packs from **/packs/*.zip** (or similar path).
- **DefaultPackListener** (in codebase) applies the default/Lost Wilderness pack; **Floodgate bypass** if Bedrock pack fails.

## Creative server

- **10 public plots** (monthly wipe), **private/clan/alliance** plots (yearly, max 2 years); **superflat**, **peaceful**, no day/night, **1M×1M**; must go via Lobby; **inventory swap + Vulcan check** on Survival load; **command blocks disabled**.
- **Hard border** between Lost Wilderness worlds and Creative.

## Lobby datapack (1.21.6)

- **Datapack ver 77** (1.21.6): **terms & conditions**, **cutscene box**, **custom menus**; lobby code waiting on 1.21.6.

## Source

- Bible sections 3 and 5: VoidWorldGenerator, DeluxeMenus, queue plugin, PackServer, DefaultPackListener, Creative rules, staff priority, datapack 77.
