# Phase 3.1 – Skills (AuraSkills integration)

## Goal

**Use AuraSkills as the skill layer.** PluginV2 does not implement its own XP/level storage for skills. When AuraSkills is on the server, V2 integrates with it so that:

- **Progression / milestones** can grant AuraSkills XP (e.g. unlock `first_join` → give 50 Fighting XP).
- **Quests / gates** (later) can check AuraSkills level (e.g. "Mining ≥ 5").
- **Classes / other systems** (later) can give starter XP or check levels via the same bridge.

## How it works

1. **AuraSkills** – You install the AuraSkills plugin on the server. It provides:
   - 11 default skills (Farming, Foraging, Mining, Fishing, Excavation, Archery, Defense, Fighting, Agility, Enchanting, Alchemy).
   - XP from mining, fishing, combat, etc., and `/skills` / `/stats` menus.

2. **PluginV2 skills module** – Optional module that:
   - Detects if AuraSkills is loaded.
   - Exposes a **bridge** (e.g. `AuraSkillsBridge` or `SkillsIntegrationService`) with:
     - `boolean isAvailable()`
     - `void addSkillXp(UUID player, String skillKey, double amount)` – only affects **online** players (API limitation).
     - `int getSkillLevel(UUID player, String skillKey)` – returns 0 if AuraSkills not present or player offline.
   - Skill keys are lowercase AuraSkills names: `farming`, `mining`, `fighting`, `fishing`, `foraging`, `excavation`, `archery`, `defense`, `agility`, `enchanting`, `alchemy`.

3. **Linking**
   - **Progression:** When a milestone is unlocked, optionally grant AuraSkills XP (configurable, e.g. in `progression.yml` or `skills.yml`).
   - **Other modules:** Get the bridge from `ServiceRegistry` and call `addSkillXp` / `getSkillLevel` when needed.

## Dependency

- **compileOnly** `dev.aurelium:auraskills-api-bukkit:2.3.x` so the code compiles. At runtime the AuraSkills plugin provides the API; no need to ship it in our JAR.

## Config (optional)

- `config/skills.yml` (or under progression): list of milestone → skill XP rewards, e.g. `first_join: { fighting: 50 }`, `join_3_times: { fighting: 25 }`. If missing, no XP is granted on milestone unlock.

## AuraSkills config (what to set up)

You don’t need to connect AuraSkills to PluginV2’s database. AuraSkills can keep using its default file storage (`sql.enabled: false` in `plugins/AuraSkills/config.yml`). If you want AuraSkills to use MySQL, set `sql.enabled: true` and fill in host/database/username/password; that’s independent of PluginV2’s DB.

**Important for milestone XP:**

- **Worlds** – In AuraSkills `config.yml`, `blocked_worlds` and `disabled_worlds` stop XP (including API-granted XP) in those worlds. If the list is only `Example`, no real world is blocked. If you add your play world there, players there won’t get the 50 XP from first_join. Leave the list empty or only with worlds where you want skills disabled.
- **Skills** – Fighting (and other default skills) are enabled by default. No need to enable anything extra for the bridge.
- **Optional** – Use `default_language`, `action_bar`, `boss_bar`, etc. as you like; they don’t affect the bridge.

## No DB linking

You do **not** need to link PluginV2’s database to AuraSkills. Each plugin uses its own DB: V2 for progression (milestones, counters), AuraSkills for skill XP/levels. The bridge only calls AuraSkills’ API when a milestone unlocks (e.g. grant 50 Fighting XP). AuraSkills stores that XP in its own DB.

## Troubleshooting: no XP on first_join

1. **AuraSkills in `plugins/`** – The AuraSkills JAR must be on the server. On startup you should see `[skills] AuraSkills detected; bridge active.` in the RPG_Core_V2 log. If you see `AuraSkills not detected`, install AuraSkills and restart.
2. **Skills module enabled** – In `config/core.yml`, `enabled-modules` must include `skills`. Restart after changing.
3. **Load order** – `plugin.yml` has `softdepend: [AuraSkills]` so RPG_Core_V2 loads after AuraSkills when both are present.
4. **Timing** – XP is granted ~0.75s after join so AuraSkills has time to load the player. If you see `[skills] AuraSkills returned no user for ...` in the console, AuraSkills may not have the player loaded yet; the delay is already in place to reduce this.
5. **Success** – When XP is granted you should see: `[skills] Granted 50.0 fighting XP via AuraSkills for <uuid>.` in the console. Check `/skills` (AuraSkills) to confirm Fighting XP increased.

## Summary

| Item | Action |
|------|--------|
| AuraSkills on server | You install it; provides all skill XP and menus. |
| PluginV2 skills module | Registers bridge; other modules use it to give/check XP and levels. |
| Milestone → XP | Optional config; progression (or listener) calls bridge when milestone unlocks. |
