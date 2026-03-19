---
name: explain-feature
description: Explain Lost Wilderness features comprehensively by combining docs and code
user-invocable: true
homepage: https://github.com/yourusername/lost-wilderness
metadata: {"requires": {"env": []}}
---

# Explain Lost Wilderness Feature

When a user asks to explain a feature, system, or component:

## Process

1. **Identify the feature** from the user's query
2. **Gather information from multiple sources**:
   - Documentation in `Docs/` directories
   - Implementation code in `PluginV2/`
   - Configuration files
   - Architecture documents
   - Implementation status

3. **Compile a comprehensive explanation covering**:
   - **What it is**: High-level description and purpose
   - **How it works**: Architecture and flow
   - **Implementation**: Key classes and components
   - **Configuration**: Relevant config files
   - **Status**: Current implementation state
   - **Related systems**: Dependencies and integrations

4. **Cite all sources** with file paths

## Major Features to Know

- **Personality System**: 13 traits, 5 elements, progressive tiers, ultimate items
- **Class System**: 5 player classes with skill trees, mastery items
- **Party System**: Group play mechanics
- **Calendar System**: Multi-server date/season sync
- **Portal System**: Cross-server travel
- **Clan System**: Player organizations
- **Quest System**: Data-driven objectives
- **Economy System**: Multi-currency wallets
- **Skills System**: AuraSkills integration
- **Events System**: World events engine

## Example Queries

- "explain the personality system"
- "tell me about party system"
- "how does the calendar work?"
- "what is the class mastery system?"
- "explain portal mechanics"

## Output Structure

```
# [Feature Name]

## Overview
[High-level description]

## Architecture
[How it's designed]

## Implementation
[Key classes and files]

## Configuration
[Config files and settings]

## Current Status
[Implementation progress]

## Related Systems
[Dependencies and integrations]

## Sources
- [File paths with line numbers]
```
