import os, sys, time

sentinel = os.path.join(os.environ.get("TEMP", "/tmp"), "lw_serena_session")

try:
    age = time.time() - os.path.getmtime(sentinel)
    if age < 3600:  # already activated within the last hour
        sys.exit(0)
except FileNotFoundError:
    pass

open(sentinel, "w").close()
print("SERENA: Project should be auto-activated via --project-from-cwd. If Serena tools aren't working, call mcp__plugin_serena_serena__activate_project with project='Lost Wilderness'.")
