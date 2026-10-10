$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$assets=Join-Path $root 'src/main/resources/assets/atouchofclass'
$tables=@()
$names=@('upper_left','upper_right','lower_left','lower_right')
foreach($name in $names) {
    $wall=Get-Content "$assets/models/block/oak_railing_$name.json" -Raw | ConvertFrom-Json
    $elements=@($wall.elements | Where-Object {$_.faces.north.texture -eq '#oak'})
    if($name.StartsWith('upper')) {
        # Two one-unit columns follow the underside of the stepped chamfer.
        # Place the post wholly on the stair's high half, mirrored for descending runs.
        foreach($x in @(9,10)) {
            $left=if($name -eq 'upper_left'){$x}else{15-$x}
            $faces=@{}
            foreach($face in @('north','south','east','west','up','down')) {
                $faces[$face]=@{texture=$(if($face -in @('up','down')){'#end'}else{'#post'})}
            }
            $elements+=@{from=@($left,0,11.75);to=@(($left+1),[Math]::Min([double]16,[double]($x+5.5)),13.75);faces=$faces}
        }
    }
    @{loader='atouchofclass:railing';grain_slope=$wall.grain_slope;grain_lower=$wall.grain_lower;parent='minecraft:block/block';
      textures=@{oak='minecraft:block/stripped_oak_log';post='minecraft:block/stripped_oak_log';end='minecraft:block/stripped_oak_log_top';particle='minecraft:block/stripped_oak_log'};
      elements=$elements} | ConvertTo-Json -Depth 20 | Set-Content "$assets/models/block/oak_balustrade_$name.json"
    $tables+='        {'+(($elements | ForEach-Object {'{'+(($_.from+$_.to)-join ',')+'}'})-join ',')+'}'
}
foreach($name in @('upper_left','upper_right')) {
    $model=Get-Content "$assets/models/block/oak_balustrade_$name.json" -Raw | ConvertFrom-Json
    $parts=@($model.elements | Where-Object {
        $_.faces.north.texture -eq '#post' -or $(if($name -eq 'upper_left'){$_.to[0] -le 6}else{$_.from[0] -ge 10})
    })
    foreach($part in $parts) {if($part.faces.north.texture -eq '#post') {$part.to[1]=12}}
    $flat=Get-Content "$assets/models/block/oak_railing.json" -Raw | ConvertFrom-Json
    foreach($part in @($flat.elements | Where-Object {$_.faces.north.texture -ne '#copper'})) {
        if($name -eq 'upper_left'){$part.from[0]=6}else{$part.to[0]=10}
        foreach($face in $part.faces.PSObject.Properties.Value) {if($face.texture -eq '#oak'){$face.texture='#horizontal'}}
        $parts+=$part
    }
    $model.elements=$parts
    $model.textures | Add-Member -NotePropertyName horizontal -NotePropertyValue 'minecraft:block/stripped_oak_log'
    $model | ConvertTo-Json -Depth 20 | Set-Content "$assets/models/block/oak_balustrade_${name}_top.json"
    $tables+='        {'+(($parts | ForEach-Object {'{'+(($_.from+$_.to)-join ',')+'}'})-join ',')+'}'
}
foreach($name in @('bottom_left','bottom_right')) {
    $wall=Get-Content "$assets/models/block/oak_railing_$name.json" -Raw | ConvertFrom-Json
    $elements=@($wall.elements | Where-Object {$_.faces.north.texture -ne '#copper'})
    # Keep the landing's centered post, with each column meeting the chamfer.
    foreach($x in @(7,8)) {
        $left=if($name -eq 'bottom_left'){$x}else{15-$x}
        $faces=@{}
        foreach($face in @('north','south','east','west','up','down')) {
            $faces[$face]=@{texture=$(if($face -in @('up','down')){'#end'}else{'#post'})}
        }
        $elements+=@{from=@($left,0,11.75);to=@(($left+1),($x+5.5),13.75);faces=$faces}
    }
    @{loader='atouchofclass:railing';grain_slope=$wall.grain_slope;grain_lower=$false;parent='minecraft:block/block';
      textures=@{oak='minecraft:block/stripped_oak_log';horizontal='minecraft:block/stripped_oak_log';post='minecraft:block/stripped_oak_log';end='minecraft:block/stripped_oak_log_top';particle='minecraft:block/stripped_oak_log'};
      elements=$elements} | ConvertTo-Json -Depth 20 | Set-Content "$assets/models/block/oak_balustrade_$name.json"
    $tables+='        {'+(($elements | ForEach-Object {'{'+(($_.from+$_.to)-join ',')+'}'})-join ',')+'}'
}
$names+=@('bottom_left','bottom_right')
$variants=@{}
$rotation=0
foreach($facing in @('north','east','south','west')) {
    foreach($shape in @('straight','inner_left','inner_right','outer_left','outer_right')) {
        foreach($slope in (@('flat')+$names)) {
            $model=if($slope -eq 'flat'){$shape}else{$slope}
            foreach($top in @('false','true')) {
                $variant=if($top -eq 'true' -and $slope -in @('upper_left','upper_right')){"${model}_top"}else{$model}
                $variants["facing=$facing,shape=$shape,slope=$slope,top_join=$top"]=@{model="atouchofclass:block/oak_balustrade_$variant";y=$rotation;uvlock=$true}
            }
        }
    }
    $rotation+=90
}
@{variants=$variants} | ConvertTo-Json -Depth 10 | Set-Content "$assets/blockstates/oak_balustrade.json"
$table=$tables-join ",`n"
@"
package com.digitaldownpour.atouchofclass.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.*;
import com.digitaldownpour.atouchofclass.blocks.OakRailingBlock.Slope;

// Generated by tools/generate-sloped-balustrade.ps1. Cached, axis-aligned hitboxes.
final class SlopedBalustradeGeometry {
    private static final double[][][] BOXES={
$table
    };
    private static final VoxelShape[][] SHAPES=new VoxelShape[8][4];
    static {
        for(int s=0;s<8;s++) for(int r=0;r<4;r++) {
            VoxelShape shape=Shapes.empty();
            for(double[] b:BOXES[s]) {
                double x0=b[0],z0=b[2],x1=b[3],z1=b[5];
                for(int i=0;i<r;i++){double n0=16-z1,n1=16-z0;z0=x0;z1=x1;x0=n0;x1=n1;}
                shape=Shapes.joinUnoptimized(shape,Block.box(x0,b[1],z0,x1,b[4],z1),BooleanOp.OR);
            }
            SHAPES[s][r]=shape.optimize();
        }
    }
    static VoxelShape shape(Slope slope,Direction facing,boolean top) {
        int s=switch(slope){case UPPER_LEFT->0;case UPPER_RIGHT->1;case LOWER_LEFT->2;case LOWER_RIGHT->3;case BOTTOM_LEFT->6;case BOTTOM_RIGHT->7;default->throw new IllegalArgumentException();};
        if(top && s<2) s+=4;
        int r=switch(facing){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        return SHAPES[s][r];
    }
}
"@ | Set-Content "$root/src/main/java/com/digitaldownpour/atouchofclass/blocks/SlopedBalustradeGeometry.java"
