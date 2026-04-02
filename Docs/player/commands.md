---
title: Commands Reference
description: Complete player-facing command reference.
tags:
  - player
  - commands
status: reference
phase: ongoing
owner: player-facing
action: none
---
# Common (Admin)

Shared core for all Lost Wilderness plugins; the only player-facing part is the admin command below.


## Commands (Admin)

| Command | Description |
| --- | --- |
| **/lwconfig reload** | Reloads the Common plugin's config file. Requires permission `lw.admin.config`. |


## Tips

- If an admin changes the Common config (e.g. `plugins/LostWilderness-Common/config.yml`), they can run **/lwconfig reload** to apply it without restarting the server.
