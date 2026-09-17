from __future__ import annotations

import io
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[2]
CLIENT_JAR = Path(r"C:\Users\vwner\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.4_client.jar")
OUTPUT = Path(__file__).with_name("cabinet_countertop_preview.png")

WIDTH, HEIGHT = 1600, 1180
BG = "#edeae3"
PANEL = "#f7f5f0"
INK = "#292723"
MUTED = "#6f6960"
LINE = "#4a453e"
GUIDE = "#a59e93"
OPENING = "#202222"
METAL = "#a58963"


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    name = "seguisb.ttf" if bold else "segoeui.ttf"
    path = Path(r"C:\Windows\Fonts") / name
    return ImageFont.truetype(str(path), size)


FONT_TITLE = font(42, True)
FONT_SUBTITLE = font(23)
FONT_CARD_TITLE = font(28, True)
FONT_CARD_SUB = font(20)
FONT_NOTE = font(18)


def load_vanilla_texture(name: str) -> Image.Image:
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        data = jar.read(f"assets/minecraft/textures/block/{name}.png")
    return Image.open(io.BytesIO(data)).convert("RGBA")


CALCITE = load_vanilla_texture("calcite")
DARK_OAK = load_vanilla_texture("dark_oak_planks")


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
    source_points = [(0, 0), (255, 0), (255, 255), (0, 255)]
    coeffs = perspective_coefficients(destination, source_points)
    warped = source.transform(canvas.size, Image.Transform.PERSPECTIVE, coeffs, Image.Resampling.BICUBIC)
    mask = Image.new("L", canvas.size, 0)
    ImageDraw.Draw(mask).polygon(points, fill=255)
    warped.putalpha(mask)
    canvas.alpha_composite(warped)
    if outline:
        ImageDraw.Draw(canvas).line([*points, points[0]], fill=outline, width=width, joint="curve")


def polygon(draw: ImageDraw.ImageDraw, points, fill, outline=LINE, width=2):
    draw.polygon(points, fill=fill)
    if outline:
        draw.line([*points, points[0]], fill=outline, width=width, joint="curve")


def arrow(draw: ImageDraw.ImageDraw, start, end, label):
    draw.line([start, end], fill=MUTED, width=3)
    angle = math.atan2(end[1] - start[1], end[0] - start[0])
    for offset in (2.55, -2.55):
        tip = (end[0] + 14 * math.cos(angle + offset), end[1] + 14 * math.sin(angle + offset))
        draw.line([end, tip], fill=MUTED, width=3)
    draw.text((start[0] - 4, start[1] + 10), label, fill=MUTED, font=FONT_NOTE)


def dimension(draw: ImageDraw.ImageDraw, start, end, label, label_offset=(0, -26)):
    draw.line([start, end], fill=GUIDE, width=2)
    dx, dy = end[0] - start[0], end[1] - start[1]
    length = max(1, math.hypot(dx, dy))
    nx, ny = -dy / length, dx / length
    for point in (start, end):
        draw.line([(point[0] - nx * 8, point[1] - ny * 8), (point[0] + nx * 8, point[1] + ny * 8)], fill=GUIDE, width=2)
    bbox = draw.textbbox((0, 0), label, font=FONT_NOTE)
    tw = bbox[2] - bbox[0]
    mid = ((start[0] + end[0]) / 2 + label_offset[0], (start[1] + end[1]) / 2 + label_offset[1])
    draw.rounded_rectangle((mid[0] - tw / 2 - 8, mid[1] - 2, mid[0] + tw / 2 + 8, mid[1] + 24), 5, fill=PANEL)
    draw.text((mid[0] - tw / 2, mid[1]), label, fill=MUTED, font=FONT_NOTE)


def card_base(canvas: Image.Image, box, title, subtitle):
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle(box, radius=26, fill=PANEL)
    x1, y1, _, _ = box
    draw.text((x1 + 32, y1 + 26), title, fill=INK, font=FONT_CARD_TITLE)
    draw.text((x1 + 32, y1 + 65), subtitle, fill=MUTED, font=FONT_CARD_SUB)


