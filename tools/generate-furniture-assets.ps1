param()

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
$assetsRoot = Join-Path $projectRoot "src/main/resources/assets/atouchofclass"
$dataRoot = Join-Path $projectRoot "src/main/resources/data/atouchofclass"

function Write-JsonFile([string] $Path, $Value) {
    $directory = Split-Path $Path -Parent
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $Value | ConvertTo-Json -Depth 30 | Set-Content -Encoding utf8 -Path $Path
}

function New-Faces([string] $Texture, [int] $NorthRotation = 0, $NorthUv = $null) {
    $northFace = [ordered]@{ texture = $Texture }
    if ($null -ne $NorthUv) {
        $northFace.uv = @($NorthUv)
    }
    if ($NorthRotation -ne 0) {
        $northFace.rotation = $NorthRotation
    }
    return [ordered]@{
        down = [ordered]@{ texture = $Texture }
        up = [ordered]@{ texture = $Texture }
        north = $northFace
        south = [ordered]@{ texture = $Texture }
        west = [ordered]@{ texture = $Texture }
        east = [ordered]@{ texture = $Texture }
    }
}

function New-Element($From, $To, [string] $Texture, [int] $NorthRotation = 0,
                     [bool] $WorldAlignedNorth = $false) {
    $northUv = $null
    if ($WorldAlignedNorth) {
        # With a 90-degree face rotation, the first UV axis runs vertically.
        # Derive both axes from block coordinates so stacked faces meet at the
        # texture's repeating 0/16 edge instead of restarting a partial sample.
        $northUv = @(
            (16 - [double]$To[1]),
            (16 - [double]$To[0]),
            (16 - [double]$From[1]),
            (16 - [double]$From[0])
        )
    }
    return [ordered]@{
        from = @($From)
        to = @($To)
        faces = New-Faces $Texture $NorthRotation $northUv
    }
}

function Write-SharedAssets([string] $Name, [bool] $Oriented) {
    if ($Oriented) {
        $blockstate = [ordered]@{
            variants = [ordered]@{
                "facing=north" = [ordered]@{ model = "atouchofclass:block/$Name" }
                "facing=east" = [ordered]@{ model = "atouchofclass:block/$Name"; y = 90 }
                "facing=south" = [ordered]@{ model = "atouchofclass:block/$Name"; y = 180 }
                "facing=west" = [ordered]@{ model = "atouchofclass:block/$Name"; y = 270 }
            }
        }
    } else {
        $blockstate = [ordered]@{
            variants = [ordered]@{
                "" = [ordered]@{ model = "atouchofclass:block/$Name" }
            }
        }
    }

    Write-JsonFile (Join-Path $assetsRoot "blockstates/$Name.json") $blockstate
    Write-JsonFile (Join-Path $assetsRoot "items/$Name.json") ([ordered]@{
        model = [ordered]@{ type = "minecraft:model"; model = "atouchofclass:item/$Name" }
    })
    Write-JsonFile (Join-Path $assetsRoot "models/item/$Name.json") ([ordered]@{
        parent = "atouchofclass:block/$Name"
    })
    Write-JsonFile (Join-Path $dataRoot "loot_table/blocks/$Name.json") ([ordered]@{
        type = "minecraft:block"
        pools = @([ordered]@{
            rolls = 1
            conditions = @([ordered]@{ condition = "minecraft:survives_explosion" })
            entries = @([ordered]@{ type = "minecraft:item"; name = "atouchofclass:$Name" })
        })
    })
}

function Write-DoorCabinetBlockstate([string] $Name) {
    $variants = [ordered]@{}
    $facings = [ordered]@{ north = 0; east = 90; south = 180; west = 270 }
    foreach ($facing in $facings.Keys) {
        foreach ($joinedAbove in @($false, $true)) {
            foreach ($joinedBelow in @($false, $true)) {
                $suffix = if ($joinedAbove -and $joinedBelow) { "_joined_vertical" }
                          elseif ($joinedAbove) { "_joined_above" }
                          elseif ($joinedBelow) { "_joined_below" }
                          else { "" }
                $key = "facing=$facing,joined_above=$($joinedAbove.ToString().ToLower()),joined_below=$($joinedBelow.ToString().ToLower())"
                $variant = [ordered]@{ model = "atouchofclass:block/$Name$suffix" }
                if ($facings[$facing] -ne 0) {
                    $variant.y = $facings[$facing]
                }
                $variants[$key] = $variant
            }
        }
    }
    Write-JsonFile (Join-Path $assetsRoot "blockstates/$Name.json") ([ordered]@{ variants = $variants })
}

