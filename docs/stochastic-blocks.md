# Stochastic block appearance

`StochasticBlock` rolls texture variant and face-local UV rotation on server-side
placement. Normal block states save and synchronize the result. The geometry,
collision, and placement orientation do not rotate. The same quarter turn applies
to every face; patterns are not guaranteed to match across edges.

Register a block using `new StochasticBlock(properties, textureCount)` for all four
rotations, or pass allowed quarter turns, e.g. `(properties, textureCount, 0, 2)`
to preserve grain direction. Choices are uniform; repeated entries in the rotation
list weight those choices. The reusable base supports 1–16 texture variants and
reserves 64 states, so adding textures within that limit preserves property values.
This first asset generator supports full cubes; shaped models require equivalent
per-face UV variants of their own geometry.

Marble tile currently enables one texture and four rotations. When additional
textures arrive, put them in `src/main/resources/assets/atouchofclass/textures/block/`
and run, for example:

```powershell
python tools/generate_stochastic_models.py marble_tile marble_tile marble_tile_2 marble_tile_3
```

Change marble's `StochasticBlock` texture count in `ModBlocks.createBasicBlock` to
match the number of textures supplied. Keep texture order stable: it determines
the saved variant numbers. Reserved, inactive variants render the first texture.
The original marble model remains the inventory model.

Loot must drop the ordinary block item with no `copy_state` or component-copying
functions for these appearance properties. Marble already does this, including
when mined using Silk Touch. Breaking removes the placed state; placing its drop
rolls again. No item appearance data or block entity is used.
