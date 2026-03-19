# Testing strategy

How to test PluginV2 at different levels.

---

## Unit tests

- **Infra:** ConfigService parsing, SqlExecutor (with test DB or mocks), SchedulerService (mock Bukkit scheduler).
- **Services:** ProgressionService, SkillService, etc. with mocked repositories.
- **Repositories:** With embedded DB (H2) or testcontainers; async behaviour with CompletableFuture.get() or await.

---

## Integration tests

- **Module load:** ModuleManager loads and enables PlayerModule; no real server.
- **Profile load/save:** PlayerProfileRepository + real or in-memory DB; verify load/save round-trip.

---

## Manual / play tests

- **Dev server:** Only PluginV2 in `plugins/`; join, leave, run commands; check logs for profile load/save and no errors.
- **Per vertical slice:** After adding a feature (e.g. progression), run through the flow (e.g. unlock milestone, run `/v2progress`).

---

## What to add later

- TPS checks under load (optional).
- Automated “join N players, leave, check DB” if desired.
