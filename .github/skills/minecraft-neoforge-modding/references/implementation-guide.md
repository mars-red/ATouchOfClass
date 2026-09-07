# NeoForge implementation guide

Use only the sections relevant to the task, and adapt every API name to the pinned
Minecraft and NeoForge versions.

## Content and registration

- Identify the correct registry and existing registration holder before adding content.
- Register blocks and their item forms deliberately; do not assume one creates the other.
- Keep identifiers stable and namespaced with the repository's mod id.
- Delay access to registered values until their lifecycle permits it; avoid eager static
  initialization that dereferences registry objects too early.
- For dynamic/datapack registries, follow the pinned version's codec and bootstrap/data
  generation model rather than treating them like startup registries.

## Events and lifecycle

- Determine whether an event belongs on the mod event bus or game/runtime event bus.
- Check whether handlers run in parallel and use the supported enqueue/scheduling API
  for work that must be serialized.
- Make subscriber side and distribution explicit when client classes are referenced.
- Avoid duplicate registration through both annotations and manual listeners.

## Menus, screens, rendering, and input

- Keep menu/container logic common and screen/render/input registration client-only.
- Validate interaction distance, ownership, slot rules, and server state; the screen is
  not a security boundary.
- Do not mutate authoritative gameplay state solely from a render or input callback.
- Register renderers, color handlers, screens, key mappings, and model extensions in
  the lifecycle phase used by the pinned version and repository.

## Networking

- Use the pinned NeoForge payload registration API and its supported codec mechanism.
- Give payloads stable names and an explicit direction where supported.
- On receipt, validate permissions, entity/block targets, dimensions, ranges, sizes,
  indices, enum values, and rate-sensitive actions before changing server state.
- Move world mutations to the proper game thread using the payload context or the
  version-appropriate scheduling mechanism.
- Do not send secrets, entire registries, or large unbounded collections. Prefer vanilla
  synchronization hooks when they already cover the state.

## Persistent and data-driven state

- Choose the mechanism that matches ownership and lifetime: component/data component,
  attachment/capability, block entity, entity/player data, level data, config, recipe,
  tag, or datapack registry.
- Define copy/clone, death, dimension-change, save/load, and synchronization behavior.
- Keep codecs and stream codecs symmetric. Bound collections and handle decode failure
  according to the pinned API's expectations.
- Treat migrations and renamed identifiers as explicit compatibility work.

## Resources and data generation

- Verify paths and namespaces for models, blockstates, textures, language entries, loot
  tables, recipes, tags, advancements, sounds, and worldgen data.
- Keep resource identifiers lowercase and use forward-slash logical paths.
- When data generation is configured, update providers and run the configured generation
  task. Review the resulting JSON rather than assuming provider success means semantic
  correctness.
- Avoid hand-editing generated output unless the repository intentionally treats it as
  authored source.

## Porting

Create a short migration inventory before editing: build plugin and Java level, metadata,
mappings/name changes, registries, events, networking, rendering, serialization, data
formats, and removed hooks. Port one coherent slice at a time and compile after each
slice. Replace obsolete APIs with the target version's design instead of mechanical
renames when lifecycle or semantics changed.

