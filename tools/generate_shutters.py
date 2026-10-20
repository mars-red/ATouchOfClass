"""Build all vanilla wood shutter models, items, recipes, unlocks, and loot."""
from pathlib import Path
import copy
import importlib.util
import json
import re

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("shutter_preview", ROOT / "preview/shutters/generate_preview.py")
preview = importlib.util.module_from_spec(spec)
spec.loader.exec_module(preview)
ASSETS = ROOT / "src/main/resources/assets/atouchofclass"
DATA = ROOT / "src/main/resources/data/atouchofclass"


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def generate_recipe(wood):
    """Shared recipe/unlock convention for oak and future wood variants.

    Four matching planks and two sticks craft eight shutters. Model textures
    remain stripped wood and are independent of the crafting ingredient.
    """
    shutter_id = f"atouchofclass:{wood}_shutter"
    planks = f"minecraft:{wood}_planks"
    write(DATA / f"recipe/{wood}_shutter.json", {
          "type": "minecraft:crafting_shaped", "category": "building",
          "pattern": ["WSW", "WSW"],
          "key": {"W": planks, "S": "minecraft:stick"},
          "result": {"id": shutter_id, "count": 8}})
    write(DATA / f"advancement/recipes/building_blocks/{wood}_shutter.json", {
          "parent": "minecraft:recipes/root", "criteria": {
              "has_material": {"trigger": "minecraft:inventory_changed", "conditions": {
                  "items": [{"items": planks}]}},
              "has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {
                  "recipes": [shutter_id]}}},
          "requirements": [["has_material", "has_the_recipe"]],
          "rewards": {"recipes": [shutter_id]}})


def opened(model, hinge):
    result = copy.deepcopy(model)
    # Left/right are viewed from outside the wall: north's left edge is east.
    def transform(p):
        x, y, z = p
        return [30-z, y, x] if hinge == "left" else [z-14, y, 16-x]
    # A rigid rotation remaps the faces as well as the element bounds.
    remap = ({"north": "east", "east": "south", "south": "west", "west": "north"}
             if hinge == "left" else
             {"north": "west", "west": "south", "south": "east", "east": "north"})
    for part in result["elements"]:
        # Explicit UVs preserve the closed model's grain through a hinge rotation.
        faces = {}
        for face in part["faces"]:
            faces[remap.get(face, face)] = {"texture": "#wood", "uv": preview.uv_for(part, face)}
            if face in ("up", "down"):
                faces[face]["rotation"] = (90 if hinge == "left" else 270) if face == "up" else (270 if hinge == "left" else 90)
        part["faces"] = faces
        a, b = transform(part["from"]), transform(part["to"])
        part["from"] = [min(a[i], b[i]) for i in range(3)]
        part["to"] = [max(a[i], b[i]) for i in range(3)]
        if "rotation" in part:
            part["rotation"]["origin"] = transform(part["rotation"]["origin"])
            part["rotation"]["axis"] = "z"
            if hinge == "right":
                part["rotation"]["angle"] *= -1
    return result


def generate_variant(wood):
    shutter = f"{wood}_shutter"
    texture = ("stripped_bamboo_block" if wood == "bamboo" else
               f"stripped_{wood}_stem" if wood in ("crimson", "warped") else f"stripped_{wood}_log")
    variants = {}
    for below, above, part in ((False, False, "single"), (False, True, "bottom"),
                               (True, True, "middle"), (True, False, "top")):
        closed = preview.model(below, above)
        closed["textures"] = {"particle": f"minecraft:block/{texture}", "wood": f"minecraft:block/{texture}"}
        closed["ambientocclusion"] = False
        write(ASSETS / f"models/block/{shutter}_{part}.json", closed)
        for hinge in ("left", "right"):
            write(ASSETS / f"models/block/{shutter}_{part}_open_{hinge}.json", opened(closed, hinge))
            for facing, rotation in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                for is_open in (False, True):
                    suffix = f"_open_{hinge}" if is_open else ""
                    key = (f"facing={facing},hinge={hinge},joined_above={str(above).lower()},"
                           f"joined_below={str(below).lower()},open={str(is_open).lower()}")
                    value = {"model": f"atouchofclass:block/{shutter}_{part}{suffix}"}
                    if rotation:
                        value["y"] = rotation
                    variants[key] = value
    write(ASSETS / f"blockstates/{shutter}.json", {"variants": variants})
    write(ASSETS / f"models/item/{shutter}.json", {"parent": f"atouchofclass:block/{shutter}_single"})
    write(ASSETS / f"items/{shutter}.json", {"model": {"type": "minecraft:model", "model": f"atouchofclass:item/{shutter}"}})
    generate_recipe(wood)
    write(DATA / f"loot_table/blocks/{shutter}.json", {"type": "minecraft:block", "pools": [{
          "rolls": 1, "condition": {"type": "minecraft:survives_explosion"},
          "entries": [{"type": "minecraft:item", "name": f"atouchofclass:{shutter}"}]}]})
    lang_path = ASSETS / "lang/en_us.json"
    lang = lang_path.read_text(encoding="utf-8")
    label_key = f"block.atouchofclass.{shutter}"
    if f'"{label_key}"' not in lang:
        label = wood.replace("_", " ").title() + " Shutter"
        lang_path.write_text(lang.replace("{", f'{{\n    "{label_key}": "{label}",', 1), encoding="utf-8")
    axe_path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json"
    axe = axe_path.read_text(encoding="utf-8")
    if f'"atouchofclass:{shutter}"' not in axe:
        axe_path.write_text(re.sub(r'("values"\s*:\s*\[)', rf'\1\n                   "atouchofclass:{shutter}",', axe, count=1), encoding="utf-8")


def main():
    source = (ROOT / "src/main/java/com/digitaldownpour/atouchofclass/ShutterVariants.java").read_text()
    woods = re.findall(r'new Definition\("([a-z_]+)"', source)
    if not woods or len(set(woods)) != len(woods):
        raise ValueError("Expected unique registered shutter materials")
    for wood in woods:
        generate_variant(wood)
    print(f"Generated {len(woods)} wood variants, each with 64 states, 12 models, item assets, recipe, unlock, and loot.")


if __name__ == "__main__":
    main()
