# Native Minecraft geometry; preserves other families in the shared data maps and tags.
$ErrorActionPreference='Stop'
$resources=Join-Path (Split-Path $PSScriptRoot -Parent) 'src/main/resources'
function Write-Json($path,$value) {
    $full=Join-Path $resources $path
    [IO.Directory]::CreateDirectory((Split-Path $full))|Out-Null
    [IO.File]::WriteAllText($full,($value|ConvertTo-Json -Depth 30)+"`n")
}
function Read-Json($path) { Get-Content (Join-Path $resources $path) -Raw|ConvertFrom-Json -AsHashtable }
function Box($from,$to,$texture='copper',$rotation=$null) {
    $faces=[ordered]@{}
    foreach($face in @('north','south','east','west','up','down')) {
        $faces[$face]=@{texture="#$texture";uv=@(0,0,16,16)}
    }
    $e=@{from=$from;to=$to;faces=$faces}
    if($rotation){$e.rotation=$rotation}
    if($texture -eq 'crystal'){$e.light_emission=15;$e.shade=$false}
    $script:elements+=$e
}
function Ring($radius,$y,$height) {
    $length=2*$radius*[Math]::Tan([Math]::PI/8)+0.1
    for($i=0;$i -lt 8;$i++) {
        $angle=$i*[Math]::PI/4
        $x=8+$radius*[Math]::Sin($angle);$z=8+$radius*[Math]::Cos($angle)
        $rotation=$null
        if($i%2 -eq 1){$rotation=@{origin=@($x,$y,$z);axis='y';angle=if($i%4 -eq 1){45}else{-45}}}
        if($i%4 -eq 2){Box @(($x-0.25),$y,($z-$length/2)) @(($x+0.25),($y+$height),($z+$length/2))}
        else{Box @(($x-$length/2),$y,($z-0.25)) @(($x+$length/2),($y+$height),($z+0.25)) 'copper' $rotation}
    }
}
$assets='assets/atouchofclass'
$elements=@()
Ring 6.3 10.5 0.65
Ring 4 7.3 0.4
Ring 1.9 4.2 0.35
# Slim four-way suspension braces, meeting at the top mounting hub.
foreach($axis in @('x','z')){foreach($sign in @(-1,1)){
    Box @(7.8,8.6,7.8) @(8.2,15.7,8.2) 'copper' @{origin=@(8,15.7,8);axis=$axis;angle=(45*$sign)}
}}
# Short radial seats connect the angled braces to the outer ring.
Box @(1.7,10.5,7.8) @(3.1,10.9,8.2)
Box @(12.9,10.5,7.8) @(14.3,10.9,8.2)
Box @(7.8,10.5,1.7) @(8.2,10.9,3.1)
Box @(7.8,10.5,12.9) @(8.2,10.9,14.3)
Box @(7.25,15.4,7.25) @(8.75,16,8.75)
# Fine internal hangers support the smaller tiers beneath the suspension braces.
foreach($offset in @(-4,4)) {
    Box @((7.9+$offset),7.3,7.9) @((8.1+$offset),11.8,8.1)
    Box @(7.9,7.3,(7.9+$offset)) @(8.1,11.8,(8.1+$offset))
}
Box @(4,7.3,7.9) @(12,7.5,8.1)
Box @(7.9,7.3,4) @(8.1,7.5,12)
foreach($offset in @(-1.9,1.9)) {
    Box @((7.9+$offset),4.2,7.9) @((8.1+$offset),7.3,8.1)
    Box @(7.9,4.2,(7.9+$offset)) @(8.1,7.3,(8.1+$offset))
}
# Hanging crystal prisms, with small copper hangers and narrower pointed ends.
foreach($tier in @(@(6,10.5,7.5,16),@(3.8,7.3,4.5,12),@(1.65,4.2,1.5,8))){
    $radius=$tier[0];$top=$tier[1];$bottom=$tier[2];$count=$tier[3]
    for($i=0;$i -lt $count;$i++){
        $a=2*[Math]::PI*$i/$count;$x=8+$radius*[Math]::Cos($a);$z=8+$radius*[Math]::Sin($a)
        Box @(($x-0.1),($top-0.5),($z-0.1)) @(($x+0.1),$top,($z+0.1))
        Box @(($x-0.28),($bottom+0.35),($z-0.28)) @(($x+0.28),($top-0.5),($z+0.28)) 'crystal'
        Box @(($x-0.18),$bottom,($z-0.18)) @(($x+0.18),($bottom+0.35),($z+0.18)) 'crystal'
    }
}
$display=@{gui=@{rotation=@(25,225,0);translation=@(0,0,0);scale=@(0.65,0.65,0.65)}}
Write-Json "$assets/models/block/copper_chandelier_base.json" @{parent='minecraft:block/block';textures=@{particle='#copper';crystal='atouchofclass:block/chandelier_crystal'};elements=$elements;display=$display}
$elements=@()
# Four narrow rectangular links per block, alternating planes. Solid geometry needs no special renderer.
for($i=0;$i -lt 4;$i++){
    $y=$i*4
    $parts=@(@(7.25,$y,7.85,8.75,($y+0.3),8.15),@(7.25,($y+3.7),7.85,8.75,($y+4),8.15),@(7.25,($y+0.3),7.85,7.55,($y+3.7),8.15),@(8.45,($y+0.3),7.85,8.75,($y+3.7),8.15))
    foreach($p in $parts){
        if($i%2 -eq 0){Box @($p[0],$p[1],$p[2]) @($p[3],$p[4],$p[5])}
        else{Box @($p[2],$p[1],$p[0]) @($p[5],$p[4],$p[3])}
    }
}
Write-Json "$assets/models/block/copper_chain_base.json" @{parent='minecraft:block/block';textures=@{particle='#copper'};elements=$elements;display=@{gui=@{rotation=@(20,225,0);translation=@(0,0,0);scale=@(1,1,1)}}}
$language=Read-Json "$assets/lang/en_us.json"
$wax=Read-Json 'data/neoforge/data_maps/block/waxables.json'
$ox=Read-Json 'data/neoforge/data_maps/block/oxidizables.json'
$pick=Read-Json 'data/minecraft/tags/block/mineable/pickaxe.json'
function Unlock($name,$ingredient){
    Write-Json "data/atouchofclass/advancement/recipes/decorations/$name.json" @{
        parent='minecraft:recipes/root';criteria=@{
            has_material=@{trigger='minecraft:inventory_changed';conditions=@{items=@(@{items=$ingredient})}}
            has_the_recipe=@{trigger='minecraft:recipe_unlocked';conditions=@{recipe="atouchofclass:$name"}}
        };requirements=@(,@('has_material','has_the_recipe'));rewards=@{recipes=@("atouchofclass:$name")}
    }
}
foreach($kind in @('chain','chandelier')){
    $names=@("copper_$kind","exposed_copper_$kind","weathered_copper_$kind","oxidized_copper_$kind")
    $textures=@('copper_block','exposed_copper','weathered_copper','oxidized_copper')
    for($i=0;$i -lt 4;$i++){
        $base=$names[$i]
        Write-Json "$assets/models/block/$base.json" @{parent="atouchofclass:block/copper_${kind}_base";textures=@{copper="minecraft:block/$($textures[$i])"}}
        $wax.values["atouchofclass:$base"]=@{waxed="atouchofclass:waxed_$base"}
        if($i -lt 3){$ox.values["atouchofclass:$base"]=@{next_oxidation_stage="atouchofclass:$($names[$i+1])"}}
        foreach($isWaxed in @($false,$true)){
            $name=if($isWaxed){"waxed_$base"}else{$base}
            $model="atouchofclass:block/$base"
            $variants=if($kind -eq 'chain'){@{'axis=y'=@{model=$model};'axis=x'=@{model=$model;x=90;y=90};'axis=z'=@{model=$model;x=90}}}else{@{''=@{model=$model}}}
            Write-Json "$assets/blockstates/$name.json" @{variants=$variants}
            Write-Json "$assets/items/$name.json" @{model=@{type='minecraft:model';model=$model}}
            Write-Json "data/atouchofclass/loot_table/blocks/$name.json" @{type='minecraft:block';pools=@(@{rolls=1;entries=@(@{type='minecraft:item';name="atouchofclass:$name"});conditions=@(@{condition='minecraft:survives_explosion'})})}
            # Keep legacy resource IDs while giving the thin mod chain a distinct display name.
            $displayName=$name.Replace('_',' ')
            if($kind -eq 'chain'){$displayName=$displayName.Replace('copper chain','copper chandelier chain')}
            $language["block.atouchofclass.$name"]=(Get-Culture).TextInfo.ToTitleCase($displayName)
            $pick.values+= "atouchofclass:$name"
            if($isWaxed){
                Write-Json "data/atouchofclass/recipe/$name.json" @{type='minecraft:crafting_shapeless';category='misc';ingredients=@("atouchofclass:$base",'minecraft:honeycomb');result=@{id="atouchofclass:$name";count=1}}
                Unlock $name "atouchofclass:$base"
            }
        }
    }
}
Write-Json "$assets/lang/en_us.json" $language
Write-Json 'data/neoforge/data_maps/block/waxables.json' $wax
Write-Json 'data/neoforge/data_maps/block/oxidizables.json' $ox
$pick.values=@($pick.values|Select-Object -Unique)
Write-Json 'data/minecraft/tags/block/mineable/pickaxe.json' $pick
Write-Json 'data/atouchofclass/recipe/copper_chain.json' @{type='minecraft:crafting_shaped';category='misc';pattern=@('N','N','N');key=@{N='minecraft:copper_nugget'};result=@{id='atouchofclass:copper_chain';count=1}}
Write-Json 'data/atouchofclass/recipe/copper_chandelier.json' @{type='minecraft:crafting_shaped';category='misc';pattern=@(' C ','NGN','AGA');key=@{C='atouchofclass:copper_chain';N='minecraft:copper_nugget';G='minecraft:glowstone_dust';A='minecraft:amethyst_shard'};result=@{id='atouchofclass:copper_chandelier';count=1}}
Unlock 'copper_chain' 'minecraft:copper_nugget'
Unlock 'copper_chandelier' 'atouchofclass:copper_chain'
# Faceted crystal highlights remain legible without shaders or translucent sorting.
Add-Type -AssemblyName System.Drawing
$b=[Drawing.Bitmap]::new(16,16)
for($y=0;$y -lt 16;$y++){for($x=0;$x -lt 16;$x++){
    $hex=if($x -lt 2 -or $x -gt 13){'FFF8DF'}elseif($x -lt 5){'DEDCCC'}elseif($x -lt 9){'FAF1D4'}elseif($x -lt 12){'C6D4D1'}else{'E8E5CE'}
    if($y -eq 2 -or $y -eq 13){$hex='FFFBE8'}
    $b.SetPixel($x,$y,[Drawing.ColorTranslator]::FromHtml('#'+$hex))
}}
$b.Save((Join-Path $resources "$assets/textures/block/chandelier_crystal.png"),[Drawing.Imaging.ImageFormat]::Png);$b.Dispose()
