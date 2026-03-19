# Plugin: Party

Party system (in-memory or optionally persistent).

## Purpose

- Form party, invite, leave; optional cross-server party via messaging.
- Used for shared objectives, rewards, or matchmaking.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `PartyService` | Create party, invite, accept, leave; party state. |
| `Party` | Party model: leader, members, state. |

## Config

- `config/party.yml` – Max size, invite timeout, cross-server (if supported).

## DB

- Optional: persist party for cross-server; otherwise in-memory per backend.

## Integration

- Quests or events may check “in party” or “party leader”; listeners delegate to PartyService.
