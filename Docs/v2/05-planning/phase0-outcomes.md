# Phase 0 – Outcomes

Decisions and context from the orientation phase. Update as you refine.

---

## Build tool & Java

| Decision | Choice |
|----------|--------|
| **Build tool** | Gradle (single project under `PluginV2/`, wrapper in repo) |
| **Java version** | 21 (set in `build.gradle`) |
| **Paper API** | 1.21.1-R0.1-SNAPSHOT |

---

## Servers (fixed – three nodes)

PluginV2 targets **exactly three server roles**, matching the current setup:

| Server | Role | Purpose |
|--------|------|--------|
| **Lobby** | `lobby` | Auth, verification, cosmetics, routing to Survival/Amplified |
| **Survival** | `survival` | Main survival world; full RPG stack (calendar leader, events, clans, portals, etc.) |
| **Amplified** | `amplified` | Amplified world; calendar follower; same RPG stack as Survival |

Same JAR on all three; config sets `server-role` and `enabled-modules` per server. Instance/arena servers are optional later.

---

## Playable baseline

What “playable” means for the first testable build (Phase 1–2):

- **Phase 1:** Plugin loads; players can join/leave; profile loads on pre-login and saves on quit. No gameplay features yet.
- **Phase 2:** One visible feature (e.g. progression + `/v2progress`). Still minimal but proves the spine works.
- **Later:** Survival + calendar + portals + skills + economy + events + clans + quests as each module is added.

*(Adjust to your own definition.)*

---

## Environments

| Env | Purpose |
|-----|--------|
| **dev** | Local machine; single backend + proxy (or single server); only PluginV2 in `plugins/`. |
| **stage** | Optional test network (proxy + lobby + 1–2 backends) before prod. |
| **prod** | Live network when you go live; same jar, config per role. |

Phase 1 testing uses **dev** only.

---

## Full remake

PluginV2 is a **full remake** of the current plugin: all features from the existing plugin and from **Docs** (implementation-status, design) must be **reimplemented** in V2—same behaviour, new code. Nothing is dropped unless explicitly decided. See [v2-feature-parity.md](../04-features/v2-feature-parity.md) for the checklist.

---

## Reference

- Architecture: [V2_ARCHITECTURE_PLAN.md](../../V2_ARCHITECTURE_PLAN.md)
- **Feature parity checklist:** [v2-feature-parity.md](../04-features/v2-feature-parity.md)
- PluginV2 docs: [README](../README.md)
- Next: [Phase 1 spec](phase1-spec.md) and [roadmap](roadmap.md)
