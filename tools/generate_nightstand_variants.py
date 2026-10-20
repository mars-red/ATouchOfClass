"""Generate non-oak nightstands from the finalized oak source variant.

The oak recipe and oak asset files are never written by this script. In
particular, the complete oak recipe is cloned before material/output IDs are
substituted, so hand-edited fields such as the output count are preserved.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"
VARIANTS_JAVA = (
    ROOT
    / "src/main/java/com/digitaldownpour/atouchofclass/NightstandVariants.java"
)

WOODS = {
    "oak": {
        "label": "Oak",
        "texture": "stripped_oak_log",
        "slab": "oak_slab",
        "planks": "oak_planks",
        "source": "STRIPPED_OAK_WOOD",
    },
    "spruce": {
        "label": "Spruce",
        "texture": "stripped_spruce_log",
        "slab": "spruce_slab",
        "planks": "spruce_planks",
        "source": "STRIPPED_SPRUCE_WOOD",
    },
    "birch": {
        "label": "Birch",
        "texture": "stripped_birch_log",
        "slab": "birch_slab",
        "planks": "birch_planks",
        "source": "STRIPPED_BIRCH_WOOD",
    },
    "poplar": {
        "label": "Poplar",
        "texture": "stripped_poplar_log",
        "slab": "poplar_slab",
        "planks": "poplar_planks",
        "source": "STRIPPED_POPLAR_WOOD",
    },
    "jungle": {
        "label": "Jungle",
        "texture": "stripped_jungle_log",
        "slab": "jungle_slab",
        "planks": "jungle_planks",
        "source": "STRIPPED_JUNGLE_WOOD",
    },
    "acacia": {
        "label": "Acacia",
        "texture": "stripped_acacia_log",
        "slab": "acacia_slab",
        "planks": "acacia_planks",
        "source": "STRIPPED_ACACIA_WOOD",
    },
    "dark_oak": {
        "label": "Dark Oak",
        "texture": "stripped_dark_oak_log",
        "slab": "dark_oak_slab",
        "planks": "dark_oak_planks",
        "source": "STRIPPED_DARK_OAK_WOOD",
    },
    "mangrove": {
        "label": "Mangrove",
        "texture": "stripped_mangrove_log",
        "slab": "mangrove_slab",
        "planks": "mangrove_planks",
        "source": "STRIPPED_MANGROVE_WOOD",
    },
    "cherry": {
        "label": "Cherry",
        "texture": "stripped_cherry_log",
        "slab": "cherry_slab",
        "planks": "cherry_planks",
        "source": "STRIPPED_CHERRY_WOOD",
    },
    "pale_oak": {
        "label": "Pale Oak",
        "texture": "stripped_pale_oak_log",
        "slab": "pale_oak_slab",
        "planks": "pale_oak_planks",
        "source": "STRIPPED_PALE_OAK_WOOD",
    },
    "bamboo": {
        "label": "Bamboo",
        "texture": "stripped_bamboo_block",
        "slab": "bamboo_slab",
        "planks": "bamboo_planks",
        "source": "STRIPPED_BAMBOO_BLOCK",
    },
    "crimson": {
        "label": "Crimson",
        "texture": "stripped_crimson_stem",
        "slab": "crimson_slab",
        "planks": "crimson_planks",
        "source": "STRIPPED_CRIMSON_HYPHAE",
    },
    "warped": {
        "label": "Warped",
        "texture": "stripped_warped_stem",
        "slab": "warped_slab",
        "planks": "warped_planks",
        "source": "STRIPPED_WARPED_HYPHAE",
    },
}


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def replace_recipe_ids(value: object, replacements: dict[str, str]) -> object:
    if isinstance(value, dict):
        return {
            key: replace_recipe_ids(child, replacements)
            for key, child in value.items()
        }
    if isinstance(value, list):
        return [replace_recipe_ids(child, replacements) for child in value]
    if isinstance(value, str):
        return replacements.get(value, value)
    return value


def variant_recipe(wood: str, definition: dict[str, str], oak_recipe: dict) -> dict:
    """Clone every oak recipe field, including result.count, then swap IDs."""
    return replace_recipe_ids(
        oak_recipe,
        {
            "minecraft:oak_slab": f"minecraft:{definition['slab']}",
            "minecraft:oak_planks": f"minecraft:{definition['planks']}",
            "atouchofclass:oak_nightstand": f"atouchofclass:{wood}_nightstand",
        },
    )


def blockstate(name: str) -> dict:
    model = f"atouchofclass:block/{name}"
    return {
        "variants": {
            "facing=north": {"model": model},
            "facing=east": {"model": model, "y": 90},
            "facing=south": {"model": model, "y": 180},
            "facing=west": {"model": model, "y": 270},
        }
    }


def write_variant(wood: str, definition: dict[str, str], oak_recipe: dict) -> None:
    if wood == "oak":
        return

    name = f"{wood}_nightstand"
    texture = f"minecraft:block/{definition['texture']}"
    textures = {
        "particle": texture,
        "wood": texture,
        "handle": "minecraft:block/raw_gold_block",
    }

    write_json(
        ASSETS / "models/block" / f"{name}.json",
        {"parent": "atouchofclass:block/nightstand", "textures": textures},
    )
    write_json(ASSETS / "blockstates" / f"{name}.json", blockstate(name))
    write_json(
        ASSETS / "models/item" / f"{name}.json",
        {"parent": "atouchofclass:item/nightstand", "textures": textures},
    )
    write_json(
        ASSETS / "items" / f"{name}.json",
        {"model": {"type": "minecraft:model", "model": f"atouchofclass:item/{name}"}},
    )
    write_json(
        DATA / "loot_table/blocks" / f"{name}.json",
        {
            "type": "minecraft:block",
            "pools": [
                {
                    "rolls": 1,
                    "conditions": [{"condition": "minecraft:survives_explosion"}],
                    "entries": [
                        {"type": "minecraft:item", "name": f"atouchofclass:{name}"}
                    ],
                }
            ],
        },
    )
    write_json(
        DATA / "recipe" / f"{name}.json",
        variant_recipe(wood, definition, oak_recipe),
    )
    write_json(
        DATA / "advancement/recipes/decorations" / f"{name}.json",
        {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_planks": {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {
                        "items": [{"items": f"minecraft:{definition['planks']}"}]
                    },
                },
                "has_the_recipe": {
                    "trigger": "minecraft:recipe_unlocked",
                    "conditions": {"recipe": f"atouchofclass:{name}"},
                },
            },
            "requirements": [["has_planks", "has_the_recipe"]],
            "rewards": {"recipes": [f"atouchofclass:{name}"]},
        },
    )


def update_java_definitions() -> None:
    contents = VARIANTS_JAVA.read_text(encoding="utf-8")
    start_marker = "            // GENERATED_NIGHTSTAND_DEFINITIONS_START\n"
    end_marker = "            // GENERATED_NIGHTSTAND_DEFINITIONS_END"
    start = contents.index(start_marker) + len(start_marker)
    end = contents.index(end_marker, start)
    definitions = ",\n".join(
        f'            new Definition("{wood}", Blocks.{definition["source"]})'
        for wood, definition in WOODS.items()
    )
    VARIANTS_JAVA.write_text(
        contents[:start] + definitions + "\n" + contents[end:], encoding="utf-8"
    )


def update_language() -> None:
    path = ASSETS / "lang/en_us.json"
    contents = path.read_text(encoding="utf-8-sig")
    additions = []
    for wood, definition in WOODS.items():
        name = f"{wood}_nightstand"
        for kind in ("block", "item"):
            key = f"{kind}.atouchofclass.{name}"
            if f'"{key}"' not in contents:
                additions.append(f'    "{key}": "{definition["label"]} Nightstand"')
    if not additions:
        return
    end = contents.rfind("}")
    prefix = contents[:end].rstrip()
    if not prefix.endswith("{"):
        prefix += ","
    path.write_text(prefix + "\n" + ",\n".join(additions) + "\n}\n", encoding="utf-8")


def update_axe_tag() -> None:
    path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    contents = path.read_text(encoding="utf-8-sig")
    additions = [
        f"atouchofclass:{wood}_nightstand"
        for wood in WOODS
        if f'"atouchofclass:{wood}_nightstand"' not in contents
    ]
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


def check_recipe_cloning(oak_recipe: dict) -> None:
    oak_count = oak_recipe.get("result", {}).get("count", 1)
    for wood, definition in WOODS.items():
        if wood == "oak":
            continue
        generated = variant_recipe(wood, definition, oak_recipe)
        generated_count = generated.get("result", {}).get("count", 1)
        if generated_count != oak_count:
            raise RuntimeError(
                f"{wood} output count {generated_count} does not match oak count {oak_count}"
            )
    print(f"Oak recipe output count {oak_count} will be preserved for every variant.")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check",
        action="store_true",
        help="validate oak recipe cloning without creating variants",
    )
    args = parser.parse_args()

    oak_recipe_path = DATA / "recipe/oak_nightstand.json"
    oak_recipe = json.loads(oak_recipe_path.read_text(encoding="utf-8-sig"))
    check_recipe_cloning(oak_recipe)
    if args.check:
        return

    for wood, definition in WOODS.items():
        write_variant(wood, definition, oak_recipe)
    update_java_definitions()
    update_language()
    update_axe_tag()
    print("Generated nightstand variants without modifying the oak source files.")


if __name__ == "__main__":
    main()
