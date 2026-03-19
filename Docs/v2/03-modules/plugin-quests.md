# Plugin: Quests

Data-driven quests and storyline.

## Purpose

- Quest definitions (objectives, conditions, rewards) from config or DB.
- Track quest state per player; objectives use handler interface (kill, collect, visit, talk, craft, etc.).
- Conditions: reputation, skill level, milestones. Rewards: items, XP, currency, progression flags.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `QuestService` | Load definitions; start/progress/complete quest; state in dedicated tables. |
| `QuestDefinition` | Objectives, conditions, rewards (data model). |
| `ObjectiveHandler` | Interface per objective type; implementations registered with QuestService. |
| `QuestRepository` | Async load/save quest state. |

## Config

- `config/quests.yml` – Quest definitions (or path to JSON/DB). Objective types and reward templates.

## DB

- `quest_definitions` (or loaded from config). `quest_state` – player_uuid, quest_id, step, progress, completed_at, etc.

## Integration

- Quest state is part of or referenced from PlayerProfile storyState. Other modules (progression, economy, skills) are called for conditions and rewards.
