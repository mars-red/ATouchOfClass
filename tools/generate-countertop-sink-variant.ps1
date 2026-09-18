<#
.SYNOPSIS
Generates a countertop and sink family from the finalized calcite family.

.EXAMPLE
.\tools\generate-countertop-sink-variant.ps1 deepslate

.EXAMPLE
.\tools\generate-countertop-sink-variant.ps1 custom_stone `
    -SlabId othermod:custom_stone_slab `
    -TextureId othermod:block/custom_stone

The material name determines the generated IDs, such as
deepslate_countertop and deepslate_sink. By default the ingredient is
minecraft:<material>_slab and the texture is minecraft:block/<material>.
Use the override parameters when a slab or texture has a different ID.
The shorthand "deepslate" uses vanilla cobbled deepslate slab and texture,
because Minecraft does not provide an undecorated deepslate slab.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $SlabType,

    [string] $SlabId,

    [string] $TextureId
)

$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$resourceRoot = Join-Path $projectRoot 'src\main\resources'
$material = $SlabType.Trim().ToLowerInvariant().Replace(' ', '_')
if ($material.EndsWith('_slab')) {
    $material = $material.Substring(0, $material.Length - 5)
}
if ($material -notmatch '^[a-z0-9]+(?:_[a-z0-9]+)*$') {
    throw 'Slab type must be a valid snake_case Minecraft resource name.'
}
if ([string]::IsNullOrWhiteSpace($SlabId)) {
    $SlabId = if ($material -eq 'deepslate') {
        'minecraft:cobbled_deepslate_slab'
    } else {
        "minecraft:${material}_slab"
    }
}
if ([string]::IsNullOrWhiteSpace($TextureId)) {
    $TextureId = if ($material -eq 'deepslate') {
        'minecraft:block/cobbled_deepslate'
    } else {
        "minecraft:block/$material"
    }
}
$resourcePattern = '^[a-z0-9_.-]+:[a-z0-9_./-]+$'
if ($SlabId -notmatch $resourcePattern) { throw "Invalid slab resource ID: $SlabId" }
if ($TextureId -notmatch $resourcePattern) { throw "Invalid texture resource ID: $TextureId" }

