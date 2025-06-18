# 🎣 Feeshman Deelux - Advanced Auto-Fishing Mod

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.6-brightgreen.svg)](https://minecraft.net)
[![Fabric API](https://img.shields.io/badge/Fabric%20API-0.127.0+1.21.6-blue.svg)](https://fabricmc.net)
[![Fabric Loom](https://img.shields.io/badge/Fabric%20Loom-1.10.5-purple.svg)](https://github.com/FabricMC/fabric-loom)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)](#)

> **The most sophisticated auto-fishing mod for Minecraft Fabric with advanced bite detection, clean HUD display, and intelligent safety features.**

## ✨ Key Features

### 🎯 **Advanced Bite Detection**
- **5-Method Detection System**: Velocity analysis, downward motion, position tracking, water validation, and bobber dip detection
- **High Accuracy**: Detects fish bites with 0.15-0.6 second human-like reaction time
- **False Positive Prevention**: Smart cooldowns and multi-layered validation

### 🎮 **Smart Automation**
- **Auto-Cast & Auto-Reel**: Fully automated fishing with randomized timing (2-4 second delays)
- **Stuck Detection**: 30-second threshold with intelligent water state analysis
- **Mob Avoidance**: Automatically detects and avoids squids, drowned, and other interfering mobs

### 🎨 **Live HUD Display**
- **Real-time Statistics**: Fish count, session time, catch rate, and rod durability
- **Environmental Info**: Weather conditions, day/night cycle, moon phases, and current biome  
- **Enhanced Unicode Interface**: Carefully selected symbols (◆ ● ▲ ♦ ☀ ☽ ▼ ✦ ◈ ★) for improved visual appeal
- **Cross-platform Compatibility**: Tested Unicode characters that render consistently across all systems

### 🛡️ **Safety Features**
- **Rod Durability Warnings**: Alerts when fishing rod has low durability (≤10 uses)
- **Smart Inventory Tracking**: Announces new catches with color-coded messages
- **Emergency Safeguards**: Multiple failsafes prevent getting stuck or losing equipment

### 🔊 **Audio & Visual**
- **Bite Alert Sounds**: Customizable volume bite detection alerts
- **Achievement Toasts**: Milestone notifications for fishing progress
- **Colorful Chat Messages**: Enhanced fishing announcements and compliments

## 🛠️ Technical Specifications

### **Current Version (June 2025)**
- **Minecraft**: 1.21.6
- **Fabric Loader**: 0.16.14
- **Fabric API**: 0.127.0+1.21.6
- **Fabric Loom**: 1.10.5  
- **Gradle**: 8.12
- **Java**: 21

## 🚀 Installation

1. Install **[Fabric Loader 0.16.14+](https://fabricmc.net/use/installer/)**
2. Download **[Fabric API 0.127.0+1.21.6](https://modrinth.com/mod/fabric-api)**
3. Download **Feeshman Deelux** from releases
4. Place both mods in your mods folder

### Dependencies
- **Required**: Fabric API 0.127.0+1.21.6
- **Optional**: [ModMenu](https://modrinth.com/mod/modmenu) for configuration screen

## 🎮 Usage

### Controls
- **[O] Key** - Toggle auto-fishing on/off
- **ModMenu** - Access configuration screen for sound settings

### Commands
- `/feeshman` - Show help and command list
- `/feeshstats` - View fishing statistics and session data
- `/feeshstats biome` - View biome-specific catch breakdown  
- `/feeshleaderboard` - View fishing leaderboard

### Getting Started
1. Equip a fishing rod in your main hand
2. Press **[O]** to enable auto-fishing
3. Cast your rod manually (or let the mod auto-cast)
4. Watch the live HUD for real-time statistics
5. Press **[O]** again to disable

## 📊 HUD Information Display

When active, the mod shows:
- **◆ Fish Count**: Total catches in current session
- **● Session Time**: Duration of current fishing session  
- **▲ Rod Durability**: Remaining uses and percentage with visual bar
- **♦ Weather**: Current weather conditions (Clear/Rainy/Thunder)
- **☀ ☽ Time**: Day/night cycle with moon phases
- **▼ Biome**: Current fishing location with color coding
- **✦ Catch Rate**: Fish per minute with efficiency rating
- **◈ Status**: Current mod activity (Active, Bite Detected, Recasting, etc.)
- **★ Lifetime**: Total catches across all sessions

## 🔧 Advanced Features

### **Bite Detection Methods**
1. **Velocity Analysis** - Monitors bobber movement patterns
2. **Downward Motion** - Detects fish pulling bobber underwater  
3. **Position Tracking** - Analyzes sudden position changes
4. **Water Validation** - Ensures bobber is properly submerged
5. **Bobber Dip Detection** - Instant response to Y-axis drops

### **Safety Systems**
- **Mob Collision Detection** - Avoids squids, drowned, zombies, skeletons
- **Intelligent Stuck Detection** - 30s threshold with multiple validation layers
- **Human-like Timing** - 0.15-0.6s reaction delays with randomization
- **Durability Monitoring** - Warns at 10 or fewer rod uses remaining

## 🏆 Statistics & Achievements

- **Session Tracking**: Fish caught, time elapsed, catch rate analysis
- **Lifetime Statistics**: Total catches across all sessions  
- **Biome Analytics**: Track catches by biome type
- **Achievement System**: Milestone toasts for 1, 10, 25, 50, 100+ fish
- **Leaderboard**: Persistent tracking of top fishing sessions

## 📈 Recent Updates

### **v1.21.6 - Enhanced Unicode Interface (June 2025)**
- ✅ **Carefully selected Unicode symbols** - Added compatible symbols (◆ ● ▲ ♦ ☀ ☽ ▼ ✦ ◈ ★) for better visual appeal
- ✅ **Cross-platform compatibility** - Tested Unicode characters that render consistently across systems
- ✅ **Modern HUD design** - Enhanced visual hierarchy with meaningful symbols
- ✅ **HUD API preparation** - Ready for migration to HudElementRegistry when available in Fabric API

### **v1.21.6 - Production Ready (June 2025)**  
- ✅ **Full Minecraft 1.21.6 support** with latest Fabric toolchain
- ✅ **Optimized build system** - Loom 1.10.5, Gradle 8.12, Java 21
- ✅ **Enhanced HUD system** - Modern transparent design with improved spacing
- ✅ **Advanced bite detection** - 5-method detection system with human-like timing

## 🔧 Development

### Building from Source
```bash
git clone <repository-url>
cd feeshman-deelux/Feeshman-Deelux
./gradlew build
```

### Development Environment
- **Java 21** (required)
- **Gradle 8.12+** (wrapper included)
- **Fabric Loom 1.10.5**

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

---

**Made with ❤️ for the Minecraft community** • **Not affiliated with Mojang Studios**