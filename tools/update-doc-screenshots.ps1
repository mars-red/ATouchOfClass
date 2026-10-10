#Requires -Version 7.0
param([switch]$Check)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$records = @(Get-Content -LiteralPath (Join-Path $root 'docs/contributing/screenshots.json') -Raw | ConvertFrom-Json)
$ids = @{}
$files = @{}
$pages = @{}
$pending = [Collections.Generic.List[string]]::new()
$checklist = [Collections.Generic.List[string]]::new()
$checklist.Add('# Manual screenshot checklist')
$checklist.Add('')
$checklist.Add('[Capture standard and review workflow](README.md) · [Manual](../README.md)')
$checklist.Add('')
$checklist.Add('Generated from screenshots.json. Tasks are grouped by shared setup. The five tall-door pilot images are already supplied. File presence does not imply visual approval. Use F2 for game-only PNG captures.')
$checklist.Add('')

function Resolve-DocPath([string]$relative) {
    $full = [IO.Path]::GetFullPath((Join-Path $root $relative))
    $docsRoot = [IO.Path]::GetFullPath((Join-Path $root 'docs')) + [IO.Path]::DirectorySeparatorChar
    if (-not $full.StartsWith($docsRoot, [StringComparison]::OrdinalIgnoreCase)) { throw "Path must stay under docs: $relative" }
    return $full
}

$lastSession = ''
foreach ($r in $records) {
    foreach ($field in @('id','title','page','file','setup','capture','mustShow','avoid','alt','caption')) {
        if ([string]::IsNullOrWhiteSpace($r.$field)) { throw "Missing $field in screenshot record" }
    }
    if ($r.id -notmatch '^[a-z0-9]+(-[a-z0-9]+)+$' -or $ids.ContainsKey($r.id)) { throw "Invalid or duplicate screenshot ID: $($r.id)" }
    if ($files.ContainsKey($r.file)) { throw "Duplicate image path: $($r.file)" }
    if ($r.file -notmatch '^docs/images/[a-z0-9/-]+\.png$') { throw "Invalid image path: $($r.file)" }
    $ids[$r.id] = $true
    $files[$r.file] = $true
    $page = Resolve-DocPath $r.page
    $picture = Resolve-DocPath $r.file
    if (-not $pages.ContainsKey($page)) { $pages[$page] = (Get-Content -LiteralPath $page -Raw).Replace("`r`n", "`n") }
    $start = "<!-- screenshot:$($r.id):start -->"
    $end = "<!-- screenshot:$($r.id):end -->"
    $pattern = '(?s)' + [regex]::Escape($start) + '.*?' + [regex]::Escape($end)
    if ([regex]::Matches($pages[$page], $pattern).Count -ne 1) { throw "Expected one marker pair for $($r.id)" }
    $present = Test-Path -LiteralPath $picture -PathType Leaf
    $details = @(
        "**Purpose:** $($r.title)",
        "**Setup:** $($r.setup)",
        "**Capture:** $($r.capture)",
        "**Must show:** $($r.mustShow)",
        "**Avoid:** $($r.avoid)",
        "**Save as:** ``$($r.file)``",
        "**Alt text:** $($r.alt)",
        "**Caption:** $($r.caption)"
    )
    if ($present) {
        $relative = [IO.Path]::GetRelativePath([IO.Path]::GetDirectoryName($page), $picture).Replace('\','/')
        $body = "![$($r.alt)]($relative)`n`n*$($r.caption)*"
        $status = 'Image supplied; see the contributor guide for visual review status'
    } else {
        $body = "> **Screenshot needed — $($r.id): $($r.title)**`n>" + "`n> " + ($details -join "`n>`n> ")
        $status = 'Awaiting capture'
        $pending.Add($r.id)
    }
    $replacement = "$start`n$body`n$end"
    $pages[$page] = [regex]::Replace($pages[$page], $pattern, [Text.RegularExpressions.MatchEvaluator]{ param($m) $replacement })
    $session = if ($r.session) { $r.session } else { 'Ungrouped captures' }
    if ($session -ne $lastSession) {
        $checklist.Add("## $session")
        $checklist.Add('')
        $lastSession = $session
    }
    $checklist.Add("### $($r.id) — $($r.title)")
    $checklist.Add('')
    $checklist.Add("**Status:** $status")
    $checklist.Add('')
    foreach ($detail in $details) { $checklist.Add($detail); $checklist.Add('') }
}
$pages[(Join-Path $root 'docs/contributing/screenshots.md')] = ($checklist -join "`n").TrimEnd() + "`n"
$changed = 0
foreach ($path in $pages.Keys) {
    $desired = $pages[$path]
    $current = if (Test-Path -LiteralPath $path) { (Get-Content -LiteralPath $path -Raw).Replace("`r`n", "`n") } else { '' }
    if ($current -ne $desired) {
        $changed++
        if (-not $Check) { [IO.File]::WriteAllText($path, $desired, [Text.UTF8Encoding]::new($false)) }
    }
}
if ($Check -and $changed) { throw "$changed generated documentation file(s) need updating. Run without -Check." }
Write-Output "$($records.Count) screenshot records validated; $($pending.Count) awaiting capture; $changed file(s) $(if ($Check) {'need updates'} else {'updated'})."
