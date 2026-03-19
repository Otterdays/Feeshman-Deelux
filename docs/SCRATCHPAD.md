<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SCRATCHPAD

## Active Tasks
- [x] ~~Regenerate Gradle wrapper~~ — Done; build verified
- [x] ~~Upgrade to Minecraft 1.21.11 build~~ — Done; `./gradlew build` succeeds
- [x] ~~Server-first networking: payloads, FishCaught, StatsSync, DurabilityWarning~~ — Done
- [x] ~~Restore original features: item announcements, achievement toasts, biome sync~~ — Done
- [ ] In-game verification (O toggle, HUD, bite sounds, ModMenu, commands)
- [x] ~~Final docs/version alignment~~ — Done (README, SBOM, PROJECT_STATUS, BUILD_GUIDE, SUMMARY, CHANGELOG)

## Blockers
- None

## Last 5 Actions (2025-03-18)
1. Verified Gradle wrapper fix — build succeeds
2. Updated fabric.mod.json: authors "Otterdays", contact URLs from git remote
3. Created DOCS/ARCHITECTURE.md with Mermaid component, sequence, module diagrams
4. Replaced `Random` with `ThreadLocalRandom.current()` for all random calls
5. Removed `another-mod` placeholder from fabric.mod.json suggests

## Last 5 Actions (2026-03-18)
1. Restored item announcements: server inventory diff, ItemAnnouncementPayload, client format (fish/treasure/junk)
2. Added achievement milestone toasts (1, 10, 25, 50, 100 session; 100, 500, 1000 lifetime)
3. Synced biome count to client via FishCaughtPayload + StatsSyncPayload; achievements screen now accurate
4. Updated FEATURES_STATUS, ARCHITECTURE, SCRATCHPAD with server-first status
5. Build verified: `./gradlew build` succeeds

## Last 5 Actions (2026-03-18) [networking]
1. Implemented S2C payloads: FishCaughtPayload, StatsSyncPayload, DurabilityWarningPayload, ItemAnnouncementPayload
2. Added FeeshmanNetworking.registerPayloads(), FeeshmanMod (main entrypoint)
3. AutoFishService: send FishCaughtPayload on reel, StatsSyncPayload on join, DurabilityWarningPayload when rod ≤10 uses
4. FeeshmanDeeluxClient: ClientPlayNetworking receivers for bite sound, HUD stats sync, durability toast
5. Build verified: `./gradlew build` succeeds

## Last 5 Actions (2026-03-19)
1. Removed duplicate Current Version block from DOCS/SUMMARY.md
2. Build verified: `./gradlew build` succeeds on 1.21.11
3. Marked docs alignment complete; in-game verification left for user
4. Upgraded to Minecraft 1.21.11 (Gradle 9.2.1, Fabric Loader 0.18.1, Fabric API 0.141.3+1.21.11, Loom 1.14.1)
5. Fixed compile breaks (bobber position, GUI border, KeyBinding category, moon phase)

## Last 5 Actions (2026-03-18) [README]
1. Sexy up README for GitHub: bigger badges (width/height), more badges
2. Added Gradle, Loom, Release, Stars, Issues, Last commit, Mod Menu badges
3. Added footer links: Source, Issues, Docs
4. Replaced placeholder clone URL with actual repo URL

## Last 5 Actions (2026-03-19) [Mixin fix]
1. Fixed FishingBobberEntityAccessor mixin failure: "No candidates for state:Ljava/lang/Enum"
2. Replaced mixin accessor with public API: bobber.getHookedEntity() != null for HOOKED_IN_ENTITY
3. Removed FishingBobberEntityAccessor mixin, emptied mixins array in feeshmandeelux.mixins.json

## Last 5 Actions (2026-03-19) [Multiplayer server]
1. Added "server" entrypoint (FeeshmanServerMod) for dedicated servers
2. Server-side join message: "Press [O] to toggle auto-fishing • /feeshman for commands"
3. FeeshmanMod skips server registration when EnvType.SERVER (FeeshmanServerMod handles it)
4. FeeshmanServerMod calls FeeshmanNetworking.registerPayloads()