$countertopName = "${material}_countertop"
$sinkName = "${material}_sink"
$countertopConstant = $countertopName.ToUpperInvariant()
$sinkConstant = $sinkName.ToUpperInvariant()
$displayMaterial = (($material -split '_') | ForEach-Object {
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

$countertopReplacements = [ordered]@{
    'calcite_countertop' = $countertopName
    'minecraft:block/calcite' = $TextureId
}
$sinkReplacements = [ordered]@{
    'calcite_sink' = $sinkName
    'minecraft:block/calcite' = $TextureId
}

$countertopTemplates = @(
    'assets\atouchofclass\blockstates\calcite_countertop.json',
    'assets\atouchofclass\items\calcite_countertop.json',
    'assets\atouchofclass\models\block\calcite_countertop.json',
    'assets\atouchofclass\models\item\calcite_countertop.json',
    'data\atouchofclass\loot_table\blocks\calcite_countertop.json'
)
foreach ($template in $countertopTemplates) {
    $target = $template.Replace('calcite_countertop', $countertopName)
    Plan-NewFile $target (Read-Template $template $countertopReplacements)
}

$sinkTemplates = @(
    'assets\atouchofclass\blockstates\calcite_sink.json',
    'assets\atouchofclass\items\calcite_sink.json',
    'assets\atouchofclass\models\block\calcite_sink.json',
    'assets\atouchofclass\models\item\calcite_sink.json',
    'data\atouchofclass\loot_table\blocks\calcite_sink.json'
)
foreach ($template in $sinkTemplates) {
    $target = $template.Replace('calcite_sink', $sinkName)
    Plan-NewFile $target (Read-Template $template $sinkReplacements)
}

$countertopRecipeReplacements = [ordered]@{
    'atouchofclass:calcite_countertop' = "atouchofclass:$countertopName"
    'atouchofclass:calcite_slab' = $SlabId
}
$sinkRecipeReplacements = [ordered]@{
    'atouchofclass:calcite_sink' = "atouchofclass:$sinkName"
    'atouchofclass:calcite_slab' = $SlabId
}
Plan-NewFile "data\atouchofclass\recipe\$countertopName.json" `
    (Read-Template 'data\atouchofclass\recipe\calcite_countertop.json' $countertopRecipeReplacements)
Plan-NewFile "data\atouchofclass\recipe\$sinkName.json" `
    (Read-Template 'data\atouchofclass\recipe\calcite_sink.json' $sinkRecipeReplacements)

$countertopAdvancementReplacements = [ordered]@{
    'atouchofclass:calcite_countertop' = "atouchofclass:$countertopName"
    'atouchofclass:calcite_slab' = $SlabId
    'has_calcite_slab' = 'has_material_slab'
}
$sinkAdvancementReplacements = [ordered]@{
    'atouchofclass:calcite_sink' = "atouchofclass:$sinkName"
    'atouchofclass:calcite_slab' = $SlabId
    'has_calcite_slab' = 'has_material_slab'
}
Plan-NewFile "data\atouchofclass\advancement\recipes\building_blocks\$countertopName.json" `
    (Read-Template 'data\atouchofclass\advancement\recipes\building_blocks\calcite_countertop.json' $countertopAdvancementReplacements)
Plan-NewFile "data\atouchofclass\advancement\recipes\building_blocks\$sinkName.json" `
    (Read-Template 'data\atouchofclass\advancement\recipes\building_blocks\calcite_sink.json' $sinkAdvancementReplacements)

$modBlocksPath = Join-Path $projectRoot 'src\main\java\com\digitaldownpour\atouchofclass\ModBlocks.java'
$modBlocks = [IO.File]::ReadAllText($modBlocksPath)
if ($modBlocks.Contains('"' + $countertopName + '"') -or $modBlocks.Contains('"' + $sinkName + '"')) {
    throw "The '$material' countertop/sink family is already registered."
}
$javaNewline = Get-Newline $modBlocks
$blockRegistration = @"
    public static final DeferredBlock<CountertopBlock> $countertopConstant =
            BLOCKS.register("$countertopName", registryName -> new CountertopBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

    public static final DeferredBlock<SinkBlock> $sinkConstant =
            BLOCKS.register("$sinkName", registryName -> new SinkBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
                            .mapColor(MapColor.STONE)
                            .strength(1.5F, 6.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()));

"@ -replace "`r?`n", $javaNewline
$modBlocks = Insert-BeforeMarker $modBlocks '    // COUNTERTOP_SINK_VARIANT_REGISTRATIONS' $blockRegistration $modBlocksPath
$plannedFiles[$modBlocksPath] = $modBlocks

$modItemsPath = Join-Path $projectRoot 'src\main\java\com\digitaldownpour\atouchofclass\ModItems.java'
$modItems = [IO.File]::ReadAllText($modItemsPath)
$itemNewline = Get-Newline $modItems
$itemRegistration = @"
    public static final DeferredItem<BlockItem> $countertopConstant =
            ITEMS.registerSimpleBlockItem("$countertopName", ModBlocks.$countertopConstant);
    public static final DeferredItem<BlockItem> $sinkConstant =
            ITEMS.registerSimpleBlockItem("$sinkName", ModBlocks.$sinkConstant);

"@ -replace "`r?`n", $itemNewline
$modItems = Insert-BeforeMarker $modItems '    // FURNITURE_VARIANT_ITEM_REGISTRATIONS' $itemRegistration $modItemsPath
$creativeEntries = "            event.accept($countertopConstant);${itemNewline}            event.accept($sinkConstant);${itemNewline}"
$modItems = Insert-BeforeMarker $modItems '            // FURNITURE_VARIANT_CREATIVE_ENTRIES' $creativeEntries $modItemsPath
$plannedFiles[$modItemsPath] = $modItems

$languagePath = Join-Path $resourceRoot 'assets\atouchofclass\lang\en_us.json'
$language = Get-Content -LiteralPath $languagePath -Raw | ConvertFrom-Json
$languageEntries = [ordered]@{
    "block.atouchofclass.$countertopName" = "$displayMaterial Countertop"
    "item.atouchofclass.$countertopName" = "$displayMaterial Countertop"
    "block.atouchofclass.$sinkName" = "$displayMaterial Sink"
    "item.atouchofclass.$sinkName" = "$displayMaterial Sink"
}
foreach ($entry in $languageEntries.GetEnumerator()) {
    if ($null -ne $language.PSObject.Properties[$entry.Key]) {
        throw "Language entry already exists: $($entry.Key)"
    }
    $language | Add-Member -NotePropertyName $entry.Key -NotePropertyValue $entry.Value
}
$plannedFiles[$languagePath] = ($language | ConvertTo-Json -Depth 20) + [Environment]::NewLine

$pickaxeTagPath = Join-Path $resourceRoot 'data\minecraft\tags\block\mineable\pickaxe.json'
$pickaxeTag = Get-Content -LiteralPath $pickaxeTagPath -Raw | ConvertFrom-Json
$pickaxeValues = @($pickaxeTag.values)
foreach ($blockId in @("atouchofclass:$countertopName", "atouchofclass:$sinkName")) {
    if ($pickaxeValues -notcontains $blockId) { $pickaxeValues += $blockId }
}
$pickaxeTag.values = $pickaxeValues
$plannedFiles[$pickaxeTagPath] = ($pickaxeTag | ConvertTo-Json -Depth 20) + [Environment]::NewLine

foreach ($entry in $plannedFiles.GetEnumerator()) {
    [IO.Directory]::CreateDirectory((Split-Path $entry.Key -Parent)) | Out-Null
    [IO.File]::WriteAllText($entry.Key, [string] $entry.Value)
}

Write-Output "Generated '$countertopName' and '$sinkName'."
Write-Output "Ingredient: $SlabId"
Write-Output "Texture: $TextureId"
Write-Output 'Run .\gradlew.bat build and runGameTestServer to validate the new family.'
