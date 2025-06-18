package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public class FeeshmanDeeluxClient implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    private static KeyBinding toggleKey;
    private boolean autoFishEnabled = false;
    private int recastDelayTicks = 0;
    private final int BASE_RECAST_DELAY = 20; // 1 second at 20 TPS (was 2 seconds)
    private final Random random = new Random();

    // Sound events
    public static final Identifier BITE_ALERT_ID = Identifier.of("feeshmandeelux", "bite_alert");
    public static final SoundEvent BITE_ALERT_SOUND = SoundEvent.of(BITE_ALERT_ID);

    // Bite detection variables
    private Vec3d lastBobberPos = null;
    private Vec3d lastBobberVelocity = null;
    private int biteDetectionCooldown = 0;
    private int fishingSessionTicks = 0;
    private int totalFishCaught = 0;
    private int lifetimeFishCaught = 0;

    // Enhanced bobber stuck detection with multiple validation systems
    private Vec3d bobberStuckCheckPos = null;
    private int bobberStuckTicks = 0;
    private final int BOBBER_STUCK_THRESHOLD = 600; // 30 seconds without significant movement (much more lenient)
    private final double STUCK_MOVEMENT_THRESHOLD = 0.3; // Smaller threshold for more sensitive movement detection
    private boolean hasWarnedAboutMob = false;
    private boolean bobberInWater = false;
    private int consecutiveStuckChecks = 0;
    private Vec3d lastValidBobberPos = null;

    // Human-like timing - Made faster and more responsive
    private int humanReactionDelay = 0;
    private final int MIN_REACTION_TIME = 3; // 0.15 seconds (was 0.5s)
    private final int MAX_REACTION_TIME = 12; // 0.6 seconds (was 1.5s)

    // Add delay for catch announcement to ensure inventory updates
    private int catchAnnouncementDelay = 0;
    private final int CATCH_ANNOUNCEMENT_DELAY = 2; // 0.1 seconds delay (5x faster than before!)

    // Welcome message system
    private boolean hasShownWelcomeMessage = false;
    private int welcomeMessageDelay = 100; // 5 seconds at 20 TPS
    private int welcomeMessageTimer = 0;

    // Session tracking
    private boolean hasWarnedDurability = false;
    private Map<String, Integer> biomeCatchTracker = new HashMap<>();
    private Map<String, Integer> previousInventoryCount = new HashMap<>(); // Track item counts instead of stacks
    private long sessionStartTime = 0;

    // Sound volume control removed - using FeeshmanConfig.getBiteAlertVolume() directly

    // Lucky catch compliments
    private final String[] LUCKY_COMPLIMENTS = {
        "🌟 The fishing gods smile upon you!",
        "🔥 Legendary angling skills on display!",
        "⚡ Lightning reflexes secure the prize!",
        "🎯 Precision fishing mastery achieved!",
        "🌊 The ocean yields its secrets!",
        "💫 Cosmic fishing luck activated!",
        "🏆 Hall of Fame worthy catch!",
        "🎪 Spectacular fishing performance!",
        "🌈 Rainbow luck shines down!",
        "⭐ Stellar fishing technique!"
    };

    // Fishing quotes
    private final String[] FISHING_QUOTES = {
        "🎣 \"Patience is the angler's virtue.\"",
        "🌊 \"The sea rewards those who wait.\"",
        "🐟 \"Every cast is a new adventure.\"",
        "⭐ \"Fortune favors the persistent fisher.\"",
        "🌅 \"Dawn brings the best catches.\"",
        "🎯 \"Skill and luck dance together on the water.\"",
        "💎 \"Treasures hide beneath calm waters.\"",
        "🌙 \"Night fishing reveals hidden wonders.\"",
        "🔮 \"The depths hold ancient secrets.\"",
        "🏆 \"Master anglers are made, not born.\""
    };

    private static final TagKey<net.minecraft.item.Item> TREASURE_TAG = TagKey.of(net.minecraft.registry.RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "treasure"));
    private static final TagKey<net.minecraft.item.Item> JUNK_TAG = TagKey.of(net.minecraft.registry.RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "junk"));

    @Override
    public void onInitializeClient() {
        LOGGER.info("🎣 Feeshman Deelux Initializing!");

        // Load configuration
        FeeshmanConfig.load();
        // Load leaderboard
        FeeshLeaderboard.load();

        // Register sound event
        Registry.register(Registries.SOUND_EVENT, BITE_ALERT_ID, BITE_ALERT_SOUND);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.feeshmandeelux.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.feeshmandeelux.general"
        ));

        // Register enhanced HUD renderer (will migrate to new HudElementRegistry when available)
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (autoFishEnabled) {
                renderPolishedHUD(context);
            }
        });

        // Register world join event for welcome message
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            hasShownWelcomeMessage = false;
            welcomeMessageTimer = welcomeMessageDelay;
            sessionStartTime = System.currentTimeMillis();
            
            // Show random fishing quote on join
            if (random.nextFloat() < 0.3f) { // 30% chance
                String quote = FISHING_QUOTES[random.nextInt(FISHING_QUOTES.length)];
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§7" + quote), false);
                    }
                });
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle welcome message
            if (!hasShownWelcomeMessage && client.player != null && welcomeMessageTimer > 0) {
                welcomeMessageTimer--;
                if (welcomeMessageTimer <= 0) {
                    hasShownWelcomeMessage = true;
                    showEnhancedWelcomeMessage(client.player);
                }
            }

            if (toggleKey.wasPressed()) {
                autoFishEnabled = !autoFishEnabled;
                if (client.player != null) {
                    String status = autoFishEnabled ? "§a§lEnabled" : "§c§lDisabled";
                    client.player.sendMessage(Text.literal("🎣 §6§lFeeshman Deelux " + status), false);
                    
                    if (autoFishEnabled) {
                        fishingSessionTicks = 0;
                        totalFishCaught = 0;
                        hasWarnedDurability = false;
                        hasWarnedAboutMob = false;
                        takeInventorySnapshot(client.player);
                        client.player.sendMessage(Text.literal("§7Press O again to disable. Happy fishing! 🐟"), false);
                        
                        // Show random fishing quote on enable
                        if (random.nextFloat() < 0.2f) { // 20% chance
                            String quote = FISHING_QUOTES[random.nextInt(FISHING_QUOTES.length)];
                            client.player.sendMessage(Text.literal("§7" + quote), false);
                        }
                    } else {
                        int sessionMinutes = fishingSessionTicks / 1200; // 20 TPS * 60 seconds
                        client.player.sendMessage(Text.literal("§7Session: " + sessionMinutes + " minutes, " + totalFishCaught + " fish caught! 📊"), false);
                    }
                }
            }

            if (autoFishEnabled && client.player != null && client.world != null) {
                fishingSessionTicks++;
                
                // Check rod durability warning
                checkRodDurability(client.player);
                
                // Handle various delays
                if (recastDelayTicks > 0) {
                    recastDelayTicks--;
                    return;
                }
                
                if (biteDetectionCooldown > 0) {
                    biteDetectionCooldown--;
                }
                
                // Handle catch announcement delay
                if (catchAnnouncementDelay > 0) {
                    catchAnnouncementDelay--;
                    if (catchAnnouncementDelay == 0) {
                        // Now check for new items after delay
                        checkForNewItems(client.player);
                    }
                }
                
                if (humanReactionDelay > 0) {
                    humanReactionDelay--;
                    if (humanReactionDelay == 0 && client.player.fishHook != null) {
                        // Time to reel in the fish! Use proper right-click simulation
                        LOGGER.info("🎣 Feeshman Deelux: Reeling in fish!");
                        
                        // Proper right-click simulation for fishing - reel in the fish
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        
                        recastDelayTicks = BASE_RECAST_DELAY + random.nextInt(40); // 2-4 seconds
                        totalFishCaught++;
                        lifetimeFishCaught++;
                        
                        // Track biome catch
                        trackBiomeCatch(client);
                        
                        // Update leaderboard
                        FeeshLeaderboard.addCatch(client.player);
                        
                        // Start delayed catch announcement to ensure inventory updates
                        catchAnnouncementDelay = CATCH_ANNOUNCEMENT_DELAY;
                        
                        // Lucky catch compliment (5% chance)
                        if (random.nextFloat() < 0.05f) {
                            String compliment = LUCKY_COMPLIMENTS[random.nextInt(LUCKY_COMPLIMENTS.length)];
                            client.player.sendMessage(Text.literal("§6" + compliment), false);
                        }
                        
                        // Show catch notification
                        if (totalFishCaught % 5 == 0) {
                            client.player.sendMessage(Text.literal("🐟 §a" + totalFishCaught + " fish caught this session! §7Keep it up!"), false);
                        }
                        
                        // Achievement toasts
                        showAchievementToasts(client, totalFishCaught, lifetimeFishCaught);
                    }
                    return;
                }

                ClientPlayerEntity player = client.player;
                
                // Check if player has fishing rod
                if (player.getMainHandStack().getItem().toString().contains("fishing_rod")) {
                    if (player.fishHook == null) {
                        // Auto-recast with randomized delay
                        LOGGER.info("🎣 Feeshman Deelux: Recasting rod...");
                        
                        // Proper right-click simulation for casting
                        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                        
                        recastDelayTicks = BASE_RECAST_DELAY + random.nextInt(20); // 2-3 seconds
                        lastBobberPos = null;
                        lastBobberVelocity = null;
                        bobberStuckCheckPos = null;
                        bobberStuckTicks = 0;
                        hasWarnedAboutMob = false;
                    } else {
                        // Monitor bobber for bite detection and stuck detection
                        FishingBobberEntity bobber = player.fishHook;
                        Vec3d currentPos = bobber.getPos();
                        Vec3d currentVelocity = bobber.getVelocity();
                        
                        // Update bobber water status for smarter stuck detection
                        bobberInWater = bobber.isInFluid();
                        
                        // Check for mob collision first
                        if (checkMobCollision(client, bobber)) {
                            if (!hasWarnedAboutMob) {
                                client.player.sendMessage(Text.literal("§e⚠️ Bobber hooked a mob! Recasting..."), false);
                                hasWarnedAboutMob = true;
                            }
                            
                            // Force recast
                            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                            recastDelayTicks = 40; // 2 second delay before next cast
                            lastBobberPos = null;
                            lastBobberVelocity = null;
                            bobberStuckCheckPos = null;
                            bobberStuckTicks = 0;
                            return;
                        }
                        
                        // Check if bobber is stuck (less aggressive)
                        if (checkBobberStuck(currentPos)) {
                            LOGGER.info("🎣 Feeshman Deelux: Bobber appears stuck, recasting...");
                            client.player.sendMessage(Text.literal("§e⚠️ Bobber stuck detected, recasting..."), false);
                            
                            // Force recast
                            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                            recastDelayTicks = 40; // 2 second delay before next cast
                            lastBobberPos = null;
                            lastBobberVelocity = null;
                            bobberStuckCheckPos = null;
                            bobberStuckTicks = 0;
                            return;
                        }
                        
                        if (detectFishBite(bobber, currentPos, currentVelocity)) {
                            // Fish bite detected!
                            LOGGER.info("🐟 Feeshman Deelux: Fish bite detected!");
                            
                            // Play bite alert sound with enhanced volume
                            if (client.world != null && client.player != null) {
                                float volume = Math.min(1.0f, FeeshmanConfig.getBiteAlertVolume() * 2.0f); // Double the volume but cap at 1.0
                                
                                // Primary sound method - play at player's location
                                client.world.playSound(
                                    client.player,
                                    client.player.getBlockPos(),
                                    BITE_ALERT_SOUND,
                                    SoundCategory.PLAYERS,
                                    volume,
                                    1.0f  // Pitch
                                );
                                
                                // Fallback method for better audio reliability
                                client.player.playSound(BITE_ALERT_SOUND, volume, 1.0f);
                            }
                            
                            // Add human-like reaction delay
                            humanReactionDelay = MIN_REACTION_TIME + random.nextInt(MAX_REACTION_TIME - MIN_REACTION_TIME);
                        }
                        
                        // Update tracking variables
                        lastBobberPos = currentPos;
                        lastBobberVelocity = currentVelocity;
                    }
                } else if (autoFishEnabled) {
                    // No fishing rod detected
                    client.player.sendMessage(Text.literal("§e⚠️ No fishing rod in main hand! Auto-fishing paused."), false);
                    autoFishEnabled = false;
                }
            }
        });

        // Register commands with colorized help
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Main /feeshman command with help
            dispatcher.register(ClientCommandManager.literal("feeshman")
                .executes(context -> {
                    showFeeshmanHelp(context.getSource().getPlayer());
                    return 1;
                })
            );
            
            // /feeshstats command
            dispatcher.register(ClientCommandManager.literal("feeshstats")
                .executes(context -> {
                    showFishingStats(context.getSource().getPlayer());
                    return 1;
                })
                .then(ClientCommandManager.literal("biome")
                    .executes(context -> {
                        showBiomeStats(context.getSource().getPlayer());
                        return 1;
                    })
                )
            );
            
            // /feeshleaderboard command
            dispatcher.register(ClientCommandManager.literal("feeshleaderboard")
                .executes(context -> {
                    showLeaderboard(context.getSource().getPlayer());
                    return 1;
                })
            );
        });
    }
    
    private void renderPolishedHUD(DrawContext context) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        var textRenderer = client.textRenderer;
        
        // Enhanced HUD dimensions and positioning
        int hudX = 6;
        int hudY = 6;
        int hudWidth = 220; // Slightly wider for better content layout
        int hudHeight = 160; // Slightly taller for better spacing
        
        // Modern translucent colors with subtle gradients
        int outerBorderColor = 0x80000000; // Semi-transparent black outer border
        int innerBorderColor = 0xA0222222; // Semi-transparent dark gray border
        int backgroundColorMain = 0x85000000; // More transparent main background
        int titleBackgroundColor = 0xB0004466; // Semi-transparent dark blue title
        int accentColor = 0xFF00DDFF; // Bright cyan accent
        
        // Draw modern layered border with transparency
        context.fill(hudX - 2, hudY - 2, hudX + hudWidth + 2, hudY + hudHeight + 2, outerBorderColor);
        context.fill(hudX - 1, hudY - 1, hudX + hudWidth + 1, hudY + hudHeight + 1, innerBorderColor);
        context.fill(hudX, hudY, hudX + hudWidth, hudY + hudHeight, backgroundColorMain);
        
        // Draw elegant title background with gradient effect
        context.fill(hudX, hudY, hudX + hudWidth, hudY + 20, titleBackgroundColor);
        context.fill(hudX, hudY + 18, hudX + hudWidth, hudY + 20, accentColor); // Accent line
        
        // Enhanced title header with better unicode and styling
        String title = "⚡ Feeshman Deelux ⚡";
        int titleWidth = textRenderer.getWidth(title);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        // Draw title with elegant styling
        context.drawText(textRenderer, title, titleX, hudY + 6, 0xFFFFFFFF, true); // Bright white text
        
        // Content area starts below title with better spacing
        int contentY = hudY + 26;
        int lineHeight = 14; // Better line spacing
        int currentLine = 0;
        
        // Enhanced fish counter with elegant styling
        String fishText = String.format("🐠 %d fish caught", totalFishCaught);
        int fishColor = 0xFF4AE54A; // Elegant bright green
        context.drawText(textRenderer, fishText, hudX + 8, contentY + (currentLine * lineHeight), fishColor, true);
        currentLine++;
        
        // Enhanced session time with better formatting and unicode
        int sessionMinutes = fishingSessionTicks / 1200;
        int sessionSeconds = (fishingSessionTicks % 1200) / 20;
        String timeText = String.format("⏱️  %02d:%02d session", sessionMinutes, sessionSeconds);
        context.drawText(textRenderer, timeText, hudX + 8, contentY + (currentLine * lineHeight), 0xFFFFA500, true); // Orange
        currentLine++;
        
        // Enhanced rod durability with visual bar
        if (client.player != null) {
            ItemStack rod = client.player.getMainHandStack();
            if (rod.getItem() == Items.FISHING_ROD) {
                int maxDurability = rod.getMaxDamage();
                int currentDamage = rod.getDamage();
                int remainingUses = maxDurability - currentDamage;
                int durabilityPercent = (remainingUses * 100) / maxDurability;
                
                String durabilityText = String.format("🛠️  %d uses (%d%%)", remainingUses, durabilityPercent);
                int color = durabilityPercent > 50 ? 0xFF4AE54A : durabilityPercent > 20 ? 0xFFFFB347 : 0xFFFF6B6B;
                context.drawText(textRenderer, durabilityText, hudX + 8, contentY + (currentLine * lineHeight), color, true);
                
                // Draw enhanced durability bar with transparency
                int barWidth = 90;
                int barHeight = 4;
                int barX = hudX + hudWidth - barWidth - 10;
                int barY = contentY + (currentLine * lineHeight) + 3;
                
                // Background bar with transparency
                context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x60333333);
                // Durability bar with glow effect
                int fillWidth = (barWidth * durabilityPercent) / 100;
                context.fill(barX, barY, barX + fillWidth, barY + barHeight, color);
                
                currentLine++;
            }
        }
        
        // Enhanced weather and time indicators
        if (client.player != null && client.world != null) {
            // Weather indicator with enhanced styling and better unicode
            String weatherIcon = client.world.isRaining() ? (client.world.isThundering() ? "⛈️" : "🌧️") : "☀️";
            String weatherText = String.format("%s %s", weatherIcon, (client.world.isRaining() ? "Rainy" : "Clear"));
            int weatherColor = client.world.isRaining() ? 0xFF4A9AFF : 0xFFFFD700;
            context.drawText(textRenderer, weatherText, hudX + 8, contentY + (currentLine * lineHeight), weatherColor, true);
            currentLine++;
            
            // Enhanced day/night and moon phase indicator with better spacing
            long timeOfDay = client.world.getTimeOfDay() % 24000;
            boolean isDay = timeOfDay < 12000;
            String timeIcon = isDay ? "🌅" : "🌙";
            String dayNightText = String.format("%s %s", timeIcon, (isDay ? "Day" : "Night"));
            
            if (!isDay) {
                int moonPhase = client.world.getMoonPhase();
                String[] moonPhases = {"🌑", "🌒", "🌓", "🌔", "🌕", "🌖", "🌗", "🌘"};
                dayNightText = String.format("%s %s", moonPhases[moonPhase], "Night");
            }
            
            int timeColor = isDay ? 0xFFFFD700 : 0xFFADD8E6;
            context.drawText(textRenderer, dayNightText, hudX + 8, contentY + (currentLine * lineHeight), timeColor, true);
            currentLine++;
        }
        
        // Enhanced current biome with color coding
        if (client.player != null && client.world != null) {
            RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
            String biomeName = biome.getKey().map(key -> key.getValue().toString()).orElse("unknown");
            biomeName = biomeName.replace("minecraft:", "").replace("_", " ");
            String biomeText = String.format("🌍 %s", capitalizeWords(biomeName));
            
            // Color code biomes with elegant colors
            int biomeColor = 0xFF40E0D0; // Default turquoise
            if (biomeName.contains("ocean")) biomeColor = 0xFF0080FF;
            else if (biomeName.contains("river")) biomeColor = 0xFF87CEEB;
            else if (biomeName.contains("swamp")) biomeColor = 0xFF90EE90;
            else if (biomeName.contains("jungle")) biomeColor = 0xFF32CD32;
            else if (biomeName.contains("desert")) biomeColor = 0xFFFFA500;
            else if (biomeName.contains("forest")) biomeColor = 0xFF228B22;
            
            context.drawText(textRenderer, biomeText, hudX + 8, contentY + (currentLine * lineHeight), biomeColor, true);
            currentLine++;
        }
        
        // Enhanced catch rate indicator with efficiency rating
        if (fishingSessionTicks > 0) {
            float catchRate = (float) totalFishCaught / (fishingSessionTicks / 1200.0f); // fish per minute
            String efficiency = catchRate > 2.0f ? "Excellent" : catchRate > 1.0f ? "Good" : catchRate > 0.5f ? "Fair" : "Slow";
            String rateText = String.format("📊 %.1f/min (%s)", Math.max(0, catchRate), efficiency);
            int rateColor = catchRate > 2.0f ? 0xFF4AE54A : catchRate > 1.0f ? 0xFF9ACD32 : catchRate > 0.5f ? 0xFFFFB347 : 0xFFFF7F50;
            context.drawText(textRenderer, rateText, hudX + 8, contentY + (currentLine * lineHeight), rateColor, true);
            currentLine++;
        }
        
        // Enhanced status indicator with animated colors
        String statusText;
        int statusColor;
        
                if (!autoFishEnabled) {
            statusText = "🎣 Manual Mode (Press O for auto)";
            statusColor = 0xFFDDA0DD;
        } else if (humanReactionDelay > 0) {
            statusText = "🎯 Bite Detected!";
            statusColor = 0xFFFF4500; // Bright orange-red
        } else if (recastDelayTicks > 0) {
            statusText = "🔄 Auto Recasting...";
            statusColor = 0xFF1E90FF;
        } else if (biteDetectionCooldown > 0) {
            statusText = "⏳ Auto Waiting...";
            statusColor = 0xFFFFB347;
        } else {
            statusText = "⚡ Auto Active";
            statusColor = 0xFF4AE54A;
        }
        context.drawText(textRenderer, statusText, hudX + 8, contentY + (currentLine * lineHeight), statusColor, true);
        currentLine++;
        
        // Add lifetime stats with better styling if available
        if (lifetimeFishCaught > 0) {
            String lifetimeText = String.format("🏆 %d lifetime catches", lifetimeFishCaught);
            context.drawText(textRenderer, lifetimeText, hudX + 8, contentY + (currentLine * lineHeight), 0xFFFFD700, true);
        }
        
        // HUD rendering complete
    }
    
    private String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    private boolean checkMobCollision(net.minecraft.client.MinecraftClient client, FishingBobberEntity bobber) {
        if (client.world == null) return false;
        
        // Check for entities near the bobber
        Vec3d pos = bobber.getPos();
        Box searchBox = new Box(pos.x - 1.5, pos.y - 1.5, pos.z - 1.5, pos.x + 1.5, pos.y + 1.5, pos.z + 1.5);
        List<Entity> nearbyEntities = client.world.getOtherEntities(bobber, searchBox);
        
        for (Entity entity : nearbyEntities) {
            // Check for mobs that commonly interfere with fishing
            if (entity instanceof SquidEntity || 
                entity instanceof DrownedEntity || 
                entity instanceof ZombieEntity || 
                entity instanceof SkeletonEntity ||
                (entity instanceof MobEntity && entity.getBoundingBox().intersects(bobber.getBoundingBox()))) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean checkBobberStuck(Vec3d currentPos) {
        if (bobberStuckCheckPos == null) {
            bobberStuckCheckPos = currentPos;
            bobberStuckTicks = 0;
            consecutiveStuckChecks = 0;
            lastValidBobberPos = currentPos;
            return false;
        }
        
        // Check if bobber has moved significantly (more sensitive movement detection)
        double distance = currentPos.distanceTo(bobberStuckCheckPos);
        double distanceFromLastValid = lastValidBobberPos != null ? currentPos.distanceTo(lastValidBobberPos) : distance;
        
        // Multiple validation systems to prevent false positives
        boolean hasMovedRecently = distance >= STUCK_MOVEMENT_THRESHOLD;
        boolean hasMovedFromLastValid = distanceFromLastValid >= STUCK_MOVEMENT_THRESHOLD;
        
        if (hasMovedRecently || hasMovedFromLastValid) {
            // Bobber moved significantly, reset all tracking
            bobberStuckCheckPos = currentPos;
            bobberStuckTicks = 0;
            consecutiveStuckChecks = 0;
            lastValidBobberPos = currentPos;
            return false;
        } else {
            // Bobber hasn't moved much
            bobberStuckTicks++;
            consecutiveStuckChecks++;
            
            // Only consider stuck after much longer time AND multiple consecutive checks
            if (bobberStuckTicks >= BOBBER_STUCK_THRESHOLD && consecutiveStuckChecks >= 100) { // 30 seconds + 5 seconds of consecutive checks
                // Additional validation: check if bobber is actually in a problematic state
                if (bobberInWater) {
                    // If bobber is in water and hasn't moved for 30+ seconds, it might be legitimately fishing
                    // Only recast if we're absolutely sure it's stuck
                    return bobberStuckTicks >= (BOBBER_STUCK_THRESHOLD * 2); // 60 seconds for in-water bobbers
                } else {
                    // Bobber not in water and hasn't moved - likely stuck on land/blocks
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void showEnhancedWelcomeMessage(ClientPlayerEntity player) {
        // Enhanced welcome message with detailed detection methods and colorful presentation
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§6§l╔══════════════════════════════════════════════════════════════╗"), false);
        player.sendMessage(Text.literal("§6§l║                    🎣 §b§lFEESHMAN DEELUX §6§l🎣                    ║"), false);
        player.sendMessage(Text.literal("§6§l╚══════════════════════════════════════════════════════════════╝"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§a✨ §6§lWelcome to the Ultimate Auto-Fishing Experience! §a✨"), false);
        player.sendMessage(Text.literal("§7Press §a§l[O]§r§7 to toggle auto-fishing §8• §7ModMenu for settings"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§e§l🔬 ADVANCED DETECTION SYSTEMS:"), false);
        player.sendMessage(Text.literal("§7▸ §b§lVelocity Analysis§7: Monitors bobber movement patterns"), false);
        player.sendMessage(Text.literal("§7▸ §b§lDownward Motion§7: Detects fish pulling bobber underwater"), false);
        player.sendMessage(Text.literal("§7▸ §b§lPosition Tracking§7: Analyzes sudden position changes"), false);
        player.sendMessage(Text.literal("§7▸ §b§lWater Validation§7: Ensures bobber is properly submerged"), false);
        player.sendMessage(Text.literal("§7▸ §b§lBobber Dip Detection§7: Instant response to Y-axis drops"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§e§l🛡 SMART SAFETY FEATURES:"), false);
        player.sendMessage(Text.literal("§7▸ §c§lMob Collision Detection§7: Auto-avoids squids, drowned, etc."), false);
        player.sendMessage(Text.literal("§7▸ §c§lIntelligent Stuck Detection§7: 30s threshold with water validation"), false);
        player.sendMessage(Text.literal("§7▸ §c§lHuman-like Timing§7: 0.15-0.6s reaction delays"), false);
        player.sendMessage(Text.literal("§7▸ §c§lDurability Monitoring§7: Warns at low rod durability"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§e§l📊 ENHANCED HUD DISPLAY:"), false);
        player.sendMessage(Text.literal("§7▸ §d§lReal-time Statistics§7: Fish count, session time, catch rate"), false);
        player.sendMessage(Text.literal("§7▸ §d§lWeather & Time§7: Rain indicator, day/night, moon phases"), false);
        player.sendMessage(Text.literal("§7▸ §d§lBiome Tracking§7: Current location and catch analytics"), false);
        player.sendMessage(Text.literal("§7▸ §d§lStatus Indicators§7: Live fishing state and activity"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§a🎵 §7Bite alert sounds §8• §a🏆 §7Achievement toasts §8• §a📈 §7Statistics tracking"), false);
        player.sendMessage(Text.literal("§a🎭 §7Fishing quotes §8• §a🌟 §7Lucky compliments §8• §a🎛 §7ModMenu integration"), false);
        player.sendMessage(Text.literal(""), false);
        
        player.sendMessage(Text.literal("§6§l═══════════════════════════════════════════════════════════════"), false);
        player.sendMessage(Text.literal("§7🌊 §b§lHappy Fishing, and may your lines be tight! §7🐟✨"), false);
        player.sendMessage(Text.literal("§6§l═══════════════════════════════════════════════════════════════"), false);
        player.sendMessage(Text.literal(""), false);
    }
    
    private void showFeeshmanHelp(ClientPlayerEntity player) {
        player.sendMessage(Text.literal("§6§l=== 🎣 Feeshman Deelux Help ==="), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§e§lCommands:"), false);
        player.sendMessage(Text.literal("§a/feeshman §7- §fShow this help message"), false);
        player.sendMessage(Text.literal("§a/feeshstats §7- §fView fishing statistics"), false);
        player.sendMessage(Text.literal("§a/feeshstats biome §7- §fView biome catch breakdown"), false);
        player.sendMessage(Text.literal("§a/feeshleaderboard §7- §fView leaderboard"), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§e§lControls:"), false);
        player.sendMessage(Text.literal("§a[O] §7- §fToggle auto-fishing on/off"), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§e§lFeatures:"), false);
        player.sendMessage(Text.literal("§7• §fAdvanced 5-method bite detection"), false);
        player.sendMessage(Text.literal("§7• §fSmart bobber stuck & mob collision detection"), false);
        player.sendMessage(Text.literal("§7• §fLive HUD with session stats & rod durability"), false);
        player.sendMessage(Text.literal("§7• §fItem announcements & achievement toasts"), false);
        player.sendMessage(Text.literal("§7• §fBiome tracking & fishing statistics"), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§7Happy fishing! 🐟✨"), false);
    }
    
    private void checkRodDurability(ClientPlayerEntity player) {
        ItemStack rod = player.getMainHandStack();
        if (rod.getItem() == Items.FISHING_ROD && !hasWarnedDurability) {
            int maxDurability = rod.getMaxDamage();
            int currentDamage = rod.getDamage();
            int remainingUses = maxDurability - currentDamage;
            
            if (remainingUses <= 10) {
                hasWarnedDurability = true;
                player.sendMessage(Text.literal("§c⚠️ §lWARNING: §r§cFishing rod durability low! Only " + remainingUses + " uses remaining!"), false);
            }
        }
    }
    
    private void trackBiomeCatch(net.minecraft.client.MinecraftClient client) {
        if (client.player != null && client.world != null) {
            RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
            String biomeName = biome.getKey().map(key -> key.getValue().toString()).orElse("unknown");
            biomeCatchTracker.put(biomeName, biomeCatchTracker.getOrDefault(biomeName, 0) + 1);
        }
    }
    
    private void takeInventorySnapshot(ClientPlayerEntity player) {
        previousInventoryCount.clear();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && isFishingLoot(stack)) {
                previousInventoryCount.put(stack.getItem().toString(), previousInventoryCount.getOrDefault(stack.getItem().toString(), 0) + stack.getCount());
            }
        }
    }
    
    private void checkForNewItems(ClientPlayerEntity player) {
        // Only check if we have a previous snapshot to compare against
        if (previousInventoryCount.isEmpty()) {
            takeInventorySnapshot(player);
            return;
        }
        
        // Get current inventory counts efficiently
        Map<String, Integer> currentInventoryCount = new HashMap<>();
        Map<String, ItemStack> itemStackMap = new HashMap<>(); // Keep reference to ItemStack for announcements
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && isFishingLoot(stack)) {
                String itemKey = stack.getItem().toString();
                currentInventoryCount.put(itemKey, currentInventoryCount.getOrDefault(itemKey, 0) + stack.getCount());
                itemStackMap.put(itemKey, stack); // Keep one reference for announcements
            }
        }
        
        // Find new items by comparing counts
        for (Map.Entry<String, Integer> entry : currentInventoryCount.entrySet()) {
            String itemKey = entry.getKey();
            int currentCount = entry.getValue();
            int previousCount = previousInventoryCount.getOrDefault(itemKey, 0);
            
            if (currentCount > previousCount) {
                // Item count increased, announce only the new items
                int newItems = currentCount - previousCount;
                ItemStack stackToAnnounce = itemStackMap.get(itemKey);
                
                for (int i = 0; i < newItems; i++) {
                    announceNewItem(player, stackToAnnounce);
                }
            }
        }
        
        // Update snapshot for next comparison
        takeInventorySnapshot(player);
    }
    
    private boolean isFishingLoot(ItemStack stack) {
        // Comprehensive fishing loot detection
        // First check vanilla fish tag
        if (stack.isIn(ItemTags.FISHES)) {
            return true;
        }
        
        // Check our custom tags
        if (stack.isIn(TREASURE_TAG) || stack.isIn(JUNK_TAG)) {
            return true;
        }
        
        // Fallback: Manual check for all known fishing loot items
        // This ensures compatibility even if tags aren't working
        return stack.getItem() == Items.COD ||
               stack.getItem() == Items.SALMON ||
               stack.getItem() == Items.TROPICAL_FISH ||
               stack.getItem() == Items.PUFFERFISH ||
               stack.getItem() == Items.ENCHANTED_BOOK ||
               stack.getItem() == Items.NAME_TAG ||
               stack.getItem() == Items.SADDLE ||
               stack.getItem() == Items.NAUTILUS_SHELL ||
               stack.getItem() == Items.BOW ||
               stack.getItem() == Items.FISHING_ROD ||
               stack.getItem() == Items.LEATHER_BOOTS ||
               stack.getItem() == Items.LEATHER ||
               stack.getItem() == Items.BONE ||
               stack.getItem() == Items.STRING ||
               stack.getItem() == Items.STICK ||
               stack.getItem() == Items.BOWL ||
               stack.getItem() == Items.ROTTEN_FLESH ||
               stack.getItem() == Items.POTION ||
               stack.getItem() == Items.TRIPWIRE_HOOK ||
               stack.getItem() == Items.INK_SAC ||
               stack.getItem() == Items.LILY_PAD ||
               stack.getItem() == Items.BAMBOO ||
               stack.getItem() == Items.COCOA_BEANS;
    }
    
    private void announceNewItem(ClientPlayerEntity player, ItemStack stack) {
        String itemName = stack.getName().getString();
        String message = getItemMessage(stack, itemName);
        
        // Add sound effect for special catches
        if (isSpecialCatch(stack)) {
            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }
        
        player.sendMessage(Text.literal(message), false);
    }
    
    private boolean isSpecialCatch(ItemStack stack) {
        // Treasure items are considered special
        return stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.NAME_TAG ||
               stack.getItem() == Items.SADDLE || stack.getItem() == Items.NAUTILUS_SHELL ||
               stack.getItem() == Items.BOW || stack.getItem() == Items.FISHING_ROD;
    }
    
    private String getItemMessage(ItemStack stack, String itemName) {
        // Enhanced messages with better categorization and colors
        
        // Fish messages (Green theme)
        if (stack.getItem() == Items.COD) {
            return "§a🐟 §l§aFresh cod caught: §f" + itemName + "§a!";
        } else if (stack.getItem() == Items.SALMON) {
            return "§a🍣 §l§aSalmon secured: §f" + itemName + "§a!";
        } else if (stack.getItem() == Items.TROPICAL_FISH) {
            return "§a🌺 §l§aTropical beauty: §f" + itemName + "§a!";
        } else if (stack.getItem() == Items.PUFFERFISH) {
            return "§a🐡 §l§aSpiky surprise: §f" + itemName + "§a!";
        }
        
        // Treasure messages (Gold/Yellow theme with emphasis)
        else if (stack.getItem() == Items.ENCHANTED_BOOK) {
            return "§6📚✨ §l§6TREASURE! §e§lAncient knowledge surfaces: §f" + itemName + "§6!";
        } else if (stack.getItem() == Items.NAME_TAG) {
            return "§6🏷️🌟 §l§6TREASURE! §e§lA mysterious tag emerges: §f" + itemName + "§6!";
        } else if (stack.getItem() == Items.SADDLE) {
            return "§6🐎⚡ §l§6TREASURE! §e§lAdventure gear acquired: §f" + itemName + "§6!";
        } else if (stack.getItem() == Items.NAUTILUS_SHELL) {
            return "§6🐚💎 §l§6TREASURE! §e§lRare ocean treasure: §f" + itemName + "§6!";
        } else if (stack.getItem() == Items.BOW) {
            return "§6🏹✨ §l§6TREASURE! §e§lEnchanted bow discovered: §f" + itemName + "§6!";
        } else if (stack.getItem() == Items.FISHING_ROD && stack.hasEnchantments()) {
            return "§6🎣✨ §l§6TREASURE! §e§lEnchanted fishing rod found: §f" + itemName + "§6!";
        }
        
        // Junk messages (Gray/Dark theme)
        else if (stack.getItem() == Items.BONE) {
            return "§8🦴 §7Skeletal remains: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.LEATHER_BOOTS) {
            return "§8👢 §7Waterlogged boots: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.LEATHER) {
            return "§8🧳 §7Leather scraps: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.STRING) {
            return "§8🧵 §7Tangled string: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.STICK) {
            return "§8🪵 §7Driftwood stick: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.BOWL) {
            return "§8🥣 §7Floating bowl: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.ROTTEN_FLESH) {
            return "§8🧟 §7Questionable meat: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.POTION) {
            return "§8🍼 §7Waterlogged bottle: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.TRIPWIRE_HOOK) {
            return "§8🪝 §7Rusty hook: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.INK_SAC) {
            return "§8🖤 §7Squid ink: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.LILY_PAD) {
            return "§8🪷 §7Floating lily pad: §f" + itemName + "§7!";
        } else if (stack.getItem() == Items.BAMBOO) {
            return "§2🎋 §a§lJungle bamboo: §f" + itemName + "§a!";
        } else if (stack.getItem() == Items.COCOA_BEANS) {
            return "§6🍫 §e§lCocoa beans: §f" + itemName + "§e!";
        } else if (stack.getItem() == Items.FISHING_ROD && !stack.hasEnchantments()) {
            return "§8🎣 §7Old fishing rod: §f" + itemName + "§7!";
        } else {
            return "§b🎣 §l§bReeled in: §f" + itemName + "§b!";
        }
    }
    
    private void showAchievementToasts(net.minecraft.client.MinecraftClient client, int sessionFish, int lifetimeFish) {
        // First fish achievement
        if (sessionFish == 1) {
            showToast(client, "🎣 First Catch!", "Your fishing journey begins!");
        }
        // Milestone achievements
        else if (sessionFish == 10) {
            showToast(client, "🐟 Getting Started!", "10 fish in one session!");
        }
        else if (sessionFish == 25) {
            showToast(client, "🌊 Making Waves!", "25 fish caught!");
        }
        else if (sessionFish == 50) {
            showToast(client, "⚡ Lightning Fisher!", "50 fish in one session!");
        }
        else if (sessionFish == 100) {
            showToast(client, "🏆 Fishing Master!", "100 fish caught!");
        }
        
        // Lifetime achievements
        if (lifetimeFish == 100) {
            showToast(client, "🌟 Century Club!", "100 lifetime catches!");
        }
        else if (lifetimeFish == 500) {
            showToast(client, "💎 Fishing Legend!", "500 lifetime catches!");
        }
        else if (lifetimeFish == 1000) {
            showToast(client, "👑 Angling Royalty!", "1000 lifetime catches!");
        }
    }
    
    private void showToast(net.minecraft.client.MinecraftClient client, String title, String description) {
        client.getToastManager().add(SystemToast.create(
            client,
            SystemToast.Type.NARRATOR_TOGGLE, // Using existing toast type
            Text.literal(title),
            Text.literal(description)
        ));
    }
    
    private boolean detectFishBite(FishingBobberEntity bobber, Vec3d currentPos, Vec3d currentVelocity) {
        if (biteDetectionCooldown > 0 || lastBobberPos == null || lastBobberVelocity == null) {
            return false;
        }
        
        // Enhanced bite detection with multiple methods - Made more sensitive
        
        // Method 1: Velocity-based detection (primary) - More sensitive
        double velocityChange = currentVelocity.distanceTo(lastBobberVelocity);
        boolean suddenVelocityChange = velocityChange > 0.05; // Lowered from 0.1 for faster detection
        
        // Method 2: Downward movement detection (fish pulling bobber down) - More sensitive
        boolean suddenDownwardMovement = currentVelocity.y < -0.1 && lastBobberVelocity.y > -0.03; // More sensitive thresholds
        
        // Method 3: Check if bobber is in water and moving unusually - More sensitive
        boolean inWater = bobber.isInFluid();
        boolean unusualMovement = currentVelocity.horizontalLength() > 0.03; // Lowered from 0.05
        
        // Method 4: Position-based detection (bobber being pulled) - More sensitive
        double positionChange = currentPos.distanceTo(lastBobberPos);
        boolean suddenPositionChange = positionChange > 0.1; // Lowered from 0.2
        
        // Method 5: Y-position drop detection (immediate bobber dip)
        boolean bobberDipped = currentPos.y < lastBobberPos.y - 0.05; // New: detect immediate dip
        
        // Combine detection methods for better accuracy and speed
        boolean biteDetected = inWater && (
            (suddenVelocityChange && unusualMovement) ||
            suddenDownwardMovement ||
            (suddenPositionChange && velocityChange > 0.03) ||
            bobberDipped // New method for immediate detection
        );
        
        if (biteDetected) {
            biteDetectionCooldown = 30; // Reduced from 60 (1.5 seconds instead of 3)
            return true;
        }
        
        return false;
    }
    
    private void showFishingStats(ClientPlayerEntity player) {
        long sessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000; // seconds
        int sessionMinutes = (int) (sessionTime / 60);
        
        player.sendMessage(Text.literal("§6§l=== 🎣 Feeshman Deelux Statistics ==="), false);
        player.sendMessage(Text.literal("§7Session Fish: §a" + totalFishCaught), false);
        player.sendMessage(Text.literal("§7Lifetime Fish: §a" + lifetimeFishCaught), false);
        player.sendMessage(Text.literal("§7Session Time: §a" + sessionMinutes + " minutes"), false);
        player.sendMessage(Text.literal("§7Status: " + (autoFishEnabled ? "§aEnabled" : "§cDisabled")), false);
        player.sendMessage(Text.literal("§7Use §e/feeshstats biome §7for biome breakdown"), false);
    }
    
    private void showBiomeStats(ClientPlayerEntity player) {
        player.sendMessage(Text.literal("§6§l=== 🗺️ Biome Catch Statistics ==="), false);
        
        if (biomeCatchTracker.isEmpty()) {
            player.sendMessage(Text.literal("§7No biome data yet. Start fishing to track catches!"), false);
            return;
        }
        
        // Sort biomes by catch count and show top 3
        biomeCatchTracker.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                String biomeName = entry.getKey().replace("minecraft:", "");
                int catches = entry.getValue();
                player.sendMessage(Text.literal("§7" + biomeName + ": §a" + catches + " fish"), false);
            });
    }

    private void showLeaderboard(ClientPlayerEntity player) {
        player.sendMessage(Text.literal("§6§l=== 🏆 Feeshman Leaderboard ==="), false);
        var top = FeeshLeaderboard.getTop(5);
        if (top.isEmpty()) {
            player.sendMessage(Text.literal("§7No data yet. Start fishing to populate the leaderboard!"), false);
            return;
        }
        int rank = 1;
        for (var entry : top) {
            String line = String.format("§e#%d §7%s: §a%d fish", rank, entry.getKey(), entry.getValue());
            player.sendMessage(Text.literal(line), false);
            rank++;
        }
    }
}