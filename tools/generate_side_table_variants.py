"""Generate the shared side-table geometry and enabled stripped-wood variants."""

from __future__ import annotations

import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"

# Texture names intentionally use the side-grain texture on every face so each
# variant reads as a stripped-wood block rather than a stripped log.
WOODS = {
    "oak": {"label": "Oak", "texture": "stripped_oak_log", "slab": "oak_slab", "planks": "oak_planks"},
    "spruce": {"label": "Spruce", "texture": "stripped_spruce_log", "slab": "spruce_slab", "planks": "spruce_planks"},
    "birch": {"label": "Birch", "texture": "stripped_birch_log", "slab": "birch_slab", "planks": "birch_planks"},
    "poplar": {"label": "Poplar", "texture": "stripped_poplar_log", "slab": "poplar_slab", "planks": "poplar_planks"},
    "jungle": {"label": "Jungle", "texture": "stripped_jungle_log", "slab": "jungle_slab", "planks": "jungle_planks"},
    "acacia": {"label": "Acacia", "texture": "stripped_acacia_log", "slab": "acacia_slab", "planks": "acacia_planks"},
    "dark_oak": {"label": "Dark Oak", "texture": "stripped_dark_oak_log", "slab": "dark_oak_slab", "planks": "dark_oak_planks"},
    "mangrove": {"label": "Mangrove", "texture": "stripped_mangrove_log", "slab": "mangrove_slab", "planks": "mangrove_planks"},
    "cherry": {"label": "Cherry", "texture": "stripped_cherry_log", "slab": "cherry_slab", "planks": "cherry_planks"},
    "pale_oak": {"label": "Pale Oak", "texture": "stripped_pale_oak_log", "slab": "pale_oak_slab", "planks": "pale_oak_planks"},
    "bamboo": {"label": "Bamboo", "texture": "stripped_bamboo_block", "slab": "bamboo_slab", "planks": "bamboo_planks"},
    "crimson": {"label": "Crimson", "texture": "stripped_crimson_stem", "slab": "crimson_slab", "planks": "crimson_planks"},
    "warped": {"label": "Warped", "texture": "stripped_warped_stem", "slab": "warped_slab", "planks": "warped_planks"},
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
            "gui": {"rotation": [30, 225, 0], "scale": [0.625, 0.625, 0.625]},
            "ground": {"translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.7, 0.7, 0.7]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
        },
    })


def replace_recipe_materials(value: object, replacements: dict[str, str]) -> object:
    if isinstance(value, dict):
        return {key: replace_recipe_materials(child, replacements) for key, child in value.items()}
    if isinstance(value, list):
        return [replace_recipe_materials(child, replacements) for child in value]
    if isinstance(value, str):
        return replacements.get(value, value)
    return value


def variant_recipe(wood: str, definition: dict[str, str], oak_recipe: dict) -> dict:
    name = f"{wood}_side_table"
    return replace_recipe_materials(oak_recipe, {
        "minecraft:oak_slab": f"minecraft:{definition['slab']}",
        "minecraft:oak_planks": f"minecraft:{definition['planks']}",
        "minecraft:stripped_oak_log": f"minecraft:{definition['texture']}",
        "atouchofclass:oak_side_table": f"atouchofclass:{name}",
    })


def write_variant(wood: str, definition: dict[str, str], oak_recipe: dict) -> None:
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

    planks = f"minecraft:{definition['planks']}"
    # Oak is the hand-tuned source of truth. Never rewrite it; every other
    # recipe clones its complete structure and swaps only material/output IDs.
    if wood != "oak":
        write_json(DATA / "recipe" / f"{name}.json",
                   variant_recipe(wood, definition, oak_recipe))
    write_json(DATA / "advancement/recipes/decorations" / f"{name}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_planks": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": planks}]},
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"atouchofclass:{name}"},
            },
        },
        "requirements": [["has_planks", "has_the_recipe"]],
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
    oak_recipe_path = DATA / "recipe/oak_side_table.json"
    oak_recipe = json.loads(oak_recipe_path.read_text(encoding="utf-8"))
    write_shared_models()
    for wood, definition in WOODS.items():
        write_variant(wood, definition, oak_recipe)
    update_language()
    update_axe_tag()
    print("Generated side-table assets: " + ", ".join(WOODS))


if __name__ == "__main__":
    main()
