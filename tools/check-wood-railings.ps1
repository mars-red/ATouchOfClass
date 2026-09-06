$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$resources="$root/src/main/resources"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$vanilla=[IO.Compression.ZipFile]::OpenRead("$root/build/moddev/artifacts/neoforge-21.4.136-client-extra-aka-minecraft-resources.jar")
try {
    $woods=@('spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','pale_oak','bamboo','crimson','warped')
    foreach($wood in $woods) {
        $stripped=switch($wood){'bamboo'{'stripped_bamboo_block'} 'crimson'{'stripped_crimson_stem'} 'warped'{'stripped_warped_stem'} default{"stripped_${wood}_log"}}
        foreach($source in Get-ChildItem "$resources/assets/atouchofclass/models/block" -Filter 'oak_*.json' | Where-Object {$_.BaseName -match '^oak_(railing|baluster|balustrade|newel_post)($|_)'}) {
            $file=$source.FullName.Replace($source.Name,($source.Name -replace '^oak_',"${wood}_"))
            $model=Get-Content $file -Raw | ConvertFrom-Json
            $oak=Get-Content $source.FullName -Raw | ConvertFrom-Json
            if(($model.elements | ConvertTo-Json -Depth 30 -Compress) -ne ($oak.elements | ConvertTo-Json -Depth 30 -Compress)){throw "$file changes geometry or UVs"}
            foreach($texture in $model.textures.PSObject.Properties.Value) {
                if($texture.StartsWith('minecraft:')) {
                    $path='assets/minecraft/textures/'+$texture.Substring(10)+'.png'
                    if(!$vanilla.GetEntry($path)){throw "Missing vanilla texture $texture"}
                    if($texture -ne 'minecraft:block/copper_block' -and !$texture.Contains($stripped)){throw "Wrong wood texture $texture"}
                }
            }
        }
        foreach($kind in @('railing','baluster','balustrade','newel_post')) {
            $id="${wood}_$kind"
            $state=Get-Content "$resources/assets/atouchofclass/blockstates/$id.json" -Raw | ConvertFrom-Json
            foreach($variant in $state.variants.PSObject.Properties.Value) {
                if(!$variant.model.StartsWith("atouchofclass:block/${wood}_")){throw "Wrong species in blockstate $id"}
                $path=$variant.model.Replace('atouchofclass:',"$resources/assets/atouchofclass/models/")+'.json'
                if(!(Test-Path $path)){throw "Missing variant model $path"}
            }
            $recipe=Get-Content "$resources/data/atouchofclass/recipe/$id.json" -Raw | ConvertFrom-Json
            $oak=Get-Content "$resources/data/atouchofclass/recipe/oak_$kind.json" -Raw | ConvertFrom-Json
            if($recipe.result.id -ne "atouchofclass:$id" -or $recipe.result.count -ne $oak.result.count -or ($recipe.pattern -join '|') -ne ($oak.pattern -join '|')){throw "Recipe mismatch $id"}
            foreach($ingredient in $recipe.key.PSObject.Properties.Value) {
                if($ingredient.StartsWith('minecraft:') -and !$vanilla.GetEntry('assets/minecraft/items/'+$ingredient.Substring(10)+'.json')){throw "Unknown ingredient $ingredient"}
            }
            $loot=Get-Content "$resources/data/atouchofclass/loot_table/blocks/$id.json" -Raw
            if(!$loot.Contains("atouchofclass:$id")){throw "Wrong loot $id"}
            if(!(Test-Path "$resources/assets/atouchofclass/items/$id.json")){throw "Missing item $id"}
            $item=Get-Content "$resources/assets/atouchofclass/items/$id.json" -Raw | ConvertFrom-Json
            if($item.model.model -notmatch "^atouchofclass:(item|block)/${wood}_"){throw "Wrong species in item $id"}
        }
    }
    Write-Output 'All 44 blocks passed: model parity, existing vanilla textures and ingredients, blockstate model references, recipes, items, and loot.'
} finally {$vanilla.Dispose()}
