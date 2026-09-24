from __future__ import annotations

import importlib.util
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
SIDE_TABLE_PREVIEW = ROOT / "preview" / "side_table" / "generate_preview.py"
OUTPUT = Path(__file__).with_name("oak_nightstand_preview.png")

spec = importlib.util.spec_from_file_location("side_table_preview", SIDE_TABLE_PREVIEW)
preview = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(preview)


WIDTH, HEIGHT = preview.WIDTH, preview.HEIGHT
BG, PANEL = preview.BG, preview.PANEL
INK, MUTED = preview.INK, preview.MUTED
LINE, GUIDE = preview.LINE, preview.GUIDE


def draw_solid_cuboid(canvas: Image.Image, project, bounds, colors=("#d5b453", "#b8892f", "#916821"), width=2):
    x1, y1, z1, x2, y2, z2 = bounds
    top = [project(x1, y2, z1), project(x2, y2, z1), project(x2, y2, z2), project(x1, y2, z2)]
    right = [project(x2, y1, z1), project(x2, y2, z1), project(x2, y2, z2), project(x2, y1, z2)]
    front = [project(x1, y1, z2), project(x2, y1, z2), project(x2, y2, z2), project(x1, y2, z2)]
    draw = ImageDraw.Draw(canvas)
    for points, color in ((right, colors[2]), (front, colors[1]), (top, colors[0])):
        draw.polygon(points, fill=color)
        draw.line([*points, points[0]], fill=LINE, width=width, joint="curve")


CHAMFERED_TOP = [(1, 0), (15, 0), (16, 1), (16, 15), (15, 16), (1, 16), (0, 15), (0, 1)]


def draw_chamfered_top(canvas: Image.Image, project, width=2):
    bottom_y, top_y = 14, 16
    for start_index, end_index in ((2, 3), (3, 4), (4, 5)):
        x1, z1 = CHAMFERED_TOP[start_index]
        x2, z2 = CHAMFERED_TOP[end_index]
        face = [
            project(x1, bottom_y, z1),
            project(x2, bottom_y, z2),
            project(x2, top_y, z2),
            project(x1, top_y, z1),
        ]
        brightness = 0.72 if x1 == x2 else 0.84
        preview.textured_face(canvas, preview.OAK, face, brightness, width=width)

    top_outline = [project(x, top_y, z) for x, z in CHAMFERED_TOP]
    full_quad = [project(0, top_y, 0), project(16, top_y, 0), project(16, top_y, 16), project(0, top_y, 16)]
    preview.textured_polygon(canvas, preview.OAK, full_quad, top_outline, 1.08, width=width)


def draw_nightstand(canvas: Image.Image, origin, scale=1.0):
    project = preview.make_projection(origin, sx=20 * scale, sy=10 * scale, sh=20 * scale)
    line_width = max(1, round(2 * scale))

    # Four short, recessed feet and a substantial cabinet body.
    for bounds in (
        (2, 0, 2, 5, 2, 5),
        (11, 0, 2, 14, 2, 5),
        (2, 0, 11, 5, 2, 14),
        (11, 0, 11, 14, 2, 14),
        (1, 2, 1, 15, 14, 15),
    ):
        preview.draw_cuboid(canvas, project, bounds, width=line_width)

    # Raised drawer fronts. Both use the stripped-wood side grain on every face.
    for bounds in (
        (2, 8.4, 14.75, 14, 13.25, 15.35),
        (2, 3.0, 14.75, 14, 7.85, 15.35),
    ):
        preview.draw_cuboid(canvas, project, bounds, brightness=(1.02, 0.92, 0.72), width=line_width)

    # Raw-gold pulls match the yellowish handles used by the dresser models.
    for y in (10.55, 5.15):
        draw_solid_cuboid(canvas, project, (7.25, y, 15.35, 8.75, y + 0.8, 15.85), width=line_width)

    draw_chamfered_top(canvas, project, width=line_width)
    return project


def dimension(draw: ImageDraw.ImageDraw, start, end, label, label_pos):
    preview.dimension(draw, start, end, label, label_pos)


