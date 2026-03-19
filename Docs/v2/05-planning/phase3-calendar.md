# Phase 3.3 – Calendar V2 (shared date & season)

## Goal

Provide a shared in-game calendar (date, day count, season, join dates) for Survival and Amplified via `RPG_Core_V2`, so both backends see the same world date/season while Lobby stays separate.

## Architecture

- **Core (`RPG_Core_V2`)**
  - `calendar` module (`CalendarModule`) uses the existing infra (ConfigService, DatabaseProvider, SchedulerService).
  - `CalendarRepository` stores:
    - A single global `calendar_state` row: `epoch_day`, `mc_day`, `year`, `month`, `day`, `season`.
    - Per-player join records in `calendar_player_join` (`player_uuid`, `join_epoch_day`).
  - `CalendarServiceV2` (and `CalendarServiceV2Impl`) exposes:
    - `getCurrentSnapshot()` → date, day count, season, mcDay.
    - `getPlayerJoinDate(UUID)` and `recordFirstJoinIfAbsent(UUID)`.
    - Admin operations: `advanceToNextDay()`, `resetCalendar()`.
  - A repeating task (every ~10 seconds) watches Minecraft day (`world.getFullTime()/24000`) and advances the calendar when MC day increases.

- **Wrappers (`LW-Survival-V2`, `LW-Amplified-V2`)**
  - Both depend on `RPG_Core_V2` and fetch `CalendarServiceV2` from `RPGCorePlugin.getInstance().getService(CalendarServiceV2.class)`.
  - **Survival** registers:
    - `/date`, `/time`, `/eoc`, `/season`, `/datejoined` – read-only.
    - `/nextday`, `/resetcalendar` – admin-only (permission `lw.admin.calendar`).
  - **Amplified** registers read-only commands:
    - `/date`, `/time`, `/eoc`, `/season`, `/datejoined`.

## Behaviour

- **Global state**
  - Calendar starts at an arbitrary base date (1/1/1), epoch day 0, season SPRING, mcDay 0.
  - Each new Minecraft day triggers `advanceToNextDayInternal`:
    - Increments `epoch_day`, `date`, and recomputes `season` based on month:
      - Spring: Mar–May, Summer: Jun–Aug, Autumn: Sep–Nov, Winter: Dec–Feb.
    - Persists to `calendar_state`.
    - (Future: will also fire a Bukkit event that world events can listen to.)

- **Player join dates**
  - On first join, wrappers (or other listeners) can call `recordFirstJoinIfAbsent(UUID)` so `/datejoined` has a stable value.
  - Join date is stored as `join_epoch_day`; `getPlayerJoinDate(UUID)` returns a `LocalDate` for display.

- **Backends**
  - Survival and Amplified both use the same `player` datasource and thus share `calendar_state` and `calendar_player_join`.
  - Lobby does not load `RPG_Core_V2` or the calendar module; it remains independent.

## Commands summary

- **Survival (`LW-Survival-V2`)**
  - `/date` – show current calendar date.
  - `/time` – show current in-game clock + MC day number.
  - `/eoc` – show days since Epoch (calendar day count).
  - `/season` – show current season.
  - `/datejoined` – show when you first joined (calendar date).
  - `/nextday` – admin; advance calendar to the next day.
  - `/resetcalendar` – admin; reset calendar back to day 0.

- **Amplified (`LW-Amplified-V2`)**
  - `/date`, `/time`, `/eoc`, `/season`, `/datejoined` – same as Survival, all read-only.

