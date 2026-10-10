# Server owner guide

[Manual contents](README.md) · [Player installation](getting-started.md) · [Troubleshooting](reference.md)

A Touch of Class adds world blocks, items, inventories, saved decoration data, and client interfaces. Install it on both the dedicated server and every connecting client. Keep the mod build, Minecraft version, and compatible NeoForge versions aligned.

## Install on a server

1. Prepare a Java 21 environment and a NeoForge server for Minecraft 1.21.4. This source tree declares NeoForge 21.4.136 as the minimum; use the tested loader distributed with your pack.
2. Follow the [official NeoForge server installation instructions](https://docs.neoforged.net/user/docs/server/). On Windows, the installed server uses `run.bat`; complete the normal first-run setup and EULA acceptance yourself.
3. Stop the server and place the mod JAR in its `mods` folder. Put the matching JAR in each client's game-instance `mods` folder.
4. Start the server, inspect startup output, and connect with a matching client. Confirm the mod content appears before opening the world to players.
5. On a test copy, exercise a container, a decorated wall, a coded tall door, and a two-stop elevator. Disconnect/reconnect and restart once to check persistence.

This manual's instructions are source-reviewed. A dedicated-server smoke test for this complete manual has not yet been recorded.

## Configuration and administration

The current mod source registers **no custom configuration file and no custom administrative command tree**. There is no documented setting to change key-ring capacity, storage size, elevator limits, or lamp brightness. The [reference](reference.md) lists fixed values. Data packs can change recipes and tags, as described in the [modpack author guide](modpack-authors.md).

Ordinary Minecraft operator commands remain available according to your server's permissions. For example, `/give @s atouchofclass:key_ring` gives an empty ring to the executing player. This is a vanilla command using a mod item ID, not a mod-specific administration feature. Use the key cutter to create demonstration codes through the supported interface.

## Shared access and storage

Coded access applies to the mod's **tall doors**, not vanilla doors or elevator/shower doors. Players can share keys by using identical codes. There is no owner identity check; anyone with a matching code can prepare another key. A code is an access convention, not a claim or block-protection system.

Both leaves of paired tall doors should have the same code: the clicked leaf's code is checked before its paired neighbor moves. Codes and descriptive names survive breaking and replacing tall doors. Breaking a tall door still allows passage through the opening, and a tall door left open stays open until used again.

Storage furniture is shared block storage. Cabinets, nightstands, dressers, and refrigerators do not gain private ownership from a nearby coded entrance. Contents drop when their container is destroyed. Key rings are different: their inventory travels with the ring item.

Do not assume compatibility with a claim/protection mod without testing. Check placement, ordinary breaking, overlay removal, paired-door use, and inventory access as both an authorized and unauthorized player. A Touch of Class's source does not establish a universal protection-plugin contract.

## Elevator operations

One active reservation exists per shaft column per dimension. A busy shaft can be held by entities inside the cabin volume, including mobs or dropped items. The [elevator chapter](elevators.md) explains boundaries, timeouts, floor numbering, and recovery checks.

Elevator reservations are transient and cleared on server lifecycle events; floor definitions are rediscovered from blocks. Do not edit shafts or landing footprints during a trip. Build matching clear arrival areas, and test entity transport before allowing a complicated multi-cabin layout in a shared world.

## Backups and upgrades

Stop the server cleanly before making a consistent backup. Back up the **whole world**, including dimension data and player data, along with the mod list and any data packs. Inventories and coded items live in world/player data; wall finishes also use per-level saved data named `atouchofclass_wall_overlays`. A structure-only export is not a complete backup of this mod's world state.

Before upgrading, restore a copy of that backup into a separate test server, install the proposed build on it and a test client, and repeat the persistence checks. Verify that existing tall doors, ring contents, furniture storage, wall finishes, and elevators still behave as expected. Read the release notes for the actual build; this manual does not promise compatibility between arbitrary builds.

Removing the mod makes its registered content unavailable. Plan removal on a copy: empty mod containers, recover needed items, and replace mod-dependent construction before changing the production installation. Keep the original backup for rollback.

## Diagnose and report

Record the exact mod build, Minecraft and NeoForge versions, other mods/data packs, whether the problem occurs in single-player, and a short reproduction. Include the relevant server/client log or crash report after reviewing it for private server information. Screenshots should show the entire relevant structure; for an elevator, include floor contact with the shaft and the positions of the controls.

Start with the chapter's troubleshooting table. If a fresh test world with only the mod works, reintroduce the pack's changes methodically. No broad compatibility certification is implied by a successful source build.
