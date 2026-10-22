# Content audit and verification record

[Manual](../README.md) · [Catalog](../catalog.md) · [Recipe reference](../recipes.md) · [Capture checklist](screenshots.md)

Initial pilot audit: October 7, 2026. Full-manual source review: October 8, 2026, using the current working tree including local implementation changes. This is not a runtime registry dump, an in-game test report, or release certification.

## Inventory coverage

The current generated reference covers **687 registered player-content items** and **666 bundled recipe files**, verified on October 22, 2026. It includes all 34 poplar items: door and drawer cabinets, side table, nightstand, dresser, dining table and chair, 16 four-post beds, shutter, window box, parquet, tall sidelight, tall door, tall patio door, transom, railing, baluster, balustrade, and newel post. The parser follows literal item registrations, basic block names, dresser/side-table/dining-table/dining-chair/nightstand/shutter/window-box/flower-bunch/patio-door definitions (also used for transoms), four-post-bed combinations, and wood railing families. It excludes the separately registered internal Creative tab icon and rejects recipes whose mod outputs are absent from those registration paths.

All listed items have a recipe or an oxidation route; no acquisition routes remain unresolved. All 32 colored concrete countertops/sinks now have recipes using vanilla concrete slabs. The 24 exposed/weathered/oxidized copper lighting and chain forms without direct recipes are reached through the oxidation data map. Four-post beds use 13 woods × 16 colors, including bamboo and poplar. Earlier dated implementation records below retain the counts verified at those times.

The earlier [acquisition inventory](acquisition-inventory.md) is retained as a historical pilot snapshot of labels and recipe outputs. Use the generated catalog for the current player-facing inventory. A new registration mechanism requires updating the parser; generated output does not replace a final in-game Creative-tab check.

## Chapter evidence

| Chapter | Source reviewed | Key distinctions documented |
| --- | --- | --- |
| Getting started | [Versions](../../gradle.properties), [Creative tab](../../src/main/java/com/digitaldownpour/atouchofclass/ModCreativeTabs.java), bundled recipes | Correct target, first-room ingredients, server/client installation |
| Building | [Overlay base](../../src/main/java/com/digitaldownpour/atouchofclass/items/BlockOverlayItemBase.java), [plaster](../../src/main/java/com/digitaldownpour/atouchofclass/items/BucketOfPlasterItem.java), [overlay removal](../../src/main/java/com/digitaldownpour/atouchofclass/WallOverlayBreakEvents.java), trim/rail block classes | Allowed faces, reusable finishes, 32-use plaster, replacement/removal, edge-click trim, supported rails |
| Tall doors | [Door block](../../src/main/java/com/digitaldownpour/atouchofclass/blocks/TallDoorBlock.java), [door item](../../src/main/java/com/digitaldownpour/atouchofclass/items/TallDoorItem.java), [door data](../../src/main/java/com/digitaldownpour/atouchofclass/entities/TallDoorBlockEntity.java), all 24 regular tall-door and patio-door loot tables | Three sections, paired-door access, code/name preserved on break |
| Keys | [Cutter menu](../../src/main/java/com/digitaldownpour/atouchofclass/KeyCutterMenu.java), [cutter screen](../../src/main/java/com/digitaldownpour/atouchofclass/gui/KeyCutterMenuScreen.java), [ring item](../../src/main/java/com/digitaldownpour/atouchofclass/items/KeyRingItem.java), [ring menu](../../src/main/java/com/digitaldownpour/atouchofclass/KeyRingMenu.java) | Exact codes, 10-character field, 16 coded-key slots, persistent ring inventory |
| Furniture | Chair/love-seat/sofa blocks, [seat entity](../../src/main/java/com/digitaldownpour/atouchofclass/entities/SeatEntity.java), dresser/nightstand inventories, [bed block](../../src/main/java/com/digitaldownpour/atouchofclass/blocks/FourPostBedBlock.java), variant definitions | Reserved volumes, seats, storage drops, two mattress sides, bed explosion behavior |
| Kitchens/bathrooms | [Oven](../../src/main/java/com/digitaldownpour/atouchofclass/entities/OvenBlockEntity.java), [cooktop](../../src/main/java/com/digitaldownpour/atouchofclass/blocks/InductionCooktopBlock.java), cabinet/refrigerator/tub/toilet/shower classes | Smelting versus decorative cooktop, independent cabinets, six-section refrigerator, tub fill/lid toggles |
| Lighting | Light registrations, fixture support checks, weathering classes, [oxidation map](../../src/main/resources/data/neoforge/data_maps/block/oxidizables.json), [waxing map](../../src/main/resources/data/neoforge/data_maps/block/waxables.json) | Permanent illumination, light level, support, aging routes |
| Elevators | [Discovery](../../src/main/java/com/digitaldownpour/atouchofclass/elevator/ElevatorUtil.java), [flow](../../src/main/java/com/digitaldownpour/atouchofclass/elevator/ElevatorFlow.java), [server events](../../src/main/java/com/digitaldownpour/atouchofclass/elevator/ElevatorServerEvents.java), [locks](../../src/main/java/com/digitaldownpour/atouchofclass/elevator/ElevatorLockManager.java), control/door blocks and screen | One shaft contact, 256 limits, rectangular volumes, transient reservations, one-based displayed floor numbers |
| Server/modpack guides | Entry point, component registration, saved overlay data, recipe/tag/assets paths, official versioned guidance | No custom configs/commands, complete world backups, data/resource pack formats and examples |

