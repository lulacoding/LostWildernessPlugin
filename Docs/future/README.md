# Operations And Scaling

Part of the [Lost Wilderness documentation hub](../README.md). This section covers infrastructure, security, performance, backups, monitoring, routing, and other operational planning for running Lost Wilderness like a serious multi-server network.

---

## What This Section Is For

Use this folder when you are planning or operating the network itself rather than changing gameplay code.

- Proxy and backend security
- Database sizing, schema, and backup policy
- Paper and JVM tuning
- Monitoring and alerting
- Multi-instance routing
- Load testing and runbooks

This section is intentionally more operational and more forward-looking than the main [roadmap](../roadmap.md).

---

## Core Operations Guides

| Guide | Purpose |
| --- | --- |
| [Network and security](network-and-security.md) | Proxy trust, firewalling, backend exposure, and secure topology |
| [Database architecture and tuning](database-architecture-and-tuning.md) | MySQL design, pool sizing, schema, and incident guidance |
| [Backups and disaster recovery](backups-and-disaster-recovery.md) | Backup policy, restore process, and recovery drills |
| [Paper performance and capacity](paper-performance-and-capacity.md) | JVM flags, Paper tuning, and safe player-count planning |
| [Monitoring, logging, and alerting](monitoring-logging-and-alerting.md) | Logs, dashboards, metrics, and operational alerts |

---

## Scaling Guides

| Guide | Purpose |
| --- | --- |
| [Multi-instance servers and routing](multi-instance-servers-and-routing.md) | Naming and routing patterns like `survival-1`, `survival-2`, `lobby-1` |
| [Load testing and capacity planning](load-testing-and-capacity-planning.md) | Benchmarking the network and finding the real bottlenecks |
| [Auth, anticheat, and trust boundaries](auth-anticheat-and-trust-boundaries.md) | Identity, trust, cross-play, and anti-abuse boundaries |
| [Ops playbooks and runbooks](ops-playbooks-and-runbooks.md) | Incident response and repeatable admin procedures |

---

## How To Use This Folder

1. Use [implementation-status.md](../implementation-status.md) to see the current live state.
2. Use these pages to plan or harden the next layer of infrastructure.
3. Move completed work back into status docs, changelog entries, and server config notes.

