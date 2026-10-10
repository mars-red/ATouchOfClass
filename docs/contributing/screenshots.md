# Manual screenshot checklist

[Capture standard and review workflow](README.md) · [Manual](../README.md)

Generated from screenshots.json. Tasks are grouped by shared setup. The five tall-door pilot images are already supplied. File presence does not imply visual approval. Use F2 for game-only PNG captures.

## Tall doors and keys (pilot images supplied)

### doors-01 — Single entrance with sidelights

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Single entrance with sidelights

**Setup:** Creative test world, daylight, default textures. On a solid floor, build a five-block-wide wall with a three-block-wide, three-block-high opening. Place an uncoded tall oak door in the center and a tall oak sidelight on each side, all facing the same way. Close the tall door.

**Capture:** Stand outside, centered on the entrance, far enough back to include the floor, all three vertical sections, and the surrounding frame. Hide the HUD for this world view.

**Must show:** One closed tall oak door and two fixed oak sidelights, their full height, and enough surrounding blocks to judge scale.

**Avoid:** Do not include the cutter, dropped items, mobs, chat, debug overlays, shaders, or a cropped top section.

**Save as:** `docs/images/doors/single-entrance.png`

**Alt text:** A closed tall oak door, three blocks high, between two matching sidelights in a framed opening.

**Caption:** A tall door and two sidelights fill a three-block-wide, three-block-high opening.

### doors-02 — Paired tall doors

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Paired tall doors

**Setup:** Beside the first scene, make a two-block-wide, three-block-high opening on a solid floor. Place two uncoded tall oak doors with the same facing and opposite hinges. Test that using either leaf moves both. Leave both open.

**Capture:** From outside at a slight angle, show the entire opening and both swung leaves. Hide the HUD. Adjust the camera so neither leaf is hidden behind the frame.

**Must show:** Two open tall doors of the same wood, opposite hinges, all three sections, and the clear passage between them.

**Avoid:** Do not use mixed woods or show one leaf closed. A static image illustrates the result; record separately whether both leaves moved together.

**Save as:** `docs/images/doors/double-doors.png`

**Alt text:** Two tall oak doors with opposite hinges stand open on either side of a shared passage.

**Caption:** Paired tall doors open together when their facing and hinges form a pair.

### doors-03 — Code the tall door

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Code the tall door

**Setup:** Place a key cutter and use it to open its interface. A fresh tall oak door should be in your inventory; you do not need to hold it. Put exactly one tall oak door in the left input slot; keep the right slot empty. Enter HOUSE01 in Key code and Front Door in Name. Do not press the set-code button yet.

**Capture:** Capture the full interface with the title, both text fields, both slots, center button, and player inventory visible. Keep the pointer on unused space so no tooltip hides the fields.

**Must show:** HOUSE01, Front Door, one tall oak door in the left slot, an empty right slot, and the enabled center button.

**Avoid:** Do not substitute an ordinary vanilla door, put a key in the input, or capture after processing. Keep the interface visible; do not hide it with F1.

**Save as:** `docs/images/doors/key-cutter-door.png`

**Alt text:** Key cutter with a tall oak door in its input, HOUSE01 as the code, and Front Door as the name.

**Caption:** Set the tall door's code before placing it in the world.

### doors-04 — Code the matching key

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Code the matching key

**Setup:** Finish the previous operation and take the coded tall door. Put one blank door key in the left slot. Enter HOUSE01 and Front Door again if needed. Press the center button, leaving the processed key in the right output slot.

**Capture:** Capture the same full interface and crop as doors-03. Keep the pointer away from controls and items so text remains unobscured.

**Must show:** HOUSE01 and Front Door still visible, the empty input slot, and the coded key in the right output slot.

**Avoid:** Do not remove the key before capturing or leave a tall door in either slot. If fields behave differently in game, report that before trying to match the requested image.

**Save as:** `docs/images/doors/key-cutter-key.png`

