## Workflow Orchestration

### 1. Plan Mode Default
- Enter plan mode for ANY non-trivial task (3+ steps or architectural decisions)
- If something goes sideways, STOP and re-plan immediately — don't keep pushing
- Use plan mode for verification steps, not just building
- Write detailed specs upfront to reduce ambiguity

### 2. Subagent Strategy
- Use subagents liberally to keep main context window clean
- Offload research, exploration, and parallel analysis to subagents
- **Parallel subagents only when tasks are truly independent** — non-overlapping scope, no shared state; merge results carefully
- For complex problems, throw more compute at it via subagents
- One task per subagent for focused execution

### 3. Self-Improvement Loop
- After ANY correction from the user: update `tasks/lessons.md` with the pattern
- Write rules for yourself that prevent the same mistake
- **Strict write discipline:** Only record lessons/patterns after verifying success — never log failed attempts as truth
- **Memory as hint:** Treat `tasks/lessons.md` and `MEMORY.md` as hints, not ground truth — verify facts from source before acting on them
- Ruthlessly iterate on these lessons until mistake rate drops
- Review lessons at session start for relevant project

### 4. Verification Before Done
- Never mark a task complete without proving it works
- **Before writing code:** State explicitly how you'll test it (which command, test, or manual check)
- **After writing code:** Perform adversarial self-review — critique your own `git diff` as a strict senior engineer who dislikes your implementation; name at least one potential bug or edge case before finishing
- Challenge your own work before presenting it

### 5. Autonomous Bug Fixing
- When given a bug report: just fix it. Don't ask for hand-holding
- Point at logs, errors, failing tests — then resolve them

## Output Contract

Every meaningful task response must end with:
1. **Files changed** — list each file modified
2. **Behavior changed** — one sentence per file
3. **Verification** — concrete command or manual step to confirm it works

No exceptions. No "it should work" without a verification path.

## Task Management

1. **Plan First**: Write plan to `tasks/todo.md` with checkable items
2. **Verify Plan**: Check in before starting implementation
3. **Track Progress**: Mark items complete as you go
4. **Explain Changes**: High-level summary at each step
5. **Document Results**: Add review section to `tasks/todo.md`
6. **Capture Lessons**: Update `tasks/lessons.md` after corrections — only after verifying the lesson is true

## Core Principles

- **Simplicity First**: Make every change as simple as possible. Impact minimal code.
- **No Laziness**: Find root causes. No temporary fixes. Senior developer standards.
- **Minimal Impact**: Changes should only touch what's necessary. Avoid introducing bugs.
- **Tool Discipline**: Search before opening. Open the smallest relevant section. Limit to 1–2 external lookups per ambiguity. Never re-read a section already read this session unless new evidence requires it. Stop using tools once the answer is grounded.

## Commit Hygiene

- Commit messages, changelog entries, and code comments must not expose internal codenames, unreleased feature names, or developer-only identifiers
- Write commit messages as if they're public — because they are

## Project: Lost Wilderness — Plugin Development

### Build Commands
- Main plugin (fat JAR): `cd PluginV2 && ./gradlew.bat shadowJar -x test`
- Survival wrapper only: `./gradlew.bat :survival-plugin:jar -x test` (no shadow plugin)
- Build + deploy all to all 3 servers: `PluginV2/build-and-copy-to-server.bat`
- Deployed JAR: `PluginV2/build/libs/RPG_Core_V2-2.0.0-SNAPSHOT-all.jar` → `Server/backends/*/plugins/RPG_Core_V2.jar`

<important if="writing Java code or touching any Bukkit/Paper/Spigot/AuraSkills API">
### Key Technical Gotchas
- **Always use Context7 MCP for Paper/Spigot/Bukkit/AuraSkills API lookups** — do not guess methods or invent APIs. If Context7 is unavailable, state the uncertainty explicitly and do not proceed until resolved.
- **Never `.join()` on main thread** — all DB calls return CompletableFuture; use cache + `whenComplete` pattern to avoid blocking the server thread
- **GUI title check** — use `event.getView().getTitle()` (String), NOT Adventure `.title()` — all existing menus use the deprecated String form
- **BetonQuest API** — compileOnly dep: `files('../Server/backends/survival-1/plugins/BetonQuest.jar')`. Tag check: `PlayerConverter.getID(player)` → `BetonQuest.getInstance().getPlayerData(profile)` → `playerData.hasTag(fullTag)`
- **Deprecation warnings are expected** — ChatColor, setDisplayName, setLore, getTitle all deprecated in Paper 1.21 but used project-wide; warnings are normal, errors are not
</important>

### Project Structure
- `PluginV2/src/` — RPG_Core_V2 shared core (20 domains: boss, calendar, clans, classes, events, party, pets, personality, portals, progression, quests, reputation, skills, story, zodiac…)
- `PluginV2/survival-plugin/` — LW_Survival_V2 thin wrapper (personality listeners, elemental passives)
- `PluginV2/lobby-plugin/` — LW_Lobby_V2 (personality quiz)
- `PluginV2/amplified-plugin/` — LW_Amplified_V2
- New RPG modules: implement `RpgModule`, register in `RPGCorePlugin.java`, add to `plugin.yml` + `core.yml` `enabled-modules`

### Server Stack (survival-1)
- BetonQuest, AuraSkills, Citizens, Iris (volcanic spawn region), WorldGuard, WorldEdit, spark
- BetonQuest packages: `lw_elder`, `lw_blacksmith`, `lw_herbalist`, `lw_shrine`, `lw_professor`
- Serena dashboard (Claude Code): http://127.0.0.1:24283/dashboard/index.html
