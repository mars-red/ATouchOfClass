"""Preview-only oak dining chair model and orthographic renders.

Writes only to this preview directory. Uses vanilla stripped oak side grain on
every face, as stripped oak wood does. Nothing is registered or added to the mod.
"""
from __future__ import annotations
import argparse
import io
import json
from pathlib import Path
import zipfile
import numpy as np
from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).absolute().parent
CLIENT_JAR = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.4_client.jar'
FACES = ['down', 'up', 'north', 'south', 'west', 'east']


def element(name, bounds):
    return {'name': name, 'from': bounds[:3], 'to': bounds[3:],
            'faces': {face: {'texture': '#wood'} for face in FACES}}


def chair_model():
    parts = [
        ('Rear left post', (2, 0, 2, 4, 24, 4)),
        ('Rear right post', (12, 0, 2, 14, 24, 4)),
        ('Front left leg', (2, 0, 12, 4, 8, 14)),
        ('Front right leg', (12, 0, 12, 14, 8, 14)),
        ('Front apron', (3, 6, 12.5, 13, 8, 13.5)),
        ('Rear apron', (3, 6, 2.5, 13, 8, 3.5)),
        ('Left apron', (2.5, 6, 3, 3.5, 8, 13)),
        ('Right apron', (12.5, 6, 3, 13.5, 8, 13)),
        ('Left stretcher', (2.5, 3, 3, 3.5, 4, 13)),
        ('Right stretcher', (12.5, 3, 3, 13.5, 4, 13)),
        ('Rear stretcher', (3, 3, 2.5, 13, 4, 3.5)),
        # Three rectangles form a seat with one-unit clipped corners.
        ('Seat center', (2, 8, 1, 14, 10, 15)),
        ('Seat left edge', (1, 8, 2, 2, 10, 14)),
        ('Seat right edge', (14, 8, 2, 15, 10, 14)),
        ('Lower back rail', (4, 11, 2.5, 12, 12, 3.5)),
        ('Left back slat', (5, 12, 2.5, 6, 21, 3.5)),
        ('Center back slat', (7.5, 12, 2.5, 8.5, 21, 3.5)),
        ('Right back slat', (10, 12, 2.5, 11, 21, 3.5)),
        ('Top back rail', (4, 21, 2, 12, 24, 4)),
    ]
    return {'parent': 'minecraft:block/block', 'ambientocclusion': True,
            'textures': {'wood': 'minecraft:block/stripped_oak_log', 'particle': '#wood'},
            'elements': [element(name, bounds) for name, bounds in parts]}


def render(model, texture, size, scale, origin, view='perspective'):
    width, height = size
    rgb = np.full((height, width, 3), (247, 245, 240), dtype=np.uint8)
    depth = np.full((height, width), -np.inf)
    ox, oy = origin

    def project(v):
        x, y, z = v
        if view == 'front': return np.array([ox + x * scale, oy - y * scale])
        if view == 'side': return np.array([ox + z * scale, oy - y * scale])
        return np.array([ox + (x - z) * scale, oy + (x + z) * scale * .47 - y * scale * 1.05])

    def triangle(vertices, uv, light):
        p = np.array([project(v) for v in vertices])
        xmin, ymin = np.maximum(np.floor(p.min(axis=0)).astype(int), 0)
        xmax, ymax = np.minimum(np.ceil(p.max(axis=0)).astype(int), [width - 1, height - 1])
        if xmax < xmin or ymax < ymin: return
        yy, xx = np.mgrid[ymin:ymax + 1, xmin:xmax + 1]
        denominator = (p[1,1]-p[2,1])*(p[0,0]-p[2,0]) + (p[2,0]-p[1,0])*(p[0,1]-p[2,1])
        if abs(denominator) < 1e-8: return
        a = ((p[1,1]-p[2,1])*(xx+.5-p[2,0]) + (p[2,0]-p[1,0])*(yy+.5-p[2,1])) / denominator
        b = ((p[2,1]-p[0,1])*(xx+.5-p[2,0]) + (p[0,0]-p[2,0])*(yy+.5-p[2,1])) / denominator
        c = 1-a-b
        weights = np.stack([a,b,c], axis=-1)
        distances = [v[2] if view == 'front' else v[0] if view == 'side' else v[0]+v[2]+v[1]*.895 for v in vertices]
        d = weights @ np.array(distances)
        region = depth[ymin:ymax+1, xmin:xmax+1]
        mask = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6) & (d > region)
        coords = weights @ np.array(uv)
        u = np.clip(np.floor(coords[...,0]).astype(int), 0, 15)
        v = np.clip(np.floor(coords[...,1]).astype(int), 0, 15)
        colors = np.clip(texture[v,u]*light, 0, 255).astype(np.uint8)
        rgb[ymin:ymax+1, xmin:xmax+1][mask] = colors[mask]
        region[mask] = d[mask]

    for part in model['elements']:
        x0,y0,z0 = part['from']; x1,y1,z1 = part['to']
        faces = [
            ('up', [(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)], 1.0),
            ('east', [(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)], .72),
            ('south', [(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)], .88),
        ]
        for face, vertices, light in faces:
            u0,v0,u1,v1 = part['faces'][face]['uv']
            if face == 'up': uv = [(u0,v0),(u1,v0),(u1,v1),(u0,v1)]
            elif face == 'east': uv = [(u1,v1),(u0,v1),(u0,v0),(u1,v0)]
            else: uv = [(u0,v1),(u1,v1),(u1,v0),(u0,v0)]
            for indices in [(0,1,2),(0,2,3)]:
                triangle([vertices[i] for i in indices], [uv[i] for i in indices], light)
    return Image.fromarray(rgb)


