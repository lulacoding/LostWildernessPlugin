
# Applies status/phase/owner/action fields to every file's frontmatter.
# Run from repo root: powershell -File tasks\apply-log-fields.ps1

$base = "c:\Users\cthvh\OneDrive\Desktop\Lost Wilderness\Docs\"

# Map: relative path from Docs\ => [status, phase, owner, action]
$map = @{
  # ── systems ──────────────────────────────────────────────────────────────────
  "systems\calendar.md"              = "implemented","phase-1","dev","none"
  "systems\clans.md"                 = "implemented","phase-1","dev","none"
  "systems\classes.md"               = "implemented","phase-1","dev","none"
  "systems\core.md"                  = "implemented","ongoing","dev","none"
  "systems\economy.md"               = "planned","phase-2","dev","needs-dev"
  "systems\events.md"                = "partial","phase-1","dev","needs-dev"
  "systems\party.md"                 = "implemented","phase-1","dev","none"
  "systems\personality-mechanics.md" = "implemented","phase-1","dev","none"
  "systems\personality-overview.md"  = "implemented","phase-1","dev","none"
  "systems\portals.md"               = "implemented","phase-1","dev","none"
  "systems\progression.md"           = "implemented","phase-1","dev","none"
  "systems\quests.md"                = "planned","phase-2","dev","needs-dev"
  "systems\skills.md"                = "implemented","phase-1","dev","none"
  "systems\zodiac.md"                = "implemented","phase-1","dev","none"
  "systems\systems-hub.md"           = "reference","ongoing","admin","none"
  # ── design ───────────────────────────────────────────────────────────────────
  "design\design-hub.md"                     = "reference","ongoing","admin","none"
  "design\audio-visual.md"                   = "planned","phase-2","design","needs-design"
  "design\open-questions.md"                 = "reference","ongoing","admin","needs-review"
  "design\worlds\design-worlds-hub.md"       = "reference","ongoing","admin","none"
  "design\worlds\amplified.md"               = "reference","ongoing","design","none"
  "design\worlds\devoid-and-diablo.md"       = "planned","phase-2","design","needs-design"
  "design\worlds\galactic-journey.md"        = "planned","phase-3","design","needs-design"
  "design\worlds\lobby-and-creative.md"      = "reference","ongoing","design","none"
  "design\worlds\seasons-and-hemisphere.md"  = "implemented","phase-1","design","none"
  "design\worlds\spawn-village.md"           = "reference","ongoing","design","none"
  "design\worlds\storyline-progression.md"   = "planned","phase-2","design","needs-design"
  "design\worlds\survival.md"                = "reference","ongoing","design","none"
  "design\worlds\war-and-pvp.md"             = "reference","ongoing","design","none"
  # ── overview ─────────────────────────────────────────────────────────────────
  "overview\overview-hub.md"      = "reference","ongoing","admin","none"
  "overview\factions.md"          = "reference","ongoing","design","none"
  "overview\glossary.md"          = "reference","ongoing","admin","none"
  "overview\goals.md"             = "reference","ongoing","admin","none"
  "overview\storyline.md"         = "reference","ongoing","design","none"
  "overview\vision-and-concept.md"= "reference","ongoing","design","none"
  # ── operations ───────────────────────────────────────────────────────────────
  "operations\operations-hub.md"  = "reference","ongoing","ops","none"
  "operations\backup.md"          = "reference","ongoing","ops","none"
  "operations\database.md"        = "reference","ongoing","ops","none"
  "operations\deployment.md"      = "reference","ongoing","ops","none"
  "operations\firewall-linux.md"  = "reference","ongoing","ops","none"
  "operations\firewall-windows.md"= "reference","ongoing","ops","none"
  "operations\monitoring.md"      = "planned","phase-2","ops","needs-ops"
  "operations\network-security.md"= "reference","ongoing","ops","none"
  "operations\npc-setup.md"       = "reference","ongoing","ops","none"
  "operations\performance.md"     = "reference","ongoing","ops","none"
  "operations\proxy.md"           = "reference","ongoing","ops","none"
  "operations\runbooks.md"        = "reference","ongoing","ops","none"
  "operations\waypoints.md"       = "reference","ongoing","ops","none"
  # ── ops (legacy) ─────────────────────────────────────────────────────────────
  "ops\backup-setup.md"           = "reference","ongoing","ops","none"
  "ops\firewall-linux-ufw.md"     = "reference","ongoing","ops","none"
  "ops\firewall-windows.md"       = "reference","ongoing","ops","none"
  "ops\monitoring-setup.md"       = "reference","ongoing","ops","none"
  # ── planning ─────────────────────────────────────────────────────────────────
  "planning\planning-hub.md"                         = "reference","ongoing","admin","none"
  "planning\gap-analysis.md"                         = "reference","ongoing","admin","none"
  "planning\goals-and-scope.md"                      = "reference","ongoing","admin","none"
  "planning\industry-arsenal-plan.md"                = "speculative","post-story","design","needs-design"
  "planning\phase2-phase3-design-spec.md"            = "planned","phase-2","dev","needs-dev"
  "planning\feature-plans\aeternum-features.md"      = "implemented","phase-1","dev","none"
  "planning\feature-plans\betonquest-milestones.md"  = "implemented","phase-1","dev","none"
  "planning\feature-plans\boss-progression.md"       = "partial","phase-2","dev","needs-dev"
  "planning\feature-plans\class-skill-tree.md"       = "implemented","phase-1","dev","none"
  "planning\feature-plans\milestones-categories.md"  = "implemented","phase-1","dev","none"
  "planning\feature-plans\personality.md"            = "implemented","phase-1","dev","none"
  "planning\feature-plans\pet-module.md"             = "planned","phase-3","dev","needs-dev"
  "planning\feature-plans\phase3-events.md"          = "partial","phase-2","dev","needs-dev"
  "planning\feature-plans\skills-auraskills.md"      = "implemented","phase-1","dev","none"
  "planning\feature-plans\world-spawn-flow.md"       = "planned","phase-2","design","needs-design"
  "planning\feature-plans\zodiac-npc-reveal.md"      = "planned","phase-2","dev","needs-dev"
  "planning\feature-plans\storyline\milestones.md"   = "planned","phase-2","design","needs-design"
  "planning\feature-plans\storyline\phase-1-earth.md"= "planned","phase-2","design","needs-design"
  # ── roadmap ───────────────────────────────────────────────────────────────────
  "roadmap\roadmap-hub.md"          = "reference","ongoing","admin","none"
  "roadmap\feature-inventory.md"    = "reference","ongoing","admin","none"
  "roadmap\feature-parity.md"       = "reference","ongoing","admin","none"
  "roadmap\implementation-status.md"= "reference","ongoing","admin","none"
  "roadmap\phase-1.md"              = "implemented","phase-1","dev","none"
  "roadmap\phase-2.md"              = "planned","phase-2","dev","needs-dev"
  "roadmap\phase-3.md"              = "planned","phase-3","dev","needs-dev"
  "roadmap\timeline.md"             = "reference","ongoing","admin","none"
  # ── development ──────────────────────────────────────────────────────────────
  "development\development-hub.md"         = "reference","ongoing","dev","none"
  "development\ai-collaboration.md"        = "reference","ongoing","dev","none"
  "development\centralized-config.md"      = "reference","ongoing","dev","none"
  "development\console-commands.md"        = "reference","ongoing","dev","none"
  "development\getting-started.md"         = "reference","ongoing","dev","none"
  "development\lobby-and-discord-bot-setup.md" = "reference","ongoing","dev","none"
  "development\migration-from-v1.md"       = "reference","ongoing","dev","none"
  "development\style-guide.md"             = "reference","ongoing","dev","none"
  "development\wiki-publishing.md"         = "reference","ongoing","dev","none"
  # ── architecture ─────────────────────────────────────────────────────────────
  "architecture\architecture-hub.md" = "reference","ongoing","dev","none"
  "architecture\data-model.md"       = "reference","ongoing","dev","none"
  "architecture\decisions.md"        = "reference","ongoing","dev","none"
  "architecture\infrastructure.md"   = "reference","ongoing","dev","none"
  "architecture\module-contracts.md" = "reference","ongoing","dev","none"
  "architecture\overview.md"         = "reference","ongoing","dev","none"
  "architecture\v2-architecture.md"  = "reference","ongoing","dev","none"
  # ── reference ────────────────────────────────────────────────────────────────
  "reference\reference-hub.md"  = "reference","ongoing","admin","none"
  "reference\bug-list.md"       = "reference","ongoing","dev","needs-review"
  "reference\config-reference.md" = "reference","ongoing","dev","none"
  "reference\discord-bot.md"    = "planned","phase-2","dev","needs-dev"
  "reference\external-api.md"   = "reference","ongoing","dev","none"
  "reference\ideas.md"          = "reference","ongoing","admin","none"
  # ── testing ──────────────────────────────────────────────────────────────────
  "testing\testing-hub.md"        = "reference","ongoing","dev","none"
  "testing\boss-arena-test.md"    = "partial","phase-1","dev","needs-test"
  "testing\personality-test.md"   = "implemented","phase-1","dev","none"
  "testing\test-guide.md"         = "reference","ongoing","dev","none"
  "testing\zodiac-test.md"        = "implemented","phase-1","dev","none"
  # ── player ───────────────────────────────────────────────────────────────────
  "player\player-handbook.md" = "reference","ongoing","player-facing","none"
  "player\amplified.md"       = "reference","ongoing","player-facing","none"
  "player\calendar.md"        = "reference","ongoing","player-facing","none"
  "player\clans.md"           = "reference","ongoing","player-facing","none"
  "player\commands.md"        = "reference","ongoing","player-facing","none"
  "player\events.md"          = "reference","ongoing","player-facing","none"
  "player\footer.md"          = "reference","ongoing","player-facing","none"
  "player\getting-started.md" = "reference","ongoing","player-facing","none"
  "player\portals.md"         = "reference","ongoing","player-facing","none"
  "player\sidebar.md"         = "reference","ongoing","player-facing","none"
  "player\survival.md"        = "reference","ongoing","player-facing","none"
  # ── plugins ──────────────────────────────────────────────────────────────────
  "plugins\plugins-hub.md"    = "reference","ongoing","admin","none"
  "plugins\anticheat.md"      = "planned","phase-2","ops","needs-ops"
  "plugins\npc-quest-stack.md"= "planned","phase-2","dev","needs-dev"
  "plugins\resource-packs.md" = "partial","phase-2","dev","needs-dev"
  "plugins\third-party.md"    = "reference","ongoing","admin","none"
  # ── archive ──────────────────────────────────────────────────────────────────
  "archive\archive-hub.md"               = "archive","archive","admin","none"
  "archive\code-audit-2026-03-19.md"     = "archive","archive","admin","none"
  "archive\cursor-chat-export.md"        = "archive","archive","admin","none"
  "archive\discord-updates.md"           = "archive","archive","admin","none"
  "archive\grovyle-vision-notes.md"      = "archive","archive","admin","none"
  "archive\iris-volcano-guide.md"        = "archive","archive","admin","none"
  "archive\party-phases.md"              = "archive","archive","admin","none"
  "archive\personality-final-impl.md"    = "archive","archive","admin","none"
  "archive\phase0-outcomes.md"           = "archive","archive","admin","none"
  "archive\phase1-gap-report.md"         = "archive","archive","admin","none"
  "archive\phase-1-implementation.md"    = "archive","archive","admin","none"
  "archive\phase-1-ingame-setup.md"      = "archive","archive","admin","none"
  "archive\phase1-spec.md"               = "archive","archive","admin","none"
  "archive\template-implementation-plan.md" = "archive","archive","admin","none"
  "archive\v1-implementation-status.md"  = "archive","archive","admin","none"
  "archive\v1-roadmap.md"                = "archive","archive","admin","none"
  # ── ideas — new files (all speculative) ─────────────────────────────────────
  "ideas\brainstorming-hub.md"               = "reference","ongoing","admin","none"
  "ideas\alignment-invisible-powers.md"      = "speculative","post-story","design","needs-design"
  "ideas\bedrock-compatibility-notes.md"     = "reference","ongoing","dev","needs-review"
  "ideas\clan-governance-system.md"          = "speculative","phase-3","design","needs-design"
  "ideas\clan-hierarchy-tiers.md"            = "speculative","phase-3","design","needs-design"
  "ideas\glowing-banners.md"                 = "speculative","phase-3","dev","needs-dev"
  "ideas\gunsmithing-and-firearms.md"        = "speculative","post-story","dev","needs-dev"
  "ideas\matter-engines-and-fuel-tiers.md"   = "speculative","post-story","dev","needs-design"
  "ideas\npc-automation-system.md"           = "speculative","phase-3","dev","needs-dev"
  "ideas\road-infrastructure-and-law.md"     = "speculative","post-story","dev","needs-design"
  "ideas\space-travel-and-multiverse-endgame.md" = "speculative","post-story","design","needs-design"
  "ideas\vehicle-configurations.md"          = "speculative","post-story","dev","needs-dev"
  "ideas\vehicle-system-core.md"             = "speculative","post-story","dev","needs-dev"
  "ideas\world-seeds-chakra-progression.md"  = "speculative","phase-3","design","needs-design"
  # ── ideas — industry arsenal (all speculative) ───────────────────────────────
  "ideas\industry-arsenal\industry-arsenal-hub.md"       = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\demon-allies.md"               = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\geopolitics.md"                = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\honor-and-alignment.md"        = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\industry-supply-chain.md"      = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\overview.md"                   = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\pets-and-familiars.md"         = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\professions-and-traits.md"     = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\professors-and-side-quests.md" = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\second-coming.md"              = "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\storyline-chapters-industry.md"= "speculative","post-story","design","needs-design"
  "ideas\industry-arsenal\storyline-progression.md"      = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\vanilla-mechanics-synergy.md"  = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\war-and-military.md"           = "speculative","phase-3","design","needs-design"
  "ideas\industry-arsenal\zeratari-planets.md"           = "speculative","post-story","design","needs-design"
  # ── root ─────────────────────────────────────────────────────────────────────
  "documentation-hub.md" = "reference","ongoing","admin","none"
  "graph.md"             = "reference","ongoing","admin","none"
  "index.md"             = "reference","ongoing","admin","none"
  "link-mapping.md"      = "reference","ongoing","dev","none"
  "README.md"            = "reference","ongoing","admin","none"
  # ── v2 sub-folder ────────────────────────────────────────────────────────────
  "v2\discord-updates\README.md" = "archive","archive","admin","none"
}

