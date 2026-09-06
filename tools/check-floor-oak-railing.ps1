$ErrorActionPreference = 'Stop'
$assets = Join-Path (Split-Path $PSScriptRoot -Parent) 'src/main/resources/assets/atouchofclass'
foreach ($shape in @('straight','inner_left','inner_right','outer_left','outer_right')) {
    $rail = Get-Content "$assets/models/block/oak_balustrade_$shape.json" -Raw | ConvertFrom-Json
    $post = (Get-Content "$assets/models/block/oak_baluster_$shape.json" -Raw | ConvertFrom-Json).elements[0]
    $integrated = $rail.elements[-1]
    foreach ($axis in @(0,2)) {
        if ($post.from[$axis] -ne $integrated.from[$axis] -or $post.to[$axis] -ne $integrated.to[$axis]) { throw "$shape post seam mismatch" }
    }
    if ($post.from[1] -ne 0 -or $post.to[1] -ne 16 -or $integrated.from[1] -ne 0 -or $integrated.to[1] -ne 12) { throw "$shape post heights incorrect" }
    $source = if ($shape -eq 'straight') {'oak_railing'} else {"oak_railing_$shape"}
    $wood = @((Get-Content "$assets/models/block/$source.json" -Raw | ConvertFrom-Json).elements | Where-Object {$_.faces.north.texture -ne '#copper'})
    if ($rail.elements.Count -ne $wood.Count+1) {throw "$shape must have one post and no copper"}
    for ($i=0;$i -lt $wood.Count;$i++) {
        if (($wood[$i] | ConvertTo-Json -Depth 20 -Compress) -ne ($rail.elements[$i] | ConvertTo-Json -Depth 20 -Compress)) {throw "$shape handrail differs from wall profile"}
    }
    foreach ($element in $rail.elements) {
        foreach ($face in $element.faces.PSObject.Properties.Value) {
            if ($face.texture -notin @('#oak','#end')) {throw "$shape unexpected texture"}
        }
    }
}
Write-Output 'Floor railing geometry passed: matching wall corner profiles, seamless post extensions, stripped oak textures, and no copper.'
foreach($name in @('upper_left','upper_right','lower_left','lower_right')) {
    $model=Get-Content "$assets/models/block/oak_balustrade_$name.json" -Raw | ConvertFrom-Json
    $wall=Get-Content "$assets/models/block/oak_railing_$name.json" -Raw | ConvertFrom-Json
    $wood=@($model.elements | Where-Object {$_.faces.north.texture -eq '#oak'})
    $original=@($wall.elements | Where-Object {$_.faces.north.texture -eq '#oak'})
    if(($wood | ConvertTo-Json -Depth 20 -Compress) -ne ($original | ConvertTo-Json -Depth 20 -Compress)) {throw "$name must preserve the seamless stepped rail"}
    $posts=@($model.elements | Where-Object {$_.faces.north.texture -eq '#post'})
    if($name.StartsWith('lower')) {
        if($posts.Count -ne 0) {throw "$name must be post-free"}
    } else {
        if($posts.Count -ne 2) {throw "$name must have a two-unit stepped-top post"}
        foreach($post in $posts) {
            if($post.from[1] -ne 0) {throw "$name must touch the stair"}
            if(($name -eq 'upper_left' -and $post.from[0] -lt 8) -or ($name -eq 'upper_right' -and $post.to[0] -gt 8)) {throw "$name post misses the upper tread"}
            $cap=@($wood | Where-Object {$_.from[0] -eq $post.from[0] -and $_.from[2] -eq 11.75} | Sort-Object {$_.from[1]})[0]
            if($post.to[1] -ne $cap.from[1]) {throw "$name post must touch the chamfer without overlapping"}
        }
    }
    foreach($element in $model.elements) {
        if($element.faces.north.texture -eq '#copper') {throw "$name contains copper"}
        for($i=0;$i -lt 3;$i++) {if($element.from[$i] -lt 0 -or $element.to[$i] -gt 16) {throw "$name exceeds block bounds"}}
    }
}
Write-Output 'Diagonal balustrade geometry passed: alternating posts, upper-tread footprints, matching chamfers, and unchanged seamless rail geometry.'
