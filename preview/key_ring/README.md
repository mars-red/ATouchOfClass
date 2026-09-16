# Key ring textures

Generated using the built-in image generation tool. The selected transparent source
PNGs are preserved here. Runtime textures are 64 x 64 PNGs, reduced with nearest
neighbor sampling to retain hard pixel edges and the generated alpha channel.

- `src/main/resources/assets/atouchofclass/textures/item/key_ring_empty.png`
- `src/main/resources/assets/atouchofclass/textures/item/key_ring_filled.png`

The item definition uses `minecraft:has_component` with `minecraft:container` and
`ignore_default: true`. KeyRingItem defines the default container as EMPTY;
KeyRingMenu saves edited contents using ItemContainerContents.fromItems, which
normalizes an all-empty inventory to EMPTY. Thus any stored key selects the filled
model, including a key in the last slot, and removing all keys selects the empty
model. The same condition applies in the inventory, in hand, and to dropped items.
The old `item/key_ring` model name remains as an alias for the empty model.

## Prompts

### Empty sprite

Use case: stylized-concept. Asset type: Minecraft inventory item texture PNG, empty key ring state. Generate ONE isolated small silver iron split key ring WITHOUT ANY KEYS, transparent background including the hole. Crisp chunky pixel art, Minecraft vanilla item style, limited palette of dark gray outlines, steel gray and near-white highlight pixels. Flat front view with slight diagonal tilt, visibly open hole, a small split in the metal band near upper right. Center the ring in the upper half of a square canvas, ring spanning roughly x=25% to 75%, y=10% to 60%; leave lower half empty transparent for keys to hang in a future matching variant. Coarse 32x32 pixel-grid appearance, hard pixel edges, no anti-aliasing, no blur, no shadow outside sprite, no letters, no labels, no checkerboard backdrop. Output genuinely transparent PNG.

### Filled sprite, referencing the empty sprite

Use case: precise-object-edit. Asset type: matching Minecraft inventory item texture, filled key ring state. Edit the provided empty silver ring sprite by adding three small golden brass door keys hanging from its lower arc and fanning slightly left, down and right. Preserve the existing ring's exact position, size, silhouette, silver palette, pixel style and square canvas. Keys have simple loop heads, thin shafts and square teeth, recognizable at inventory size, extending downward into the existing blank lower space to about 90% of canvas height. Keep the upper arc and hollow upper center of the ring clearly visible. Coarse Minecraft pixel art with hard pixel edges and limited colors, no antialiasing. Keep the background and holes genuinely transparent. No text, no labels, no outside shadows, no checkerboard pattern. Output transparent PNG.

### Filled sprite transparency correction

Use case: background-extraction. Edit target: the supplied silver key ring with three golden keys Minecraft sprite. Remove the entire painted white/gray checkerboard background, including inside the ring hole and key head holes. Replace all background with actual zero-alpha transparency in the PNG. Preserve the silver ring and three gold keys exactly, preserving their colors, position, size, shapes, pixel edges. No checkerboard pixels may remain. This is an isolated transparent game texture, not a preview of transparency. Keep square canvas. Output a PNG with a real alpha channel.
