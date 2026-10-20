"""Recolor the tall birch door design to the general shade of vanilla poplar planks."""
import colorsys
import io
from pathlib import Path
import zipfile

from PIL import Image
from generate_parquet_variants import color_center

ROOT = Path(__file__).absolute().parents[1]
TEXTURES = ROOT / 'src/main/resources/assets/atouchofclass/textures'


def main():
    parts = [TEXTURES / f'block/tall_birch_door_{part}.png' for part in ('bottom', 'middle', 'top')]
    pixels = [p for path in parts for p in Image.open(path).convert('RGBA').getdata() if p[3]]
    reference = Image.new('RGBA', (len(pixels), 1))
    reference.putdata(pixels)
    source_h, source_l, source_s = color_center(reference)
    with zipfile.ZipFile(ROOT / 'build/neoForm/neoFormJoined26.3-1/raw.jar') as jar:
        target = Image.open(io.BytesIO(jar.read('assets/minecraft/textures/block/poplar_planks.png')))
        target_h, target_l, target_s = color_center(target)
    for path in parts + [TEXTURES / 'item/tall_birch_door.png']:
        image = Image.open(path).convert('RGBA')
        result = []
        for pixel in image.getdata():
            if not pixel[3]:
                result.append(pixel)
                continue
            h, l, s = colorsys.rgb_to_hls(*(c / 255 for c in pixel[:3]))
            rgb = colorsys.hls_to_rgb((h + target_h - source_h) % 1,
                                     min(1, l * target_l / source_l),
                                     min(1, s * target_s / source_s))
            result.append(tuple(round(c * 255) for c in rgb) + (pixel[3],))
        output = Image.new('RGBA', image.size)
        output.putdata(result)
        assert output.getchannel('A').tobytes() == image.getchannel('A').tobytes()
        output.save(path.with_name(path.name.replace('birch', 'poplar')))
    print('Generated three tall poplar door textures and item icon, preserving detail and alpha.')


if __name__ == '__main__':
    main()
