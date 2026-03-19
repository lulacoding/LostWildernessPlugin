## Future: Monitoring, Logging & Alerting

To run a Hypixel-style network, you must be able to **see problems before players do**. This page defines the desired monitoring stack and what you should alert on.

---

### 1. Goals

- Know, in real time:
  - Player counts per server.
  - TPS, tick time, and lag spikes.
  - DB connection pool health and query latency.
  - Crashes and restarts across all servers.
- Have **single-click access** to logs when debugging.
- Receive **Discord alerts** for serious incidents (not for every warning).

---

### 2. Components

Recommended minimal stack:

- **Log aggregation**:
  - Centralised logs via Loki + Promtail, Elastic Stack, or a hosted log service.
  - Each JVM ships its `latest.log` (and plugin logs) to a central place.
- **Metrics and dashboards**:
  - Prometheus (or similar) scraping exporters:
    - Paper metrics (via plugins or JMX).
    - MySQL metrics.
  - Grafana (or equivalent) dashboards showing per-server health.
- **Alerting**:
  - Grafana or alertmanager sending to a **Discord webhook** for:
    - Server down.
    - TPS below threshold for sustained period.
    - DB connection saturation / high error rate.

You don't need this all at once, but this is the end-state design.

---

### 3. What to log from plugins

For your own plugins (Calendar, Portals, Clans, Lobby, etc.):

- **Log levels**:
  - `INFO` for normal startup/shutdown and major events (e.g. plugin enabled, DB connected, "Calendar leader mode on SURVIVAL").
  - `WARN` for recoverable problems players might notice (e.g. failed portal build due to safety checks).
  - `ERROR` for unexpected exceptions, DB failures, or data corruption symptoms.
- Include:
  - Player name/UUID when player-specific.
  - Server type (`SURVIVAL`, `AMPLIFIED`, `LOBBY`) when relevant.

Aim for logs that can be filtered by:

- Plugin name.
- Severity.
- Player or server.

---

### 4. Metrics to track

Per **Paper server**:

- TPS and **max tick time**.
- Number of players.
- Chunk count loaded.
- Entity counts (total + by type if available).
- Memory usage (heap).

Per **MySQL instance**:

- Queries per second.
- Average and p95 query latency.
- Active and idle connections per pool.
- Replication lag if you add replicas.

Per **Discord bot**:

- Slash command failures or timeout rate for `/verify` and `/players`.
- DB connection failures.

Define which metrics are **dashboard only** vs. **alert-worthy**.

---

### 5. Alerts and thresholds (first pass)

Examples of sensible initial alerts:

- **Server down**:
  - No heartbeat / metrics for > 60 seconds.
- **Low TPS**:
  - TPS < 18 for > 60 seconds on Survival/Amplified.
- **Database issues**:
  - DB connection pool uses > 80% of max connections for > 5 minutes.
  - New `ER_ACCESS_DENIED_ERROR` or "Communications link failure" messages appear.
- **Discord bot**:
  - Repeated failures to connect to DB.
  - HTTP 5xx rate above a small threshold.

All of these should go to a **single "infra-alerts" Discord channel**, not general chat.

---

### 6. Crash handling and incident logging

For crashes:

- Paper's watchdog generates crash reports; ensure:
  - Reports are uploaded/archived automatically.
  - Crash logs are easy to fetch from your log system.
- On crash/restart:
  - Send a **Discord webhook message** summarising:
    - Which server crashed/restarted.
    - Uptime.
    - Basic player count at time of crash (if known).

Later, you can add:

- Automatic linking from crash notifications to relevant Grafana panels (time range around crash).

Use this page to keep your **actual alert rules and links** documented as you implement them.

---

### 7. Implementation — ops artifacts and setup

**Full setup guide:** [`Docs/ops/monitoring-setup.md`](../ops/monitoring-setup.md)

**Log locations (production Linux):**

- Proxy: `/opt/lostwilderness/Server/proxy/logs/`
- lobby-1: `/opt/lostwilderness/Server/backends/lobby-1/logs/latest.log`
- survival-1: `/opt/lostwilderness/Server/backends/survival-1/logs/latest.log`
- amplified-1: `/opt/lostwilderness/Server/backends/amplified-1/logs/latest.log`
- Backup logs: `/var/log/lw-backup/`

**Committed ops artifacts (`Docs/ops/`):**

| File | Purpose |
|------|---------|
| `promtail-config.yml` | Promtail to Loki log shipping for all backends, proxy, and backup logs |
| `prometheus.yml` | Prometheus scrape config (spark Paper metrics, MariaDB exporter, node exporter) |
| `lw-alerts.yml` | Alert rules: server down, low TPS, critical TPS, high heap, MariaDB down, DB saturation, disk/RAM |
| `alertmanager.yml` | Alertmanager routing to Discord webhook (set `DISCORD_INFRA_WEBHOOK` in `/etc/lw-ops.env`) |
| `mysql-exporter.service` | systemd unit for prometheus-mysqld-exporter on port 9104 |
| `lw-ops.env.example` | Secrets env file template (Discord webhook URL, MySQL exporter DSN) |
| `discord-alert.sh` | CLI script to send any message to Discord webhook |
| `crash-notify.sh` | Called via systemd `ExecStopPost` on backend crash — sends Discord alert |
| `lw-survival-1.service.example` | Example Paper backend systemd unit with crash-notify and EnvironmentFile wired in |

**Stack:**
- **Logs:** Loki + Promtail
- **Metrics + alerts:** Prometheus + Alertmanager to Discord webhook
- **Dashboards:** Grafana (Loki + Prometheus data sources)
- **Paper metrics:** spark plugin (port 9101/9102/9103 per backend)

**Alert rules summary (full rules in `Docs/ops/lw-alerts.yml`):**

| Alert | Condition | Severity |
|-------|-----------|----------|
| `LWServerDown` | Backend metrics missing > 60s | Critical |
| `LWProxyDown` | Proxy metrics missing > 60s | Critical |
| `LWLowTPS` | TPS < 18 for > 60s (survival/amplified) | Warning |
| `LWCriticalTPS` | TPS < 12 for > 30s | Critical |
| `LWHighHeapUsage` | Heap > 85% for > 2 min | Warning |
| `LWMariaDBDown` | MariaDB exporter unreachable > 60s | Critical |
| `LWDBConnectionSaturation` | Connections > 80% of max for > 5 min | Warning |
| `LWDBHighQueryLatency` | Avg query time > 1s for > 3 min | Warning |
| `LWDiskSpaceLow` | Root disk > 85% | Warning |
| `LWDiskSpaceCritical` | Root disk > 95% | Critical |
| `LWHighMemoryUsage` | RAM > 90% for > 5 min | Warning |

**Secrets:** Discord webhook URL and DB credentials go in `/etc/lw-ops.env` (chmod 600, not committed). The `alertmanager.yml` contains a `CHANGE_ME` placeholder — replace before deploying.
