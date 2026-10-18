# Modpack author guide

[Manual contents](README.md) Â· [Catalog](catalog.md) Â· [Recipe reference](recipes.md)

Use this guide to change acquisition and presentation while keeping the mod's implemented behavior clear to players. It describes Minecraft 1.21.9 with the mod's current NeoForge target. Keep a copy of the unmodified recipes for comparison.

## Supported customization surfaces

| Surface | What it can change |
| --- | --- |
| Data-pack recipes | Inputs, output counts, and acquisition routes |
| Item/block tags | Membership used by recipes and vanilla/NeoForge systems |
| NeoForge data maps | Declared oxidation and waxing relationships; changes need careful testing |
| Resource-pack assets | Textures, models, blockstates, item definitions, language, and sounds |
| Java implementation | Inventories, placement rules, interactions, elevator limits; these are not exposed as general config settings |

There is no custom config file or custom command tree registered in this source. Do not advertise scripting-mod or recipe-viewer integration without testing your chosen integration; the presence of ordinary recipe JSON is not proof of a dedicated integration plugin.

## Recipe identifiers and formats

The namespace is `atouchofclass`. Recipe files live under `data/atouchofclass/recipe/` (singular `recipe`). For example, the key-ring recipe ID is `atouchofclass:key_ring`. Use the same namespace and path in a higher-priority data pack to override that recipe. Use your pack's own namespace for an additional recipe that should coexist.

Minecraft 1.21.9 uses **data-pack format 88.0** and **resource-pack format 69.0**. Use the required `min_format` and `max_format` fields in `pack.mcmeta`; these version-specific values must be reviewed when changing Minecraft targets. [Minecraft 1.21.9 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9)

### Worked example: make key rings cost iron ingots

Create a folder named `atoc-pack-tweaks` in the test world's `datapacks` directory. Put these files inside it, with `pack.mcmeta` at the root (not inside another extra folder):

```text
atoc-pack-tweaks/
  pack.mcmeta
  data/atouchofclass/recipe/key_ring.json
```

`pack.mcmeta`:

```json
{
  "pack": {
    "min_format": [88, 0],
    "max_format": [88, 0],
    "description": "A Touch of Class recipe tweaks for Minecraft 1.21.9"
  }
}
```

`data/atouchofclass/recipe/key_ring.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "equipment",
  "pattern": [" I ", "I I", " I "],
  "key": { "I": "minecraft:iron_ingot" },
  "result": { "id": "atouchofclass:key_ring", "count": 1 }
}
```

Load the test world and use the vanilla operator command `/reload`, then inspect `/datapack list enabled` and the log for loading errors. At a crafting table, check that four iron ingots in a diamond produce one ring and that the original nugget pattern no longer does. The recipe changes cost, not the ring's 16-slot capacity. This example is schema-matched to bundled recipes; its in-game reload test remains part of pack validation.

The [recipe reference](recipes.md) includes exact grids, ingredient IDs/tags, output quantities, and cooking times. `#minecraft:wool`, for example, means a tag choice rather than one specific wool color. Changing recipe tags can change the accepted ingredients for multiple recipes.

## Tags and material interoperability

The bundled resources include vanilla block/item categories such as doors, slabs, stairs, walls, and mineable tags. Copper furniture recipes use the vanilla copper nugget. Inspect the actual files before modifying membership; a tag's presence is not a promise that every similarly named item belongs to it.

For an additive item tag, use `data/<namespace>/tags/item/<path>.json` and a structure such as:

```json
{
  "replace": false,
  "values": ["minecraft:copper_nugget"]
}
```

This illustrates adding the vanilla copper nugget to a pack-defined item tag. Avoid `replace: true` unless removing the existing members is the intended pack-wide effect.

## Creative-only content and progression

Concrete countertops and sinks are intentionally Creative-only in the base mod. Their deferred recipes depend on a future target containing vanilla concrete slabs. You may deliberately add pack-specific acquisition, but document it as your pack's change rather than a missing base-mod recipe.

Aged copper lighting and chains can lack direct crafting recipes because oxidation provides them. Consider every route before claiming an item is unobtainable or gated: crafting, smelting, stonecutting, waxing, scraping, and in-world transformations. Decorative overlay items are reusable in the current implementation, while plaster consumes applications. Applied plaster drops nothing when stripped or when its supporting block is broken. That difference matters when balancing material costs.

## Resource-pack changes

Use `"min_format": [69, 0]` and `"max_format": [69, 0]` in the resource pack's `pack.mcmeta`, and mirror paths under `assets/atouchofclass/`. For example, replacing `textures/item/key_ring_empty.png` changes the empty-ring image; the filled state uses `textures/item/key_ring_filled.png`. Keep both states consistent. Override language entries in `assets/atouchofclass/lang/en_us.json` to change English labels.

Texture changes do not change collision, reserved placement volumes, inventory sizes, or access checks. A visual redesign of a tall door or bathtub must still fit the implemented spaces. Item definitions, models, and blockstates may select different textures by state, so inspect the relevant assets rather than replacing just the inventory image.

## Persistent data and compatibility boundaries

Tall doors and door keys use the component ID `atouchofclass:keyable_info_data_component_type`. Its data describes the code and descriptive name. Rings store item contents in Minecraft's container component. Wall overlays use dimension-level saved data, not a separate replacement block at each decorated face. Do not discard components or dimension saved data when moving content between worlds.

These details explain persistence; they are not a stable public programming API promise. Prefer normal item interactions for player-facing setup and preserve complete world backups before migration. Integration that replaces loot tables must preserve the tall-door component-copy function if coded drops are expected to retain their codes.

## Pack validation checklist

- Load a fresh world and an upgraded copy using the exact server/client mod set.
- Confirm recipe changes, including ingredient substitutions and the intended acquisition of every progression item.
- Test wall finishing/removal, container drops, and coded tall-door drops under any protection or loot-changing mods.
- Check all lamp stages and multiblock placement after resource changes.
- Test a two-floor elevator with two players; include mobs or vehicles only if the pack promises support for them.
- Restart and confirm stored inventories, ring contents, codes, and finishes persist.

Record the pack version alongside the mod version when reporting differences from this manual. There are no dedicated screenshots required for file-only configuration examples; screenshots belong to the player-facing behavior they change.
