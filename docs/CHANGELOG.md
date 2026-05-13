<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# CHANGELOG

All notable changes to Feeshman Deelux. [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.4.0] — Unreleased

### Fixed (2026-05-13 IDE diagnostics)
- Reduced Eclipse/JSpecify null-analysis noise across client, server, and networking helpers by removing stale `@SuppressWarnings("null")` annotations where they triggered `1102` warnings and keeping targeted suppressions only at Mojang/Fabric interop boundaries that still need them.
- `FeeshmanConfigScreen` and `FeeshmanAchievementsScreen` now match inherited screen nullability expectations and no longer carry unreachable `onClose()` fallback branches flagged as dead code.
- `FeeshLeaderboard` biome lookup and `FeeshmanServerCommands` Brigadier/string formatting paths were simplified to clear unchecked nullability warnings without changing gameplay behavior.
- `AutoFishService`, `FeeshmanDeeluxClient`, `FeeshmanNetworking`, and `network/FeeshmanPayloads` received small helper cleanup passes so the touched files are IDE-lint clean again.
- Follow-up pass: `AutoFishService` payload string constructors now go through tiny helper factories, and stale suppression tokens above `getBiomeId(...)`, client helper methods, and payload helper methods were trimmed where they were the source of the remaining `1102` warnings.
- Result: the 26.1 port files touched in this diagnostics sweep (`AutoFishService`, `FeeshmanDeeluxClient`, `FeeshmanNetworking`, `FeeshmanPayloads`, `FeeshLeaderboard`, `FeeshmanServerCommands`, `FeeshmanConfigScreen`, `FeeshmanAchievementsScreen`) are IDE-lint clean without gameplay changes.

### Added (2026-05-05 QoL pass)
- **Rod hotbar fallback** — when the main-hand rod is gone, scans hotbar slots 0–8 and swaps before counting grace ticks
- **Inventory full guard** — pauses auto-casting with a chat warning when no free slots remain; retries every 5s
- **Per-player toggle persistence** — `autoFishEnabled` saved to `kv` table on change, restored on next join
- **HUD: T/J/F session tally** — `⬡ T:X J:Y F:Z` line shows treasure / junk / fish breakdown for the session
- **HUD: last caught item** — `▸ Last: <name>` line (gold for treasure, gray for junk, green for fish)
- **HUD: next achievement progress** — `↑ Next: N session (X more)` / lifetime / biome hint at bottom of HUD
- **HUD: inventory warning** — red `⚠ Inventory: N slots left` line appears when ≤4 free slots
- **HUD compact mode** — `[I]` cycles full → compact (one-line `⚡ N | MM:SS | X.X/min | T:N J:N`) → hidden
- **Session summary on disable** — pressing `[O]` to stop prints fish count, elapsed time, rate, T/J counts to chat

### Fixed (2026-05-05 QoL pass)
- Catch counter no longer inflates on empty reels — `totalFishCaught++` now gated on confirmed inventory delta
- Catch rate denominator uses `firstCatchTime` (first confirmed catch) instead of session start — idle pre-fishing time no longer deflates the displayed rate
- `detectCatchDelta` + `sendItemAnnouncementIfDetected` merged into single inventory scan per reel (`detectCatchAndAnnounce`), eliminating duplicate iteration

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
