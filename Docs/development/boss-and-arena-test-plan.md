# Boss And Arena Test Plan

Part of the [development documentation](README.md). This page is the focused test reference for boss progression, arena generation, and related admin commands.

---

## Scope

Use this plan when testing:

- Devoider spawning on Survival
- Diablo spawning on Amplified
- Shared kill tracking through MySQL
- Boss drops and minion behavior
- Bedrock-breaking behavior for charged skulls
- Arena generation with `/diablo-lair` and `/arena-test`

---

## Environment Requirements

- Paper 1.21.x
- Java 21
- Shared MySQL database for Survival and Amplified
- Operator access or `lw.admin.arena`

Both servers should point at the same MySQL database so boss progression carries across the network.

---

## Core Test Flow

1. On Survival, kill enough normal Withers to unlock Devoider eligibility.
2. Spawn a Wither at Nether roof height and confirm it becomes Devoider.
3. Kill enough Devoiders to unlock Diablo eligibility.
4. Switch to Amplified with the same database and confirm Nether roof spawn becomes Diablo.
5. Verify drops, minions, and one-Diablo-at-a-time behavior.
6. Verify charged blue skulls can break bedrock.
7. Run `/diablo-lair` and `/arena-test` to confirm arena generation and movement boundaries.

---

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

---

## Things To Verify

| Area | What To Confirm |
| --- | --- |
| Devoider unlock | Survival Nether roof spawn only works after enough Wither kills |
| Diablo unlock | Amplified Nether roof spawn only works after enough Devoider kills |
| Shared progression | Both servers read the same DB state |
| Drops | Devoider and Diablo reward the intended star/XP values |
| Minions | Diablo spawns the expected extra mobs at the health threshold |
| Bedrock break | Only charged blue skulls break bedrock |
| Arena commands | `/diablo-lair` and `/arena-test` register and behave correctly |

---

## Troubleshooting

| Issue | Check |
| --- | --- |
| Boss never transforms | Verify roof height, eligibility counts, and shared DB config |
| Clan progress missing | Ensure at least one clan member is close enough at kill time |
| Diablo never unlocks | Confirm Devoider kills are recorded in the shared database |
| Arena commands missing | Confirm the correct plugin modules are loaded and permissions are present |

For broader regression coverage, use the main [test guide](test-guide.md).
