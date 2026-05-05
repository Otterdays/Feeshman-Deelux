<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# SCRATCHPAD

## Status — 2026-05-05

[2026-05-05]: **Doc audit** — `docs/FEATURES_STATUS.md` (25 features incl. SQLite; hooked-entity guard; command list; HUD item marked done), `docs/MODRINTH_DESCRIPTION.md` (Modrinth body), `docs/PROJECT_STATUS.md` + `docs/SUMMARY.md` (build no longer blocked), root `README.md` (Loom 1.16.1, Java 25, commands).

[2026-05-05]: Added static promo site: **`website/index.html`** + **`website/styles.css`** (open in browser or serve folder).

[AMENDED 2026-05-05]: **`build.gradle`** targets **Java 25** (`toolchain` 25 + `release 25`) so the built JAR matches launchers on **Java 25** (fixes Prism `UnsupportedClassVersionError`: mod was **v70**/Java 26 bytecode vs JVM max **v69**).

**`./gradlew build` is green** on MC **26.1.2** with Mojang-named sources. Gameplay Java ported (`AutoFishService`, `FeeshLeaderboard`, `FeeshmanServerCommands`, `FeeshmanDeeluxClient`, config/achievements screens, `FeeshmanNetworking` uses `PayloadTypeRegistry.clientboundPlay()`). **`FeeshmanDatabase`**: `read` / `writeReturning` now take `ThrowingFunction` so DAO `throws Exception` lambdas compile under Java 26. Next: smoke-test client + dedicated server; refresh any stale doc lines below that still claim compile blocked.

## AI RESUME HERE — 2026-05-04 (read this first)

**Goal the user cares about:** SQLite-backed persistent stats at runtime (`config/feeshmandeelux/stats.sqlite`), legacy `feeshman-leaderboard.properties` one-shot import, commands/history/top — **DB layer and wiring largely exist**; the mod does not compile yet on the new toolchain.

### Authoritative toolchain (do not guess from older SCRATCHPAD lines)
| Source | Value |
|--------|--------|
| `gradle.properties` | `minecraft_version=26.1.2`, `loader_version=0.19.2`, `loom_version=1.16.1`, `fabric_api_version=0.148.0+26.1.2`, `mod_version=1.4.0` |
| `build.gradle` | Plugin **`id 'net.fabricmc.fabric-loom'`** (26.1 **unobfuscated**); **no** Yarn `mappings` line. `implementation include("org.xerial:sqlite-jdbc:3.46.1.3")`. Java **`release = 25`** (match Fabric 26.1 example). |
| `[AMENDED 2026-05-05]` | `build.gradle` now uses **`java { toolchain { languageVersion = 26 } }`** (no `release` on compile task). |
| `[AMENDED 2026-05-05 — Prism crash]` | Toolchain back to **25** + explicit **`JavaCompile.options.release = 25`** (ship bytecode **69**, not **70**). |
| `fabric.mod.json` | Should match 26.1 / loader floors (verify if build still fails after source port). |

### What is already implemented (SQLite / persistence)
- Package **`com.yourname.feeshmandeelux.db`**: `FeeshmanDatabase`, `MigrationsV1`, `KvStore`, DAOs (`PlayersDao`, `CatchesDao`, `LeaderboardDao`, `AchievementsDao`, `SessionsDao`, `PlayerStatsDao`), `LegacyImporter`, `AchievementEvaluation`.
- **`FeeshLeaderboard`**: facade over DB (catch recording, sync, sessions, biome breakdown, top lists, etc.).
- **`FeeshmanMod`**: `ServerLifecycleEvents.SERVER_STARTING` → `FeeshmanDatabase.open(.../feeshmandeelux/stats.sqlite)` + `LegacyImporter.runIfNeeded()`; `SERVER_STOPPED` → close.
- **`AutoFishService`**: DB-backed deltas, session rows, fishing-seconds tick, disconnect finalize; achievements sync payload when supported.
- **Networking / commands / client**: `AchievementsSyncPayload`, server commands (`/feeshleaderboard` today/week, `/feeshhistory`, `/feeshtopitems`), client CSV + screen hooks — **all assume game sources compile**.

### Blocker: `./gradlew compileJava` fails (~100 errors, 2026-05-04)
**Cause:** Game code still uses **Yarn-era / intermediate** package names (`net.minecraft.item.*`, `net.minecraft.registry.*`, `net.minecraft.server.command.*`, old client toast packages, etc.). **Minecraft 26.1.2 + `fabric-loom` uses Mojang unobfuscated names** (e.g. `net.minecraft.world.item.ItemStack`, `net.minecraft.core.registries.Registries`, `net.minecraft.server.level.ServerPlayer`, `net.minecraft.commands.Commands` / brigadier patterns — **verify each against merged sources / Fabric example mod 26.1 branch**; do not trust this list blindly).

