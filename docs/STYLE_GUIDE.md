<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# STYLE_GUIDE

## Purpose
Project-specific coding and documentation conventions for Feeshman Deelux.

## Java Conventions
- Keep methods focused and short where practical.
- Prefer clear names over abbreviations.
- Use `LOGGER` (Log4j2/SLF4J bridge), avoid `System.out`.
- Use early returns for guard clauses.
- Keep side effects near call sites; avoid hidden mutation.

## Minecraft/Fabric Conventions
- Treat `gradle.properties` + `build.gradle` as source of truth for versions.
- Prefer Mojang-named APIs used by the current Loom setup.
- Keep client UX in client classes and gameplay authority in server-side services.

## Documentation Conventions
- Status docs are append-only: annotate with `[AMENDED YYYY-MM-DD]`.
- Add newest updates near the top where feasible.
- If legacy sections are stale, annotate them rather than deleting.
- Keep handoff details in `docs/SCRATCHPAD.md`.

## Trace Tag Format
When needed, use:
- `// [TRACE: docs/SCRATCHPAD.md]`
- `// [TRACE: docs/ARCHITECTURE.md]`

Only add trace tags where they improve maintainability.

## Comment Rules
- Comments explain **why**, not **what**.
- Allowed prefixes: `TODO:`, `FIXME:`, `NOTE:`.
- Avoid obvious comments for self-explanatory code.

## Error Handling
- Fail fast at boundaries (I/O, network, DB).
- Do not swallow exceptions silently.
- Surface actionable logs when recoverable.

## Versioning / Release Notes
- Record behavior changes in `docs/CHANGELOG.md`.
- Keep player-facing docs aligned with shipped behavior.

## [AMENDED 2026-05-05]
This file was added during docs organization to provide a single, stable convention reference.