def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('seguisb.ttf' if bold else 'segoeui.ttf'), size)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--client-jar', type=Path, default=CLIENT_JAR)
    args = parser.parse_args()
    model = chair_model()
    # Explicit UVs stay within Minecraft's 0..16 range, including tall posts.
    for part in model['elements']:
        x0,y0,z0 = part['from']; x1,y1,z1 = part['to']
        for face in FACES:
            if face in ('up','down'): uv = [x0,z0,x1,z1]
            elif face in ('east','west'): uv = [z0,0,z1,min(16,y1-y0)]
            else: uv = [x0,0,x1,min(16,y1-y0)]
            part['faces'][face]['uv'] = uv
    model_path = HERE / 'oak_dining_chair.json'
    model_path.write_text(json.dumps(model, indent=2)+'\n', encoding='utf-8')
    model = json.loads(model_path.read_text(encoding='utf-8'))
    with zipfile.ZipFile(args.client_jar) as jar:
        texture = np.asarray(Image.open(io.BytesIO(jar.read('assets/minecraft/textures/block/stripped_oak_log.png'))).convert('RGB'))
    image = Image.new('RGB', (1500,1050), '#e9e5dc')
    image.paste(render(model, texture, (830,775), 18, (415,505)), (40,165))
    image.paste(render(model, texture, (270,420), 14, (22,390), 'front'), (900,200))
    image.paste(render(model, texture, (270,420), 14, (22,390), 'side'), (1190,200))
    draw = ImageDraw.Draw(image)
    draw.text((50,35), 'Oak dining chair — model concept', fill='#302b24', font=font(43,True))
    draw.text((52,96), 'Stripped oak wood • open slatted back • square legs • clipped seat corners', fill='#71695d', font=font(24))
    draw.text((62,186), 'Three-quarter view', fill='#302b24', font=font(25,True))
    draw.text((920,170), 'Front', fill='#302b24', font=font(24,True))
    draw.text((1210,170), 'Side', fill='#302b24', font=font(24,True))
    draw.rounded_rectangle((900,645,1460,940), radius=16, fill='#f7f5f0')
    draw.text((925,670), 'Proposed proportions', fill='#302b24', font=font(27,True))
    for i,line in enumerate(['Seat: 14 × 14 units; top at 10 units', 'Back height: 24 units (1½ blocks)', 'Legs: 2 × 2 units; footprint within one block', 'Armless, with three vertical back slats', 'Raw stripped oak grain on every face']):
        draw.text((925,723+i*37), line, fill='#71695d', font=font(21))
    draw.text((52,983), 'Design preview • approved oak geometry • front, side, and three-quarter views', fill='#71695d', font=font(22))
    output = HERE / 'oak_dining_chair_preview.png'
    image.save(output)
    print(output)

    woods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove',
             'cherry', 'pale_oak', 'bamboo', 'crimson', 'warped']
    assets = HERE.parents[1] / 'src/main/resources/assets/atouchofclass'
    gallery = Image.new('RGB', (1820,1450), '#e9e5dc')
    draw = ImageDraw.Draw(gallery)
    draw.text((40,30), 'Dining chairs — all twelve wood finishes', fill='#302b24', font=font(43,True))
    draw.text((42,92), 'Approved slatted-back design • vanilla stripped wood grain • matching slab-and-stick recipes', fill='#71695d', font=font(24))
    with zipfile.ZipFile(args.client_jar) as jar:
        for i,wood in enumerate(woods):
            variant = json.loads((assets / f'models/block/{wood}_dining_chair.json').read_text())
            if 'elements' not in variant:
                parent = json.loads((assets / ('models/' + variant['parent'].split(':')[1] + '.json')).read_text())
                variant = {**parent, **variant, 'textures': {**parent['textures'], **variant['textures']}}
            texture_name = variant['textures']['wood'].split(':')[1]
            grain = np.asarray(Image.open(io.BytesIO(jar.read(f'assets/minecraft/textures/{texture_name}.png'))).convert('RGB'))
            x,y = 40+(i%4)*440, 155+(i//4)*400
            gallery.paste(Image.new('RGB',(420,380),'#f7f5f0'),(x,y))
            draw.text((x+20,y+14), wood.replace('_',' ').title(), fill='#302b24', font=font(27,True))
            gallery.paste(render(variant,grain,(420,310),8,(210,197)),(x,y+55))
    draw.text((42,1390), 'Rendered from shipped models and vanilla textures • all woods share seating and safe dismount behavior', fill='#71695d', font=font(22))
    output = HERE / 'all_wood_dining_chairs_preview.png'
    gallery.save(output)
    print(output)


if __name__ == '__main__': main()
