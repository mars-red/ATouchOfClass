from __future__ import annotations

import io
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[2]
CLIENT_JAR = Path(r"C:\Users\vwner\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.4_client.jar")
OUTPUT = Path(__file__).with_name("oak_side_table_preview.png")

WIDTH, HEIGHT = 1500, 980
BG = "#e9e5dc"
PANEL = "#f7f5f0"
INK = "#292621"
MUTED = "#6d675e"
LINE = "#463d32"
GUIDE = "#999186"


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    filename = "seguisb.ttf" if bold else "segoeui.ttf"
    return ImageFont.truetype(str(Path(r"C:\Windows\Fonts") / filename), size)


FONT_TITLE = font(43, True)
FONT_SUBTITLE = font(23)
FONT_CARD_TITLE = font(27, True)
FONT_CARD_SUB = font(19)
FONT_NOTE = font(18)
FONT_LABEL = font(20, True)


def load_vanilla_texture(name: str) -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        data = jar.read(f"assets/minecraft/textures/block/{name}.png")
    return Image.open(io.BytesIO(data)).convert("RGBA")


OAK = load_vanilla_texture("stripped_oak_log")


def shade(texture: Image.Image, factor: float) -> Image.Image:
    return ImageEnhance.Brightness(texture.convert("RGB")).enhance(factor).convert("RGBA")


def perspective_coefficients(destination, source):
    matrix = []
    values = []
    for (x, y), (u, v) in zip(destination, source):
        matrix.append([x, y, 1, 0, 0, 0, -u * x, -u * y])
        values.append(u)
        matrix.append([0, 0, 0, x, y, 1, -v * x, -v * y])
        values.append(v)
    return np.linalg.solve(np.asarray(matrix), np.asarray(values))


def textured_face(canvas: Image.Image, texture: Image.Image, points, brightness=1.0, outline=LINE, width=2):
    source = shade(texture, brightness).resize((256, 256), Image.Resampling.NEAREST)
    destination = [tuple(map(float, point)) for point in points]
    coeffs = perspective_coefficients(destination, [(0, 0), (255, 0), (255, 255), (0, 255)])
    warped = source.transform(canvas.size, Image.Transform.PERSPECTIVE, coeffs, Image.Resampling.BICUBIC)
    mask = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(mask).polygon(points, fill=255)
    warped.putalpha(mask)
    canvas.alpha_composite(warped)
    ImageDraw.Draw(canvas).line([*points, points[0]], fill=outline, width=width, joint="curve")


def textured_polygon(canvas: Image.Image, texture: Image.Image, destination_quad, points, brightness=1.0, outline=LINE, width=2):
    source = shade(texture, brightness).resize((256, 256), Image.Resampling.NEAREST)
    destination = [tuple(map(float, point)) for point in destination_quad]
    coeffs = perspective_coefficients(destination, [(0, 0), (255, 0), (255, 255), (0, 255)])
    warped = source.transform(canvas.size, Image.Transform.PERSPECTIVE, coeffs, Image.Resampling.BICUBIC)
    mask = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(mask).polygon(points, fill=255)
    warped.putalpha(mask)
    canvas.alpha_composite(warped)
    ImageDraw.Draw(canvas).line([*points, points[0]], fill=outline, width=width, joint="curve")


def make_projection(origin, sx=20, sy=10, sh=20):
    ox, oy = origin

    def project(x, y, z):
        return (round(ox + (x - z) * sx), round(oy + (x + z) * sy - y * sh))

    return project


def draw_cuboid(canvas: Image.Image, project, bounds, brightness=(1.08, 0.84, 0.68), width=2):
    x1, y1, z1, x2, y2, z2 = bounds
    top = [project(x1, y2, z1), project(x2, y2, z1), project(x2, y2, z2), project(x1, y2, z2)]
    right = [project(x2, y1, z1), project(x2, y2, z1), project(x2, y2, z2), project(x2, y1, z2)]
    front = [project(x1, y1, z2), project(x2, y1, z2), project(x2, y2, z2), project(x1, y2, z2)]
    textured_face(canvas, OAK, right, brightness[2], width=width)
    textured_face(canvas, OAK, front, brightness[1], width=width)
    textured_face(canvas, OAK, top, brightness[0], width=width)


TABLE_PARTS = [
    # Four 2 x 2 legs, inset two units from the block boundary.
    (2, 0, 2, 4, 13, 4),
    (12, 0, 2, 14, 13, 4),
    (2, 0, 12, 4, 13, 14),
    (12, 0, 12, 14, 13, 14),
    # A simple apron below all four edges.
    (3.25, 11, 2.25, 12.75, 13, 3.25),
    (3.25, 11, 12.75, 12.75, 13, 13.75),
    (2.25, 11, 3.25, 3.25, 13, 12.75),
    (12.75, 11, 3.25, 13.75, 13, 12.75),
]

CHAMFERED_TOP = [(1, 0), (15, 0), (16, 1), (16, 15), (15, 16), (1, 16), (0, 15), (0, 1)]


def draw_chamfered_top(canvas: Image.Image, project, width=2):
    bottom_y, top_y = 13, 16
    # Only the five perimeter segments facing the camera are visible.
    visible_edges = [(2, 3), (3, 4), (4, 5)]
    for start_index, end_index in visible_edges:
        x1, z1 = CHAMFERED_TOP[start_index]
        x2, z2 = CHAMFERED_TOP[end_index]
        face = [
            project(x1, bottom_y, z1),
            project(x2, bottom_y, z2),
            project(x2, top_y, z2),
            project(x1, top_y, z1),
        ]
        brightness = 0.72 if x1 == x2 else 0.84
        textured_face(canvas, OAK, face, brightness, width=width)

    top_outline = [project(x, top_y, z) for x, z in CHAMFERED_TOP]
    full_quad = [project(0, top_y, 0), project(16, top_y, 0), project(16, top_y, 16), project(0, top_y, 16)]
    textured_polygon(canvas, OAK, full_quad, top_outline, 1.08, width=width)


