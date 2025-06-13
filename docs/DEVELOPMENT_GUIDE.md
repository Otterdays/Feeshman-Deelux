# 🛠️ Feeshman Deelux - Development Guide

> **For Developers, Contributors, and Curious Minds**  
> *Everything you need to know to contribute to the ultimate fishing mod!*

---

## 🎯 **Quick Start for Developers**

### Prerequisites Checklist
- [ ] ☕ **Java 21** installed and configured
- [ ] 🎮 **Minecraft 1.21.4** (for testing)
- [ ] 🧵 **Fabric Loader 0.16.9** 
- [ ] 📦 **Fabric API 0.119.3+1.21.4**
- [ ] 🔧 **IDE** (IntelliJ IDEA recommended)
- [ ] 📚 **Git** for version control

### 🚀 **Setup in 5 Minutes**
```bash
# 1. Clone the repository
git clone https://github.com/yourusername/feeshman-deelux.git
cd feeshman-deelux

# 2. Set Java 21 (Windows)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# 3. Generate development sources
.\gradlew genSources

# 4. Build and test
.\gradlew clean build

# 5. Run in development
.\gradlew runClient
```

**🎉 You're ready to code!**

---

## 📚 **Understanding the Codebase**

### 🏗️ **Architecture Overview**

```
Feeshman Deelux Architecture
├── 🎮 Client Entry Point (FeeshmanDeeluxClient)
├── 🎣 Fishing Logic (Future: FishingDetector)
├── ⏰ Timing System (Future: TimingController)
├── ⚙️ Configuration (Future: ConfigManager)
├── 📊 Statistics (Future: StatisticsTracker)
└── 🎨 UI Components (Future: HudRenderer)
```

### 📁 **Current File Structure**
```
src/main/java/com/yourname/feeshmandeelux/
├── FeeshmanDeeluxClient.java        # 🎮 Main mod entry point
└── [Future Classes]                 # 📋 Planned components

src/main/resources/
├── fabric.mod.json                  # 📦 Mod metadata
├── feeshmandeelux.mixins.json      # 🔧 Mixin configuration
└── assets/feeshmandeelux/
    ├── icon.png                     # 🎨 Mod icon
    └── lang/en_us.json             # 🌐 Localization

docs/                                # 📖 Documentation
├── BUILD_GUIDE.md                   # 🏗️ Build instructions
├── techdoc.md                       # 🔧 Technical details
├── FEATURES_STATUS.md               # 📊 Feature tracking
└── DEVELOPMENT_GUIDE.md             # 🛠️ This file!
```

### 🎯 **Core Components Deep Dive**

#### 1. 🎮 **FeeshmanDeeluxClient.java** (Main Entry Point)
```java
public class FeeshmanDeeluxClient implements ClientModInitializer {
    // Current responsibilities:
    // ✅ Keybind registration and handling
    // ✅ Toggle state management
    // ✅ Chat message system
    // ✅ Basic tick event handling
    // 🚧 Placeholder fishing logic
    
    // Key methods:
    // - onInitializeClient() - Mod initialization
    // - tick event handler - Main game loop integration
}
```

**Current Status**: 69 lines, functional but needs expansion

#### 2. 🎣 **FishingDetector.java** (Planned)
```java
// Future class for bobber monitoring
public class FishingDetector {
    // Planned responsibilities:
    // - Monitor FishingBobberEntity state
    // - Detect bite conditions (velocity, particles, sounds)
    // - Provide bite notifications to main system
    // - Handle edge cases (line breaks, lag, etc.)
}
```

#### 3. ⏰ **TimingController.java** (Planned)
```java
// Future class for human-like timing
public class TimingController {
    // Planned responsibilities:
    // - Generate randomized reaction delays
    // - Implement anti-pattern behavior
    // - Provide configurable timing presets
    // - Simulate human-like variations
}
```

---

## 🔧 **Development Workflow**

### 🌟 **Contributing Process**

1. **🍴 Fork & Clone**
   ```bash
   git fork https://github.com/original/feeshman-deelux.git
   git clone https://github.com/yourusername/feeshman-deelux.git
   ```

2. **🌿 Create Feature Branch**
   ```bash
   git checkout -b feature/bobber-detection
   # or
   git checkout -b fix/timing-issue
   ```

3. **💻 Develop & Test**
   ```bash
   # Make your changes
   .\gradlew runClient  # Test in development
   .\gradlew build      # Ensure it builds
   ```

4. **📝 Document Changes**
   - Update relevant documentation
   - Add code comments
   - Update FEATURES_STATUS.md if needed

5. **🚀 Submit Pull Request**
   - Clear title and description
   - Reference related issues
   - Include testing notes

### 🧪 **Testing Strategy**

#### **Manual Testing Checklist**
- [ ] Mod loads without errors
- [ ] Keybind works (O key toggle)
- [ ] Chat messages appear correctly
- [ ] No console errors during gameplay
- [ ] Compatible with multiplayer servers
- [ ] Performance impact is minimal

#### **Development Testing**
```bash
# Run in development environment
.\gradlew runClient

# Build and test jar
.\gradlew build
# Test the jar in .minecraft/mods folder
```

