"""Generate the oak patio door assets; frame texture comes directly from vanilla.

The three sections form one 16 x 48 x 3 leaf. No rail crosses the pane.
Hinge-specific models keep both lever handles opposite the hinge.
"""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).absolute().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data'
NAME = 'tall_oak_patio_door'


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


def generate():
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
        glass.save(ASSETS / f'textures/block/{NAME}_glass_{section}.png')
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
                     'textures': {'wood': 'minecraft:block/stripped_oak_log',
                                  'glass': f'atouchofclass:block/{NAME}_glass_{section}',
                                  'metal': 'minecraft:block/iron_block',
                                  'particle': 'minecraft:block/stripped_oak_log'},
                     'elements': elements}
            write(ASSETS / f'models/block/{NAME}_{section}_{hinge}.json', model)
            for facing, rotation in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                for opened in (False, True):
                    angle = (rotation + ((270 if hinge == 'left' else 90) if opened else 0)) % 360
                    variants.append({'when': {'part': part, 'hinge': hinge, 'open': opened, 'facing': facing},
                                     'apply': {'model': f'atouchofclass:block/{NAME}_{section}_{hinge}', 'y': angle}})
    write(ASSETS / f'blockstates/{NAME}.json', {'multipart': variants})
    write(ASSETS / f'items/{NAME}.json', {'model': {'type': 'minecraft:model', 'model': f'atouchofclass:item/{NAME}'}})
    write(ASSETS / f'models/item/{NAME}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'atouchofclass:item/{NAME}'}})
    loot = json.loads((DATA / 'atouchofclass/loot_table/blocks/tall_oak_door.json').read_text())
    loot['pools'][0]['entries'][0]['name'] = 'atouchofclass:' + NAME
    write(DATA / f'atouchofclass/loot_table/blocks/{NAME}.json', loot)
    write(DATA / f'atouchofclass/recipe/{NAME}.json', {
        'type': 'minecraft:crafting_shaped', 'category': 'building',
        'key': {'W': 'minecraft:oak_planks', 'G': 'minecraft:glass'},
        'pattern': ['WGW', 'WGW', 'WGW'], 'result': {'count': 4, 'id': 'atouchofclass:' + NAME}})
    advancement = json.loads((DATA / 'atouchofclass/advancement/recipes/misc/tall_oak_door.json').read_text().replace('tall_oak_door', NAME))
    write(DATA / f'atouchofclass/advancement/recipes/misc/{NAME}.json', advancement)


if __name__ == '__main__':
    generate()
