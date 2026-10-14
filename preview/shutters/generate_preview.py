"""Generate reviewable Minecraft geometry and render three joined oak shutters.

Game assets are generated from this geometry by tools/generate_shutters.py.
"""
from __future__ import annotations

import io
import json
import math
import os
from pathlib import Path
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFont

HERE = Path(__file__).resolve().parent
CLIENT_JAR = Path(os.environ.get("MINECRAFT_CLIENT_JAR", str(
    Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_1.21.4_client.jar")))
TEXTURE = "minecraft:block/stripped_oak_log"
FACES = ("down", "up", "north", "south", "west", "east")


def element(start, end, rotation=None):
    result = {"from": start, "to": end,
              "faces": {face: {"texture": "#wood"} for face in FACES}}
    if rotation:
        result["rotation"] = rotation
    return result


def model(joined_below, joined_above):
    parts = [element([0, 0, 14], [2, 16, 16]),
             element([14, 0, 14], [16, 16, 16])]
    for stile in parts:
        if joined_below:
            del stile["faces"]["down"]
        if joined_above:
            del stile["faces"]["up"]
    if not joined_below:
        parts.append(element([2, 0, 14], [14, 1, 16]))
    if not joined_above:
        parts.append(element([2, 15, 14], [14, 16, 16]))
    # Repeating two-unit spacing crosses block boundaries without a double rail.
    # The outermost slat is omitted only at an exposed cap.
    for y in range(1, 16, 2):
        if (y == 1 and not joined_below) or (y == 15 and not joined_above):
            continue
        parts.append(element([2, y - .375, 14.1], [14, y + .375, 15.9],
                             {"origin": [8, y, 15], "axis": "x", "angle": 22.5}))
    return {"parent": "minecraft:block/block", "textures": {
        "particle": TEXTURE, "wood": TEXTURE}, "elements": parts}


def vertices(part, offset, state):
    lo, hi = part["from"], part["to"]
    points = np.array([[x, y, z] for x in (lo[0], hi[0])
                       for y in (lo[1], hi[1]) for z in (lo[2], hi[2])], dtype=float)
    rotation = part.get("rotation")
    if rotation:
        origin = np.array(rotation["origin"])
        a = math.radians(rotation["angle"])
        matrix = np.array([[1, 0, 0], [0, math.cos(a), -math.sin(a)],
                           [0, math.sin(a), math.cos(a)]])
        points = (points - origin) @ matrix.T + origin
    if state == "right":
        x, z = points[:, 0].copy(), points[:, 2].copy()
        points[:, 0], points[:, 2] = z - 14, 16 - x
    elif state == "left":
        x, z = points[:, 0].copy(), points[:, 2].copy()
        points[:, 0], points[:, 2] = 30 - z, x
    points[:, 1] += offset
    return points


# Winding points outward, with consistent grain on opposite faces.
QUADS = {"west": [0, 1, 3, 2], "east": [5, 4, 6, 7],
         "down": [0, 4, 5, 1], "up": [3, 7, 6, 2],
         "north": [4, 0, 2, 6], "south": [1, 5, 7, 3]}


def uv_for(part, face):
    x1, y1, z1 = part["from"]
    x2, y2, z2 = part["to"]
    # Match Minecraft's default box UV dimensions; narrow rails crop the
    # texture instead of squeezing a full block of grain into two pixels.
    return {"west": [z1, 16-y2, z2, 16-y1],
            "east": [16-z2, 16-y2, 16-z1, 16-y1],
            "down": [x1, 16-z2, x2, 16-z1],
            "up": [x1, z1, x2, z2],
            "north": [16-x2, 16-y2, 16-x1, 16-y1],
            "south": [x1, 16-y2, x2, 16-y1]}[face]


def texture_face(canvas, texture, points, uv, light):
    # Only warp into the face's bounding rectangle, keeping the render fast.
    xmin, ymin = np.floor(np.min(points, axis=0)).astype(int)
    xmax, ymax = np.ceil(np.max(points, axis=0)).astype(int)
    width, height = xmax-xmin+1, ymax-ymin+1
    if width < 2 or height < 2:
        return
    destination = np.array(points) - [xmin, ymin]
    u1, v1, u2, v2 = uv
    source = np.array([[u1, v2], [u2, v2], [u2, v1], [u1, v1]]) * 16
    equations, values = [], []
    for (x, y), (u, v) in zip(destination, source):
        equations += [[x, y, 1, 0, 0, 0, -u*x, -u*y],
                      [0, 0, 0, x, y, 1, -v*x, -v*y]]
        values += [u, v]
    coeffs = np.linalg.solve(equations, values)
    tex = ImageEnhance.Brightness(texture).enhance(light)
    warped = tex.transform((width, height), Image.Transform.PERSPECTIVE,
                           coeffs, Image.Resampling.NEAREST)
    mask = Image.new("L", (width, height))
    ImageDraw.Draw(mask).polygon([tuple(p) for p in destination], fill=255)
    warped.putalpha(mask)
    canvas.alpha_composite(warped, (int(xmin), int(ymin)))


def stack(canvas, models, texture, state, center, floor):
    scale = 11.5
    camera = np.array([.62, .24, .92])
    faces = []
    def project(p):
        x, y, z = p
        return [center + (x-8)*scale*.92 - (z-8)*scale*.62,
                floor - y*scale + (x-8)*scale*.12 + (z-8)*scale*.18]
    for index, m in enumerate(models):
        for part in m["elements"]:
            points = vertices(part, index*16, state)
            for name, ids in QUADS.items():
                if name not in part["faces"]:
                    continue
                quad = points[ids]
                normal = np.cross(quad[1]-quad[0], quad[2]-quad[0])
                normal /= np.linalg.norm(normal)
                if normal @ camera <= 0:
                    continue
                light = .67 + .30*max(0, normal @ (camera / np.linalg.norm(camera)))
                if normal[1] > .8:
                    light = 1.06
                faces.append((quad.mean(axis=0) @ camera, quad,
                              uv_for(part, name), light))
    for _, quad, uv, light in sorted(faces, key=lambda f: f[0]):
        texture_face(canvas, texture, [project(p) for p in quad], uv, light)


def font(size, bold=False):
    return ImageFont.truetype(str(Path("C:/Windows/Fonts") /
                                 ("seguisb.ttf" if bold else "segoeui.ttf")), size)


def main():
    HERE.mkdir(parents=True, exist_ok=True)
    models = [model(False, True), model(True, True), model(True, False)]
    for label, m in zip(("bottom", "middle", "top"), models):
        (HERE / f"oak_shutter_{label}.json").write_text(json.dumps(m, indent=2)+"\n")
    (HERE / "oak_shutter_single.json").write_text(json.dumps(model(False, False), indent=2)+"\n")
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        vanilla = json.loads(jar.read("assets/minecraft/models/block/stripped_oak_wood.json"))
        assert vanilla["textures"]["side"] == vanilla["textures"]["end"] == TEXTURE
        texture = Image.open(io.BytesIO(jar.read("assets/minecraft/textures/block/stripped_oak_log.png"))).convert("RGBA")
    texture = texture.resize((256, 256), Image.Resampling.NEAREST)
    canvas = Image.new("RGBA", (1440, 980), "#e9e5dc")
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 28), "Oak shutters", font=font(40, True), fill="#292621")
    draw.text((50, 82), "Three joined blocks  /  stripped oak wood on every face", font=font(23), fill="#6d675e")
    for left, state, title in ((40, "closed", "Closed"), (505, "left", "Open · left hinge"),
                               (970, "right", "Open · right hinge")):
        draw.rounded_rectangle((left, 133, left+430, 888), radius=20, fill="#f7f5f0")
        draw.text((left+26, 153), title, font=font(25, True), fill="#292621")
        draw.text((left+26, 190), "3 × one-block sections", font=font(19), fill="#6d675e")
        draw.ellipse((left+66, 837, left+331, 875), fill="#e3ded4")
        stack(canvas, models, texture, state, left+215, 845)
    draw.text((50, 913), "Continuous stiles · repeating angled louvers · caps only at the exposed ends", font=font(21), fill="#4f493f")
    canvas.convert("RGB").save(HERE / "oak_shutters_preview.png")
    # Geometry checks cover all three sections and both hinge transforms.
    for m in [*models, model(False, False)]:
        for part in m["elements"]:
            for state in ("closed", "left", "right"):
                points = vertices(part, 0, state)
                assert points.min() >= -1e-6 and points.max() <= 16+1e-6
    print(HERE / "oak_shutters_preview.png")


if __name__ == "__main__":
    main()
