---
description: Write or update a feature spec doc for an existing implemented feature. Args: feature name (e.g. "personality", "classes", "calendar").
---

You are documenting an implemented feature in the Lost Wilderness plugin.

## Step 1 — Read the current state
- Check if `Docs/planning/feature-plans/$ARGUMENTS.md` already exists and read it
- Read the relevant source files under `PluginV2/src/main/java/com/lostwilderness/rpgcore/$ARGUMENTS/`
- Read any related config files in `Server/backends/survival-1/plugins/RPG_Core_V2/`

## Step 2 — Write or update the spec
Create or update `Docs/planning/feature-plans/$ARGUMENTS.md` using this structure:

```markdown
# Feature: [Name]
## Status
[Complete / In Progress / Planned]

## Purpose
[One paragraph: what problem this solves for players]

## Player-Facing Behaviour
[What players experience — commands, UI, mechanics]

## Commands
| Command | Permission | Description |
|---------|-----------|-------------|
| /example | lw.example | Does X |

## Config Options
[Key config values and what they control]

## Technical Notes
[Architecture, key classes, integration points]

## Known Limitations
[Anything that doesn't work or is intentionally cut]
```

## Rules
- Read the actual code — don't invent behaviour
- If something is unclear, note it as "needs verification"
- Cross-reference related features with relative links
- Never use Context7 for internal plugin docs — read the source directly
