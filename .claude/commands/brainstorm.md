---
description: Brainstorm a new feature for Lost Wilderness. Reads existing docs first to avoid conflicts. Args: feature name/idea.
---

You are brainstorming a new feature for the Lost Wilderness Minecraft RPG server.

## Step 1 — Read existing context first
Before generating any ideas, read:
- `Docs/` directory overview to understand what features already exist
- Any existing feature specs related to $ARGUMENTS if they exist
- `PluginV2/src/main/java/com/lostwilderness/rpgcore/` package list to understand current domains

## Step 2 — Brainstorm
Brainstorm "$ARGUMENTS" as a new feature. Cover:
- **Player-facing behaviour** — 2-3 design variants with trade-offs
- **Fit with existing systems** — how it integrates with personality, classes, quests, AuraSkills
- **Implementation scope** — which domains/modules it touches, rough complexity
- **Config options needed** — what would go in config files
- **Potential conflicts or edge cases** — what could break or feel wrong

## Step 3 — Output a draft spec
Save the draft to `Docs/planning/feature-plans/$ARGUMENTS-draft.md` using this structure:

```markdown
# Feature: [Name]
## Purpose
## Player-Facing Behaviour
### Variant A
### Variant B
## Integration Points
## Config Options
## Edge Cases / Risks
## Open Questions
```

Do not implement anything. This is design only.
