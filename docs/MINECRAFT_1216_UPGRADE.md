# Minecraft 1.21.6 Upgrade Guide

## Overview
This document outlines the process of upgrading Feeshman Deelux from Minecraft 1.21.4 to 1.21.6.

## Current Status
✅ **Successfully building with Minecraft 1.21.4**
⚠️ **Minecraft 1.21.6 requires newer toolchain**

## Version Requirements for 1.21.6

According to [Fabric's official blog post](https://fabricmc.net/2025/06/15/1216.html):

- **Minecraft**: 1.21.6  
- **Fabric Loader**: 0.16.14 (latest stable)
- **Fabric API**: 0.127.0+1.21.6 (latest for 1.21.6)
- **Fabric Loom**: 1.10+ (required for 1.21.6)
- **Gradle**: 8.12+ (required by Loom 1.10)
- **Java**: 21 (already supported)

## Breaking Changes in 1.21.6

### 1. HUD API Complete Rewrite
The old `HudRenderCallback` has been completely rewritten. We need to update from:

```java
// OLD (1.21.4 and earlier)
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@SuppressWarnings("deprecation")
var hudCallback = HudRenderCallback.EVENT;
hudCallback.register((context, tickCounter) -> {
    if (autoFishEnabled) {
        renderPolishedHUD(context);
    }
});
```

To:

```java
// NEW (1.21.6+)
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;

HudElementRegistry.addLast(Identifier.of("feeshmandeelux", "fishing_hud"), (context, tickCounter) -> {
    if (autoFishEnabled) {
        renderPolishedHUD(context);
    }
});
```

### 2. NBT Changes
- `NbtCompound` methods now return `Optional` instead of direct values
- New methods for codec-based NBT serialization

### 3. Fabric API Module Changes
- Several deprecated modules removed
- Some modules merged for simplicity
- Material API removed from Fabric Rendering API

## Current Roadblock

**Issue**: Loom 1.10+ is not yet available in the standard plugin repositories, even though it was officially released.

**Error**: `Plugin [id: 'fabric-loom', version: '1.10'] was not found`

## Workarounds Attempted

1. ✅ Legacy plugin syntax with Loom 1.8.13 - works for 1.21.4
2. ❌ Plugin syntax with Loom 1.8+ - not available in repositories  
3. ❌ Building with 1.21.6 using Loom 1.8.13 - fails with version incompatibility

## Next Steps

1. **Wait for Loom 1.10+ availability** in Maven repositories
2. **Test with development builds** if available
3. **Update documentation** once upgrade is complete

## Files That Need Updates for 1.21.6

- `gradle.properties` - version numbers
- `build.gradle` - Loom version, dependencies
- `fabric.mod.json` - version requirements  
- `FeeshmanDeeluxClient.java` - HUD API changes

## Testing Checklist

- [ ] Project builds successfully
- [ ] HUD renders correctly in-game
- [ ] Auto-fishing functionality works
- [ ] ModMenu integration works
- [ ] Configuration screen accessible
- [ ] Sound alerts function properly
- [ ] All existing features preserved

## Rollback Plan

If issues arise with 1.21.6:
1. Revert to 1.21.4 configuration (current working state)
2. Test all functionality  
3. Document any lost features
4. Plan alternative upgrade path

---

**Last Updated**: December 18, 2024  
**Status**: Waiting for Loom 1.10+ availability 