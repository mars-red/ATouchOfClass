# Tall oak patio door

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

Oak is registered with the existing tall door placement, pairing, and locking
behavior. Other vanilla wood variants can reuse this geometry.
The key cutter accepts oak patio door stacks and assigns their key code and
name. Coded doors require a matching individual key or a matching key on a
key ring; breaking and replacing them preserves the assigned key information.

Crafting uses three rows of `WGW`, with oak planks for `W` and glass blocks for
`G`, and yields four doors. Acquiring oak planks unlocks the recipe. Breaking
any door section removes the complete leaf and drops one door item with its
key information preserved.

Regenerate assets with `python tools/generate_tall_patio_door.py`, followed by
`python preview/tall_patio_door/generate_preview.py` for the preview and item icon.
The renderer reads the locally cached Minecraft client jar.
