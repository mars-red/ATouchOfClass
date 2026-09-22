from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

from generate_preview import (
    BG,
    FONT_CARD_SUB,
    FONT_CARD_TITLE,
    FONT_NOTE,
    FONT_SUBTITLE,
    FONT_TITLE,
    INK,
    MUTED,
    PANEL,
    Box,
    draw_box,
    draw_top_plan,
    label,
    make_projection,
)


OUTPUT = Path(__file__).with_name("reference_dark_oak_red_four_post_bed_preview.png")
WIDTH, HEIGHT = 1600, 1120


def add_ornate_post(boxes: list[Box], x: float, z: float, order: int):
    """Layered block-model interpretation of the reference's carved posts."""
    boxes.extend([
        Box((x, 0, z), (x + 4, 1.0, z + 4), order=order),
        Box((x + .35, 1.0, z + .35), (x + 3.65, 2.0, z + 3.65), order=order),
        Box((x + .65, 2.0, z + .65), (x + 3.35, 5.5, z + 3.35), order=order),
        Box((x + .85, 5.5, z + .85), (x + 3.15, 6.5, z + 3.15), order=order),
        Box((x + 1.1, 6.5, z + 1.1), (x + 2.9, 26.25, z + 2.9), order=order),
    ])

    # Raised bands break up the shaft like the carved spiral/ring work in the reference.
    for y in (8.4, 12.4, 16.4, 20.4, 24.4):
        boxes.append(Box((x + .82, y, z + .82), (x + 3.18, y + .55, z + 3.18), order=order))

    boxes.extend([
        Box((x + .75, 26.25, z + .75), (x + 3.25, 27.1, z + 3.25), order=order),
        Box((x + .45, 27.1, z + .45), (x + 3.55, 28.0, z + 3.55), order=order),
        Box((x + .15, 28.0, z + .15), (x + 3.85, 29.0, z + 3.85), order=order),
        Box((x + .55, 29.0, z + .55), (x + 3.45, 30.7, z + 3.45), order=order),
        Box((x, 30.7, z), (x + 4, 31.7, z + 4), order=order),
        Box((x + .4, 31.7, z + .4), (x + 3.6, 32.5, z + 3.6), order=order),
    ])


def add_headboard(boxes: list[Box]):
    # A stepped silhouette converts the reference's compound curves to legal cuboids.
    boxes.extend([
        Box((3.5, 6.0, 2.7), (28.5, 9.0, 4.8), order=1),
        Box((4.2, 9.0, 2.9), (27.8, 17.0, 4.6), order=1),
        Box((5.0, 17.0, 2.9), (27.0, 19.0, 4.6), order=1),
        Box((7.0, 19.0, 2.9), (25.0, 21.0, 4.6), order=1),
        Box((10.0, 21.0, 2.9), (22.0, 23.0, 4.6), order=1),
        Box((13.0, 23.0, 2.9), (19.0, 24.5, 4.6), order=1),
        Box((15.0, 24.5, 2.7), (17.0, 26.0, 4.8), order=1),
        # Projecting frame rails around the inset center panel.
        Box((4.0, 8.0, 4.55), (6.0, 18.0, 5.0), order=1),
        Box((26.0, 8.0, 4.55), (28.0, 18.0, 5.0), order=1),
        Box((6.0, 8.0, 4.55), (26.0, 10.0, 5.0), order=1),
        Box((6.0, 17.0, 4.55), (26.0, 19.0, 5.0), order=1),
        Box((10.0, 19.0, 4.55), (22.0, 21.0, 5.0), order=1),
        Box((13.0, 21.0, 4.55), (19.0, 23.0, 5.0), order=1),
    ])


def add_tester_rail(boxes: list[Box], axis: str, fixed: float, order: int):
    """Thin double rails and stepped tracery inspired by the wrought scrollwork."""
    if axis == "x":
        boxes.extend([
            Box((2.0, 29.2, fixed), (30.0, 30.0, fixed + .7), order=order),
            Box((2.0, 26.5, fixed), (30.0, 27.2, fixed + .7), order=order),
        ])
        for x in (5.0, 9.5, 14.0, 18.5, 23.0, 27.0):
            boxes.extend([
                Box((x, 27.2, fixed), (x + .45, 28.2, fixed + .7), order=order),
                Box((x + .45, 28.2, fixed), (x + 1.35, 28.65, fixed + .7), order=order),
                Box((x + 1.35, 27.2, fixed), (x + 1.8, 28.2, fixed + .7), order=order),
            ])
    else:
        boxes.extend([
            Box((fixed, 29.2, 2.0), (fixed + .7, 30.0, 38.0), order=order),
            Box((fixed, 26.5, 2.0), (fixed + .7, 27.2, 38.0), order=order),
        ])
        for z in (5.0, 10.5, 16.0, 21.5, 27.0, 32.5):
            boxes.extend([
                Box((fixed, 27.2, z), (fixed + .7, 28.2, z + .45), order=order),
                Box((fixed, 28.2, z + .45), (fixed + .7, 28.65, z + 1.55), order=order),
                Box((fixed, 27.2, z + 1.55), (fixed + .7, 28.2, z + 2.0), order=order),
            ])


