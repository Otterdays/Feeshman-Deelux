<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

# Catch Detection / Stats Investigation

## [AMENDED 2026-05-13]: Fox snag follow-up

- User repro: hooked a fox jumping in water and the loop did not retract/recast promptly.
- Follow-up adjustment: lowered the hooked-entity confirmation window in `AutoFishService` from ~1 second to 2 ticks so live mob snags are reeled almost immediately instead of requiring a long uninterrupted hooked state.
- Local verification: `ReadLints` clean for `AutoFishService.java`; `./gradlew build` still passes.
- Runtime check still needed: confirm a mob snag now reels fast enough even when the hooked entity is moving erratically.

## [AMENDED 2026-05-13]: Follow-up fixes completed

- Server/client auto-fish state sync now uses `StatsSyncPayload.autoFishEnabled`, and the client `[O]` key now sends `/feeshman toggle` instead of guessing the next state locally.
- Bite alert volume is now taken from `FeeshmanConfig.getBiteAlertVolume()` when the fish-caught payload plays the alert sound.
- Hook/entity snags now recover automatically after a short timeout instead of leaving the auto-fish loop parked forever on `bobber.getHookedIn() != null`.
- Local verification: `./gradlew build` is still green after these follow-up fixes.
- Remaining runtime checks: confirm join-time HUD state matches the real server toggle, bite alert volume slider audibly changes the alert sound, and hooked-entity snags recover cleanly without false stat writes.

## [AMENDED 2026-05-13]: Fix implemented

- `AutoFishService.reelIn(...)` no longer relies on a same-method before/after inventory snapshot to identify the caught item.
- The server now arms a short pending catch-detection window after reeling and re-checks inventory diffs for up to 8 ticks.
- Confirmed catches now go through a shared record path so item announcements, session/lifetime stats, SQLite rows, and `FishCaughtPayload` stay tied to the delayed item detection result.
- Local verification: `./gradlew build` succeeds after the change.
- Remaining runtime check: confirm in-game on 26.1.2 that caught item names, treasure/junk tagging, `/feeshhistory`, and HUD/session counters now update together again.

## Status

- Date: 2026-05-13
- State: Mitigated in code, pending in-game verification
- Scope: Auto-fish catch detection as the source of truth for stats, DB rows, HUD sync, and achievements

## Problem Statement

User report: catch detection appears to be broken for stats.

Current code review points to a high-risk coupling in the server pipeline: stats only advance when the inventory-diff catch detector succeeds during `AutoFishService.reelIn(...)`.

## What The Current Flow Does

1. `AutoFishService.reelIn(...)` snapshots inventory counts before reeling.
2. It calls `player.gameMode.useItem(...)` to reel the hook in.
3. It snapshots inventory counts again immediately after that call.
4. It runs `detectCatchAndAnnounce(...)` to infer a caught item from the before/after diff.
5. It only increments `state.totalFishCaught`, writes SQLite data through `FeeshLeaderboard.recordCatch(...)`, and sends `FishCaughtPayload` if `delta.itemId() != null`.

## Key Code-Level Findings

### 1. Stats are gated behind item detection

`AutoFishService.reelIn(...)` treats "identified inventory delta" as the only success signal:

- No detected item delta -> no session increment
- No session increment -> no lifetime write
- No lifetime write -> no catch row, no biome update, no achievement evaluation
- No `FishCaughtPayload` -> client HUD/session summary stay stale

This makes item classification a single point of failure for the entire stats system.

### 2. There is no fallback stats path

`CatchDelta.unknown()` exists, and `FeeshLeaderboard.recordCatch(...)` can accept a `CatchDelta`, but the calling code never records an "unknown but real" catch. If detection returns `itemId = null`, the pipeline exits with no stats update at all.

### 3. The timing window is fragile

The current detection strategy depends on an immediate same-method before/after inventory diff around `useItem(...)`.

Likely failure modes:

- the caught stack is not visible to the inventory scan yet when the second snapshot runs
- stack merge timing differs from what this code assumes
- future API/runtime changes preserve the catch but not the exact inventory-diff timing this method relies on

This needs live verification, but the structure is brittle even before repro.

### 4. Client-facing stats rely on the same success gate

The client HUD does not independently derive catch counts. It mainly trusts server sync:

- `FishCaughtPayload` drives session/lifetime count updates
- item announcement payloads drive last-item and treasure/junk session tally
- catch-rate display uses confirmed catch count

If the server misses one catch, the HUD, session summary, and DB all drift together.

## Impacted Features

- `/feeshstats`
- `/feeshleaderboard`
- `/feeshhistory`
- `/feeshtopitems`
- SQLite `catches` rows
- biome breakdown / biome distinct count
- achievement unlock evaluation
- client HUD fish count
- client HUD treasure/junk counters
- client "last caught item"
- client catch-rate denominator

## Secondary Risk

`AutoFishService.tickPlayer(...)` returns early when `bobber.getHookedIn() != null`.

That is separate from the stats gate above, but it is relevant because a snagged hook can leave the loop sitting on an active bobber without a reel/recast recovery path. If players hit that state, stats will also appear frozen because no new successful reel flow completes.

## Recommended Fix Direction

### Option A: Decouple counting from item classification

Treat "successful reel" and "identified item" as separate concepts.

Suggested rule:

- confirmed catch/retrieve success -> increment session/lifetime stats
- identified item -> enrich the catch row, announcements, treasure/junk flags
- unidentified catch -> record an "unknown" catch row instead of dropping the event

This is the safest way to prevent silent stat loss.

### Option B: Move item detection to a deferred follow-up step

Instead of taking the second inventory snapshot immediately after `useItem(...)`, defer catch classification by 1-3 ticks and compare against the pre-reel snapshot then.

That would reduce false negatives if inventory mutation lands after the current method returns.

### Option C: Add temporary diagnostics before gameplay changes

Add short-lived logging around:

- reel attempt start
- whether a bobber existed
- whether an inventory delta was found
- whether stats were recorded
- item id chosen (if any)

This would confirm whether the real bug is timing, classification, or loop control.

## Suggested Implementation Order

1. Add temporary diagnostics around `reelIn(...)` and `detectCatchAndAnnounce(...)`.
2. Reproduce in-game with a clean SQLite file.
3. Decouple stat increment from item classification.
4. Add a deferred catch-classification fallback if immediate diff still fails.
5. Smoke-test `/feeshstats`, `/feeshhistory`, `/feeshtopitems`, achievements, and HUD tallies together.

## Verification Checklist

- session fish count increments on every real catch
- lifetime count increments across reconnects
- unknown classification still records a catch instead of dropping it
- biome stats still populate when item detection works
- treasure/junk counters remain correct when tags resolve
- HUD count matches SQLite count after a fishing session
- `/feeshhistory` shows rows for catches from the same session

## Short Takeaway

The likely root problem is not "stats logic" in isolation. The real issue is that the stats system is currently downstream of a brittle inventory-diff detector with no fallback path. When that detector misses, the entire stats pipeline misses.
