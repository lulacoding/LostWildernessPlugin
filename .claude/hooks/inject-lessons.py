#!/usr/bin/env python3
"""
UserPromptSubmit hook: print tasks/lessons.md so corrections survive /clear.
No stdin required. Quiet if file missing or empty.
"""
import os
import sys

# .claude/hooks/ -> repo root
_here = os.path.dirname(os.path.abspath(__file__))
_repo = os.path.dirname(os.path.dirname(_here))
lessons = os.path.join(_repo, "tasks", "lessons.md")

if not os.path.isfile(lessons):
    sys.exit(0)

try:
    with open(lessons, encoding="utf-8", errors="replace") as f:
        text = f.read().strip()
except OSError:
    sys.exit(0)

if not text:
    sys.exit(0)

# Cap size so a huge file does not flood context every message
max_chars = 12000
if len(text) > max_chars:
    text = text[:max_chars] + "\n\n[… truncated: edit tasks/lessons.md or raise inject-lessons.py max_chars …]"

print("--- tasks/lessons.md (session reminder) ---\n")
print(text)
print("\n--- end lessons ---")
