"""Generate fire pit assets and data for all sixteen concrete colours.

Models are described once as north-oriented element lists. Blockstate multipart entries rotate
them for the other sides, and the item model bakes rotated copies together into an isolated pit.
Re-running is idempotent; existing lang and tag entries are never duplicated.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data/atouchofclass'
PICKAXE_TAG = ROOT / 'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json'
LANG = ASSETS / 'lang/en_us.json'

COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
          'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
DIRS = ['north', 'east', 'south', 'west']
CORNERS = ['north_east', 'south_east', 'south_west', 'north_west']


def face(texture, cull=None):
    data = {'texture': texture}
    if cull:
        data['cullface'] = cull
    return data


def box(lo, hi, faces, texture, culls=()):
    return {'from': list(lo), 'to': list(hi),
            'faces': {name: face(texture, name if name in culls else None) for name in faces}}


C, G = '#concrete', '#gravel'
ALL = ['north', 'east', 'south', 'west', 'up', 'down']

# Concrete parts, north-oriented. Base steps in by two units; walls and posts are 4 units thick.
CONCRETE_PARTS = {
    'base_core': [box((2, 0, 2), (14, 0, 14), ['down'], C)],
    'base_face': [box((2, 0, 2), (14, 2, 2), ['north'], C)],
    'base_face_west_ext': [box((0, 0, 2), (2, 2, 2), ['north'], C)],
    'base_face_east_ext': [box((14, 0, 2), (16, 2, 2), ['north'], C)],
    'base_strip': [box((2, 0, 0), (14, 0, 2), ['down'], C)],
    'base_corner': [box((0, 0, 0), (2, 0, 2), ['down'], C),
                    box((0, 0, 0), (2, 2, 0), ['north'], C),
                    box((0, 0, 0), (0, 2, 2), ['west'], C)],
    'wall': [box((4, 2, 0), (12, 10, 4), ['north', 'south', 'up', 'down'], C, ['north'])],
    'post': [box((12, 2, 0), (16, 10, 4), ALL, C, ['north', 'east'])],
}
GRAVEL_PARTS = {
    'gravel_core': [box((4, 9, 4), (12, 9, 12), ['up'], G)],
    'gravel_strip': [box((4, 9, 0), (12, 9, 4), ['up'], G)],
    'gravel_corner': [box((12, 9, 0), (16, 9, 4), ['up'], G)],
}
FACE_ROTATION = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north'}


def rotate(elements, turns):
    result = elements
    for _ in range(turns):
        rotated = []
        for el in result:
            corners = [(x, z) for x in (el['from'][0], el['to'][0]) for z in (el['from'][2], el['to'][2])]
            moved = [(8 - (z - 8), 8 + (x - 8)) for x, z in corners]
            xs, zs = [m[0] for m in moved], [m[1] for m in moved]
            faces = {}
            for name, data in el['faces'].items():
                data = dict(data)
                if 'cullface' in data:
                    data['cullface'] = FACE_ROTATION.get(data['cullface'], data['cullface'])
                faces[FACE_ROTATION.get(name, name)] = data
            rotated.append({'from': [min(xs), el['from'][1], min(zs)],
                            'to': [max(xs), el['to'][1], max(zs)], 'faces': faces})
        result = rotated
    return result


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def blockstate(pid):
    def part(model, when=None, turns=0):
        apply = {'model': f'atouchofclass:block/{model}'}
        if turns:
            apply['y'] = 90 * turns
        entry = {'apply': apply}
        if when:
            entry['when'] = when
        return entry

    parts = [part(f'{pid}_base_core'), part('fire_pit_gravel_core')]
    for k in range(4):
        side, left, right = DIRS[k], DIRS[(k + 3) % 4], DIRS[(k + 1) % 4]
        parts += [
            part(f'{pid}_wall', {side: 'false'}, k),
            part(f'{pid}_base_face', {side: 'false'}, k),
            part(f'{pid}_base_face_west_ext', {side: 'false', left: 'true'}, k),
            part(f'{pid}_base_face_east_ext', {side: 'false', right: 'true'}, k),
            part(f'{pid}_base_strip', {side: 'true'}, k),
            part('fire_pit_gravel_strip', {side: 'true'}, k),
            part(f'{pid}_base_corner', {side: 'true', left: 'true'}, k),
            part(f'{pid}_post', {CORNERS[k]: 'false'}, k),
            part('fire_pit_gravel_corner', {CORNERS[k]: 'true'}, k),
        ]
    return {'multipart': parts}


def item_elements():
    elements = list(CONCRETE_PARTS['base_core'])
    elements += GRAVEL_PARTS['gravel_core']
    for k in range(4):
        for part in ('wall', 'base_face', 'post'):
            elements += rotate(CONCRETE_PARTS[part], k)
    return elements


def generate_color(color):
    pid = f'{color}_concrete_fire_pit'
    textures = {'concrete': f'minecraft:block/{color}_concrete', 'particle': f'minecraft:block/{color}_concrete'}
    for name, elements in CONCRETE_PARTS.items():
        write_json(ASSETS / f'models/block/{pid}_{name}.json',
                   {'textures': textures, 'elements': elements})
    write_json(ASSETS / f'blockstates/{pid}.json', blockstate(pid))
    write_json(ASSETS / f'models/item/{pid}.json', {
        'parent': 'minecraft:block/block',
        'textures': {**textures, 'gravel': 'minecraft:block/gravel'},
        'elements': item_elements(),
    })
    write_json(ASSETS / f'items/{pid}.json',
               {'model': {'type': 'minecraft:model', 'model': f'atouchofclass:item/{pid}'}})
    write_json(DATA / f'loot_table/blocks/{pid}.json', {
        'type': 'minecraft:block',
        'pools': [{'rolls': 1, 'condition': {'type': 'minecraft:survives_explosion'},
                   'entries': [{'type': 'minecraft:item', 'name': f'atouchofclass:{pid}'}]}],
    })
    write_json(DATA / f'recipe/{pid}.json', {
        'type': 'minecraft:crafting_shaped',
        'category': 'misc',
        'pattern': ['CGC', ' F '],
        'key': {'C': f'minecraft:{color}_concrete', 'G': 'minecraft:gravel', 'F': 'minecraft:campfire'},
        'result': {'count': 2, 'id': f'atouchofclass:{pid}'},
    })
    write_json(DATA / f'advancement/recipes/misc/{pid}.json', {
        'parent': 'minecraft:recipes/root',
        'criteria': {
            'has_material': {'trigger': 'minecraft:inventory_changed',
                             'conditions': {'items': [{'items': f'minecraft:{color}_concrete'}]}},
            'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked',
                               'conditions': {'recipes': [f'atouchofclass:{pid}']}},
        },
        'requirements': [['has_material', 'has_the_recipe']],
        'rewards': {'recipes': [f'atouchofclass:{pid}']},
    })
    return pid


def append_lang(entries):
    text = LANG.read_text(encoding='utf-8')
    body = text.rstrip()
    assert body.endswith('}')
    body = body[:-1].rstrip()
    additions = [f'    "{key}":  "{value}"' for key, value in entries.items() if f'"{key}"' not in text]
    if additions:
        LANG.write_text(body + ',\n' + ',\n'.join(additions) + '\n}\n', encoding='utf-8')


def append_pickaxe(ids):
    text = PICKAXE_TAG.read_text(encoding='utf-8')
    new = [i for i in ids if f'"atouchofclass:{i}"' not in text]
    if not new:
        return
    marker = text.rindex(']')
    head = text[:marker].rstrip()
    lines = ',\n'.join(f'                   "atouchofclass:{i}"' for i in new)
    PICKAXE_TAG.write_text(head + ',\n' + lines + '\n               ' + text[marker:], encoding='utf-8')


def main():
    for name, elements in GRAVEL_PARTS.items():
        write_json(ASSETS / f'models/block/fire_pit_{name}.json',
                   {'textures': {'gravel': 'minecraft:block/gravel', 'particle': 'minecraft:block/gravel'},
                    'elements': elements})
    ids = [generate_color(color) for color in COLORS]
    append_lang({f'block.atouchofclass.{pid}': ' '.join(w.capitalize() for w in pid.split('_'))
                 for pid in ids})
    append_pickaxe(ids)


if __name__ == '__main__':
    main()
