$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$assets = Join-Path $project 'src/main/resources/assets/atouchofclass'
# Keep the horizontal rail's 3:0.5:0.5 cross-section proportions in the
# six-unit-high stepped diagonal approximation, with half-unit depth insets.
$all = [ordered]@{}
foreach ($name in @('upper_left','lower_right','lower_left','upper_right')) {
    $upper = $name -in @('upper_left','upper_right')
    $mirror = $name -in @('lower_left','upper_right')
    $offset = if ($upper) { 8 } else { -8 }
    $boxes = @(
        for ($x = 0; $x -lt 16; $x++) {
            foreach ($band in @(@(-2.25, 2.25, 11.25, 14.25), @(-3, -2.25, 11.75, 13.75), @(2.25, 3, 11.75, 13.75))) {
                $bottom = [Math]::Max([double]0, [double]($x + 0.5 + $offset + $band[0]))
                $top = [Math]::Min([double]16, [double]($x + 0.5 + $offset + $band[1]))
                if ($top -gt $bottom) { ,@($x, $bottom, $band[2], ($x + 1), $top, $band[3]) }
            }
        }
    )
    $oakCount = $boxes.Count
    $mountX = if ($upper) { 5 } else { 13 }
    $railBottom = $mountX + 0.5 + $offset - 3
    $boxes += @(,@($mountX, ($railBottom - 2), 15.25, ($mountX + 1), ($railBottom + 1), 16))
    $boxes += @(,@($mountX, ($railBottom - 1.25), 12.75, ($mountX + 1), ($railBottom - 0.25), 15.25))
    $boxes += @(,@($mountX, ($railBottom - 0.25), 12.75, ($mountX + 1), $railBottom, 13.75))
    if ($mirror) {
        foreach ($b in $boxes) { $oldX = $b[0]; $b[0] = 16 - $b[3]; $b[3] = 16 - $oldX }
    }
    $all[$name] = $boxes
    $elements = for ($i = 0; $i -lt $boxes.Count; $i++) {
        $b = $boxes[$i]; $faces = [ordered]@{}
        foreach ($side in @('north','south','east','west','up','down')) {
            $faces[$side] = @{texture = $(if ($i -lt $oakCount) { '#oak' } else { '#copper' })}
        }
        @{from = @($b[0],$b[1],$b[2]); to = @($b[3],$b[4],$b[5]); faces = $faces}
    }
    @{loader='atouchofclass:railing'; grain_slope=$(if ($mirror) { -1 } else { 1 }); grain_lower=(-not $upper); parent='minecraft:block/block'; textures=@{oak='minecraft:block/stripped_oak_log'; copper='minecraft:block/copper_block'; particle='minecraft:block/stripped_oak_log'}; elements=@($elements)} |
        ConvertTo-Json -Depth 12 | Set-Content "$assets/models/block/oak_railing_$name.json" -Encoding utf8
}
$flatModel = Get-Content "$assets/models/block/oak_railing.json" -Raw | ConvertFrom-Json
foreach ($name in @('transition_right','transition_left')) {
    $parts = [Collections.Generic.List[object]]::new()
    # Keep ten units level, including the chamfer and original copper stand-off.
    foreach ($original in $flatModel.elements) {
        if ($original.from[0] -ge 10) { continue }
        $e = $original | ConvertTo-Json -Depth 12 | ConvertFrom-Json
        $e.to[0] = [Math]::Min([double]10, [double]$e.to[0])
        foreach ($face in $e.faces.PSObject.Properties) {
            if ($face.Value.texture -eq '#oak') { $face.Value.texture = '#horizontal' }
        }
        $parts.Add($e)
    }
    # A six-unit ramp drops six units from Y=14 to Y=8, giving a 45-degree slope.
    # Gradually widen its cross-section
    # from the level profile to the stepped diagonal profile at the far edge.
    $ramp = @(
        for ($x = 10; $x -lt 16; $x++) {
            $t = ($x + 0.5 - 10) / 6
            $center = 14 - 6 * $t
            $coreHalf = 1.5 + 0.75 * $t
            $outerHalf = 2 + $t
            foreach ($band in @(@(-$coreHalf,$coreHalf,11.25,14.25), @(-$outerHalf,-$coreHalf,11.75,13.75), @($coreHalf,$outerHalf,11.75,13.75))) {
                ,@($x, ($center + $band[0]), $band[2], ($x + 1), ([Math]::Min([double]16,[double]($center + $band[1]))), $band[3])
            }
        }
    )
    $mountBottom = 14 - 6 * (2.5 / 6) - (2 + 2.5 / 6)
    $hardware = @(
        ,@(12, ($mountBottom - 2), 15.25, 13, ($mountBottom + 1), 16)
        ,@(12, ($mountBottom - 1.25), 12.75, 13, ($mountBottom - 0.25), 15.25)
        ,@(12, ($mountBottom - 0.25), 12.75, 13, $mountBottom, 13.75)
    )
    foreach ($b in @($ramp) + @($hardware)) {
        $faces = [ordered]@{}
        $material = if ($parts.Count -lt 6 + $ramp.Count) { '#oak' } else { '#copper' }
        foreach ($side in @('north','south','east','west','up','down')) { $faces[$side] = @{texture=$material} }
        $parts.Add([pscustomobject]@{from=@($b[0],$b[1],$b[2]); to=@($b[3],$b[4],$b[5]); faces=$faces})
    }
    if ($name -eq 'transition_left') {
        foreach ($e in $parts) { $oldX=$e.from[0]; $e.from[0]=16-$e.to[0]; $e.to[0]=16-$oldX }
    }
    $all[$name] = @($parts | ForEach-Object { ,(@($_.from) + @($_.to)) })
    @{loader='atouchofclass:railing'; grain_slope=$(if ($name -eq 'transition_right') { -1 } else { 1 }); grain_lower=$false; parent='minecraft:block/block'; textures=@{horizontal='minecraft:block/stripped_oak_log'; end='minecraft:block/stripped_oak_log_top'; oak='minecraft:block/stripped_oak_log'; copper='minecraft:block/copper_block'; particle='minecraft:block/stripped_oak_log'}; elements=@($parts.ToArray())} |
        ConvertTo-Json -Depth 12 | Set-Content "$assets/models/block/oak_railing_$name.json" -Encoding utf8
}
$variants = [ordered]@{}
foreach ($slope in @('flat') + @($all.Keys)) {
    $modelName = if ($slope -eq 'flat') { 'oak_railing' } else { "oak_railing_$slope" }
    $turn = 0
    foreach ($facing in @('north','east','south','west')) {
        $variants["facing=$facing,slope=$slope"] = @{model="atouchofclass:block/$modelName"; y=$turn; uvlock=$true}
        $turn += 90
    }
}
@{variants=$variants} | ConvertTo-Json -Depth 8 | Set-Content "$assets/blockstates/oak_railing.json" -Encoding utf8
$java = @('package com.digitaldownpour.atouchofclass.blocks;', '',
    '// Generated by tools/generate-angled-railing.ps1; shared model and hitbox coordinates.',
    'final class OakRailingGeometry {', '    private OakRailingGeometry() {}')
foreach ($name in $all.Keys) {
    $java += '    private static final double[][] ' + $name.ToUpperInvariant() + ' = {'
    $java += $all[$name] | ForEach-Object {
        '        {' + (($_ | ForEach-Object { ([double]$_).ToString([Globalization.CultureInfo]::InvariantCulture) }) -join ', ') + '},'
    }
    $java += '    };'
}
$java += @('    static double[][] boxes(OakRailingBlock.Slope slope) {', '        return switch (slope) {')
foreach ($name in $all.Keys) { $java += '            case ' + $name.ToUpperInvariant() + ' -> ' + $name.ToUpperInvariant() + ';' }
$java += @('            default -> throw new IllegalArgumentException("No corner geometry for " + slope);', '        };', '    }', '}')
$java | Set-Content "$project/src/main/java/com/digitaldownpour/atouchofclass/blocks/OakRailingGeometry.java" -Encoding utf8

