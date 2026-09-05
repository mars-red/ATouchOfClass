$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$model = Get-Content "$project/src/main/resources/assets/atouchofclass/models/block/oak_railing.json" -Raw | ConvertFrom-Json
$rail = @($model.elements | Where-Object { $_.faces.north.texture -eq '#oak' })
$plates = @($model.elements | Where-Object { $_.faces.north.texture -eq '#copper' -and $_.to[2] -eq 16 })
function Assert-Railing($condition, $message) {
    if (-not $condition) { throw $message }
}
Assert-Railing ($rail.Count -eq 3 -and $plates.Count -eq 2) 'Expected shaped oak rail and two copper plates.'
foreach ($element in $rail) {
    Assert-Railing ($element.from[0] -eq 0 -and $element.to[0] -eq 16) 'Every rail segment must span the full block.'
}
$bottom = ($rail | ForEach-Object { $_.from[1] } | Measure-Object -Minimum).Minimum
$top = ($rail | ForEach-Object { $_.to[1] } | Measure-Object -Maximum).Maximum
Assert-Railing ($bottom -eq 12 -and $top -eq 16) 'Rail must be four units high and flush with the block top at Y=16.'
$back = ($rail | ForEach-Object { $_.to[2] } | Measure-Object -Maximum).Maximum
foreach ($plate in $plates) {
    Assert-Railing (($plate.from[2] - $back) -eq 1) 'Rail-to-plate clearance must be one unit.'
}
$centers = @($plates | ForEach-Object { ($_.from[0] + $_.to[0]) / 2 } | Sort-Object)
Assert-Railing (($centers[1] - $centers[0]) -eq 8) 'Stand-offs must be eight units apart.'
Assert-Railing ((16 + $centers[0] - $centers[1]) -eq 8) 'Spacing must remain eight units across a join.'
# Collision and selection geometry must exactly match every rendered element.
$java = Get-Content "$project/src/main/java/com/digitaldownpour/atouchofclass/blocks/OakRailingBlock.java" -Raw
$geometry = [regex]::Match($java, '(?s)double\[\]\[\] boxes = \{(.*?)\n        \};').Groups[1].Value
$boxes = [regex]::Matches($geometry, '\{([^{}]+)\}')
Assert-Railing ($boxes.Count -eq $model.elements.Count) 'Shape element count differs from model.'
for ($i = 0; $i -lt $boxes.Count; $i++) {
    $numbers = @($boxes[$i].Groups[1].Value.Split(',') | ForEach-Object { [double]::Parse($_.Trim(), [Globalization.CultureInfo]::InvariantCulture) })
    $expected = @($model.elements[$i].from) + @($model.elements[$i].to)
    for ($j = 0; $j -lt 6; $j++) {
        Assert-Railing ($numbers[$j] -eq $expected[$j]) "Shape differs from model at element $i coordinate $j."
    }
}
Write-Output 'Oak railing geometry checks passed: width, top alignment, even height, clearance, repeating spacing, and collision parity.'

