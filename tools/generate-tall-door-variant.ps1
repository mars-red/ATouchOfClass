[CmdletBinding()]
param([Parameter(Mandatory, Position = 0)][string] $WoodName)

$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$resources = Join-Path $root 'src\main\resources'
$wood = $WoodName.Trim().ToLowerInvariant().Replace(' ', '_')
if ($wood -notmatch '^[a-z0-9]+(?:_[a-z0-9]+)*$') { throw 'Wood name must be a valid Minecraft resource name.' }
$validWoods = @('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','pale_oak','bamboo','crimson','warped')
if ($wood -notin $validWoods) { throw "Unknown Minecraft wood '$wood'. Expected one of: $($validWoods -join ', ')" }
$id = "tall_${wood}_door"
$constant = $id.ToUpperInvariant()
$displayWood = (($wood -split '_') | ForEach-Object { $_.Substring(0,1).ToUpperInvariant() + $_.Substring(1) }) -join ' '

function Require-File([string] $path) { if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Required texture not found: $path" } }
function Add-BeforeMarker([string] $path, [string] $marker, [string] $text) {
    $content = [IO.File]::ReadAllText($path)
    if (-not $content.Contains($marker)) { throw "Generator marker missing from ${path}: $marker" }
    [IO.File]::WriteAllText($path, $content.Replace($marker, $text + $marker))
}
function Copy-Template([string] $source, [string] $target) {
    if (Test-Path -LiteralPath $target) { throw "Refusing to overwrite existing file: $target" }
    $content = [IO.File]::ReadAllText($source).Replace('tall_mangrove_door', $id).Replace('minecraft:mangrove_planks', "minecraft:${wood}_planks")
    [IO.Directory]::CreateDirectory((Split-Path $target -Parent)) | Out-Null
    [IO.File]::WriteAllText($target, $content)
}

$textureRoot = Join-Path $resources 'assets\atouchofclass\textures'
@('bottom','middle','top') | ForEach-Object { Require-File (Join-Path $textureRoot "block\${id}_$_.png") }
Require-File (Join-Path $textureRoot "item\$id.png")
$modBlocksPath = Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\ModBlocks.java'
if ([IO.File]::ReadAllText($modBlocksPath).Contains($id)) { throw "Tall door variant '$id' is already registered." }

$templates = @(
    @('assets\atouchofclass\blockstates\tall_mangrove_door.json', "assets\atouchofclass\blockstates\$id.json"),
    @('assets\atouchofclass\items\tall_mangrove_door.json', "assets\atouchofclass\items\$id.json"),
    @('assets\atouchofclass\models\item\tall_mangrove_door.json', "assets\atouchofclass\models\item\$id.json"),
    @('data\atouchofclass\loot_table\blocks\tall_mangrove_door.json', "data\atouchofclass\loot_table\blocks\$id.json"),
    @('data\atouchofclass\recipe\tall_mangrove_door.json', "data\atouchofclass\recipe\$id.json")
)
@('bottom','middle','top') | ForEach-Object { $templates += ,@("assets\atouchofclass\models\block\tall_mangrove_door_$_.json", "assets\atouchofclass\models\block\${id}_$_.json") }
foreach ($pair in $templates) { $target = Join-Path $resources $pair[1]; if (Test-Path -LiteralPath $target) { throw "Refusing to overwrite existing file: $target" } }
foreach ($pair in $templates) { Copy-Template (Join-Path $resources $pair[0]) (Join-Path $resources $pair[1]) }

$nl = [Environment]::NewLine
Add-BeforeMarker (Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\ModBlocks.java') '// TALL_DOOR_VARIANT_REGISTRATIONS' "public static final DeferredHolder<Block, TallDoorBlock> $constant =${nl}            registerTallDoor(`"$id`", MapColor.WOOD);${nl}${nl}    "
Add-BeforeMarker (Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\ModItems.java') '// TALL_DOOR_VARIANT_ITEM_REGISTRATIONS' "public static final DeferredItem<TallDoorItem> ${constant}_ITEM = ITEMS.register(${nl}            `"$id`",${nl}            key -> new TallDoorItem(ModBlocks.$constant.get(),${nl}                    new Item.Properties().setId(ResourceKey.create(Registries.ITEM, key)).useBlockDescriptionPrefix())${nl}    );${nl}${nl}    "
Add-BeforeMarker (Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\ModItems.java') '// TALL_DOOR_VARIANT_CREATIVE_ENTRIES' "event.accept(${constant}_ITEM);${nl}            "
Add-BeforeMarker (Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\ModEntities.java') '// TALL_DOOR_VARIANT_BLOCK_ENTITY_ENTRIES' "ModBlocks.$constant.get(),${nl}                    "
Add-BeforeMarker (Join-Path $root 'src\main\java\com\digitaldownpour\atouchofclass\KeyCutterMenu.java') '// TALL_DOOR_VARIANT_KEY_CUTTER_ENTRIES' "ATouchofClass.MODID + `":$id`",${nl}            "

$langPath = Join-Path $resources 'assets\atouchofclass\lang\en_us.json'; $lang = Get-Content $langPath -Raw | ConvertFrom-Json
$lang | Add-Member -NotePropertyName "block.atouchofclass.$id" -NotePropertyValue "Tall $displayWood Door"
$lang | Add-Member -NotePropertyName "item.atouchofclass.$id" -NotePropertyValue "Tall $displayWood Door"
[IO.File]::WriteAllText($langPath, ($lang | ConvertTo-Json -Depth 20) + $nl)
$tagPath = Join-Path $resources 'data\minecraft\tags\item\doors.json'; $tag = Get-Content $tagPath -Raw | ConvertFrom-Json; $tag.values = @($tag.values) + "atouchofclass:$id"; [IO.File]::WriteAllText($tagPath, ($tag | ConvertTo-Json -Depth 10) + $nl)
Write-Output "Generated tall door variant '$id'."
