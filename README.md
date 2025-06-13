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
- 🎣 **Smart Toggle System**: Press **F** to enable/disable auto-fishing
- 💬 **Beautiful Chat Messages**: Colorful, styled in-game notifications
- ⏰ **Session Tracking**: Monitor your fishing time and toggle count
- 🎨 **Visual Feedback**: Elegant status indicators and welcome messages
- 🛡️ **Client-Side Only**: Works on any server without installation

### 🚧 **In Development**
- 🔍 **Intelligent Bite Detection**: Advanced bobber monitoring system
- 🎯 **Auto-Reel & Recast**: Seamless fishing automation
- ⚡ **Human-Like Timing**: Randomized delays to avoid detection
- 📊 **Enhanced Statistics**: Detailed catch tracking and analytics

## 🎯 Planned Features (45+ Amazing Ideas!)

Our [**Ultimate Feature Roadmap**](suggestions.md) includes incredible features like:

### 🔥 **Quick Wins** (Coming Soon!)
- 🔔 **Bite Alert Sounds** - Audio notifications for multitasking
- 💦 **Splash Particles** - Beautiful water effects on catches
- 🌟 **Lucky Catch Celebrations** - Random compliments and effects
- 📊 **HUD Counter** - Live fish count display
- 🪓 **Rod Durability Warnings** - Never break a rod again!

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

- ☕ **Java 21** or newer ([Download from Adoptium](https://adoptium.net/releases.html))
- 🎮 **Minecraft Java Edition 1.21.4**
- 🧵 **Fabric Loader 0.16.9** (latest for 1.21.4)
- 📦 **Fabric API 0.119.3+1.21.4** or newer

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

## ⚙️ Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| **Toggle Key** | `O` | Enable/disable auto-fishing |
| **Chat Messages** | `Enabled` | Beautiful status notifications |
| **Session Tracking** | `Enabled` | Monitor usage statistics |
| **Welcome Message** | `10 seconds` | Delay after joining world |

*More configuration options coming with future updates!*

## 🛠️ Technical Details

### Current Implementation Status
| Component | Status | Description |
|-----------|--------|-------------|
| 🎮 **Keybind System** | ✅ **Complete** | O key toggle with chat feedback |
| 💬 **Chat Integration** | ✅ **Complete** | Colorful, formatted messages |
| ⏰ **Session Tracking** | ✅ **Complete** | Toggle count and time tracking |
| 🎣 **Fishing Detection** | 🚧 **In Progress** | Bobber monitoring system |
| 🤖 **Auto-Reeling** | 🚧 **In Progress** | Automated catch handling |
| ⚡ **Smart Timing** | 📋 **Planned** | Human-like randomization |

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
- [x] Smart auto-reel mechanism  
- [x] Intelligent auto-recast functionality
- [x] Human-like timing randomization
- [x] Bite alert sound system

### Phase 3: Enhancement 🚧 **CURRENT FOCUS**
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