def make_projection(origin, sx, sy, szx, szy):
    ox, oy = origin

    def project(x, y, z):
        return (round(ox + x * sx + z * szx), round(oy - y * sy + z * szy))

    return project


def draw_top(canvas: Image.Image, box, sink=False):
    x1, y1, _, _ = box
    p = make_projection((x1 + 83, y1 + 252), 18, 18, 11, 6)
    draw = ImageDraw.Draw(canvas)

    back = [p(0, 0, 0), p(16, 0, 0), p(16, 2, 0), p(0, 2, 0)]
    side = [p(16, 0, 0), p(16, 0, 16), p(16, 2, 16), p(16, 2, 0)]
    front = [p(0, 0, 16), p(16, 0, 16), p(16, 2, 16), p(0, 2, 16)]
    top = [p(0, 2, 0), p(16, 2, 0), p(16, 2, 16), p(0, 2, 16)]

    textured_face(canvas, CALCITE, back, 0.79)
    textured_face(canvas, CALCITE, side, 0.72)
    textured_face(canvas, CALCITE, front, 0.84)
    textured_face(canvas, CALCITE, top, 1.05)

    if sink:
        cx, cz, rx, rz = 8, 10.4, 5.0, 3.45
        outer = [p(cx + (rx + .42) * math.cos(t), 2.07, cz + (rz + .42) * math.sin(t)) for t in np.linspace(0, 2 * math.pi, 72, endpoint=False)]
        hole = [p(cx + rx * math.cos(t), 2.09, cz + rz * math.sin(t)) for t in np.linspace(0, 2 * math.pi, 72, endpoint=False)]
        inner = [p(cx + (rx - .48) * math.cos(t), 1.78, cz + (rz - .48) * math.sin(t)) for t in np.linspace(0, 2 * math.pi, 72, endpoint=False)]
        polygon(draw, outer, "#aaa69d", outline="#726e67", width=2)
        polygon(draw, hole, OPENING, outline="#4d4f4f", width=2)
        draw.polygon(inner, fill="#363a3b")
        front_half = [p(cx + (rx - .55) * math.cos(t), 1.61, cz + (rz - .55) * math.sin(t)) for t in np.linspace(0, math.pi, 36)]
        draw.line(front_half, fill="#777a78", width=3)

    dimension(draw, p(0, -.8, 16), p(16, -.8, 16), "16 units wide", (0, 12))
    dimension(draw, p(16.8, 0, 16), p(16.8, 2, 16), "2 high", (42, -8))
    arrow(draw, (x1 + 530, y1 + 350), (x1 + 585, y1 + 326), "front")


def draw_panel_frame(canvas, draw, p, xa, xb, ya, yb, inset=0.62):
    outer = [p(xa, ya, 15.08), p(xb, ya, 15.08), p(xb, yb, 15.08), p(xa, yb, 15.08)]
    polygon(draw, outer, "#352116", outline="#24160f", width=3)
    face = [p(xa + .25, ya + .25, 15.13), p(xb - .25, ya + .25, 15.13), p(xb - .25, yb - .25, 15.13), p(xa + .25, yb - .25, 15.13)]
    textured_face(canvas, DARK_OAK, face, 0.94, outline="#5f3c27", width=2)
    inner = [p(xa + inset, ya + inset, 15.18), p(xb - inset, ya + inset, 15.18), p(xb - inset, yb - inset, 15.18), p(xa + inset, yb - inset, 15.18)]
    polygon(draw, inner, "#342117", outline="#24160f", width=2)
    inset_face = [p(xa + inset + .20, ya + inset + .20, 15.20), p(xb - inset - .20, ya + inset + .20, 15.20), p(xb - inset - .20, yb - inset - .20, 15.20), p(xa + inset + .20, yb - inset - .20, 15.20)]
    textured_face(canvas, DARK_OAK, inset_face, 0.72, outline="#6a442c", width=2)


