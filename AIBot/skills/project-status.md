---
name: project-status
description: Report on Lost Wilderness project status and progress
user-invocable: true
homepage: https://github.com/yourusername/lost-wilderness
metadata: {"requires": {"env": []}}
---

# Lost Wilderness Project Status

When a user asks about project status, progress, or what's been completed:

## Process

1. **Check key status files**:
   - `CHANGELOG.md` - Recent changes
   - `Docs/implementation-status.md` - Feature completion status
   - `Docs/roadmap.md` - Planned features
   - `Docs/v2/v2-feature-parity.md` - V1 to V2 migration status

2. **Report on**:
   - Completed features
   - In-progress work
   - Planned features
   - Recent changes
   - Known issues or blockers

3. **Organize by category**:
   - Core systems (profiles, persistence)
   - Gameplay features (personality, classes, parties)
   - Cross-server features (portals, calendar)
   - Social features (clans, parties)
   - Economy and progression

## Status Categories

- ✅ **Complete**: Fully implemented and tested
- 🚧 **In Progress**: Currently being worked on
- 📋 **Planned**: Designed but not started
- ⏸️ **Deferred**: Low priority, future work
- ❌ **Blocked**: Waiting on dependencies

## Example Queries

- "what's the project status?"
- "what features are complete?"
- "what are you working on now?"
- "what's on the roadmap?"
- "what changed recently?"
- "show me implementation progress"

## Output Format

```
# Lost Wilderness Project Status

## Recently Completed
- [Features from CHANGELOG.md]

## Current Implementation Status
- ✅ [Complete features]
- 🚧 [In progress]
- 📋 [Planned]

## Next on Roadmap
- [Upcoming features]

## Known Issues
- [Any blockers or issues]

## Last Updated
[Date from CHANGELOG.md]
```
