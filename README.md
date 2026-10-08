# A Touch of Class

Architectural details, furniture, lighting, kitchens, bathrooms, keyed doors, and elevators for Minecraft.

**[Read the manual](docs/README.md)**

| Start here | What you will find |
| --- | --- |
| [Players](docs/getting-started.md) | Installation, building, furnishing, and using the mod |
| [Server owners](docs/server-owners.md) | Multiplayer setup, operation, and maintenance |
| [Modpack authors](docs/modpack-authors.md) | Content availability and customization |

## Current development target

This checkout targets Minecraft **1.21.4** and NeoForge **21.4.136**. The declared NeoForge minimum is 21.4.136; later builds must still be compatible with Minecraft 1.21.4. The project declares mod version **1.0.0**; this is a source version, not a statement that a public release is available. Use matching mod builds on the server and its clients.

The manual is under construction. [Tall doors, keys, and access](docs/doors-and-access.md) is the first full chapter, ready for text review and screenshot capture. Other chapters currently contain their agreed scope.

Some content is intentionally Creative-only. Concrete countertops and sinks have no bundled crafting recipes; crafting is deferred until the mod targets a Minecraft version with vanilla concrete slabs.

## Documentation review

- [Pilot screenshot checklist](docs/contributing/screenshots.md)
- [Capture and documentation conventions](docs/contributing/README.md)
- [Content audit and verification notes](docs/contributing/content-audit.md)

## Building from source

Use the included Gradle wrapper with Java 21. On Windows, run `./gradlew.bat build`; development client launch uses `./gradlew.bat runClient`.

Minecraft mappings are covered by Mojang's mapping license; see the mapping license supplied with the development environment.
