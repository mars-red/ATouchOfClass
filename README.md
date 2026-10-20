# A Touch of Class

Architectural details, furniture, lighting, kitchens, bathrooms, coded tall doors, and elevators for Minecraft.

**[Read the manual](docs/README.md)**

> Licensed under the Creative Commons Attribution 4.0 International License. See [LICENSE](LICENSE) for details.

| Start here | What you will find |
| --- | --- |
| [Players](docs/getting-started.md) | Installation, building, furnishing, and using the mod |
| [Server owners](docs/server-owners.md) | Multiplayer setup, operation, and maintenance |
| [Modpack authors](docs/modpack-authors.md) | Content availability and customization |

## Current development target

This checkout targets Minecraft **26.3** with NeoForge **26.3.0.92** and **Java 25**. The declared NeoForge minimum is 26.3.0.92; later builds must still be compatible with Minecraft 26.3. The project declares mod version 1.2.0.

The manual covers players, server owners, and modpack authors, with a [content catalog](docs/catalog.md) and [recipe reference](docs/recipes.md). Author review of all chapters and sections was completed on October 10, 2026.

## Documentation review

- [Manual screenshot checklist](docs/contributing/screenshots.md)
- [Capture and documentation conventions](docs/contributing/README.md)
- [Content audit and verification notes](docs/contributing/content-audit.md)

## Building from source

Use the included Gradle wrapper with Java 25. On Windows, run `./gradlew.bat build`; development client launch uses `./gradlew.bat runClient`.

Minecraft mappings are covered by Mojang's mapping license; see the mapping license supplied with the development environment.
