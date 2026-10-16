"""Generate the oak prototype. Add wood definitions here after the design is approved.

The finish is base * (1-opacity) + vanilla stripped grain * opacity. Baking the
blend preserves the requested appearance while keeping the furniture opaque.
Requires Pillow and a local Minecraft 1.21.4 client jar (override with --client-jar).
"""
from __future__ import annotations

import argparse
import io
import json
from pathlib import Path
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
DATA = ROOT / 'src/main/resources/data/atouchofclass'
CLIENT_JAR = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.4_client.jar'
BASE_COLOR = '#b8945f'
GRAIN_OPACITY = 0.35
EDGES = ['north', 'east', 'south', 'west']
CORNERS = ['north_east', 'south_east', 'south_west', 'north_west']
# Oak remains the only enabled prototype. Expand this when variants are enabled.
ENABLED_WOODS = ('oak',)


def variant_recipe(wood, oak_recipe):
    """Clone the entire hand-edited oak recipe, swapping only material/output IDs."""
    stripped_wood = {
        'bamboo': 'stripped_bamboo_block',
        'crimson': 'stripped_crimson_hyphae',
        'warped': 'stripped_warped_hyphae',
    }.get(wood, f'stripped_{wood}_wood')
    replacements = {
        'minecraft:oak_planks': f'minecraft:{wood}_planks',
        'minecraft:oak_slab': f'minecraft:{wood}_slab',
        'minecraft:stripped_oak_wood': f'minecraft:{stripped_wood}',
        'atouchofclass:oak_dining_table': f'atouchofclass:{wood}_dining_table',
    }

    def replace(value):
        if isinstance(value, dict):
            return {key: replace(child) for key, child in value.items()}
        if isinstance(value, list):
            return [replace(child) for child in value]
        return replacements.get(value, value) if isinstance(value, str) else value

    return replace(oak_recipe)


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')


def element(x0, y0, z0, x1, y1, z1):
    return {'from': [x0, y0, z0], 'to': [x1, y1, z1],
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


def model(elements):
    return {'parent': 'minecraft:block/block', 'ambientocclusion': True,
            'textures': {'wood': 'atouchofclass:block/oak_dining_table', 'particle': '#wood'}, 'elements': elements}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--client-jar', type=Path, default=CLIENT_JAR)
    args = parser.parse_args()
    # Read before generating anything. Never overwrite the hand-tuned source.
    oak_recipe = json.loads((DATA / 'recipe/oak_dining_table.json').read_text(encoding='utf-8'))
    with zipfile.ZipFile(args.client_jar) as jar:
        grain = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/block/stripped_oak_log.png'))).convert('RGB')
    texture = Image.blend(Image.new('RGB', grain.size, BASE_COLOR), grain, GRAIN_OPACITY)
    texture_path = ASSETS / 'textures/block/oak_dining_table.png'
    texture_path.parent.mkdir(parents=True, exist_ok=True)
    texture.save(texture_path)
    parts = []
    for mask in range(16):
        name = f'oak_dining_table/body_{mask}'
        write(ASSETS / f'models/block/{name}.json', model(body(mask)))
        parts.append({'when': {edge: str(bool(mask & (1 << i))).lower() for i, edge in enumerate(EDGES)},
                      'apply': {'model': f'atouchofclass:block/{name}'}})
    for i, corner in enumerate(CORNERS):
        name = f'oak_dining_table/leg_{corner}'
        write(ASSETS / f'models/block/{name}.json', model([leg(i)]))
        parts.append({'when': {corner: 'true'}, 'apply': {'model': f'atouchofclass:block/{name}'}})
    write(ASSETS / 'blockstates/oak_dining_table.json', {'multipart': parts})
    write(ASSETS / 'models/block/oak_dining_table.json', model(body(0) + [leg(i) for i in range(4)]))
    display = json.loads((ASSETS / 'models/item/side_table.json').read_text())['display']
    write(ASSETS / 'models/item/oak_dining_table.json', {
        'parent': 'atouchofclass:block/oak_dining_table', 'gui_light': 'side', 'display': display})
    write(ASSETS / 'items/oak_dining_table.json', {'model': {'type': 'minecraft:model', 'model': 'atouchofclass:item/oak_dining_table'}})
    write(DATA / 'loot_table/blocks/oak_dining_table.json', {
        'type': 'minecraft:block', 'pools': [{'rolls': 1, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
        'entries': [{'type': 'minecraft:item', 'name': 'atouchofclass:oak_dining_table'}]}]})
    for wood in ENABLED_WOODS:
        if wood != 'oak':
            write(DATA / f'recipe/{wood}_dining_table.json', variant_recipe(wood, oak_recipe))
    write(DATA / 'advancement/recipes/decorations/oak_dining_table.json', {
        'parent': 'minecraft:recipes/root', 'criteria': {
            'has_wood': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'minecraft:stripped_oak_wood'}]}},
            'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipe': 'atouchofclass:oak_dining_table'}}},
        'requirements': [['has_wood', 'has_the_recipe']], 'rewards': {'recipes': ['atouchofclass:oak_dining_table']}})
    path = ASSETS / 'lang/en_us.json'
    contents = path.read_text(encoding='utf-8')
    key = 'block.atouchofclass.oak_dining_table'
    if f'"{key}"' not in contents:
        end = contents.rfind('}')
        path.write_text(contents[:end].rstrip() + f',\n    "{key}": "Oak Dining Table"\n' + contents[end:], encoding='utf-8')
    path = ROOT / 'src/main/resources/data/minecraft/tags/block/mineable/axe.json'
    contents = path.read_text(encoding='utf-8')
    if 'atouchofclass:oak_dining_table' not in contents:
        end = contents.rfind(']')
        path.write_text(contents[:end].rstrip() + ',\n    "atouchofclass:oak_dining_table"\n  ' + contents[end:], encoding='utf-8')
    print('Generated oak dining table; base:', BASE_COLOR, 'grain opacity:', GRAIN_OPACITY)


if __name__ == '__main__':
    main()
