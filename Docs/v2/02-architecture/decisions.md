# Architecture decisions

Log of non-obvious decisions for PluginV2. Add new entries as you make them.

---

## One jar

- **Decision:** Single JAR contains all modules; behaviour controlled by config (server-role, enabled-modules).
- **Reason:** Simpler deployment and versioning; same binary everywhere; no JAR soup in plugins/.

---

## No V1 jars in dev plugins/

- **Decision:** Dev server runs only PluginV2; old plugins stay in repo as read-only reference.
- **Reason:** Server is not live; clean test of V2 without legacy interference.

---

## Async profile load on AsyncPlayerPreLoginEvent

- **Decision:** Load full profile (or minimal profile) async in pre-login; cache before PlayerJoinEvent.
- **Reason:** Protects main thread; player never waits on DB during join.

---

## Module dependency graph

- **Decision:** ModuleManager orders load/enable/disable by declared dependencies (e.g. Quests depends on Player, Progression, Economy).
- **Reason:** Ensures services exist before modules that use them; clean shutdown order.

---

---

## Full remake + three servers

- **Decision:** PluginV2 is a full remake of the current plugin. All features from the existing plugin and from Docs (implementation-status, design) must be reimplemented in V2. No feature drop unless explicitly decided.
- **Decision:** Target deployment is exactly three server roles: **Lobby**, **Survival**, **Amplified** (same as current). Same JAR; config per role.
- **Reference:** [v2-feature-parity.md](../04-features/v2-feature-parity.md) is the checklist; use it in Phase 2–4 so nothing is missed.

---

*(Add more as you go.)*
