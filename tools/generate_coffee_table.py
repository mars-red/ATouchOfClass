"""Generate coffee-table assets and data for every dining-table wood.

Coffee tables reuse the dining-table geometry lowered by six pixels (10 pixels
tall) and the matching dining-table textures. Run after generate_dining_table.py.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data/atouchofclass'
EDGES = ['north', 'east', 'south', 'west']
CORNERS = ['north_east', 'south_east', 'south_west', 'north_west']
WOODS = ['oak', 'spruce', 'birch', 'poplar', 'jungle', 'acacia', 'dark_oak', 'mangrove',
         'cherry', 'pale_oak', 'bamboo', 'crimson', 'warped']
DROP = 6


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')


def element(x0, y0, z0, x1, y1, z1):
    return {'from': [x0, y0 - DROP if y0 else 0, z0], 'to': [x1, y1 - DROP, z1],
            'faces': {face: {'texture': '#wood'} for face in ['down', 'up', *EDGES]}}


def body(mask):
    n, e, s, w = [bool(mask & (1 << i)) for i in range(4)]
    result = [element(1, 13, 0, 15, 16, 16), element(0, 13, 1, 1, 16, 15), element(15, 13, 1, 16, 16, 15)]
    for fill, x, z in [(n or w, 0, 0), (n or e, 15, 0), (s or w, 0, 15), (s or e, 15, 15)]:
        if fill:
            result.append(element(x, 13, z, x + 1, 16, z + 1))
    if not n: result.append(element(0 if w else 3.25, 11, 2.25, 16 if e else 12.75, 13, 3.25))
    if not s: result.append(element(0 if w else 3.25, 11, 12.75, 16 if e else 12.75, 13, 13.75))
    if not w: result.append(element(2.25, 11, 0 if n else 3.25, 3.25, 13, 16 if s else 12.75))
    if not e: result.append(element(12.75, 11, 0 if n else 3.25, 13.75, 13, 16 if s else 12.75))
    return result


def leg(i):
    x, z = (12 if i < 2 else 2), (2 if i in (0, 3) else 12)
    return element(x, 0, z, x + 2, 13, z + 2)


def model(wood, elements):
    return {'parent': 'minecraft:block/block', 'ambientocclusion': True,
            'textures': {'wood': f'atouchofclass:block/{wood}_dining_table', 'particle': '#wood'},
            'elements': elements}


def main():
    for wood in WOODS:
        name = f'{wood}_coffee_table'
        parts = []
        for mask in range(16):
            part = f'{name}/body_{mask}'
            write(ASSETS / f'models/block/{part}.json', model(wood, body(mask)))
            parts.append({'when': {edge: str(bool(mask & (1 << i))).lower() for i, edge in enumerate(EDGES)},
                          'apply': {'model': f'atouchofclass:block/{part}'}})
        for i, corner in enumerate(CORNERS):
            part = f'{name}/leg_{corner}'
            write(ASSETS / f'models/block/{part}.json', model(wood, [leg(i)]))
            parts.append({'when': {corner: 'true'}, 'apply': {'model': f'atouchofclass:block/{part}'}})
        write(ASSETS / f'blockstates/{name}.json', {'multipart': parts})
        write(ASSETS / f'models/block/{name}.json', model(wood, body(0) + [leg(i) for i in range(4)]))
        display = json.loads((ASSETS / f'models/item/{wood}_dining_table.json').read_text(encoding='utf-8'))['display']
        write(ASSETS / f'models/item/{name}.json', {
            'parent': f'atouchofclass:block/{name}', 'gui_light': 'side', 'display': display})
        write(ASSETS / f'items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'atouchofclass:item/{name}'}})
        write(DATA / f'loot_table/blocks/{name}.json', {
            'type': 'minecraft:block', 'pools': [{'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
            'entries': [{'type': 'minecraft:item', 'name': f'atouchofclass:{name}'}]}]})
        key = json.loads((DATA / f'recipe/{wood}_dining_table.json').read_text(encoding='utf-8'))['key']
        write(DATA / f'recipe/{name}.json', {
            'type': 'minecraft:crafting_shaped', 'category': 'building', 'pattern': ['PWP'], 'key': key,
            'result': {'id': f'atouchofclass:{name}', 'count': 4}})
        write(DATA / f'advancement/recipes/decorations/{name}.json', {
            'parent': 'minecraft:recipes/root', 'criteria': {
                'has_wood': {'trigger': 'minecraft:inventory_changed',
                             'conditions': {'items': [{'items': sorted(set(key.values()))}]}},
                'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked',
                                   'conditions': {'recipes': [f'atouchofclass:{name}']}}},
            'requirements': [['has_wood', 'has_the_recipe']], 'rewards': {'recipes': [f'atouchofclass:{name}']}})

    path = ASSETS / 'lang/en_us.json'
    contents = path.read_text(encoding='utf-8')
    additions = [f'    "block.atouchofclass.{w}_coffee_table": "{w.replace("_", " ").title()} Coffee Table"'
                 for w in WOODS if f'"block.atouchofclass.{w}_coffee_table"' not in contents]
    if additions:
        end = contents.rfind('}')
        path.write_text(contents[:end].rstrip() + ',\n' + ',\n'.join(additions) + '\n' + contents[end:], encoding='utf-8')
    path = ROOT / 'src/main/resources/data/minecraft/tags/block/mineable/axe.json'
    contents = path.read_text(encoding='utf-8')
    additions = [f'    "atouchofclass:{w}_coffee_table"' for w in WOODS
                 if f'"atouchofclass:{w}_coffee_table"' not in contents]
    if additions:
        end = contents.rfind(']')
        path.write_text(contents[:end].rstrip() + ',\n' + ',\n'.join(additions) + '\n  ' + contents[end:], encoding='utf-8')
    print('Generated coffee tables:', ', '.join(WOODS))


if __name__ == '__main__':
    main()