**Next agent work (ordered):**
1. Set **`JAVA_HOME`** to JDK **25+** for the **Gradle JVM** (Loom enforces this). `java -version` on PATH can lie relative to what Gradle uses.
2. Run **`./gradlew compileJava`** (or `build`) and fix errors **file-by-file** until green. Use Fabric **`fabric-example-mod`** `26.1` branch and local **genSources** output as reference.
3. When compile is green: run client/server smoke test; confirm DB file appears under `config/feeshmandeelux/` and legacy import runs once.
4. Update **SBOM / SUMMARY “Current Version”** table to match `gradle.properties` (many doc lines below this handoff still say 1.21.11 — **stale**).

### Key paths
- DB open: search `FeeshmanDatabase.open` in `FeeshmanMod.java`.
- SQLite file: `config/feeshmandeelux/stats.sqlite` (ensure `FeeshmanDatabase` path matches mod id folder).
- Build troubleshooting: `docs/BUILD_GUIDE.md` (26.1.2 / Loom / JDK section appended 2026-05-04).

### Out-of-scope observations (for triage)
- `docs/README.md` still links to **`../DOCS/`** (capital); this repo’s status docs live in **`docs/`** (lowercase). Fix link when touching that file for another reason.

---

## Last 5 Actions (2026-05-04) [AI handoff]
1. Added **“AI RESUME HERE — 2026-05-04”** (this file): toolchain table, SQLite/file paths, `compileJava` blocker, ordered next steps, `DOCS/` vs `docs/` note
2. **`SUMMARY.md`**: [AMENDED] next-agent steps + Quick Links to SCRATCHPAD/SBOM; flagged stale “Current Version” table
3. **`docs/README.md`**: developer bullet → SCRATCHPAD resume; [AMENDED] path clarification
4. **`CHANGELOG.md`** [Unreleased]: noted handoff doc updates
5. **`./gradlew compileJava`** still fails (~100 errors) — **Mojang 26.1 package migration** is the gating task before SQLite can be validated in-game

## Last 5 Actions (2026-05-04) [docs]
1. Consolidated `docs/SBOM.md` (dependencies + toolchain only); aligned with `build.gradle` / `gradle.properties` / wrapper
2. Updated `SUMMARY.md` (Gradle nightly), `CHANGELOG.md` [Unreleased], `BUILD_GUIDE.md` (wrapper amend + 1.21.11 block)
3. SBOM: removed superseded duplicate tables; `README.md` Gradle badge → 9.6 nightly
4. Root `summary.md`: [AMENDED 2026-05-04] Gradle nightly + pointer to `docs/SBOM.md`

## Last 5 Actions (2026-05-04) [audit + fixes]
1. Code audit: critical bite-detection bug (hooked-entity logic backwards); parseInt crash on corrupt leaderboard
2. Fixed FeeshLeaderboard: parseInt → parseIntSafe; UUID-keyed storage with `.count`/`.name` schema; legacy plain-name fallback
3. Fixed AutoFishService: 
   - Added velocity-based bite detection (Y < -0.04 threshold, 2-tick confirm, grace on entity hook)
   - Flipped hooked-entity logic: skip reel on mob (was reeling into squids)
   - Fixed RNG off-by-one: `nextInt(MAX-MIN+1)` for inclusive range
4. Fixed FeeshmanDeeluxClient: wired showAchievementToastIfMilestone in FishCaught receiver; removed duplicate import; cut welcome banner from 30 lines → 1 (no more spam)
5. FeeshmanServerCommands: BIOME_CATCH_TRACKER keyed by UUID (was display-name); FeeshmanMod gating clarified with comment

## Active Tasks

[AMENDED 2026-05-04]: **Resume priority** = items below this line until build is green; older unchecked items may depend on a working 26.1 JAR.

- [ ] **P0 — 26.1 source port:** `./gradlew compileJava` → fix all Yarn/intermediate → **Mojang unobfuscated** imports (see **“AI RESUME HERE”** above). Gate for everything below.
- [ ] **P1 — SQLite validation:** run client/server; confirm `config/feeshmandeelux/stats.sqlite` created; `LegacyImporter` + commands behave; fix runtime bugs if any.
- [ ] **P2 — Docs:** append **“Current Version (26.1)”** subsection to `SUMMARY.md` when P0 done (do not delete historical 1.21.11 table).

- [x] ~~Code audit~~ — Done; 10 issues identified, safe fixes applied
- [ ] In-game verification (bite detection on non-mob fish, achievements)
- [x] ~~Final docs/version alignment~~ — Done (README, SBOM, PROJECT_STATUS, BUILD_GUIDE, SUMMARY, CHANGELOG) — [AMENDED 2026-05-04]: 26.1 retarget reopened version/docs drift; use `gradle.properties` as truth.

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

## Last 5 Actions (2026-03-19) [Version 1.3.01]
1. Bumped mod_version to 1.3.01 in gradle.properties
2. Build outputs feeshmandeelux-1.3.01.jar
3. Updated SUMMARY, CHANGELOG, FEATURES_STATUS, SBOM graciously

## Last 5 Actions (2026-03-29) [Docs + git]
1. Verified SUMMARY, CHANGELOG, FEATURES_STATUS, SBOM align on mod **1.3.01** / MC **1.21.11**
2. Added PROJECT_STATUS amend note for last doc/git verification (2026-03-29)
3. Confirmed clean working tree; `main` already pushed and matches `origin/main`
