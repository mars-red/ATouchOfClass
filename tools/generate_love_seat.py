"""Generate the leather love-seat models, textures, and data assets."""

from __future__ import annotations

import json
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"
NAME = "leather_love_seat"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def png_chunk(kind: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))


def write_texture(path: Path, dark: bool = False) -> None:
    palette = (
        (151, 77, 38), (160, 84, 42), (143, 68, 33),
        (171, 93, 48), (132, 61, 31), (155, 73, 35),
    )
    rows = []
    for y in range(16):
        row = bytearray([0])
        for x in range(16):
            index = (x * 11 + y * 7 + x * y * 3 + (x // 4) * 5) % len(palette)
            red, green, blue = palette[index]
            if (dark):
                red, green, blue = int(red * 0.62), int(green * 0.58), int(blue * 0.55)
            row.extend((red, green, blue))
        rows.append(bytes(row))
    payload = b"".join(rows)
    png = b"\x89PNG\r\n\x1a\n"
    png += png_chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 2, 0, 0, 0))
    png += png_chunk(b"IDAT", zlib.compress(payload, 9))
    png += png_chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def box(x0: float, y0: float, z0: float,
        x1: float, y1: float, z1: float, material: str) -> tuple:
    return x0, y0, z0, x1, y1, z1, material


def love_seat_boxes() -> list[tuple]:
    boxes = [
        # Four dark wooden feet.
        box(1, 0, 2, 3.5, 2, 5, "wood"),
        box(1, 0, 13, 3.5, 2, 16, "wood"),
        box(28.5, 0, 2, 31, 2, 5, "wood"),
        box(28.5, 0, 13, 31, 2, 16, "wood"),
        # Upholstered base, arms, and back shell.
        box(1, 2, 1.5, 31, 6, 15.5, "leather"),
        box(0, 2, 1, 4, 13, 16, "leather"),
        box(28, 2, 1, 32, 13, 16, "leather"),
        box(3, 5, 11, 29, 16, 16, "leather"),
        # Two individual seat and back cushions.
        box(4, 5.5, 1.25, 15.5, 8, 11.5, "leather"),
        box(16.5, 5.5, 1.25, 28, 8, 11.5, "leather"),
        box(4, 8, 9.25, 15.5, 15.5, 13.25, "leather"),
        box(16.5, 8, 9.25, 28, 15.5, 13.25, "leather"),
        # Base and cushion piping.
        box(1, 5.5, 1, 31, 6, 1.5, "seam"),
    ]
    for x0, x1 in ((4, 15.5), (16.5, 28)):
        boxes.extend((
            box(x0, 5.5, 0.9, x1, 6, 1.25, "seam"),
            box(x0, 7.5, 0.9, x1, 8, 1.25, "seam"),
            box(x0, 8, 9, x0 + 0.45, 15.5, 9.25, "seam"),
            box(x1 - 0.45, 8, 9, x1, 15.5, 9.25, "seam"),
            box(x0, 8, 9, x1, 8.45, 9.25, "seam"),
            box(x0, 15.05, 9, x1, 15.5, 9.25, "seam"),
        ))
    return boxes


def faces(material: str) -> dict:
    texture = f"#{material}"
    return {face: {"texture": texture}
            for face in ("down", "up", "north", "south", "west", "east")}


def clipped_elements(boxes: list[tuple], min_x: int, max_x: int) -> list[dict]:
    elements = []
    for x0, y0, z0, x1, y1, z1, material in boxes:
        clipped_x0, clipped_x1 = max(x0, min_x), min(x1, max_x)
        if clipped_x0 >= clipped_x1:
            continue
        elements.append({
            "from": [clipped_x0 - min_x, y0, z0],
            "to": [clipped_x1 - min_x, y1, z1],
            "faces": faces(material),
        })
    return elements


