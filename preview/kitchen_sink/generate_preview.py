"""Render the actual sink and cabinet JSON models with textures and a depth buffer."""
from __future__ import annotations

import io
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).absolute().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/atouchofclass'
JAR = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.4_client.jar'
OUT = Path(__file__).parent
TEXTURES = {}


def texture(resource):
    if resource not in TEXTURES:
        namespace, path = resource.split(':')
        if namespace == 'minecraft':
            with zipfile.ZipFile(JAR) as jar:
                source = io.BytesIO(jar.read(f'assets/minecraft/textures/{path}.png'))
        else:
            source = ASSETS / 'textures' / (path + '.png')
        TEXTURES[resource] = np.asarray(Image.open(source).convert('RGB'))
    return TEXTURES[resource]


def font(size, bold=False):
    return ImageFont.truetype(str(Path('C:/Windows/Fonts') / ('seguisb.ttf' if bold else 'segoeui.ttf')), size)


def render(placements, size, scale, yaw=26, elevation=30):
    width, height = size
    az, el = math.radians(yaw), math.radians(elevation)
    right = np.array([math.cos(az), 0, math.sin(az)])
    up = np.array([-math.sin(az)*math.sin(el), math.cos(el), math.cos(az)*math.sin(el)])
    toward = np.cross(up, right)
    matrix = np.array([right, up, toward])
    models = [(json.loads((ASSETS/'models/block'/f'{name}.json').read_text()), np.array(offset))
              for name, offset in placements]
    all_points = np.array([np.array(e[key])+offset for model, offset in models
                           for e in model['elements'] for key in ('from','to')])
    projected = all_points @ matrix.T
    center = (projected[:,:2].min(axis=0) + projected[:,:2].max(axis=0))/2
    pixels = np.full((height,width,3), [247,245,240], dtype=np.uint8)
    depth = np.full((height,width), -np.inf)

    def triangle(points, uv, tex, brightness):
        p = np.array(points)
        x0, y0 = np.maximum(np.floor(p[:,:2].min(axis=0)).astype(int), [0,0])
        x1, y1 = np.minimum(np.ceil(p[:,:2].max(axis=0)).astype(int), [width-1,height-1])
        if x1 < x0 or y1 < y0:
            return
        xx, yy = np.meshgrid(np.arange(x0,x1+1)+.5,np.arange(y0,y1+1)+.5)
        a,b,c = p
        denom = (b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
        if abs(denom) < 1e-8:
            return
        wa = ((b[1]-c[1])*(xx-c[0])+(c[0]-b[0])*(yy-c[1]))/denom
        wb = ((c[1]-a[1])*(xx-c[0])+(a[0]-c[0])*(yy-c[1]))/denom
        wc = 1-wa-wb
        z = wa*a[2]+wb*b[2]+wc*c[2]
        region = depth[y0:y1+1,x0:x1+1]
        mask = (wa>=-1e-7)&(wb>=-1e-7)&(wc>=-1e-7)&(z>region)
        u = wa*uv[0][0]+wb*uv[1][0]+wc*uv[2][0]
        v = wa*uv[0][1]+wb*uv[1][1]+wc*uv[2][1]
        tx = np.clip(np.floor(u/16*tex.shape[1]).astype(int), 0, tex.shape[1]-1)
        ty = np.clip(np.floor(v/16*tex.shape[0]).astype(int), 0, tex.shape[0]-1)
        color = np.clip(tex[ty,tx]*brightness,0,255).astype(np.uint8)
        pixels[y0:y1+1,x0:x1+1][mask] = color[mask]
        region[mask] = z[mask]

    normals = {'north':(0,0,-1),'south':(0,0,1),'east':(1,0,0),
               'west':(-1,0,0),'up':(0,1,0),'down':(0,-1,0)}
    for model, offset in models:
        for e in model['elements']:
            x,y,z = e['from']; X,Y,Z = e['to']
            faces = {
                'north':[(X,Y,z),(x,Y,z),(x,y,z),(X,y,z)],
                'south':[(x,Y,Z),(X,Y,Z),(X,y,Z),(x,y,Z)],
                'east':[(X,Y,Z),(X,Y,z),(X,y,z),(X,y,Z)],
                'west':[(x,Y,z),(x,Y,Z),(x,y,Z),(x,y,z)],
                'up':[(x,Y,z),(X,Y,z),(X,Y,Z),(x,Y,Z)],
                'down':[(x,y,Z),(X,y,Z),(X,y,z),(x,y,z)]}
            defaults = {'north':[16-X,16-Y,16-x,16-y], 'south':[x,16-Y,X,16-y],
                        'east':[16-Z,16-Y,16-z,16-y], 'west':[z,16-Y,Z,16-y],
                        'up':[x,z,X,Z], 'down':[x,16-Z,X,16-z]}
            for face, info in e['faces'].items():
                u,v,U,V = info.get('uv',defaults[face])
                assert all(0 <= value <= 16 for value in (u,v,U,V)), \
                    f"Out-of-sprite UV on {e.get('name', 'element')} {face}: {(u,v,U,V)}"
                n = np.array(normals[face])
                if n @ toward <= 0:
                    continue
                p = (np.array(faces[face])+offset) @ matrix.T
                p[:,0] = (p[:,0]-center[0])*scale+width/2
                p[:,1] = height/2-(p[:,1]-center[1])*scale
                uv = [(u,v),(U,v),(U,V),(u,V)]
                resource = info['texture']
                while resource.startswith('#'):
                    resource = model['textures'][resource[1:]]
                tex = texture(resource)
                brightness = {'north':.85,'south':.85,'east':.69,'west':.69,'up':1,'down':.55}[face]
                for inds in ((0,1,2),(0,2,3)):
                    triangle(p[list(inds)], [uv[i] for i in inds], tex, brightness)
    return Image.fromarray(pixels)


def scene(double=False):
    if double:
        return [('oak_door_cabinet',(0,-16,0)), ('oak_door_cabinet',(16,-16,0)),
                ('kitchen_sink_double_left',(0,0,0)), ('kitchen_sink_double_right',(16,0,0))]
    return [('oak_door_cabinet',(0,-16,0)), ('kitchen_sink',(0,0,0))]


def main():
    image = Image.new('RGB',(1800,1190),'#e9e5dd')
    draw = ImageDraw.Draw(image)
    draw.text((55,32),'Stainless steel kitchen sink',font=font(40,True),fill='#292723')
    draw.text((57,88),'Actual block models on oak door cabinets',font=font(22),fill='#6f6960')
    for double, x, width in [(False,35,675),(True,735,1030)]:
        draw.rounded_rectangle((x,140,x+width,1020),24,fill='#f7f5f0')
        draw.text((x+27,160),'Double sink' if double else 'Single sink',font=font(28,True),fill='#292723')
        draw.text((x+27,202),'13 × 10 basins · 2-unit divider lowered 1 unit' if double else '12 × 10 opening · restaurant-style sprayer',
                  font=font(20),fill='#6f6960')
        view = render(scene(double),(width-24,720),19 if double else 20)
        image.paste(view,(x+12,267))
        view.save(OUT/('double_sink.png' if double else 'single_sink.png'))
    draw.text((57,1052),'Single: 12 × 10  ·  Double: 13 × 10 per basin  ·  Faucet mounting plate flush with rear opening edge',font=font(24,True),fill='#393631')
    draw.text((57,1093),'Front lip overhangs 1 unit  ·  apron is 2 units deep and drops 5 units below the sink block',
              font=font(22),fill='#6f6960')
    draw.text((57,1134),'Black basin floor and double divider sit 1 unit below the rim. Apron has no collision or selection.',font=font(20),fill='#6f6960')
    image.save(OUT/'kitchen_sink_preview.png')
    # Higher view makes the opening and the two-unit divider easy to inspect.
    render(scene(True),(1500,1000),26,yaw=15,elevation=63).save(OUT/'double_sink_top.png')
    print(OUT/'kitchen_sink_preview.png')


if __name__ == '__main__':
    main()
