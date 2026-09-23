"""Generate the one-wide leather chair models and data assets."""

from generate_love_seat import ASSETS, DATA, FACINGS, ROOT, box, faces, write_json

NAME = "leather_chair"


def chair_boxes() -> list[tuple]:
    boxes = [
        # Four dark wooden feet.
        box(1, 0, 2, 3.5, 2, 5, "wood"),
        box(1, 0, 13, 3.5, 2, 16, "wood"),
        box(12.5, 0, 2, 15, 2, 5, "wood"),
        box(12.5, 0, 13, 15, 2, 16, "wood"),
        # Upholstered base, arms, and back shell.
        box(1, 2, 1.5, 15, 6, 15.5, "leather"),
        box(0, 2, 1, 3.5, 13, 16, "leather"),
        box(12.5, 2, 1, 16, 13, 16, "leather"),
        box(3, 5, 11, 13, 16, 16, "leather"),
        # Seat and back cushions.
        box(3.5, 5.5, 1.25, 12.5, 8, 11.5, "leather"),
        box(3.5, 8, 9.25, 12.5, 15.5, 13.25, "leather"),
        # Base and cushion piping.
        box(1, 5.5, 1, 15, 6, 1.5, "seam"),
        box(3.5, 5.5, 0.9, 12.5, 6, 1.25, "seam"),
        box(3.5, 7.5, 0.9, 12.5, 8, 1.25, "seam"),
        box(3.5, 8, 9, 3.95, 15.5, 9.25, "seam"),
        box(12.05, 8, 9, 12.5, 15.5, 9.25, "seam"),
        box(3.5, 8, 9, 12.5, 8.45, 9.25, "seam"),
        box(3.5, 15.05, 9, 12.5, 15.5, 9.25, "seam"),
    ]
    return boxes


def textures() -> dict[str, str]:
    return {
        "particle": "atouchofclass:block/cognac_leather",
        "leather": "atouchofclass:block/cognac_leather",
        "seam": "atouchofclass:block/cognac_leather_dark",
        "wood": "minecraft:block/stripped_dark_oak_log",
    }


def elements() -> list[dict]:
    return [{
        "from": [x0, y0, z0],
        "to": [x1, y1, z1],
        "faces": faces(material),
    } for x0, y0, z0, x1, y1, z1, material in chair_boxes()]


def write_models() -> None:
    write_json(ASSETS / "models/block/chair.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "textures": {"particle": "#leather"},
        "elements": elements(),
    })
    write_json(ASSETS / "models/block" / f"{NAME}.json", {
        "parent": "atouchofclass:block/chair",
        "textures": textures(),
    })
    write_json(ASSETS / "blockstates" / f"{NAME}.json", {
        "variants": {
            f"facing={facing}": {
                "model": f"atouchofclass:block/{NAME}",
                "y": rotation,
            } for facing, rotation in FACINGS.items()
        }
    })

    write_json(ASSETS / "models/item/chair.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "gui_light": "side",
        "textures": {"particle": "#leather"},
        "display": {
            "gui": {"rotation": [30, 225, 0], "scale": [0.85, 0.85, 0.85]},
            "ground": {"translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.7, 0.7, 0.7]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 1.5, 0], "scale": [0.55, 0.55, 0.55]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1, 0], "scale": [0.65, 0.65, 0.65]},
        },
        "elements": elements(),
    })
    write_json(ASSETS / "models/item" / f"{NAME}.json", {
        "parent": "atouchofclass:item/chair",
        "textures": textures(),
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
        "pattern": ["L", "W", "S"],
        "key": {
            "L": "minecraft:leather",
            "W": "#minecraft:wool",
            "S": "#minecraft:wooden_slabs",
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
            additions.append(f'    "{key}": "Leather Chair"')
    if additions:
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
    write_models()
    write_data()
    update_language()
    update_axe_tag()
    print("Generated leather chair assets")


if __name__ == "__main__":
    main()