**Alt text:** Key cutter showing a processed door key in the output and the matching HOUSE01 code.

**Caption:** A key with the same code can operate the coded tall door; the descriptive name is only a label.

### doors-05 — Organize a key ring

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Organize a key ring

**Setup:** Take the HOUSE01 key. Make a second key with code SHED01 and name Shed. Hold a key ring and use it into the air, then put one coded key in each of its first two slots. Leave a blank key in the player inventory for comparison. Renaming the ring is optional and is not required for this capture.

**Capture:** Capture the full ring interface including its title, all 16 ring slots, player inventory, and hotbar. Hover the first coded key so its Front Door tooltip is readable without obscuring the title or second key.

**Must show:** Key Ring title (or the custom name if renamed), two separate occupied ring slots, remaining empty slots, and the Front Door tooltip. Position the tooltip so the occupied slots remain visible; it may overlap empty slots.

**Avoid:** Do not fill every slot, put the blank key on the ring, or crop out the player inventory. Do not add callouts; supply the clean original.

**Save as:** `docs/images/doors/key-ring.png`

**Alt text:** Key Ring interface with two coded keys in its sixteen-slot grid and the Front Door key tooltip visible.

**Caption:** A ring holds up to sixteen coded keys and automatically uses a matching one.

## First room and furniture

### start-01 — Find the mod in Creative

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Find the mod in Creative

**Setup:** Use a new Creative Overworld test world with this mod installed. Open Creative inventory and select A Touch of Class. Return its item list to the first page/scroll position.

**Capture:** Capture the complete inventory screen. Hover the mod tab only if its name is otherwise unclear; do not cover the item grid.

**Must show:** The selected mod tab and a representative grid of mod items.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/getting-started/creative-tab.png`

**Alt text:** The A Touch of Class Creative inventory tab showing the mod content.

**Caption:** The mod Creative tab is the starting point for exploring the complete palette.

### start-02 — First furnished room

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** First furnished room

**Setup:** Build the five-by-five room in Getting started, with four blocks of interior height. Place one oak side table, a copper table lamp on it, one leather chair facing into the room, and one oak nightstand. Leave floor space between them. Use daylight or enough lighting to see the shapes.

**Capture:** From a corner inside the room, show all four furnishings and some floor/walls. Hide the HUD; keep furniture uncropped.

**Must show:** Chair, table, supported lamp, and nightstand as distinct objects.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/getting-started/first-room.png`

**Alt text:** A small room furnished with a leather chair, oak side table and nightstand, and copper table lamp.

**Caption:** A first room introduces seating, storage, and lighting with craftable items.

### furniture-01 — Compare seating widths

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Compare seating widths

**Setup:** On a flat full-block floor beside the first room, place a leather chair, love seat, and sofa facing the same direction. Leave two empty columns between pieces. Use Creative to obtain the items.

**Capture:** Take a front three-quarter view with the complete row visible. Keep the floor grid visible enough to compare one-, two-, and three-block widths.

**Must show:** All three distinct seating models and their full widths; no player or mob obscuring seats.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/furniture/seating.png`

**Alt text:** Leather chair, love seat, and sofa arranged side by side to compare their widths.

**Caption:** Chair, love seat, and sofa provide one, two, and three seating positions.

### furniture-02 — Dresser storage

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Dresser storage

**Setup:** Place an oak dresser on a supported two-wide footprint with two spaces of height clear. Open it and place one oak plank stack in the first slot and one torch stack in the second. Leave remaining slots empty.

**Capture:** Capture the complete storage screen with its title, all six rows, player inventory, and hotbar. Keep the mouse away from items.

**Must show:** 54 dresser slots, two occupied slots, and the container title.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/furniture/dresser-storage.png`

**Alt text:** Dresser interface showing six rows of shared storage.

**Caption:** The complete dresser shares one 54-slot inventory.

