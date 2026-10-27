"""Oak sidelight proposal: modeled wood frame and translucent etched strips.

Run from the repository root. Only preview assets are written.
"""
import sys
sys.dont_write_bytecode = True
import importlib.util
import json
from pathlib import Path
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
spec = importlib.util.spec_from_file_location('door_preview', ROOT/'preview/tall_oak_door/generate_preview.py')
door = importlib.util.module_from_spec(spec)
spec.loader.exec_module(door)
renderer = door.renderer


def generate():
    elements = []
    def add(a, b, texture='wood'):
        elements.append(door.box(a, b, texture))
    # Match the existing 3-unit stiles, 3-unit head, and 5-unit sill.
    add([0, 0, 0], [3, 48, 3])
    add([13, 0, 0], [16, 48, 3])
    add([3, 0, 0], [13, 5, 3])
    add([3, 45, 0], [13, 48, 3])
    # A shallow glazing bead gives the inner wood opening real relief.
    add([3, 5, .375], [3.5, 45, 2.625])
    add([12.5, 5, .375], [13, 45, 2.625])
    add([3.5, 5, .375], [12.5, 5.5, 2.625])
    add([3.5, 44.5, .375], [12.5, 45, 2.625])
    # Two nested half-unit strips reproduce the original light/dark etch.
    # The clear center needs no surface; only the etching carries opacity.
    for inset, material in ((0, 'etch_light'), (.5, 'etch_dark')):
        x, X, y, Y = 4+inset, 12-inset, 6+inset, 43.5-inset
        for a, b in (([x,y,1.4375],[x+.5,Y,1.5625]),
                     ([X-.5,y,1.4375],[X,Y,1.5625]),
                     ([x+.5,y,1.4375],[X-.5,y+.5,1.5625]),
                     ([x+.5,Y-.5,1.4375],[X-.5,Y,1.5625])):
            add(a, b, material)
    textures = HERE/'textures/block'
    textures.mkdir(parents=True, exist_ok=True)
    for name, gray in (('etch_light', 217), ('etch_dark', 186)):
        Image.new('RGBA', (16,16), (gray,gray,gray,117)).save(textures/f'sidelight_{name}.png')
    for part, section in enumerate(('bottom','middle','top')):
        clipped=[]
        for e in elements:
            a,b=e['from'].copy(),e['to'].copy()
            lo,hi=max(a[1],part*16),min(b[1],(part+1)*16)
            if lo>=hi: continue
            a[1],b[1]=lo-part*16,hi-part*16
            element=door.box(a,b,e['faces']['south']['texture'][1:])
            if part and lo==part*16: element['faces'].pop('down')
            if part<2 and hi==(part+1)*16: element['faces'].pop('up')
            clipped.append(element)
        model={'parent':'minecraft:block/block','render_type':'minecraft:translucent',
               'textures':{'wood':'minecraft:block/stripped_oak_log',
                           'particle':'minecraft:block/stripped_oak_log',
                           'etch_light':'atouchofclass:block/sidelight_etch_light',
                           'etch_dark':'atouchofclass:block/sidelight_etch_dark'},
               'elements':clipped}
        path=HERE/f'models/block/tall_oak_sidelight_{section}.json'
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(model,indent=2)+'\n')


def main():
    generate()
    renderer.ASSETS=HERE
    source=(ROOT/'preview/tall_patio_door/generate_preview.py').read_text()
    function=source[source.index('def render('):source.index('\ndef font(')]
    function=function.replace('tall_{wood}_patio_door_{section}_{hinge}.json','tall_oak_sidelight_{section}.json')
    exec(function,renderer.__dict__)
    sheet=Image.new('RGBA',(1200,1000),'#e9e5dd')
    pen=ImageDraw.Draw(sheet)
    pen.text((40,28),'Tall oak sidelight | Model preview',font=renderer.font(36,True),fill='#302b25')
    pen.text((42,82),'Stripped oak frame / Recessed glazing bead / Modeled translucent etched rectangle',font=renderer.font(21),fill='#686157')
    for left,title in ((30,'FRONT'),(610,'ANGLED')):
        pen.rounded_rectangle((left,140,left+560,910),radius=18,fill='#f7f5f0')
        pen.text((left+24,162),title,font=renderer.font(20,True),fill='#71695e')
        # A contrasting backdrop shows real transparency through the etching.
        for x in range(left+30,left+540,40):
            for y in range(220,880,40):
                pen.rectangle((x,y,x+39,y+39),fill=('#b5c5cb' if ((x-left-30)//40+(y-220)//40)%2 else '#839ba5'))
    sheet.alpha_composite(renderer.render(sheet.size,(200,850),12.5,0,False))
    sheet.alpha_composite(renderer.render(sheet.size,(785,822),12.5,28,False))
    pen.text((42,942),'1 block wide x 3 blocks tall | Etching: original 46% opacity | Preview only',font=renderer.font(22),fill='#403a32')
    sheet.convert('RGB').save(HERE/'tall_oak_sidelight_preview.png')
    print('Generated three oak sidelight sections and two translucent material swatches.')


if __name__=='__main__':
    main()
