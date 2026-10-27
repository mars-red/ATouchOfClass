"""Apply the approved oak sidelight geometry to the other wood variants.

The translucent etched-border materials are shared by every wood.
Existing blockstates and item icons remain in place.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODELS = ROOT / 'src/main/resources/assets/atouchofclass/models/block'


def generate():
    templates = {part: json.loads((MODELS / f'tall_oak_sidelight_{part}.json').read_text())
                 for part in ('bottom', 'middle', 'top')}
    woods = sorted(path.name.removeprefix('tall_').removesuffix('_sidelight_middle.json')
                   for path in MODELS.glob('tall_*_sidelight_middle.json'))
    for wood in woods:
        if wood == 'oak':
            continue
        texture = ('stripped_bamboo_block' if wood == 'bamboo' else
                   f'stripped_{wood}_stem' if wood in ('crimson', 'warped') else
                   f'stripped_{wood}_log')
        for part, template in templates.items():
            model = dict(template, textures=dict(template['textures'],
                         wood=f'minecraft:block/{texture}',
                         particle=f'minecraft:block/{texture}'))
            (MODELS / f'tall_{wood}_sidelight_{part}.json').write_text(
                json.dumps(model, indent=2) + '\n', encoding='utf-8')
    print(f'Updated {len(woods)-1} sidelight wood variants from the approved oak models.')


if __name__ == '__main__':
    generate()
