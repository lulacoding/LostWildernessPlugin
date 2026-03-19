# Plugin: Player

Player profile and session. The “spine” everything else hangs off.

## Purpose

- Load player profiles **asynchronously** on `AsyncPlayerPreLoginEvent`.
- Expose profiles via a thread-safe cache so other systems never hit DB from listeners.
- Persist profiles on quit (and optionally periodic flush or checkpoints).

## Main classes

| Class | Responsibility |
|-------|----------------|
| `PlayerProfile` | UUID, lastSeen, and (later) progression/skills/economy/quest state. |
| `PlayerProfileService` | Cache; `loadProfileAsync(UUID)`, `getProfile(UUID)`, `saveProfileAsync(UUID)`, `saveAllDirtyAsync()`. |
| `PlayerSessionManager` | Tracks online sessions; lifecycle callbacks. |
| `PlayerProfileRepository` | Async load/save of profile rows (and/or coordination of sub-repos). |
| `PlayerProfilePreloadListener` | Listens `AsyncPlayerPreLoginEvent` → calls `PlayerProfileService.loadProfileAsync`. |
| `PlayerSessionListener` | Join/quit; ensure profile in cache on join, schedule save + evict on quit. |

## Config

- `config/player.yml` – Cache TTL, save interval, timeouts (if any).

## DB

- `player_profiles` (or equivalent) – Base profile row; domain-specific data may be in separate tables and coordinated by the service/repo.

## Lifecycle

- **Pre-login:** Listener → `loadProfileAsync(uuid)` → repo loads (async) → cache populated before join.
- **Join:** Listener checks profile in cache; if missing, fallback load with short timeout (rare).
- **Quit:** Mark dirty, call `saveProfileAsync`, then evict from cache (or keep for TTL).
