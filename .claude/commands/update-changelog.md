---
description: Read recent git commits and update the project CHANGELOG. Run after completing any feature or fix.
---

Update the Lost Wilderness plugin changelog.

## Step 1 — Read current state
- Read `CHANGELOG.md` if it exists, or check `Docs/` for any changelog file
- Run `git log --oneline -20` to see the 20 most recent commits
- Identify the last commit that was already documented in the changelog

## Step 2 — Categorise new commits
Group undocumented commits into Keep-a-Changelog categories:
- **Added** — new features
- **Changed** — changes to existing behaviour
- **Fixed** — bug fixes
- **Removed** — removed features
- **Technical** — refactors, build changes, dependency updates

Skip: merge commits, doc-only commits, chore commits unless significant.

## Step 3 — Update the changelog
If `CHANGELOG.md` exists at project root, update it.
Otherwise create it at `CHANGELOG.md`.

Format:
```markdown
# Changelog

## [Unreleased]
### Added
- ...
### Fixed
- ...

## [YYYY-MM-DD]
...
```

Keep entries concise — one line per change, player-facing language where possible.
