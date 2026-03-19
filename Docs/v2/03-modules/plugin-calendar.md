# Plugin: Calendar

Global in-game date, season, and leader/follower semantics.

## Purpose

- Single source of truth for in-game day/year/season.
- One leader node writes to DB and (optionally) emits ticks; followers poll or subscribe via messaging.
- Formalize seasons and special cycles (e.g. eclipse).

## Main classes

| Class | Responsibility |
|-------|----------------|
| `CalendarService` | Current day/year/season; leader/follower logic; read/write via CalendarRepository. |
| `SeasonModel` | Season definitions, eclipse cadence, etc. |
| `CalendarRepository` | Async load/save of global calendar state. |

## Config

- `config/calendar.yml` – Real-time ratio, season lengths, eclipse cycle, leader node id (if multi-node).

## DB

- `calendar_state` or similar – current_day, year, season, last_tick, etc.

## Integration

- EventEngine and other modules read from CalendarService; only the designated leader writes.
