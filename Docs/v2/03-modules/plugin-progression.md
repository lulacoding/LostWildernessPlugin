# Plugin: Progression

Milestones / achievements. Used for gating (portals, bosses, storyline).

## Purpose

- Track per-player unlocked milestones (e.g. `story:el_diablo`, `boss:roofwither_kills_10`).
- Expose APIs for “has unlocked?” and “unlock”; all persistence async via repository.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `AchievementKey` | Namespaced ID (e.g. string or enum) for milestones. |
| `ProgressionService` | Unlock checks and unlock actions; uses cache + `ProgressionRepository`. |
| `ProgressionRepository` | Async load/save of achievement rows. |

## Config

- `config/progression.yml` – Optional: list of known keys, display names, or gating rules.

## DB

- `progression` or `player_achievements` – e.g. `player_uuid`, `key`, `unlocked_at`.

## Integration

- Other modules (portals, bosses, quests) call `ProgressionService.hasUnlocked(uuid, key)` or `unlock(uuid, key)`.
- ProgressionModule depends on PlayerModule; progression state can be part of or referenced from `PlayerProfile`.
