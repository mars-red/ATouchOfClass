"""Generate the three-wide leather sofa models and data assets."""

from generate_love_seat import ASSETS, DATA, FACINGS, ROOT, box, clipped_elements, faces, write_json

NAME = "leather_sofa"


def sofa_boxes() -> list[tuple]:
    boxes = [
        # Four dark wooden feet.
        box(1, 0, 2, 3.5, 2, 5, "wood"),
        box(1, 0, 13, 3.5, 2, 16, "wood"),
        box(44.5, 0, 2, 47, 2, 5, "wood"),
        box(44.5, 0, 13, 47, 2, 16, "wood"),
        # Upholstered base, outer arms, and back shell.
        box(1, 2, 1.5, 47, 6, 15.5, "leather"),
        box(0, 2, 1, 4, 13, 16, "leather"),
        box(44, 2, 1, 48, 13, 16, "leather"),
        box(3, 5, 11, 45, 16, 16, "leather"),
        # Base piping.
        box(1, 5.5, 1, 47, 6, 1.5, "seam"),
    ]
    cushions = ((4, 15.5), (16.25, 31.75), (32.5, 44))
    for x0, x1 in cushions:
        boxes.extend((
            box(x0, 5.5, 1.25, x1, 8, 11.5, "leather"),
            box(x0, 8, 9.25, x1, 15.5, 13.25, "leather"),
            box(x0, 5.5, 0.9, x1, 6, 1.25, "seam"),
            box(x0, 7.5, 0.9, x1, 8, 1.25, "seam"),
            box(x0, 8, 9, x0 + 0.45, 15.5, 9.25, "seam"),
            box(x1 - 0.45, 8, 9, x1, 15.5, 9.25, "seam"),
            box(x0, 8, 9, x1, 8.45, 9.25, "seam"),
            box(x0, 15.05, 9, x1, 15.5, 9.25, "seam"),
        ))
    return boxes


def textures() -> dict[str, str]:
    return {
        "particle": "atouchofclass:block/cognac_leather",
        "leather": "atouchofclass:block/cognac_leather",
        "seam": "atouchofclass:block/cognac_leather_dark",
        "wood": "minecraft:block/stripped_dark_oak_log",
    }


def write_models() -> None:
    boxes = sofa_boxes()
    sections = {"left": (0, 16), "middle": (16, 32), "right": (32, 48)}
    for section, bounds in sections.items():
        write_json(ASSETS / "models/block" / f"sofa_{section}.json", {
            "parent": "minecraft:block/block",
            "ambientocclusion": True,
            "textures": {"particle": "#leather"},
            "elements": clipped_elements(boxes, *bounds),
        })
        write_json(ASSETS / "models/block" / f"{NAME}_{section}.json", {
            "parent": f"atouchofclass:block/sofa_{section}",
            "textures": textures(),
        })

    variants = {}
    for section in sections:
        for facing, rotation in FACINGS.items():
            variants[f"facing={facing},section={section}"] = {
                "model": f"atouchofclass:block/{NAME}_{section}",
                "y": rotation,
            }
    write_json(ASSETS / "blockstates" / f"{NAME}.json", {"variants": variants})

    scale = 1 / 3
    inset = (16 - 16 * scale) / 2
    item_elements = []
    for x0, y0, z0, x1, y1, z1, material in boxes:
        item_elements.append({
            "from": [x0 * scale, y0 * scale + inset, z0 * scale + inset],
            "to": [x1 * scale, y1 * scale + inset, z1 * scale + inset],
            "faces": faces(material),
        })
    write_json(ASSETS / "models/item/sofa.json", {
        "parent": "minecraft:block/block",
        "ambientocclusion": True,
        "gui_light": "side",
        "textures": {"particle": "#leather"},
        "display": {
            "gui": {"rotation": [30, 225, 0], "scale": [1.05, 1.05, 1.05]},
            "ground": {"translation": [0, 2, 0], "scale": [0.55, 0.55, 0.55]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.85, 0.85, 0.85]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.75, 0.75, 0.75]},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 1.5, 0], "scale": [0.75, 0.75, 0.75]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1, 0], "scale": [0.85, 0.85, 0.85]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1, 0], "scale": [0.85, 0.85, 0.85]},
        },
        "elements": item_elements,
    })
    write_json(ASSETS / "models/item" / f"{NAME}.json", {
        "parent": "atouchofclass:item/sofa",
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
        "pattern": ["LLL", "WWW", "SSS"],
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
            additions.append(f'    "{key}": "Leather Sofa"')
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
    print("Generated leather sofa assets")


if __name__ == "__main__":
    main()
