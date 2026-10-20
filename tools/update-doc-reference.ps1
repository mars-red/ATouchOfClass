#Requires -Version 7.0
param([switch]$Check)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$java = Join-Path $root 'src/main/java/com/digitaldownpour/atouchofclass'
$resources = Join-Path $root 'src/main/resources'
$registered = [Collections.Generic.HashSet[string]]::new()
function Add-Id([string]$id) { [void]$registered.Add("atouchofclass:$id") }
function Strings([string]$text) { @([regex]::Matches($text, '"([a-z0-9_]+)"') | ForEach-Object { $_.Groups[1].Value }) }
function Named-List([string]$file, [string]$name) {
    $source = Get-Content -LiteralPath (Join-Path $java $file) -Raw
    $match = [regex]::Match($source, '(?s)\b' + $name + '\s*=\s*(?:java\.util\.)?List\.of\((.*?)\);')
    if (-not $match.Success) { throw "Cannot parse $name in $file" }
    Strings $match.Groups[1].Value
}
$items = Get-Content (Join-Path $java 'ModItems.java') -Raw
foreach ($m in [regex]::Matches($items, '(?:ITEMS\.register\w*|registerWallpaper|registerWallFinish)\(\s*"([a-z0-9_]+)"')) { Add-Id $m.Groups[1].Value }
foreach ($id in (Named-List 'ModBlocks.java' 'BASIC_BLOCK_NAMES')) { Add-Id $id }
foreach ($wood in (Named-List 'DresserVariants.java' 'WOODS')) { Add-Id ($wood + '_dresser') }
foreach ($family in @('SideTable','DiningTable','DiningChair','Nightstand','Shutter','PatioDoor')) {
    $source = Get-Content (Join-Path $java ($family + 'Variants.java')) -Raw
    $suffix = switch ($family) { 'SideTable' { '_side_table' } 'DiningTable' { '_dining_table' } 'DiningChair' { '_dining_chair' } 'Nightstand' { '_nightstand' } 'Shutter' { '_shutter' } }
    foreach ($m in [regex]::Matches($source, 'new Definition\("([a-z0-9_]+)"')) {
        if ($family -eq 'PatioDoor') { Add-Id ('tall_' + $m.Groups[1].Value + '_patio_door') }
        else { Add-Id ($m.Groups[1].Value + $suffix) }
    }
}
$woods = Named-List 'FourPostBedVariants.java' 'WOODS'
$colors = Named-List 'FourPostBedVariants.java' 'COLORS'
foreach ($wood in $woods) { foreach ($color in $colors) { Add-Id ($wood + '_' + $color + '_four_post_bed') } }
$railSource = Get-Content (Join-Path $java 'WoodRailingVariants.java') -Raw
$railMatch = [regex]::Match($railSource, 'new String\[\]\{([^}]+)\}')
if (-not $railMatch.Success) { throw 'Cannot parse railing variants' }
foreach ($wood in (Strings $railMatch.Groups[1].Value)) {
    foreach ($suffix in @('_railing','_baluster','_balustrade','_newel_post')) { Add-Id ($wood + $suffix) }
}
$labels = Get-Content (Join-Path $resources 'assets/atouchofclass/lang/en_us.json') -Raw | ConvertFrom-Json -AsHashtable
function Label([string]$id) {
    $key = $id.Replace(':','.')
    foreach ($prefix in @('item.','block.')) { if ($labels.ContainsKey($prefix + $key)) { return $labels[$prefix + $key] } }
    return $id
}
$oxidation = (Get-Content (Join-Path $resources 'data/neoforge/data_maps/block/oxidizables.json') -Raw | ConvertFrom-Json -AsHashtable).values
$waxing = (Get-Content (Join-Path $resources 'data/neoforge/data_maps/block/waxables.json') -Raw | ConvertFrom-Json -AsHashtable).values
$fromOxidation = @{}
foreach ($id in $oxidation.Keys) { $fromOxidation[$oxidation[$id].next_oxidation_stage] = $id }
$recipes = @()
$byOutput = @{}
foreach ($file in (Get-ChildItem (Join-Path $resources 'data/atouchofclass/recipe') -Filter '*.json' | Sort-Object Name)) {
    $r = Get-Content $file.FullName -Raw | ConvertFrom-Json -AsHashtable
    $output = if ($r.result -is [string]) { $r.result } else { $r.result.id }
    if (-not $output) { throw "Unsupported recipe result: $($file.Name)" }
    if ($output.StartsWith('atouchofclass:') -and -not $registered.Contains($output)) { throw "Recipe output is not covered by registration parser: $output" }
    $record = @{ Name=$file.BaseName; Data=$r; Output=$output }
    $recipes += $record
    if (-not $byOutput.ContainsKey($output)) { $byOutput[$output] = @() }
    $byOutput[$output] += $record
}
function Ingredient($value) {
    if ($value -is [string]) { return $value }
    if ($value -is [System.Collections.IDictionary]) {
        if ($value.item) { return $value.item }
        if ($value.tag) { return '#' + $value.tag }
    }
    if ($value -is [array]) { return ($value | ForEach-Object { Ingredient $_ }) -join ' OR ' }
    throw 'Unsupported ingredient format'
}
$catalog = [Collections.Generic.List[string]]::new()
@('# Content catalog','','[Manual](README.md) · [Recipes](recipes.md) · [Reference](reference.md)','','Generated from explicit item registrations and the basic-block, dresser, side-table, dining-table, dining-chair, nightstand, shutter, patio-door, four-post-bed, and wood-railing registration families. The internal tab icon is excluded. Run `tools/update-doc-reference.ps1` after changing these families; a new registration mechanism requires updating that helper. This is a source-derived catalog, not a runtime registry dump.','','Acquisition describes the bundled mod. Recipes using tags may accept different materials in a modpack. Copper aging is an in-world transformation, not a crafting recipe.','') | ForEach-Object { $catalog.Add($_) }
$catalog.Add("**$($registered.Count) registered player-content items.**")
$catalog.Add('')
$catalog.Add('| Item | Identifier | Acquisition |')
$catalog.Add('| --- | --- | --- |')
$unresolved = @()
foreach ($id in ($registered | Sort-Object)) {
    if ($byOutput.ContainsKey($id)) {
        $links = @($byOutput[$id] | ForEach-Object { '[' + $_.Name + '](recipes.md#' + $_.Name.Replace('_','-') + ')' })
        $route = 'Survival obtainable — ' + ($links -join ', ')
    } elseif ($fromOxidation.ContainsKey($id)) { $route = 'Survival obtainable — oxidation of `' + $fromOxidation[$id] + '`' }
    else { $route = 'Acquisition under review'; $unresolved += $id }
    $catalog.Add('| ' + (Label $id) + ' | `' + $id + '` | ' + $route + ' |')
}
$recipeDoc = [Collections.Generic.List[string]]::new()
@('# Bundled recipe reference','','[Manual](README.md) · [Catalog](catalog.md)','','Generated from every recipe under the mod namespace. Names below are recipe IDs without the `atouchofclass:` prefix. Grids show rows from top to bottom; `.` is an empty slot. `#` prefixes an ingredient tag. Cooking times are ticks (20 ticks per second at normal speed). Shaped patterns can be positioned within a crafting grid as the game permits; shapeless ingredients may be arranged freely.','','Use the catalog links or search this page for the item ID. Recipes listed here are the bundled defaults; data packs can override them.','') | ForEach-Object { $recipeDoc.Add($_) }
foreach ($record in $recipes) {
    $r = $record.Data
    $count = if ($r.result -is [System.Collections.IDictionary] -and $r.result.ContainsKey('count')) { $r.result.count } else { 1 }
    $recipeDoc.Add('## ' + $record.Name.Replace('_','-'))
    $recipeDoc.Add('')
    $recipeDoc.Add('**Output:** ' + $count + ' × ' + (Label $record.Output) + ' (`' + $record.Output + '`). **Type:** `' + $r.type + '`.')
    $recipeDoc.Add('')
    switch ($r.type) {
        'minecraft:crafting_shaped' {
            $recipeDoc.Add('```text')
            foreach ($row in $r.pattern) { $recipeDoc.Add($row.Replace(' ','.')) }
            $recipeDoc.Add('```')
            $recipeDoc.Add('')
            $keys = @($r.key.Keys | Sort-Object | ForEach-Object { '`' + $_ + '` = `' + (Ingredient $r.key[$_]) + '`' })
            $recipeDoc.Add($keys -join '; ')
        }
        'minecraft:crafting_shapeless' {
            $ingredients = @($r.ingredients | ForEach-Object { '`' + (Ingredient $_) + '`' })
            $recipeDoc.Add('**Ingredients (each occurrence is one item):** ' + ($ingredients -join ', '))
        }
        { $_ -in @('minecraft:smelting','minecraft:blasting','minecraft:smoking','minecraft:campfire_cooking','minecraft:stonecutting') } {
            $recipeDoc.Add('**Input:** `' + (Ingredient $r.ingredient) + '`.')
            if ($r.ContainsKey('cookingtime')) { $recipeDoc.Add('**Cooking:** ' + $r.cookingtime + ' ticks; experience: ' + $r.experience + '.') }
        }
        default { throw "Recipe renderer does not support $($r.type)" }
    }
    $recipeDoc.Add('')
    $recipeDoc.Add('[Recipe JSON](../src/main/resources/data/atouchofclass/recipe/' + $record.Name + '.json)')
    $recipeDoc.Add('')
}
$changed = 0
foreach ($entry in @(@{Path='docs/catalog.md';Lines=$catalog}, @{Path='docs/recipes.md';Lines=$recipeDoc})) {
    $path = Join-Path $root $entry.Path
    $desired = ($entry.Lines -join "`n").TrimEnd() + "`n"
    $current = if (Test-Path $path) { (Get-Content $path -Raw).Replace("`r`n","`n") } else { '' }
    if ($current -ne $desired) { $changed++; if (-not $Check) { [IO.File]::WriteAllText($path,$desired,[Text.UTF8Encoding]::new($false)) } }
}
if ($Check -and $changed) { throw "$changed reference file(s) are stale; run without -Check." }
Write-Output "$($registered.Count) registered items; $($recipes.Count) recipes; $($unresolved.Count) unresolved acquisition routes; $changed generated file(s) changed."
if ($unresolved.Count) { Write-Output ($unresolved -join "`n") }
