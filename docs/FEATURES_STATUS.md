# Feeshman Deelux — Features

> **Version**: 1.4.0 | **Minecraft**: 26.1.2 | **Fabric Loader**: 0.19.2+ | **Fabric API**: 0.148.0+26.1.2

---

## Implemented (25 / 45)

### Core Auto-Fishing
| # | Feature | Notes |
|---|---------|-------|
| 1 | **Toggle** | `[O]` key; instant on/off with chat feedback |
| 2 | **Multi-signal bite detection** | In-water velocity dip (`deltaMovement.y`), 2-tick confirm, human-like reel delay 0.15–0.6s (3–12 ticks) |
| 3 | **Auto-Reel & Recast** | Server-side reel + 2–4s randomized recast delay |
| 4 | **Stuck Detection** | ~30s no meaningful bobber movement → auto-recast |
| 5 | **3-Second Grace Period** | ~3s (60 ticks) before disabling when rod leaves hand |
| 6 | **Hooked-entity guard** | While the bobber has `getHookedIn() != null`, bite logic is skipped (avoids false reels when snagged on a mob) |
| 7 | **Rod Durability Warnings** | Alerts when durability runs low |

### HUD & Display
| # | Feature | Notes |
|---|---------|-------|
| 8 | **Live HUD (9 elements)** | Fish count, session time, rod durability bar, weather, day/night + moon phase, biome, catch rate, status |
| 9 | **Item Catch Announcements** | Color-coded: fish §a green, treasure §6 gold, junk §7 gray |

### Stats & Progression
| # | Feature | Notes |
|---|---------|-------|
| 10 | **Session & Lifetime Stats** | Fish count, session time, catch rate |
| 11 | **Biome Tracking** | Catch counts per biome; top 3 via `/feeshstats biome` |
| 12 | **Leaderboard** | SQLite-backed; batch flush ~30s + on disconnect; `/feeshleaderboard` all-time, `today`, `week` |
| 13 | **Achievement Toasts** | Session milestones + lifetime 100/500/1000; plus Treasure Hunter & World Explorer (5 biomes) |
| 14 | **Achievements Screen** | ModMenu → config → “View Achievements”; syncs unlocks from server |

### Audio & Flavor
| # | Feature | Notes |
|---|---------|-------|
| 15 | **Bite Alert Sound** | Custom `bite_alert.ogg`; configurable volume |
| 16 | **Lucky Compliments** | 5% chance per catch; 10-message pool |
| 17 | **Fishing Quotes** | 30% on world join, 20% on enable; 10-quote pool |
| 18 | **Welcome Message** | Feature overview with 5s delay on first join |

### Technical
| # | Feature | Notes |
|---|---------|-------|
| 19 | **ModMenu Integration** | Config: bite volume, auto-fish toggle, button to achievements screen |
| 20 | **Persistent Config** | Auto-save/load; survives restarts |
| 21 | **Tag-Based Item Detection** | `#minecraft:fishes`, `feeshmandeelux:treasure`, `feeshmandeelux:junk` |
| 22 | **Server-First Architecture** | AutoFishService runs server-side; client handles UX only |
| 23 | **Dedicated Server Entrypoint** | `FeeshmanServerMod` for multiplayer; join-message on connect |
| 24 | **Professional Logging** | Log4j2 throughout; no System.out |
| 25 | **SQLite persistence** | Runtime DB at `config/feeshmandeelux/stats.sqlite` (catches, leaderboard, achievements, sessions); optional legacy properties import |

---

## Commands

| Command | Description |
|---------|-------------|
| `/feeshman` | Help & feature overview |
| `/feeshstats` | Session + lifetime stats |
| `/feeshstats biome` | Top 3 biomes by catch count |
| `/feeshleaderboard` | All-time top 5 |
| `/feeshleaderboard today` | Top 5 since UTC midnight |
| `/feeshleaderboard week` | Top 5 for the last 7 days |
| `/feeshhistory [n]` | Last *n* catches (default 10) |
| `/feeshtopitems [n]` | Top *n* caught items (default 10) |

---

## Planned

### High Priority
- **AFK Safety Timer** — auto-disable after configurable duration (default 60 min)
- **Splash Particles** — visual water particles on catch
- **Auto-Reequip** — swap to next rod in inventory when current breaks
- ~~**HUD API Migration**~~ — **done:** client uses `HudElementRegistry` (26.1 HUD API)

### Medium Priority
- Discord webhook for rare catches
- Fishing waypoints (save/navigate favorite spots)
- Weather-based fish type hints
- Smart inventory sort (auto-organize catches)

### Future
- Fish Museum / virtual aquarium
- Multiplayer fishing tournaments
- Seasonal events
- Advanced analytics dashboard
