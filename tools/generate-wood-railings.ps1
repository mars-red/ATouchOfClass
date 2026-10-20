$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$resources=Join-Path $root 'src/main/resources'
$woods=@('spruce','birch','poplar','jungle','acacia','dark_oak','mangrove','cherry','pale_oak','bamboo','crimson','warped')
$folders=@('assets/atouchofclass/models/block','assets/atouchofclass/models/item','assets/atouchofclass/items','assets/atouchofclass/blockstates','data/atouchofclass/recipe','data/atouchofclass/loot_table/blocks','data/atouchofclass/advancement/recipes/building_blocks')
$languagePath="$resources/assets/atouchofclass/lang/en_us.json"
$language=Get-Content $languagePath -Raw | ConvertFrom-Json -AsHashtable
foreach($wood in $woods) {
    $stripped=switch($wood){'bamboo'{'stripped_bamboo_block'} 'crimson'{'stripped_crimson_stem'} 'warped'{'stripped_warped_stem'} default{"stripped_${wood}_log"}}
    foreach($folder in $folders) {
        foreach($file in Get-ChildItem "$resources/$folder" -Filter 'oak_*.json' | Where-Object {$_.BaseName -match '^oak_(railing|baluster|balustrade|newel_post)($|_)'}) {
            $text=Get-Content $file.FullName -Raw
            # Keep the #oak material slot: the diagonal loader uses it to identify rotated grain.
            $text=$text.Replace('stripped_oak_log',$stripped).Replace('atouchofclass:block/oak_',"atouchofclass:block/${wood}_").Replace('atouchofclass:item/oak_',"atouchofclass:item/${wood}_").Replace('atouchofclass:oak_',"atouchofclass:${wood}_").Replace('minecraft:oak_',"minecraft:${wood}_")
            $name=$file.Name -replace '^oak_',"${wood}_"
            [IO.File]::WriteAllText("$resources/$folder/$name",$text)
        }
    }
    $title=(Get-Culture).TextInfo.ToTitleCase($wood.Replace('_',' '))
    $language["block.atouchofclass.${wood}_railing"]="Wall Mounted $title Railing"
    $language["block.atouchofclass.${wood}_baluster"]="$title Baluster"
    $language["block.atouchofclass.${wood}_balustrade"]="$title Balustrade"
    $language["block.atouchofclass.${wood}_newel_post"]="$title Newel Post"
}
$language | ConvertTo-Json -Depth 10 | Set-Content $languagePath
$tagPath="$resources/data/minecraft/tags/block/mineable/axe.json"
$tag=Get-Content $tagPath -Raw | ConvertFrom-Json
$values=@($tag.values)
foreach($wood in $woods){foreach($kind in @('railing','baluster','balustrade','newel_post')){$values+="atouchofclass:${wood}_$kind"}}
$tag.values=@($values | Select-Object -Unique)
$tag | ConvertTo-Json -Depth 10 | Set-Content $tagPath
Write-Output "Generated all $($woods.Count) additional wood families from the current oak models and recipes."
