---
name: search-docs
description: Search Lost Wilderness project documentation
user-invocable: true
homepage: https://github.com/yourusername/lost-wilderness
metadata: {"requires": {"env": []}}
---

# Search Lost Wilderness Documentation

When a user asks to search documentation or find information about a feature, system, or concept:

## Process

1. **Identify the search query** from the user's message
2. **Search these documentation directories** in order of priority:
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/Docs/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/Docs/v2/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/Docs/design/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/Docs/development/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/Docs/player/`

3. **Search for relevant files** containing the query terms
4. **Read matching files** and extract relevant sections
5. **Present results** with:
   - File paths (clickable format: `file_path:line_number`)
   - Relevant excerpts or summaries
   - Context about where the information appears

## Key Documentation Files

- `CHANGELOG.md` - Recent changes and updates
- `V2_ARCHITECTURE_PLAN.md` - Architecture overview
- `Docs/implementation-status.md` - Current implementation status
- `Docs/roadmap.md` - Future plans
- `Docs/v2/README.md` - V2 documentation index

## Example Queries

- "search for personality system"
- "find docs about calendar sync"
- "what documentation exists about portals?"
- "show me party system docs"

## Output Format

Always cite sources with file paths and provide direct quotes or summaries from the documentation.
