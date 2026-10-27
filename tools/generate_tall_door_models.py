"""Propagate the approved oak tall-door geometry and blockstates to every wood.

Oak's two middle models encode hardware handedness. Open states exchange them
because blockstate rotations turn around the block center rather than the hinge.
Item icons and patio doors are deliberately outside this generator's scope.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
MODELS = ASSETS / 'models/block'


def generate():
    templates = {part: json.loads((MODELS / f'tall_oak_door_{part}.json').read_text())
                 for part in ('bottom', 'middle', 'middle_right', 'top')}
    for part, model in templates.items():
        for element in model['elements']:
            # The recessed panel core spans z=.75..2.25; trim and rails
            # are separate cuboids. Tint only its two visible broad faces.
            if element['from'][2] == 0.75 and element['to'][2] == 2.25:
                for side in ('north', 'south'):
                    element['faces'][side]['tintindex'] = 0
        (MODELS / f'tall_oak_door_{part}.json').write_text(
            json.dumps(model, indent=2) + '\n', encoding='utf-8')
    states = (ASSETS / 'blockstates/tall_oak_door.json').read_text()
    woods = sorted(path.name.removeprefix('tall_').removesuffix('_door_middle.json')
                   for path in MODELS.glob('tall_*_door_middle.json'))
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
            (MODELS / f'tall_{wood}_door_{part}.json').write_text(
                json.dumps(model, indent=2) + '\n', encoding='utf-8')
        (ASSETS / f'blockstates/tall_{wood}_door.json').write_text(
            states.replace('tall_oak_door', f'tall_{wood}_door'), encoding='utf-8')
    print(f'Updated {len(woods)-1} wood variants from the approved oak models.')


if __name__ == '__main__':
    generate()
