<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# CHANGELOG

All notable changes to Feeshman Deelux. [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.4.0] — Unreleased

### Changed
- Retargeted to **Minecraft 26.1.2** (Fabric Loader 0.19.2, Fabric API 0.148.0+26.1.2, Loom 1.16.1)
- Java **Gradle toolchain** set to **26** (was release 25 in earlier 1.4.0 attempt)
- [2026-05-05] Java **25** toolchain + **`release 25`** again for shipped JAR compatibility with **Java 25** runtimes (see Fixed)
- ModMenu updated to 18.0.0-alpha.8; Placeholder API to 3.0.0+26.1
- All user-facing docs updated to reflect 26.1.2
- **Mojang 26.1 source port:** Yarn-era imports replaced across server + client (e.g. `ServerPlayer`, `Commands`/`CommandSourceStack`, `Component`, `FishingHook`, `HudElementRegistry` + `GuiGraphicsExtractor`, `PayloadTypeRegistry.clientboundPlay()`, `KeyMappingHelper`, ModMenu screens on `extractRenderState`)

### Fixed
- **Prism / Java 25 launchers:** `UnsupportedClassVersionError` (mod **v70** vs JVM **≤ v69**) — build now emits **Java 25** bytecode (`toolchain` 25, `options.release = 25`)
- `FeeshmanDatabase.read` / `writeReturning` now accept `ThrowingFunction` so SQLite DAO lambdas that `throws Exception` compile on Java 26

### Added
- SQLite persistence layer (`org.xerial:sqlite-jdbc 3.46.1.3`) — stats DB at `config/feeshmandeelux/stats.sqlite`

## [Unreleased — docs]

### Changed
- Docs: **`SCRATCHPAD.md`** — added **“AI RESUME HERE — 2026-05-04”** handoff (26.1.2 toolchain, SQLite implementation status, `compileJava` blocker, ordered next steps, key paths). **`SUMMARY.md`** — amend block + Quick Link to SCRATCHPAD; note stale “Current Version” table. **`docs/README.md`** — AI agent pointer to SCRATCHPAD resume block.
- Docs: `SBOM.md` consolidated (single dependency + toolchain view); `SUMMARY.md`, `BUILD_GUIDE.md`, root `summary.md`, `README.md` (Gradle badge) aligned with nightly wrapper **9.6.0-20260503004846+0000**
- [2026-05-05] Project audit: **`docs/FEATURES_STATUS.md`**, **`docs/MODRINTH_DESCRIPTION.md`**, **`docs/PROJECT_STATUS.md`**, **`docs/SUMMARY.md`**, root **`README.md`** (toolchain badges, commands, hooked-entity guard vs old mob-collision copy, SQLite)

### Added
- (none)

## [1.3.01] - 2026-03

### Fixed
- FishingBobberEntityAccessor mixin failure on 1.21.11 — replaced with `getHookedEntity()` public API

### Added
- Dedicated server entrypoint (FeeshmanServerMod) for multiplayer
- Server-side join message: "Press [O] to toggle auto-fishing • /feeshman for commands"

### Changed
- FeeshmanMod skips server registration on dedicated server (FeeshmanServerMod handles it)

## [1.3.0] - 2026-03

### Added
- Server-first architecture: AutoFishService on server, S2C payloads for client UX
- Item announcements: inventory diff on server, colored chat (fish §a, treasure §6, junk §7)
- Achievement milestone toasts: 1st, 10th, 25th, 50th, 100th session; 100, 500, 1000 lifetime
- Biome count sync to client for achievements screen accuracy
- DOCS/ARCHITECTURE.md with Mermaid diagrams (component, sequence, module)

### Fixed
- Leaderboard no longer writes to disk on every catch — batch flush every 30s + on disconnect
- Lifetime fish count now persists across game restarts (loaded from leaderboard on join)
- Achievements screen now shows real session fish, biomes visited, and session time
- Rod detection uses `Items.FISHING_ROD` instead of fragile string matching
- Auto-fish no longer disables instantly when rod briefly out of hand — 3-second grace period

### Changed
- fabric.mod.json: authors "Otterdays", contact URLs (github.com/Otterdays/Feeshman-Deelux)
- Mixin config `required` set to `false` (empty mixins)
- Replaced `Random` with `ThreadLocalRandom` for random number generation
- Removed `another-mod` placeholder from fabric.mod.json suggests
- Added `/.gradle/` to .gitignore
- Upgraded to Minecraft 1.21.11 (Fabric Loader 0.18.1, Fabric API 0.141.3, Loom 1.14.1)

## [1.2.9] - 2025-06

### Added
- Leaderboard system, ModMenu integration, achievement toasts, biome tracking