function Write-Countertop([string] $Name, [string] $Texture) {
    Write-SharedAssets $Name $false
    Write-JsonFile (Join-Path $assetsRoot "models/block/$Name.json") ([ordered]@{
        parent = "minecraft:block/block"
        ambientocclusion = $true
        textures = [ordered]@{ particle = $Texture; material = $Texture }
        elements = @(New-Element @(0, 0, 0) @(16, 2, 16) "#material")
    })
}

function Write-Sink([string] $Name, [string] $Texture, [string] $FaucetTexture) {
    Write-SharedAssets $Name $true
    $boxes = @(
        @(@(0, 0, 0), @(16, 2, 2)),
        @(@(0, 0, 2), @(6, 2, 3)),
        @(@(10, 0, 2), @(16, 2, 3)),
        @(@(0, 0, 3), @(4, 2, 4)),
        @(@(12, 0, 3), @(16, 2, 4)),
        @(@(0, 0, 4), @(3, 2, 7)),
        @(@(13, 0, 4), @(16, 2, 7)),
        @(@(0, 0, 7), @(4, 2, 8)),
        @(@(12, 0, 7), @(16, 2, 8)),
        @(@(0, 0, 8), @(6, 2, 9)),
        @(@(10, 0, 8), @(16, 2, 9)),
        @(@(0, 0, 9), @(16, 2, 16))
    )
    $elements = [System.Collections.ArrayList]::new()
    foreach ($box in $boxes) {
        [void]$elements.Add((New-Element $box[0] $box[1] "#material"))
    }

    $blackBasinBoxes = @(
        @(@(6, 0, 2), @(10, 1, 3)),
        @(@(4, 0, 3), @(12, 1, 4)),
        @(@(3, 0, 4), @(13, 1, 7)),
        @(@(4, 0, 7), @(12, 1, 8)),
        @(@(6, 0, 8), @(10, 1, 9))
    )
    foreach ($box in $blackBasinBoxes) {
        [void]$elements.Add((New-Element $box[0] $box[1] "#basin"))
    }

    $faucetBoxes = @(
        @(@(6.5, 2, 9.25), @(9.5, 2.4, 10.25)),
        @(@(7.4, 2.4, 9.55), @(8.6, 5, 10.45)),
        @(@(7.4, 4.4, 7), @(8.6, 5.2, 10.2)),
        @(@(7.4, 3.8, 6.8), @(8.6, 5.2, 7.6))
    )
    foreach ($box in $faucetBoxes) {
        [void]$elements.Add((New-Element $box[0] $box[1] "#faucet"))
    }

    Write-JsonFile (Join-Path $assetsRoot "models/block/$Name.json") ([ordered]@{
        parent = "minecraft:block/block"
        ambientocclusion = $true
        textures = [ordered]@{
            particle = $Texture
            material = $Texture
            basin = "minecraft:block/black_concrete"
            faucet = $FaucetTexture
        }
        elements = @($elements)
    })
}

function Add-DoorElements([System.Collections.ArrayList] $Elements, [double] $X0, [double] $X1,
                          [bool] $JoinedAbove, [bool] $JoinedBelow) {
    $y0 = if ($JoinedBelow) { 0 } else { 1.5 }
    $y1 = if ($JoinedAbove) { 16 } else { 15.25 }
    $panel = New-Element @($X0, $y0, 1.5) @($X1, $y1, 2) "#wood"
    $panel.faces.north.tintindex = 0
    [void]$Elements.Add($panel)
    [void]$Elements.Add((New-Element @($X0, $y0, 1.2) @(($X0 + .75), $y1, 1.5) "#wood"))
    [void]$Elements.Add((New-Element @(($X1 - .75), $y0, 1.2) @($X1, $y1, 1.5) "#wood"))
    if (-not $JoinedBelow) {
        [void]$Elements.Add((New-Element @(($X0 + .75), 1.5, 1.2) @(($X1 - .75), 2.25, 1.5) "#wood"))
    }
    if (-not $JoinedAbove) {
        [void]$Elements.Add((New-Element @(($X0 + .75), 14.5, 1.2) @(($X1 - .75), 15.25, 1.5) "#wood"))
    }
}

