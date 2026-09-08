# NeoForge technical design record

Adapt this outline to the size of the decision. Do not retain empty boilerplate.

## Context and version contract

- Feature or problem
- Minecraft, NeoForge, Java, mappings, and build-plugin versions
- Existing repository patterns and compatibility commitments
- Goals, non-goals, assumptions, and acceptance criteria
- Evidence status for important version-specific claims

## Recommended design

- Short decision summary and rationale
- Components and responsibilities
- State ownership and lifetime
- Lifecycle and event flow
- Client/common/server boundaries
- Persistence and migration
- Networking and validation
- Registries, resources, tags, codecs, and data generation
- Integration or extension points

For a multi-step interaction, include a compact sequence such as:

```text
client input -> request payload -> server validation -> authoritative mutation
             <- bounded state sync <- persistence/update hooks
```

Use a diagram only when it makes ownership, hierarchy, or a multi-step flow clearer
than prose.

## Alternatives and tradeoffs

For each viable alternative, state the meaningful benefit, cost, compatibility risk,
and why it was selected or rejected. Include the trigger that would justify revisiting
the decision.

## Failure and compatibility analysis

Address relevant cases: dedicated-server classloading, disconnects, stale targets,
unloaded chunks, death/clone, dimension changes, malformed payloads or data, missing
optional dependencies, old saves, renamed identifiers, partial datapacks, and server
shutdown. Do not list irrelevant cases merely to fill the template.

## Verification plan

- Compile or static checks that can falsify API assumptions
- Unit, game-test, or data-generation coverage
- Dedicated-server and multiplayer scenarios
- Persistence/reload and migration scenarios
- Performance or scale checks
- Manual rendering or interaction checks

## Implementation sequence

Order work so that each slice is coherent and verifiable. Identify any prototype or
source inspection needed before committing to a version-specific API.

## Open questions

List only decisions that remain unresolved, who or what can resolve them, and their
impact. If there are none, state `None`.