def draw_table(canvas: Image.Image, origin, scale=1.0):
    project = make_projection(origin, sx=20 * scale, sy=10 * scale, sh=20 * scale)
    # Back legs and rails first, then front geometry and tabletop.
    order = [0, 1, 4, 2, 3, 5, 6, 7]
    for index in order:
        draw_cuboid(canvas, project, TABLE_PARTS[index], width=max(1, round(2 * scale)))
    draw_chamfered_top(canvas, project, width=max(1, round(2 * scale)))
    return project


def dimension(draw: ImageDraw.ImageDraw, start, end, label, label_pos):
    draw.line([start, end], fill=GUIDE, width=2)
    for x, y in (start, end):
        draw.line([(x - 7, y - 7), (x + 7, y + 7)], fill=GUIDE, width=2)
    box = draw.textbbox(label_pos, label, font=FONT_NOTE)
    draw.rounded_rectangle((box[0] - 7, box[1] - 3, box[2] + 7, box[3] + 4), radius=5, fill=PANEL)
    draw.text(label_pos, label, fill=MUTED, font=FONT_NOTE)


def draw_front_elevation(canvas: Image.Image, box):
    draw = ImageDraw.Draw(canvas)
    x1, y1, x2, y2 = box
    scale = 14
    ox = x1 + 173
    floor = y2 - 48

    def rect(xa, ya, xb, yb, fill):
        points = (ox + xa * scale, floor - yb * scale, ox + xb * scale, floor - ya * scale)
        draw.rectangle(points, fill=fill, outline=LINE, width=2)

    rect(2, 0, 4, 13, "#b88a57")
    rect(12, 0, 14, 13, "#9e7043")
    rect(3.25, 11, 12.75, 13, "#bd8d58")
    rect(0, 13, 16, 16, "#d0a06a")

    draw.line([(ox, floor), (ox + 16 * scale, floor)], fill=GUIDE, width=2)
    dimension(draw, (ox, floor + 25), (ox + 16 * scale, floor + 25), "16-unit top", (ox + 92, floor + 31))
    dimension(draw, (ox - 27, floor), (ox - 27, floor - 16 * scale), "16 high", (ox - 73, floor - 145))
    draw.text((x1 + 28, y1 + 24), "Front elevation", fill=INK, font=FONT_CARD_TITLE)
    draw.text((x1 + 28, y1 + 62), "Square legs • shallow apron • open underneath", fill=MUTED, font=FONT_CARD_SUB)


def draw_spec_card(canvas: Image.Image, box):
    draw = ImageDraw.Draw(canvas)
    x1, y1, x2, y2 = box
    draw.text((x1 + 30, y1 + 25), "Block-space fit", fill=INK, font=FONT_CARD_TITLE)
    draw.text((x1 + 30, y1 + 63), "Every part stays inside a single 16 × 16 block", fill=MUTED, font=FONT_CARD_SUB)

    px, py = x1 + 38, y1 + 123
    cell = 13
    draw.rectangle((px, py, px + 16 * cell, py + 16 * cell), fill="#eeeae2")
    footprint_points = [(px + x * cell, py + z * cell) for x, z in CHAMFERED_TOP]
    draw.polygon(footprint_points, fill="#c18a50")
    for i in range(17):
        draw.line([(px + i * cell, py), (px + i * cell, py + 16 * cell)], fill="#d4cec4", width=1)
        draw.line([(px, py + i * cell), (px + 16 * cell, py + i * cell)], fill="#d4cec4", width=1)
    draw.rectangle((px, py, px + 16 * cell, py + 16 * cell), outline=LINE, width=3)
    draw.text((px + 22, py + 16 * cell + 10), "16 × 16 nominal top", fill=INK, font=FONT_LABEL)

    notes_x = px + 250
    notes_y = py - 5
    rows = [
        ("Height", "16 units"),
        ("Top", "16 × 16 × 3"),
        ("Legs", "2 × 2 square"),
        ("Function", "decorative only"),
        ("Storage", "none"),
    ]
    for label, value in rows:
        draw.text((notes_x, notes_y), label.upper(), fill=MUTED, font=FONT_NOTE)
        draw.text((notes_x, notes_y + 25), value, fill=INK, font=FONT_LABEL)
        notes_y += 55


def main():
    image = Image.new("RGBA", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.text((62, 42), "Stripped oak side table — block model preview", fill=INK, font=FONT_TITLE)
    draw.text((64, 100), "Plain, compact furniture • one block • no storage", fill=MUTED, font=FONT_SUBTITLE)

    hero = (45, 155, 850, 930)
    details = (885, 155, 1455, 520)
    footprint = (885, 550, 1455, 930)
    for box in (hero, details, footprint):
        draw.rounded_rectangle(box, radius=25, fill=PANEL)

    draw.text((hero[0] + 30, hero[1] + 25), "Three-quarter view", fill=INK, font=FONT_CARD_TITLE)
    draw.text((hero[0] + 30, hero[1] + 63), "Stripped oak wood • subtly chamfered tabletop corners", fill=MUTED, font=FONT_CARD_SUB)
    draw_table(image, (450, 610), scale=0.98)

    draw_front_elevation(image, details)
    draw_spec_card(image, footprint)

    image.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
