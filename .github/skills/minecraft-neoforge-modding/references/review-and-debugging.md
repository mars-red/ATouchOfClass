# NeoForge review and debugging

## Triage evidence

Collect the exact task or launch configuration, first meaningful exception, complete
`Caused by` chain, relevant log section, Minecraft/NeoForge/Java versions, and whether
the failure occurs on client, integrated server, or dedicated server. Treat later mixin,
registry, or classloading noise as secondary until the earliest causal error is understood.

For build failures, separate:

- Java compilation or type errors;
- Gradle/plugin/toolchain incompatibility;
- dependency resolution or network failure;
- data-generation validation failure;
- test/game-test failure;
- run configuration or runtime crash.

## High-value review checks

- **Version fit:** imports and APIs exist in the pinned dependencies and mappings.
- **Registration:** unique identifiers, correct registry, correct bus/phase, no eager access.
- **Sides:** common paths cannot load client-only classes; server authority is preserved.
- **Networking:** direction, codec symmetry, thread handoff, validation, bounds, permissions.
- **Persistence:** save/load symmetry, defaults, copy/clone behavior, compatibility impact.
- **Resources:** correct namespace/path/case, complete model/texture/lang/data references.
- **Data generation:** providers are the source of truth and generated diffs are intentional.
- **Gameplay lifecycle:** invalidated entities/block entities, unloaded chunks, null players,
  dimension changes, death/respawn, and server shutdown are handled where relevant.
- **Performance:** no per-tick full-world scans, repeated allocations or syncs, blocking I/O
  on game threads, or unbounded caches.
- **Compatibility:** tags and supported extension points are preferred; invasive hooks are
  justified and narrowly targeted.

## Verification ladder

Choose the lowest rung that can disprove the change, then climb as risk warrants:

1. compile the affected source set;
2. run focused unit tests or data generation;
3. run game tests if configured;
4. run the normal `check` or `build` lifecycle;
5. launch the relevant client or dedicated-server configuration for runtime-only behavior;
6. perform a concise manual scenario for rendering, interaction, networking, persistence,
   or multiplayer behavior that automation does not cover.

State exactly where verification stopped. Compilation is not proof that registration,
resources, networking, save compatibility, or gameplay behavior works at runtime.

