#!/usr/bin/env bash
# discord-alert.sh
# Send a plain message to a Discord webhook from the command line.
# Used by crash-notify.sh and any ad-hoc ops alerting.
#
# Usage:
#   bash discord-alert.sh "Your message here"
#   bash discord-alert.sh "Server survival-1 restarted after crash."
#
# Environment variable (set in calling script or shell):
#   DISCORD_INFRA_WEBHOOK   — full Discord webhook URL
#   (alternatively hardcode WEBHOOK_URL below for quick setup)
#
# To test:
#   DISCORD_INFRA_WEBHOOK="https://discord.com/api/webhooks/..." \
#   bash discord-alert.sh "Test alert from ops script"

set -euo pipefail

WEBHOOK_URL="${DISCORD_INFRA_WEBHOOK:-}"

if [[ -z "${WEBHOOK_URL}" ]]; then
    echo "ERROR: DISCORD_INFRA_WEBHOOK is not set." >&2
    echo "       Export it before running this script, or set it in /etc/lw-ops.env" >&2
    exit 1
fi

MESSAGE="${1:-}"
if [[ -z "${MESSAGE}" ]]; then
    echo "Usage: $0 \"message\"" >&2
    exit 1
fi

# Escape double-quotes in the message for JSON
ESCAPED_MSG="${MESSAGE//\"/\\\"}"

curl -s -X POST "${WEBHOOK_URL}" \
    -H "Content-Type: application/json" \
    -d "{\"content\": \"${ESCAPED_MSG}\"}"
