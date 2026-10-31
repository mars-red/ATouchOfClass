"""Check trim resource boxes against the placement/collision coordinate frame."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'

# World-space right when looking toward each supporting face.
RIGHT = {'north': 'west', 'south': 'east', 'east': 'north',
         'west': 'south', 'up': 'east', 'down': 'east'}
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west',
            'west': 'east', 'up': 'down', 'down': 'up'}
AXIS_SIGN = {'east': (0, 1), 'west': (0, -1), 'up': (1, 1),
             'down': (1, -1), 'south': (2, 1), 'north': (2, -1)}


def check_models():
    variants = json.loads((ASSETS / 'blockstates/wall_tile_trim_strip.json').read_text())['variants']
    checked = 0
    for facing in RIGHT:
        top = {'up': 'north', 'down': 'south'}.get(facing, 'up')
        edges = {'top': top, 'bottom': OPPOSITE[top],
                 'right': RIGHT[facing], 'left': OPPOSITE[RIGHT[facing]]}
        for edge, direction in edges.items():
            state = f'facing={facing},edge={edge}'
            variant = variants[state]
            assert not variant.get('x', 0) and not variant.get('y', 0), state
            model = variant['model'].split(':', 1)[1]
            elements = json.loads((ASSETS / 'models' / (model + '.json')).read_text())['elements']
            assert len(elements) == 1, state
            low, high = [0, 0, 0], [16, 16, 16]
            for normal, size in ((OPPOSITE[facing], .5), (direction, 1)):
                axis, sign = AXIS_SIGN[normal]
                if sign > 0:
                    low[axis] = 16 - size
                else:
                    high[axis] = size
            element = elements[0]
            assert element['from'] == low and element['to'] == high, (
                f'{state}: rendered box differs from placement/collision bounds')
            checked += 1
    print(f'All {checked} trim models match placement/collision bounds.')


if __name__ == '__main__':
    check_models()