$shapeSource = Get-Content "$project/src/main/java/com/digitaldownpour/atouchofclass/blocks/OakRailingGeometry.java" -Raw
$models = @{}
foreach ($name in @('upper_left','lower_right','lower_left','upper_right')) {
    $m = Get-Content "$project/src/main/resources/assets/atouchofclass/models/block/oak_railing_$name.json" -Raw | ConvertFrom-Json
    $models[$name] = $m
    $table = [regex]::Match($shapeSource, ('(?s)' + $name.ToUpperInvariant() + ' = \{(.*?)\n    \};')).Groups[1].Value
    $rows = [regex]::Matches($table, '\{([^{}]+)\}')
    Assert-Railing ($m.elements.Count -le 36 -and $rows.Count -eq $m.elements.Count) 'Corner geometry must remain compact and match its hitbox.'
    $wood = @($m.elements | Where-Object { $_.faces.north.texture -eq '#oak' })
    $hardware = @($m.elements | Where-Object { $_.faces.north.texture -eq '#copper' })
    Assert-Railing ($hardware.Count -eq 3) 'Preserve the copper bracket.'
    $core = @($wood | Where-Object { $_.from[2] -eq 11.25 -and $_.to[2] -eq 14.25 })
    $caps = @($wood | Where-Object { $_.from[2] -eq 11.75 -and $_.to[2] -eq 13.75 })
    Assert-Railing ($core.Count -gt 0 -and $caps.Count -gt 0 -and ($core.Count + $caps.Count) -eq $wood.Count) 'Grip must have a full-depth core and half-unit-inset caps.'
    foreach ($cap in $caps) { Assert-Railing (($cap.to[1] - $cap.from[1]) -le 0.75) 'Chamfer bands must retain the horizontal cross-section proportions.' }
    for ($i=0; $i -lt $rows.Count; $i++) {
        $element=$m.elements[$i]
        Assert-Railing ($null -eq $element.rotation) 'Corner elements must be unrotated.'
        $expected=@($rows[$i].Groups[1].Value.Split(',') | ForEach-Object { [double]::Parse($_.Trim(), [Globalization.CultureInfo]::InvariantCulture) })
        $actual=@($element.from)+@($element.to)
        for ($j=0; $j -lt 6; $j++) { Assert-Railing ($expected[$j] -eq $actual[$j]) 'Render and hitbox coordinates must match.' }
        Assert-Railing ($element.from[0] -ge 0 -and $element.to[0] -le 16 -and $element.from[1] -ge 0 -and $element.to[1] -le 16) 'Corner geometry must fit its block.'
        if ($element.faces.north.texture -eq '#oak') { Assert-Railing (($element.to[0]-$element.from[0]) -eq 1) 'Grip steps must be one unit wide.' }
    }
    Assert-Railing (($hardware[0].from[2]-$core[0].to[2]) -eq 1) 'Preserve one-unit wall clearance.'
}
# Compare occupied face areas at quarter-unit samples across alternating joins.
function Occupied($m, $x, $y, $z) {
    foreach ($e in $m.elements) {
        if ($x -gt $e.from[0] -and $x -lt $e.to[0] -and $y -gt $e.from[1] -and $y -lt $e.to[1] -and $z -gt $e.from[2] -and $z -lt $e.to[2]) { return $true }
    }
    return $false
}
foreach ($pair in @(@('upper_left','lower_right'),@('upper_right','lower_left'))) {
    for ($x=0.125; $x -lt 16; $x+=0.25) {
        for ($z=11.375; $z -lt 14.25; $z+=0.25) {
            Assert-Railing ((Occupied $models[$pair[0]] $x 15.999 $z) -eq (Occupied $models[$pair[1]] $x 0.001 $z)) 'Vertical join must match exactly.'
        }
    }
}
# Full-depth cores overlap across side joins despite the one-unit stair riser.
foreach ($pair in @(@('lower_right','upper_left'),@('upper_right','lower_left'))) {
    $a=@($models[$pair[0]].elements | Where-Object { $_.to[0] -eq 16 -and $_.faces.north.texture -eq '#oak' })[0]
    $b=@($models[$pair[1]].elements | Where-Object { $_.from[0] -eq 0 -and $_.faces.north.texture -eq '#oak' })[0]
    Assert-Railing (([Math]::Min($a.to[1],$b.to[1])-[Math]::Max($a.from[1],$b.from[1])) -eq 3.5) 'Side joins must stay continuous.'
}
Write-Output 'Corner checks passed: one-unit steps, block bounds, model/hitbox parity, wall clearance, and seamless alternating joins.'

