<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SUMMARY

## [AMENDED 2026-05-04]: For the next AI agent — resume work

1. Open **`docs/SCRATCHPAD.md`** and read **“AI RESUME HERE — 2026-05-04”** (toolchain, SQLite status, compile blocker, ordered next steps).
2. **Authoritative versions** for builds: **`gradle.properties`** + **`build.gradle`** — not the “Current Version” list below (historical 1.21.11 snapshot).
3. **[AMENDED 2026-05-05]:** `./gradlew compileJava` is **green** on the 26.1.2 Mojang-named tree; SQLite stack can ship with the mod JAR. Older SCRATCHPAD lines may still mention a historical compile blocker — verify with a fresh **`./gradlew build`** if unsure.

## Project Status
**Feeshman Deelux** — Minecraft 26.1.2 Fabric auto-fishing mod.

[AMENDED 2026-05-04]: **Active development target** is **Minecraft 26.1.2** / Fabric Loom **`net.fabricmc.fabric-loom` 1.16.1** / mod **1.4.0** (see `gradle.properties`). **SQLite + DB layer** ship with the mod; `./gradlew build` is expected to succeed on the 26.1.2 tree ([AMENDED 2026-05-05]: verify locally after pulls).


## Quick Links
- **[SCRATCHPAD — AI resume block](SCRATCHPAD.md)** — **Start here** for continuation (2026-05-04)
- [README](../README.md) — User-facing overview, install, usage
- [ARCHITECTURE](ARCHITECTURE.md) — System structure + Mermaid diagrams
- [docs/README](../docs/README.md) — Documentation hub
- [docs/PROJECT_STATUS](../docs/PROJECT_STATUS.md) — Detailed status
- [docs/BUILD_GUIDE](../docs/BUILD_GUIDE.md) — Build & troubleshoot
- [SBOM](SBOM.md) — Dependencies + toolchain notes
- [CHANGELOG](CHANGELOG.md) — Version history

## Current Version (26.1.2)
- **Mod**: 1.4.0
- **Minecraft**: 26.1.2
- **Fabric Loader**: 0.19.2
- **Fabric API**: 0.148.0+26.1.2
- **Loom**: 1.16.1
- **ModMenu**: 18.0.0-alpha.8
- **Gradle** (wrapper): 9.6.0-20260503004846+0000 (nightly)
- **Java**: 25

## Previous Version (1.21.11 — stable)
- **Mod**: 1.3.01
- **Minecraft**: 1.21.11
- **Fabric Loader**: 0.18.1
- **Fabric API**: 0.141.3+1.21.11
- **Loom**: 1.14.1