$updated = 0
$skipped = 0
$errors  = 0

foreach ($rel in $map.Keys) {
  $path = $base + $rel
  if (-not (Test-Path $path)) {
    Write-Warning "NOT FOUND: $rel"
    $errors++
    continue
  }

  $vals   = $map[$rel]
  $status = $vals[0]; $phase = $vals[1]; $owner = $vals[2]; $action = $vals[3]

  $content = Get-Content $path -Raw -Encoding UTF8

  # Skip if already has these fields
  if ($content -match '(?m)^status:' -and $content -match '(?m)^phase:' -and
      $content -match '(?m)^owner:' -and $content -match '(?m)^action:') {
    $skipped++
    continue
  }

  # Find the closing --- of the frontmatter block
  # The frontmatter is: starts with ---, ends with the SECOND ---
  if ($content -notmatch '(?ms)^(---\r?\n.*?\r?\n)(---\r?\n)') {
    Write-Warning "No valid frontmatter: $rel"
    $errors++
    continue
  }

  $insertion = "status: $status`r`nphase: $phase`r`nowner: $owner`r`naction: $action`r`n"

  # Insert the 4 fields immediately before the closing ---
  $newContent = $content -replace '(?ms)(^---\r?\n)(.*?)(^---\r?\n)', {
    param($m)
    $m.Groups[1].Value + $m.Groups[2].Value + $insertion + $m.Groups[3].Value
  }

  if ($newContent -eq $content) {
    Write-Warning "Replace had no effect: $rel"
    $errors++
    continue
  }

  [System.IO.File]::WriteAllText($path, $newContent, [System.Text.Encoding]::UTF8)
  $updated++
}

Write-Host ""
Write-Host "=== DONE ==="
Write-Host "Updated : $updated"
Write-Host "Skipped : $skipped (already had all fields)"
Write-Host "Errors  : $errors"
