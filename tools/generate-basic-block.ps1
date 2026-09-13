[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string] $TextureName
)

$ErrorActionPreference = 'Stop'

$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$resourceRoot = Join-Path $projectRoot 'src\main\resources'
$textureDirectory = Join-Path $resourceRoot 'assets\atouchofclass\textures\block'
$inputLeaf = [System.IO.Path]::GetFileName($TextureName)

if ($inputLeaf -ne $TextureName) {
    throw "Pass only a texture filename or block name, not a path: $TextureName"
}

$blockName = [System.IO.Path]::GetFileNameWithoutExtension($inputLeaf).ToLowerInvariant()
if ($blockName -notmatch '^[a-z0-9]+(?:_[a-z0-9]+)*$') {
    throw "The texture name must be a valid snake_case Minecraft resource name."
}

$texturePath = Join-Path $textureDirectory ($blockName + '.png')
if (-not (Test-Path -LiteralPath $texturePath -PathType Leaf)) {
    throw "Texture not found: $texturePath"
}

$constantName = $blockName.ToUpperInvariant()
$displayName = (($blockName -split '_') | ForEach-Object {
    if ($_.Length -eq 0) { return $_ }
    $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1)
}) -join ' '

function Write-GeneratedFile {
    param([string] $Path, [string] $Content)

    $normalized = $Content.TrimStart("`r", "`n") -replace "`r?`n", [Environment]::NewLine
    $normalized += [Environment]::NewLine
    if (Test-Path -LiteralPath $Path) {
        $existing = [System.IO.File]::ReadAllText($Path)
        $existingForComparison = $existing -replace "`r?`n", "`n"
        $generatedForComparison = $normalized -replace "`r?`n", "`n"
        if ($existingForComparison -ne $generatedForComparison) {
            throw "Refusing to overwrite an existing file with different content: $Path"
        }
        return
    }

    $parent = Split-Path -Parent $Path
    [System.IO.Directory]::CreateDirectory($parent) | Out-Null
    [System.IO.File]::WriteAllText($Path, $normalized)
}

$files = [ordered]@{
    "assets\atouchofclass\blockstates\$blockName.json" = @"
{
  "variants": {
    "": {
      "model": "atouchofclass:block/$blockName"
    }
  }
}
"@
    "assets\atouchofclass\models\block\$blockName.json" = @"
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "atouchofclass:block/$blockName"
  }
}
"@
    "assets\atouchofclass\models\item\$blockName.json" = @"
{
  "parent": "atouchofclass:block/$blockName"
}
"@
    "assets\atouchofclass\items\$blockName.json" = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "atouchofclass:item/$blockName"
  }
}
"@
    "data\atouchofclass\loot_table\blocks\$blockName.json" = @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "atouchofclass:$blockName"
        }
      ]
    }
  ]
}
"@
    "data\atouchofclass\recipe\$blockName.json" = @"
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "key": {},
  "pattern": [],
  "result": {
    "count": 1,
    "id": "atouchofclass:$blockName"
  }
}
"@
}

foreach ($entry in $files.GetEnumerator()) {
    Write-GeneratedFile -Path (Join-Path $resourceRoot $entry.Key) -Content $entry.Value
}

$modBlocksPath = Join-Path $projectRoot 'src\main\java\com\digitaldownpour\atouchofclass\ModBlocks.java'
$modBlocks = [System.IO.File]::ReadAllText($modBlocksPath)
$crlfCount = ([regex]::Matches($modBlocks, "`r`n")).Count
$lfCount = ([regex]::Matches($modBlocks, "(?<!`r)`n")).Count
$javaNewline = if ($crlfCount -gt $lfCount) { "`r`n" } else { "`n" }
$listStart = 'private static final java.util.List<String> BASIC_BLOCK_NAMES = java.util.List.of('
$startIndex = $modBlocks.IndexOf($listStart, [StringComparison]::Ordinal)
if ($startIndex -lt 0) { throw 'Could not find the generated basic-block registration list in ModBlocks.java.' }
$endIndex = $modBlocks.IndexOf(');', $startIndex, [StringComparison]::Ordinal)
$listText = $modBlocks.Substring($startIndex, $endIndex + 2 - $startIndex)
if ($listText -notmatch ('"' + [regex]::Escape($blockName) + '"')) {
    $existingNames = [regex]::Matches($listText, '"([a-z0-9_]+)"') | ForEach-Object { $_.Groups[1].Value }
    $allNames = @($existingNames) + $blockName | Sort-Object -Unique
    $entries = ($allNames | ForEach-Object { '            "' + $_ + '"' }) -join (',' + $javaNewline)
    $newListText = $listStart + $javaNewline + $entries + $javaNewline + '    );'
    $modBlocks = $modBlocks.Remove($startIndex, $listText.Length).Insert($startIndex, $newListText)
    [System.IO.File]::WriteAllText($modBlocksPath, $modBlocks)
}

$languagePath = Join-Path $resourceRoot 'assets\atouchofclass\lang\en_us.json'
$language = [System.IO.File]::ReadAllText($languagePath)
$languageKey = "block.atouchofclass.$blockName"
if ($language -notmatch ('"' + [regex]::Escape($languageKey) + '"\s*:')) {
    $parsedLanguage = $language | ConvertFrom-Json
    $parsedLanguage | Add-Member -NotePropertyName $languageKey -NotePropertyValue $displayName
    $language = $parsedLanguage | ConvertTo-Json -Depth 10
    [System.IO.File]::WriteAllText($languagePath, $language + [Environment]::NewLine)
}

# Basic blocks use stone-like properties, so include them in the pickaxe tag used by
# requiresCorrectToolForDrops().
$pickaxeTagPath = Join-Path $resourceRoot 'data\minecraft\tags\block\mineable\pickaxe.json'
$pickaxeTag = Get-Content -LiteralPath $pickaxeTagPath -Raw | ConvertFrom-Json
$blockResource = "atouchofclass:$blockName"
if ($pickaxeTag.values -notcontains $blockResource) {
    $pickaxeTag.values = @($pickaxeTag.values) + $blockResource
    $pickaxeJson = $pickaxeTag | ConvertTo-Json -Depth 10
    [System.IO.File]::WriteAllText($pickaxeTagPath, $pickaxeJson + [Environment]::NewLine)
}

Write-Output "Generated basic block '$blockName' from $texturePath"
Write-Output "Registration field: ModBlocks.BASIC_BLOCKS[`"$blockName`"]"
