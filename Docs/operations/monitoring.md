---
title: Monitoring
description: Planned server monitoring and alerting setup.
tags:
  - operations
  - monitoring
status: planned
phase: phase-2
owner: ops
action: needs-ops
---
## Ops: Monitoring setup (Ubuntu/Debian)

This guide covers installing and wiring the full monitoring stack for Lost Wilderness:
**Loki + Promtail** (logs) Â· **Prometheus + Alertmanager** (metrics + alerts) Â· **Grafana** (dashboards) Â· **Discord webhook** (notifications)

For the design and alert rules rationale see the **Future:** sections later in this file (merged from former `Docs/future/monitoring-logging-and-alerting.md`).


### Step 1 â€” Create the secrets env file

```bash
sudo cp /path/to/repo/Docs/ops/lw-ops.env.example /etc/lw-ops.env
sudo nano /etc/lw-ops.env   # fill in DISCORD_INFRA_WEBHOOK and MYSQL_EXPORTER_DSN
sudo chmod 600 /etc/lw-ops.env
sudo chown root:root /etc/lw-ops.env
```

Create the Discord webhook:
- Discord server â†’ **Settings â†’ Integrations â†’ Webhooks â†’ New Webhook**
- Choose the **#infra-alerts** channel (create it if needed)
- Copy the URL into `/etc/lw-ops.env` as `DISCORD_INFRA_WEBHOOK`


### Step 3 â€” Install Prometheus + Alertmanager (metrics + alerts)

```bash
sudo apt install -y prometheus prometheus-alertmanager

# Deploy configs
sudo cp /path/to/repo/Docs/ops/prometheus.yml /etc/prometheus/prometheus.yml
sudo mkdir -p /etc/prometheus/rules
sudo cp /path/to/repo/Docs/ops/lw-alerts.yml /etc/prometheus/rules/lw-alerts.yml

# Deploy Alertmanager config
sudo cp /path/to/repo/Docs/ops/alertmanager.yml /etc/alertmanager/alertmanager.yml
# Edit /etc/alertmanager/alertmanager.yml â€” replace CHANGE_ME__DISCORD_WEBHOOK_URL

sudo systemctl enable prometheus prometheus-alertmanager
sudo systemctl restart prometheus prometheus-alertmanager

# Verify alert rules loaded correctly
curl -s http://localhost:9090/api/v1/rules | python3 -m json.tool | grep '"name"'
```


### Step 5 â€” Install node exporter (host CPU/RAM/disk)

```bash
sudo apt install -y prometheus-node-exporter
sudo systemctl enable prometheus-node-exporter
sudo systemctl start prometheus-node-exporter

# Verify
curl -s http://127.0.0.1:9100/metrics | grep node_uname
```


### Step 7 â€” Enable Spark on Paper backends

Install the [spark plugin](https://spark.lucko.me/) on each Paper backend:

1. Download `spark-*.jar` and place in each backend's `plugins/` folder.
2. In `plugins/spark/config.json`, enable Prometheus:

```json
{
  "metrics": {
    "prometheus": {
      "enabled": true,
      "port": 9101
    }
  }
}
```

Use ports **9101** (lobby-1), **9102** (survival-1), **9103** (amplified-1) to match `prometheus.yml`.


### Step 9 â€” Test the full pipeline

```bash
# 1. Test Discord alert
DISCORD_INFRA_WEBHOOK="$(grep DISCORD_INFRA_WEBHOOK /etc/lw-ops.env | cut -d= -f2)" \
bash /opt/lostwilderness/Docs/ops/discord-alert.sh "Test alert: monitoring stack is live."

# 2. Check Prometheus targets are UP
curl -s http://localhost:9090/api/v1/targets | python3 -m json.tool | grep '"health"'

# 3. Check Loki is receiving logs
curl -s "http://localhost:3100/loki/api/v1/query?query={job=\"lw-survival-1\"}&limit=5"

# 4. Confirm Alertmanager config loaded
curl -s http://localhost:9093/api/v1/status | python3 -m json.tool
```


## Future: full observability stack

## Future: Monitoring, Logging & Alerting

To run a Hypixel-style network, you must be able to **see problems before players do**. This page defines the desired monitoring stack and what you should alert on.


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

### 7. Implementation â€” ops artifacts and setup

**Full setup guide:** this document (top section).

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
| `crash-notify.sh` | Called via systemd `ExecStopPost` on backend crash â€” sends Discord alert |
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

**Secrets:** Discord webhook URL and DB credentials go in `/etc/lw-ops.env` (chmod 600, not committed). The `alertmanager.yml` contains a `CHANGE_ME` placeholder â€” replace before deploying.
