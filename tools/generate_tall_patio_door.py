"""Generate all vanilla wood patio door assets; frame texture comes directly from vanilla.

The three sections form one 16 x 48 x 3 leaf. No rail crosses the pane.
Hinge-specific models keep both lever handles opposite the hinge.
"""
import json
import re
import importlib.util
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).absolute().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data'
CATALOG = ROOT / 'src/main/java/com/digitaldownpour/atouchofclass/PatioDoorVariants.java'
WOODS = re.findall(r'new Definition\("([^"]+)"', CATALOG.read_text())


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def box(start, end, texture):
    x, y, z = start
    X, Y, Z = end
    # Explicit UVs avoid stretching the stripped oak grain across thin rails.
    uv = {'north': [16-X, 16-Y, 16-x, 16-y],
          'south': [x, 16-Y, X, 16-y],
          'west': [z, 16-Y, Z, 16-y],
          'east': [16-Z, 16-Y, 16-z, 16-y],
          'up': [x, z, X, Z], 'down': [x, 16-Z, X, 16-z]}
    return {'from': start, 'to': end,
            'faces': {face: {'texture': '#' + texture, 'uv': coords} for face, coords in uv.items()}}


def generate_variant(wood):
    name = f'tall_{wood}_patio_door'
    texture = ('stripped_bamboo_block' if wood == 'bamboo' else
               f'stripped_{wood}_stem' if wood in ('crimson', 'warped') else f'stripped_{wood}_log')
    variants = []
    for part, section in enumerate(('bottom', 'middle', 'top')):
        # Glass has sparse opaque vanilla-style glints, with a clear background.
        # Only the outer wood frames the window; no borders at section boundaries.
        glass = Image.new('RGBA', (16, 16))
        pen = ImageDraw.Draw(glass)
        if part == 0:
            pen.line((4, 7, 7, 4), fill=(193, 221, 225, 255))
        if part == 2:
            pen.line((9, 10, 12, 7), fill=(193, 221, 225, 255))
            pen.line((8, 8, 10, 6), fill=(225, 242, 245, 255))
        glass.save(ASSETS / f'textures/block/{name}_glass_{section}.png')
        for hinge in ('left', 'right'):
            elements = [box([0, 0, 0], [2, 16, 3], 'wood'),
                        box([14, 0, 0], [16, 16, 3], 'wood')]
            if part == 0:
                elements.append(box([2, 0, 0], [14, 2, 3], 'wood'))
            if part == 2:
                elements.append(box([2, 14, 0], [14, 16, 3], 'wood'))
            pane = box([2, 2 if part == 0 else 0, 1],
                       [14, 14 if part == 2 else 16, 2], 'glass')
            # Side faces are internal to the frame or the adjacent glass section.
            pane['faces'] = {k: v for k, v in pane['faces'].items() if k in ('north', 'south')}
            elements.append(pane)
            if part == 1:
                # Carve matching recesses in the latch stile. The core separates
                # the two pockets; the levers remain visible within z=0..3.
                elements.pop(1)
                elements.extend([
                    box([14, 0, 0], [16, 4.75, 3], 'wood'),
                    box([14, 8.25, 0], [16, 16, 3], 'wood'),
                    box([14, 4.75, 0], [14.125, 8.25, 3], 'wood'),
                    box([15.875, 4.75, 0], [16, 8.25, 3], 'wood'),
                    box([14.125, 4.75, 1], [15.875, 8.25, 2], 'wood')])
                for front in (False, True):
                    a, b = (2, 2.25) if front else (0.75, 1)
                    elements.append(box([14.25, 5, a], [15.75, 8, b], 'metal'))
                    a, b = (2.25, 2.5) if front else (0.5, 0.75)
                    elements.append(box([14.5, 6, a], [15.5, 7, b], 'metal'))
                    a, b = (2.5, 3) if front else (0, 0.5)
                    elements.append(box([11.5, 6, a], [15.5, 6.75, b], 'metal'))
            if hinge == 'right':
                for element in elements:
                    element['from'][0], element['to'][0] = 16-element['to'][0], 16-element['from'][0]
            # Omit hidden section-end faces so the frame has no internal seams.
            for element in elements:
                if part < 2 and element['to'][1] == 16:
                    element['faces'].pop('up', None)
                if part > 0 and element['from'][1] == 0:
                    element['faces'].pop('down', None)
            model = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
                     'textures': {'wood': f'minecraft:block/{texture}',
                                  'glass': f'atouchofclass:block/{name}_glass_{section}',
                                  'metal': 'minecraft:block/iron_block',
                                  'particle': f'minecraft:block/{texture}'},
                     'elements': elements}
            write(ASSETS / f'models/block/{name}_{section}_{hinge}.json', model)
            for facing, rotation in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                for opened in (False, True):
                    angle = (rotation + ((270 if hinge == 'left' else 90) if opened else 0)) % 360
                    variants.append({'when': {'part': part, 'hinge': hinge, 'open': opened, 'facing': facing},
                                     'apply': {'model': f'atouchofclass:block/{name}_{section}_{hinge}', 'y': angle}})
    write(ASSETS / f'blockstates/{name}.json', {'multipart': variants})
    write(ASSETS / f'items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'atouchofclass:item/{name}'}})
    write(ASSETS / f'models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'atouchofclass:item/{name}'}})
    loot = json.loads((DATA / 'atouchofclass/loot_table/blocks/tall_oak_door.json').read_text())
    loot['pools'][0]['entries'][0]['name'] = 'atouchofclass:' + name
    write(DATA / f'atouchofclass/loot_table/blocks/{name}.json', loot)
    write(DATA / f'atouchofclass/recipe/{name}.json', {
        'type': 'minecraft:crafting_shaped', 'category': 'building',
        'key': {'W': f'minecraft:{wood}_planks', 'G': 'minecraft:glass'},
        'pattern': ['WGW', 'WGW', 'WGW'], 'result': {'count': 4, 'id': 'atouchofclass:' + name}})
    advancement = json.loads((DATA / 'atouchofclass/advancement/recipes/misc/tall_oak_door.json').read_text().replace('tall_oak_door', name).replace('minecraft:oak_planks', f'minecraft:{wood}_planks'))
    write(DATA / f'atouchofclass/advancement/recipes/misc/{name}.json', advancement)


def generate():
    for wood in WOODS:
        generate_variant(wood)
    # Generate the centered item textures as part of the same command.
    spec = importlib.util.spec_from_file_location('patio_preview', ROOT / 'preview/tall_patio_door/generate_preview.py')
    preview = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(preview)
    for wood in WOODS:
        preview.generate_item_icon(wood)
    preview.generate_variants_preview(WOODS)
    lang_path = ASSETS / 'lang/en_us.json'
    lang = lang_path.read_text(encoding='utf-8')
    additions = []
    for wood in WOODS:
        name = f'tall_{wood}_patio_door'
        label = 'Tall ' + wood.replace('_', ' ').title() + ' Patio Door'
        for kind in ('block', 'item'):
            key = f'{kind}.atouchofclass.{name}'
            if f'"{key}"' not in lang:
                additions.append('    ' + json.dumps(key) + ': ' + json.dumps(label) + ',')
    if additions:
        lang_path.write_text(lang.replace('{', '{\n' + '\n'.join(additions) + '\n', 1), encoding='utf-8')
    for kind, path in (('item', 'doors'), ('block', 'mineable/axe')):
        tag_path = DATA / f'minecraft/tags/{kind}/{path}.json'
        tag_text = tag_path.read_text()
        tag = json.loads(tag_text)
        missing = []
        for wood in WOODS:
            value = f'atouchofclass:tall_{wood}_patio_door'
            if value not in tag['values']:
                missing.append(value)
        if missing:
            # Preserve formatting and unrelated entries in the existing tags.
            end = tag_text.rfind(']')
            prefix = tag_text[:end].rstrip()
            indent = re.findall(r'\n([ \t]+)"[^"\n]+"', prefix)[-1]
            prefix += ',' + ''.join('\n' + indent + json.dumps(value) + (',' if i < len(missing)-1 else '')
                                    for i, value in enumerate(missing))
            tag_path.write_text(prefix + tag_text[len(tag_text[:end].rstrip()):], encoding='utf-8')
    print(f'Generated {len(WOODS)} patio door variants, including centered item icons.')


if __name__ == '__main__':
    generate()