def draw_front_elevation(canvas: Image.Image, box):
    draw = ImageDraw.Draw(canvas)
    x1, y1, x2, y2 = box
    scale = 14
    ox = x1 + 173
    floor = y2 - 48

    def rect(xa, ya, xb, yb, fill, outline=LINE, width=2):
        points = (ox + xa * scale, floor - yb * scale, ox + xb * scale, floor - ya * scale)
        draw.rectangle(points, fill=fill, outline=outline, width=width)

    rect(2, 0, 5, 2, "#a4774b")
    rect(11, 0, 14, 2, "#8d6039")
    rect(1, 2, 15, 14, "#b98755")
    rect(2, 8.4, 14, 13.25, "#c3945e")
    rect(2, 3.0, 14, 7.85, "#bd8d58")
    rect(7.25, 10.55, 8.75, 11.35, "#b8892f")
    rect(7.25, 5.15, 8.75, 5.95, "#b8892f")
    rect(0, 14, 16, 16, "#d0a06a")

    draw.line([(ox, floor), (ox + 16 * scale, floor)], fill=GUIDE, width=2)
    dimension(draw, (ox, floor + 25), (ox + 16 * scale, floor + 25), "16-unit top", (ox + 92, floor + 31))
    dimension(draw, (ox - 27, floor), (ox - 27, floor - 16 * scale), "16 high", (ox - 73, floor - 145))
    draw.text((x1 + 28, y1 + 24), "Front elevation", fill=INK, font=preview.FONT_CARD_TITLE)
    draw.text((x1 + 28, y1 + 62), "Two drawers • centered pulls • recessed feet", fill=MUTED, font=preview.FONT_CARD_SUB)


def draw_spec_card(canvas: Image.Image, box):
    draw = ImageDraw.Draw(canvas)
    x1, y1, x2, y2 = box
    draw.text((x1 + 30, y1 + 25), "Block-space fit", fill=INK, font=preview.FONT_CARD_TITLE)
    draw.text((x1 + 30, y1 + 63), "Full height and contained inside one 16 × 16 block", fill=MUTED, font=preview.FONT_CARD_SUB)

    px, py = x1 + 38, y1 + 123
    cell = 13
    draw.rectangle((px, py, px + 16 * cell, py + 16 * cell), fill="#eeeae2")
    footprint_points = [(px + x * cell, py + z * cell) for x, z in CHAMFERED_TOP]
    draw.polygon(footprint_points, fill="#c18a50")
    for i in range(17):
        draw.line([(px + i * cell, py), (px + i * cell, py + 16 * cell)], fill="#d4cec4", width=1)
        draw.line([(px, py + i * cell), (px + 16 * cell, py + i * cell)], fill="#d4cec4", width=1)
    draw.rectangle((px, py, px + 16 * cell, py + 16 * cell), outline=LINE, width=3)
    draw.text((px + 22, py + 16 * cell + 10), "16 × 16 nominal top", fill=INK, font=preview.FONT_LABEL)

    notes_x = px + 250
    notes_y = py - 5
    rows = [
        ("Height", "16 units"),
        ("Drawers", "2 stacked"),
        ("Storage", "single chest"),
        ("Capacity", "27 slots"),
        ("Texture", "stripped wood"),
    ]
    for label, value in rows:
        draw.text((notes_x, notes_y), label.upper(), fill=MUTED, font=preview.FONT_NOTE)
        draw.text((notes_x, notes_y + 25), value, fill=INK, font=preview.FONT_LABEL)
        notes_y += 55


def main():
    image = Image.new("RGBA", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.text((62, 42), "Stripped oak nightstand — block model preview", fill=INK, font=preview.FONT_TITLE)
    draw.text((64, 100), "Two-drawer storage furniture • one block • chest-sized inventory", fill=MUTED, font=preview.FONT_SUBTITLE)

    hero = (45, 155, 850, 930)
    details = (885, 155, 1455, 520)
    footprint = (885, 550, 1455, 930)
    for box in (hero, details, footprint):
        draw.rounded_rectangle(box, radius=25, fill=PANEL)

    draw.text((hero[0] + 30, hero[1] + 25), "Three-quarter view", fill=INK, font=preview.FONT_CARD_TITLE)
    draw.text((hero[0] + 30, hero[1] + 63), "Stripped oak wood • no visible log-end grain", fill=MUTED, font=preview.FONT_CARD_SUB)
    draw_nightstand(image, (450, 625), scale=0.98)

    draw_front_elevation(image, details)
    draw_spec_card(image, footprint)

    image.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
