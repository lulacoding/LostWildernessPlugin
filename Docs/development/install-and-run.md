# Install and run guide

Full server setup for LostWilderness plugins on **Paper 1.21.11**.

**You will need:** Java 21 (required); MySQL or MariaDB (required for Portals, Clans, and other DB-backed features).

---

## Part 1: Prerequisites

### 1.1 Java (JDK 21)

- **Required:** Java 21 (LTS).
- **Download:** [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64) or pick your OS.
- Set `JAVA_HOME` to the JDK 21 install path.
- **Verify:** `java -version` shows OpenJDK 21.

### 1.2 MySQL or MariaDB

- Required for **Portals**, **Clans**, and DB-backed features.
- **Compatibility:** MySQL 8 or MariaDB 10.x+.

#### Create database and user

```sql
CREATE DATABASE IF NOT EXISTS minecraft
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'minecraft'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD';
GRANT ALL PRIVILEGES ON minecraft.* TO 'minecraft'@'localhost';
FLUSH PRIVILEGES;
```

Use the same host, username, password, and database in `configs/common/config.yml`.

### 1.3 Build the plugins

From the project root (e.g. `Lost Wilderness` or plugin repo root):

```powershell
.\gradlew.bat build
```

JARs are written to the project **`plugins/`** folder.

---

## Part 2: Server directory

1. Create a folder (e.g. `C:\MC-Server`).
2. Download [Paper 1.21.11](https://papermc.io/downloads/paper) and place as `paper.jar`.
3. Run once: `java -Xms1G -Xmx2G -jar paper.jar`, then set `eula=true` in `eula.txt` and run again. Stop the server.

---

## Part 3: Install plugins

Copy from the project **`plugins/`** folder into the server **`plugins/`** folder:

| JAR | Purpose |
| --- | --- |
| `LostWilderness-Common-*-all.jar` | **Required first** (for Survival/Amplified). Shared config and APIs. |
| `LostWilderness-Survival-*-all.jar` | Main plugin: calendar, events, clans, portals, commands. |
| `LostWilderness-Lobby-*-all.jar` | **Lobby server only.** /verify and teleporter to Survival (verified players). |

Optional: `LostWilderness-Amplified-*-all.jar`, Calendar, Clans, Events, Portals.  
Optional dependency: **ProtocolLib** for Fog event visibility.

**Lobby server:** Use a separate Paper server (e.g. port 25568); copy only **LostWilderness-Lobby** into its `plugins/`. Configure proxy so new players connect to the lobby first. See [Lobby and Discord bot setup](lobby-and-discord-bot-setup.md). **Discord verify bot:** Run the bot in `DiscordBot/` (Node.js); see [DiscordBot/README.md](../../DiscordBot/README.md) and [Lobby and Discord bot setup](lobby-and-discord-bot-setup.md).

---

## Part 4: Configuration

After first run with plugins:

- **Server type:** In the plugin config (e.g. `configs/survival/config.yml`): `server-name: survival` or `amplified`.
- **Database:** In `configs/common/config.yml`, set `mysql.host`, `mysql.port`, `mysql.database`, `mysql.username`, `mysql.password`.
- **Eclipse / resource packs:** Configure `eclipse-port` and `resource-packs` URLs in survival config if used.
- **Discord verification:** Configure the lobby plugin and run the Discord bot if you want the lobby `/verify` flow.

Restart the server or use `/lwplugins reload <plugin>` after editing config.

---

## Part 5: Running

```powershell
cd C:\MC-Server
java -Xms1G -Xmx2G -jar paper.jar
```

- **BungeeCord:** If using a proxy, set `bungeecord: true` in each backend’s `config/spigot.yml`.
- **Portals:** Ensure MySQL tables exist (plugin can create them, or see full SQL in the original install guide).

---

## Quick reference

| Step | Action |
|------|--------|
| 1 | Install JDK 21, set `JAVA_HOME`. |
| 2 | If using Portals/Clans: install MySQL, create DB/user, set `mysql` in `configs/common/config.yml`. |
| 3 | Build: `.\gradlew.bat build`. |
| 4 | Create server folder; download Paper 1.21.11. |
| 5 | Run server once, set `eula=true`, run again, then `stop`. |
| 6 | Copy Common and Survival (and optional) JARs into server `plugins/`. For lobby: copy only Lobby JAR; see [Lobby and Discord bot setup](lobby-and-discord-bot-setup.md). |
| 7 | Start: `java -Xms1G -Xmx2G -jar paper.jar`. If using lobby: configure proxy and start Discord bot (see lobby-and-discord-bot-setup). |
| 8 | Edit config for `server-name`, MySQL, bot URL. |
| 9 | Use the [Test guide](test-guide.md) to verify commands and features. |

For full step-by-step (Windows/macOS/Linux MySQL install, BungeeCord, portal tables SQL), see the repository history or ask for the extended install doc.
