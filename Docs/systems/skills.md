---
title: Skills
description: AuraSkills bridge for skill XP and level queries.
tags:
  - system
  - skills
status: implemented
phase: phase-1
owner: dev
action: none
---
# Plugin: Skills

XP, levels, and skill curves (mining, combat, etc.).

## Purpose

- Define a registry of skills and configurable XP curves.
- Handle XP gain and level-up; persist via async repository.
- Integrate with items, quests, and other rewards.

## Main classes

| Class | Responsibility |
|-------|----------------|
| `Skill` | Skill definition (id, display name, XP curve ref). |
| `SkillState` | Per-player XP and level for a skill. |
| `SkillService` | XP gain, level-up logic, rewards; uses cache + SkillRepository. |
| `SkillRepository` | Async load/save of skill state per player. |

## Config

- `config/skills.yml` – Skill IDs, XP curve formulas (e.g. level → XP required), rewards per level if any.

## DB

- `player_skills` – e.g. `player_uuid`, `skill_id`, `xp`, `level`, `updated_at`.

## Integration

- Listeners (block break, kill, etc.) delegate to `SkillService.addXp(uuid, skillId, amount)`.
- SkillService depends on PlayerModule; state can be part of profile or loaded via service cache.
