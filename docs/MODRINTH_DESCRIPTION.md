# Feeshman Deelux

**Advanced auto-fishing for Minecraft 26.1.2 (Fabric).** Press `[O]`, walk away, come back to fish.

---

## Features

### Auto-Fishing
- **Bite detection** — in-water bobber velocity + short confirm window, then 0.15–0.6s human-like reel delay
- **Hooked-entity guard** — if the bobber snags a mob (`getHookedIn`), auto-fish skips bite logic so you do not false-reel
- Randomized 2–4s recast delays
- Stuck detection (~30s idle bobber) with auto-recast
- 3-second rod grace period — won't stop fishing if you swap items briefly

### Live HUD
14 live stats right on your screen: fish count, session T/J/F tally, last caught item, session time, rod durability bar, weather, day/night, biome (color-coded by type), catch rate + quality label, next achievement progress, status, inventory warning, and lifetime total.

Press `[I]` to cycle between **full**, **compact** (single line: `⚡ N | MM:SS | X/min | T:N J:N`), and **hidden**.

### Stats & Competition
- Session and lifetime fish tracking — **SQLite** at `config/feeshmandeelux/stats.sqlite` (legacy leaderboard file imported once if present)
- Biome breakdown (`/feeshstats biome`) — top 3 fishing spots
- **Leaderboard** — `/feeshleaderboard` (all-time top 5), `today`, `week`
- **History & analytics** — `/feeshhistory [n]`, `/feeshtopitems [n]`
- Achievement toasts (session + lifetime milestones, treasure hunter, 5-biome explorer) + in-game achievements screen from ModMenu config

### Audio & Flavor
- Custom bite-alert sound with adjustable volume
- 5% chance of a lucky compliment after each catch
- Random fishing quotes on join and on enable

### Quality of Life
- **ModMenu support** — config (bite alert volume, auto-fish toggle, open achievements screen)
- **Rod hotbar fallback** — auto-swaps to the next fishing rod in your hotbar when the current one breaks
- **Inventory full guard** — pauses fishing with a warning when your inventory is full; resumes automatically when space frees up
- **Toggle persistence** — auto-fish on/off preference saved per-player; survives server restarts
- **Session summary** — pressing `[O]` to stop prints fish count, time, rate, and T/J breakdown to chat
- Colored catch announcements: fish in green, treasure in gold, junk in gray
- Tag-based item detection (`#minecraft:fishes`, custom treasure/junk tags) — modpack friendly
- Dedicated server entrypoint — works on multiplayer servers out of the box

---

## Commands

| Command | What it does |
|---------|-------------|
| `/feeshman` | Help overview |
| `/feeshstats` | Your session + lifetime stats |
| `/feeshstats biome` | Top 3 biomes by catches |
| `/feeshleaderboard` | All-time top 5 |
| `/feeshleaderboard today` | Top 5 since UTC midnight |
| `/feeshleaderboard week` | Top 5 for the last 7 days |
| `/feeshhistory [n]` | Last *n* catches (default 10) |
| `/feeshtopitems [n]` | Top *n* caught items (default 10) |

---

## Requirements

- **Minecraft** 26.1.2
- **Fabric Loader** 0.19.2+
- **Fabric API** 0.148.0+26.1.2
- **ModMenu** *(optional)* — for the config screen

---

## Source & License

[github.com/Otterdays/Feeshman-Deelux](https://github.com/Otterdays/Feeshman-Deelux) · MIT
