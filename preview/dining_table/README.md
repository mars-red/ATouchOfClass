# Dining tables

`oak_dining_table_2x3_preview.png` is an orthographic software render of the
shipped multipart models and texture, not an in-game screenshot. It shows six
blocks in a 2 × 3 rectangle, with exactly four corner legs, and a separate single
block. The single block has the same geometry as the side table.
`all_wood_dining_tables_preview.png` shows each of the twelve finishes as a joined
2 × 3 table, rendered from its actual model and texture.

All twelve vanilla woods are enabled: oak, spruce, birch, jungle, acacia, dark oak,
mangrove, cherry, pale oak, bamboo, crimson, and warped. Connections compare the
exact block type, so different wood varieties remain separate. Every block
needs a sturdy surface beneath it, matching the side table's placement rules.

## Connections

The top fills its clipped corners along connected edges. Aprons run only along
the perimeter, extending across block boundaries. A corner has a leg when:

- Neither of its two cardinal neighbors matches (an outside corner).
- Both neighbors match but the diagonal is missing (an inside corner).

Straight edges and filled interior corners have no legs. Holes therefore receive
inside-corner legs as well. Diagonally touching tables remain separate. Diagonal
leg changes after removal settle on the next game tick.

## Finish

Oak retains the approved base color `#B8945F` and `stripped_oak_log` grain. Each
other variant uses the average color of its vanilla stripped wood texture as
its solid base and that texture's grain blended at 35% opacity:
`base × 0.65 + grain × 0.35`. Bamboo uses stripped bamboo block grain; crimson
and warped use stripped stem grain. The result is an opaque PNG, so the requested
layered appearance needs no runtime transparency. Oak's base color and the
shared grain opacity are editable constants in `tools/generate_dining_table.py`.
Variant models inherit the approved oak geometry and override only the texture.
For the current target, read textures from a local Minecraft 1.21.9 client jar; use
`--client-jar` to specify another location. Assets are generated ahead of time;
players do not need Python or Pillow.

Regenerate assets and preview from the repository root:

```powershell
python tools/generate_dining_table.py
python preview/dining_table/generate_preview.py
```

The recipe is `PWP` / `P P`, where `P` is oak planks and `W` is stripped oak
wood; it makes four blocks. The oak recipe JSON is the hand-edited source of
truth and the generator never overwrites it. All other wood variants copy
its full pattern, ingredient structure, and output count, substituting only
material and output IDs (including bamboo blocks and nether hyphae).
The item is in the mod creative tab and
the vanilla Functional Blocks tab, and drops itself when mined.

Run the game tests in a disposable world and build:

```powershell
./gradlew.bat runGameTestServer build --offline -I tools/dining-table-check.init.gradle
```

Coverage includes the 2 × 3 rectangle, L-shape removal and refill, a ring with
four inward corners, isolated geometry parity, collision and decoration support,
support removal, other furniture isolation, rotation, and mirroring. Additional
checks cover all twelve 2 × 3 wood tables, every pair of wood varieties, mixed-wood
diagonal replacement, item-to-block registration, and material fire behavior.
