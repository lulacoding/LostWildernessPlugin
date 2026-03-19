---
name: find-code
description: Find code implementations in Lost Wilderness PluginV2
user-invocable: true
homepage: https://github.com/yourusername/lost-wilderness
metadata: {"requires": {"env": []}}
---

# Find Lost Wilderness Code

When a user asks to locate code, find a class, or search for implementations:

## Process

1. **Identify what to search for** (class name, feature, system, method)
2. **Search these source directories**:
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/PluginV2/src/main/java/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/PluginV2/lobby-plugin/src/main/java/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/PluginV2/survival-plugin/src/main/java/`
   - `C:/Users/cthvh/OneDrive/Desktop/Lost Wilderness/PluginV2/amplified-plugin/src/main/java/`

3. **Look for**:
   - Java class files (`.java`)
   - Package structures
   - Service implementations
   - Configuration files

4. **Present results with**:
   - Full file paths
   - Class/method signatures
   - Brief code snippets showing the implementation
   - Package locations

## Common Searches

- Services: `*Service.java`, `*Manager.java`, `*Handler.java`
- Models: `*Data.java`, `*Profile.java`, `*Config.java`
- Commands: `*Command.java`
- Listeners: `*Listener.java`, `*Handler.java`

## Example Queries

- "find PartyService class"
- "locate personality system code"
- "where is the calendar implementation?"
- "show me PlayerProfile class"
- "find all skill-related classes"

## Output Format

Provide:
1. Full file path with line numbers where relevant
2. Package name
3. Class/method signatures
4. Brief description of what the code does
5. Key dependencies or related classes