Source entry points: [ModBlocks](../../src/main/java/com/digitaldownpour/atouchofclass/ModBlocks.java), [ModItems](../../src/main/java/com/digitaldownpour/atouchofclass/ModItems.java), [block classes](../../src/main/java/com/digitaldownpour/atouchofclass/blocks), [entity classes](../../src/main/java/com/digitaldownpour/atouchofclass/entities).

## Corrections preserved from pilot review

- Always specify tall doors when describing code-based access; vanilla doors are not supported.
- Opening the key cutter does not require holding a tall-door item.
- Placement instructions do not require a trial placement before coding.
- Breaking tall doors preserves code and descriptive name in the updated implementation. All 24 regular tall-door and patio-door loot tables copy the keyable component, and the block entity synchronizes it across the sections.
- The supplied ring screenshot uses its default title; renaming is optional in its capture task.
- Use Minecraft F2 captures to omit window chrome. Preserve the five supplied originals.

## Patio door update — October 14, 2026

The door chapter, material/dimension reference, catalog, and recipe reference now include all twelve patio-door woods. The generated assets retain recessed handles, centered square item textures, and three rows of `WGW` using matching planks and glass blocks to yield four doors. The doc-reference helper follows `PatioDoorVariants` so regeneration retains all entries.

All 66 automated Minecraft game tests passed during implementation. Patio tests iterate all twelve woods and cover item placement, breaking each section in Survival and Creative, one-item drops and preserved key data, crafting, recipe advancement unlocks, key-cutter processing, matching/nonmatching keys, and matching key rings. Asset checks verified all 72 models, 576 block-state combinations, centered icons, texture references, and byte-identical regeneration. This is automated verification, not a new author-confirmed visual gameplay review. The patio illustration is explicitly labeled as a block-model render; the existing 22 in-game screenshot records are unchanged.

## Dining table update — October 16, 2026

