# Plugin console commands and diagnostics

Ideas for admin and diagnostics commands for the LostWilderness plugin suite.

---

## Plugin management and diagnostics

| Command | Purpose |
| ------- | ------- |
| `/lwplugins` or `/lwplugins status` | List loaded plugins, versions, enabled status; summary and load errors. |
| `/lwplugins info <plugin>` | Detailed info: version, authors, main class, dependencies, commands, config status. |
| `/lwplugins health` | Check for plugin errors/warnings in log; report slow startup or conflicts. |
| `/lwplugins reload <plugin>` | Reload a plugin (or all) safely where supported. |
| `/lwplugins metrics` | Runtime stats: events handled, commands executed, listeners (optional memory). |
| `/lwplugins timings` | Integrate with Paper timings for plugin CPU use. |
| `/lwplugins configcheck` | Validate config files and report missing/invalid. |
| `/lwplugins updatecheck` | Check for updates if update URLs/APIs exist. |

---

## Example output

**`/lwplugins status`:**
```
[LostWilderness] Plugin Status:
- LostWilderness-Common v2.0.0 [ENABLED]
- LostWilderness-Survival v2.0.0 [ENABLED]
Total: 2 plugins (2 enabled)
No load errors detected.
```

**`/lwplugins health`:**
```
[LostWilderness] Plugin Health Check:
- All plugins loaded successfully.
- No errors or severe warnings found in latest log.
```

Expand this list as the plugin suite grows.
