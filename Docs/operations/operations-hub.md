---
title: Operations Hub
description: Navigation hub for server operations documentation.
tags:
  - operations
  - hub
status: reference
phase: ongoing
owner: ops
action: none
---
# Operations

Runbooks and infrastructure (merged live ops + scaling plans).

| Page | Description |
| --- | --- |
| [Deployment](deployment.md) | Deploy PluginV2 per role |
| [Database](database.md) | JDBC, MySQL, tuning |
| [Backup](backup.md) | Scripts, DR |
| [Monitoring](monitoring.md) | Logs, metrics, alerts |
| [Firewall (Linux)](firewall-linux.md) | UFW |
| [Firewall (Windows)](firewall-windows.md) | Windows rules |
| [Proxy and routing](proxy.md) | Multi-instance, routing |
| [Network security](network-security.md) | Topology and trust |
| [Performance](performance.md) | Paper/JVM capacity |
| [Runbooks](runbooks.md) | Incident response |
| [NPC setup](npc-setup.md) | Citizens / quest NPCs |
| [Waypoints](waypoints.md) | Waypoints + quests integration |

### Source files in `Docs/ops/` (scripts and originals)

Merged guides above include this content; the committed **scripts and raw setup markdown** still live next to them for copy/paste paths:

| File | Purpose |
| --- | --- |
| [ops/monitoring-setup.md](../ops/monitoring-setup.md) | Monitoring stack install (source) |
| [ops/backup-setup.md](../ops/backup-setup.md) | Backup timer and scripts (source) |
| [ops/firewall-linux-ufw.md](../ops/firewall-linux-ufw.md) | UFW reference |
| [ops/firewall-windows.md](../ops/firewall-windows.md) | Windows firewall reference |

Also see: merged content from former `Docs/future/` is in [backup](backup.md), [monitoring](monitoring.md), [database](database.md), and related pages.