def write_models() -> None:
    boxes = love_seat_boxes()
    textures = {
        "particle": "atouchofclass:block/cognac_leather",
        "leather": "atouchofclass:block/cognac_leather",
        "seam": "atouchofclass:block/cognac_leather_dark",
        "wood": "minecraft:block/stripped_dark_oak_log",
    }

    for side, bounds in {"left": (0, 16), "right": (16, 32)}.items():
        write_json(ASSETS / "models/block" / f"love_seat_{side}.json", {
            "parent": "minecraft:block/block",
            "ambientocclusion": True,
            "textures": {"particle": "#leather"},
            "elements": clipped_elements(boxes, *bounds),
        })
        write_json(ASSETS / "models/block" / f"{NAME}_{side}.json", {
            "parent": f"atouchofclass:block/love_seat_{side}",
            "textures": textures,
        })

    variants = {}
    for side in ("left", "right"):
        for facing, rotation in FACINGS.items():
            variants[f"facing={facing},side={side}"] = {
                "model": f"atouchofclass:block/{NAME}_{side}",
                "y": rotation,
            }
    write_json(ASSETS / "blockstates" / f"{NAME}.json", {"variants": variants})

    item_elements = []
    for x0, y0, z0, x1, y1, z1, material in boxes:
        item_elements.append({
            "from": [x0 * 0.5, y0 * 0.5 + 4, z0 * 0.5 + 4],
            "to": [x1 * 0.5, y1 * 0.5 + 4, z1 * 0.5 + 4],
            "faces": faces(material),
        })
    write_json(ASSETS / "models/item/love_seat.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "gui_light": "side",
        "textures": {"particle": "#leather"},
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
            "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.75, 0.75, 0.75]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.65, 0.65, 0.65]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 1.5, 0], "scale": [0.65, 0.65, 0.65]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 0], "scale": [0.75, 0.75, 0.75]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1, 0], "scale": [0.75, 0.75, 0.75]},
        },
        "elements": item_elements,
    })
    write_json(ASSETS / "models/item" / f"{NAME}.json", {
        "parent": "atouchofclass:item/love_seat",
        "textures": textures,
    })
    write_json(ASSETS / "items" / f"{NAME}.json", {
        "model": {"type": "minecraft:model", "model": f"atouchofclass:item/{NAME}"},
    })


def write_data() -> None:
    write_json(DATA / "loot_table/blocks" / f"{NAME}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": f"atouchofclass:{NAME}"}],
        }],
    })
    write_json(DATA / "recipe" / f"{NAME}.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "pattern": ["LLL", "WWW", "S S"],
        "key": {
            "L": "minecraft:leather",
            "W": "minecraft:brown_wool",
            "S": "minecraft:stripped_dark_oak_log",
        },
        "result": {"id": f"atouchofclass:{NAME}"},
    })
    write_json(DATA / "advancement/recipes/decorations" / f"{NAME}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_leather": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": "minecraft:leather"}]},
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"atouchofclass:{NAME}"},
            },
        },
        "requirements": [["has_leather", "has_the_recipe"]],
        "rewards": {"recipes": [f"atouchofclass:{NAME}"]},
    })


def update_language() -> None:
    path = ASSETS / "lang/en_us.json"
    contents = path.read_text(encoding="utf-8")
    additions = []
    for kind in ("block", "item"):
        key = f"{kind}.atouchofclass.{NAME}"
        if f'"{key}"' not in contents:
            additions.append(f'    "{key}": "Leather Love Seat"')
    if not additions:
        return
    end = contents.rfind("}")
    prefix = contents[:end].rstrip()
    if not prefix.endswith("{"):
        prefix += ","
    path.write_text(prefix + "\n" + ",\n".join(additions) + "\n}\n", encoding="utf-8")


def update_axe_tag() -> None:
    path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    contents = path.read_text(encoding="utf-8")
    entry = f"atouchofclass:{NAME}"
    if f'"{entry}"' in contents:
        return
    marker = "\n               ]"
    end = contents.rfind(marker)
    if end == -1:
        raise RuntimeError("Could not find axe-tag values closing bracket")
    prefix = contents[:end].rstrip()
    if not prefix.endswith("["):
        prefix += ","
    path.write_text(prefix + f'\n                   "{entry}"' + contents[end:], encoding="utf-8")


def main() -> None:
    write_texture(ASSETS / "textures/block/cognac_leather.png")
    write_texture(ASSETS / "textures/block/cognac_leather_dark.png", dark=True)
    write_models()
    write_data()
    update_language()
    update_axe_tag()
    print("Generated leather love-seat assets")


if __name__ == "__main__":
    main()
