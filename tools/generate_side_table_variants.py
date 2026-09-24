"""Generate the shared side-table geometry and enabled stripped-wood variants."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"

# Add woods here and in SideTableVariants.java after the oak design is final.
# Texture names intentionally use the side-grain texture on every face so each
# variant reads as a stripped-wood block rather than a stripped log.
WOODS = {
    "oak": {"label": "Oak", "texture": "stripped_oak_log", "slab": "oak_slab"},
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def wood_faces() -> dict:
    return {
        face: {"texture": "#wood"}
        for face in ("down", "up", "north", "south", "west", "east")
    }


def element(x0: float, y0: float, z0: float, x1: float, y1: float, z1: float) -> dict:
    return {
        "from": [x0, y0, z0],
        "to": [x1, y1, z1],
        "faces": wood_faces(),
    }


def geometry() -> list[dict]:
    return [
        # Four square legs extending into the apron.
        element(2, 0, 2, 4, 13, 4),
        element(12, 0, 2, 14, 13, 4),
        element(2, 0, 12, 4, 13, 14),
        element(12, 0, 12, 14, 13, 14),
        # Shallow rails below the top.
        element(3.25, 11, 2.25, 12.75, 13, 3.25),
        element(3.25, 11, 12.75, 12.75, 13, 13.75),
        element(2.25, 11, 3.25, 3.25, 13, 12.75),
        element(12.75, 11, 3.25, 13.75, 13, 12.75),
        # A 16-wide top with one-unit corner notches. The tiny steps are the
        # block-model approximation of the preview's lightly rounded corners.
        element(1, 13, 0, 15, 16, 16),
        element(0, 13, 1, 1, 16, 15),
        element(15, 13, 1, 16, 16, 15),
    ]


def write_shared_models() -> None:
    write_json(ASSETS / "models/block/side_table.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "textures": {"particle": "#wood"},
        "elements": geometry(),
    })
    write_json(ASSETS / "models/item/side_table.json", {
        "parent": "atouchofclass:block/side_table",
        "gui_light": "side",
        "display": {
            "gui": {"rotation": [30, 225, 0], "scale": [0.8, 0.8, 0.8]},
            "ground": {"translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.7, 0.7, 0.7]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
        },
    })


def write_variant(wood: str, definition: dict[str, str]) -> None:
    name = f"{wood}_side_table"
    texture = f"minecraft:block/{definition['texture']}"
    textures = {"particle": texture, "wood": texture}

    write_json(ASSETS / "models/block" / f"{name}.json", {
        "parent": "atouchofclass:block/side_table",
        "textures": textures,
    })
    write_json(ASSETS / "blockstates" / f"{name}.json", {
        "variants": {"": {"model": f"atouchofclass:block/{name}"}},
    })
    write_json(ASSETS / "models/item" / f"{name}.json", {
        "parent": "atouchofclass:item/side_table",
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

    stripped_wood = f"minecraft:{definition['texture']}"
    write_json(DATA / "recipe" / f"{name}.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "pattern": ["SSS", "L L", "L L"],
        "key": {
            "S": f"minecraft:{definition['slab']}",
            "L": stripped_wood,
        },
        "result": {"id": f"atouchofclass:{name}"},
    })
    write_json(DATA / "advancement/recipes/decorations" / f"{name}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_stripped_wood": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": stripped_wood}]},
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
    for wood, definition in WOODS.items():
        name = f"{wood}_side_table"
        label = f"{definition['label']} Side Table"
        for kind in ("block", "item"):
            key = f"{kind}.atouchofclass.{name}"
            if f'"{key}"' not in contents:
                additions.append(f'    "{key}": "{label}"')
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
    additions = [f"atouchofclass:{wood}_side_table" for wood in WOODS
                 if f'"atouchofclass:{wood}_side_table"' not in contents]
    if not additions:
        return
    marker = "\n               ]"
    end = contents.rfind(marker)
    if end == -1:
        raise RuntimeError("Could not find axe-tag values closing bracket")
    prefix = contents[:end].rstrip()
    if not prefix.endswith("["):
        prefix += ","
    entries = ",\n".join(f'                   "{entry}"' for entry in additions)
    path.write_text(prefix + "\n" + entries + contents[end:], encoding="utf-8")


def main() -> None:
    write_shared_models()
    for wood, definition in WOODS.items():
        write_variant(wood, definition)
    update_language()
    update_axe_tag()
    print("Generated side-table assets: " + ", ".join(WOODS))


if __name__ == "__main__":
    main()