Assert-Railing ($model.textures.oak -eq 'minecraft:block/stripped_oak_log') 'Horizontal wood must use stripped oak.'
foreach ($corner in $models.Values) { Assert-Railing ($corner.textures.oak -eq 'minecraft:block/stripped_oak_log') 'Diagonal wood must use vanilla stripped oak.' }
foreach ($name in @('transition_left','transition_right')) {
    $m = Get-Content "$project/src/main/resources/assets/atouchofclass/models/block/oak_railing_$name.json" -Raw | ConvertFrom-Json
    $table = [regex]::Match($shapeSource, ('(?s)' + $name.ToUpperInvariant() + ' = \{(.*?)\n    \};')).Groups[1].Value
    $rows = [regex]::Matches($table, '\{([^{}]+)\}')
    Assert-Railing ($rows.Count -eq $m.elements.Count -and $rows.Count -le 36) 'Transition model and hitbox must match and stay compact.'
    Assert-Railing ($m.textures.horizontal -eq 'minecraft:block/stripped_oak_log' -and $m.textures.oak -eq 'minecraft:block/stripped_oak_log') 'Transition wood must use vanilla stripped oak.'
    for ($i=0; $i -lt $rows.Count; $i++) {
        $e=$m.elements[$i]
        $expected=@($rows[$i].Groups[1].Value.Split(',') | ForEach-Object { [double]::Parse($_.Trim(), [Globalization.CultureInfo]::InvariantCulture) })
        $actual=@($e.from)+@($e.to)
        for ($j=0; $j -lt 6; $j++) { Assert-Railing ($expected[$j] -eq $actual[$j]) 'Transition hitbox must match model.' }
        Assert-Railing ($null -eq $e.rotation -and $e.from[1] -ge 0 -and $e.to[1] -le 16) 'Transition must remain unrotated and inside its block.'
        if ($e.faces.north.texture -eq '#horizontal') {
            Assert-Railing ($(if ($name -eq 'transition_right') { $e.from[0] -eq 0 -and $e.to[0] -eq 10 } else { $e.from[0] -eq 6 -and $e.to[0] -eq 16 })) 'Level portion must span ten units.'
        }
    }
    $flatX = if ($name -eq 'transition_right') { 0.001 } else { 15.999 }
    for ($y=0.125; $y -lt 16; $y+=0.25) {
        for ($z=11.375; $z -lt 14.25; $z+=0.25) {
            Assert-Railing ((Occupied $m $flatX $y $z) -eq (Occupied $model $flatX $y $z)) 'Level end must exactly match the horizontal rail.'
        }
    }
    $rampCore = @($m.elements | Where-Object { $_.faces.north.texture -eq '#oak' -and $_.from[2] -eq 11.25 } | Sort-Object { $_.from[0] })
    Assert-Railing ($rampCore.Count -eq 6) 'Transition must have six one-unit ramp steps.'
    for ($i=1; $i -lt $rampCore.Count; $i++) {
        $previous = ($rampCore[$i-1].from[1] + $rampCore[$i-1].to[1]) / 2
        $current = ($rampCore[$i].from[1] + $rampCore[$i].to[1]) / 2
        Assert-Railing ([Math]::Abs([Math]::Abs($current-$previous)-1) -lt 0.000001) 'Ramp must rise or fall one unit per unit of run (45 degrees).'
    }
    # Every adjacent ramp slice must share wood with its neighbor, including the bend.
    for ($x=1; $x -lt 16; $x++) {
        foreach ($z in @(11.5,12.5,14.0)) {
            $connected=$false
            for ($y=0.0625; $y -lt 16; $y+=0.125) {
                if ((Occupied $m ($x-0.001) $y $z) -and (Occupied $m ($x+0.001) $y $z)) { $connected=$true; break }
            }
            Assert-Railing $connected 'Transition grip must have no gaps between slices.'
        }
    }
    $endX = 16-$flatX
    $corners = if ($name -eq 'transition_right') { @('upper_left','lower_left') } else { @('upper_right','lower_right') }
    foreach ($cornerName in $corners) {
        foreach ($z in @(11.5,12.5,14.0)) {
            $connected=$false
            for ($y=0.0625; $y -lt 16; $y+=0.125) {
                if ((Occupied $m $endX $y $z) -and (Occupied $models[$cornerName] $flatX $y $z)) { $connected=$true; break }
            }
            Assert-Railing $connected 'Transition must connect to either slope direction.'
        }
    }
}
Write-Output 'Transition checks passed: texture separation, matching hitboxes, level ends, internal continuity, and joins to both diagonal directions.'