### furniture-03 — Bed footprint

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Bed footprint

**Setup:** In the Overworld, reserve a two-wide by three-deep by two-high space on a solid floor. Place an oak red four-post bed, standing at its foot and looking toward the intended head. Leave a one-block walkway on both sides and at the foot. Use contrasting floor blocks around the footprint if needed to show scale.

**Capture:** Capture a three-quarter view from the foot with all posts, both mattress sides, and nearby walkways visible. Hide the HUD.

**Must show:** Full bed height, two-wide mattress, three-deep footprint, and clear exit space.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/furniture/four-post-bed.png`

**Alt text:** An oak four-post bed with red bedding and clear walkways around its sides.

**Caption:** Reserve the full two-by-three footprint and two block spaces of height.

## Wall and stair sample area

### building-01 — Layer wall rows with finishes

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Layer wall rows with finishes

**Setup:** Build a six-wide, three-high wall of full stone blocks on a solid floor. Leave the leftmost column bare. Apply wainscoting to the bottom faces of the other five columns and cornflower wallpaper to their two rows above. Use Creative; no plaster prerequisite is needed.

**Capture:** Face the wall at a slight angle showing the finished surface and one exposed edge. Hide the HUD.

**Must show:** Bare comparison column, wainscoting row, and wallpaper above; no extra overlay on the bare column.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/building/wall-finishes.png`

**Alt text:** Stone wall with a bare column beside wainscoting below cornflower wallpaper.

**Caption:** Different rows can carry different finishes without replacing the wall blocks.

### building-02 — Trim a wall opening

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Trim a wall opening

**Setup:** Build a seven-wide, five-high wall with a three-wide by two-high window opening, one block above the floor. Frame the opening with casing using edge clicks; put a window apron below it. Add baseboard at floor level and crown moulding along the top. Extend the wall around one right-angle corner so the automatic trim corner is visible. Use a full-block wall and leave adjacent trim spaces clear.

**Capture:** Capture the whole opening, floor trim, ceiling trim, and corner from a slight angle. Hide the HUD. If a connection cannot be reproduced, report it rather than concealing it.

**Must show:** Casing edge alignment, apron, baseboard, crown, and one connected corner.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/building/window-trim.png`

**Alt text:** A framed window with an apron, baseboard, and crown moulding continuing around a wall corner.

**Caption:** Place the supporting wall first, then align trim to its faces and opening edges.

### building-03 — Stair rail and balustrade

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Stair rail and balustrade

**Setup:** Make a three-block-wide stair run rising one block per step for four steps, using bottom-half oak stairs and a solid wall on the west side. Put a level landing at each end. Add oak wall railings on the wall, oak balustrades on the open east edge, and oak newel posts on the landings. Start balustrades on the stairs upper treads, then extend the sloped run as the placement rules permit.

**Capture:** From the open side, show the full rise and both landings, with wall rail and freestanding rail distinguishable. Hide the HUD. Record any placement ambiguity for the chapter review.

**Must show:** At least one slope transition, the wall-mounted rail, the freestanding balustrade, and a full-height newel.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/building/stair-rails.png`

**Alt text:** A straight stair run with a wall handrail, open-side balustrade, and landing newel posts.

**Caption:** Wall railings and freestanding balustrades serve different support arrangements.

## Kitchen and bathroom suite

### kitchen-01 — Craftable kitchen worktop

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Craftable kitchen worktop

**Setup:** Place a row of four oak cabinets on a full-block floor, mixing door and drawer cabinets. Sneak-place calcite countertops above two units, a calcite sink above the third, and an induction cooktop above the fourth. Stack two matching oak door cabinets beside the row with the same facing. Use Creative for setup, but use only the named craftable materials.

**Capture:** Capture a front three-quarter view with the top surfaces, sink faucet, cabinet styles, and tall stacked pair visible.