def draw_cabinet(canvas: Image.Image, box, drawers=False):
    x1, y1, _, _ = box
    p = make_projection((x1 + 80, y1 + 373), 16, 16, 10, 6)
    draw = ImageDraw.Draw(canvas)

    right = [p(16, 0, 0), p(16, 0, 15), p(16, 16, 15), p(16, 16, 0)]
    front = [p(0, 0, 15), p(16, 0, 15), p(16, 16, 15), p(0, 16, 15)]
    top = [p(0, 16, 0), p(16, 16, 0), p(16, 16, 15), p(0, 16, 15)]
    textured_face(canvas, DARK_OAK, right, 0.62)
    textured_face(canvas, DARK_OAK, front, 0.82)
    textured_face(canvas, DARK_OAK, top, 1.02)

    toe = [p(.65, .25, 15.12), p(15.35, .25, 15.12), p(15.35, 1.3, 15.12), p(.65, 1.3, 15.12)]
    polygon(draw, toe, "#291a12", outline="#21140e", width=2)

    if drawers:
        draw_panel_frame(canvas, draw, p, .65, 15.35, 1.65, 7.70, .62)
        draw_panel_frame(canvas, draw, p, .65, 15.35, 8.25, 15.35, .62)
        for y in (5.1, 12.25):
            a, b = p(5.4, y, 15.28), p(10.6, y, 15.28)
            draw.line([a, b], fill="#493b2e", width=11)
            draw.line([a, b], fill=METAL, width=6)
            for point in (a, b):
                draw.ellipse((point[0] - 5, point[1] - 5, point[0] + 5, point[1] + 5), fill=METAL, outline="#655038", width=2)
    else:
        draw_panel_frame(canvas, draw, p, .65, 7.78, 1.65, 15.35, .78)
        draw_panel_frame(canvas, draw, p, 8.22, 15.35, 1.65, 15.35, .78)
        for x in (6.9, 9.1):
            point = p(x, 9.4, 15.33)
            draw.ellipse((point[0] - 7, point[1] - 7, point[0] + 7, point[1] + 7), fill=METAL, outline="#655038", width=2)

    dimension(draw, p(0, -.9, 15), p(16, -.9, 15), "16 units wide", (0, 16))
    dimension(draw, p(-1.2, 0, 15), p(-1.2, 16, 15), "16 high", (-40, 0))
    dimension(draw, p(16, 17.0, 0), p(16, 17.0, 15), "15 deep", (8, -24))
    arrow(draw, (x1 + 520, y1 + 432), (x1 + 578, y1 + 408), "front")


def main():
    image = Image.new("RGBA", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.text((70, 45), "Cabinet & countertop block model preview", fill=INK, font=FONT_TITLE)
    draw.text((72, 102), "Separate 16-unit block models • vanilla calcite and dark oak plank textures", fill=MUTED, font=FONT_SUBTITLE)

    cards = [
        (50, 165, 775, 625),
        (825, 165, 1550, 625),
        (50, 655, 775, 1135),
        (825, 655, 1550, 1135),
    ]
    card_base(image, cards[0], "Calcite countertop", "Smooth, flat top • bottom 2 units of its block space")
    card_base(image, cards[1], "Calcite sink", "Oval opening • centered side-to-side • biased toward front")
    card_base(image, cards[2], "Dark oak door cabinet", "Two framed doors side-by-side • full height • 15-unit depth")
    card_base(image, cards[3], "Dark oak drawer cabinet", "Two framed drawers stacked vertically • full height • 15-unit depth")

    draw_top(image, cards[0], sink=False)
    draw_top(image, cards[1], sink=True)
    draw_cabinet(image, cards[2], drawers=False)
    draw_cabinet(image, cards[3], drawers=True)

    image.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
