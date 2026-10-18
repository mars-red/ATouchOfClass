"""Generate kitchen sink models; runtime pairing lives in KitchenSinkBlock.

North is the front. Openings run z=1..11, y=2 with black floor y=1.
Single opening: x=2..14. Double openings: world x=2..15 and x=17..30.
The double divider spans world x=15..17 with its top lowered to y=1.
The front lip is z=-1, so the opening is two units behind the lip.
The apron is two units deep (z=-1..1) and extends to y=-5.
Double-left owns the shared faucet at local x=16; double-right has no faucet.
"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).absolute().parents[1]
MODELS = ROOT / 'src/main/resources/assets/atouchofclass/models/block'
OPENING_BACK = 11


def fit_uv(uv):
    """Translate each UV axis into its sprite without changing texel scale.

    Model coordinates may extend outside a block. Atlas UVs cannot extend
    outside 0..16: Minecraft samples adjacent sprites instead of repeating.
    All sink faces fit within one sprite, so translation is sufficient.
    """
    result = list(uv)
    for start, end in ((0, 2), (1, 3)):
        low, high = sorted((result[start], result[end]))
        assert high - low <= 16, 'Split faces larger than one texture before mapping them'
        shift = -low if low < 0 else 16 - high if high > 16 else 0
        result[start] = round(result[start] + shift, 4)
        result[end] = round(result[end] + shift, 4)
    return result


def box(name, start, end, texture='steel'):
    # Explicit UVs keep apron/faucet texels at the same scale as the deck.
    x, y, z = start
    X, Y, Z = end
    uvs = {
        'down': [x, 16-Z, X, 16-z], 'up': [x, z, X, Z],
        'north': [16-X, 16-Y, 16-x, 16-y], 'south': [x, 16-Y, X, 16-y],
        'west': [z, 16-Y, Z, 16-y], 'east': [16-Z, 16-Y, 16-z, 16-y],
    }
    return {'name': name, 'from': start, 'to': end,
            'faces': {face: {'texture': '#' + texture, 'uv': fit_uv(uv)} for face, uv in uvs.items()}}


def sink_body(configuration='single'):
    left, right = {'single': (2, 14), 'left': (2, 15), 'right': (1, 14)}[configuration]
    left_height = 1 if configuration == 'right' else 2
    right_height = 1 if configuration == 'left' else 2
    return [
        box('Front rim and five-unit apron', [0, -5, -1], [16, 2, 1]),
        box('Lowered divider right half' if configuration == 'right' else 'Left rim',
            [0, 0, 1], [left, left_height, OPENING_BACK]),
        box('Lowered divider left half' if configuration == 'left' else 'Right rim',
            [right, 0, 1], [16, right_height, OPENING_BACK]),
        box('Rear deck', [0, 0, OPENING_BACK], [16, 2, 16]),
        box('Black shallow basin floor', [left, 0, 1], [right, 1, OPENING_BACK], 'basin'),
    ]


def faucet(cx):
    parts = []
    # The original mounting plate begins at z=10.5. Align its front edge
    # exactly with the opening, translating the entire faucet together.
    offset_z = OPENING_BACK - 10.5

    def part(name, x, y, z, X, Y, Z, texture='steel'):
        parts.append(box(name, [round(cx+x, 4), round(y, 4), round(z+offset_z, 4)],
                         [round(cx+X, 4), round(Y, 4), round(Z+offset_z, 4)], texture))

    part('Faucet mounting plate', -1.6, 2, 10.5, 1.6, 2.35, 13.1)
    part('Mixer body', -.65, 2.35, 11.05, .65, 4.7, 12.35)
    part('Riser', -.32, 4.7, 11.38, .32, 11.5, 12.02)
    part('Mixer lever stem', .65, 3.4, 11.45, 1.4, 3.8, 11.85)
    part('Mixer lever', 1.1, 3.5, 11.45, 1.45, 5.1, 11.85)
    part('Sprayer support arm', -.23, 7.8, 5.5, .23, 8.25, 11.7)
    part('Sprayer cradle', -.75, 7.8, 5.25, .75, 8.25, 6.35)
    # Stepped gooseneck hose, deliberately built from Minecraft cuboids.
    points = [(11.7, 10.9), (11.7, 12.6), (11.25, 13.65),
              (10.4, 14.35), (9.3, 14.65), (8.2, 14.55),
              (7.2, 14.05), (6.4, 13.25), (5.9, 12.2), (5.8, 10.5)]
    for index, ((z, y), (Z, Y)) in enumerate(zip(points, points[1:])):
        part(f'Flexible hose segment {index+1}', -.23, min(y,Y)-.23, min(z,Z)-.23,
             .23, max(y,Y)+.23, max(z,Z)+.23, 'hose')
    # Open steel spring wraps the hose with visible gaps, rather than a solid arch.
    for index, (z, y) in enumerate([(11.7, h) for h in (7, 7.8, 8.6, 9.4, 10.2, 11, 11.8, 12.6)]
                                  + [(11.2,13.5), (10.4,14.2), (9.4,14.55),
                                     (8.4,14.5), (7.4,14.05), (6.6,13.35), (6,12.4)]):
        part(f'Spring {index+1} front', -.53, y, z-.53, .53, y+.16, z-.35)
        part(f'Spring {index+1} rear', -.53, y, z+.35, .53, y+.16, z+.53)
        part(f'Spring {index+1} left', -.53, y, z-.35, -.35, y+.16, z+.35)
        part(f'Spring {index+1} right', .35, y, z-.35, .53, y+.16, z+.35)
    part('Sprayer upper coupling', -.43, 9.7, 5.37, .43, 10.6, 6.23)
    part('Sprayer dark grip', -.45, 7.4, 5.35, .45, 9.7, 6.25, 'hose')
    part('Sprayer trigger', .45, 7.7, 5.48, .68, 9.25, 6.12)
    part('Sprayer nozzle flare', -.64, 6.85, 5.16, .64, 7.4, 6.44)
    part('Spray outlet', -.5, 6.7, 5.3, .5, 6.85, 6.3, 'hose')
    return parts


def main():
    for name, center, configuration in [('kitchen_sink', 8, 'single'),
                                       ('kitchen_sink_double_left', 16, 'left'),
                                       ('kitchen_sink_double_right', None, 'right')]:
        elements = sink_body(configuration) + (faucet(center) if center is not None else [])
        model = {'parent': 'minecraft:block/block', 'ambientocclusion': False,
                 'textures': {'particle': 'atouchofclass:block/brushed_stainless_steel',
                              'steel': 'atouchofclass:block/brushed_stainless_steel',
                              'basin': 'minecraft:block/black_concrete',
                              'hose': 'minecraft:block/gray_concrete'}, 'elements': elements}
        for element in elements:
            assert all(-16 <= a < b <= 32 for a, b in zip(element['from'], element['to']))
            assert all('cullface' not in face for face in element['faces'].values())
            assert all(0 <= value <= 16 for face in element['faces'].values() for value in face['uv'])
        (MODELS / (name + '.json')).write_text(json.dumps(model, indent=2) + '\n')
        print(f'{name}: {len(elements)} elements')


if __name__ == '__main__':
    main()