#### **Future: Automated Testing**
```java
// Planned unit tests
@Test
public void testBobberDetection() {
    // Test bite detection accuracy
}

@Test
public void testTimingRandomization() {
    // Test human-like timing patterns
}
```

---

## 🎯 **Implementation Priorities**

### 🔥 **Phase 2: Core Fishing (CURRENT FOCUS)**

#### **Priority 1: Bobber Detection System**
**Goal**: Implement reliable fish bite detection

```java
// Implementation approach:
public class FishingDetector {
    private FishingBobberEntity currentBobber;
    private Vec3d lastBobberPos;
    private Vec3d lastBobberVelocity;
    
    public boolean detectBite() {
        // Method 1: Velocity-based detection
        if (bobberVelocityChanged()) {
            return true;
        }
        
        // Method 2: Particle-based detection (backup)
        if (bubbleParticlesDetected()) {
            return true;
        }
        
        // Method 3: Sound-based detection (backup)
        if (splashSoundDetected()) {
            return true;
        }
        
        return false;
    }
}
```

**Key Challenges**:
- Accessing FishingBobberEntity reliably
- Distinguishing bite from normal bobber movement
- Handling network lag and edge cases
- Avoiding false positives

**Implementation Steps**:
1. Create FishingDetector class
2. Implement bobber entity tracking
3. Add velocity-based bite detection
4. Create particle/sound backup detection
5. Add comprehensive error handling
6. Test extensively in various conditions

#### **Priority 2: Timing & Randomization**
**Goal**: Add human-like behavior patterns

```java
public class TimingController {
    private Random random = new Random();
    private long lastActionTime;
    
    public int getReactionDelay() {
        // Base delay: 100-300ms
        int baseDelay = 100 + random.nextInt(200);
        
        // Add human-like variance
        int variance = random.nextInt(100) - 50;
        
        // Apply fatigue simulation (slower over time)
        int fatigueDelay = calculateFatigueDelay();
        
        return Math.max(50, baseDelay + variance + fatigueDelay);
    }
}
```

**Implementation Steps**:
1. Create TimingController class
2. Implement basic randomization
3. Add configurable timing presets
4. Create anti-pattern behaviors
5. Add fatigue simulation
6. Test timing patterns for naturalness

### ⭐ **Phase 3: Enhancement (NEXT)**

#### **Priority 3: Configuration System**
**Goal**: User-friendly settings management

```java
public class ConfigManager {
    private JsonObject config;
    
    // Settings categories:
    // - Timing preferences
    // - Audio/visual options
    // - Safety features
    // - Keybind customization
}
```

#### **Priority 4: Audio & Visual Effects**
**Goal**: Enhanced user experience

```java
public class EffectsManager {
    // Bite alert sounds
    // Splash particles
    // HUD elements
    // Achievement notifications
}
```

---

## 🔍 **Code Standards & Best Practices**

### 📝 **Coding Guidelines**

#### **Java Style**
```java
// ✅ Good: Clear, descriptive names
public class FishingDetector {
    private boolean isBiteDetected;
    private long lastBiteTime;
    
    public boolean detectFishBite() {
        // Implementation
    }
}

// ❌ Avoid: Unclear, abbreviated names
public class FD {
    private boolean bd;
    private long lbt;
    
    public boolean det() {
        // Implementation
    }
}
```

#### **Documentation Standards**
```java
/**
 * Detects when a fish bites the fishing line by monitoring bobber behavior.
 * 
 * This class uses multiple detection methods:
 * 1. Velocity-based detection (primary)
 * 2. Particle effect monitoring (backup)
 * 3. Sound event detection (backup)
 * 
 * @author YourName
 * @since 1.0.0
 */
public class FishingDetector {
    
    /**
     * Checks if a fish bite has occurred.
     * 
     * @return true if bite detected, false otherwise
     * @throws IllegalStateException if no bobber is currently active
     */
    public boolean detectBite() {
        // Implementation
    }
}
```

#### **Error Handling**
```java
// ✅ Good: Proper error handling
public boolean detectBite() {
    try {
        if (currentBobber == null) {
            return false;
        }
        
        return checkBobberVelocity();
    } catch (Exception e) {
        LOGGER.warn("Error detecting fish bite: " + e.getMessage());
        return false;
    }
}

// ❌ Avoid: No error handling
public boolean detectBite() {
    return checkBobberVelocity(); // Could throw NPE
}
```

### 🎯 **Performance Guidelines**

#### **Efficient Tick Handling**
```java
// ✅ Good: Limit expensive operations
private int tickCounter = 0;

public void onTick() {
    tickCounter++;
    
    // Only check every 5 ticks (4 times per second)
    if (tickCounter % 5 == 0) {
        performExpensiveCheck();
    }
    
    // Always do lightweight checks
    performLightweightCheck();
}

// ❌ Avoid: Expensive operations every tick
public void onTick() {
    performExpensiveCheck(); // 20 times per second!
}
```

#### **Memory Management**
```java
// ✅ Good: Reuse objects
private final List<Particle> particleCache = new ArrayList<>();

// ❌ Avoid: Creating new objects frequently
public void spawnParticles() {
    List<Particle> particles = new ArrayList<>(); // New object every call
}
```

