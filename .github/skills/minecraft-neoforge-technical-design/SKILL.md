---
name: minecraft-neoforge-technical-design
description: Analyze and design version-specific architecture for Minecraft NeoForge mods. Use for feasibility studies, design discussions, architecture decisions, feature specifications, data flow, client/server boundaries, persistence, networking, interoperability, performance, or implementation planning before code changes. Do not use for Fabric-only or Bukkit/Spigot projects.
argument-hint: "[feature or architecture question]"
---

# Minecraft NeoForge technical design

Produce a decision-ready design grounded in the repository's exact Minecraft and
NeoForge versions. A design request authorizes analysis and documentation, not changes
to production code unless the user also asks for implementation.

## Establish evidence and constraints

Inspect the relevant project files before recommending APIs or architecture:
`gradle.properties`, `settings.gradle*`, `build.gradle*`, version catalogs,
`neoforge.mods.toml` or `mods.toml`, the main `@Mod` class, existing registrations,
events, payloads, saved state, data generators, and related integrations.

Determine the pinned Minecraft, NeoForge, Java, mappings, and Gradle-plugin versions;
the mod id and project structure; existing architectural conventions; compatibility
commitments; generated-resource policy; and whether the feature must work on dedicated
servers, multiplayer, existing worlds, or optional mod combinations.

For version-sensitive claims, prefer evidence in this order:

1. compiling repository code and its established patterns;
2. resolved dependency sources for the pinned version;
3. matching-version pages at `https://docs.neoforged.net/`;
4. NeoForged source or official examples for that version.

Do not silently borrow Forge, Fabric, another Minecraft release, or an unpinned
"latest" API. Label important claims as **confirmed**, **inferred**, or **unresolved**
when the available evidence differs in strength. Ask a focused question only when an
unresolved choice would materially change the design; otherwise proceed with an
explicit assumption.

## Model the problem before selecting APIs

Identify:

- desired behavior, non-goals, constraints, and observable acceptance criteria;
- authoritative owner of each state value and its lifetime;
- logical-side and physical-distribution boundaries;
- events and transitions that change state;
- persistence, synchronization, migration, and invalidation requirements;
- extension points exposed to other mods or datapacks;
- scale assumptions and likely hot paths;
- failure modes, trust boundaries, and malformed or stale data handling.

Distinguish data that is registry content, datapack-driven content, configuration,
world state, level/chunk/block-entity state, entity/player state, transient runtime
state, or client presentation state. Do not choose a storage or synchronization API
until ownership and lifetime are clear.

## Compare viable designs

When more than one reasonable design exists, compare two or three concrete options.
Evaluate each against version fit, complexity, lifecycle correctness, dedicated-server
safety, bandwidth, save compatibility, extensibility, interoperability, testability,
performance, and porting cost. Reject options for specific reasons rather than taste.

Recommend one option and state what new evidence would change that recommendation.
Avoid vague abstraction layers and speculative generality; include extension points
only where a credible consumer or compatibility requirement exists.

## NeoForge design invariants

- The server is authoritative for gameplay state. Clients request actions and present
  synchronized state; they do not decide trusted outcomes.
- Common code must not cause dedicated servers to load client-only classes.
- Registration and event handling must occur on the correct bus and lifecycle phase
  for the pinned version.
- Payloads need an explicit direction, symmetric bounded codecs, correct-thread work,
  and validation of permissions, targets, ranges, sizes, and stale context.
- Persistent formats and identifiers are APIs once released. Address defaults,
  migrations, renamed identifiers, missing data, clone/death, dimension changes, and
  synchronization where relevant.
- Prefer supported NeoForge or vanilla extension points, tags, codecs, and data-driven
  content over global patches, reflection, or invasive mixins.
- Generated resources come from providers when the repository treats generation as
  the source of truth.
- Hot paths must not perform unbounded scans, blocking I/O, redundant serialization,
  or indiscriminate per-tick synchronization.

## Deliver a decision-ready artifact

Use [references/design-record.md](references/design-record.md) as a shape, omitting
sections that genuinely do not apply. Scale the detail to the decision: a small feature
may need a page, while a cross-cutting system may need component and sequence diagrams.

The result must make implementation possible without rediscovering the architecture.
Include specific classes or responsibilities, state ownership, relevant lifecycle
hooks, data and packet shapes, resource/data-generation impact, failure handling,
validation strategy, compatibility concerns, and an ordered implementation sequence.
Use API names only when confirmed for the pinned version; otherwise describe the
required capability and mark the exact symbol for verification.

End with unresolved questions and manual or automated validation scenarios. If no
questions remain, say so explicitly. Do not claim runtime behavior was validated by
design analysis alone.

