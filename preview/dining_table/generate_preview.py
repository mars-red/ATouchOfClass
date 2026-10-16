"""Orthographic software render of the actual multipart JSON and shipped texture.

No hand-drawn replacement geometry: selects blockstate parts for each placed
block, then rasterizes their cuboids with Minecraft's default per-face UVs.
"""
from __future__ import annotations
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).absolute().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
OUTPUT = Path(__file__).absolute().with_name('oak_dining_table_2x3_preview.png')
TEXTURE = np.asarray(Image.open(ASSETS / 'textures/block/oak_dining_table.png').convert('RGB'))
EDGES = [('north', 0, -1), ('east', 1, 0), ('south', 0, 1), ('west', -1, 0)]
CORNERS = ['north_east', 'south_east', 'south_west', 'north_west']


def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('seguisb.ttf' if bold else 'segoeui.ttf'), size)


def model_elements(name):
    model = json.loads((ASSETS / f'models/{name}.json').read_text())
    if 'elements' in model:
        return model['elements']
    return model_elements(model['parent'].split(':')[1])


def geometry(layout, wood='oak'):
    parts = json.loads((ASSETS / f'blockstates/{wood}_dining_table.json').read_text())['multipart']
    boxes = []
    for x, z in sorted(layout):
        state = {key: (x + dx, z + dz) in layout for key, dx, dz in EDGES}
        for i, corner in enumerate(CORNERS):
            a, dx, dz = EDGES[i]
            b, ex, ez = EDGES[(i + 1) % 4]
            state[corner] = (not state[a] and not state[b]) or (state[a] and state[b] and (x + dx + ex, z + dz + ez) not in layout)
        for part in parts:
            if all(str(state[key]).lower() == value for key, value in part['when'].items()):
                name = part['apply']['model'].split(':')[1]
                for item in model_elements(name):
                    low, high = np.array(item['from'], float), np.array(item['to'], float)
                    offset = np.array([16 * x, 0, 16 * z])
                    boxes.append((low + offset, high + offset, low, high))
    return boxes


def render(layout, size, scale, origin, wood='oak'):
    texture = np.asarray(Image.open(ASSETS / f'textures/block/{wood}_dining_table.png').convert('RGB'))
    width, height = size
    rgb = np.full((height, width, 3), (247, 245, 240), dtype=np.uint8)
    depth = np.full((height, width), -np.inf)
    ox, oy = origin

    def project(v):
        x, y, z = v
        return np.array([ox + (x - z) * scale, oy + (x + z) * scale * 0.47 - y * scale * 1.05])

    def triangle(vertices, uv, light):
        p = np.array([project(v) for v in vertices])
        xmin, ymin = np.maximum(np.floor(p.min(axis=0)).astype(int), 0)
        xmax, ymax = np.minimum(np.ceil(p.max(axis=0)).astype(int), [width - 1, height - 1])
        if xmax < xmin or ymax < ymin: return
        yy, xx = np.mgrid[ymin:ymax + 1, xmin:xmax + 1]
        denominator = (p[1, 1] - p[2, 1]) * (p[0, 0] - p[2, 0]) + (p[2, 0] - p[1, 0]) * (p[0, 1] - p[2, 1])
        if abs(denominator) < 1e-8: return
        a = ((p[1, 1] - p[2, 1]) * (xx + .5 - p[2, 0]) + (p[2, 0] - p[1, 0]) * (yy + .5 - p[2, 1])) / denominator
        b = ((p[2, 1] - p[0, 1]) * (xx + .5 - p[2, 0]) + (p[0, 0] - p[2, 0]) * (yy + .5 - p[2, 1])) / denominator
        c = 1 - a - b
        weights = np.stack([a, b, c], axis=-1)
        distances = np.array([v[0] + v[2] + v[1] * .895 for v in vertices])
        d = weights @ distances
        region = depth[ymin:ymax + 1, xmin:xmax + 1]
        mask = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6) & (d > region)
        coords = weights @ np.asarray(uv)
        u = np.clip(np.floor(coords[..., 0]).astype(int), 0, 15)
        v = np.clip(np.floor(coords[..., 1]).astype(int), 0, 15)
        colors = np.clip(texture[v, u] * light, 0, 255).astype(np.uint8)
        rgb[ymin:ymax + 1, xmin:xmax + 1][mask] = colors[mask]
        region[mask] = d[mask]

    for low, high, local_low, local_high in geometry(layout, wood):
        x0, y0, z0 = low; x1, y1, z1 = high
        lx0, ly0, lz0 = local_low; lx1, ly1, lz1 = local_high
        faces = [
            ([(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)], [(lx0,lz0),(lx1,lz0),(lx1,lz1),(lx0,lz1)], 1.0),
            ([(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)], [(16-lz0,16-ly0),(16-lz1,16-ly0),(16-lz1,16-ly1),(16-lz0,16-ly1)], .68),
            ([(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)], [(lx0,16-ly0),(lx1,16-ly0),(lx1,16-ly1),(lx0,16-ly1)], .83),
        ]
        for vertices, uv, light in faces:
            for ids in [(0, 1, 2), (0, 2, 3)]:
                triangle([vertices[i] for i in ids], [uv[i] for i in ids], light)
    return Image.fromarray(rgb)


