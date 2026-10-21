"""Regenerate the two window-ledge asset families and recipes.

The regular brick finish is the approved preview texture. Buff brick uses
the same pixel sampling and mortar rhythm with the existing buff finish.
"""
from pathlib import Path
import json
import shutil
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/atouchofclass'

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')

def main():
    for material, ingredient in [('brick', 'red_brick'), ('buff_brick', 'buff_brick')]:
        name = material + '_window_ledge'
        ident = 'atouchofclass:' + name
        texture = ASSETS / f'textures/block/{name}.png'
        if material == 'brick':
            shutil.copyfile(ROOT / 'preview/window_ledge/brick_window_ledge.png', texture)
        else:
            source = np.asarray(Image.open(ASSETS / 'textures/block/buff_brick.png').convert('RGB'))
            atlas = np.zeros((16,16,3), dtype=np.uint8)
            for strip in range(4):
                for x in range(16):
                    for y in range(4):
                        atlas[strip*4+y,x] = source[y%3,x]
                        if x%4 == 3:
                            atlas[strip*4+y,x] = source[3,(x+y)%16]
            Image.fromarray(atlas).save(texture)
        uvs = {'north':[16,0,0,4], 'south':[0,0,16,4], 'up':[0,4,16,8],
               'down':[0,8,16,12], 'east':[0,12,4,16], 'west':[0,12,4,16]}
        model = {'textures':{'ledge':f'atouchofclass:block/{name}', 'particle':f'atouchofclass:block/{name}'},
                 'elements':[{'from':[0,12,12], 'to':[16,16,16],
                              'faces':{face:{'uv':uv,'texture':'#ledge'} for face,uv in uvs.items()}}],
                 'display':{'gui':{'rotation':[30,225,0],'translation':[0,-3,0],'scale':[.9,.9,.9]},
                            'fixed':{'translation':[0,-4,-4]},
                            'ground':{'translation':[0,0,0],'scale':[.5,.5,.5]}}}
        write(ASSETS / f'models/block/{name}.json',model)
        write(ASSETS / f'blockstates/{name}.json',{'variants':{
            f'facing={face}':{'model':f'atouchofclass:block/{name}','y':angle}
            for face,angle in [('north',0),('east',90),('south',180),('west',270)]}})
        write(ASSETS / f'models/item/{name}.json',{'parent':f'atouchofclass:block/{name}'})
        write(ASSETS / f'items/{name}.json',{'model':{'type':'minecraft:model','model':f'atouchofclass:item/{name}'}})
        write(RES / f'data/atouchofclass/recipe/{name}.json',{
            'type':'minecraft:crafting_shaped','category':'building','pattern':['BB'],
            'key':{'B':'atouchofclass:'+ingredient},'result':{'count':1,'id':ident}})
        write(RES / f'data/atouchofclass/loot_table/blocks/{name}.json',{
            'type':'minecraft:block','pools':[{'rolls':1,
                'conditions':[{'condition':'minecraft:survives_explosion'}],
                'entries':[{'type':'minecraft:item','name':ident}]}]})
        write(RES / f'data/atouchofclass/advancement/recipes/building_blocks/{name}.json',{
            'parent':'minecraft:recipes/root','criteria':{
                'has_brick':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'atouchofclass:'+ingredient}]}},
                'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipes':[ident]}}},
            'requirements':[['has_brick','has_the_recipe']],'rewards':{'recipes':[ident]}})

if __name__ == '__main__':
    main()
