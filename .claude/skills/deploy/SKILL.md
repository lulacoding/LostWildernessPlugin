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
