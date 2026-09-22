from __future__ import annotations

import io
import math
import zipfile
from dataclasses import dataclass
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[2]
CLIENT_JAR = Path(r"C:\Users\vwner\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.4_client.jar")
OUTPUT = Path(__file__).with_name("dark_oak_red_canopy_bed_preview.png")

WIDTH, HEIGHT = 1600, 1120
BG = "#eae7df"
PANEL = "#f7f5f0"
INK = "#292723"
MUTED = "#6f6960"
LINE = "#34271f"
GUIDE = "#9e978c"
RED = "#b02e26"


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    name = "seguisb.ttf" if bold else "segoeui.ttf"
    return ImageFont.truetype(str(Path(r"C:\Windows\Fonts") / name), size)


FONT_TITLE = font(42, True)
FONT_SUBTITLE = font(23)
FONT_CARD_TITLE = font(27, True)
FONT_CARD_SUB = font(19)
FONT_NOTE = font(18)
FONT_LABEL = font(17, True)


def load_texture(name: str) -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        data = jar.read(f"assets/minecraft/textures/block/{name}.png")
    return Image.open(io.BytesIO(data)).convert("RGBA")


WOOD_SIDE = load_texture("stripped_dark_oak_log")
WOOD_END = load_texture("stripped_dark_oak_log_top")
RED_FABRIC = load_texture("red_wool")
WHITE_FABRIC = load_texture("white_wool")


def tint(texture: Image.Image, color: str, strength: float = 0.55) -> Image.Image:
    base = texture.convert("RGB")
    overlay = Image.new("RGB", base.size, color)
    return Image.blend(base, overlay, strength).convert("RGBA")


RED_FABRIC = tint(RED_FABRIC, RED, 0.3)


def shade(texture: Image.Image, factor: float) -> Image.Image:
    return ImageEnhance.Brightness(texture.convert("RGB")).enhance(factor).convert("RGBA")


def perspective_coefficients(destination, source):
    matrix, values = [], []
    for (x, y), (u, v) in zip(destination, source):
        matrix.append([x, y, 1, 0, 0, 0, -u * x, -u * y])
        values.append(u)
        matrix.append([0, 0, 0, x, y, 1, -v * x, -v * y])
        values.append(v)
    return np.linalg.solve(np.asarray(matrix), np.asarray(values))


def textured_face(canvas: Image.Image, texture: Image.Image, points, brightness=1.0,
                  outline=LINE, width=2):
    source = shade(texture, brightness).resize((256, 256), Image.Resampling.NEAREST)
    destination = [tuple(map(float, point)) for point in points]
    coeffs = perspective_coefficients(destination, [(0, 0), (255, 0), (255, 255), (0, 255)])
    warped = source.transform(canvas.size, Image.Transform.PERSPECTIVE, coeffs, Image.Resampling.BICUBIC)
    mask = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(mask).polygon(points, fill=255)
    warped.putalpha(mask)
    canvas.alpha_composite(warped)
    if outline:
        ImageDraw.Draw(canvas).line([*points, points[0]], fill=outline, width=width, joint="curve")


def make_projection(origin, sx, sy, szx, szy):
    ox, oy = origin

    def project(x, y, z):
        return (round(ox + x * sx + z * szx), round(oy - y * sy + z * szy))

    return project


@dataclass
class Box:
    xyz0: tuple[float, float, float]
    xyz1: tuple[float, float, float]
    material: str = "wood"
    label: str = ""
    order: int = 0

    @property
    def depth(self):
        # Back-to-front painter ordering for this projection.
        return self.order * 10000 + self.xyz1[0] + self.xyz1[2] - self.xyz0[1] * 0.08


