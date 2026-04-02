---
title: Classes
description: Five player classes with skill trees and AuraSkills mastery integration.
tags:
  - system
  - classes
status: implemented
phase: phase-1
owner: dev
action: none
---
# Classes

Player **classes** (Templar, Ranger, Artificer, Cultist, Berserker) in PluginV2: skill tree, passives, ultimates, AuraSkills integration.

## Status and implementation

Authoritative per-feature status: [Implementation status](../roadmap/implementation-status.md) (Classes row). Design and milestone planning: [Class skill tree plan](../planning/feature-plans/class-skill-tree.md).

## Code locations (PluginV2)

- `ClassesModule`, `ClassService`, `ClassSkillTreeService`, `PlayerClassRepository`
- Listeners: `ClassAbilityListener`, `ClassPassiveListener`, `ClassMasteryXpListener`, `ClassStarterXpJoinListener`
- Commands/UI: `ClassCommand`, `ClassSelectionMenu`, `UltimateItemBuilder`
