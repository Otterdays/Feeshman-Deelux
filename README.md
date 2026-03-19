<!-- PRESERVATION RULE: Never delete or replace content. Append or annotate only. -->

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.11-62B47A?style=for-the-badge" alt="Minecraft" width="180" height="28"/>
  <img src="https://img.shields.io/badge/Fabric-0.141.3-DFDFDF?style=for-the-badge" alt="Fabric" width="180" height="28"/>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge" alt="Java 21" width="180" height="28"/>
  <img src="https://img.shields.io/badge/Gradle-9.2.1-02303A?style=for-the-badge&logo=gradle" alt="Gradle" width="180" height="28"/>
  <img src="https://img.shields.io/badge/Loom-1.14.1-DFDFDF?style=for-the-badge" alt="Loom" width="180" height="28"/>
  <img src="https://img.shields.io/badge/License-MIT-A31F34?style=for-the-badge" alt="MIT" width="180" height="28"/>
</p>

<p align="center">
  <img src="https://img.shields.io/github/v/release/Otterdays/Feeshman-Deelux?style=for-the-badge&label=Release" alt="Release" width="180" height="28"/>
  <img src="https://img.shields.io/github/stars/Otterdays/Feeshman-Deelux?style=for-the-badge" alt="Stars" width="180" height="28"/>
  <img src="https://img.shields.io/github/issues/Otterdays/Feeshman-Deelux?style=for-the-badge" alt="Issues" width="180" height="28"/>
  <img src="https://img.shields.io/github/last-commit/Otterdays/Feeshman-Deelux?style=for-the-badge" alt="Last commit" width="180" height="28"/>
  <img src="https://img.shields.io/badge/Mod_Menu-Optional-8BC34A?style=for-the-badge" alt="Mod Menu" width="180" height="28"/>
</p>

<h1 align="center">🎣 Feeshman Deelux</h1>
<p align="center">
  <strong>Advanced Auto-Fishing for Minecraft Fabric</strong>
</p>
<p align="center">
  <em>5-method bite detection • Live HUD • Smart safety • Achievement tracking</em>
</p>

---

## ✨ Why Feeshman Deelux?

| Feature | What You Get |
|---------|--------------|
| **Bite Detection** | 5-method system (velocity, position, water validation, bobber dip) — 0.15–0.6s human-like reaction |
| **Automation** | Auto-cast & auto-reel, 2–4s randomized delays, stuck detection, mob avoidance |
| **HUD** | Fish count, session time, rod durability bar, weather, biome, catch rate, status |
| **Safety** | Durability warnings, 3s no-rod grace period, mob collision detection |
| **Stats** | Session + lifetime tracking, biome analytics, leaderboard, achievement toasts |

---

## 🚀 Quick Start

1. Install [Fabric Loader 0.18.1+](https://fabricmc.net/use/installer/)
2. Add [Fabric API 0.141.3+1.21.11](https://modrinth.com/mod/fabric-api)
3. Drop **Feeshman Deelux** into your `mods` folder
4. In-game: equip rod → press **[O]** → fish

**Optional:** [ModMenu](https://modrinth.com/mod/modmenu) for the config screen (bite alert volume, achievements viewer).

---

## 🎮 Controls & Commands

| Action | Input |
|--------|-------|
| Toggle auto-fish | **[O]** |
| Help | `/feeshman` |
| Stats | `/feeshstats` |
| Biome breakdown | `/feeshstats biome` |
| Leaderboard | `/feeshleaderboard` |

---

## 📊 HUD Legend

| Symbol | Meaning |
|--------|---------|
| ◆ | Fish caught (session) |
| ● | Session time |
| ▲ | Rod durability (bar) |
| ♦ | Weather |
| ☀ ☽ | Day/night, moon phase |
| ▼ | Current biome |
| ✦ | Catch rate (fish/min) |
| ◈ | Status (Active, Bite Detected, Recasting…) |
| ★ | Lifetime catches |

---

## 🛠️ Build from Source

```bash
git clone https://github.com/Otterdays/Feeshman-Deelux.git
cd Feeshman-Deelux
./gradlew build
```

**Requires:** Java 21, Gradle 8.12+ (wrapper included)

If `gradlew build` fails with a wrapper error, run:
```bash
gradle wrapper --gradle-version 8.12
```

---

## 📄 License

MIT — see [LICENSE](LICENSE).

---

<p align="center">
  <a href="https://github.com/Otterdays/Feeshman-Deelux">Source</a> •
  <a href="https://github.com/Otterdays/Feeshman-Deelux/issues">Issues</a> •
  <a href="docs/README.md">Docs</a>
</p>
<p align="center">
  <sub>Made with ❤️ for the Minecraft community • Not affiliated with Mojang Studios</sub>
</p>
