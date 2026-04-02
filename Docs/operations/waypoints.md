---
title: Waypoints
description: Server waypoint and teleport configuration.
tags:
  - operations
  - waypoints
status: reference
phase: ongoing
owner: ops
action: none
---
# Waypoints plugin — implementation guide (Lost Wilderness)

This guide covers **[Waypoints](https://modrinth.com/plugin/waypoints)** by Md5Lukas (navigation HUD, many destinations, permission-gated POIs). It matches a **Paper** survival/RPG server with **LuckPerms**, **BetonQuest**, and optional **BlueMap** (you already use BlueMap on survival).

**Upstream docs:** [GitHub README (v5)](https://github.com/Sytm/waypoints/blob/v5/main/README.md) · [Config reference (v4 branch)](https://github.com/Sytm/waypoints/blob/v4/master/waypoints/src/main/resources/config.yml) · [English lang](https://github.com/Sytm/waypoints/blob/v4/master/waypoints/src/main/resources/lang/en.yml)

**Note:** The GitHub repo was archived / moved; **Modrinth** is the distribution source of truth. Always pick a build that matches your **exact Paper** version.


## 2. Requirements

- **Java 21** (Waypoints v5 README states minimum Java 21).
- **Paper** (recommended) matching the Waypoints jar’s supported MC version — **verify on Modrinth** before production.
- **SQLite** is used by the plugin (bundled); no MySQL setup for Waypoints itself.

**Optional plugins**

| Plugin | Purpose |
|--------|--------|
| **Vault** | Economy-backed **teleport costs** (optional). |
| **ProtocolLib** | Only if you enable **hologram**-style pointers that need it. |
| **BlueMap** (or others) | Map markers for **public** waypoints — Waypoints **softdepends** BlueMap. |


## 4. Configuration (first pass)

Edit `plugins/Waypoints/config.yml` after a first boot. **Survival backend:** `Server/backends/survival-1/plugins/Waypoints/config.yml` is already tuned (action bar + boss bar, trail/hologram off, BlueMap-only integrations, cross-world list hidden, limits 50/10). Adjust if you add Dynmap/SquareMap or want trail particles.

Priorities for Lost Wilderness:

### 4.1 Philosophy: walk vs teleport

- If you want **navigation only** (no pay-to-win TP):
  - Set **teleport** options so players **cannot** teleport, or set **extreme costs** via Vault later.
  - Remove or restrict `waypoints.teleport.*` perms (see §6).
- If you allow **limited TP** (e.g. only to **private** waypoints they placed at the spot):
  - Keep private TP enabled; keep **public/permission** TP off or expensive.

Exact keys depend on the jar version — compare your generated `config.yml` with the [upstream config](https://github.com/Sytm/waypoints/blob/v4/master/waypoints/src/main/resources/config.yml).

### 4.2 Direction indicators

Enable at least:

- **Boss bar** (top-of-screen compass feel) and/or  
- **Action bar** (always-on direction segments).

Tune update intervals and visibility so they are not spammy. Disable **player tracking** unless you explicitly want players to track each other (off by default in many builds).

### 4.3 World limits

If some worlds must **not** allow waypoint placement, use the world denylist / allowlist in config (names match Bukkit world names, e.g. `world`, `world_nether`).

### 4.4 Limits

Set **max waypoints / folders per player** for SMP fairness (unless you grant `waypoints.unlimited` to VIPs).

### 4.5 Language

Edit `plugins/Waypoints/lang/en.yml` (or your locale) for server-specific wording.


## 6. Permissions (LuckPerms) — recommended layout

**Everyone (default group):**

- `waypoints.command.use`
- `waypoints.modify.private`
- `waypoints.temporaryWaypoint` (if you want temp markers — optional)

**Moderators / builders**

- `waypoints.modify.public`
- `waypoints.modify.permission`
- `waypoints.command.reload` (or only console)

**Admins**

- `waypoints.*` or explicit admin nodes

**Restrict teleport (if used)**

- Only grant `waypoints.teleport.private` / `.public` / `.permission` to ranks that should TP.

**Scripting (important)**

- `waypoints.command.scripting` — **console only** unless you trust staff; used for `/waypointsscript` and UUID lookup.

Example LP group snippet (conceptual):

```text
lp group default permission set waypoints.command.use true
lp group default permission set waypoints.modify.private true
lp group moderator permission set waypoints.modify.public true
lp group moderator permission set waypoints.modify.permission true
```


## 8. Interaction with RPG_Core_V2 (Thornwell compass)

You already have **custom** Thornwell logic in **RPG_Core_V2** (`story-compass.yml`, `/v2thornwellcompass`, arrival radius).

**Choose one pattern to avoid duplicate UX:**

| Option | Behavior |
|--------|----------|
| **A — Waypoints only** | Define **Thornwell** as a public or permission waypoint; **disable** or stop using the RPG_Core compass command + listener for Thornwell. |
| **B — Hybrid** | Keep RPG_Core for **one** intro beat (Amos) and use **Waypoints** for **all other** cities — document which is canonical. |
| **C — RPG_Core only for chapter 1** | Until `thornwell_reached`, hide Waypoint permissions; after arrival, grant `waypoints.command.use` + city perms. |

Align **coordinates** between `story-compass.yml` and the Waypoints POI so players never get two conflicting targets.


## 10. BlueMap

Waypoints **softdepends** BlueMap. With **public** waypoints enabled, markers should appear on the BlueMap web UI when both plugins load.

Verify:

1. BlueMap renders the overworld.
2. Create a test **public** waypoint.
3. Refresh the map — marker should show (timing may depend on plugin update cycle).

**Note:** Permission-only POIs (§9) usually **do not** appear on the web map until you also use a **public** marker for the same town (optional duplicate for map only).


## 12. Troubleshooting

| Issue | What to check |
|-------|----------------|
| Plugin disables on boot | Java version (21+), Paper version vs jar, full `latest.log` stack trace. |
| Commands unknown | Typo; wrong jar; plugin not enabled. |
| No map markers | Public waypoint? BlueMap loaded? Integration section in Waypoints config. |
| Players can TP everywhere | Remove `waypoints.teleport.*` from default group; set costs in config. |
| Duplicate navigation with RPG_Core | §8 — pick one system per destination. |


*Last updated: 2026-04-02 — §9 Amos/Thornwell wiring added; verify Modrinth/Paper compatibility before production deploy.*