**Must show:** Thin surfaces above the units, sink, decorative cooktop, and vertically connected door cabinets.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/kitchens/worktop.png`

**Alt text:** Oak kitchen cabinets with calcite worktops and sink, an induction cooktop, and a stacked pantry.

**Caption:** Cabinets store items; countertops, sinks, and cooktops complete the kitchen appearance.

### kitchen-02 — Refrigerator scale

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Refrigerator scale

**Setup:** Place a refrigerator beside the kitchen, reserving two blocks of width, one of depth, and three of height. Use a wall or tiled backdrop with a visible block grid. Keep the view unoccupied by players.

**Capture:** Show the full appliance from a front three-quarter angle, including its feet, top, and adjacent cabinetry for scale.

**Must show:** Both columns and all three vertical sections, without cropping the top.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/kitchens/refrigerator.png`

**Alt text:** A full-height refrigerator beside a kitchen cabinet row.

**Caption:** The refrigerator reserves a two-by-one-by-three volume and shares 54 storage slots.

### kitchen-03 — Working oven interface

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Working oven interface

**Setup:** Place an oven, open it, put eight raw iron in the input and one coal in the fuel slot. Wait until at least one iron ingot is in the output while smelting is still active. Leave the output in place.

**Capture:** Capture the full oven screen with the title, input, fuel, flame/progress indicators, output, and player inventory. Keep the pointer away from slots.

**Must show:** Raw iron input, coal fuel, active cooking indication, and iron ingot output.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/kitchens/oven.png`

**Alt text:** Oven interface smelting raw iron into iron ingots with coal fuel.

**Caption:** The oven uses furnace smelting recipes, not just food recipes.

### kitchen-04 — Bathroom interactions

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Bathroom interactions

**Setup:** In a room with a solid floor and ceiling, leave a three-by-three area for a clawfoot bathtub and place it. Use it once to show the filled state. Place a toilet beside it and open the lid. Build a separate three-high glass shower surround with a glass shower door; attach a wall shower head to the wall and a rain shower head under the ceiling above the shower. Keep all fixtures unobstructed.

**Capture:** Capture from a raised three-quarter viewpoint showing the filled tub, open toilet lid, shower glass, and both shower-head mounts. Hide the HUD.

**Must show:** Tub filled appearance, toilet lid, shower entrance, and supporting wall/ceiling for heads.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/kitchens/bathroom.png`

**Alt text:** Bathroom fittings with a filled clawfoot tub, open toilet lid, and glass shower enclosure.

**Caption:** The bath and toilet have appearance toggles; shower heads are decorative fittings.

## Copper lighting gallery

### lighting-01 — Lighting placement gallery

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Lighting placement gallery

**Setup:** Build a well-lit display room with a full-block floor, wall, and ceiling six blocks above the floor. Include a copper wall sconce, a chandelier hanging from a vertical copper chain, both copper ceiling-light styles, a copper table lamp on an oak side table, a copper floor lamp, and a copper lamp post. Obtain base-stage copper variants from Creative immediately before capture.

**Capture:** Use a wide three-quarter view that shows ceiling support, wall support, table, and both three-high standing lights. Hide the HUD; keep every fixture fully in frame.

**Must show:** Seven lighting forms and the vertical chain supporting the chandelier.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/lighting/fixtures.png`

**Alt text:** A display of copper sconces, chandelier, ceiling lights, table lamp, floor lamp, and lamp post.

**Caption:** Each light has a placement role; copper chains provide support rather than illumination.

### lighting-02 — Compare copper stages

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Compare copper stages

**Setup:** Place four oak side tables in a row, one block apart. From Creative, place a waxed copper table lamp, waxed exposed copper table lamp, waxed weathered copper table lamp, and waxed oxidized copper table lamp in that left-to-right order. The waxed forms preserve the display colors. This is a Creative comparison, not a Survival aging demonstration.

**Capture:** Take a front three-quarter view with all four lamps at similar scale. Hide the HUD; use neutral lighting so the finish colors remain distinct.

**Must show:** Copper, exposed, weathered, and oxidized stages in the stated order.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/lighting/copper-stages.png`

