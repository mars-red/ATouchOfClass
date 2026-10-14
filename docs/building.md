# Building and architectural details

[Manual contents](README.md) · [Recipes](recipes.md) · [Catalog](catalog.md)

Use structural blocks for floors and walls, face-applied finishes for their appearance, and separate trim blocks for depth. Finishes do not replace the underlying block; trim occupies an adjacent block space even when its model is thin.

## Materials and flooring

The building palette includes buff bricks and their slabs, stairs, and walls; calcite slabs, calcite tile, marble tile, and octagonal inset tile; parquet wood flooring; and brushed stainless steel. See the catalog for exact variants and the recipe reference for crafting, smelting, and stonecutting routes. Parquet is a building block, while wallpaper and siding are applied finishes.

## Apply plaster and wall finishes

Use a finish item on the face you want to cover. Each face stores one finish: applying another replaces its appearance rather than adding another layer. Different faces of the same block can carry different finishes.

| Finish | Allowed faces | Use behavior |
| --- | --- | --- |
| Bucket of plaster | Any face, including top and underside | 32 applications in Survival; returns an empty bucket after the last use |
| Wallpaper: cornflower, scalloped, ogee lattice, striped, gothic | Four side faces | Reusable item  |
| Wainscoting | Four side faces | Reusable item |
| Shake siding | Four side faces | Reusable item |
| White stucco | All six faces | Reusable item |
| Red brick and buff brick finishes | Side faces and top | Reusable item; cannot apply underneath |

Make quicklime powder by smelting calcite. Combine quicklime powder, a water bucket, and sand in a shapeless recipe to make a bucket of plaster. The bucket also participates in crafting recipes; each crafting remainder uses one application and eventually returns an empty bucket. Plaster cannot be enchanted. Reapplying plaster to a face already carrying that plaster does not consume another application.

To decorate a room, apply wainscoting to the lower row of wall faces and wallpaper above it. Plaster is not a prerequisite for wallpaper. For predictable results, start with ordinary full blocks and a clear view of the target face.

<!-- screenshot:building-01:start -->
![Stone wall with a bare column beside wainscoting below cornflower wallpaper.](images/building/wall-finishes.png)

*Different rows can carry different finishes without replacing the wall blocks.*
<!-- screenshot:building-01:end -->

### Remove or replace a finish

Start mining the decorated face to remove its overlay first. That initial action is intercepted so the supporting block is not broken by the same start action. Non-plaster overlays return their associated items in Survival; Creative removal drops nothing. Plaster overlays drop nothing, whether stripped from a face or removed by breaking the supporting block. Stop mining after stripping the face if you want to keep the wall intact. Breaking a block through an undecorated face also clears its remaining finishes.

Applying a replacement finish directly overwrites the old finish. Remove a non-plaster finish first if you want its item back. Applied plaster cannot be recovered.

## Wood shutters

Shutters are available in oak, spruce, birch, jungle, acacia, dark oak,
mangrove, cherry, pale oak, bamboo, crimson, and warped.
Place a shutter against a wall's side face. It sits flush against that
face; clicking the left or right half selects that hinge as viewed from
outside the wall. Each shutter is one block high. Stack matching shutters
vertically for continuous side rails and end caps only at the exposed ends.
Sneak-use the top or bottom of a shutter to extend its column, or place each
section against the wall. Top/bottom clicks on ordinary blocks are not accepted.

Use any shutter to open or close its entire uninterrupted vertical group.
The group must have the same wood variety, facing, and hinge side. A gap,
different hinge, or different facing ends the group. Breaking one shutter
leaves the others intact and restores their exposed end caps. Survival
breaking drops one shutter; Creative breaking drops none.

Craft eight shutters from four matching planks and two sticks, with
`WSW` in two crafting rows. Obtaining those planks unlocks that wood's recipe.
Every model face uses the matching stripped wood grain, including the narrow
edges (stripped bamboo block for bamboo, stripped hyphae for crimson and warped).

## Add trim

| Piece | Placement technique |
| --- | --- |
| Baseboard | Click a side face near the bottom of the wall; the piece is bottom-aligned. Adjacent pieces determine corner shapes. |
| Crown moulding | Click a side face at the ceiling line; the piece is top-aligned. Floor/ceiling-face clicks are not accepted. |
| Casing | Click near the desired edge of a face. The nearest horizontal or vertical edge determines orientation. |
| Plinth block | Uses casing's edge selection and adapts beside baseboard. Useful at the foot of an opening. |
| Corner block | Click the desired quadrant of the supporting face to choose its corner. |
| Window apron | Starts along the top edge of the clicked face. Clicking the end of an existing apron adds an end cap; a capped end cannot be extended again that way. |

