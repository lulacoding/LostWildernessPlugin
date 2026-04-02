---
title: Boss Arena Test Plan
description: Test plan for boss encounters and arena mechanics.
tags:
  - testing
  - boss
status: partial
phase: phase-1
owner: dev
action: needs-test
---
# Boss And Arena Test Plan

Part of the [development documentation](README.md). This page is the focused test reference for boss progression, arena generation, and related admin commands.


## Environment Requirements

- Paper 1.21.x
- Java 21
- Shared MySQL database for Survival and Amplified
- Operator access or `lw.admin.arena`

Both servers should point at the same MySQL database so boss progression carries across the network.


## Database Checks

Use these queries while testing:

```sql
SELECT * FROM boss_kills;
SELECT * FROM clan_boss_kills;
```

Reset counts when needed:

```sql
DELETE FROM boss_kills;
DELETE FROM clan_boss_kills;
```


## Troubleshooting

| Issue | Check |
| --- | --- |
| Boss never transforms | Verify roof height, eligibility counts, and shared DB config |
| Clan progress missing | Ensure at least one clan member is close enough at kill time |
| Diablo never unlocks | Confirm Devoider kills are recorded in the shared database |
| Arena commands missing | Confirm the correct plugin modules are loaded and permissions are present |

For broader regression coverage, use the main [test guide](test-guide.md).
