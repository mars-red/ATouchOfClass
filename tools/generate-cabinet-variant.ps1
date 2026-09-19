<#
.SYNOPSIS
Generates door and drawer cabinets from the finalized dark oak family.

.EXAMPLE
.\tools\generate-cabinet-variant.ps1 birch

.EXAMPLE
.\tools\generate-cabinet-variant.ps1 custom_wood `
    -SlabId othermod:custom_wood_slab `
    -TextureId othermod:block/stripped_custom_wood_log

The wood name determines the generated IDs, such as birch_door_cabinet and
birch_drawer_cabinet. By default the ingredient is minecraft:<wood>_slab and
the texture is minecraft:block/stripped_<wood>_log. Bamboo, crimson, and
warped automatically use their vanilla stripped block/stem texture names.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $WoodType,

    [string] $SlabId,

    [string] $TextureId
)

$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$resourceRoot = Join-Path $projectRoot 'src\main\resources'
$wood = $WoodType.Trim().ToLowerInvariant().Replace(' ', '_')
if ($wood.EndsWith('_slab')) {
    $wood = $wood.Substring(0, $wood.Length - 5)
}
if ($wood -notmatch '^[a-z0-9]+(?:_[a-z0-9]+)*$') {
    throw 'Wood type must be a valid snake_case Minecraft resource name.'
}
if ([string]::IsNullOrWhiteSpace($SlabId)) {
    $SlabId = "minecraft:${wood}_slab"
}
if ([string]::IsNullOrWhiteSpace($TextureId)) {
    $TextureId = switch ($wood) {
        'bamboo' { 'minecraft:block/stripped_bamboo_block' }
        'crimson' { 'minecraft:block/stripped_crimson_stem' }
        'warped' { 'minecraft:block/stripped_warped_stem' }
        default { "minecraft:block/stripped_${wood}_log" }
    }
}
$resourcePattern = '^[a-z0-9_.-]+:[a-z0-9_./-]+$'
if ($SlabId -notmatch $resourcePattern) { throw "Invalid slab resource ID: $SlabId" }
if ($TextureId -notmatch $resourcePattern) { throw "Invalid texture resource ID: $TextureId" }

$doorName = "${wood}_door_cabinet"
$drawerName = "${wood}_drawer_cabinet"
$doorConstant = $doorName.ToUpperInvariant()
$drawerConstant = $drawerName.ToUpperInvariant()
$displayWood = (($wood -split '_') | ForEach-Object {
    $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1)
}) -join ' '

function Get-Newline([string] $Content) {
    if ($Content.Contains("`r`n")) { return "`r`n" }
    return "`n"
}

function Insert-BeforeMarker([string] $Content, [string] $Marker, [string] $Insertion, [string] $Path) {
    $markerIndex = $Content.IndexOf($Marker, [StringComparison]::Ordinal)
    if ($markerIndex -lt 0) { throw "Generator marker missing from ${Path}: $Marker" }
    if ($Content.IndexOf($Marker, $markerIndex + $Marker.Length, [StringComparison]::Ordinal) -ge 0) {
        throw "Generator marker is not unique in ${Path}: $Marker"
    }
    return $Content.Insert($markerIndex, $Insertion)
}

function Read-Template([string] $RelativePath, [System.Collections.IDictionary] $Replacements) {
    $path = Join-Path $resourceRoot $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Template file not found: $path" }
    $content = [IO.File]::ReadAllText($path)
    foreach ($replacement in $Replacements.GetEnumerator()) {
        $content = $content.Replace([string] $replacement.Key, [string] $replacement.Value)
    }
    return $content
}

$plannedFiles = [ordered]@{}
function Plan-NewFile([string] $RelativePath, [string] $Content) {
    $path = Join-Path $resourceRoot $RelativePath
    if (Test-Path -LiteralPath $path) { throw "Refusing to overwrite existing file: $path" }
    $script:plannedFiles[$path] = $Content
}

