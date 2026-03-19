---
name: architecture-query
description: Answer questions about Lost Wilderness architecture and design
user-invocable: true
homepage: https://github.com/yourusername/lost-wilderness
metadata: {"requires": {"env": []}}
---

# Lost Wilderness Architecture Query

When a user asks about architecture, design decisions, or technical structure:

## Process

1. **Understand the architectural question**
2. **Reference these key documents**:
   - `V2_ARCHITECTURE_PLAN.md` - Main architecture document
   - `Docs/v2/architecture-overview.md` - Detailed architecture
   - `Docs/v2/goals-and-scope.md` - Project goals
   - `Docs/v2/roadmap.md` - Development roadmap
   - `Docs/v2/phase0-outcomes.md` - Foundation decisions
   - `Docs/v2/module-contracts.md` - Module system design
   - `Docs/v2/data-model.md` - Data structures

3. **Provide context about**:
   - Overall architecture (3-server setup: Lobby, Survival, Amplified)
   - Module system and dependencies
   - Data persistence strategy
   - Cross-server communication
   - Technology stack (Java 21, Paper 1.21.1, Gradle)

## Key Architecture Concepts

### Three-Server Setup
- **Lobby**: Hub server, player profiles, party system
- **Survival**: Gameplay server with full feature set
- **Amplified**: Endgame server with enhanced mechanics

### Module System
- Modular plugin architecture
- Clear module contracts and lifecycle
- Dependency-based loading
- Async-first design

### Data Model
- Player profiles (persistent identity)
- Skills data (XP, levels, stats)
- Party data (group membership)
- Economy data (wallets, balances)
- Calendar state (global date/season)

### Technology Stack
- Java 21
- Paper 1.21.1 (Minecraft server)
- Gradle build system
- AsyncMySQL for persistence
- PDC (Persistent Data Container) for items

## Example Queries

- "what is the overall architecture?"
- "how does cross-server communication work?"
- "explain the module system"
- "what's the data persistence strategy?"
- "why three servers instead of one?"
- "how do player profiles work?"

## Output Format

Provide clear, structured answers with:
- Direct answers to the question
- Architectural diagrams (if relevant, describe in text)
- File references for detailed information
- Related architectural decisions
- Trade-offs and rationale
