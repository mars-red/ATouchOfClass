# Kitchens and bathrooms

[Manual contents](README.md) · [Recipes](recipes.md) · [Catalog](catalog.md)

Build the supporting room first, then add storage and fittings. The table below distinguishes implemented interactions from appearance; no pipes or electrical power network are required by these furnishings.

| Fitting | Implemented behavior |
| --- | --- |
| Door/drawer cabinet | 27-slot storage per block |
| Refrigerator | 54-slot storage shared across the appliance |
| Oven | Furnace-style smelting with input, fuel, and output |
| Induction cooktop | Attaches to cabinets; decorative surface; adjacent matching cooktops can pair visually |
| Countertop and sink | Decorative surfaces; sinks have no fluid inventory or faucet interaction |
| Farmhouse kitchen sink | Two side-by-side sinks with the same facing share a center sprayer faucet |
| Clawfoot bathtub | Use to toggle its filled appearance |
| Toilet | Use to sit with the lid open or closed; Shift + use with an empty hand toggles the lid |
| Shower heads | Decorative fittings; no flowing-water interaction |
| Glass shower surround | Fixed panels with connections |
| Glass shower door | Use to open or close it |

## Appliance finishes

The **farmhouse kitchen sink, oven, induction cooktop, and refrigerator** each come in **stainless steel and copper**. All four copper appliances are available in unoxidized, exposed, weathered, and oxidized stages, with a waxed version of each stage.

