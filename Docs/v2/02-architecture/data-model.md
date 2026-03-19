# Data model

Main entities and how they map to DB. Expand as modules are added.

---

## PlayerProfile

- **Identity:** UUID (primary), optional username/lastSeen.
- **Sub-state (later):** progression set, skills map, reputation, economy (wallets), story (quests, flags).
- **DB:** Base row in `player_profiles`; domain-specific tables keyed by player UUID.

---

## Progression

- **AchievementKey** – namespaced string (e.g. `story:el_diablo`, `boss:roofwither_kills_10`).
- **Per player:** Set of unlocked keys + optional timestamp.
- **DB:** `player_achievements` or `progression`: player_uuid, key, unlocked_at.

---

## Skills

- **Skill** – id, display name, XP curve.
- **SkillState** – per player per skill: xp, level.
- **DB:** `player_skills`: player_uuid, skill_id, xp, level, updated_at.

---

## Economy

- **Currency** – id, symbol, display name.
- **Wallet** – per player per currency: balance.
- **DB:** `wallets`: player_uuid, currency_id, balance, updated_at. Optional: `transaction_log`.

---

## Zodiac

- **ZodiacProfile** – per player: month_sign, year_sign, is_epochian, second_sign (Epochian only), spirit_animal, revealed states.
- **Signs** – 13 zodiac signs (Aries through Ophiuchus), assigned based on MC calendar join date.
- **Spirit Animals** – 14 animals (Wolf, Fox, Bat, etc.), randomly assigned, provide passive bonuses.
- **DB:** `player_zodiac`: player_uuid (PK), month_sign, year_sign, is_epochian, second_sign (nullable), second_sign_revealed, spirit_animal, spirit_animal_revealed, personality (nullable), created_at.

---

## Calendar

- **Global state** – current_day, year, season, last_tick; one row or small table.
- **DB:** `calendar_state` or equivalent.

---

## Events, Clans, Portals, Quests

- See respective plugin docs (plugin-events.md, plugin-clans.md, plugin-portals.md, plugin-quests.md) for entities and tables when implemented.
