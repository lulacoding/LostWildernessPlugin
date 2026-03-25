#!/usr/bin/env python3
"""
PostToolUse hook: after Claude edits a .java file, run compileJava immediately.
Errors appear inline in the conversation rather than at deploy time.
"""
import sys
import json
import subprocess
import os

data = json.load(sys.stdin)
# PostToolUse wraps input under 'tool_input'
tool_input = data.get("tool_input", data)
fp = tool_input.get("file_path", "")

if not fp.endswith(".java"):
    sys.exit(0)

project_root = r"C:\Users\cthvh\OneDrive\Desktop\Lost Wilderness\PluginV2"
result = subprocess.run(
    "gradlew.bat :compileJava -x test 2>&1",
    shell=True,
    cwd=project_root,
    capture_output=True,
    text=True,
)
output = (result.stdout + result.stderr).strip()
lines = output.splitlines()
# Show last 12 lines (enough to see errors without flooding)
print("\n".join(lines[-12:]))
# Exit non-zero if compile failed so Claude sees the failure
if result.returncode != 0:
    sys.exit(1)
