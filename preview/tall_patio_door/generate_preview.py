"""Render the actual patio door JSON cuboids and face UVs using vanilla textures."""
import io
import json
import math
import zipfile
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).absolute().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
HERE = Path(__file__).absolute().parent
JAR = Path(r'C:\Users\vwner\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.4_client.jar')


def texture(name):
    namespace, path = name.split(':')
    if namespace == 'minecraft':
        with zipfile.ZipFile(JAR) as jar:
            return Image.open(io.BytesIO(jar.read('assets/minecraft/textures/' + path + '.png'))).convert('RGBA')
    return Image.open(ASSETS / ('textures/' + path + '.png')).convert('RGBA')


def render(size, origin, scale, angle=0, pair=True, wood='oak'):
    canvas = Image.new('RGBA', size)
    theta = math.radians(angle)
    c, s = math.cos(theta), math.sin(theta)
    def project(p):
        x, y, z = p
        return (origin[0] + scale*(c*x-s*z), origin[1] + scale*(-y + (0.13*x+0.22*z if angle else 0)))
    faces = []
    for leaf, hinge in enumerate(('left', 'right') if pair else ('left',)):
        for part, section in enumerate(('bottom', 'middle', 'top')):
            model = json.loads((ASSETS / f'models/block/tall_{wood}_patio_door_{section}_{hinge}.json').read_text())
            textures = {key: texture(value) for key, value in model['textures'].items()}
            for element in model['elements']:
                x, y, z = element['from']
                X, Y, Z = element['to']
                quads = {
                    'south': [(x,Y,Z), (X,Y,Z), (X,y,Z), (x,y,Z)],
                    'east': [(X,Y,Z), (X,Y,z), (X,y,z), (X,y,Z)],
                    'up': [(x,Y,z), (X,Y,z), (X,Y,Z), (x,Y,Z)]}
                for side in (('south','east','up') if angle else ('south',)):
                    if side not in element['faces']:
                        continue
                    points = [(a + leaf*16, b+part*16, d) for a,b,d in quads[side]]
                    depth = sum(s*a + c*d + (0.1*b if angle else 0) for a,b,d in points)/4
                    face = element['faces'][side]
                    faces.append((depth, [project(p) for p in points], textures[face['texture'][1:]], face['uv'], {'south':1, 'east':0.68, 'up':1.1}[side]))
    for _, points, tex, uv, shade in sorted(faces, key=lambda f:f[0]):
        u, v, U, V = uv
        source = [(u,v),(U,v),(U,V),(u,V)]
        matrix, values = [], []
        for (a,b),(tu,tv) in zip(points,source):
            matrix.extend([[a,b,1,0,0,0,-tu*a,-tu*b], [0,0,0,a,b,1,-tv*a,-tv*b]])
            values.extend([tu,tv])
        coeffs = np.linalg.solve(np.array(matrix), np.array(values))
        pixels = np.array(tex)
        pixels[:,:,:3] = np.clip(pixels[:,:,:3]*shade,0,255).astype('uint8')
        warped = Image.fromarray(pixels).transform(size, Image.Transform.PERSPECTIVE, coeffs, Image.Resampling.NEAREST)
        mask = Image.new('L', size)
        ImageDraw.Draw(mask).polygon(points, fill=255)
        alpha = np.minimum(np.array(mask), np.array(warped.getchannel('A')))
        warped.putalpha(Image.fromarray(alpha))
        canvas.alpha_composite(warped)
    return canvas


def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('seguisb.ttf' if bold else 'segoeui.ttf'), size)


