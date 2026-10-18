# Rebuild the shared cuboid model and eight variants with vanilla copper textures.
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$resources = Join-Path $root 'src/main/resources'
function Write-Json($relative, $value) {
    $path = Join-Path $resources $relative
    [IO.Directory]::CreateDirectory((Split-Path $path)) | Out-Null
    [IO.File]::WriteAllText($path, ($value | ConvertTo-Json -Depth 30) + "`n")
}
$assets = 'assets/atouchofclass'
$elements = @()
function Add-Box($from, $to, $texture, $glow = $false) {
    $faces = [ordered]@{}
    foreach ($face in @('north','south','east','west','up','down')) {
        $faces[$face] = @{ texture = "#$texture"; uv = @(0,0,16,16) }
    }
    $element = @{ from=$from; to=$to; faces=$faces }
    if ($glow) { $element['light_emission']=15; $element['shade']=$false }
    $script:elements += $element
}
Add-Box @(5,1,15) @(11,10,16) 'copper'
Add-Box @(5.5,1.5,14.5) @(10.5,9.5,15) 'copper'
Add-Box @(6,2,14) @(10,9,14.5) 'copper'
Add-Box @(7,3,9) @(9,5,14) 'copper'
Add-Box @(7,4,8) @(9,7,10) 'copper'
Add-Box @(6.5,6.5,7.5) @(9.5,7,10.5) 'copper'
Add-Box @(5,7,6) @(11,8,12) 'copper'
Add-Box @(5.5,8,6.5) @(10.5,12.5,11.5) 'glass' $true
Add-Box @(5.25,12.5,6.25) @(10.75,13,11.75) 'glass' $true
Add-Box @(5,13,6) @(11,14,12) 'copper'
Write-Json "$assets/models/block/wall_sconce.json" @{
    parent='minecraft:block/block'; ambientocclusion=$true
    textures=@{particle='#copper'; glass='atouchofclass:block/wall_sconce_glass'}
    elements=$elements
    display=@{
        gui=@{rotation=@(30,225,0); translation=@(0,0,0); scale=@(0.85,0.85,0.85)}
        ground=@{rotation=@(0,0,0); translation=@(0,3,0); scale=@(0.5,0.5,0.5)}
        fixed=@{rotation=@(0,180,0); translation=@(0,0,0); scale=@(0.8,0.8,0.8)}
    }
}
$names = @('copper_wall_sconce','exposed_copper_wall_sconce','weathered_copper_wall_sconce','oxidized_copper_wall_sconce')
$textures = @('copper_block','exposed_copper','weathered_copper','oxidized_copper')
$allNames = @()
$languagePath = Join-Path $resources "$assets/lang/en_us.json"
$language = Get-Content $languagePath -Raw | ConvertFrom-Json -AsHashtable
for ($i=0; $i -lt 4; $i++) {
    $base = $names[$i]
    Write-Json "$assets/models/block/$base.json" @{parent='atouchofclass:block/wall_sconce';textures=@{copper="minecraft:block/$($textures[$i])"}}
    foreach ($waxed in @($false,$true)) {
        $name = if ($waxed) { "waxed_$base" } else { $base }
        $allNames += "atouchofclass:$name"
        $variants = [ordered]@{}
        $turn = 0
        foreach ($facing in @('north','east','south','west')) {
            $variants["facing=$facing"] = @{model="atouchofclass:block/$base";y=$turn;uvlock=$true}
            $turn += 90
        }
        Write-Json "$assets/blockstates/$name.json" @{variants=$variants}
        Write-Json "$assets/items/$name.json" @{model=@{type='minecraft:model';model="atouchofclass:block/$base"}}
        Write-Json "data/atouchofclass/loot_table/blocks/$name.json" @{
            type='minecraft:block'; pools=@(@{rolls=1;entries=@(@{type='minecraft:item';name="atouchofclass:$name"});conditions=@(@{condition='minecraft:survives_explosion'})})
        }
        $language["block.atouchofclass.$name"] = (Get-Culture).TextInfo.ToTitleCase($name.Replace('_',' '))
        if ($waxed) {
            Write-Json "data/atouchofclass/recipe/$name.json" @{
                type='minecraft:crafting_shapeless';category='misc';ingredients=@("atouchofclass:$base",'minecraft:honeycomb');result=@{id="atouchofclass:$name";count=1}
            }
            Write-Json "data/atouchofclass/advancement/recipes/decorations/$name.json" @{
                parent='minecraft:recipes/root'
                criteria=@{
                    has_sconce=@{trigger='minecraft:inventory_changed';conditions=@{items=@(@{items="atouchofclass:$base"})}}
                    has_the_recipe=@{trigger='minecraft:recipe_unlocked';conditions=@{recipe="atouchofclass:$name"}}
                }
                requirements=@(,@('has_sconce','has_the_recipe'))
                rewards=@{recipes=@("atouchofclass:$name")}
            }
        }
    }
}
Write-Json "$assets/lang/en_us.json" $language
Write-Json 'data/atouchofclass/recipe/copper_wall_sconce.json' @{
    type='minecraft:crafting_shaped';category='misc';pattern=@(' CG',' CT',' CC')
    key=@{C='minecraft:copper_nugget';G='minecraft:white_stained_glass';T='minecraft:torch'}
    result=@{id='atouchofclass:copper_wall_sconce';count=1}
}
Write-Json 'data/atouchofclass/advancement/recipes/decorations/copper_wall_sconce.json' @{
    parent='minecraft:recipes/root'
    criteria=@{
        has_copper=@{trigger='minecraft:inventory_changed';conditions=@{items=@(@{items='minecraft:copper_nugget'})}}
        has_the_recipe=@{trigger='minecraft:recipe_unlocked';conditions=@{recipe='atouchofclass:copper_wall_sconce'}}
    }
    requirements=@(,@('has_copper','has_the_recipe'))
    rewards=@{recipes=@('atouchofclass:copper_wall_sconce')}
}
$oxidizables = [ordered]@{}
$waxables = [ordered]@{}
# Keep other copper families when regenerating sconces.
$oxidationPath = Join-Path $resources 'data/neoforge/data_maps/block/oxidizables.json'
$waxPath = Join-Path $resources 'data/neoforge/data_maps/block/waxables.json'
if (Test-Path $oxidationPath) { $oxidizables = (Get-Content $oxidationPath -Raw | ConvertFrom-Json -AsHashtable).values }
if (Test-Path $waxPath) { $waxables = (Get-Content $waxPath -Raw | ConvertFrom-Json -AsHashtable).values }
for ($i=0; $i -lt 4; $i++) {
    $waxables["atouchofclass:$($names[$i])"]=@{waxed="atouchofclass:waxed_$($names[$i])"}
    if ($i -lt 3) { $oxidizables["atouchofclass:$($names[$i])"]=@{next_oxidation_stage="atouchofclass:$($names[$i+1])"} }
}
Write-Json 'data/neoforge/data_maps/block/oxidizables.json' @{values=$oxidizables}
Write-Json 'data/neoforge/data_maps/block/waxables.json' @{values=$waxables}
$tagPath='data/minecraft/tags/block/mineable/pickaxe.json'
$tag=@{replace=$false;values=@()}
if (Test-Path (Join-Path $resources $tagPath)) { $tag=Get-Content (Join-Path $resources $tagPath) -Raw | ConvertFrom-Json -AsHashtable }
$tag.values=@($tag.values + $allNames | Select-Object -Unique)
Write-Json $tagPath $tag

# A small native pixel texture: warm ivory, with subtle frosted-glass variation.
Add-Type -AssemblyName System.Drawing
$bitmap = [Drawing.Bitmap]::new(16,16)
$random = [Random]::new(42)
for ($y=0; $y -lt 16; $y++) {
    for ($x=0; $x -lt 16; $x++) {
        $variation=$random.Next(-5,6)
        $edge=if ($x -eq 0 -or $x -eq 15) { -9 } else { 0 }
        $bitmap.SetPixel($x,$y,[Drawing.Color]::FromArgb(255,249+$variation+$edge,230+$variation+$edge,184+$variation+$edge))
    }
}
$bitmap.Save((Join-Path $resources "$assets/textures/block/wall_sconce_glass.png"),[Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()