---

## 🧪 **Testing & Debugging**

### 🔍 **Debugging Tools**

#### **Logging System**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeeshmanDeeluxClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("feeshmandeelux");
    
    public void debugBobberState() {
        LOGGER.info("Bobber position: {}", bobber.getPos());
        LOGGER.debug("Bobber velocity: {}", bobber.getVelocity());
    }
}
```

#### **In-Game Debug Commands**
```java
// Future: Debug commands for testing
// /feesh debug bobber - Show bobber information
// /feesh debug timing - Show timing statistics
// /feesh debug particles - Visualize detection areas
```

### 🎮 **Testing Scenarios**

#### **Basic Functionality Tests**
1. **Toggle Test**: O key enables/disables mod
2. **Chat Test**: Messages appear with correct formatting
3. **Persistence Test**: State maintained across world changes
4. **Multiplayer Test**: Works on servers without issues

#### **Fishing Logic Tests** (Future)
1. **Bite Detection Test**: Accurately detects fish bites
2. **False Positive Test**: Doesn't trigger on normal movement
3. **Lag Test**: Works with network latency
4. **Edge Case Test**: Handles broken lines, full inventory, etc.

#### **Performance Tests**
1. **CPU Usage**: <1% additional CPU load
2. **Memory Usage**: <5MB additional memory
3. **Network Impact**: No additional network traffic
4. **Compatibility**: Works with other mods

---

## 🎨 **UI/UX Development**

### 🖥️ **HUD Elements** (Planned)

```java
public class HudRenderer {
    public void renderFishingHud(DrawContext context) {
        // Fish count display
        // Mod status indicator
        // Session statistics
        // Bite detection visualization
    }
}
```

### ⚙️ **Configuration GUI** (Planned)

```java
public class ConfigScreen extends Screen {
    // ModMenu integration
    // Slider controls for timing
    // Toggle switches for features
    // Preset selection buttons
}
```

---

## 🚀 **Deployment & Distribution**

### 📦 **Build Process**

```bash
# Clean build
.\gradlew clean

# Generate sources (for development)
.\gradlew genSources

# Build mod jar
.\gradlew build

# Output: build/libs/feeshmandeelux-1.0.0.jar
```

### 🌐 **Distribution Checklist**
- [ ] Version number updated
- [ ] Changelog documented
- [ ] All features tested
- [ ] Documentation updated
- [ ] Screenshots/videos prepared
- [ ] Mod description written

### 📋 **Release Process**
1. **Version Bump**: Update version in gradle.properties
2. **Build & Test**: Ensure everything works
3. **Tag Release**: Create git tag
4. **Upload**: To CurseForge, Modrinth, GitHub
5. **Announce**: Community posts and updates

---

## 🤝 **Community & Collaboration**

### 💬 **Communication Channels**
- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Pull Requests**: Code contributions
- **Documentation**: Improvements and translations

### 🏆 **Recognition System**
- **Contributors**: Listed in README and mod credits
- **Major Features**: Special recognition for significant contributions
- **Documentation**: Credit for guides, tutorials, and translations
- **Testing**: Recognition for thorough testing and bug reports

### 📚 **Learning Resources**

#### **Fabric Modding**
- [Fabric Wiki](https://fabricmc.net/wiki/) - Official documentation
- [Fabric Example Mods](https://github.com/FabricMC/fabric-example-mod) - Sample code
- [Yarn Mappings](https://linkie.shedaniel.me/mappings) - Class/method names

#### **Minecraft Modding**
- [Minecraft Wiki](https://minecraft.wiki/) - Game mechanics
- [Fabric API Docs](https://maven.fabricmc.net/) - API reference
- [ModMenu](https://github.com/TerraformersMC/ModMenu) - Config GUI integration

---

## 🎯 **Future Vision**

### 🌟 **Long-term Goals**
- **Most Popular Fishing Mod**: Become the go-to auto-fishing solution
- **Feature Complete**: Implement all 45+ planned features
- **Community Driven**: Active contributor community
- **Cross-Platform**: Support for Forge, Quilt, and other loaders

### 🚀 **Innovation Areas**
- **AI-Powered Detection**: Machine learning for bite detection
- **Social Features**: Community fishing competitions
- **VR Integration**: Virtual reality fishing experience
- **Mobile Companion**: Phone app for remote monitoring

---

<div align="center">

## 🎣 **Ready to Contribute?**

**Pick an issue, fork the repo, and let's build something amazing together!**

### 🔗 **Quick Links**
- [🐛 Report Bug](https://github.com/yourusername/feeshman-deelux/issues/new?template=bug_report.md)
- [💡 Request Feature](https://github.com/yourusername/feeshman-deelux/issues/new?template=feature_request.md)
- [🤝 Start Contributing](https://github.com/yourusername/feeshman-deelux/contribute)
- [📖 View Documentation](https://github.com/yourusername/feeshman-deelux/docs)

---

**🛠️ Happy Coding! ✨**

*Made with ❤️ by developers, for developers*

</div> 