# Reference and troubleshooting

[Manual contents](README.md)

Use the [content catalog](catalog.md) to find exact item IDs and acquisition routes, and the [recipe reference](recipes.md) for complete bundled inputs and outputs. Both are generated from the current registration families and resources, not a running modpack's overrides.

## Material families

The common 12-wood set is **oak, spruce, birch, jungle, acacia, dark oak, mangrove, cherry, pale oak, bamboo, crimson, and warped**. It applies to tall doors/sidelights, cabinets, dressers, nightstands, side tables, and wood rail families. Four-post beds use that set **without bamboo**, combined with 16 bedding colors.

The 16 colors are white, orange, magenta, light blue, yellow, lime, pink, gray, light gray, cyan, purple, blue, brown, green, red, and black. Concrete countertops and sinks use these colors but are Creative-only. Wood and copper finishes are separate item variants; do not assume dyeing or swapping a material in an unspecified grid will create one.

Copper lighting/chain families use unaffected, exposed, weathered, and oxidized stages, each with waxed forms. Refer to [lighting](lighting.md) for how to reach and preserve those stages.

## Fixed capacities and dimensions

| Feature | Current value |
| --- | --- |
| Key ring | 16 slots; one coded door key per slot |
| Key-code text field | Up to 10 characters; exact matching |
| Cabinet or nightstand | 27 slots |
| Dresser or refrigerator | 54 slots per complete furnishing |
| Plaster bucket | 32 applications |
| Tall door, sidelight, shower surround/door, elevator door | Three blocks high |
| Floor lamp or lamp post | Three blocks high |
| Elevator call/destination control; newel post | Two blocks high |
| Dresser | Reserve 2 × 1 × 2 block spaces |
| Refrigerator | Reserve 2 × 1 × 3 block spaces |
| Four-post bed | Reserve 2 × 3 × 2 block spaces |
| Clawfoot bathtub | Reserve 3 × 3 × 1 block spaces |
| Elevator floor footprint | At most 256 horizontally connected floor blocks |
| Elevator shaft discovery | Up to 256 panels scanned above and below the starting height |
| Elevator travel volume | Rectangular floor bounds, five blocks above the floor top |

These are implemented values, not exposed configuration settings. Width and depth depend on the furnishing's facing. For detailed placement, follow the relevant chapter.

## Controls at a glance

| Action | Default interaction |
| --- | --- |
| Apply a finish | Use its item on an allowed face |
| Strip a finish | Start mining the decorated face |
| Open storage or sit | Use with an empty hand |
| Place on storage | Sneak-place to avoid opening the menu |
| Open a key ring | Hold it and use into the air |
| Operate a coded tall door | Use a matching key or ring on any section |
| Open a shower door; toggle tub or toilet | Use the fitting |
| Use an elevator | Call button, then destination control after boarding |
| Capture documentation | F2 in Minecraft Java; PNG saved in that instance's screenshots folder |

## Troubleshooting by symptom

| Symptom | First checks |
| --- | --- |
| Mod absent | Correct Java Edition instance, Minecraft/NeoForge target, one matching mod JAR in `mods`; inspect startup logs |
| Server connection fails | Match server/client builds and required mods; retain the actual error text |
| Recipe absent | Consult catalog availability, exact inputs, and pack overrides; concrete fittings are intentionally Creative-only |
| Large furnishing does not place | Full volume clear, support below every base section, correct clicked face |
| Surface finish appears on the wrong face | Reapply to the intended face; each face stores its own overlay |
| A decorative fitting does nothing | Check the kitchen functionality table; not every appliance model has a simulation |
| Key fails | It must operate a tall door, and the code must match exactly; names do not grant access |
| Container contents dropped on removal | Expected for storage furniture; rings retain contents as items |
| Elevator fails | Follow its footprint/shaft/contact checks before changing decorative walls |
| Behavior changed after installing a pack | Check data packs, protection mods, and resource overrides against an isolated test world |

For a useful report, include expected and actual behavior, exact versions, reproduction steps, world mode/dimension, a screenshot of the complete layout, and relevant logs. State whether the same behavior occurs with only A Touch of Class and its required loader.

## Glossary

- **Tall door:** the mod's three-high coded-access door, distinct from vanilla, shower, and elevator doors.
- **Code:** the exact string shared by a tall door and its authorized keys; not the descriptive name.
- **Overlay/finish:** decoration saved on a block face without replacing the supporting block.
- **Footprint:** a horizontally connected patch of elevator floor blocks at one height.
- **Cabin:** an elevator footprint with a destination control; its bounding rectangle defines the travel area.
- **Stop:** all elevator footprints at one height associated with a shaft.
- **Reservation:** temporary exclusive use of a shaft while calling, boarding, or traveling.
- **Variant:** a separately registered material, color, or finish form.
- **Creative only:** intentionally no bundled Survival acquisition route; not merely a missing direct recipe.

## Verification status

Author review of the full manual and all in-game acceptance checks were confirmed complete on October 10, 2026. All 22 screenshots are integrated; see the [capture checklist](contributing/screenshots.md) and [verification record](contributing/content-audit.md).