def draw_box(canvas: Image.Image, p, box: Box):
    x0, y0, z0 = box.xyz0
    x1, y1, z1 = box.xyz1
    materials = {
        "wood": (WOOD_SIDE, WOOD_SIDE, WOOD_END),
        "red": (RED_FABRIC, RED_FABRIC, RED_FABRIC),
        "white": (WHITE_FABRIC, WHITE_FABRIC, WHITE_FABRIC),
    }
    front_tex, side_tex, top_tex = materials[box.material]
    # Left/front-facing side (constant z1), right side (constant x1), then top.
    front = [p(x0, y0, z1), p(x1, y0, z1), p(x1, y1, z1), p(x0, y1, z1)]
    right = [p(x1, y0, z0), p(x1, y0, z1), p(x1, y1, z1), p(x1, y1, z0)]
    top = [p(x0, y1, z0), p(x1, y1, z0), p(x1, y1, z1), p(x0, y1, z1)]
    textured_face(canvas, front_tex, front, 0.80 if box.material == "wood" else 0.88)
    textured_face(canvas, side_tex, right, 0.66 if box.material == "wood" else 0.74)
    textured_face(canvas, top_tex, top, 1.06)


def bed_boxes():
    boxes: list[Box] = []

    # Four 3x3 posts with blocky feet, collars, and caps.
    for x0 in (0.5, 28.5):
        for z0 in (0.5, 36.5):
            order = 0 if z0 < 10 else 8
            boxes.extend([
                Box((x0, 0, z0), (x0 + 3, 2, z0 + 3), order=order),
                Box((x0 + .5, 2, z0 + .5), (x0 + 2.5, 28.5, z0 + 2.5), order=order),
                Box((x0, 27.5, z0), (x0 + 3, 29.5, z0 + 3), order=order),
                Box((x0 + .45, 29.5, z0 + .45), (x0 + 2.55, 31, z0 + 2.55), order=order),
                Box((x0, 31, z0), (x0 + 3, 32, z0 + 3), order=order),
            ])

    # Bed frame, slats, and slightly recessed mattress.
    boxes.extend([
        Box((2.5, 4, 2.5), (29.5, 7, 5), "wood", order=1),
        Box((2.5, 4, 35), (29.5, 7, 37.5), "wood", order=7),
        Box((2.5, 4, 5), (5, 7, 35), "wood", order=2),
        Box((27, 4, 5), (29.5, 7, 35), "wood", order=2),
        Box((4.25, 6, 4.25), (27.75, 9.5, 36), "white", order=3),
        Box((4.5, 9.5, 11), (27.5, 12.25, 35.75), "red", order=4),
        Box((4.5, 10.0, 4.5), (15.25, 13, 11), "red", order=4),
        Box((16.75, 10.0, 4.5), (27.5, 13, 11), "red", order=4),
    ])

    # Raised, inset-paneled headboard.
    boxes.extend([
        Box((3.25, 7, 2.75), (28.75, 9, 4.5), "wood", order=1),
        Box((3.25, 20.5, 2.75), (28.75, 23, 4.5), "wood", order=1),
        Box((3.25, 9, 2.75), (5.5, 20.5, 4.5), "wood", order=1),
        Box((26.5, 9, 2.75), (28.75, 20.5, 4.5), "wood", order=1),
        Box((5.5, 10, 3.15), (26.5, 19.5, 4.1), "wood", order=1),
        Box((15.2, 10, 2.9), (16.8, 20.5, 4.35), "wood", order=1),
    ])

    # Tester rails supporting the fabric canopy.
    boxes.extend([
        Box((2.5, 28.5, 1.25), (29.5, 30.5, 3.25), "wood", order=1),
        Box((1.25, 28.5, 3.25), (3.25, 30.5, 36.75), "wood", order=6),
        Box((28.75, 28.5, 3.25), (30.75, 30.5, 36.75), "wood", order=6),
        Box((2.5, 28.5, 36.75), (29.5, 30.5, 38.75), "wood", order=9),
    ])

    # Full red canopy, with a shallow perimeter valance and curtains gathered
    # toward each post so the bed remains visually open and usable.
    boxes.extend([
        Box((3.25, 27.9, 3.25), (28.75, 28.45, 36.75), "red", order=5),
        Box((3.25, 25.4, 3.25), (28.75, 27.9, 4.65), "red", order=5),
        Box((3.25, 25.4, 35.35), (28.75, 27.9, 36.75), "red", order=8),
        Box((3.25, 25.4, 4.65), (4.65, 27.9, 35.35), "red", order=6),
        Box((27.35, 25.4, 4.65), (28.75, 27.9, 35.35), "red", order=6),

        Box((3.35, 17.0, 3.4), (7.0, 25.4, 4.8), "red", order=5),
        Box((25.0, 17.0, 3.4), (28.65, 25.4, 4.8), "red", order=5),
        Box((3.35, 17.0, 35.2), (7.0, 25.4, 36.6), "red", order=7),
        Box((25.0, 17.0, 35.2), (28.65, 25.4, 36.6), "red", order=7),

        Box((3.35, 10.5, 3.4), (4.9, 17.0, 4.8), "red", order=5),
        Box((27.1, 10.5, 3.4), (28.65, 17.0, 4.8), "red", order=5),
        Box((3.35, 10.5, 35.2), (4.9, 17.0, 36.6), "red", order=7),
        Box((27.1, 10.5, 35.2), (28.65, 17.0, 36.6), "red", order=7),

        # Dark oak tie-backs echo the frame without introducing a new material.
        Box((3.1, 16.0, 3.2), (5.1, 17.1, 5.0), "wood", order=6),
        Box((26.9, 16.0, 3.2), (28.9, 17.1, 5.0), "wood", order=6),
        Box((3.1, 16.0, 35.0), (5.1, 17.1, 36.8), "wood", order=8),
        Box((26.9, 16.0, 35.0), (28.9, 17.1, 36.8), "wood", order=8),
    ])
    return boxes


