#!/usr/bin/env bash
# mariadb-backup.sh
# Nightly logical backup of the Lost Wilderness `minecraft` database.
#
# Usage:   sudo bash mariadb-backup.sh
# Cron / systemd timer: see lw-backup.timer / lw-backup.service in Docs/ops/
#
# Prerequisites:
#   - /etc/mysql/lw-backup.cnf exists with credentials (chmod 600, owned by root).
#     See lw-backup.cnf.example in Docs/ops/.
#   - Backup directory is writable by the user running this script.
#
# Output:  /backups/mysql/minecraft-YYYY-MM-DD.sql.gz

set -euo pipefail

BACKUP_DIR="/backups/mysql"
DB_NAME="minecraft"
CREDENTIALS_FILE="/etc/mysql/lw-backup.cnf"
DATE="$(date +%F)"
OUTPUT_FILE="${BACKUP_DIR}/minecraft-${DATE}.sql.gz"
LOG_FILE="/var/log/lw-backup/mariadb-backup.log"

# ---------------------------------------------------------------------------
# Setup
# ---------------------------------------------------------------------------
mkdir -p "${BACKUP_DIR}"
mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    echo "[$(date '+%F %T')] $*" | tee -a "${LOG_FILE}"
}

log "=== MariaDB backup start: ${DB_NAME} ==="

# ---------------------------------------------------------------------------
# Guard: credentials file must exist and be restricted
# ---------------------------------------------------------------------------
if [[ ! -f "${CREDENTIALS_FILE}" ]]; then
    log "ERROR: Credentials file not found: ${CREDENTIALS_FILE}"
    log "       Create it from Docs/ops/lw-backup.cnf.example and chmod 600."
    exit 1
fi

# ---------------------------------------------------------------------------
# Dump
# ---------------------------------------------------------------------------
log "Dumping ${DB_NAME} -> ${OUTPUT_FILE}"
mariadb-dump \
    --defaults-extra-file="${CREDENTIALS_FILE}" \
    --single-transaction \
    --skip-lock-tables \
    --databases "${DB_NAME}" \
    | gzip -9 > "${OUTPUT_FILE}"

# ---------------------------------------------------------------------------
# Verify: check the file is non-empty
# ---------------------------------------------------------------------------
SIZE="$(du -sh "${OUTPUT_FILE}" | cut -f1)"
log "Backup complete. File: ${OUTPUT_FILE} (${SIZE})"

log "=== MariaDB backup done ==="
