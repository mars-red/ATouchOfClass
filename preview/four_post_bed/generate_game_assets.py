"""Build the in-game block models from the approved reference-preview cuboids."""

from __future__ import annotations

import json
from pathlib import Path

from generate_reference_preview import reference_bed_boxes


ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"
NAME = "dark_oak_four_post_bed"

TEXTURES = {
    "particle": "minecraft:block/stripped_dark_oak_log",
    "wood": "minecraft:block/stripped_dark_oak_log",
    "end": "minecraft:block/stripped_dark_oak_log_top",
    "red": "minecraft:block/red_wool",
    "white": "minecraft:block/white_wool",
}


def write_json(path: Path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def element(coords, material):
    x0, y0, z0, x1, y1, z1 = (round(float(v), 4) for v in coords)
    if material == "wood":
        sides, ends = "#wood", "#end"
    else:
        sides = ends = f"#{material}"
    return {
        "from": [x0, y0, z0],
        "to": [x1, y1, z1],
        "faces": {
            "north": {"texture": sides}, "south": {"texture": sides},
            "east": {"texture": sides}, "west": {"texture": sides},
            "up": {"texture": ends}, "down": {"texture": ends},
        },
    }


def clip_box(box, side, section, upper):
    x0, y0, z0 = box.xyz0
    x1, y1, z1 = box.xyz1
    xbase = side * 16
    zbase = section * 16
    ybase = upper * 16
    a = max(x0, xbase), max(y0, ybase), max(z0, zbase)
    b = min(x1, xbase + 16), min(y1, ybase + 16), min(z1, zbase + 16)
    if any(lo >= hi for lo, hi in zip(a, b)):
        return None
    # The preview runs from head to foot along +Z. In-game the default facing
    # is north, so reverse each local Z slice; rotation then handles all sides.
    return (a[0] - xbase, a[1] - ybase, 16 - (b[2] - zbase),
            b[0] - xbase, b[1] - ybase, 16 - (a[2] - zbase))


def main():
    boxes = reference_bed_boxes()
    facings = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    item_elements = []

    for section, section_name in enumerate(("head", "middle", "foot")):
        for side, side_name in enumerate(("left", "right")):
            for upper in (0, 1):
                suffix = f"{section_name}_{side_name}_{'upper' if upper else 'lower'}"
                elements = []
                for box in boxes:
                    clipped = clip_box(box, side, section, upper)
                    if clipped:
                        elements.append(element(clipped, box.material))
                write_json(ASSETS / "models/block" / f"{NAME}_{suffix}.json", {
                    "parent": "minecraft:block/block",
                    "ambientocclusion": False,
                    "textures": TEXTURES,
                    "elements": elements,
                })
                for facing, rotation in facings.items():
                    variants[f"facing={facing},section={section_name},side={side_name},upper={'true' if upper else 'false'}"] = {
                        "model": f"atouchofclass:block/{NAME}_{suffix}",
                        "y": rotation,
                    }

    # The inventory model contains the entire bed at miniature scale.
    for box in boxes:
        x0, y0, z0 = box.xyz0
        x1, y1, z1 = box.xyz1
        coords = (
            8 + (x0 - 16) * .37,
            y0 * .37,
            8 + (z0 - 20) * .37,
            8 + (x1 - 16) * .37,
            y1 * .37,
            8 + (z1 - 20) * .37,
        )
        item_elements.append(element(coords, box.material))

    write_json(ASSETS / "blockstates" / f"{NAME}.json", {"variants": variants})
    write_json(ASSETS / "models/item" / f"{NAME}.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "textures": TEXTURES,
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [.9, .9, .9]},
            "ground": {"scale": [.5, .5, .5]},
            "fixed": {"scale": [.7, .7, .7]},
        },
        "elements": item_elements,
    })
    write_json(ASSETS / "items" / f"{NAME}.json", {
        "model": {"type": "minecraft:model", "model": f"atouchofclass:item/{NAME}"}
    })
    write_json(DATA / "loot_table/blocks" / f"{NAME}.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1, "conditions": [{"condition": "minecraft:survives_explosion"}],
                   "entries": [{"type": "minecraft:item", "name": f"atouchofclass:{NAME}"}]}],
    })
    print(f"Generated {12} bed section models and their block/item assets")


if __name__ == "__main__":
    main()
