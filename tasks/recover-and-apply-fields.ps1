
# Recovers files from the bad apply-log-fields.ps1 run.
# For each mapped file:
#   1. Strip the injected scriptblock junk
#   2. Remove leading blank lines
#   3. Prepend full correct frontmatter (original yaml + 4 new log fields)
#
# Run from repo root: powershell -ExecutionPolicy Bypass -File tasks\recover-and-apply-fields.ps1

$base = "c:\Users\cthvh\OneDrive\Desktop\Lost Wilderness\Docs\"

# ── Frontmatter map ──────────────────────────────────────────────────────────
# Each entry: relative path => @{ title; description; tags (array); status; phase; owner; action }
# Tags are the section + topic tags established in the previous standardisation run.

function M($title, $desc, $tags, $status, $phase, $owner, $action) {
  return @{ title=$title; desc=$desc; tags=$tags; status=$status; phase=$phase; owner=$owner; action=$action }
}

$map = @{
  # ── systems ─────────────────────────────────────────────────────────────
  "systems\calendar.md"              = M "Calendar" "Global in-game date, season, and leader/follower calendar module." @("system","calendar") "implemented" "phase-1" "dev" "none"
  "systems\clans.md"                 = M "Clans" "Clan creation, war, alliances, and display systems." @("system","clans") "implemented" "phase-1" "dev" "none"
  "systems\classes.md"               = M "Classes" "Five player classes with skill trees and AuraSkills mastery integration." @("system","classes") "implemented" "phase-1" "dev" "none"
  "systems\core.md"                  = M "Core" "RPGCorePlugin infrastructure: ModuleManager, ConfigService, DatabaseProvider, SchedulerService." @("system","core") "implemented" "ongoing" "dev" "none"
  "systems\economy.md"               = M "Economy" "Planned multi-currency wallet and transaction system (not yet started)." @("system","economy") "planned" "phase-2" "dev" "needs-dev"
  "systems\events.md"                = M "Events" "Daily and seasonal events module with 22 implemented events; Nether events pending." @("system","events") "partial" "phase-1" "dev" "needs-dev"
  "systems\party.md"                 = M "Party" "Cross-server party system with buffs, visual effects, and shared objectives." @("system","party") "implemented" "phase-1" "dev" "none"
  "systems\personality-mechanics.md" = M "Personality Mechanics" "Passive trait effects, elemental powers, holy enchants, and ultimate items detail." @("system","personality") "implemented" "phase-1" "dev" "none"
  "systems\personality-overview.md"  = M "Personality Overview" "High-level overview of the 13-trait personality system and quiz flow." @("system","personality") "implemented" "phase-1" "dev" "none"
  "systems\portals.md"               = M "Portals" "Cross-server portal system using crying obsidian frames and BungeeCord." @("system","portals") "implemented" "phase-1" "dev" "none"
  "systems\progression.md"           = M "Progression" "Milestone and achievement system with BetonQuest bridge." @("system","progression") "implemented" "phase-1" "dev" "none"
  "systems\quests.md"                = M "Quests" "Planned quest system via BetonQuest external plugin (not yet started)." @("system","quests") "planned" "phase-2" "dev" "needs-dev"
  "systems\skills.md"                = M "Skills" "AuraSkills bridge for skill XP and level queries." @("system","skills") "implemented" "phase-1" "dev" "none"
  "systems\zodiac.md"                = M "Zodiac" "13 zodiac signs, 14 spirit animals, sync bonus, and periodic effects." @("system","zodiac") "implemented" "phase-1" "dev" "none"
  "systems\systems-hub.md"           = M "Systems Hub" "Navigation hub for all RPG system documentation." @("system","hub") "reference" "ongoing" "admin" "none"
  # ── design ──────────────────────────────────────────────────────────────
  "design\design-hub.md"                    = M "Design Hub" "Navigation hub for all game design documentation." @("design","hub") "reference" "ongoing" "admin" "none"
  "design\audio-visual.md"                  = M "Audio & Visual" "Music, sound design, and visual identity planning." @("design","audio-visual") "planned" "phase-2" "design" "needs-design"
  "design\open-questions.md"                = M "Open Questions" "Aggregated open design questions across all systems." @("design","open-questions") "reference" "ongoing" "admin" "needs-review"
  "design\worlds\design-worlds-hub.md"      = M "Worlds Design Hub" "Navigation hub for world design documentation." @("design","worlds","hub") "reference" "ongoing" "admin" "none"
  "design\worlds\amplified.md"              = M "Amplified World Design" "Design notes for the Amplified server world." @("design","worlds","amplified") "reference" "ongoing" "design" "none"
  "design\worlds\devoid-and-diablo.md"      = M "Devoid & Diablo Worlds" "Design for the Devoid and Diablo challenge worlds." @("design","worlds") "planned" "phase-2" "design" "needs-design"
  "design\worlds\galactic-journey.md"       = M "Galactic Journey" "Space travel and multiverse world design." @("design","worlds","space") "planned" "phase-3" "design" "needs-design"
  "design\worlds\lobby-and-creative.md"     = M "Lobby & Creative" "Lobby and creative server design notes." @("design","worlds","lobby") "reference" "ongoing" "design" "none"
  "design\worlds\seasons-and-hemisphere.md" = M "Seasons & Hemisphere" "Seasonal mechanics and hemisphere-based weather design." @("design","worlds","seasons") "implemented" "phase-1" "design" "none"
  "design\worlds\spawn-village.md"          = M "Spawn Village" "Spawn village layout and design." @("design","worlds","spawn") "reference" "ongoing" "design" "none"
  "design\worlds\storyline-progression.md"  = M "Storyline World Progression" "World-tier progression tied to story chapters." @("design","worlds","storyline") "planned" "phase-2" "design" "needs-design"
  "design\worlds\survival.md"               = M "Survival World Design" "Core survival world design notes." @("design","worlds","survival") "reference" "ongoing" "design" "none"
  "design\worlds\war-and-pvp.md"            = M "War & PvP Design" "War zone and PvP mechanics design." @("design","worlds","clans","pvp") "reference" "ongoing" "design" "none"
  # ── overview ────────────────────────────────────────────────────────────
  "overview\overview-hub.md"       = M "Overview Hub" "Top-level navigation hub for project overview documentation." @("overview","hub") "reference" "ongoing" "admin" "none"
  "overview\factions.md"           = M "Factions" "Good and Evil faction system overview and lore." @("overview","factions","lore") "reference" "ongoing" "design" "none"
  "overview\glossary.md"           = M "Glossary" "Definitions for project-specific terms and abbreviations." @("overview","reference") "reference" "ongoing" "admin" "none"
  "overview\goals.md"              = M "Goals" "High-level project goals and success criteria." @("overview") "reference" "ongoing" "admin" "none"
  "overview\storyline.md"          = M "Storyline Overview" "High-level story arc and chapter structure." @("overview","storyline","lore") "reference" "ongoing" "design" "none"
  "overview\vision-and-concept.md" = M "Vision & Concept" "Core vision, design philosophy, and project concept." @("overview","vision") "reference" "ongoing" "design" "none"
  # ── operations ──────────────────────────────────────────────────────────
  "operations\operations-hub.md"   = M "Operations Hub" "Navigation hub for server operations documentation." @("operations","hub") "reference" "ongoing" "ops" "none"
  "operations\backup.md"           = M "Backup Strategy" "Database and world backup procedures for disaster recovery." @("operations","backup") "reference" "ongoing" "ops" "none"
  "operations\database.md"         = M "Database Operations" "MySQL setup, tuning, and maintenance procedures." @("operations","database") "reference" "ongoing" "ops" "none"
  "operations\deployment.md"       = M "Deployment" "Plugin and server deployment procedures." @("operations","deployment") "reference" "ongoing" "ops" "none"
  "operations\firewall-linux.md"   = M "Firewall - Linux" "Linux UFW firewall configuration for server backends." @("operations","firewall","linux") "reference" "ongoing" "ops" "none"
  "operations\firewall-windows.md" = M "Firewall - Windows" "Windows firewall configuration for development machines." @("operations","firewall","windows") "reference" "ongoing" "ops" "none"
  "operations\monitoring.md"       = M "Monitoring" "Planned server monitoring and alerting setup." @("operations","monitoring") "planned" "phase-2" "ops" "needs-ops"
  "operations\network-security.md" = M "Network & Security" "Network topology and security hardening." @("operations","network","security") "reference" "ongoing" "ops" "none"
  "operations\npc-setup.md"        = M "NPC Setup" "Citizens NPC configuration and setup guide." @("operations","npc") "reference" "ongoing" "ops" "none"
  "operations\performance.md"      = M "Performance" "Paper performance tuning and capacity planning." @("operations","performance") "reference" "ongoing" "ops" "none"
  "operations\proxy.md"            = M "Proxy Setup" "BungeeCord/Velocity proxy configuration." @("operations","proxy") "reference" "ongoing" "ops" "none"
  "operations\runbooks.md"         = M "Runbooks" "Operational runbooks for common server tasks and incidents." @("operations","runbooks") "reference" "ongoing" "ops" "none"
  "operations\waypoints.md"        = M "Waypoints" "Server waypoint and teleport configuration." @("operations","waypoints") "reference" "ongoing" "ops" "none"
  # ── ops ─────────────────────────────────────────────────────────────────
  "ops\backup-setup.md"            = M "Backup Setup" "Step-by-step backup configuration and scheduling guide." @("operations","backup") "reference" "ongoing" "ops" "none"
  "ops\firewall-linux-ufw.md"      = M "Linux UFW Firewall Setup" "UFW firewall rules for Linux backend servers." @("operations","firewall","linux") "reference" "ongoing" "ops" "none"
  "ops\firewall-windows.md"        = M "Windows Firewall Setup" "Windows firewall rules for development and hosting." @("operations","firewall","windows") "reference" "ongoing" "ops" "none"
  "ops\monitoring-setup.md"        = M "Monitoring Setup" "Monitoring stack installation and configuration." @("operations","monitoring") "reference" "ongoing" "ops" "none"
  # ── planning ────────────────────────────────────────────────────────────
  "planning\planning-hub.md"                          = M "Planning Hub" "Navigation hub for planning, roadmap, and feature plan documents." @("planning","hub") "reference" "ongoing" "admin" "none"
  "planning\gap-analysis.md"                          = M "Gap Analysis" "V1 to V2 feature gap analysis and parity tracking." @("planning","roadmap") "reference" "ongoing" "admin" "none"
  "planning\goals-and-scope.md"                       = M "Goals & Scope" "Project goals, scope, and out-of-scope boundaries." @("planning","overview") "reference" "ongoing" "admin" "none"
  "planning\industry-arsenal-plan.md"                 = M "Industry Arsenal Plan" "Long-range speculative plan for industry, economy, and endgame systems." @("planning","speculative","endgame") "speculative" "post-story" "design" "needs-design"
  "planning\phase2-phase3-design-spec.md"             = M "Phase 2 & 3 Design Spec" "Design specification for Phase 2 and Phase 3 development work." @("planning","roadmap") "planned" "phase-2" "dev" "needs-dev"
  "planning\feature-plans\aeternum-features.md"       = M "AeternumSeasons Features" "AeternumSeasons seasonal weather, crops, and wildlife migration integration." @("planning","events","seasons") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\betonquest-milestones.md"   = M "BetonQuest Milestones" "BetonQuest integration plan for quest bridge and progression tags." @("planning","quests","progression") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\boss-progression.md"        = M "Boss Progression" "Boss encounter progression and arena system plan." @("planning","boss") "partial" "phase-2" "dev" "needs-dev"
  "planning\feature-plans\class-skill-tree.md"        = M "Class Skill Tree" "Five-class skill tree implementation plan and status." @("planning","classes") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\milestones-categories.md"   = M "Milestones & Categories" "Progression milestone categories and paged reward menu plan." @("planning","progression") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\personality.md"             = M "Personality Feature Plan" "13-trait personality system implementation plan." @("planning","personality") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\pet-module.md"              = M "Pet Module Plan" "Planned pet companion module implementation." @("planning","pets") "planned" "phase-3" "dev" "needs-dev"
  "planning\feature-plans\phase3-events.md"           = M "Phase 3 Events" "Phase 3 events roadmap including Nether events." @("planning","events") "partial" "phase-2" "dev" "needs-dev"
  "planning\feature-plans\skills-auraskills.md"       = M "AuraSkills Integration" "AuraSkills bridge for skill XP and level integration plan." @("planning","skills") "implemented" "phase-1" "dev" "none"
  "planning\feature-plans\world-spawn-flow.md"        = M "World Spawn Flow" "Player spawn and world entry flow design." @("planning","worlds","spawn") "planned" "phase-2" "design" "needs-design"
  "planning\feature-plans\zodiac-npc-reveal.md"       = M "Zodiac NPC Reveal" "Zodiac sign reveal via NPC interaction plan." @("planning","zodiac","npc") "planned" "phase-2" "dev" "needs-dev"
  "planning\feature-plans\storyline\milestones.md"    = M "Storyline Milestones" "Story chapter milestone triggers and world state changes." @("planning","storyline") "planned" "phase-2" "design" "needs-design"
  "planning\feature-plans\storyline\phase-1-earth.md" = M "Phase 1 Earth Storyline" "Detailed Earth chapter storyline and quest design." @("planning","storyline","quests") "planned" "phase-2" "design" "needs-design"
  # ── roadmap ─────────────────────────────────────────────────────────────
  "roadmap\roadmap-hub.md"           = M "Roadmap Hub" "Navigation hub for roadmap and implementation status documents." @("roadmap","hub") "reference" "ongoing" "admin" "none"
  "roadmap\feature-inventory.md"     = M "Feature Inventory" "Complete feature inventory with behaviour, commands, and configs." @("roadmap","reference") "reference" "ongoing" "admin" "none"
  "roadmap\feature-parity.md"        = M "Feature Parity" "V1 to V2 feature parity tracking." @("roadmap","reference") "reference" "ongoing" "admin" "none"
  "roadmap\implementation-status.md" = M "Implementation Status" "Living per-module implementation status for PluginV2." @("roadmap","reference") "reference" "ongoing" "admin" "none"
  "roadmap\phase-1.md"               = M "Phase 1" "Phase 1 implementation scope and completion record." @("roadmap","phase-1") "implemented" "phase-1" "dev" "none"
  "roadmap\phase-2.md"               = M "Phase 2" "Phase 2 planned scope and feature list." @("roadmap","phase-2") "planned" "phase-2" "dev" "needs-dev"
  "roadmap\phase-3.md"               = M "Phase 3" "Phase 3 long-range feature planning." @("roadmap","phase-3") "planned" "phase-3" "dev" "needs-dev"
  "roadmap\timeline.md"              = M "Timeline" "Project timeline and milestone schedule." @("roadmap","planning") "reference" "ongoing" "admin" "none"
  # ── development ─────────────────────────────────────────────────────────
  "development\development-hub.md"              = M "Development Hub" "Navigation hub for developer documentation and guides." @("development","hub") "reference" "ongoing" "dev" "none"
  "development\ai-collaboration.md"             = M "AI Collaboration Guide" "How to work with AI agents on this codebase." @("development","ai") "reference" "ongoing" "dev" "none"
  "development\centralized-config.md"           = M "Centralized Config" "Config architecture and centralized configuration patterns." @("development","config") "reference" "ongoing" "dev" "none"
  "development\console-commands.md"             = M "Console Commands" "Server console command reference for admins." @("development","commands") "reference" "ongoing" "dev" "none"
  "development\getting-started.md"              = M "Getting Started" "Developer onboarding and environment setup guide." @("development","onboarding") "reference" "ongoing" "dev" "none"
  "development\lobby-and-discord-bot-setup.md"  = M "Lobby & Discord Bot Setup" "Setup guide for the lobby server and optional Discord bot." @("development","discord","lobby") "reference" "ongoing" "dev" "none"
  "development\migration-from-v1.md"            = M "Migration from V1" "Guide for migrating code and data from V1 to V2." @("development","migration") "reference" "ongoing" "dev" "none"
  "development\style-guide.md"                  = M "Style Guide" "Code style, naming conventions, and formatting standards." @("development","style") "reference" "ongoing" "dev" "none"
  "development\wiki-publishing.md"              = M "Wiki Publishing" "How to publish and maintain the player-facing wiki." @("development","wiki") "reference" "ongoing" "dev" "none"
  # ── architecture ────────────────────────────────────────────────────────
  "architecture\architecture-hub.md" = M "Architecture Hub" "Navigation hub for architecture and technical decision documents." @("architecture","hub") "reference" "ongoing" "dev" "none"
  "architecture\data-model.md"       = M "Data Model" "Database schema, table definitions, and entity relationships." @("architecture","database") "reference" "ongoing" "dev" "none"
  "architecture\decisions.md"        = M "Architecture Decisions" "Key architectural decisions and their rationale (ADR log)." @("architecture","decisions") "reference" "ongoing" "dev" "none"
  "architecture\infrastructure.md"   = M "Infrastructure" "Server infrastructure, network, and hosting architecture." @("architecture","infrastructure") "reference" "ongoing" "dev" "none"
  "architecture\module-contracts.md" = M "Module Contracts" "RpgModule interface contracts and inter-module communication." @("architecture","modules") "reference" "ongoing" "dev" "none"
  "architecture\overview.md"         = M "Architecture Overview" "High-level V2 architecture overview." @("architecture","overview") "reference" "ongoing" "dev" "none"
  "architecture\v2-architecture.md"  = M "V2 Architecture" "Detailed V2 plugin architecture: modules, services, and data flow." @("architecture","v2") "reference" "ongoing" "dev" "none"
  # ── reference ───────────────────────────────────────────────────────────
  "reference\reference-hub.md"   = M "Reference Hub" "Navigation hub for reference and miscellaneous documentation." @("reference","hub") "reference" "ongoing" "admin" "none"
  "reference\bug-list.md"        = M "Bug List" "Known bugs and issues tracker." @("reference","bugs") "reference" "ongoing" "dev" "needs-review"
  "reference\config-reference.md"= M "Config Reference" "Full config key reference for all plugin modules." @("reference","config") "reference" "ongoing" "dev" "none"
  "reference\discord-bot.md"     = M "Discord Bot" "Planned Discord bot integration and command reference." @("reference","discord") "planned" "phase-2" "dev" "needs-dev"
  "reference\external-api.md"    = M "External API" "External API integrations and third-party service contracts." @("reference","api") "reference" "ongoing" "dev" "none"
  "reference\ideas.md"           = M "Ideas Tracker" "Trackable idea blocks with implementation status checkboxes." @("reference","ideas") "reference" "ongoing" "admin" "none"
  # ── testing ─────────────────────────────────────────────────────────────
  "testing\testing-hub.md"     = M "Testing Hub" "Navigation hub for test guides and testing documentation." @("testing","hub") "reference" "ongoing" "dev" "none"
  "testing\boss-arena-test.md" = M "Boss Arena Test Plan" "Test plan for boss encounters and arena mechanics." @("testing","boss") "partial" "phase-1" "dev" "needs-test"
  "testing\personality-test.md"= M "Personality System Test Guide" "Comprehensive test guide for traits, elements, holy enchants, and ultimate items." @("testing","personality") "implemented" "phase-1" "dev" "none"
  "testing\test-guide.md"      = M "Test Guide" "General testing guide and approach for PluginV2." @("testing") "reference" "ongoing" "dev" "none"
  "testing\zodiac-test.md"     = M "Zodiac Test Guide" "Full test suite for zodiac signs, spirit animals, and special mechanics." @("testing","zodiac") "implemented" "phase-1" "dev" "none"
  # ── player ──────────────────────────────────────────────────────────────
  "player\player-handbook.md"  = M "Player Handbook" "Comprehensive player guide to Lost Wilderness." @("player","handbook") "reference" "ongoing" "player-facing" "none"
  "player\amplified.md"        = M "Amplified Guide" "Player guide for the Amplified server." @("player","amplified") "reference" "ongoing" "player-facing" "none"
  "player\calendar.md"         = M "Calendar Guide" "Player guide to the in-game calendar and seasons." @("player","calendar") "reference" "ongoing" "player-facing" "none"
  "player\clans.md"            = M "Clans Guide" "Player guide to creating, joining, and managing clans." @("player","clans") "reference" "ongoing" "player-facing" "none"
  "player\commands.md"         = M "Commands Reference" "Complete player-facing command reference." @("player","commands") "reference" "ongoing" "player-facing" "none"
  "player\events.md"           = M "Events Guide" "Player guide to daily and seasonal events." @("player","events") "reference" "ongoing" "player-facing" "none"
  "player\footer.md"           = M "Footer" "Wiki footer template and navigation links." @("player","wiki") "reference" "ongoing" "player-facing" "none"
  "player\getting-started.md"  = M "Getting Started" "New player onboarding and server introduction." @("player","onboarding") "reference" "ongoing" "player-facing" "none"
  "player\portals.md"          = M "Portals Guide" "Player guide to portal travel between servers." @("player","portals") "reference" "ongoing" "player-facing" "none"
  "player\sidebar.md"          = M "Sidebar" "Wiki sidebar navigation template." @("player","wiki") "reference" "ongoing" "player-facing" "none"
  "player\survival.md"         = M "Survival Guide" "Player guide to the Survival server." @("player","survival") "reference" "ongoing" "player-facing" "none"
  # ── plugins ─────────────────────────────────────────────────────────────
  "plugins\plugins-hub.md"     = M "Plugins Hub" "Navigation hub for third-party plugin documentation." @("plugins","hub") "reference" "ongoing" "admin" "none"
  "plugins\anticheat.md"       = M "Anti-Cheat" "Planned anti-cheat plugin setup and configuration." @("plugins","anticheat") "planned" "phase-2" "ops" "needs-ops"
  "plugins\npc-quest-stack.md" = M "NPC & Quest Stack" "Citizens, BetonQuest, and NPC quest integration setup." @("plugins","npc","quests") "planned" "phase-2" "dev" "needs-dev"
  "plugins\resource-packs.md"  = M "Resource Packs" "Resource pack hosting, serving, and pack update process." @("plugins","resource-packs") "partial" "phase-2" "dev" "needs-dev"
  "plugins\third-party.md"     = M "Third-Party Plugins" "Reference list of all third-party plugins and their roles." @("plugins","reference") "reference" "ongoing" "admin" "none"
  # ── archive ─────────────────────────────────────────────────────────────
  "archive\archive-hub.md"                  = M "Archive Hub" "Navigation hub for archived and historical documents." @("archive","hub") "archive" "archive" "admin" "none"
  "archive\code-audit-2026-03-19.md"        = M "Code Audit - 2026-03-19" "Full code audit results from March 2026." @("archive","audit") "archive" "archive" "admin" "none"
  "archive\cursor-chat-export.md"           = M "Cursor Chat Export" "Exported Cursor AI chat history for reference." @("archive") "archive" "archive" "admin" "none"
  "archive\discord-updates.md"             = M "Discord Updates Archive" "Archived Discord update posts and announcements." @("archive","discord") "archive" "archive" "admin" "none"
  "archive\grovyle-vision-notes.md"        = M "Grovyle Vision Notes" "Early brainstorm and vision notes from Grovyle." @("archive","vision") "archive" "archive" "admin" "none"
  "archive\iris-volcano-guide.md"          = M "Iris Volcano Guide" "Iris world volcanic spawn region setup guide." @("archive","worlds") "archive" "archive" "admin" "none"
  "archive\party-phases.md"               = M "Party System Phases" "Phase-by-phase completion record for the Party module." @("archive","party") "archive" "archive" "admin" "none"
  "archive\personality-final-impl.md"     = M "Personality Final Implementation" "Final personality system implementation summary." @("archive","personality") "archive" "archive" "admin" "none"
  "archive\phase0-outcomes.md"            = M "Phase 0 Outcomes" "Phase 0 completion outcomes and lessons." @("archive","roadmap") "archive" "archive" "admin" "none"
  "archive\phase1-gap-report.md"          = M "Phase 1 Gap Report" "Phase 1 gap analysis and remaining items." @("archive","roadmap") "archive" "archive" "admin" "none"
  "archive\phase-1-implementation.md"     = M "Phase 1 Implementation Plan" "Phase 1 in-game setup and implementation notes." @("archive","roadmap") "archive" "archive" "admin" "none"
  "archive\phase-1-ingame-setup.md"       = M "Phase 1 In-Game Setup Guide" "Step-by-step in-game setup guide for Phase 1." @("archive","roadmap") "archive" "archive" "admin" "none"
  "archive\phase1-spec.md"               = M "Phase 1 Spec" "Original Phase 1 specification document." @("archive","roadmap") "archive" "archive" "admin" "none"
  "archive\template-implementation-plan.md" = M "Template: Implementation Plan" "Reusable template for writing implementation plans." @("archive","template") "archive" "archive" "admin" "none"
  "archive\v1-implementation-status.md"  = M "V1 Implementation Status" "Legacy V1 plugin implementation status." @("archive","v1") "archive" "archive" "admin" "none"
  "archive\v1-roadmap.md"               = M "V1 Roadmap" "Legacy V1 project roadmap." @("archive","v1") "archive" "archive" "admin" "none"
  # ── ideas ───────────────────────────────────────────────────────────────
  "ideas\brainstorming-hub.md"                  = M "Brainstorming Hub" "Navigation hub for all speculative and idea documents." @("ideas","hub") "reference" "ongoing" "admin" "none"
  "ideas\alignment-invisible-powers.md"         = M "Alignment Invisible Powers" "Neutral players cannot perceive alignment-based abilities, creating invisible combat tension." @("ideas","alignment","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\bedrock-compatibility-notes.md"        = M "Bedrock Compatibility Notes" "Living notes on Geyser/Floodgate behaviour and Java-first design decisions." @("ideas","bedrock","speculative") "reference" "ongoing" "dev" "needs-review"
  "ideas\clan-governance-system.md"             = M "Clan Governance System" "Player-defined clan government types, courts, and internal law enforcement." @("ideas","clans","governance","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\clan-hierarchy-tiers.md"               = M "Clan Hierarchy Tiers" "Numbered tier system (0-13+) with military branches and empire governance." @("ideas","clans","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\glowing-banners.md"                    = M "Glowing Banners" "Custom banner glow mechanics with Java per-layer effects and Bedrock fallbacks." @("ideas","banners","speculative") "speculative" "phase-3" "dev" "needs-dev"
  "ideas\gunsmithing-and-firearms.md"           = M "Gunsmithing & Firearms" "Six-tier weapon progression from flintlocks to alien rayguns with Gunsmith profession." @("ideas","weapons","gunsmithing","speculative") "speculative" "post-story" "dev" "needs-dev"
  "ideas\matter-engines-and-fuel-tiers.md"      = M "Matter Engines & Fuel Tiers" "Fuel tier progression from oil to dark/light matter combustion engines." @("ideas","technology","fuel","speculative") "speculative" "post-story" "dev" "needs-design"
  "ideas\npc-automation-system.md"              = M "NPC Automation System" "Recruitable NPCs for mundane tasks and AI-automated endgame infrastructure." @("ideas","npc","speculative") "speculative" "phase-3" "dev" "needs-dev"
  "ideas\road-infrastructure-and-law.md"        = M "Road Infrastructure & Law" "Bitumen roads, NPC traffic enforcement, martial law, and civil war mechanics." @("ideas","infrastructure","clans","speculative") "speculative" "post-story" "dev" "needs-design"
  "ideas\space-travel-and-multiverse-endgame.md"= M "Space Travel & Multiverse Endgame" "Post-story star jet travel, resource zones, and galactic empire alliance stacking." @("ideas","space","endgame","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\vehicle-configurations.md"             = M "Vehicle Configurations" "Vehicle types, truck modular bodies, trailers, 4WD, HUD sensors, and dual control." @("ideas","vehicles","speculative") "speculative" "post-story" "dev" "needs-dev"
  "ideas\vehicle-system-core.md"                = M "Vehicle System - Core" "Research-gated vehicle progression, engine upgrades, fuel research, and transmission modes." @("ideas","vehicles","speculative") "speculative" "post-story" "dev" "needs-dev"
  "ideas\world-seeds-chakra-progression.md"     = M "World Seeds & Chakra Progression" "Seven-chakra world tier progression with equator mechanics and survival pressures." @("ideas","worlds","speculative") "speculative" "phase-3" "design" "needs-design"
  # ── ideas / industry arsenal ────────────────────────────────────────────
  "ideas\industry-arsenal\industry-arsenal-hub.md"        = M "Industry Arsenal Hub" "Hub for the long-range industry, arsenal, and endgame planning documents." @("ideas","endgame","hub","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\demon-allies.md"                = M "Demon Allies" "Demon companion system and alignment-based ally mechanics." @("ideas","storyline","lore","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\geopolitics.md"                 = M "Geopolitics" "Server-wide geopolitical systems, territories, and power balance." @("ideas","clans","endgame","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\honor-and-alignment.md"         = M "Honor & Alignment" "Honor score system and Good/Evil alignment mechanics." @("ideas","alignment","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\industry-supply-chain.md"       = M "Industry Supply Chain" "Resource extraction, manufacturing, and supply chain mechanics." @("ideas","economy","industry","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\overview.md"                    = M "Industry Arsenal Overview" "Overview of the Industry Arsenal long-range feature cluster." @("ideas","endgame","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\pets-and-familiars.md"          = M "Pets & Familiars" "Pet companion system with familiar bonding and abilities." @("ideas","pets","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\professions-and-traits.md"      = M "Professions & Traits" "Profession system with trait specialisations and bonuses." @("ideas","professions","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\professors-and-side-quests.md"  = M "Professors & Side Quests" "Professor NPCs delivering optional side quests and lore." @("ideas","npc","quests","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\second-coming.md"               = M "The Second Coming" "End-game second coming event and its world consequences." @("ideas","storyline","endgame","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\storyline-chapters-industry.md" = M "Storyline Chapters - Industry" "Story chapters covering the rise of industry and arsenal." @("ideas","storyline","speculative") "speculative" "post-story" "design" "needs-design"
  "ideas\industry-arsenal\storyline-progression.md"       = M "Storyline Progression - Industry" "Progression gates tied to industrial milestones." @("ideas","storyline","progression","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\vanilla-mechanics-synergy.md"   = M "Vanilla Mechanics Synergy" "How vanilla Minecraft mechanics integrate with custom systems." @("ideas","vanilla","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\war-and-military.md"            = M "War & Military" "Military branch specialisations, war mechanics, and combat systems." @("ideas","clans","pvp","speculative") "speculative" "phase-3" "design" "needs-design"
  "ideas\industry-arsenal\zeratari-planets.md"            = M "Zeratari Planets" "Zeratari alien worlds, lore, and resource exploration." @("ideas","space","lore","speculative") "speculative" "post-story" "design" "needs-design"
  # ── v2 subfolder ────────────────────────────────────────────────────────
  "v2\discord-updates\README.md" = M "Discord Updates" "Archived Discord update history and feature announcements." @("archive","discord") "archive" "archive" "admin" "none"
  # ── root ────────────────────────────────────────────────────────────────
  "documentation-hub.md" = M "Documentation Hub" "Root navigation hub for all Lost Wilderness documentation." @("hub","overview") "reference" "ongoing" "admin" "none"
  "graph.md"             = M "Graph Navigation" "Graph view node map and cross-section link registry." @("hub","reference") "reference" "ongoing" "admin" "none"
  "index.md"             = M "Index" "Documentation index and quick-reference links." @("hub","reference") "reference" "ongoing" "admin" "none"
  "link-mapping.md"      = M "Link Mapping" "Internal link registry for renamed and moved files." @("reference","links") "reference" "ongoing" "dev" "none"
  "README.md"            = M "Docs README" "Documentation root readme and structure guide." @("overview","reference") "reference" "ongoing" "admin" "none"
}

# ── Helper: build frontmatter block ─────────────────────────────────────────
function Build-Frontmatter($m) {
  $tagsYaml = ($m.tags | ForEach-Object { "  - $_" }) -join "`r`n"
  return "---`r`ntitle: $($m.title)`r`ndescription: $($m.desc)`r`ntags:`r`n$tagsYaml`r`nstatus: $($m.status)`r`nphase: $($m.phase)`r`nowner: $($m.owner)`r`naction: $($m.action)`r`n---`r`n"
}

# ── Strip injected scriptblock junk ─────────────────────────────────────────
function Strip-Corruption($content) {
  # The bad script replaced ---\n[yaml]\n---\n blocks with the scriptblock string.
  # Pattern: optional leading blank line + the two junk lines + trailing whitespace line
  $junk = "(?m)^\s*param\(\\\$m\)\s*$\s*^\s*\\\$m\.Groups\[1\]\.Value.*$\s*^\s*$"
  # Simpler: just find lines matching "param($m)" and "$m.Groups" and the extra whitespace line
  $lines = $content -split "`r`n|`n"
  $clean = [System.Collections.Generic.List[string]]::new()
  $skipNext = $false
  for ($i = 0; $i -lt $lines.Count; $i++) {
    $ln = $lines[$i]
    if ($ln -match '^\s*param\(\$m\)\s*$') {
      # Skip this line and the next two (the $m.Groups line and the trailing '  ')
      $i += 2
      continue
    }
    $clean.Add($ln)
  }
  return $clean -join "`r`n"
}

# ── Main loop ────────────────────────────────────────────────────────────────
$updated = 0
$notFound = 0

foreach ($rel in $map.Keys) {
  $path = $base + $rel
  if (-not (Test-Path $path)) {
    Write-Warning "NOT FOUND: $rel"
    $notFound++
    continue
  }

  $content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

  # 1. Strip the injected junk lines
  $lines = $content -split "`r`n|`n"
  $clean = [System.Collections.Generic.List[string]]::new()
  $i = 0
  while ($i -lt $lines.Count) {
    if ($lines[$i] -match '^\s*param\(\$m\)\s*$') {
      $i += 3  # skip: param($m) line, $m.Groups line, trailing '  ' line
      continue
    }
    $clean.Add($lines[$i])
    $i++
  }

  # 2. Trim leading blank lines
  while ($clean.Count -gt 0 -and $clean[0].Trim() -eq '') {
    $clean.RemoveAt(0)
  }

  # 3. Strip any leftover malformed frontmatter (opens with --- but no proper yaml)
  # If file now starts with '---' followed immediately by '#' heading, strip those opening dashes
  if ($clean.Count -gt 0 -and $clean[0] -eq '---') {
    # Check if there's a matching closing ---
    $closeIdx = -1
    for ($j = 1; $j -lt [Math]::Min(30, $clean.Count); $j++) {
      if ($clean[$j] -eq '---') { $closeIdx = $j; break }
    }
    if ($closeIdx -gt 0) {
      # Remove from 0 to closeIdx inclusive
      for ($j = 0; $j -le $closeIdx; $j++) { $clean.RemoveAt(0) }
      # Trim leading blanks again
      while ($clean.Count -gt 0 -and $clean[0].Trim() -eq '') { $clean.RemoveAt(0) }
    }
  }

  # 4. Build proper frontmatter and prepend
  $frontmatter = Build-Frontmatter $map[$rel]
  $body = $clean -join "`r`n"
  $newContent = $frontmatter + $body

  [System.IO.File]::WriteAllText($path, $newContent, [System.Text.Encoding]::UTF8)
  $updated++
}

Write-Host ""
Write-Host "=== DONE ==="
Write-Host "Updated  : $updated"
Write-Host "Not found: $notFound"
