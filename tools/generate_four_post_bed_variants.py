"""Generate every stripped-wood / dyed-linen version of the four-post bed.

The dark-oak/red geometry is the parent model. Every blockstate, including its
own, contains occupied and unoccupied states for the sleep behavior implemented
by FourPostBedBlock.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"
WOODS = (
    "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
    "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped",
)
COLORS = (
    "white", "orange", "magenta", "light_blue", "yellow", "lime",
    "pink", "gray", "light_gray", "cyan", "purple", "blue",
    "brown", "green", "red", "black",
)
SECTIONS = ("head", "middle", "foot")
SIDES = ("left", "right")
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}


def bed_id(wood: str, color: str) -> str:
    return f"{wood}_{color}_four_post_bed"


def write_json(path: Path, value: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def wood_texture(wood: str) -> str:
    if wood == "bamboo":
        return "stripped_bamboo_block"
    return f"stripped_{wood}_{'stem' if wood in ('crimson', 'warped') else 'log'}"


def log_tag(wood: str) -> str:
    if wood == "bamboo":
        return "minecraft:bamboo_blocks"
    return f"minecraft:{wood}_{'stems' if wood in ('crimson', 'warped') else 'logs'}"


def write_sleepable_blockstate(name: str) -> None:
    variants = {}
    for section in SECTIONS:
        for side in SIDES:
            for upper in (False, True):
                suffix = f"{section}_{side}_{'upper' if upper else 'lower'}"
                model = f"atouchofclass:block/{name}_{suffix}"
                for facing, rotation in FACINGS.items():
                    for occupied in (False, True):
                        variants[f"facing={facing},section={section},side={side},upper={str(upper).lower()},occupied={str(occupied).lower()}"] = {
                            "model": model, "y": rotation,
                        }
    write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": variants})


def generate_variant(wood: str, color: str) -> None:
    name = bed_id(wood, color)
    write_sleepable_blockstate(name)
    if name == "dark_oak_red_four_post_bed":
        return  # The geometry source has full models for this combination.
    texture = wood_texture(wood)
    textures = {
        "particle": f"minecraft:block/{texture}",
        "wood": f"minecraft:block/{texture}",
        "end": f"minecraft:block/{texture}_top",
        "red": f"minecraft:block/{color}_wool",
        "white": "minecraft:block/white_wool",
    }
    for section in SECTIONS:
        for side in SIDES:
            for upper in (False, True):
                suffix = f"{section}_{side}_{'upper' if upper else 'lower'}"
                write_json(ASSETS / "models/block" / f"{name}_{suffix}.json", {
                    "parent": f"atouchofclass:block/dark_oak_red_four_post_bed_{suffix}",
                    "textures": textures,
                })
    write_json(ASSETS / "models/item" / f"{name}.json", {
        "parent": "atouchofclass:item/dark_oak_red_four_post_bed", "textures": textures,
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
        "type": "minecraft:crafting_shaped", "category": "building",
        "pattern": ["LL", "BB", "LL"],
        "key": {"L": f"#{log_tag(wood)}", "B": f"minecraft:{color}_bed"},
        "result": {"id": f"atouchofclass:{name}"},
    })
    write_json(DATA / "advancement/recipes/building_blocks" / f"{name}.json", {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_bed": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": f"minecraft:{color}_bed"}]},
            },
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {"recipe": f"atouchofclass:{name}"},
            },
        },
        "requirements": [["has_bed", "has_the_recipe"]],
        "rewards": {"recipes": [f"atouchofclass:{name}"]},
    })


def update_language(names: list[tuple[str, str, str]]) -> None:
    path = ASSETS / "lang/en_us.json"
    contents = path.read_text(encoding="utf-8")
    marker = '    "block.atouchofclass.dark_oak_red_four_post_bed"'
    additions = []
    for wood, color, name in names:
        label = f"{wood.replace('_', ' ').title()} Four-Post Bed ({color.replace('_', ' ').title()})"
        for kind in ("block", "item"):
            key = f"{kind}.atouchofclass.{name}"
            if f'"{key}"' not in contents:
                additions.append(f'    "{key}": "{label}",\n')
    if additions:
        assert marker in contents
        path.write_text(contents.replace(marker, "".join(additions) + marker, 1), encoding="utf-8")


def update_axe_tag(names: list[tuple[str, str, str]]) -> None:
    path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    contents = path.read_text(encoding="utf-8")
    marker = '"atouchofclass:dark_oak_red_four_post_bed",'
    additions = []
    for _, _, name in names:
        entry = f'"atouchofclass:{name}"'
        if entry not in contents:
            additions.append(f'                   {entry},\n')
    if additions:
        assert marker in contents
        contents = contents.replace(marker, marker + "\n" + "".join(additions).rstrip("\n"), 1)
        path.write_text(contents, encoding="utf-8")


def main() -> None:
    names = [(wood, color, bed_id(wood, color)) for wood in WOODS for color in COLORS]
    for wood, color, _ in names:
        generate_variant(wood, color)
    new_names = [entry for entry in names if entry[2] != "dark_oak_red_four_post_bed"]
    update_language(new_names)
    update_axe_tag(new_names)
    print(f"Generated assets and recipes for {len(names)} bed combinations")


if __name__ == "__main__":
    main()
