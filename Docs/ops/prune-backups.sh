#!/usr/bin/env bash
# prune-backups.sh
# Enforces backup retention policy:
#   - Keep 7 daily  backups  (all files from last 7 days)
#   - Keep 4 weekly backups  (one file per week for the last 4 weeks)
#   - Keep 3 monthly backups (one file per month for the last 3 months)
#   - Delete anything older
#
# Usage:  sudo bash prune-backups.sh
# Run after world-backup.sh via lw-backup.service

set -euo pipefail

BACKUP_DIRS=(
    "/backups/mysql"
    "/backups/worlds/lobby-1"
    "/backups/worlds/survival-1"
    "/backups/worlds/amplified-1"
)
LOG_FILE="/var/log/lw-backup/prune-backups.log"

mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    echo "[$(date '+%F %T')] $*" | tee -a "${LOG_FILE}"
}

# Keep files whose names contain dates matching the keep-set.
# Strategy: build a set of date strings to keep, then delete files whose
# embedded date is NOT in that set.
#
# File name convention assumed:  *-YYYY-MM-DD.*
# e.g. minecraft-2026-03-11.sql.gz   or   worlds-2026-03-11.tar.gz

build_keep_dates() {
    local -n _KEEP=$1
    local TODAY
    TODAY="$(date +%F)"

    # Last 7 dailies
    for i in $(seq 0 6); do
        _KEEP["$(date -d "${TODAY} - ${i} days" +%F)"]=1
    done

    # Last 4 Sundays (weekly anchor)
    for i in $(seq 0 3); do
        _KEEP["$(date -d "${TODAY} - $((i * 7)) days - $(date -d "${TODAY}" +%u) days + 7 days" +%F)"]=1 2>/dev/null || true
        # Simpler: last 4 weeks, keep the Monday of each week
        _KEEP["$(date -d "last Monday - $((i * 7)) days" +%F)"]=1 2>/dev/null || true
    done

    # Last 3 first-of-month dates
    for i in $(seq 0 2); do
        local MONTH_DATE
        MONTH_DATE="$(date -d "${TODAY} - $((i * 30)) days" +%Y-%m-01)"
        _KEEP["${MONTH_DATE}"]=1
    done
}

prune_dir() {
    local DIR="$1"
    [[ -d "${DIR}" ]] || { log "WARN: Directory not found, skipping: ${DIR}"; return; }

    declare -A KEEP_DATES
    build_keep_dates KEEP_DATES

    log "Pruning: ${DIR} (keeping ${#KEEP_DATES[@]} date anchors)"

    local DELETED=0
    while IFS= read -r -d '' FILE; do
        local BASENAME
        BASENAME="$(basename "${FILE}")"
        # Extract YYYY-MM-DD from filename
        local FILE_DATE
        FILE_DATE="$(echo "${BASENAME}" | grep -oP '\d{4}-\d{2}-\d{2}' | head -1 || true)"

        if [[ -z "${FILE_DATE}" ]]; then
            log "SKIP (no date in name): ${BASENAME}"
            continue
        fi

        if [[ -z "${KEEP_DATES[${FILE_DATE}]+_}" ]]; then
            log "DELETE: ${FILE}"
            rm -f "${FILE}"
            (( DELETED++ )) || true
        else
            log "KEEP:   ${FILE}"
        fi
    done < <(find "${DIR}" -maxdepth 1 -type f \( -name "*.gz" -o -name "*.tar.*" \) -print0 | sort -z)

    log "Pruned ${DELETED} file(s) from ${DIR}"
}

log "=== Prune backups start ==="

for DIR in "${BACKUP_DIRS[@]}"; do
    prune_dir "${DIR}"
done

log "=== Prune backups done ==="