def dimension(draw: ImageDraw.ImageDraw, start, end, label, offset=(0, 0)):
    draw.line([start, end], fill=GUIDE, width=2)
    dx, dy = end[0] - start[0], end[1] - start[1]
    length = max(1, math.hypot(dx, dy))
    nx, ny = -dy / length, dx / length
    for point in (start, end):
        draw.line([(point[0] - nx * 9, point[1] - ny * 9),
                   (point[0] + nx * 9, point[1] + ny * 9)], fill=GUIDE, width=2)
    bbox = draw.textbbox((0, 0), label, font=FONT_NOTE)
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    mx = (start[0] + end[0]) / 2 + offset[0]
    my = (start[1] + end[1]) / 2 + offset[1]
    draw.rounded_rectangle((mx - tw / 2 - 8, my - th / 2 - 6,
                            mx + tw / 2 + 8, my + th / 2 + 6), 5, fill=PANEL)
    draw.text((mx - tw / 2, my - th / 2 - 2), label, fill=MUTED, font=FONT_NOTE)


def label(draw, point, text, anchor):
    draw.line([anchor, point], fill=GUIDE, width=2)
    draw.ellipse((point[0] - 4, point[1] - 4, point[0] + 4, point[1] + 4), fill=RED)
    bbox = draw.textbbox((0, 0), text, font=FONT_LABEL)
    pad = 9
    if anchor[0] < point[0]:
        x = anchor[0] - (bbox[2] - bbox[0]) - pad
    else:
        x = anchor[0] + pad
    y = anchor[1] - 12
    draw.rounded_rectangle((x - 7, y - 5, x + bbox[2] - bbox[0] + 7, y + 27), 5, fill="#ffffff")
    draw.text((x, y), text, font=FONT_LABEL, fill=MUTED)


