# Content audit and verification record

[Manual](../README.md) · [Catalog](../catalog.md) · [Recipe reference](../recipes.md) · [Capture checklist](screenshots.md)

Initial pilot audit: October 7, 2026. Full-manual source review: October 8, 2026, using the current working tree including local implementation changes. This is not a runtime registry dump, an in-game test report, or release certification.

## Inventory coverage

The current generated reference covers **560 registered player-content items** and **508 bundled recipe files**, including the six plaster column pieces added on October 13, 2026, and the twelve shutter and twelve patio-door variants included in the October 14 reference refresh. The parser follows literal item registrations, basic block names, dresser/side-table/nightstand/shutter/patio-door definitions, four-post-bed combinations, and wood railing families. It excludes the separately registered internal Creative tab icon. It rejects recipes whose mod outputs are absent from those registration paths.

All listed items have a recipe, an oxidation route, or the author's confirmed concrete exception. All 32 colored concrete countertops/sinks intentionally lack recipes. The 24 exposed/weathered/oxidized copper lighting and chain forms without direct recipes are reached through the oxidation data map. Four-post beds use 11 woods × 16 colors; bamboo is not in their current registered family.

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

## Checks and completed review

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
