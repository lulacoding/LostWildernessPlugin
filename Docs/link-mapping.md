---
title: Link Mapping
description: Internal link registry for renamed and moved files.
tags:
  - reference
  - links
status: reference
phase: ongoing
owner: dev
action: none
---
# Documentation path mapping (refactor)

Old paths below map to new locations under `Docs/`. Update bookmarks and CI links accordingly.

| Old path | New path |
|----------|----------|
| `README.md` (Docs root) | `documentation-hub.md` |
| `implementation-status.md` | `archive/v1-implementation-status.md` (V1); active status: `roadmap/implementation-status.md` |
| `roadmap.md` | `archive/v1-roadmap.md` |
| `V2_ARCHITECTURE_PLAN.md` (repo root) | `architecture/v2-architecture.md` |
| `cursor_pluginv2_features_and_event_deta.md` | `archive/cursor-chat-export.md` |
| `Docs/continuous-improvement.md` | `../continuous-improvement.md` (repo root) |
| `Docs/failure-log.md` | `../failure-log.md` (repo root) |
| `Docs/v2/**` | Split across `architecture/`, `development/`, `systems/`, `roadmap/`, `planning/`, `testing/`, `reference/`, `archive/` |
| `planning/industry-arsenal/**` | `ideas/industry-arsenal/**` (speculative; Quartz `aliases` on each page redirect old slugs) |
| `Docs/design/**` | `design/worlds/**`, `overview/`, merged into `architecture/`, `systems/`, `plugins/`, `design/audio-visual.md`, `design/open-questions.md` |
| `Docs/development/**` | `development/**` (reorganized); `test-guide.md` → `testing/test-guide.md` |
| `Docs/ops/**` | `operations/**` |
| `Docs/future/**` | `operations/**` (merged) |
| `Docs/player/README.md` | `player/player-handbook.md` |
| `Docs/player/home.md` | merged into `player/player-handbook.md` |
| `Docs/player/common.md` | `player/commands.md` |
| `Docs/player/lobby-and-verification.md` | `player/getting-started.md` |
| `Docs/player/calendar-and-time.md` + `calendar-plugin.md` | `player/calendar.md` |
| `Docs/PHASE*-ROADMAP.md` | `roadmap/phase-*.md` |

See repository `CHANGELOG.md` for the documentation restructuring entry.