function Add-DrawerElements([System.Collections.ArrayList] $Elements, [double] $Y0, [double] $Y1) {
    [void]$Elements.Add((New-Element @(.75, $Y0, 1.5) @(15.25, $Y1, 2) "#wood" 90 $true))
    [void]$Elements.Add((New-Element @(.75, $Y0, 1.2) @(1.5, $Y1, 1.5) "#wood" 90 $true))
    [void]$Elements.Add((New-Element @(14.5, $Y0, 1.2) @(15.25, $Y1, 1.5) "#wood" 90 $true))
    [void]$Elements.Add((New-Element @(1.5, $Y0, 1.2) @(14.5, ($Y0 + .75), 1.5) "#wood" 90 $true))
    [void]$Elements.Add((New-Element @(1.5, ($Y1 - .75), 1.2) @(14.5, $Y1, 1.5) "#wood" 90 $true))
}

function Write-Cabinet([string] $Name, [ValidateSet("door", "drawer")] [string] $Style,
                       [string] $WoodTexture, [string] $HandleTexture) {
    Write-SharedAssets $Name $true
    if ($Style -eq "door") {
        Write-JsonFile (Join-Path $assetsRoot "items/$Name.json") ([ordered]@{
            model = [ordered]@{
                type = "minecraft:model"
                model = "atouchofclass:item/$Name"
                tints = @([ordered]@{ type = "minecraft:constant"; value = -986896 })
            }
        })
        Write-DoorCabinetBlockstate $Name
        Write-DoorCabinetModel $Name $WoodTexture $HandleTexture $false $false ""
        Write-DoorCabinetModel $Name $WoodTexture $HandleTexture $true $false "_joined_above"
        Write-DoorCabinetModel $Name $WoodTexture $HandleTexture $false $true "_joined_below"
        Write-DoorCabinetModel $Name $WoodTexture $HandleTexture $true $true "_joined_vertical"
        return
    }

    $elements = [System.Collections.ArrayList]::new()
    [void]$elements.Add((New-Element @(0, 0, 2) @(16, 16, 16) "#wood"))
    [void]$elements.Add((New-Element @(1, 0, 1.5) @(15, 1.25, 2) "#wood"))

    Add-DrawerElements $elements 1.5 7.65
    Add-DrawerElements $elements 8.15 15.25
    [void]$elements.Add((New-Element @(5, 4.25, 1) @(11, 4.75, 1.2) "#handle"))
    [void]$elements.Add((New-Element @(5, 11.45, 1) @(11, 11.95, 1.2) "#handle"))

    Write-JsonFile (Join-Path $assetsRoot "models/block/$Name.json") ([ordered]@{
        parent = "minecraft:block/block"
        ambientocclusion = $true
        textures = [ordered]@{ particle = $WoodTexture; wood = $WoodTexture; handle = $HandleTexture }
        elements = @($elements)
    })
}

function Write-DoorCabinetModel([string] $Name, [string] $WoodTexture, [string] $HandleTexture,
                                [bool] $JoinedAbove, [bool] $JoinedBelow, [string] $Suffix) {
    $elements = [System.Collections.ArrayList]::new()
    [void]$elements.Add((New-Element @(0, 0, 2) @(16, 16, 16) "#wood"))
    if (-not $JoinedBelow) {
        [void]$elements.Add((New-Element @(1, 0, 1.5) @(15, 1.25, 2) "#wood"))
    }
    Add-DoorElements $elements .75 7.75 $JoinedAbove $JoinedBelow
    Add-DoorElements $elements 8.25 15.25 $JoinedAbove $JoinedBelow
    if (-not $JoinedBelow) {
        [void]$elements.Add((New-Element @(6.7, 8.7, 1) @(7.25, 9.25, 1.2) "#handle"))
        [void]$elements.Add((New-Element @(8.75, 8.7, 1) @(9.3, 9.25, 1.2) "#handle"))
    }

    Write-JsonFile (Join-Path $assetsRoot "models/block/$Name$Suffix.json") ([ordered]@{
        parent = "minecraft:block/block"
        ambientocclusion = $true
        textures = [ordered]@{ particle = $WoodTexture; wood = $WoodTexture; handle = $HandleTexture }
        elements = @($elements)
    })
}

Write-Countertop "calcite_countertop" "minecraft:block/calcite"
Write-Sink "calcite_sink" "minecraft:block/calcite" "atouchofclass:block/brushed_stainless_steel"
Write-Cabinet "dark_oak_door_cabinet" "door" "minecraft:block/stripped_dark_oak_log" "atouchofclass:block/brushed_stainless_steel"
Write-Cabinet "dark_oak_drawer_cabinet" "drawer" "minecraft:block/stripped_dark_oak_log" "atouchofclass:block/brushed_stainless_steel"

Write-Output "Generated countertop, sink, cabinet blockstate/model/item/loot assets."