def main():
    image = Image.new('RGB', (1600, 1040), '#e9e5dc')
    draw = ImageDraw.Draw(image)
    draw.text((54, 35), 'Oak dining table', fill='#302b24', font=font(46, True))
    draw.text((56, 96), 'Six single blocks joined into one 2 × 3 table', fill='#71695d', font=font(25))
    image.paste(render({(x,z) for x in range(2) for z in range(3)}, (1060, 755), 11.3, (620, 290)), (35, 160))
    image.paste(render({(0,0)}, (450, 330), 7.3, (225, 180)), (1115, 160))
    draw = ImageDraw.Draw(image)
    draw.text((1140, 178), 'Single block', fill='#302b24', font=font(24, True))
    draw.text((1140, 465), 'Same side-table proportions', fill='#71695d', font=font(18))
    draw.rounded_rectangle((1115, 515, 1565, 915), radius=15, fill='#f7f5f0')
    draw.text((1140, 538), 'Oak finish', fill='#302b24', font=font(26, True))
    draw.rectangle((1140, 594, 1220, 674), fill='#b8945f')
    draw.text((1240, 603), 'Solid oak base', fill='#302b24', font=font(20, True))
    draw.text((1240, 634), '#B8945F', fill='#71695d', font=font(19))
    texture = Image.fromarray(TEXTURE).resize((80,80), Image.Resampling.NEAREST)
    image.paste(texture, (1140, 698))
    draw.text((1240, 704), '35% stripped oak grain', fill='#302b24', font=font(19, True))
    draw.text((1240, 735), 'Blended into opaque texture', fill='#71695d', font=font(17))
    draw.text((1140, 810), 'Full-height top • 3 units thick', fill='#71695d', font=font(19))
    draw.text((1140, 844), 'Four corner legs • open underneath', fill='#71695d', font=font(19))
    draw.text((60, 185), 'Connected 2 × 3 model', fill='#302b24', font=font(25, True))
    draw.text((60, 860), 'Continuous tabletop and perimeter apron; legs only at the four outside corners.', fill='#71695d', font=font(20))
    draw.text((56, 961), 'Asset preview • generated from the shipped model JSON and texture • oak prototype', fill='#71695d', font=font(20))
    image.save(OUTPUT)
    print(OUTPUT)

    woods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove',
             'cherry', 'pale_oak', 'bamboo', 'crimson', 'warped']
    gallery = Image.new('RGB', (1820, 1320), '#e9e5dc')
    draw = ImageDraw.Draw(gallery)
    draw.text((40, 30), 'Dining tables — all twelve wood finishes', fill='#302b24', font=font(43, True))
    draw.text((42, 92), 'Each shown as six joined blocks in a 2 × 3 pattern • 35% stripped grain over a solid wood base',
              fill='#71695d', font=font(24))
    for i, wood in enumerate(woods):
        x, y = 40 + (i % 4) * 440, 155 + (i // 4) * 365
        gallery.paste(Image.new('RGB', (420, 340), '#f7f5f0'), (x, y))
        draw.text((x + 20, y + 14), wood.replace('_', ' ').title(), fill='#302b24', font=font(27, True))
        gallery.paste(render({(x,z) for x in range(2) for z in range(3)}, (420, 280), 4.55, (242, 95), wood), (x, y + 55))
    draw.text((42, 1265), 'Rendered from shipped model assets and textures • matching wood varieties connect; different woods remain separate',
              fill='#71695d', font=font(21))
    output = OUTPUT.with_name('all_wood_dining_tables_preview.png')
    gallery.save(output)
    print(output)


if __name__ == '__main__': main()
