---
name: minecraft-neoforge-modding
description: Build, debug, port, test, or review Java Minecraft mods that use NeoForge, NeoGradle or ModDevGradle. Use for NeoForge registries, events, networking, data generation, resources, configuration, capabilities or attachments, game tests, mappings, and Gradle setup. Do not use for Fabric-only or Bukkit/Spigot plugin projects.
argument-hint: "[feature, bug, port, or review request]"
---

# Minecraft NeoForge modding

Treat the checked-out project as the authority for versions and conventions. NeoForge
and Minecraft APIs are version-sensitive; do not transplant examples across versions
without verifying the symbols available in this build.

## Establish the project contract

Before changing code, inspect the relevant files among `gradle.properties`,
`settings.gradle*`, `build.gradle*`, version catalogs, `neoforge.mods.toml` or
`mods.toml`, `pack.mcmeta`, the main `@Mod` class, and nearby registrations/events.
Determine:

- Minecraft, NeoForge, Gradle plugin, mappings, and Java versions;
- mod id, base package, source sets, access transformers, mixins, dependencies, and
  whether generated resources are committed;
- the repository's registration, event-bus, configuration, networking, and data-gen
  patterns.

If the repository does not reveal a required version, ask before choosing one when
that choice would materially affect generated project files or public APIs. For a
new project, prefer the current official NeoForge project generator or MDK rather
than inventing build coordinates.

## Research version-specific APIs

Prefer evidence in this order:

1. compiling code and established patterns in the repository;
2. resolved dependency sources and IDE/source artifacts for the pinned version;
3. the matching-version pages at `https://docs.neoforged.net/`;
4. NeoForged source and official examples for the pinned version.

Use general web examples only as leads. Check imports, signatures, lifecycle timing,
logical side, and version before adopting them. Never silently substitute Forge or
Fabric APIs.

Read [references/implementation-guide.md](references/implementation-guide.md) when
implementing or porting features. Read
[references/review-and-debugging.md](references/review-and-debugging.md) for a review,
crash, build failure, or behavioral bug.

## Implement coherently

- Keep registration deterministic and within the correct mod-loading phase. Reuse
  the project's `DeferredRegister` or equivalent pattern.
- Separate client-only setup and classes from common/server paths. A dedicated server
  must not load client classes through static fields, common constructors, or common
  event subscribers.
- Treat the server as authoritative for gameplay state. Validate every client-to-server
  payload, schedule work on the correct game thread, and synchronize only the state
  clients need.
- Use the repository's established serialization mechanism for persistent or data-driven
  state. Consider missing, malformed, old, and unknown data; do not casually change
  saved identifiers or packet formats.
- Put assets and data under the correct namespace. Prefer data generation when the
  project already uses it, and edit providers rather than their generated output.
- Avoid mixins, access wideners/transformers, reflection, and coremod-style techniques
  when a supported event, extension point, or API exists. If invasive access is truly
  necessary, explain the compatibility cost and keep it narrowly scoped.
- Preserve interoperability: use tags instead of hard-coded item/block lists when the
  concept is shared, avoid replacing vanilla behavior globally without need, and make
  optional integrations actually optional.

## Verify

Use the checked-in Gradle wrapper (`gradlew` or `gradlew.bat`), not a globally installed
Gradle. Discover task names from the project rather than assuming them. Start with the
narrowest applicable compile, unit-test, data-generation, or game-test task; then run
the repository's normal `check` or `build` when proportionate.

For changes involving resources or data generation, inspect unexpected generated diffs
and ensure referenced files exist with exact lowercase resource paths. For client/server
boundaries or networking, include a dedicated-server check when the project provides
one. Never claim an in-game behavior was verified if only compilation succeeded.

Report the changed behavior, validation performed, and any remaining manual in-game
checks. When blocked, include the first actionable error and enough surrounding context
to distinguish code, toolchain, dependency-resolution, and runtime problems.