$doorReplacements = [ordered]@{
    'dark_oak_door_cabinet' = $doorName
    'minecraft:block/stripped_dark_oak_log' = $TextureId
}
$drawerReplacements = [ordered]@{
    'dark_oak_drawer_cabinet' = $drawerName
    'minecraft:block/stripped_dark_oak_log' = $TextureId
}

$doorTemplates = @(
    'assets\atouchofclass\blockstates\dark_oak_door_cabinet.json',
    'assets\atouchofclass\items\dark_oak_door_cabinet.json',
    'assets\atouchofclass\models\block\dark_oak_door_cabinet.json',
    'assets\atouchofclass\models\block\dark_oak_door_cabinet_joined_above.json',
    'assets\atouchofclass\models\block\dark_oak_door_cabinet_joined_below.json',
    'assets\atouchofclass\models\block\dark_oak_door_cabinet_joined_vertical.json',
    'assets\atouchofclass\models\item\dark_oak_door_cabinet.json',
    'data\atouchofclass\loot_table\blocks\dark_oak_door_cabinet.json'
)
foreach ($template in $doorTemplates) {
    $target = $template.Replace('dark_oak_door_cabinet', $doorName)
    Plan-NewFile $target (Read-Template $template $doorReplacements)
}

$drawerTemplates = @(
    'assets\atouchofclass\blockstates\dark_oak_drawer_cabinet.json',
    'assets\atouchofclass\items\dark_oak_drawer_cabinet.json',
    'assets\atouchofclass\models\block\dark_oak_drawer_cabinet.json',
    'assets\atouchofclass\models\item\dark_oak_drawer_cabinet.json',
    'data\atouchofclass\loot_table\blocks\dark_oak_drawer_cabinet.json'
)
foreach ($template in $drawerTemplates) {
    $target = $template.Replace('dark_oak_drawer_cabinet', $drawerName)
    Plan-NewFile $target (Read-Template $template $drawerReplacements)
}

$doorRecipeReplacements = [ordered]@{
    'atouchofclass:dark_oak_door_cabinet' = "atouchofclass:$doorName"
    'minecraft:dark_oak_slab' = $SlabId
}
$drawerRecipeReplacements = [ordered]@{
    'atouchofclass:dark_oak_drawer_cabinet' = "atouchofclass:$drawerName"
    'minecraft:dark_oak_slab' = $SlabId
}
Plan-NewFile "data\atouchofclass\recipe\$doorName.json" `
    (Read-Template 'data\atouchofclass\recipe\dark_oak_door_cabinet.json' $doorRecipeReplacements)
Plan-NewFile "data\atouchofclass\recipe\$drawerName.json" `
    (Read-Template 'data\atouchofclass\recipe\dark_oak_drawer_cabinet.json' $drawerRecipeReplacements)

$doorAdvancementReplacements = [ordered]@{
    'atouchofclass:dark_oak_door_cabinet' = "atouchofclass:$doorName"
    'minecraft:dark_oak_slab' = $SlabId
    'has_dark_oak_slab' = 'has_material_slab'
}
$drawerAdvancementReplacements = [ordered]@{
    'atouchofclass:dark_oak_drawer_cabinet' = "atouchofclass:$drawerName"
    'minecraft:dark_oak_slab' = $SlabId
    'has_dark_oak_slab' = 'has_material_slab'
}
Plan-NewFile "data\atouchofclass\advancement\recipes\building_blocks\$doorName.json" `
    (Read-Template 'data\atouchofclass\advancement\recipes\building_blocks\dark_oak_door_cabinet.json' $doorAdvancementReplacements)
Plan-NewFile "data\atouchofclass\advancement\recipes\building_blocks\$drawerName.json" `
    (Read-Template 'data\atouchofclass\advancement\recipes\building_blocks\dark_oak_drawer_cabinet.json' $drawerAdvancementReplacements)