def draw_top_plan(canvas, box):
    draw = ImageDraw.Draw(canvas)
    x1, y1, x2, y2 = box
    draw.rounded_rectangle(box, radius=24, fill=PANEL)
    draw.text((x1 + 30, y1 + 24), "Footprint", font=FONT_CARD_TITLE, fill=INK)
    draw.text((x1 + 30, y1 + 61), "32 × 40 model units", font=FONT_CARD_SUB, fill=MUTED)

    scale = 7.0
    ox, oy = x1 + 72, y1 + 128
    # frame and mattress in top view
    draw.rectangle((ox, oy, ox + 32 * scale, oy + 40 * scale), fill="#4a2f1e", outline=LINE, width=3)
    draw.rectangle((ox + 4 * scale, oy + 4 * scale, ox + 28 * scale, oy + 36 * scale),
                   fill=RED, outline="#712018", width=3)
    for px, pz in ((.5, .5), (28.5, .5), (.5, 36.5), (28.5, 36.5)):
        draw.rectangle((ox + px * scale, oy + pz * scale,
                        ox + (px + 3) * scale, oy + (pz + 3) * scale),
                       fill="#312016", outline="#1e140e", width=2)
    draw.line((ox + 4 * scale, oy + 11 * scale, ox + 28 * scale, oy + 11 * scale), fill="#d96a61", width=3)
    dimension(draw, (ox, oy - 22), (ox + 32 * scale, oy - 22), "32 units / 2 blocks", (0, -16))
    draw.text((ox - 48, oy + 3), "Head", font=FONT_LABEL, fill=MUTED)
    draw.text((ox - 42, oy + 40 * scale - 22), "Foot", font=FONT_LABEL, fill=MUTED)
    draw.text((x1 + 30, y2 - 43), "Length: 40 units / 2.5 blocks", font=FONT_NOTE, fill=MUTED)


def main():
    image = Image.new("RGBA", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.text((70, 42), "Dark oak canopy bed", fill=INK, font=FONT_TITLE)
    draw.text((72, 98), "Block-model concept • stripped dark oak frame • red-dyed canopy and bedding", fill=MUTED, font=FONT_SUBTITLE)

    hero = (48, 154, 1110, 1068)
    draw.rounded_rectangle(hero, radius=26, fill=PANEL)
    draw.text((hero[0] + 32, hero[1] + 26), "Three-quarter preview", fill=INK, font=FONT_CARD_TITLE)
    draw.text((hero[0] + 32, hero[1] + 64), "Fabric canopy • tied-back corner curtains • paneled headboard", fill=MUTED, font=FONT_CARD_SUB)

    p = make_projection((154, 718), 15.5, 14.7, 8.05, 4.35)
    boxes = bed_boxes()
    for box in sorted(boxes, key=lambda b: b.depth):
        draw_box(image, p, box)

    # A soft footprint shadow and a few non-intrusive callouts.
    label(draw, p(16, 28.45, 26), "red fabric canopy", (926, 328))
    label(draw, p(24, 16, 4), "paneled headboard", (895, 560))
    label(draw, p(27.7, 17, 35.7), "tied-back curtains", (910, 680))
    label(draw, p(22, 12.3, 27), "red-dyed bedding", (920, 820))

    info = (1142, 154, 1552, 524)
    draw.rounded_rectangle(info, radius=24, fill=PANEL)
    draw.text((info[0] + 28, info[1] + 25), "Model notes", font=FONT_CARD_TITLE, fill=INK)
    notes = [
        "32 × 40 unit footprint",
        "32 units overall height",
        "3-unit square corner posts",
        "2-unit canopy rails",
        "Red blanket + two pillows",
        "Full canopy + corner drapes",
    ]
    y = info[1] + 83
    for note in notes:
        draw.ellipse((info[0] + 31, y + 7, info[0] + 39, y + 15), fill=RED)
        draw.text((info[0] + 54, y), note, font=FONT_NOTE, fill=MUTED)
        y += 42

    draw_top_plan(image, (1142, 552, 1552, 1068))
    image.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
