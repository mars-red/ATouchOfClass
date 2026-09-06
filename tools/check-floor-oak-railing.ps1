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
