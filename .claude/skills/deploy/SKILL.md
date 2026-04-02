---
name: deploy
description: Build all 4 plugin JARs (RPG_Core_V2 + survival/amplified/lobby wrappers) and copy them to all 3 server backends. Use this after completing any Java changes instead of asking the user to run the build manually.
---

Run the full build and deploy using the existing batch script:

```bash
cd "C:\Users\cthvh\OneDrive\Desktop\Lost Wilderness\PluginV2"
cmd /c build-and-copy-to-server.bat
```

Report the result clearly:
- If successful: confirm all 4 JARs were built and copied to survival-1, amplified-1, and lobby-1
- If failed: show the exact error lines and stop — do not proceed

The server must be restarted (or `/reload confirm` used) for changes to take effect.

## Gotchas
- **Build fails silently if Gradle daemon is stale** — if you see no output or a hang, the bat script may need the daemon killed: add `--no-daemon` flag
- **`-x test` is intentional** — tests are skipped in all builds; do not remove this flag
- **`shadowJar` only for RPG_Core_V2** — the survival/lobby/amplified wrappers use plain `jar`, not shadow. The bat script handles this correctly; don't override it manually
- **Windows path spaces** — the path contains spaces ("Lost Wilderness"); always quote it
- **Reload vs restart** — `/reload confirm` works for config changes; full server restart is required for new listeners or commands
