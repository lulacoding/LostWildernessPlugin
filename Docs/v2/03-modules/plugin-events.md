# Plugin: Events

World events (Eclipse, Paranoia, etc.) and cascades.

## Purpose

- Schedule and run world events based on calendar, player preference, and server role.
- EventRegistry + EventCascadeRegistry; heavy logic in domain handlers, not in listeners.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `WorldEvent` | Interface for a single event type (e.g. Eclipse). |
| `EventEngine` | Schedules events; holds EventRegistry and EventCascadeRegistry. |
| `EventRegistry` | Map of event id → handler. |
| `EventCascadeRegistry` | Cascades (e.g. event A triggers B). |
| `PlayerEventPreferenceService` | Per-player event prefs; mapped into profile or separate store. |

## Config

- `config/events.yml` – Event definitions, weights, cadence, cascade rules.

## DB

- Event state (which event is active, start time, etc.) and optionally player preferences; tables TBD per design.

## Integration

- CalendarService drives tick; EventEngine reacts and runs events. Listeners only notify the engine (e.g. “day changed”, “player joined during event”).