Added the [dining table guide](../furniture.md#dining-tables), material/dimension entries, all twelve catalog entries, and their recipe references. Source evidence: [connection and corner logic](../../src/main/java/com/digitaldownpour/atouchofclass/blocks/DiningTableBlock.java), [wood definitions](../../src/main/java/com/digitaldownpour/atouchofclass/DiningTableVariants.java), [asset generator](../../tools/generate_dining_table.py), and [oak recipe](../../src/main/resources/data/atouchofclass/recipe/oak_dining_table.json). Recipes follow `PWP` / `P.P` and yield four tables, substituting matching planks and stripped wood, stripped bamboo blocks, or stripped hyphae. Each section remains a separate, floor-supported block with its own item drop.

The implementation's 74 required Minecraft game tests passed, including all twelve rectangular tables, every pair of wood varieties, inside-corner removal and refill, mixed-wood diagonal replacement, collision/support, and item registration. The oak appearance was approved by the author before the remaining variants were implemented. This does not record a new in-game acceptance review of all woods or shapes; game tests were not rerun for these documentation-only edits.

Added exactly one capture record, `furniture-04`, for a single image showing a rectangle and an L-shaped table with its inward corner leg visible. Its assigned file is `docs/images/furniture/dining-table-configurations.png`. The image was supplied, visually reviewed, and integrated on October 16, 2026. Its larger L-shaped arrangement replaces the original three-block capture example; the manifest and alt text now match the supplied image. The screenshot manifest generates both the chapter illustration and capture checklist. The reference helper now follows `DiningTableVariants`, retaining these entries on regeneration.

## Oak dining chair update — October 16, 2026

Implemented the author's approved [oak model](../../preview/dining_chair/oak_dining_chair.json) using raw stripped oak side grain on every face. The [generator](../../tools/generate_dining_chair.py) copies that geometry into the shipped block model and generates matching rotated collision shapes. The [chair block](../../src/main/java/com/digitaldownpour/atouchofclass/blocks/DiningChairBlock.java) supports one rider, turns the player's head, body, and client view toward the chair's front, checks support and upper-back clearance, and reuses the existing mount cleanup. The hand-edited [recipe](../../src/main/resources/data/atouchofclass/recipe/oak_dining_chair.json) uses five sticks and three oak slabs (`TTT` / `LLL` / `T.T`) to yield two chairs; it requires no stripped wood.

All 78 required Minecraft game tests passed during implementation. New chair tests check seating and occupancy in every facing, head/body orientation, seat height, dismount and break cleanup, actual item placement, tall-back collision and obstruction, support-loss drops, and the loaded recipe and advancement. The manual, catalog, recipe reference, item identifier, and inventory totals now include the oak dining chair. No other dining-chair wood variants are registered. These are automated checks; no new in-game author acceptance is claimed.

Dismount correction, October 16: the shared mount's default dismount point was inside the dining chair's collision. Dining chairs now choose a safe, supported neighboring exit, avoiding obstructing furniture and walls. If every exit is blocked, the fallback is slightly above the seat with a forward offset to clear the back posts. All 81 required tests passed after adding regression cases for every chair facing, a table and wall blocking exits, and completely surrounded chairs. The updated mod jar built successfully.

## Dining chair wood variants — October 16, 2026

Expanded the initial oak chair to all twelve woods via [DiningChairVariants](../../src/main/java/com/digitaldownpour/atouchofclass/DiningChairVariants.java). The other models inherit the approved oak geometry and override only their vanilla stripped side-grain texture; all variants share facing, occupancy, clearance, cleanup, and safe dismount behavior. Each recipe copies the hand-edited oak recipe, replacing only matching slab and output IDs; sticks and the two-chair yield stay the same. Bamboo, crimson, and warped use their own slabs. Source material properties preserve nether woods' resistance to ignition by lava.

All 83 required game tests passed and the mod jar built successfully. New tests cover all twelve woods in all four facings for mounting, head/body orientation, occupancy, clear-floor exits, the surrounded-chair fallback, and cleanup. Loaded recipe, item registration, source material properties, mixed-slab rejection, advancement presence, and matching one-item drops are checked for every wood. The catalog and recipe generator now recognizes the dining-chair family, and all item IDs, recipe links, and current totals are regenerated. The [finish gallery](../../preview/dining_chair/all_wood_dining_chairs_preview.png) is a model render, not a new in-game capture. The earlier oak-only registration statement describes the initial implementation, before this expansion.

## Checks and completed review

Leather seating correction, October 16, 2026: the chair, both love-seat cushions, and all three sofa cushions now share the dining chairs' successful-mount head/body/client-camera alignment. Dismounts search for supported, collision-free floor around the occupied cushion. Fully blocked exits fall back above the section's complete collision shape, clearing its arms and backrest. All 89 required game tests passed and the mod jar built successfully. New server-player regression cases cover every cushion in all four facings, clear and blocked-front exits, surrounded furniture, occupied-seat rejection, independent riders, reuse, and complete-piece break cleanup. The existing dining-chair and four-post-bed tests also pass; no additional in-game author acceptance is claimed.

Dining furniture screenshots, October 16, 2026: integrated `furniture-04` and `furniture-05` from their assigned paths. Both are game-only 854 × 480 PNGs, with no HUD or debug overlay. The table image shows a rectangular oak table alongside a larger L-shaped oak table, including the inward corner support. The chair image shows complete oak, mangrove, and warped chairs left to right with their finishes and slatted backs visible. Both images were visually reviewed and retained unchanged. All 24 screenshot records now have supplied files; none are awaiting capture. This visual review does not add a gameplay acceptance claim.

The author confirmed completion of all in-game checks on October 10, 2026. The statuses below record that confirmation; exact test build identifiers were not supplied.

Plaster removal update, October 10, 2026: the building and modpack guides now reflect that applied plaster drops nothing, both when stripped from a face and when its supporting block is broken. Confirmed against the shared drop handler in `WallOverlayBreakEvents`; no in-game test was performed for this documentation update.

The documentation helpers check reference generation, screenshot marker/manifest agreement, and whether generated content is current. Local links and recipe anchors are checked separately. No Java implementation changed in this documentation task, and game tests were not rerun solely for prose changes.

| Review | Status |
| --- | --- |
| Five tall-door images | Supplied, integrated, and visually reviewed |
| Full written manual | Author review of all chapters and sections completed October 10, 2026, with only minor changes |
| Seventeen additional capture tasks | All seventeen supplied and integrated October 9, including the separately supplied Creative inventory tab (`start-01`) |
| First room / Survival acquisition | Gameplay review completed; author-confirmed October 10 |
| Trim corners and stair rails | In-game placement checks completed; author-confirmed October 10 |
| Square and round plaster columns | In-game review completed; author-confirmed October 13 |
| Seating, beds, storage and appliances | Gameplay/multiplayer checks completed; author-confirmed October 10 |
| Waxing, scraping and multi-section lamps | In-game checks completed; author-confirmed October 10 |
| Measured two-stop elevator | Round-trip, occupancy, and multiplayer checks completed; author-confirmed October 10 |
| Dedicated-server persistence | Backup and smoke-test checks completed; author-confirmed October 10 |
| Recipe override example | `/reload` and crafting checks completed; author-confirmed October 10 |

The chapter-level review notes record the author's in-game verification separately from source and resource checks.

Earlier documentation checks: 1,585 local links, including 477 recipe anchors, validated across 19 Markdown files. Both generators reported current output; all 22 screenshot records matched chapter markers. The three JSON examples parsed, recipe mod-item ingredients resolved to the catalog, and localized player-content IDs matched catalog coverage. Runtime acceptance is complete per the author's confirmation above.

Plaster column update, October 13, 2026: added the building instructions and dimension reference for both vertical variants, regenerated the catalog and recipe reference from the six completed recipes, and updated inventory totals. Source review confirmed matching shaft stubs, inverted base/capital geometry, separate item drops, any-plank ingredients, and one-item recipe outputs. The author confirmed the columns in game before this manual update.

Documentation checks for this update: both generators report current output, all 22 screenshot records validate, and 1,609 local links including 490 heading anchors resolve across 17 Markdown files. The catalog reports no unresolved acquisition routes.

Elevator layout revision, October 9, 2026: follows the author's reference images with the shaft centered at the rear, call button facing the approach, and destination control facing into the cabin on the other side of the doorway. Construction captures now form a front/rear three-quarter pair through the same open east side. The floor-selection capture retains its stable ID elevator-02; the new rear view is elevator-03. All three captures were supplied and integrated in the October 9 import. The complete import contains 21 images, including replacements for the five tall-door pilot images; start-01 was subsequently supplied separately, visually checked, and integrated, completing all 22 captures. Source and destination file hashes matched for all 21 copies.

## Poplar manual refresh — October 19, 2026

Regenerated the catalog and recipe reference from the current registrations and recipe JSON. Updated the building, doors, furniture, kitchen, and material-reference chapters to include poplar in the common 13-wood set. The existing acquisition inventory remains a historical pilot snapshot. All 104 required game tests and the build passed after implementation of the poplar railing family; these documentation edits were checked for generated-reference consistency, poplar coverage, and local recipe links. No new in-game appearance review is claimed.

## Cinnabar work surfaces — October 20, 2026

Generated Cinnabar Countertop and Cinnabar Sink with the existing countertop/sink variant generator, using vanilla cinnabar slabs and the vanilla cinnabar block texture. Regenerated the catalog and recipe reference, updated the manual totals, and documented acquisition and placement in the kitchen chapter. The build and all 104 required game tests passed; generated-reference consistency and JSON parsing checks passed. No new in-game appearance review is claimed.

## Exterior window ledges — October 20, 2026

Added regular brick and buff brick window ledges with the approved 16 × 4 × 4 top-aligned geometry. Placement follows any clicked vertical face without a wall-material restriction. Recipes consume two matching Bricks or Buff Bricks blocks in one BB row and yield 16 ledges. Updated the building chapter, catalog, recipe reference, and totals (636 items and 615 recipes). The build and all 106 required game tests passed, including new placement, selection/collision, recipe, unlock, and loot checks for both variants. Visual review used the model preview; no in-game appearance review is claimed.

## Transom manual refresh — October 22, 2026

Added transoms to the doors chapter and material/dimension reference, including the supplied pale oak entryway screenshot. Documented unrestricted placement, automatic alignment over tall doors and sidelights, and merging only across each tall-door pair. The single-row `PGP` recipe uses matching planks and a glass block and yields four. Extended the reference generator to include all 13 transom registrations and regenerated the catalog and recipe reference: 687 player-content items, 666 recipes, and no unresolved acquisition routes. The implementation build and four focused transom game tests passed; the supplied screenshot shows the single/double/single arrangement in game.
