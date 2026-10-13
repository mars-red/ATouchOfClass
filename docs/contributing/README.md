# Documentation and screenshot workflow

[Manual](../README.md) · [Capture checklist](screenshots.md) · [Content audit](content-audit.md)

## Review the manual

1. Read the chapter you are reviewing. Note unclear wording or behavior that differs in game. The tall-door pilot already has its five images.
2. All 22 captures are supplied. Use the capture checklist when reviewing images or preparing replacements. Use Creative for setup unless a task says otherwise; follow the chapter separately in Survival when checking acquisition.
3. Save PNG files at the exact listed paths, or provide the images with their stable IDs (for example, `building-01` or `elevator-02`) so they can be placed for you.
4. Include the Minecraft, NeoForge, and mod build used, GUI scale, and any deviations from the instructions. We will inspect the images and refine the matching chapter and capture instructions.

## Capture standard

- Press **F2 in Minecraft Java** to save a PNG of the game without the window title bar. Use the current game instance's screenshots folder; development launches commonly use `run/screenshots`. The capture is a file rather than a clipboard image.
- Use a disposable test world, default resource pack, no shaders, and English interface labels for this English manual.
- Prefer 1920 × 1080 PNG captures at GUI scale 3. If that does not fit your display, use a consistent resolution and scale and report them; legibility takes priority.
- Use daylight and clear weather for exterior views. Hide the HUD for scenery; keep the full interface visible for menus. Keep chat and debug overlays closed.
- Supply clean originals without arrows, resizing, or annotations. Interface screenshots may be cropped consistently during integration; preserve every control needed to understand the step.
- The task's “Must show” field is the acceptance criterion. If the game does not produce the specified state, report what happened rather than staging a misleading image.
- Use demonstration codes only. Screenshots supplement the written instructions; essential information must remain available as text.

## How placeholders become images

The single source of capture instructions, captions, alt text, and paths is [screenshots.json](screenshots.json). Each chapter contains a pair of stable screenshot markers. From the repository root in PowerShell 7, run:

```powershell
./tools/update-doc-screenshots.ps1
```

The helper updates only the marked blocks and the capture checklist. Missing files produce readable capture instructions instead of broken images. Existing PNG files produce standard Markdown images and captions. Add the image to its assigned path and rerun the helper; no chapter editing is needed. Presence means **ready for visual review**, not approved. Review approval is recorded below, separately from generated content.

To check generated content without changing it:

```powershell
./tools/update-doc-screenshots.ps1 -Check
```

Do not edit generated blocks or the generated checklist directly. Edit the manifest, then regenerate. Keep images under `docs/images/` and use lowercase, hyphenated names. Keep IDs stable if a filename or caption changes.

## Chapter conventions

Feature chapters cover purpose, acquisition, placement, operation, variants, and troubleshooting. Group material variants into families. Document implemented behavior; label source-verified findings separately from in-game verification. Record sources in the audit rather than burdening player pages with implementation details.

Always name the mod's **tall doors** explicitly when describing placement, keys, codes, or access. These features do not apply to vanilla doors. Keep the actual item name **Door Key** and example labels such as **Front Door** unchanged.

Use **Survival obtainable**, **Creative only**, and **Acquisition under review** consistently. Concrete countertops and sinks are intentionally Creative-only until the mod targets a Minecraft version with vanilla concrete slabs. Do not invent recipes or treat that omission as a bug.

## Pilot review record

- Text review: complete as part of the full-manual author review on October 10, 2026.
- In-game behavior review: completed, confirmed by the author on October 10, 2026.
- Screenshot visual review: all five supplied images accepted for the pilot on October 8, 2026. Entrances and both cutter states show the required details. The ring uses its default title; the manifest and alt text now reflect that, and renaming is optional. Its tooltip overlaps empty slots while leaving both keys visible.
- Capture details: all five PNGs are 856 × 512, including the window frame. The title bar identifies Minecraft NeoForge 1.21.4; exact mod/loader builds and GUI scale were not supplied. Text remains legible at native size. Window frames are retained as supplied; future captures should preferably use the in-game screenshot function to omit desktop window chrome.
- Acceptance: all steps reproduce, images are legible and match their captions, and capture instructions have been revised for any ambiguity.

## Refresh the reference

Run `./tools/update-doc-reference.ps1` from the repository root in PowerShell 7 to rebuild the registered-content catalog and recipe reference. Use `-Check` to detect stale output without writing it. The generator parses the currently used registration families; extend it when adding a new registration mechanism. It rejects unsupported recipe formats and recipe outputs that it cannot reconcile to mod registrations.

## Full-manual review record

- Chapters: source-reviewed October 8, 2026. The author completed review of all chapters and sections on October 10, 2026, reporting only minor changes.
- Screenshot import, October 9, 2026: 21 timestamped PNGs from `run/screenshots` visually matched to checklist subjects and copied to their assigned paths, replacing the five pilot captures. Originals remain in `run/screenshots`. The Creative inventory tab (`start-01`) was supplied separately afterward, visually checked, and integrated, completing all 22 captures. These game-only captures supersede the window-framed pilot images described above; detailed capture acceptance and gameplay review remain separate from this import.
- In-game checks: all original checks completed, confirmed by the author on October 10, 2026. Coverage includes the first-room walkthrough; trim corner/stair placement; seating and sleeping; appliance interfaces; copper transformations; measured two-floor elevator; dedicated-server persistence; and example data-pack reload. Square and round plaster columns were subsequently confirmed in game on October 13, 2026.
- Completion is recorded from the author's confirmation. Exact test build identifiers were not supplied. Generator checks validate files and links separately from runtime behavior.
