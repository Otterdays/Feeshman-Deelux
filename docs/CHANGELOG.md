<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# CHANGELOG

All notable changes to Feeshman Deelux. [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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
