## Ops: Monitoring setup (Ubuntu/Debian)

This guide covers installing and wiring the full monitoring stack for Lost Wilderness:
**Loki + Promtail** (logs) · **Prometheus + Alertmanager** (metrics + alerts) · **Grafana** (dashboards) · **Discord webhook** (notifications)

For the design and alert rules rationale see [`Docs/future/monitoring-logging-and-alerting.md`](../future/monitoring-logging-and-alerting.md).

---

### Files in Docs/ops/ you need

| File | Purpose |
|------|---------|
| `promtail-config.yml` | Promtail config — ships logs from all backends + proxy to Loki |
| `prometheus.yml` | Prometheus scrape config (Paper spark metrics, MariaDB exporter, node exporter) |
| `lw-alerts.yml` | Prometheus alert rules (server down, low TPS, DB saturation, disk/RAM) |
| `alertmanager.yml` | Alertmanager routing → Discord webhook |
| `mysql-exporter.service` | systemd unit for prometheus-mysqld-exporter |
| `lw-ops.env.example` | Secrets env file template (Discord webhook, DB DSN) |
| `discord-alert.sh` | Standalone script to send a Discord message from the CLI |
| `crash-notify.sh` | Called by backend systemd units on unexpected stop → Discord alert |
| `lw-survival-1.service.example` | Example Paper backend systemd unit with crash-notify wired in |

---

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

---

### Step 2 — Install Loki + Promtail (log aggregation)

```bash
# Add Grafana apt repo
sudo apt install -y apt-transport-https software-properties-common wget
wget -q -O - https://apt.grafana.com/gpg.key | sudo apt-key add -
echo "deb https://apt.grafana.com stable main" | sudo tee /etc/apt/sources.list.d/grafana.list
sudo apt update

# Install
sudo apt install -y loki promtail

# Deploy Promtail config
sudo cp /path/to/repo/Docs/ops/promtail-config.yml /etc/promtail/promtail-config.yml
# Edit __path__ globs if your deploy path differs from /opt/lostwilderness/

sudo systemctl enable loki promtail
sudo systemctl start  loki promtail

# Verify Promtail is shipping logs
sudo journalctl -u promtail --since "1 minute ago"
```

---

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

---

### Step 4 — Install MariaDB exporter

```bash
sudo apt install -y prometheus-mysqld-exporter

# Create exporter DB user (run in MariaDB)
sudo mariadb -u root -p <<'SQL'
CREATE USER 'exporter'@'127.0.0.1' IDENTIFIED BY 'CHANGE_ME_EXPORTER_PASSWORD';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL

# Add to /etc/lw-ops.env:
# MYSQL_EXPORTER_DSN=exporter:CHANGE_ME_EXPORTER_PASSWORD@tcp(127.0.0.1:3306)/

sudo cp /path/to/repo/Docs/ops/mysql-exporter.service /etc/systemd/system/mysql-exporter.service
sudo systemctl daemon-reload
sudo systemctl enable mysql-exporter
sudo systemctl start mysql-exporter

# Verify metrics
curl -s http://127.0.0.1:9104/metrics | grep mysql_up
```

---

### Step 5 — Install node exporter (host CPU/RAM/disk)

```bash
sudo apt install -y prometheus-node-exporter
sudo systemctl enable prometheus-node-exporter
sudo systemctl start prometheus-node-exporter

# Verify
curl -s http://127.0.0.1:9100/metrics | grep node_uname
```

---

### Step 6 — Install Grafana (dashboards)

```bash
sudo apt install -y grafana

sudo systemctl enable grafana-server
sudo systemctl start grafana-server
```

Open `http://<server-ip>:3000` (default: admin / admin — change immediately).

Add data sources:
- **Loki**: URL = `http://localhost:3100`
- **Prometheus**: URL = `http://localhost:9090`

Import a Paper/Minecraft dashboard from [grafana.com/grafana/dashboards](https://grafana.com/grafana/dashboards) (search "minecraft paper") or build panels using the metrics in `prometheus.yml`.

Key panels to build:
- TPS per server (`spark_tps`)
- Player count per server
- Heap usage per server (`spark_heap_used_bytes`)
- MariaDB connections (`mysql_global_status_threads_connected`)
- Disk usage (`node_filesystem_avail_bytes`)

---

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

---

### Step 8 — Wire crash notifications into backend systemd units

```bash
# Deploy crash scripts
sudo cp /path/to/repo/Docs/ops/crash-notify.sh  /opt/lostwilderness/Docs/ops/
sudo cp /path/to/repo/Docs/ops/discord-alert.sh /opt/lostwilderness/Docs/ops/
sudo chmod +x /opt/lostwilderness/Docs/ops/crash-notify.sh
sudo chmod +x /opt/lostwilderness/Docs/ops/discord-alert.sh

# For each backend, deploy its systemd unit (adapt the example)
sudo cp /path/to/repo/Docs/ops/lw-survival-1.service.example \
    /etc/systemd/system/lw-survival-1.service
# Edit WorkingDirectory, ExecStart jar name, heap size
sudo systemctl daemon-reload
sudo systemctl enable lw-survival-1
sudo systemctl start  lw-survival-1
```

The `ExecStopPost` line in the unit calls `crash-notify.sh` on any non-clean exit, which sends a Discord alert.

---

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
