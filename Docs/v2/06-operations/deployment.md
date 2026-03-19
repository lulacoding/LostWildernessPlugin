# Deployment

How to deploy the PluginV2 jar and config per server role.

---

## Per-server layout

- **Lobby:** `plugins/RPG_Core_V2-<version>.jar`; config: `server-role: lobby`, enabled-modules: player, progression, lobby-routing, cosmetics (or as defined).
- **Survival / Amplified:** Same jar; `server-role: survival` or `amplified`; full module set: player, progression, skills, economy, calendar, events, clans, portals, quests, party.
- **Instance / arena:** Same jar; subset of modules (e.g. player, progression, bosses only).

---

## Config and secrets

- **db.yml:** Use env vars or a secrets manager for passwords; avoid committing real credentials.
- **core.yml:** enabled-modules and server-role per environment (dev/stage/prod).

---

## Feature flags

- Use config (e.g. per-module enable in core.yml) to turn features on/off without redeploying.
- For gradual rollout: enable one module at a time and verify before enabling the next.

---

## DB migrations

- Prefer separate migration scripts or a migration tool (e.g. Flyway) for V2 tables; document in implementation-status or a dedicated migrations doc.
