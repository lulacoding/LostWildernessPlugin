# Common (Admin)

Shared core for all Lost Wilderness plugins; the only player-facing part is the admin command below.

---

## Overview

**LostWilderness-Common** is the shared core for all Lost Wilderness plugins. As a player you don't install it yourself; it runs in the background. The only **player-facing** part is the optional admin command below.

---

## Commands (Admin)

| Command | Description |
| --- | --- |
| **/lwconfig reload** | Reloads the Common plugin's config file. Requires permission `lw.admin.config`. |

---

## What Players Might Notice

- **Resource packs:** The server may ask you to accept a default or event resource pack (e.g. for eclipses). That's controlled by the Survival/Events setup; Common helps provide the config that other plugins use.
- **No direct player commands:** Aside from **/lwconfig reload** for admins, there are no Common-only commands for regular players. Use **/lwhelp**, **/date**, **/season**, **/clan**, **/portals**, etc. from the [Survival](survival.md) or [Amplified](amplified.md) guides.

---

## Tips

- If an admin changes the Common config (e.g. `plugins/LostWilderness-Common/config.yml`), they can run **/lwconfig reload** to apply it without restarting the server.
