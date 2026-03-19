# Events & calendar gap: old plugin vs PluginV2

This document lists **what the old plugin (LWEvents/Survival) has** that **PluginV2 does not yet have**, so you can bring over “everything” (all events, Easter, etc.).

---

## 1. Calendar / special-day behaviour

| Feature | Old plugin | PluginV2 | Notes |
|--------|------------|---------|--------|
| Day-change title | "March 1, 1MC" | ✅ Same | CalendarDayChangeTitleListener |
| Day-change subtitle (season) | Season message or "Day X" | ✅ Same | buildSubtitle() |
| **Easter week subtitle** | Palm Sunday → Easter Sunday titles (e.g. "§d🌿 Palm Sunday…", "§a☀ He is risen! Happy Easter Sunday! ☀") | ❌ Missing | EasterWeekEvent.getTitleForDay(day); used in DayChangeListener before season subtitle |
| New Year fireworks | At (0,0), scale by decade/century/millennium | ✅ Same | NewYearFireworkHandler |
| Placeable calendar item (book UI) | Right-click with item opens date/season book | ❌ Missing | CalendarItemListener (optional) |

**Easter in old plugin:** Not a “world event” that starts/ends. It only provides **subtitle text** for the 8 days (Palm Sunday through Easter Sunday) based on MC calendar year and a Gregorian Easter calculation. So in V2 you only need to add Easter week **subtitle** logic to the day-change title listener (and optionally a small Easter-date helper).

---

## 2. World events (daily loop)

Old plugin runs these in one daily loop (with cooldowns, TPS guard, cascades, boss bar). PluginV2 has the loop and infra but only **Eclipse** and **TestEvent** implemented.

| Event | Old plugin | PluginV2 | Notes |
|-------|------------|----------|--------|
| **Eclipse** | Interval/offset/chance, night freeze, pack, horde | ✅ Done | EclipseEvent, EclipseHordeTask |
| **Thunder** | Base chance + “eclipse storm” (horses, etc.) | ✅ Done | ThunderEvent; cascade from Eclipse |
| **Blizzard** | Chance, block freeze + BlockRestoreManager | ✅ Done | BlizzardEvent |
| **Frost** | Chance, block freeze + BlockRestoreManager | ✅ Done | FrostEvent |
| **Fog** | Chance, restore delay | ✅ Done | FogEvent |
| **Summer heatwave** | Chance, season-gated | ✅ Done | SummerHeatwaveEvent |
| **Spring bloom** | First day of spring (March 1) | ✅ Done | SpringBloomEvent |
| **Autumn leaf fall** | Autumn season | ✅ Done | AutumnLeafFallEvent |
| **Seasonal storm** | Chance, autumn/winter months | ✅ Done | SeasonalStormEvent |
| **Jungle monsoon** | Chance, summer | ✅ Done | JungleMonsoonEvent |
| **Paranoia** | Eclipse atmosphere, horror sounds | ✅ Done | ParanoiaEvent; cascade from Eclipse |

So **missing event implementations:** Thunder, Blizzard, Frost, Fog, Heatwave, SpringBloom, AutumnLeafFall, SeasonalStorm, JungleMonsoon, Paranoia.

---

## 3. Cascades (old plugin)

| Cascade | Old plugin | PluginV2 |
|---------|------------|----------|
| Eclipse → Thunderstorm (triggerEclipseStorm) | ✅ | ❌ No ThunderEvent yet |
| Eclipse → Paranoia | ✅ | ❌ No ParanoiaEvent yet |

EventCascadeRegistry exists in V2; once Thunder and Paranoia exist, register them in EventsModule.

---

## 4. Supporting systems

| Feature | Old plugin | PluginV2 | Notes |
|--------|------------|----------|--------|
| **Block restore (crash-safe)** | BlockRestoreManager for Blizzard/Frost; restore on startup | ❌ Missing | block_restore.yml, restorePendingAndDeleteFile() |
| **Mob buffs** | EventMobBuffsListener: buff natural spawns during Eclipse/Winter | ❌ Missing | events.mob-buffs.* |
| **Horde spawner** | NaturalHordeSpawnerTask: **biome-aware** (desert→husk, etc.), jockey chance | ⚠️ Partial | V2 EclipseHordeTask is fixed types, no biome/jockey |
| **Sleep blocking** | During some events (e.g. Eclipse) | ⚠️ Partial | EclipseEvent can freeze night; explicit “no sleep” message/listener if desired |
| **Player event prefs** | MySQL player_event_prefs, /event toggle, /event togglebar | ❌ Missing | PlayerEventPreferenceManager, EventBossBarManager per-player hide |
| **Boss bar** | Shows active events; per-player toggle | ✅ Partial | EventBossBarManager exists; no DB toggle yet |
| **TPS guard** | events.pause-if-tps-below | ✅ | Same |
| **Per-event cooldowns** | events.cooldowns.<Name> | ✅ | Same |

---

## 5. Summary: status

**Calendar / titles**

- **Easter week subtitles** ✅ (EasterWeekHelper + CalendarDayChangeTitleListener).

**World events (all ported)**

- Thunder ✅ (base chance + Eclipse → Thunderstorm cascade).
- Blizzard ✅ (BlockRestoreManager).
- Frost ✅ (BlockRestoreManager, winter-only, heat-source check).
- Fog ✅.
- Summer heatwave ✅ (summer-only).
- Spring bloom ✅ (March 1), Autumn leaf fall ✅ (autumn), Seasonal storm ✅ (autumn/winter months), Jungle monsoon ✅ (summer).
- Paranoia ✅ (cascade from Eclipse, horror sounds).

**Supporting**

- BlockRestoreManager ✅ (in-memory restore for Blizzard/Frost).
- Optional (not yet): crash-safe persist/restore on startup; mob buffs; biome-aware horde + jockey; player event preferences + /event toggle, /event togglebar.

**Optional calendar**

- Placeable calendar item (book UI) — not implemented.
