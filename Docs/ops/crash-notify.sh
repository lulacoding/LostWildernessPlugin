#!/usr/bin/env bash
# crash-notify.sh
# Detects a new crash report in a Paper backend and sends a Discord alert.
#
# Run this as part of each backend's systemd unit (ExecStopPost) so it fires
# whenever the server process exits unexpectedly.
#
# Usage (from systemd unit ExecStopPost):
#   ExecStopPost=/bin/bash /opt/lostwilderness/Docs/ops/crash-notify.sh survival-1
#
# Environment:
#   DISCORD_INFRA_WEBHOOK   — Discord webhook URL (set in /etc/lw-ops.env or systemd EnvironmentFile)
#   MC_BASE                 — base path for backends (default: /opt/lostwilderness/Server/backends)
#
# How it works:
#   1. Checks if the process exited with a non-zero code (crash vs intentional stop).
#   2. Finds the newest crash report in crash-reports/ for the given backend.
#   3. Sends a Discord message with server name, exit code, and crash report name.

set -euo pipefail

SERVER_NAME="${1:-unknown}"
MC_BASE="${MC_BASE:-/opt/lostwilderness/Server/backends}"
BACKEND_DIR="${MC_BASE}/${SERVER_NAME}"
LOG_FILE="/var/log/lw-backup/crash-notify.log"
ALERT_SCRIPT="$(dirname "$0")/discord-alert.sh"

mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    echo "[$(date '+%F %T')] $*" | tee -a "${LOG_FILE}"
}

# systemd sets $SERVICE_RESULT when used as ExecStopPost
# It is "success" for clean stops, anything else for crashes/OOMs/signals.
EXIT_RESULT="${SERVICE_RESULT:-}"
EXIT_CODE="${EXIT_CODE:-}"

log "=== crash-notify triggered: server=${SERVER_NAME} result=${EXIT_RESULT} exit_code=${EXIT_CODE} ==="

# Only alert on non-clean exits
if [[ "${EXIT_RESULT}" == "success" ]]; then
    log "Clean stop detected — no alert needed."
    exit 0
fi

# Find the newest crash report
CRASH_REPORT=""
if [[ -d "${BACKEND_DIR}/crash-reports" ]]; then
    CRASH_REPORT="$(ls -t "${BACKEND_DIR}/crash-reports/" 2>/dev/null | head -1 || true)"
fi

if [[ -n "${CRASH_REPORT}" ]]; then
    CRASH_LINE="Latest crash report: \`${CRASH_REPORT}\`"
else
    CRASH_LINE="No crash report found (may be a watchdog kill or OOM)."
fi

MESSAGE=":red_circle: **Lost Wilderness — Server Crash**
**Server:** \`${SERVER_NAME}\`
**Exit result:** \`${EXIT_RESULT}\`
${CRASH_LINE}
Check logs: \`journalctl -u lw-${SERVER_NAME} --since '5 minutes ago'\`"

log "Sending Discord alert..."
bash "${ALERT_SCRIPT}" "${MESSAGE}" && log "Alert sent." || log "ERROR: Failed to send Discord alert."
