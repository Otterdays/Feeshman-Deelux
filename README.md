# 🎣 Feeshman Deelux - Ultimate Fishing Companion

<div align="center">

![Feeshman Deelux Logo](docs/fishing%20mc%20icon.png)

**The most advanced auto-fishing mod for Minecraft 1.21.4!**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-green.svg)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.16.9-blue.svg)](https://fabricmc.net)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

*Transform your fishing experience with intelligent automation, beautiful visuals, and endless customization!*

</div>

---

## ✨ Current Features

### 🎮 **Core Functionality**
- 🎣 **Smart Toggle System**: Press **O** to enable/disable auto-fishing
- 💬 **Beautiful Chat Messages**: Colorful, styled in-game notifications
- ⏰ **Session Tracking**: Monitor your fishing time and toggle count
- 🎨 **Visual Feedback**: Elegant status indicators and welcome messages
- 🛡️ **Client-Side Only**: Works on any server without installation
- 🌟 **Welcome Message**: Friendly greeting 5 seconds after joining any world

### 🎣 **Advanced Fishing System** ✅ **COMPLETE**
- 🔍 **Intelligent Bite Detection**: Multi-method bobber monitoring system with 5 detection methods
- 🎯 **Auto-Reel & Recast**: Seamless fishing automation with proper right-click simulation
- ⚡ **Human-Like Timing**: Fast, responsive timing (0.15-0.6s reaction time) for smooth gameplay
- 🔔 **Bite Alert Sounds**: Audio notifications for multitasking with configurable volume
- 📊 **Enhanced Statistics**: Detailed catch tracking and milestone notifications

### 🎮 **Quality of Life Features** ✅ **COMPLETE**
- 📊 **HUD Counter**: Live fish count display in top-left corner during fishing
- 🎣 **Item Announcements**: Beautiful messages for every item caught with special rare item alerts
- 🌟 **Lucky Catch Compliments**: Random encouraging messages (5% chance) after catches
- 🪓 **Rod Durability Warning**: Alert when fishing rod has ≤10 uses remaining
- 🗺️ **Biome Catch Tracker**: Tracks fish caught per biome with `/feeshstats biome` command
- 📝 **Statistics Command**: `/feeshstats` shows session and lifetime fishing data
- 🏆 **Achievement Toasts**: Milestone notifications (1st, 10th, 25th, 50th, 100th fish, etc.)
- 🎭 **Fishing Quotes**: Inspirational fishing wisdom on world join and mod enable

## 🎯 Planned Features (45+ Amazing Ideas!)

Our [**Ultimate Feature Roadmap**](suggestions.md) includes incredible features like:

### 🔥 **Quick Wins** (Coming Soon!)
- 💦 **Splash Particles** - Beautiful water effects on catches
- ⏰ **AFK Safety Timer** - Auto-disable after X minutes to prevent endless botting
- 🔁 **Auto-Reequip Spare Rod** - Automatically equip another rod when current breaks

### 🎪 **Fun & Immersion**
- 🎵 **Fishing Rhythm Mode** - Ambient ocean sounds
- 🌈 **Rainbow Rod Trails** - Colorful particle effects
- 🏆 **Achievement Toasts** - Custom milestone notifications
- 🎭 **Fish Personality System** - Each catch gets a unique personality
- 🌙 **Lunar Fishing Bonuses** - Moon phase affects catch rates

### 🛠️ **Quality of Life**
- 🧭 **Fishing Waypoints** - Save favorite fishing spots
- 📦 **Smart Inventory Sorting** - Auto-organize your catches
- 🌡️ **Weather Predictor** - Know which fish to expect
- 🎯 **Precision Cast Assist** - Visual casting guides
- 🔄 **Session Manager** - Save/load fishing setups

### 🚀 **Revolutionary Features**
- 🔮 **Mystical Fish Oracle** - Cryptic fishing predictions
- 🏰 **Underwater Ruins Detector** - Find treasure spots
- 🌪️ **Whirlpool Effects** - Epic visual celebrations
- 🎊 **Confetti Celebrations** - Personal best achievements
- 🗃️ **Fish Museum Builder** - Virtual aquarium showcase

*[View all 45+ features in our suggestions document!](suggestions.md)*

## 🔧 Prerequisites

### For Players
- ☕ **Java 21** or newer ([Download from Adoptium](https://adoptium.net/releases.html))
- 🎮 **Minecraft Java Edition 1.21.4**
- 🧵 **Fabric Loader 0.16.9** (latest for 1.21.4)
- 📦 **Fabric API 0.119.3+1.21.4** or newer

### For Developers & Builders
- ☕ **Java 21** or newer (Required for MC 1.21.4)
- 🎮 **Minecraft Java Edition 1.21.4**
- 🧵 **Fabric Loader 0.16.9**
- 📦 **Fabric API 0.119.3+1.21.4** or newer
- 🏗️ **Gradle 8.8+** (automatically handled by gradlew)
- 🧶 **Fabric Loom 1.8-SNAPSHOT** (build tool for Fabric mods)
- 📚 **Yarn Mappings 1.21.4+build.1** (for proper code compilation)

## 🏗️ Building the Mod

### Quick Setup (Windows)
```powershell
# 1. Clone the repository
git clone https://github.com/yourusername/feeshman-deelux.git
cd feeshman-deelux

# 2. Set Java 21 (if needed)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# 3. Build the mod
.\gradlew clean build

# 4. Find your mod in build/libs/feeshmandeelux-1.0.0.jar
```

### Development Setup
```bash
# Generate IDE sources for development
.\gradlew genSources

# Run in development environment
.\gradlew runClient
```

## 📦 Installation

1. **Install Fabric Loader** for Minecraft 1.21.4 from [FabricMC](https://fabricmc.net/use/)
2. **Download Fabric API** from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. **Place both JARs** in your `%appdata%\.minecraft\mods` folder
4. **Launch Minecraft** with the Fabric profile

## 🎮 Usage

### Getting Started
1. **Launch Minecraft** with Fabric profile
2. **Join any world** (single-player or multiplayer)
3. **Press O** to toggle Feeshman Deelux on/off
4. **Hold a fishing rod** and enjoy the automation!

### 🎯 Pro Tips
- ✅ Works on **any server** (client-side only)
- ✅ Compatible with **enchanted fishing rods**
- ✅ **Toggle off** when you want manual control
- ✅ Watch chat for **beautiful status messages**
- ✅ Perfect for **AFK fishing** (when server rules allow)

### 📊 **New Commands & Features**
- 📝 **`/feeshstats`** - View your fishing statistics (session and lifetime)
- 🗺️ **`/feeshstats biome`** - See your top 3 fishing biomes
- 📊 **HUD Counter** - Live fish count appears in top-left when fishing
- 🎣 **Item Announcements** - Get notified of every catch with special rare item messages
- 🌟 **Lucky Compliments** - Random encouraging messages (5% chance after catches)
- 🪓 **Durability Warnings** - Get alerted when your rod is almost broken
- 🏆 **Achievement Toasts** - Milestone notifications for 1st, 10th, 25th, 50th, 100th+ fish
- 🎭 **Fishing Quotes** - Inspirational fishing wisdom appears randomly

## ⚙️ Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| **Toggle Key** | `O` | Enable/disable auto-fishing |
| **Chat Messages** | `Enabled` | Beautiful status notifications |
| **Session Tracking** | `Enabled` | Monitor usage statistics |
| **Welcome Message** | `5 seconds` | Delay after joining world |

*More configuration options coming with future updates!*

## 🛠️ Technical Details

### Current Implementation Status
| Component | Status | Description |
|-----------|--------|-------------|
| 🎮 **Keybind System** | ✅ **Complete** | O key toggle with chat feedback |
| 💬 **Chat Integration** | ✅ **Complete** | Colorful, formatted messages |
| ⏰ **Session Tracking** | ✅ **Complete** | Toggle count and time tracking |
| 🌟 **Welcome Message** | ✅ **Complete** | 5-second delay after world join |
| 🎣 **Fishing Detection** | ✅ **Complete** | 5-method bobber monitoring with enhanced sensitivity |
| 🤖 **Auto-Reeling** | ✅ **Complete** | Proper right-click simulation |
| ⚡ **Smart Timing** | ✅ **Complete** | Fast, responsive timing (0.15-0.6s reaction) |
| 🔔 **Bite Alert Sound** | ✅ **Complete** | Audio notifications with configurable volume |
| 📊 **HUD Counter** | ✅ **Complete** | Live fish count display during fishing |
| 🎣 **Item Announcements** | ✅ **Complete** | Messages for all caught items with special rare alerts |
| 🌟 **Lucky Compliments** | ✅ **Complete** | Random encouraging messages (5% chance) |
| 🪓 **Rod Durability Warning** | ✅ **Complete** | Alert when rod has ≤10 uses remaining |
| 🗺️ **Biome Tracking** | ✅ **Complete** | Tracks catches per biome with stats command |
| 📝 **Stats Command** | ✅ **Complete** | `/feeshstats` and `/feeshstats biome` commands |
| 🏆 **Achievement Toasts** | ✅ **Complete** | Milestone notifications for fishing achievements |
| 🎭 **Fishing Quotes** | ✅ **Complete** | Inspirational wisdom on world join and enable |

### Project Architecture
```
src/main/java/com/yourname/feeshmandeelux/
├── FeeshmanDeeluxClient.java        # Main mod logic & keybinds
├── [Future] FishingDetector.java    # Bobber monitoring system  
├── [Future] TimingController.java   # Randomization & delays
└── [Future] ConfigManager.java      # Settings & preferences

src/main/resources/
├── fabric.mod.json                  # Mod metadata
├── feeshmandeelux.mixins.json      # Mixin configuration
└── assets/feeshmandeelux/
    ├── icon.png                     # Beautiful fishing icon
    └── lang/en_us.json             # Localization
```

### Version Compatibility
- **Minecraft**: 1.21.4
- **Fabric Loader**: 0.16.9
- **Fabric API**: 0.119.3+1.21.4
- **Yarn Mappings**: 1.21.4+build.1 *(Critical for development!)*
- **Gradle**: 8.8+
- **Java**: 21 (Required for MC 1.21.4)

## 📚 Documentation

### For Players
- 📖 **[Installation Guide](docs/BUILD_GUIDE.md)** - Complete setup instructions
- 🎯 **[Feature Roadmap](suggestions.md)** - 45+ planned features
- 💡 **[Usage Tips](docs/helpfromchat.md)** - Community wisdom

### For Developers  
- 🔧 **[Technical Documentation](docs/techdoc.md)** - Architecture & implementation
- 🏗️ **[Build Guide](docs/BUILD_GUIDE.md)** - Development setup & troubleshooting
- 🧪 **[Testing Strategy](docs/techdoc.md#testing-strategy)** - Quality assurance

## 🚀 Development Roadmap

### Phase 1: Foundation ✅ **COMPLETE**
- [x] Project setup & build system
- [x] Keybind integration
- [x] Chat message system
- [x] Session tracking
- [x] Beautiful mod icon

### Phase 2: Core Fishing ✅ **COMPLETE**
- [x] Advanced bobber detection system
- [x] Smart auto-reel mechanism with proper right-click simulation
- [x] Intelligent auto-recast functionality
- [x] Human-like timing randomization (0.5-1.5s reaction delays)
- [x] Bite alert sound system
- [x] Welcome message system (5 seconds after world join)
- [x] Enhanced bite detection with multiple methods
- [x] Session statistics and milestone notifications

### Phase 3: Enhancement ✅ **CURRENT FOCUS**
- [ ] AFK safety timer
- [ ] Rod durability warnings
- [ ] Configuration system
- [ ] HUD elements

### Phase 4: Polish & Features 🎯 **FUTURE**
- [ ] Sound effects & particles
- [ ] Statistics tracking
- [ ] Achievement system
- [ ] Community features

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### 🐛 **Bug Reports**
Found an issue? [Open an issue](https://github.com/yourusername/feeshman-deelux/issues) with:
- Minecraft version
- Mod version  
- Steps to reproduce
- Expected vs actual behavior

### 💡 **Feature Suggestions**
Have an idea? Check our [suggestions document](suggestions.md) first, then:
- Open a feature request
- Describe the use case
- Explain the expected behavior

### 🔧 **Code Contributions**
Ready to code? Great!
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

### 📖 **Documentation**
Help improve our docs:
- Fix typos or unclear instructions
- Add examples or use cases
- Translate to other languages
- Create video tutorials

## 🏆 Community & Support

### 🌟 **Show Your Support**
- ⭐ **Star this repository** if you find it useful
- 🐛 **Report bugs** to help us improve
- 💡 **Suggest features** for future updates
- 📢 **Share with friends** who love fishing in Minecraft

### 📞 **Get Help**
- 📖 Check our [documentation](docs/)
- 🐛 [Open an issue](https://github.com/yourusername/feeshman-deelux/issues) for bugs
- 💬 Join our community discussions
- 📧 Contact the maintainers

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

**TL;DR**: You can use, modify, and distribute this mod freely. Just keep the license notice! 🎉

---

<div align="center">

**🎣 Happy Fishing! ✨**

*Made with ❤️ for the Minecraft community*

**[⬆️ Back to Top](#-feeshman-deelux---ultimate-fishing-companion)**

</div>