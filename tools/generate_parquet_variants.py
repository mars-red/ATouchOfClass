"""Recolor the hand-authored oak parquet using vanilla plank color references.

Only colors change: each source RGBA color maps to one target RGBA color,
preserving every joint, grain pixel, transparent pixel, and repeating edge.
Requires Pillow and a local Minecraft 1.21.4 client jar.
"""

import argparse
import colorsys
import hashlib
import io
import math
from pathlib import Path
import statistics
import zipfile

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).absolute().parents[1]
TEXTURES = ROOT / 'src/main/resources/assets/atouchofclass/textures/block'
PREVIEW = ROOT / 'preview/parquet'
WOODS = ('oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak',
         'mangrove', 'cherry', 'pale_oak', 'bamboo', 'crimson', 'warped')
CLIENT_JAR = Path.home() / '.gradle/caches/neoformruntime/artifacts/minecraft_1.21.4_client.jar'


def color_center(image):
    values = [colorsys.rgb_to_hls(*(c / 255 for c in pixel[:3]))
              for pixel in image.convert('RGBA').getdata() if pixel[3]]
    # Circular hue avoids averaging red hues across the 0/1 boundary.
    x = sum(math.cos(h * math.tau) * s for h, _, s in values)
    y = sum(math.sin(h * math.tau) * s for h, _, s in values)
    return (math.atan2(y, x) / math.tau) % 1, statistics.mean(
        l for _, l, _ in values), statistics.mean(s for _, _, s in values)


def recolor(source, target):
    source_h, source_l, source_s = color_center(source)
    target_h, target_l, target_s = color_center(target)
    palette = {}
    for pixel in set(source.getdata()):
        h, l, s = colorsys.rgb_to_hls(*(c / 255 for c in pixel[:3]))
        rgb = colorsys.hls_to_rgb(
            (h + target_h - source_h) % 1,
            min(1, l * target_l / source_l),
            min(1, s * target_s / source_s),
        )
        palette[pixel] = tuple(round(c * 255) for c in rgb) + (pixel[3],)
    result = Image.new('RGBA', source.size)
    result.putdata([palette[pixel] for pixel in source.getdata()])
    assert result.getchannel('A').tobytes() == source.getchannel('A').tobytes()
    # Matching source colors must stay equal, including across tile borders.
    assert all(palette[a] == b for a, b in zip(source.getdata(), result.getdata()))
    return result


def preview(images, references):
    cell_w, cell_h = 280, 306
    sheet = Image.new('RGB', (cell_w * 4, cell_h * 3 + 38), '#202124')
    draw = ImageDraw.Draw(sheet)
    font_path = Path('C:/Windows/Fonts/segoeui.ttf')
    font = ImageFont.truetype(str(font_path), 17) if font_path.exists() else ImageFont.load_default()
    draw.text((12, 8), 'Parquet variants | vanilla planks at left, 3 x 3 parquet at right', fill='white', font=font)
    for index, wood in enumerate(WOODS):
        x, y = (index % 4) * cell_w + 8, (index // 4) * cell_h + 42
        draw.text((x, y), wood.replace('_', ' ').title(), fill='white', font=font)
        sheet.paste(references[wood].resize((64, 64), Image.Resampling.NEAREST), (x, y + 30))
        tile = images[wood].resize((64, 64), Image.Resampling.NEAREST)
        for row in range(3):
            for col in range(3):
                sheet.paste(tile, (x + 72 + col * 64, y + 30 + row * 64))
        draw.text((x, y + 236), 'Vanilla     Parquet', fill='#bbbbbb', font=font)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW / 'wood-variants.png')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--client-jar', type=Path, default=CLIENT_JAR)
    args = parser.parse_args()
    oak_path = TEXTURES / 'oak_parquet.png'
    original_bytes = oak_path.read_bytes()
    source = Image.open(io.BytesIO(original_bytes)).convert('RGBA')
    with zipfile.ZipFile(args.client_jar) as jar:
        references = {wood: Image.open(io.BytesIO(jar.read(
            f'assets/minecraft/textures/block/{wood}_planks.png'))).convert('RGBA')
            for wood in WOODS}
    images = {'oak': source}
    for wood in WOODS[1:]:
        images[wood] = recolor(source, references[wood])
    # Finish all recoloring before replacing any target assets.
    for wood in WOODS[1:]:
        path = TEXTURES / f'{wood}_parquet.png'
        images[wood].save(path)
        with Image.open(path) as saved:
            assert saved.size == source.size
            assert saved.convert('RGBA').tobytes() == images[wood].tobytes()
    assert oak_path.read_bytes() == original_bytes, 'Oak must remain byte-for-byte unchanged'
    preview(images, references)
    print(f'Recolored {len(WOODS) - 1} textures at {source.width}x{source.height}; oak unchanged.')
    print(f'Oak SHA-256: {hashlib.sha256(original_bytes).hexdigest()}')
    print(f'Preview: {PREVIEW / "wood-variants.png"}')


if __name__ == '__main__':
    main()
