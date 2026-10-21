"""Preview-only brick ledge concept; no registered game assets are modified."""
from pathlib import Path
import json
import numpy as np
from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SOURCE = ROOT / 'build/neoForm/neoFormJoined26.3-1/steps/transformSource/transformed/assets/minecraft/textures/block/bricks.png'
brick = np.asarray(Image.open(SOURCE).convert('RGB'))

# Four rowlock brick ends across a block. Sample fired-clay pixels directly
# from vanilla bricks, retaining its mortar color and pixel density.
atlas = np.zeros((16, 16, 3), dtype=np.uint8)
for strip in range(4):
    for x in range(16):
        for y in range(4):
            sx = (x // 4 * 4 + x % 4) % 16
            sy = y % 3
            atlas[strip * 4 + y, x] = brick[sy, sx]
            if x % 4 == 3:
                atlas[strip * 4 + y, x] = brick[3, (x + y) % 16]
Image.fromarray(atlas).save(HERE / 'brick_window_ledge.png')

uvs = {'south':[0,0,16,4], 'north':[0,0,16,4], 'up':[0,4,16,8],
       'down':[0,8,16,12], 'east':[0,12,4,16], 'west':[0,12,4,16]}
model = {'credit':'Preview concept only; 16 x 4 x 4, one brick course, top aligned',
         'textures':{'ledge':'atouchofclass:block/brick_window_ledge', 'particle':'minecraft:block/bricks'},
         'elements':[{'from':[0,12,12], 'to':[16,16,16],
                      'faces':{side:{'uv':uv,'texture':'#ledge'} for side,uv in uvs.items()}}]}
(HERE / 'brick_window_ledge.json').write_text(json.dumps(model, indent=2)+'\n')

def font(n, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/'+('seguisb.ttf' if bold else 'segoeui.ttf'),n)

BG = '#eeeae2'
INK = '#332d28'
MUTED = '#73695f'

def render(boxes, size, scale, origin, guides=False, slope=.42):
    w,h=size
    rgb=np.full((h,w,3), (248,246,241), dtype=np.uint8)
    depth=np.full((h,w),-np.inf)
    def project(v):
        x,y,z=v
        return np.array([origin[0]+(x-z)*scale, origin[1]+(x+z)*scale*slope-y*scale])
    def triangle(vertices, uv, texture, light):
        p=np.array([project(v) for v in vertices])
        xmin,ymin=np.maximum(np.floor(p.min(axis=0)).astype(int),0)
        xmax,ymax=np.minimum(np.ceil(p.max(axis=0)).astype(int),[w-1,h-1])
        if xmax<xmin or ymax<ymin:return
        yy,xx=np.mgrid[ymin:ymax+1,xmin:xmax+1]
        den=(p[1,1]-p[2,1])*(p[0,0]-p[2,0])+(p[2,0]-p[1,0])*(p[0,1]-p[2,1])
        if abs(den)<1e-8:return
        a=((p[1,1]-p[2,1])*(xx+.5-p[2,0])+(p[2,0]-p[1,0])*(yy+.5-p[2,1]))/den
        b=((p[2,1]-p[0,1])*(xx+.5-p[2,0])+(p[0,0]-p[2,0])*(yy+.5-p[2,1]))/den
        weights=np.stack([a,b,1-a-b],axis=-1)
        d=weights @ np.array([v[0]+v[2]+v[1]*.84 for v in vertices])
        region=depth[ymin:ymax+1,xmin:xmax+1]
        mask=(a>=-1e-6)&(b>=-1e-6)&(1-a-b>=-1e-6)&(d>region)
        coords=np.floor(weights @ np.asarray(uv)).astype(int)%16
        colors=np.clip(texture[coords[...,1],coords[...,0]]*light,0,255).astype(np.uint8)
        rgb[ymin:ymax+1,xmin:xmax+1][mask]=colors[mask]
        region[mask]=d[mask]
    for low,high,material in boxes:
        x0,y0,z0=low;x1,y1,z1=high
        if material=='ledge':
            tex=atlas
            top=[(0,4),(16,4),(16,8),(0,8)]
            front=[(0,4),(16,4),(16,0),(0,0)]
            side=[(0,16),(4,16),(4,12),(0,12)]
        else:
            tex=brick if material=='wall' else np.full((16,16,3),material,dtype=np.uint8)
            top=[(x0,z0),(x1,z0),(x1,z1),(x0,z1)]
            front=[(x0,-y0),(x1,-y0),(x1,-y1),(x0,-y1)]
            side=[(z0,-y0),(z1,-y0),(z1,-y1),(z0,-y1)]
        faces=[([(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)],top,1),
               ([(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],front,.87),
               ([(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)],side,.69)]
        for vertices,uv,light in faces:
            for ids in [(0,1,2),(0,2,3)]:triangle([vertices[i] for i in ids],[uv[i] for i in ids],tex,light)
    out=Image.fromarray(rgb)
    if guides:
        draw=ImageDraw.Draw(out)
        for y in [0,16]:
            pts=[tuple(project(v)) for v in [(0,y,0),(16,y,0),(16,y,16),(0,y,16),(0,y,0)]]
            draw.line(pts,fill='#bcb5aa',width=2)
        for x,z in [(0,0),(16,0),(16,16),(0,16)]:
            draw.line([tuple(project((x,0,z))),tuple(project((x,16,z)))],fill='#bcb5aa',width=2)
    return out

def main():
    page=Image.new('RGB',(1680,1080),BG)
    d=ImageDraw.Draw(page)
    d.text((48,30),'Brick window ledge',font=font(44,True),fill=INK)
    d.text((50,88),'Model concept  /  regular brick  /  preview only',font=font(23),fill=MUTED)
    page.paste(render([([0,12,12],[16,16,16],'ledge')],(700,530),14,(350,310),True),(35,150))
    boxes=[([-4,0,8],[52,12,12],'wall'),([-4,12,8],[0,52,12],'wall'),
           ([48,12,8],[52,52,12],'wall'),([0,48,8],[48,52,12],'wall'),
           ([0,16,8],[48,48,9],(57,76,81))]
    # Neutral window frame for placement context; not part of the proposed model.
    for lo,hi in [([0,16,9],[1,48,10]),([47,16,9],[48,48,10]),
                  ([0,47,9],[48,48,10]),([23,16,9],[24,48,10])]:
        boxes.append((lo,hi,(189,181,163)))
    boxes += [([x,12,12],[x+16,16,16],'ledge') for x in [0,16,32]]
    page.paste(render(boxes,(870,545),8,(300,410),slope=.25),(770,210))
    d=ImageDraw.Draw(page)
    d.text((65,170),'Single ledge + full block outline',font=font(25,True),fill=INK)
    d.text((800,170),'Three ledges beneath a window',font=font(25,True),fill=INK)
    d.text((65,700),'16 wide × 4 high × 4 deep',font=font(29,True),fill=INK)
    d.text((65,744),'Top aligned: Y = 12–16 in a 16-unit block',font=font(22),fill=MUTED)
    d.text((800,765),'Flush ends; no end caps or gaps between blocks.',font=font(22),fill=MUTED)
    d.line((50,820,1630,820),fill='#c6beb1',width=2)
    strip=Image.fromarray(atlas[:4]).resize((640,160),Image.Resampling.NEAREST)
    page.paste(strip,(65,870))
    d.text((65,1035),'Front texture • enlarged pixels',font=font(18),fill=MUTED)
    d.text((800,865),'Rowlock brick detail',font=font(27,True),fill=INK)
    d.text((800,910),'Vanilla brick colors and fired-clay grain.',font=font(22),fill=MUTED)
    d.text((800,946),'Mortar joints continue from front to top.',font=font(22),fill=MUTED)
    d.text((800,982),'Same 4-unit brick rhythm across adjacent ledges.',font=font(22),fill=MUTED)
    page.save(HERE/'brick_window_ledge_preview.png')
    print(HERE/'brick_window_ledge_preview.png')

if __name__=='__main__':main()
