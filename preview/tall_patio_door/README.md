# Tall patio doors

The preview is rendered from the shipped block model cuboids, face UVs, and
Minecraft 1.21.4 textures. It is a model preview, not an in-game screenshot.

Each leaf is 16 × 48 model units with a 3-unit-thick wooden frame, matching
the existing tall doors. Two-unit stiles and outer rails surround a single
uninterrupted clear pane. Iron levers appear opposite the hinge on both sides.
Both handles sit in carved recesses and stay inside the frame's 3-unit depth.
The generated item uses a square 64 × 64 transparent texture, pre-filtered to
the native 16-pixel slot grid so both thin stiles remain visible. The complete
door is centered with one inventory pixel of top/bottom padding. The source
geometry preserves its 1:3 proportions before pixel-grid sampling.
The frame references `minecraft:block/stripped_oak_log`, the side texture used
on every face of vanilla stripped oak wood.

All twelve vanilla woods are registered with the existing tall door placement,
pairing, and locking behavior. `PatioDoorVariants.java` supplies the wood catalog
used by registration and the asset generator. Bamboo uses stripped bamboo block;
crimson and warped use stripped stems; other woods use stripped log textures.
The key cutter accepts every patio door variant and assigns its key code and
name. Coded doors require a matching individual key or a matching key on a
key ring; breaking and replacing them preserves the assigned key information.

Crafting uses three rows of `WGW`, with matching planks for `W` and glass blocks
for `G`, and yields four doors. Acquiring matching planks unlocks the recipe. Breaking
any door section removes the complete leaf and drops one door item with its
key information preserved.

Regenerate all block models, item models and centered icons, recipes, unlocks,
loot, language entries, tags, and the wood comparison preview with
`python tools/generate_tall_patio_door.py`. This one command preserves recessed
handles, the inventory pixel layout, and the four-door planks/glass recipe.
The optional `python preview/tall_patio_door/generate_preview.py` refreshes the
detailed oak preview. The renderer reads the locally cached Minecraft client jar.
