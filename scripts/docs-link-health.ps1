<#.SYNOPSIS
  Inspect Docs/**/*.md link health: orphan count (no incoming standard Markdown links)
  and optional duplicate basenames (same filename in different folders).
#>
param(
  [switch]$DuplicateBasenames,
  [switch]$ListOrphans
)

$ErrorActionPreference = "Stop"
# scripts/ lives at repo root → parent is the workspace root
$repoRoot = Split-Path $PSScriptRoot -Parent
$docsRoot = Join-Path $repoRoot "Docs"
if (-not (Test-Path $docsRoot)) {
  Write-Error "Docs not found at $docsRoot"
}

$files = @(Get-ChildItem -Path $docsRoot -Recurse -Filter "*.md" | ForEach-Object { $_.FullName })
$pathMap = @{}
foreach ($f in $files) { $pathMap[$f.ToLowerInvariant()] = $f }

function Resolve-MdLink {
  param([string]$FromFile, [string]$Link)
  if ($Link -match '^\s*https?:') { return $null }
  $Link = ($Link -split '#')[0].Trim()
  if ([string]::IsNullOrWhiteSpace($Link)) { return $null }
  $base = Split-Path $FromFile -Parent
  $combined = Join-Path $base $Link
  try {
    $resolved = [System.IO.Path]::GetFullPath($combined)
  } catch {
    return $null
  }
  if ($resolved -like "*.md" -and (Test-Path -LiteralPath $resolved)) {
    return $resolved
  }
  return $null
}

$incoming = @{}
foreach ($f in $files) { $incoming[$f] = 0 }

$linkRe = [regex]'\[[^\]]*\]\(([^)]+)\)'
foreach ($f in $files) {
  $text = [System.IO.File]::ReadAllText($f)
  foreach ($m in $linkRe.Matches($text)) {
    $target = Resolve-MdLink -FromFile $f -Link $m.Groups[1].Value.Trim()
    if ($target -and $pathMap.ContainsKey($target.ToLowerInvariant())) {
      $canonical = $pathMap[$target.ToLowerInvariant()]
      $incoming[$canonical]++
    }
  }
}

$orphans = @($files | Where-Object { $incoming[$_] -eq 0 })

Write-Host "Docs root: $docsRoot"
Write-Host "Markdown files: $($files.Count)"
Write-Host "Orphans (zero incoming [](.md) links from other Docs/*.md): $($orphans.Count)"
if ($ListOrphans -and $orphans.Count -gt 0) {
  $orphans | ForEach-Object { Write-Host "  - $($_.Substring($docsRoot.Length + 1))" }
}

if ($DuplicateBasenames) {
  $byBase = $files | Group-Object { [System.IO.Path]::GetFileName($_) } | Where-Object { $_.Count -gt 1 } | Sort-Object Name
  Write-Host ""
  Write-Host "Duplicate basenames ($($byBase.Count) names shared by multiple paths):"
  foreach ($g in $byBase) {
    Write-Host "  $($g.Name) ($($g.Count) files)"
    foreach ($p in $g.Group | Sort-Object) {
      Write-Host "    - $($p.Substring($docsRoot.Length + 1))"
    }
  }
}

if ($orphans.Count -ne 0 -and -not $ListOrphans) {
  Write-Host "Tip: re-run with -ListOrphans to print paths."
}

exit $(if ($orphans.Count -eq 0) { 0 } else { 1 })