def reference_bed_boxes():
    boxes: list[Box] = []

    # Back posts first; foot posts are deliberately drawn last.
    add_ornate_post(boxes, 0.0, 0.0, 0)
    add_ornate_post(boxes, 28.0, 0.0, 0)
    add_headboard(boxes)

    # Bed frame and raised mattress.
    boxes.extend([
        Box((2.8, 4.5, 3.5), (29.2, 7.0, 6.0), order=2),
        Box((2.8, 4.5, 5.0), (5.0, 7.0, 36.0), order=2),
        Box((27.0, 4.5, 5.0), (29.2, 7.0, 36.0), order=2),
        Box((4.5, 6.5, 4.5), (27.5, 9.5, 36.0), "white", order=3),
        Box((4.8, 9.5, 11.5), (27.2, 12.2, 35.7), "red", order=4),
        Box((4.8, 10.0, 4.8), (15.2, 13.0, 11.5), "red", order=4),
        Box((16.8, 10.0, 4.8), (27.2, 13.0, 11.5), "red", order=4),
    ])

    # Substantial framed footboard with a shallow stepped crown.
    boxes.extend([
        Box((2.8, 4.0, 35.5), (29.2, 14.0, 38.0), order=7),
        Box((4.2, 5.2, 37.95), (27.8, 12.7, 38.45), order=7),
        Box((4.0, 4.8, 38.4), (6.0, 13.4, 38.85), order=7),
        Box((26.0, 4.8, 38.4), (28.0, 13.4, 38.85), order=7),
        Box((6.0, 4.8, 38.4), (26.0, 6.3, 38.85), order=7),
        Box((6.0, 11.8, 38.4), (26.0, 13.4, 38.85), order=7),
        Box((6.0, 14.0, 35.4), (26.0, 15.0, 38.2), order=7),
        Box((9.0, 15.0, 35.4), (23.0, 15.8, 38.2), order=7),
        Box((13.0, 15.8, 35.4), (19.0, 16.4, 38.2), order=7),
    ])

    # Open ornamental tester frame, with no canopy fabric.
    add_tester_rail(boxes, "x", 1.7, 1)
    add_tester_rail(boxes, "z", 1.7, 5)
    add_tester_rail(boxes, "z", 29.6, 6)
    add_tester_rail(boxes, "x", 37.6, 8)

    add_ornate_post(boxes, 0.0, 36.0, 9)
    add_ornate_post(boxes, 28.0, 36.0, 9)
    return boxes


def main():
    image = Image.new("RGBA", (WIDTH, HEIGHT), BG)
    draw = ImageDraw.Draw(image)
    draw.text((70, 42), "Reference-inspired four-post bed", fill=INK, font=FONT_TITLE)
    draw.text((72, 98), "Block-model concept • stripped dark oak frame • red linens", fill=MUTED, font=FONT_SUBTITLE)

    hero = (48, 154, 1110, 1068)
    draw.rounded_rectangle(hero, radius=26, fill=PANEL)
    draw.text((hero[0] + 32, hero[1] + 26), "Three-quarter preview", fill=INK, font=FONT_CARD_TITLE)
    draw.text((hero[0] + 32, hero[1] + 64),
              "Ornamented posts • stepped crown headboard • decorative tester rails",
              fill=MUTED, font=FONT_CARD_SUB)

    p = make_projection((154, 718), 15.0, 14.2, 7.75, 4.2)
    for box in sorted(reference_bed_boxes(), key=lambda b: b.depth):
        draw_box(image, p, box)

    label(draw, p(30, 31.7, 2), "layered post capitals", (920, 290))
    label(draw, p(16, 24.5, 4.7), "crowned headboard", (910, 490))
    label(draw, p(22, 12.2, 26), "red linens", (925, 690))
    label(draw, p(18, 14.5, 38.4), "paneled footboard", (925, 820))

    notes = (1142, 154, 1552, 524)
    draw.rounded_rectangle(notes, radius=24, fill=PANEL)
    draw.text((notes[0] + 28, notes[1] + 25), "Model notes", font=FONT_CARD_TITLE, fill=INK)
    items = [
        "32 × 40 unit footprint",
        "32.5 units overall height",
        "Layered post bases + capitals",
        "Stepped ornamental headboard",
        "Framed raised footboard",
        "Open double-rail tester",
    ]
    y = notes[1] + 83
    for item in items:
        draw.ellipse((notes[0] + 31, y + 7, notes[0] + 39, y + 15), fill="#b02e26")
        draw.text((notes[0] + 54, y), item, font=FONT_NOTE, fill=MUTED)
        y += 42

    draw_top_plan(image, (1142, 552, 1552, 1068))
    image.convert("RGB").save(OUTPUT, quality=95)
    print(OUTPUT)


if __name__ == "__main__":
    main()
