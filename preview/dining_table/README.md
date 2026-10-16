# Oak dining table prototype

`oak_dining_table_2x3_preview.png` is an orthographic software render of the
shipped multipart models and texture, not an in-game screenshot. It shows six
blocks in a 2 × 3 rectangle, with exactly four corner legs, and a separate single
block. The single block has the same geometry as the side table.

Only oak is registered while the design is refined. Connections compare the
exact block type, so future wood varieties will remain separate. Every block
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

The prototype uses base color `#B8945F` with vanilla `stripped_oak_log` grain
blended at 35% opacity: `base × 0.65 + grain × 0.35`. The result is an opaque PNG,
so the requested layered appearance needs no runtime transparency. Base color
and grain opacity are editable constants in `tools/generate_dining_table.py`.
The generator reads the texture from the local Minecraft 1.21.4 client jar; use
`--client-jar` to specify another location. Assets are generated ahead of time;
players do not need Python or Pillow.

Regenerate assets and preview from the repository root:

```powershell
python tools/generate_dining_table.py
python preview/dining_table/generate_preview.py
```

The recipe is `PWP` / `P P`, where `P` is oak planks and `W` is stripped oak
wood; it makes four blocks. The oak recipe JSON is the hand-edited source of
truth and the generator never overwrites it. Future enabled wood variants copy
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
support removal, other furniture isolation, rotation, and mirroring.