Build the supporting wall and opening first. Place straight runs before checking their corners, then add the casing, plinths, and apron. If a joint looks wrong, check the clicked face and edge rather than adding more pieces over it. The automatic corner rules depend on the orientation and positions of neighboring trim.

<!-- screenshot:building-02:start -->
![A framed window with an apron, baseboard, and crown moulding continuing around a wall corner.](images/building/window-trim.png)

*Place the supporting wall first, then align trim to its faces and opening edges.*
<!-- screenshot:building-02:end -->

## Plaster columns

Plaster columns come in **square** and **round** variants. The round shaft uses a stepped, blocky circle; both variants have square stepped trim at their ends and the same white concrete appearance as plaster. Each variant has three separately crafted pieces:

| Piece | Role | Square recipe | Round recipe |
| --- | --- | --- | --- |
| Column | A full-block-height shaft section that stacks vertically | [Square column](recipes.md#square-plaster-column) | [Round column](recipes.md#round-plaster-column) |
| Column base | Stepped trim at the bottom, with a short shaft above it | [Square base](recipes.md#square-plaster-column-base) | [Round base](recipes.md#round-plaster-column-base) |
| Column capital | The base design inverted, with a short shaft below the trim | [Square capital](recipes.md#square-plaster-column-capital) | [Round capital](recipes.md#round-plaster-column-capital) |

To build a five-block-high column, place one base, stack three matching column sections above it, then place one matching capital on top. Add or remove shaft sections to change the height. The base and capital each occupy one full block height, including their shaft stubs, so the total height is the number of shaft sections plus two blocks. Keep the same variant throughout for a continuous shaft profile.

All pieces stay upright when placed. Horizontal columns are not supported. Each piece occupies its own block space and can be broken separately; in Survival, it drops its corresponding item. Axes mine these blocks efficiently.

Every recipe combines a bucket of plaster with **any wood planks**, including crimson and warped planks, and produces **one piece**. Both shaft recipes use four planks; square bases and capitals use two planks each, while round bases and capitals use three each. Each recipe uses one plaster application through the bucket's crafting remainder. Follow the linked grids to distinguish the shaft, base, and capital recipes.

<!-- screenshot:building-04:start -->
![Stone wall with a round plaster column to the left and a square plaster column to the right.](images/building/plaster-columns.png)

*Round plaster column on the left, square plaster column on the right.*
<!-- screenshot:building-04:end -->

## Railings, balusters, balustrades, and newel posts

All four families have the 12 wood variants listed in the [reference](reference.md). Their roles differ:

- **Railing:** a wall-mounted handrail. It needs a wall behind it or a supported outside-corner continuation. Extend a level run by clicking its end. Nearby stair geometry and neighboring rails determine slopes and transitions.
- **Baluster:** a freestanding post supported from below. Place on a suitable floor surface.
- **Balustrade:** a freestanding rail-and-post section. Start by clicking the top of its support. It can take sloped forms above bottom-half stairs and connect to matching sections.
- **Newel post:** a two-block-high end post. Reserve both vertical spaces and a supported base.

For a first stair build, use a straight run of ordinary bottom-half stairs beside a solid wall. Add the wall handrail along that wall. On the open side, place matching balustrades on the stairs' upper treads; extend from the end of a sloped section where needed. Put newel posts on level landings. Complete the run before judging transition pieces, since neighboring geometry changes their shape.

<!-- screenshot:building-03:start -->
![A straight stair run with a wall handrail, open-side balustrade, and landing newel posts.](images/building/stair-rails.png)

*Wall railings and freestanding balustrades serve different support arrangements.*
<!-- screenshot:building-03:end -->

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Wallpaper does nothing on a floor | Wallpaper and wainscoting only accept side faces; use stucco or plaster for all-face coverage. |
| A finish seems to vanish | Only one finish is stored per face; a later application replaces it. |
| Mining removes decoration first | That is the overlay-removal action. Continue only if you also want to break the underlying block. |
| Thin trim will not share a space | Trim uses block spaces; it is not another face overlay. |
| Railing will not place | Check its supporting wall or valid corner continuation; use balustrades for freestanding edges. |
| Stair rail remains flat or disappears | Check bottom-half stairs, orientation, physical tread support, and adjacent matching sections. |
| Apron cannot be extended from its end | A cap is terminal. Rebuild the run with the desired length before finishing the end. |
