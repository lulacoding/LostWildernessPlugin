#!/usr/bin/env bash
# world-backup.sh
# Nightly world backup for all Lost Wilderness Paper backends.
#
# Usage:   sudo bash world-backup.sh
# Cron / systemd timer: runs after mariadb-backup.sh via lw-backup.service
#
# Strategy:
#   For each backend, stop the server (safest for consistency), archive world
#   folders, then restart.  If AUTO_RESTART=false, you manage server lifecycle
#   externally (e.g. a maintenance window).
#
# Prerequisites:
#   - Backends run via systemd units named lw-lobby-1, lw-survival-1, lw-amplified-1
#     (edit BACKENDS array to match your actual unit names).
#   - /backups/worlds/ is writable by root.

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration — edit to match your deploy paths and systemd unit names
# ---------------------------------------------------------------------------

MC_BASE="/opt/lostwilderness/Server/backends"
BACKUP_BASE="/backups/worlds"
DATE="$(date +%F)"
LOG_FILE="/var/log/lw-backup/world-backup.log"

# Each entry: "folder_name:systemd_unit_name:world_subdirs (space-separated)"
declare -a BACKENDS=(
    "lobby-1:lw-lobby-1:world"
    "survival-1:lw-survival-1:world world_nether world_the_end"
    "amplified-1:lw-amplified-1:world world_nether world_the_end"
)

# Set to false to skip automatic stop/start (manage manually or via other tooling)
AUTO_RESTART=true

# ---------------------------------------------------------------------------
# Setup
# ---------------------------------------------------------------------------
mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    echo "[$(date '+%F %T')] $*" | tee -a "${LOG_FILE}"
}

backup_backend() {
    local BACKEND_DIR="$1"
    local UNIT_NAME="$2"
    local WORLD_DIRS="$3"

    local SRC="${MC_BASE}/${BACKEND_DIR}"
    local DEST="${BACKUP_BASE}/${BACKEND_DIR}"
    mkdir -p "${DEST}"

    local ARCHIVE="${DEST}/worlds-${DATE}.tar.gz"

    log "--- Backend: ${BACKEND_DIR} ---"

    if [[ "${AUTO_RESTART}" == "true" ]]; then
        log "Stopping ${UNIT_NAME}..."
        systemctl stop "${UNIT_NAME}" || log "WARN: systemctl stop ${UNIT_NAME} returned non-zero (may already be stopped)"
        sleep 3
    else
        log "AUTO_RESTART=false; skipping stop of ${UNIT_NAME}"
    fi

    # Build the list of world directories that actually exist
    local EXISTING_DIRS=()
    for DIR in ${WORLD_DIRS}; do
        if [[ -d "${SRC}/${DIR}" ]]; then
            EXISTING_DIRS+=("${DIR}")
        else
            log "WARN: ${SRC}/${DIR} does not exist — skipping"
        fi
    done

    if [[ ${#EXISTING_DIRS[@]} -eq 0 ]]; then
        log "WARN: No world directories found for ${BACKEND_DIR} — skipping archive"
    else
        log "Archiving: ${EXISTING_DIRS[*]} -> ${ARCHIVE}"
        tar -czf "${ARCHIVE}" -C "${SRC}" "${EXISTING_DIRS[@]}"
        local SIZE
        SIZE="$(du -sh "${ARCHIVE}" | cut -f1)"
        log "Archive complete: ${ARCHIVE} (${SIZE})"
    fi

    if [[ "${AUTO_RESTART}" == "true" ]]; then
        log "Starting ${UNIT_NAME}..."
        systemctl start "${UNIT_NAME}" || log "ERROR: Failed to restart ${UNIT_NAME} — check manually!"
    fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
log "=== World backup start ==="

for ENTRY in "${BACKENDS[@]}"; do
    IFS=':' read -r FOLDER UNIT WORLDS <<< "${ENTRY}"
    backup_backend "${FOLDER}" "${UNIT}" "${WORLDS}"
done

log "=== World backup done ==="
