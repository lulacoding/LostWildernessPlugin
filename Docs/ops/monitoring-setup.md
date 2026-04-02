---
title: Monitoring Setup
description: Monitoring stack installation and configuration.
tags:
  - operations
  - monitoring
status: reference
phase: ongoing
owner: ops
action: none
---
## Ops: Monitoring setup (Ubuntu/Debian)

This guide covers installing and wiring the full monitoring stack for Lost Wilderness:
**Loki + Promtail** (logs) · **Prometheus + Alertmanager** (metrics + alerts) · **Grafana** (dashboards) · **Discord webhook** (notifications)

For the design and alert rules rationale see [`Docs/future/monitoring-logging-and-alerting.md`](../future/monitoring-logging-and-alerting.md).


### Step 1 — Create the secrets env file

```bash
sudo cp /path/to/repo/Docs/ops/lw-ops.env.example /etc/lw-ops.env
sudo nano /etc/lw-ops.env   # fill in DISCORD_INFRA_WEBHOOK and MYSQL_EXPORTER_DSN
sudo chmod 600 /etc/lw-ops.env
sudo chown root:root /etc/lw-ops.env
```

Create the Discord webhook:
- Discord server → **Settings → Integrations → Webhooks → New Webhook**
- Choose the **#infra-alerts** channel (create it if needed)
- Copy the URL into `/etc/lw-ops.env` as `DISCORD_INFRA_WEBHOOK`


### Step 3 — Install Prometheus + Alertmanager (metrics + alerts)

```bash
sudo apt install -y prometheus prometheus-alertmanager

# Deploy configs
sudo cp /path/to/repo/Docs/ops/prometheus.yml /etc/prometheus/prometheus.yml
sudo mkdir -p /etc/prometheus/rules
sudo cp /path/to/repo/Docs/ops/lw-alerts.yml /etc/prometheus/rules/lw-alerts.yml

# Deploy Alertmanager config
sudo cp /path/to/repo/Docs/ops/alertmanager.yml /etc/alertmanager/alertmanager.yml
# Edit /etc/alertmanager/alertmanager.yml — replace CHANGE_ME__DISCORD_WEBHOOK_URL

sudo systemctl enable prometheus prometheus-alertmanager
sudo systemctl restart prometheus prometheus-alertmanager

# Verify alert rules loaded correctly
curl -s http://localhost:9090/api/v1/rules | python3 -m json.tool | grep '"name"'
```


### Step 5 — Install node exporter (host CPU/RAM/disk)

```bash
sudo apt install -y prometheus-node-exporter
sudo systemctl enable prometheus-node-exporter
sudo systemctl start prometheus-node-exporter

# Verify
curl -s http://127.0.0.1:9100/metrics | grep node_uname
```


### Step 7 — Enable Spark on Paper backends

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


### Step 9 — Test the full pipeline

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

---

### Grafana dashboard checklist

- [ ] Add Loki + Prometheus data sources
- [ ] Create a row per server (lobby-1, survival-1, amplified-1) with TPS, player count, heap
- [ ] Add a MariaDB row (connections, query rate)
- [ ] Add a host row (CPU, RAM, disk)
- [ ] Set up a Grafana alert → Discord for any panel breaching threshold (complements Alertmanager)
