"""Generate thin framed transoms for every vanilla wood in the shared catalog."""
import json
from pathlib import Path
from PIL import Image, ImageDraw
from generate_tall_patio_door import WOODS, box, write

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data'

def generate():
    glass = Image.new('RGBA', (16, 16))
    pen = ImageDraw.Draw(glass)
    pen.line((5, 9, 8, 6), fill=(193, 221, 225, 255))
    pen.line((6, 10, 8, 8), fill=(225, 242, 245, 255))
    glass.save(ASSETS / 'textures/block/transom_glass.png')
    for wood in WOODS:
        name = wood + '_transom'
        timber = ('stripped_bamboo_block' if wood == 'bamboo' else
                  f'stripped_{wood}_stem' if wood in ('crimson', 'warped') else f'stripped_{wood}_log')
        variants = {}
        for join in ('single', 'left', 'right'):
            elements = [box([0, 0, 0], [16, 1, 3], 'wood'), box([0, 15, 0], [16, 16, 3], 'wood')]
            if join != 'right': elements.append(box([0, 1, 0], [1, 15, 3], 'wood'))
            if join != 'left': elements.append(box([15, 1, 0], [16, 15, 3], 'wood'))
            pane = box([0 if join == 'right' else 1, 1, 1], [16 if join == 'left' else 15, 15, 2], 'glass')
            pane['faces'] = {k:v for k,v in pane['faces'].items() if k in ('north','south')}
            elements.append(pane)
            write(ASSETS / f'models/block/{name}_{join}.json', {
                'parent':'minecraft:block/block', 'render_type':'minecraft:cutout', 'ambientocclusion':False,
                'textures':{'wood':f'minecraft:block/{timber}', 'glass':'atouchofclass:block/transom_glass',
                            'particle':f'minecraft:block/{timber}'}, 'elements':elements})
            for facing, angle in (('north',0),('east',90),('south',180),('west',270)):
                variants[f'facing={facing},join={join}'] = {'model':f'atouchofclass:block/{name}_{join}', 'y':angle}
        write(ASSETS / f'blockstates/{name}.json', {'variants':variants})
        write(ASSETS / f'models/item/{name}.json', {'parent':f'atouchofclass:block/{name}_single'})
        write(ASSETS / f'items/{name}.json', {'model':{'type':'minecraft:model','model':f'atouchofclass:item/{name}'}})
        loot = json.loads((DATA / 'atouchofclass/loot_table/blocks/brick_window_ledge.json').read_text())
        loot['pools'][0]['entries'][0]['name'] = 'atouchofclass:' + name
        write(DATA / f'atouchofclass/loot_table/blocks/{name}.json', loot)
        write(DATA / f'atouchofclass/recipe/{name}.json', {
            'type':'minecraft:crafting_shaped','category':'building',
            'key':{'P':f'minecraft:{wood}_planks','G':'minecraft:glass'},
            'pattern':['PGP'],'result':{'count':4,'id':'atouchofclass:'+name}})
        advancement = (DATA / 'atouchofclass/advancement/recipes/misc/tall_oak_door.json').read_text()
        write(DATA / f'atouchofclass/advancement/recipes/misc/{name}.json',
              json.loads(advancement.replace('tall_oak_door',name).replace('minecraft:oak_planks',f'minecraft:{wood}_planks')))
    langpath = ASSETS / 'lang/en_us.json'
    text = langpath.read_text(encoding='utf-8')
    lang = json.loads(text)
    additions = {}
    for wood in WOODS:
        for kind in ('block','item'):
            key = f'{kind}.atouchofclass.{wood}_transom'
            if key not in lang: additions[key] = wood.replace('_',' ').title() + ' Transom'
    if additions:
        end = text.rfind('}')
        text = text[:end].rstrip() + ',\n' + ',\n'.join('    '+json.dumps(k)+': '+json.dumps(v) for k,v in additions.items()) + '\n}\n'
        langpath.write_text(text,encoding='utf-8')
    tagpath = DATA / 'minecraft/tags/block/mineable/axe.json'
    tag = json.loads(tagpath.read_text())
    for wood in WOODS:
        entry = 'atouchofclass:'+wood+'_transom'
        if entry not in tag['values']: tag['values'].append(entry)
    write(tagpath,tag)

if __name__ == '__main__': generate()