**Alt text:** Four waxed table lamps showing copper, exposed, weathered, and oxidized finishes from left to right.

**Caption:** Waxing preserves each stage; unwaxed copper changes through oxidation.

## Measured two-floor elevator

### elevator-01 — Open-side elevator: front three-quarter view

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Open-side elevator: front three-quarter view

**Setup:** Build the revised coordinate example at Y=64: 3x3 elevator floors at Y=64 and 70, X=1..3 and Z=0..2; one continuous shaft-panel column at X=2 Z=-1 from Y=64 through 70. At each stop place the call button at X=1 Z=2 facing south, the door at X=2 Z=2, and the destination control at X=3 Z=2 facing north, all directly above elevator floors. Add ordinary entrance frames, rear/west walls, and approach platforms to the south. Leave the east side beyond X=3 open on both levels. Keep the same build for elevator-03. Use a clear Creative test area and offset every coordinate together if needed.

**Capture:** Take a front three-quarter view from the southeast, looking northwest through the open east side. Frame both stops and their rear shaft connection. Position the camera far enough toward the front to read the exterior call-button faces beside the doors, while keeping the shaft panels visible through the open side. Doors may be closed; do not obscure the functional parts with decoration.

**Must show:** The elevator entrance and outward-facing call button beside it, the shaft-panel column on the opposite rear edge, both 3x3 floor levels, and the open east side. The door, call button, and shaft must be distinguishable in the same view.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/elevators/two-floor-front.png`

**Alt text:** Front three-quarter view of a two-stop elevator with exterior call buttons beside the doors and the rear shaft visible through an open side.

**Caption:** The call button faces the approach beside the door; the shaft panels sit opposite the entrance at the rear.

### elevator-03 — Open-side elevator: rear three-quarter view

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Open-side elevator: rear three-quarter view

**Setup:** Use the exact same open-east-side build and decoration as elevator-01. Do not move either control between views. The destination controls are at X=3 Z=2, facing north into the cabins, opposite the outward-facing call buttons across each doorway.

**Capture:** Move to the northeast of the build and look southwest through the open east side toward the inside of the entrance. Frame both landings if the control faces remain legible; otherwise prioritize a clear interior view of one landing with enough of the other to establish the shared build. Keep the inside door face and adjacent destination-control face unobstructed.

**Must show:** The inside of the elevator door, the inward-facing destination control beside it, and their positions directly above the elevator floor. The open side must reveal that this is the same layout as the front view.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/elevators/two-floor-rear.png`

**Alt text:** Rear three-quarter view through the open side of an elevator, showing the inside of the door and the destination control beside it.

**Caption:** Inside the cabin, the destination control faces the passenger from the other side of the doorway.

### elevator-02 — Select an elevator destination

**Status:** Image supplied; see the contributor guide for visual review status

**Purpose:** Select an elevator destination

**Setup:** Use the same measured example. At the lower floor, use the call button, board fully, and use its destination control. Stop with the destination screen open before selecting a floor. With only two stops, it should offer Floor 2.

**Capture:** Capture the entire destination screen with its title and Floor 2 button legible. Keep the pointer off the button if hover obscures text. Record whether the trip up and back succeeds separately.

**Must show:** Selection-screen title and Floor 2 as the destination from the lower stop.

**Avoid:** Use default textures without shaders. Close chat and debug overlays. Use F2; hide the HUD for world views, but keep menus visible for interface captures. Supply an unedited PNG. Report any setup that does not reproduce the requested state.

**Save as:** `docs/images/elevators/floor-selection.png`

**Alt text:** Elevator destination screen offering Floor 2 from the lower landing.

**Caption:** Floor numbering follows height; the current stop is omitted from the destination choices.