$modBlocksPath = Join-Path $projectRoot 'src\main\java\com\digitaldownpour\atouchofclass\ModBlocks.java'
$modBlocks = [IO.File]::ReadAllText($modBlocksPath)
if ($modBlocks.Contains('"' + $doorName + '"') -or $modBlocks.Contains('"' + $drawerName + '"')) {
    throw "The '$wood' cabinet family is already registered."
}
$javaNewline = Get-Newline $modBlocks
$blockRegistration = @"
    public static final DeferredBlock<DoorCabinetBlock> $doorConstant =
            registerDoorCabinet("$doorName");

    public static final DeferredBlock<CabinetBlock> $drawerConstant =
            registerCabinet("$drawerName");

"@ -replace "`r?`n", $javaNewline
$modBlocks = Insert-BeforeMarker $modBlocks '    // CABINET_VARIANT_REGISTRATIONS' $blockRegistration $modBlocksPath
$plannedFiles[$modBlocksPath] = $modBlocks

$modItemsPath = Join-Path $projectRoot 'src\main\java\com\digitaldownpour\atouchofclass\ModItems.java'
$modItems = [IO.File]::ReadAllText($modItemsPath)
$itemNewline = Get-Newline $modItems
$itemRegistration = @"
    public static final DeferredItem<BlockItem> $doorConstant =
            ITEMS.registerSimpleBlockItem("$doorName", ModBlocks.$doorConstant);
    public static final DeferredItem<BlockItem> $drawerConstant =
            ITEMS.registerSimpleBlockItem("$drawerName", ModBlocks.$drawerConstant);

"@ -replace "`r?`n", $itemNewline
$modItems = Insert-BeforeMarker $modItems '    // FURNITURE_VARIANT_ITEM_REGISTRATIONS' $itemRegistration $modItemsPath
$creativeEntries = "            event.accept($doorConstant);${itemNewline}            event.accept($drawerConstant);${itemNewline}"
$modItems = Insert-BeforeMarker $modItems '            // FURNITURE_VARIANT_CREATIVE_ENTRIES' $creativeEntries $modItemsPath
$plannedFiles[$modItemsPath] = $modItems

$languagePath = Join-Path $resourceRoot 'assets\atouchofclass\lang\en_us.json'
$language = Get-Content -LiteralPath $languagePath -Raw | ConvertFrom-Json
$languageEntries = [ordered]@{
    "block.atouchofclass.$doorName" = "$displayWood Door Cabinet"
    "item.atouchofclass.$doorName" = "$displayWood Door Cabinet"
    "block.atouchofclass.$drawerName" = "$displayWood Drawer Cabinet"
    "item.atouchofclass.$drawerName" = "$displayWood Drawer Cabinet"
}
foreach ($entry in $languageEntries.GetEnumerator()) {
    if ($null -ne $language.PSObject.Properties[$entry.Key]) {
        throw "Language entry already exists: $($entry.Key)"
    }
    $language | Add-Member -NotePropertyName $entry.Key -NotePropertyValue $entry.Value
}
$plannedFiles[$languagePath] = ($language | ConvertTo-Json -Depth 20) + [Environment]::NewLine

$axeTagPath = Join-Path $resourceRoot 'data\minecraft\tags\block\mineable\axe.json'
$axeTag = Get-Content -LiteralPath $axeTagPath -Raw | ConvertFrom-Json
$axeValues = @($axeTag.values)
foreach ($blockId in @("atouchofclass:$doorName", "atouchofclass:$drawerName")) {
    if ($axeValues -notcontains $blockId) { $axeValues += $blockId }
}
$axeTag.values = $axeValues
$plannedFiles[$axeTagPath] = ($axeTag | ConvertTo-Json -Depth 20) + [Environment]::NewLine

foreach ($entry in $plannedFiles.GetEnumerator()) {
    [IO.Directory]::CreateDirectory((Split-Path $entry.Key -Parent)) | Out-Null
    [IO.File]::WriteAllText($entry.Key, [string] $entry.Value)
}

Write-Output "Generated '$doorName' and '$drawerName'."
Write-Output "Ingredient: $SlabId"
Write-Output "Texture: $TextureId"
Write-Output 'Run .\gradlew.bat build and runGameTestServer to validate the new family.'
