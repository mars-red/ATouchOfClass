# Furniture and storage

[Manual contents](README.md) · [Recipes](recipes.md) · [Catalog](catalog.md)

Furniture combines decorative models with seating, storage, or sleeping behavior. Most pieces face the player when placed. Use an empty hand to interact, and leave room for the whole piece rather than only its visible feet.

## Space and function

Dimensions below are the block spaces to reserve, in width × depth × height. A model may be smaller than its reserved space.

| Furniture | Space | Function |
| --- | --- | --- |
| Leather chair | 1 × 1 × 1 | One seat |
| Leather love seat | 2 × 1 × 1 | Two seating positions |
| Leather sofa | 3 × 1 × 1 | Three seating positions |
| Side table | 1 × 1 × 1 | Decorative surface; no inventory |
| Nightstand | 1 × 1 × 1 | 27 storage slots |
| Dresser | 2 × 1 × 2 | One shared 54-slot inventory; model is about 1¼ blocks high |
| Four-post bed | 2 × 3 × 2 | Two mattress-side sleeping positions; normal sleep/spawn restrictions |

## Chairs, love seats, and sofas

Place on a sturdy floor. The love seat places both sections together; the sofa places its middle section at the clicked position and extends to either side. Keep those neighboring spaces free. Use a seating section with an empty hand to sit there. An occupied section cannot take another passenger, and a player already riding something must dismount first. Use the normal dismount control to get up.

Breaking a section of a love seat or sofa removes the complete piece and clears its seating entities. These are leather furniture items, not dyeable upholstery families; their wool recipe ingredient does not determine a fabric color.

<!-- screenshot:furniture-01:start -->
![Leather chair, love seat, and sofa arranged side by side to compare their widths.](images/furniture/seating.png)

*Chair, love seat, and sofa provide one, two, and three seating positions.*
<!-- screenshot:furniture-01:end -->

## Tables, nightstands, and dressers

Side tables offer a supported top for a lamp but do not open a storage screen. Nightstands offer the same room-scale footprint with a three-row storage menu. Dressers need two blocks of width and two block spaces of height; their upper sections occupy only part of those spaces.

Use a nightstand or any dresser section to open storage. Click, drag, or shift-click items as in a normal chest interface. A dresser's sections share one inventory, not separate drawers. Storage fullness can be read by comparators. Breaking the storage furniture drops its contents; it is not a portable filled container like a key ring. Empty it first when moving a room's furnishings.

These three families each have 12 woods: oak, spruce, birch, jungle, acacia, dark oak, mangrove, cherry, pale oak, bamboo, crimson, and warped. Match a species across pieces to create a coordinated room.

<!-- screenshot:furniture-02:start -->
![Dresser interface showing six rows of shared storage.](images/furniture/dresser-storage.png)

*The complete dresser shares one 54-slot inventory.*
<!-- screenshot:furniture-02:end -->

## Four-post beds

Reserve a supported 2 × 3 floor area and two clear block spaces above that footprint. Stand at the foot and look toward the head when placing the bed. The whole structure is placed from one item, and removing a section removes the whole bed.

Use the bed to sleep. Interaction is routed to the middle mattress position on the clicked side. Each side has its own occupied state. Normal restrictions still apply: an occupied side cannot accept another sleeper, and sleep problems are reported by the game's usual messages. Leave clear space beside or at the ends of the bed for waking and respawning.

**Beds explode in dimensions where beds cannot set spawn**, as vanilla beds do. Use the Overworld for the first demonstration.

Four-post beds have 16 bedding colors and 11 woods: oak, spruce, birch, jungle, acacia, dark oak, mangrove, cherry, pale oak, crimson, and warped. **Bamboo four-post beds are not registered in the current source.** The [catalog](catalog.md) reflects registration rather than assuming every wood family is identical.

<!-- screenshot:furniture-03:start -->
![An oak four-post bed with red bedding and clear walkways around its sides.](images/furniture/four-post-bed.png)

*Reserve the full two-by-three footprint and two block spaces of height.*
<!-- screenshot:furniture-03:end -->

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Sofa, dresser, or bed will not place | Clear its complete footprint and upper spaces; check support below every lower section. |
| Clicking a side table does nothing | It has no storage; use a nightstand for an inventory. |
| Cannot sit | Use an empty hand, choose an unoccupied section, and dismount any current vehicle. |
| Storage items scatter when furniture is broken | Contents drop separately. Collect them or empty the furniture before moving it. |
| Bed refuses sleep | Read the game's message, check the selected side, and follow normal sleep rules. |
| Bed has no safe wake-up location | Clear nearby floor space beside the bed and at its ends. |
