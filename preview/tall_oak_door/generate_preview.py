"""Standalone oak door proposal. Render the same JSON geometry supplied for review.

Run from the repository root with python preview/tall_oak_door/generate_preview.py.
Production resources are intentionally untouched.
"""
import sys
sys.dont_write_bytecode = True
import importlib.util
import json
from pathlib import Path
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
spec = importlib.util.spec_from_file_location('patio_preview', ROOT / 'preview/tall_patio_door/generate_preview.py')
renderer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(renderer)


def box(a, b, material='wood'):
    x, y, z = a
    X, Y, Z = b
    # Local block UVs keep vanilla grain at its native scale on every section.
    uv = {'south': [x, 16-Y, X, 16-y], 'north': [16-X, 16-Y, 16-x, 16-y],
          'east': [16-Z, 16-Y, 16-z, 16-y], 'west': [z, 16-Y, Z, 16-y],
          'up': [x, z, X, Z], 'down': [x, 16-Z, X, 16-z]}
    return {'from': a, 'to': b, 'faces': {f: {'texture': '#'+material, 'uv': v} for f, v in uv.items()}}


def generate_models():
    elements = []
    def add(a, b, material='wood'):
        elements.append(box(a, b, material))
    # Full-height stiles, central mullion, and four structural rails.
    for x, X in ((0, 2), (7.5, 8.5), (14, 16)):
        add([x, 0, 0], [X, 48, 3])
    for y, Y in ((0, 3), (14.5, 17), (30, 32.5), (45, 48)):
        for x, X in ((2, 7.5), (8.5, 14)):
            add([x, y, 0], [X, Y, 3])
    for x, X in ((2, 7.5), (8.5, 14)):
        for y, Y in ((3, 14.5), (17, 30), (32.5, 45)):
            # A shallow inner frame surrounds a further-recessed solid panel.
            w = 0.5
            for a, b in (([x, y, .375], [x+w, Y, 2.625]),
                         ([X-w, y, .375], [X, Y, 2.625]),
                         ([x+w, y, .375], [X-w, y+w, 2.625]),
                         ([x+w, Y-w, .375], [X-w, Y, 2.625]),
                         ([x+w, y+w, .75], [X-w, Y-w, 2.25])):
                add(a, b)
    # Squared escutcheons, short spindles, and faceted-looking square knobs.
    for a, b in (([14.25, 21, -.125], [15.75, 24, 0]),
                 ([14.25, 21, 3], [15.75, 24, 3.125]),
                 ([14.75, 22, -.625], [15.25, 22.5, -.125]),
                 ([14.75, 22, 3.125], [15.25, 22.5, 3.625]),
                 ([14.375, 21.625, -1.25], [15.625, 22.875, -.625]),
                 ([14.375, 21.625, 3.625], [15.625, 22.875, 4.25])):
        add(a, b, 'metal')
    for part, section in enumerate(('bottom', 'middle', 'top')):
        clipped = []
        for e in elements:
            a, b = e['from'].copy(), e['to'].copy()
            lo, hi = max(a[1], part*16), min(b[1], (part+1)*16)
            if lo >= hi:
                continue
            a[1], b[1] = lo-part*16, hi-part*16
            element = box(a, b, e['faces']['south']['texture'][1:])
            if part and lo == part*16:
                element['faces'].pop('down')
            if part < 2 and hi == (part+1)*16:
                element['faces'].pop('up')
            clipped.append(element)
        model = {'parent': 'minecraft:block/block', 'textures': {
            'wood': 'minecraft:block/stripped_oak_log', 'metal': 'minecraft:block/iron_block',
            'particle': 'minecraft:block/stripped_oak_log'}, 'elements': clipped}
        path = HERE / 'models/block' / f'tall_oak_door_{section}.json'
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(model, indent=2)+'\n')
    return elements


def main():
    generate_models()
    # Reuse the repository's textured cuboid renderer with this proposal's paths.
    renderer.ASSETS = HERE
    source = (ROOT / 'preview/tall_patio_door/generate_preview.py').read_text()
    function = source[source.index('def render('):source.index('\ndef font(')]
    function = function.replace('tall_{wood}_patio_door_{section}_{hinge}.json', 'tall_oak_door_{section}.json')
    # Approximate ambient occlusion inside the modeled recesses.
    function = function.replace("{'south':1, 'east':0.68, 'up':1.1}[side]", "{'south': (0.87 if Z == 2.25 else 0.96 if Z == 2.625 else 1), 'east':0.68, 'up':1.1}[side]")
    exec(function, renderer.__dict__)
    sheet = Image.new('RGBA', (1200, 1000), '#e9e5dd')
    pen = ImageDraw.Draw(sheet)
    pen.text((40, 28), 'Tall oak door | Model preview', font=renderer.font(36, True), fill='#302b25')
    pen.text((42, 82), 'Vanilla stripped oak / Six recessed panels / Modeled iron knobs on both sides', font=renderer.font(21), fill='#686157')
    for left, title in ((30, 'FRONT'), (610, 'ANGLED')):
        pen.rounded_rectangle((left, 140, left+560, 910), radius=18, fill='#f7f5f0')
        pen.text((left+24, 162), title, font=renderer.font(20, True), fill='#71695e')
    sheet.alpha_composite(renderer.render(sheet.size, (200, 850), 12.5, 0, False))
    sheet.alpha_composite(renderer.render(sheet.size, (785, 822), 12.5, 28, False))
    pen.text((42, 942), '1 block wide x 3 blocks tall | 3/16-block leaf | Preview only; game assets unchanged', font=renderer.font(22), fill='#403a32')
    sheet.convert('RGB').save(HERE / 'tall_oak_door_preview.png')
    print('Generated three valid section models and textured preview.')


if __name__ == '__main__':
    main()