| Appliance | Stainless steel recipe | Copper recipe |
| --- | --- | --- |
| Farmhouse kitchen sink | [Stainless steel](recipes.md#kitchen-sink) | [Copper](recipes.md#copper-kitchen-sink) |
| Oven | [Stainless steel](recipes.md#oven) | [Copper](recipes.md#copper-oven) |
| Induction cooktop | [Stainless steel](recipes.md#induction-cooktop) | [Copper](recipes.md#copper-induction-cooktop) |
| Refrigerator | [Stainless steel](recipes.md#refrigerator) | [Copper](recipes.md#copper-refrigerator) |

## Cabinets and work surfaces

Cabinets come in door and drawer styles across the 13 woods in the [reference](reference.md). Use one with an empty hand to open its inventory. Each cabinet keeps its own 27 slots; stacking cabinets does not merge inventories. Pairs of door cabinets of the same wood and facing connect their appearance vertically.

Use any countertop, sink, or induction cooktop item on a door or drawer cabinet to attach it, consuming one item in Survival. Both finishes of the farmhouse kitchen sink and induction cooktop can attach to cabinets. You do not need to sneak to attach a fitting. Each cabinet holds one attachment; trying to attach another fitting, or place a standalone fitting in the block space immediately above an occupied cabinet, is rejected.

Attached fittings belong to the cabinet and leave the block position above it free. Their models extend into that space: countertops remain 1/8 block thick, while sinks include their basin and faucet. Ordinary blocks and decorations can be placed above them without restrictions, even if their models overlap. Sneak-place ordinary blocks against the cabinet to avoid opening its storage. Attachments are decorative and do not add a separate collision surface or faucet target; interact with the cabinet to open its storage. Sinks do not supply water buckets.

**Sneak-use the cabinet with an empty hand to remove only its attachment.** In Survival, the fitting returns to your inventory, or drops beside the cabinet if your inventory is full. The cabinet and its contents stay in place. Normally breaking a cabinet drops both its cabinet item and its attached fitting, along with its stored contents, under the usual Survival drop rules. Creative removal does not return an attachment item.

Attached induction cooktops pair side by side when they have the same facing. Pairs stop at two; removing either attachment or breaking its cabinet restores the remaining cooktop's single appearance. Cooktops share the same one-attachment limit as sinks and countertops, so a cooktop cannot be stacked over an attached countertop or sink. Attached cooktops pair with other attached cooktops; standalone cooktops pair separately. They remain decorative and do not cook food.

Copper attachments continue to oxidize. Use honeycomb on the cabinet to wax its fitting, or an axe to remove wax or scrape oxidation. Facing and the current copper stage are saved with the cabinet.

Countertops, sinks, and induction cooktops placed elsewhere still work as standalone blocks. Existing builds keep their standalone fittings and original block occupancy when upgrading; there is no automatic conversion. To free the space above an existing cabinet, break its old standalone fitting and use that item on the cabinet to attach it.

The **farmhouse kitchen sink** has a front apron in both finishes. Place a second sink beside it with the same facing to form a double sink with one shared sprayer faucet. Pairs stop at two sinks; adding another sink leaves the existing pair intact. Removing either attachment or breaking its cabinet restores the remaining sink's single appearance. Standalone kitchen sinks also retain their existing pairing behavior; an attached sink pairs with another attached sink, not a standalone sink. The apron is decorative and does not block access to the cabinet below.

Like the other sinks, it has no fluid inventory or water interaction.

<!-- screenshot:kitchen-01:start -->
![Oak kitchen cabinets with calcite worktops and sink, an induction cooktop, and a stacked pantry.](images/kitchens/worktop.png)

*Cabinets store items; countertops, sinks, and cooktops complete the kitchen appearance.*
<!-- screenshot:kitchen-01:end -->

For countertops, place the slabs in one horizontal row:

```text
SSS
```

For sinks, use this two-row grid (`.` is an empty slot):

```text
S.S
.S.
```

`S` is the matching concrete slab, such as `minecraft:white_concrete_slab`. Obtaining one unlocks both recipes for that color. See the [white countertop recipe](recipes.md#white-concrete-countertop), [white sink recipe](recipes.md#white-concrete-sink), and [catalog](catalog.md) for every color. The resulting fittings remain decorative; sinks have no fluid inventory or water interaction.

## Wall tile overlays

Use wall tile overlays for kitchen backsplashes, shower surrounds, or other decorative walls. Like wallpaper, they cover the clicked face without occupying another block space.

| Wall tile | Allowed faces | Use behavior | Crafting recipe |
| --- | --- | --- | --- |
| Prismarine Fish Scale Wall Tiles | Four side faces | Reusable item | Five Prismarine blocks yield [40 wall tile items](recipes.md#prismarine-fish-scale-wall-tiles) |
| Calcite Subway Tiles | Four side faces | Reusable item | Two Calcite blocks yield [16 wall tile items](recipes.md#calcite-subway-tiles) |
| Calcite Wall Tiles | Four side faces | Reusable item | Four Calcite blocks yield [32 wall tile items](recipes.md#calcite-wall-tiles) |
| Deepslate Wall Tiles | Four side faces | Reusable item | Two Deepslate blocks yield [16 wall tile items](recipes.md#deepslate-wall-tiles) |
| Resin Wall Tiles | Four side faces | Reusable item | Three Blocks of Resin yield [24 wall tile items](recipes.md#resin-wall-tiles) |
| Yellow Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#yellow-glazed-terracotta-wall-tiles) |
| White Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#white-glazed-terracotta-wall-tiles) |
| Red Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#red-glazed-terracotta-wall-tiles) |
| Purple Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#purple-glazed-terracotta-wall-tiles) |
| Pink Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#pink-glazed-terracotta-wall-tiles) |
| Orange Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#orange-glazed-terracotta-wall-tiles) |
| Magenta Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#magenta-glazed-terracotta-wall-tiles) |
| Lime Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#lime-glazed-terracotta-wall-tiles) |
| Light Gray Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#light-gray-glazed-terracotta-wall-tiles) |
| Light Blue Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#light-blue-glazed-terracotta-wall-tiles) |
| Green Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#green-glazed-terracotta-wall-tiles) |
| Gray Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#gray-glazed-terracotta-wall-tiles) |
| Cyan Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#cyan-glazed-terracotta-wall-tiles) |
| Brown Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#brown-glazed-terracotta-wall-tiles) |
| Blue Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#blue-glazed-terracotta-wall-tiles) |
| Black Glazed Terracotta Wall Tiles | Four side faces | Reusable item | Two matching glazed terracotta blocks yield [16 wall tile items](recipes.md#black-glazed-terracotta-wall-tiles) |

Apply tiles to any side face of a block; top and underside faces do not accept them. Each face holds one finish, so applying tiles replaces any existing finish on that face. Different faces of the same block can carry different finishes.

Start mining a tiled face to strip its overlay first. Stop mining after stripping it if you want to keep the supporting block intact. Removal returns the wall tile item in Survival; Creative removal drops nothing. Remove tiles before applying another finish if you want their item back. See [finish removal and replacement](building.md#remove-or-replace-a-finish) for the shared overlay behavior.

Craft Prismarine Fish Scale Wall Tiles using this pattern (`.` is an empty slot):

```text
.P.
P.P
P.P
```

`P` is a **Prismarine block**. Five blocks yield **40 wall tile items**, or eight items per block. Obtaining prismarine unlocks the recipe.

Craft **Calcite Subway Tiles** from two **Calcite blocks** placed diagonally in a 2×2 grid, using rows `C ` and ` C`. The recipe yields **16 wall tile items**, or eight items per block. Obtaining calcite unlocks the recipe.

Craft **Deepslate Wall Tiles** with the same diagonal pattern as Calcite Subway Tiles, using **Deepslate blocks** instead of calcite. Two blocks yield **16 wall tile items**. Use ordinary deepslate, not cobbled deepslate; obtaining deepslate unlocks the recipe.

Craft **Resin Wall Tiles** from three **Blocks of Resin** using rows `R R` and ` R `. The recipe yields **24 wall tile items**, or eight items per block. Obtaining a Block of Resin unlocks the recipe.

Craft **Glazed Terracotta Wall Tiles** in all 16 colors using two matching glazed terracotta blocks diagonally, with rows `G ` and ` G`. Each recipe yields **16 wall tile items**. Use glazed terracotta of the matching color; ordinary terracotta and mixed colors do not substitute. Obtaining the matching glazed terracotta unlocks its recipe.

Craft **Calcite Wall Tiles** from four **Calcite blocks** in a 2×2 square, using rows `CC` and `CC`. The recipe yields **32 wall tile items**, or eight items per block. Obtaining calcite unlocks the recipe.

## Wall tile trim strips

**Wall Tile Trim Strip** is a straight stainless steel strip for finishing tile edges, such as the top of a backsplash. It is **Survival obtainable** and can be placed against blocks whether or not they have a tile overlay.

Click near the **top, bottom, left, or right edge** of a block's side face to place the strip along that edge. The nearest edge to the click determines placement. Top and underside faces do not accept strips. Each strip spans the full block edge, projects **1/2 model unit (1/32 block)** from the wall, and is **1 model unit (1/16 block)** wide across the face. Strips remain straight and do not form corner joins. They occupy the adjacent block space; mine the visible strip to remove it.

Craft **64 strips** from **three Brushed Stainless Steel blocks** in one horizontal row:

```text
SSS
```

`S` is a **Brushed Stainless Steel block** (`atouchofclass:brushed_stainless_steel`). Obtaining one unlocks the recipe. See the [trim strip recipe](recipes.md#wall-tile-trim-strip) and [catalog entry](catalog.md).

## Refrigerator

Reserve a **2-wide × 1-deep × 3-high** space on a sturdy floor. The six sections form one refrigerator; you do not place each section separately. Use any section to open the six-row, 54-slot inventory.

This is storage, not a food-preservation simulation: it does not require power, consume coolant, or change item spoilage. Its inventory can hold ordinary items, not only food. Breaking a section removes the appliance and drops its stored contents. Empty it before relocating it.

<!-- screenshot:kitchen-02:start -->
![A full-height refrigerator beside a kitchen cabinet row.](images/kitchens/refrigerator.png)

*The refrigerator reserves a two-by-one-by-three volume and shares 54 storage slots.*
<!-- screenshot:kitchen-02:end -->

## Oven and induction cooktop

The oven occupies one block and uses the familiar furnace screen. Put a smeltable input in the upper slot and furnace fuel in the lower slot; collect the output on the right. It uses **smelting recipes**, so it is not restricted to food. The oven lights while burning and emits light level 13 in that state. It uses fuel rather than an electrical connection.

For a straightforward demonstration, use raw iron and coal and wait for an iron ingot. The exact available recipes and fuel behavior can be changed by a modpack.

The induction cooktop is a decorative, countertop-height piece. Place it in the block space above an oven or cabinet. Two cooktops next to one another, with the same facing, form a visual pair; adding a third does not create an unlimited merged surface. There is no cooktop inventory, cooking progress, or power input.

<!-- screenshot:kitchen-03:start -->
![Oven interface smelting raw iron into iron ingots with coal fuel.](images/kitchens/oven.png)

*The oven uses furnace smelting recipes, not just food recipes.*
<!-- screenshot:kitchen-03:end -->

## Bath and toilet

The clay toilet and clay clawfoot bathtub are crafting intermediates; follow their firing recipes in the [recipe reference](recipes.md) to obtain the finished fittings.

The clawfoot bathtub's visible body is about **1½ × 2½ blocks**, but it occupies a **3 × 3 × 1** arrangement of block sections. Keep the full area clear and supported. Use any section to toggle the filled appearance across the whole tub; no water bucket is required. It does not behave as a fluid tank. Breaking one section removes the complete tub.

The toilet occupies one block and seats one player at a time. Right-click it to sit with the lid either open or closed. Sitting leaves the lid in its current state; press Shift to stand up. Breaking an occupied toilet also dismounts its rider.

To open or close the lid, hold Shift and right-click the toilet with an empty hand. This plays the lid interaction sound. The toilet does not offer storage or a waste-processing mechanic.

## Shower fittings

Wall shower heads attach to a supporting wall; rain shower heads attach underneath a supported ceiling. They are decorative rather than water emitters.

Glass shower surrounds and shower doors are three blocks high. Place on a supported base with all three spaces clear. Surrounds connect to neighbors of the same facing. Shower doors swing when used; their hinge depends on the placement click. These doors are separate from the coded tall-door system and cannot be coded with the key cutter.

<!-- screenshot:kitchen-04:start -->
![Bathroom fittings with a filled clawfoot tub, open toilet lid, and glass shower enclosure.](images/kitchens/bathroom.png)

*Use the bath to toggle its filled appearance. The toilet offers seating and a lid toggle; shower heads are decorative fittings.*
<!-- screenshot:kitchen-04:end -->

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Sink, shower, or cooktop has no interface | These are decorative; the oven and storage units have interfaces. |
| Tall pantry looks connected but storage is separate | Each cabinet retains its own inventory. |
| Fridge or bathtub will not fit | Check reserved block spaces, not just the visible model dimensions. |
| Oven does not cook a particular item | It accepts furnace smelting recipes and needs valid fuel plus room for output. |
| Cooktops do not join | Check that both are unpaired neighbors with the same facing. |
| Concrete fitting will not craft | Use three matching-color concrete slabs, not full concrete blocks or mixed colors; check the recipe grid and pack overrides. |
| Shower door does not respond to a coded key | Use it normally; coded access belongs to tall doors. |
