"""Generate the shared dresser geometry and all stripped-wood variants."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"

WOODS = {
    "oak": "Oak",
    "spruce": "Spruce",
    "birch": "Birch",
    "jungle": "Jungle",
    "acacia": "Acacia",
    "dark_oak": "Dark Oak",
    "mangrove": "Mangrove",
    "cherry": "Cherry",
    "pale_oak": "Pale Oak",
    "bamboo": "Bamboo",
    "crimson": "Crimson",
    "warped": "Warped",
}
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
HEIGHT_SCALE = 20 / 24
PARTS = {
    "left_lower": (0, 16, 0, 16),
    "right_lower": (16, 32, 0, 16),
    "left_upper": (0, 16, 16, 32),
    "right_upper": (16, 32, 16, 32),
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def texture_name(wood: str) -> str:
    if wood == "bamboo":
        return "stripped_bamboo_block"
    suffix = "stem" if wood in ("crimson", "warped") else "log"
    return f"stripped_{wood}_{suffix}"


def stripped_item(wood: str) -> str:
    return f"minecraft:{texture_name(wood)}"


def dresser_id(wood: str) -> str:
    return f"{wood}_dresser"


def wood_box(x0: float, y0: float, z0: float,
             x1: float, y1: float, z1: float, rotate_top: bool = False) -> tuple:
    return (x0, y0 * HEIGHT_SCALE, z0, x1, y1 * HEIGHT_SCALE, z1, "wood", rotate_top)


def handle_box(x0: float, y0: float, z0: float,
               x1: float, y1: float, z1: float) -> tuple:
    return (x0, y0 * HEIGHT_SCALE, z0, x1, y1 * HEIGHT_SCALE, z1, "handle", False)


def dresser_boxes() -> list[tuple]:
    boxes = [
        # Four short feet and the lower plinth.
        wood_box(1, 0, 2, 4, 2, 5),
        wood_box(1, 0, 13, 4, 2, 16),
        wood_box(28, 0, 2, 31, 2, 5),
        wood_box(28, 0, 13, 31, 2, 16),
        wood_box(0.5, 2, 1.5, 31.5, 4, 16),
        # Solid carcass plus the period front frame.
        wood_box(1, 4, 2, 31, 22, 16),
        wood_box(1, 4, 1.25, 3, 22, 2),
        wood_box(15, 4, 1.25, 17, 22, 2),
        wood_box(29, 4, 1.25, 31, 22, 2),
        wood_box(3, 9.75, 1.25, 29, 10.5, 2),
        wood_box(3, 15.75, 1.25, 29, 16.5, 2),
        # Moulded cornice and top slab.
        wood_box(0.25, 21.5, 1.25, 31.75, 22.5, 16),
        wood_box(0, 22.5, 0.75, 32, 24, 16, rotate_top=True),
    ]

    drawer_rows = ((4.5, 9.75), (10.5, 15.75), (16.5, 21.5))
    drawer_columns = ((3.5, 14.5, 9.0), (17.5, 28.5, 23.0))
    for y0, y1 in drawer_rows:
        for x0, x1, center in drawer_columns:
            # Recessed drawer field with a narrow raised perimeter.
            boxes.extend((
                wood_box(x0, y0, 1.0, x1, y1, 1.5),
                wood_box(x0, y0, 0.75, x0 + 0.6, y1, 1.0),
                wood_box(x1 - 0.6, y0, 0.75, x1, y1, 1.0),
                wood_box(x0 + 0.6, y0, 0.75, x1 - 0.6, y0 + 0.6, 1.0),
                wood_box(x0 + 0.6, y1 - 0.6, 0.75, x1 - 0.6, y1, 1.0),
                handle_box(center - 0.75, (y0 + y1) / 2 - 0.75, 0,
                           center + 0.75, (y0 + y1) / 2 + 0.75, 0.75),
            ))
    return boxes


def faces(material: str, rotate_top: bool = False) -> dict:
    if material == "handle":
        return {face: {"texture": "#handle"}
                for face in ("down", "up", "north", "south", "west", "east")}
    # Stripped wood uses the bark-free side grain on every face rather than
    # exposing the circular end grain used by stripped logs.
    wood_faces = {face: {"texture": "#wood"}
                  for face in ("down", "up", "north", "south", "west", "east")}
    if rotate_top:
        wood_faces["up"]["rotation"] = 90
    return wood_faces


def clipped_elements(boxes: list[tuple], bounds: tuple[int, int, int, int]) -> list[dict]:
    min_x, max_x, min_y, max_y = bounds
    elements = []
    for x0, y0, z0, x1, y1, z1, material, rotate_top in boxes:
        clipped = (max(x0, min_x), max(y0, min_y), z0,
                   min(x1, max_x), min(y1, max_y), z1)
        if clipped[0] >= clipped[3] or clipped[1] >= clipped[4]:
            continue
        elements.append({
            "from": [clipped[0] - min_x, clipped[1] - min_y, clipped[2]],
            "to": [clipped[3] - min_x, clipped[4] - min_y, clipped[5]],
            "faces": faces(material, rotate_top),
        })
    return elements


def write_geometry() -> None:
    boxes = dresser_boxes()
    for part, bounds in PARTS.items():
        write_json(ASSETS / "models/block" / f"dresser_{part}.json", {
            "parent": "minecraft:block/block",
            "ambientocclusion": True,
            "textures": {"particle": "#wood"},
            "elements": clipped_elements(boxes, bounds),
        })

    # Keep item geometry inside the conventional 0..16 model cube. This avoids
    # oversized two-block geometry being clipped or offset in GUIs and hands.
    item_elements = []
    for x0, y0, z0, x1, y1, z1, material, rotate_top in boxes:
        item_elements.append({
            "from": [x0 * 0.5, y0 * 0.5, z0 * 0.5 + 4],
            "to": [x1 * 0.5, y1 * 0.5, z1 * 0.5 + 4],
            "faces": faces(material, rotate_top),
        })
    write_json(ASSETS / "models/item/dresser.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "gui_light": "side",
        "textures": {"particle": "#wood"},
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 1.5, 0], "scale": [0.9, 0.9, 0.9]},
            "ground": {"translation": [0, 3, 0], "scale": [0.5, 0.5, 0.5]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.75, 0.75, 0.75]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.65, 0.65, 0.65]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 2.5, 0], "scale": [0.65, 0.65, 0.65]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.75, 0.75, 0.75]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 2, 0], "scale": [0.75, 0.75, 0.75]},
        },
        "elements": item_elements,
    })


def write_variant(wood: str) -> None:
    name = dresser_id(wood)
    texture = texture_name(wood)
    textures = {
        "particle": f"minecraft:block/{texture}",
        "wood": f"minecraft:block/{texture}",
        "handle": "minecraft:block/raw_gold_block",
    }

    variants = {}
    for side in ("left", "right"):
        for upper in (False, True):
            suffix = f"{side}_{'upper' if upper else 'lower'}"
            for facing, rotation in FACINGS.items():
                variants[f"facing={facing},side={side},upper={str(upper).lower()}"] = {
                    "model": f"atouchofclass:block/{name}_{suffix}",
                    "y": rotation,
                }
            write_json(ASSETS / "models/block" / f"{name}_{suffix}.json", {
                "parent": f"atouchofclass:block/dresser_{suffix}",
                "textures": textures,
            })
    write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": variants})
    write_json(ASSETS / "models/item" / f"{name}.json", {
        "parent": "atouchofclass:item/dresser",
        "textures": textures,
    })
    write_json(ASSETS / "items" / f"{name}.json", {
        "model": {"type": "minecraft:model", "model": f"atouchofclass:item/{name}"},
    })
    write_json(DATA / "loot_table/blocks" / f"{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "conditions": [{"condition": "minecraft:survives_explosion"}],
            "entries": [{"type": "minecraft:item", "name": f"atouchofclass:{name}"}],
        }],
    })
    write_json(DATA / "recipe" / f"{name}.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "pattern": ["SS", "CC"],
        "key": {"S": stripped_item(wood), "C": "minecraft:chest"},
        "result": {"id": f"atouchofclass:{name}"},
    })
    write_json(DATA / "advancement/recipes/decorations" / f"{name}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_stripped_wood": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": stripped_item(wood)}]},
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"atouchofclass:{name}"},
            },
        },
        "requirements": [["has_stripped_wood", "has_the_recipe"]],
        "rewards": {"recipes": [f"atouchofclass:{name}"]},
    })


def update_language() -> None:
    path = ASSETS / "lang/en_us.json"
    contents = path.read_text(encoding="utf-8")
    additions = []
    for wood, label in WOODS.items():
        name = dresser_id(wood)
        for kind in ("block", "item"):
            key = f"{kind}.atouchofclass.{name}"
            if f'"{key}"' not in contents:
                additions.append(f'    "{key}": "{label} Dresser"')
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
    additions = [f"atouchofclass:{dresser_id(wood)}" for wood in WOODS
                 if f'"atouchofclass:{dresser_id(wood)}"' not in contents]
    if not additions:
        return
    marker = "\n               ]"
    end = contents.rfind(marker)
    if end == -1:
        raise RuntimeError("Could not find axe-tag values closing bracket")
    prefix = contents[:end].rstrip()
    if not prefix.endswith("["):
        prefix += ","
    entries = ",\n".join(f'                   "{name}"' for name in additions)
    path.write_text(prefix + "\n" + entries + contents[end:], encoding="utf-8")


def main() -> None:
    write_geometry()
    for wood in WOODS:
        write_variant(wood)
    update_language()
    update_axe_tag()
    print(f"Generated dresser assets and recipes for {len(WOODS)} stripped-wood variants")


if __name__ == "__main__":
    main()
