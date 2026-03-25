#!/usr/bin/env python3
"""
PreToolUse hook: block Claude from editing live world/database files.
Exiting with code 2 cancels the tool call and shows the message to Claude.
"""
import sys
import json

data = json.load(sys.stdin)
tool_input = data.get("tool_input", data)
fp = tool_input.get("file_path", "").replace("\\", "/")

# Only block files inside the live server backends (not source files)
if "Server/backends" not in fp:
    sys.exit(0)

BLOCKED_EXTS = (".dat", ".dat_old", ".mca", ".db", ".uid")
if fp.endswith(BLOCKED_EXTS):
    print(
        f"BLOCKED: '{fp}' is a live world/database file. "
        "Editing it could corrupt server data. Do this in-game or via server tools.",
        file=sys.stderr,
    )
    sys.exit(2)