def generate_variants_preview(woods):
    sheet = Image.new('RGBA', (1080, 1050), '#e9e5dd')
    pen = ImageDraw.Draw(sheet)
    pen.text((30, 20), 'Tall Patio Doors — all vanilla woods', font=font(32, True), fill='#302b25')
    for index, wood in enumerate(woods):
        x, y = (index % 4)*270, 85 + (index // 4)*315
        pen.rounded_rectangle((x+12,y,x+258,y+300), radius=12, fill='#f7f5f0')
        sheet.alpha_composite(render(sheet.size,(x+63,y+248),4.5,0,True,wood))
        pen.text((x+24,y+265), wood.replace('_',' ').title(), font=font(21,True), fill='#403a32')
    sheet.convert('RGB').save(HERE/'tall_patio_door_variants_preview.png')


def generate_item_icon(wood):
    # item/generated maps the entire texture to a square inventory quad.
    # Keep a square canvas and center the full 1:3 leaf with equal padding.
    source_icon = render((64,64),(22 + 2/3,60),7/6,0,False,wood)
    # Pre-filter to the native 16-pixel slot grid so nearest texture sampling
    # cannot skip either thin stile. Upscale without changing that pixel grid.
    slot_icon = source_icon.resize((16,16), Image.Resampling.BOX)
    assert slot_icon.getbbox() == (5, 1, 11, 15), 'The full leaf must be centered and unclipped'
    icon = slot_icon.resize((64,64), Image.Resampling.NEAREST)
    icon.save(ASSETS/f'textures/item/tall_{wood}_patio_door.png')
    return icon


def main():
    image = Image.new('RGBA', (1450,1000), '#e9e5dd')
    draw = ImageDraw.Draw(image)
    draw.text((58,34), 'Tall Oak Patio Doors', font=font(42,True), fill='#302b25')
    draw.text((60,92), 'Paired block model  •  Vanilla stripped oak frame  •  One clear pane per leaf', font=font(23), fill='#686157')
    for left, right, title in ((40,696,'FRONT VIEW'),(724,1410,'ANGLED VIEW')):
        draw.rounded_rectangle((left,152,right,900), radius=20, fill='#f7f5f0')
        draw.text((left+26,174), title, font=font(20,True), fill='#71695e')
        # A pale grid makes the actual transparent pane and its glints visible.
        for gx in range(left+28,right-20,44):
            draw.line((gx,232,gx,830), fill='#e9e6df')
        for gy in range(232,831,44):
            draw.line((left+28,gy,right-20,gy), fill='#e9e6df')
    image.alpha_composite(render(image.size,(172,840),12,0))
    image.alpha_composite(render(image.size,(860,808),12,22))
    draw = ImageDraw.Draw(image)
    draw.text((187,862), '2 blocks wide × 3 blocks high', font=font(21), fill='#645c51')
    draw.text((892,862), '3/16-block frame thickness', font=font(21), fill='#645c51')
    draw.text((60,934), 'Opposing hinges • Mirrored iron levers on both sides • No rail across the glass', font=font(24), fill='#403a32')
    image.convert('RGB').save(HERE/'tall_oak_patio_doors_preview.png')
    icon = generate_item_icon('oak')
    slots = Image.new('RGBA', (660,260), '#e9e5dd')
    pen = ImageDraw.Draw(slots)
    pen.text((24,18), 'Oak patio door — inventory icon', font=font(27,True), fill='#302b25')
    for x, label, background in ((60, 'Light slot', '#b7b7b7'), (270, 'Dark slot', '#373737'), (480, '16 px slot', '#8b8b8b')):
        pen.rectangle((x-4,72,x+132,208), fill='#f5f5f5')
        pen.rectangle((x-2,74,x+130,206), fill='#373737')
        pen.rectangle((x,76,x+128,204), fill=background)
        shown = icon if x != 480 else icon.resize((16,16), Image.Resampling.NEAREST)
        slots.alpha_composite(shown.resize((128,128), Image.Resampling.NEAREST), (x,76))
        pen.text((x,222), label, font=font(18), fill='#403a32')
    slots.convert('RGB').save(HERE/'tall_oak_patio_door_inventory_preview.png')


if __name__ == '__main__':
    main()
