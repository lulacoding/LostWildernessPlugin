
# Quotes any frontmatter field whose unquoted value contains a colon,
# which would cause YAML parse errors.
# Covers: title, description (the two fields most likely to have colons).
# Run: powershell -ExecutionPolicy Bypass -File tasks\fix-yaml-colons.ps1

$docsPath = "c:\Users\cthvh\OneDrive\Desktop\Lost Wilderness\Docs"
$fixed = 0

foreach ($file in (Get-ChildItem $docsPath -Recurse -Filter "*.md")) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
    $newContent = $content

    # Quote title: and description: values that contain a colon and aren't already quoted
    foreach ($field in @("title", "description")) {
        $pattern     = "(?m)^($field`: )(?![`"'])(.+?:.+?)\r?$"
        $replacement = '$1"$2"'
        $newContent  = [regex]::Replace($newContent, $pattern, $replacement)
    }

    if ($newContent -ne $content) {
        [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
        Write-Host "Fixed: $($file.FullName.Replace($docsPath + '\', ''))"
        $fixed++
    }
}

Write-Host ""
Write-Host "Total files fixed: $fixed"
