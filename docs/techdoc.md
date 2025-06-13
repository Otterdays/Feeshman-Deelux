# AutoFishing Mod - Technical Design Document

## 1. Project Overview

**Mod Name:** Feeshman_Deelux  
**Target Platform:** Minecraft Java Edition 1.21.4  
**Mod Framework:** Fabric  
**Client/Server:** Client-side only  
**Purpose:** Automatically detect fish bites and reel in catches, then recast line

## 2. Core Requirements

### Functional Requirements
- Detect when fishing bobber indicates a fish bite
- Automatically reel in the line when bite is detected
- Automatically recast fishing line after successful catch
- Provide configurable delays and randomization
- Include toggle functionality (on/off hotkey)
- Display status indicators to user

### Non-Functional Requirements
- Must work on multiplayer servers
- Should avoid detection by common anti-cheat systems
- Minimal performance impact
- User-friendly configuration
- Stable operation during extended use

## 3. System Architecture

### 3.1 Core Components

```
AutoFishing Mod
├── FishingDetector (monitors bobber state)
├── ActionExecutor (handles mouse clicks)
├── ConfigManager (user settings)
├── TimingController (delays and randomization)
└── UIManager (status display and controls)
```

### 3.2 Data Flow
1. Player casts fishing line manually or mod auto-casts
2. FishingDetector monitors FishingBobberEntity
3. Detector identifies bite condition
4. TimingController applies delay/randomization
5. ActionExecutor performs right-click action
6. System waits for catch completion
7. ActionExecutor recasts line
8. Loop repeats

## 4. Technical Implementation

### 4.1 Bobber Detection Logic

**Primary Detection Method:**
- Monitor `FishingBobberEntity.getVelocity()` for sudden downward movement
- Typical bite signature: Y-velocity changes from ~0 to negative value (-0.1 to -0.3)

**Backup Detection Methods:**
- Particle effects around bobber (bubble particles)
- Sound events (fishing bobber splash sounds)
- Bobber position relative to water surface

### 4.2 Timing System

**Reaction Time Randomization:**
- Base delay: 100-300ms after bite detection
- Human-like variance: ±50-150ms random offset
- Configurable reaction speed (slow/normal/fast presets)

**Recast Timing:**
- Wait for reel-in animation completion (~500-800ms)
- Additional delay before recast: 200-500ms
- Anti-pattern randomization: ±100-300ms variance

### 4.3 Event Handling

**Key Events to Monitor:**
- `FishingBobberEntity` spawn/despawn
- Entity velocity changes
- Player interaction events
- Inventory changes (new fish caught)

**Mixins Required:**
- `FishingBobberEntityMixin` - Access to bobber state
- `ClientPlayerInteractionManagerMixin` - Intercept clicks if needed

### 4.4 Configuration Options

```json
{
  "enabled": true,
  "reactionSpeed": "normal",
  "customDelayMin": 100,
  "customDelayMax": 300,
  "recastDelay": 400,
  "randomization": true,
  "debugMode": false,
  "keybinds": {
    "toggle": "R",
    "configGui": "O"
  }
}
```

## 5. User Interface

### 5.1 Status Indicators
- HUD overlay showing mod status (enabled/disabled)
- Current action indicator (waiting/bite detected/reeling/casting)
- Session statistics (fish caught, time elapsed)

### 5.2 Configuration GUI
- ModMenu integration for easy access
- Slider controls for timing adjustments
- Preset profiles (Legit/Normal/Fast)
- Real-time preview of timing settings

### 5.3 Keybinds
- Toggle mod on/off (default: R key)
- Open config GUI (default: O key)
- Emergency stop (default: ESC)

## 6. Anti-Detection Measures

### 6.1 Behavioral Randomization
- Vary reaction times within human-reasonable ranges
- Occasional "missed" bites (1-3% miss rate)
- Random micro-movements during waiting periods
- Vary recast timing patterns

### 6.2 Pattern Breaking
- Implement "fatigue" simulation (slightly slower reactions over time)
- Random brief pauses every 10-20 catches
- Simulate player attention lapses

### 6.3 Fail-safes
- Automatic disable if inventory becomes full
- Stop if player moves significantly
- Pause if other players are nearby (configurable)

## 7. Error Handling

### 7.1 Common Scenarios
- Bobber entity becomes null (line breaks)
- Player runs out of fishing rods
- Network lag causing delayed updates
- Inventory full scenarios

### 7.2 Recovery Mechanisms
- Graceful degradation when bobber tracking fails
- Automatic retry logic for failed casts
- User notifications for critical errors
- Automatic mod disable on repeated failures

## 8. Performance Considerations

### 8.1 Optimization Strategies
- Limit bobber checks to 20 TPS (once per tick)
- Use event-driven detection where possible
- Cache frequently accessed entity data
- Minimize memory allocations in hot paths

### 8.2 Resource Usage
- Target: <1% CPU overhead
- Memory footprint: <5MB additional
- Network impact: None (client-side only)

## 9. Testing Strategy

### 9.1 Unit Testing
- Bobber detection accuracy tests
- Timing randomization validation
- Configuration loading/saving
- Error handling scenarios

### 9.2 Integration Testing
- Single-player functionality
- Multiplayer server compatibility
- Mod compatibility testing
- Extended runtime stability tests

### 9.3 Anti-Cheat Testing
- Test on servers with known anti-cheat systems
- Monitor for detection patterns
- Validate randomization effectiveness

## 10. Development Phases

### Phase 1: Core Functionality (Week 1-2)
- Basic bobber detection
- Simple auto-clicking mechanism
- Toggle functionality
- Basic configuration

### Phase 2: Enhancement (Week 3)
- Timing randomization
- GUI implementation
- Status indicators
- Error handling

### Phase 3: Polish (Week 4)
- Anti-detection measures
- Performance optimization
- Comprehensive testing
- Documentation

### Phase 4: Release (Week 5)
- Final testing
- User documentation
- Distribution setup
- Community feedback integration

## 11. Dependencies

### Required Libraries
- Fabric API
- Fabric Loader
- ModMenu (optional, for config GUI)

### Development Tools
- Fabric MDK
- IntelliJ IDEA or similar IDE
- Minecraft Development Kit

## 12. Risk Assessment

### High Risk
- Anti-cheat detection leading to bans
- Minecraft version updates breaking compatibility
- Server-specific rule violations

### Medium Risk
- Performance impact on low-end systems
- Conflicts with other mods
- False positive bite detection

### Low Risk
- Configuration corruption
- GUI rendering issues
- Minor timing inaccuracies

## 13. Success Metrics

- Bite detection accuracy >95%
- Zero false positives in testing
- Stable operation for 2+ hour sessions
- Compatible with top 10 popular servers (testing)
- User satisfaction >4.0/5.0 in feedback

## 14. Future Enhancements

- Multiple fishing rod support
- Advanced fish filtering (keep/discard specific types)
- Integration with storage systems
- Fishing location optimization
- Statistics tracking and analysis