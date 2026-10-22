"""Generate cube models with a shared UV rotation on all six faces.

Usage: python tools/generate_stochastic_models.py marble_tile marble_tile marble_tile_2
Texture names are relative to textures/block in the atouchofclass namespace.
Set the StochasticBlock variant count in ModBlocks to the number of textures.
"""
import argparse
import json
import re
from pathlib import Path

def generate(block, textures):
    if not 1 <= len(textures) <= 16:
        raise ValueError("Supply 1..16 textures")
    if any(not re.fullmatch(r"[a-z0-9_]+", name) for name in [block, *textures]):
        raise ValueError("Use simple lowercase resource names")
    root = Path(__file__).resolve().parents[1] / "src/main/resources/assets/atouchofclass"
    for texture in textures:
        if not (root / "textures/block" / f"{texture}.png").is_file():
            raise ValueError(f"Missing texture: {texture}.png")
    variants = {}
    for variant in range(16):
        # Unused reserved states have a valid fallback, including debug-stick states.
        selected = variant if variant < len(textures) else 0
        for rotation in range(4):
            model = f"{block}_variant_{selected}_rotation_{rotation}"
            variants[f"texture_variant={variant},texture_rotation={rotation}"] = {
                "model": f"atouchofclass:block/{model}"}
    for variant, texture in enumerate(textures):
        for rotation in range(4):
            faces = {face: {"texture": "#all", "uv": [0, 0, 16, 16],
                            "cullface": face, "rotation": rotation * 90}
                     for face in ("down", "up", "north", "south", "west", "east")}
            model = {"parent": "minecraft:block/block",
                     "textures": {"all": f"atouchofclass:block/{texture}",
                                  "particle": f"atouchofclass:block/{texture}"},
                     "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}]}
            path = root / "models/block" / f"{block}_variant_{variant}_rotation_{rotation}.json"
            path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
    (root / "blockstates" / f"{block}.json").write_text(
        json.dumps({"variants": variants}, indent=2) + "\n", encoding="utf-8")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("block")
    parser.add_argument("textures", nargs="+")
    args = parser.parse_args()
    generate(args.block, args.textures)
