package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.network.FeeshmanPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeeshmanDeeluxClient implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    private static FeeshmanDeeluxClient instance;

    private static KeyBinding toggleKey;
    private boolean autoFishEnabled = false;

    // Sound events (optional client bite alert)
    public static final Identifier BITE_ALERT_ID = Identifier.of("feeshmandeelux", "bite_alert");
    public static final SoundEvent BITE_ALERT_SOUND = SoundEvent.of(BITE_ALERT_ID);

    // Client UX state for HUD (server is authoritative; these are placeholders when no sync)
    private int fishingSessionTicks = 0;
    private int totalFishCaught = 0;
    private int lifetimeFishCaught = 0;
    private int biomeCount = 0;
    private long sessionStartTime = 0;

    // Welcome message
    private boolean hasShownWelcomeMessage = false;
    private int welcomeMessageDelay = 100;
    private int welcomeMessageTimer = 0;

    // Fishing quotes
    private final String[] FISHING_QUOTES = {
        "\"Patience is the angler's virtue.\"",
        "\"The sea rewards those who wait.\"",
        "\"Every cast is a new adventure.\"",
        "\"Fortune favors the persistent fisher.\"",
        "\"Dawn brings the best catches.\"",
        "\"Skill and luck dance together on the water.\"",
        "\"Treasures hide beneath calm waters.\"",
        "\"Night fishing reveals hidden wonders.\"",
        "\"The depths hold ancient secrets.\"",
        "\"Master anglers are made, not born.\""
    };

    public static int getSessionFishCount() {
        return instance != null ? instance.totalFishCaught : 0;
    }

    public static int getBiomesVisitedCount() {
        return instance != null ? instance.biomeCount : 0;
    }

    public static long getSessionStartTime() {
        return instance != null ? instance.sessionStartTime : 0;
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Feeshman Deelux Initializing!");

        instance = this;
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
                KeyBinding.Category.MISC
        ));

        // Register enhanced HUD renderer (will migrate to HudElementRegistry when available in Fabric API)
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
            if (client.player != null) {
                lifetimeFishCaught = FeeshLeaderboard.getPlayerTotal(client.player.getName().getString());
            }
            
            // Show random fishing quote on join
            if (ThreadLocalRandom.current().nextFloat() < 0.3f) { // 30% chance
                String quote = FISHING_QUOTES[ThreadLocalRandom.current().nextInt(FISHING_QUOTES.length)];
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§7" + quote), false);
                    }
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FeeshLeaderboard.flushIfDirty());

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.FishCaughtPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (instance != null) {
                    instance.totalFishCaught = payload.sessionFish();
                    instance.lifetimeFishCaught = payload.lifetimeFish();
                    if (context.client().player != null) {
                        context.client().player.playSound(BITE_ALERT_SOUND, 0.5f, 1.0f);
                        if (!payload.luckyCompliment().isEmpty()) {
                            context.client().player.sendMessage(Text.literal("§6§l" + payload.luckyCompliment()), false);
                        }
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.StatsSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (instance != null) {
                    instance.totalFishCaught = payload.sessionFish();
                    instance.lifetimeFishCaught = payload.lifetimeFish();
                    instance.sessionStartTime = payload.sessionStartTime();
                    instance.biomeCount = payload.biomeCount();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.ItemAnnouncementPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player != null) {
                    String msg = formatItemAnnouncement(payload.itemId(), payload.hasEnchantments());
                    if (msg != null) {
                        context.client().player.sendMessage(Text.literal(msg), false);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.DurabilityWarningPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var manager = context.client().getToastManager();
                if (manager != null) {
                    SystemToast.show(manager, SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal("Rod Durability Low"),
                            Text.literal("Only " + payload.remainingUses() + " uses left!"));
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!hasShownWelcomeMessage && client.player != null && welcomeMessageTimer > 0) {
                welcomeMessageTimer--;
                if (welcomeMessageTimer <= 0) {
                    hasShownWelcomeMessage = true;
                    showEnhancedWelcomeMessage(client.player);
                }
            }

            if (toggleKey.wasPressed() && client.player != null && client.getNetworkHandler() != null) {
                autoFishEnabled = !autoFishEnabled;
                client.getNetworkHandler().sendChatCommand(autoFishEnabled ? "feeshman enable" : "feeshman disable");
            }
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
        
        // Enhanced title header with compatible unicode and styling
        String title = "⚡ Feeshman Deelux";
        int titleWidth = textRenderer.getWidth(title);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        // Draw title with elegant styling
        context.drawText(textRenderer, title, titleX, hudY + 6, 0xFFFFFFFF, true); // Bright white text
        
        // Content area starts below title with better spacing
        int contentY = hudY + 26;
        int lineHeight = 14; // Better line spacing
        int currentLine = 0;
        
        // Fish counter (server has real stats; client shows placeholder or hint)
        String fishText = totalFishCaught > 0
                ? String.format("◆ Fish: %d caught", totalFishCaught)
                : "◆ Fish: Use /feeshstats for stats";
        int fishColor = 0xFF4AE54A; // Elegant bright green
        context.drawText(textRenderer, fishText, hudX + 8, contentY + (currentLine * lineHeight), fishColor, true);
        currentLine++;
        
        // Enhanced session time with better formatting and safe unicode
        int sessionTicks = sessionStartTime > 0
                ? (int) ((System.currentTimeMillis() - sessionStartTime) / 50)
                : fishingSessionTicks;
        int sessionMinutes = sessionTicks / 1200;
        int sessionSeconds = (sessionTicks % 1200) / 20;
        String timeText = String.format("● Time: %02d:%02d session", sessionMinutes, sessionSeconds);
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
                
                String durabilityText = String.format("▲ Rod: %d uses (%d%%)", remainingUses, durabilityPercent);
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
            // Weather indicator with enhanced styling and safe unicode
            String weatherText = client.world.isRaining() ? 
                (client.world.isThundering() ? "♦ Weather: Thunder" : "♦ Weather: Rainy") : 
                "♦ Weather: Clear";
            int weatherColor = client.world.isRaining() ? 0xFF4A9AFF : 0xFFFFD700;
            context.drawText(textRenderer, weatherText, hudX + 8, contentY + (currentLine * lineHeight), weatherColor, true);
            currentLine++;
            
            // Enhanced day/night and moon phase indicator with better spacing
            long timeOfDay = client.world.getTimeOfDay() % 24000;
            boolean isDay = timeOfDay < 12000;
            String dayNightText = isDay ? "☀ Time: Day" : "☽ Time: Night";
            
            if (!isDay) {
                dayNightText = "☽ Time: Night";
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
            String biomeText = String.format("▼ Biome: %s", capitalizeWords(biomeName));
            
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
        int rateTicks = sessionStartTime > 0 ? (int) ((System.currentTimeMillis() - sessionStartTime) / 50) : fishingSessionTicks;
        if (rateTicks > 0) {
            float catchRate = (float) totalFishCaught / (rateTicks / 1200.0f); // fish per minute
            String efficiency = catchRate > 2.0f ? "Excellent" : catchRate > 1.0f ? "Good" : catchRate > 0.5f ? "Fair" : "Slow";
            String rateText = String.format("✦ Rate: %.1f/min (%s)", Math.max(0, catchRate), efficiency);
            int rateColor = catchRate > 2.0f ? 0xFF4AE54A : catchRate > 1.0f ? 0xFF9ACD32 : catchRate > 0.5f ? 0xFFFFB347 : 0xFFFF7F50;
            context.drawText(textRenderer, rateText, hudX + 8, contentY + (currentLine * lineHeight), rateColor, true);
            currentLine++;
        }
        
        // Status indicator (server-authoritative; client tracks optimistic toggle)
        String statusText = autoFishEnabled ? "◈ Status: Server Auto-Fishing" : "◈ Status: Manual (Press O for auto)";
        int statusColor = autoFishEnabled ? 0xFF4AE54A : 0xFFDDA0DD;
        context.drawText(textRenderer, statusText, hudX + 8, contentY + (currentLine * lineHeight), statusColor, true);
        currentLine++;
        
        // Add lifetime stats with better styling if available
        if (lifetimeFishCaught > 0) {
            String lifetimeText = String.format("★ Lifetime: %d total catches", lifetimeFishCaught);
            context.drawText(textRenderer, lifetimeText, hudX + 8, contentY + (currentLine * lineHeight), 0xFFFFD700, true);
        }
        
        // HUD rendering complete
    }
    
    private static final TagKey<Item> TAG_TREASURE = TagKey.of(RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "treasure"));
    private static final TagKey<Item> TAG_JUNK = TagKey.of(RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "junk"));

    private static String formatItemAnnouncement(String itemId, boolean hasEnchantments) {
        var item = Registries.ITEM.get(Identifier.tryParse(itemId));
        if (item == null || item == net.minecraft.item.Items.AIR) return null;
        ItemStack stack = new ItemStack(item);
        String name = item.getName().getString();
        if (stack.isIn(TAG_TREASURE)) {
            return "§6§l✨ " + (hasEnchantments ? "§d" : "§6") + name + " §6§l✨";
        }
        if (stack.isIn(TAG_JUNK)) {
            return "§7▸ " + name;
        }
        return "§a▸ " + name;
    }

    private static void showAchievementToastIfMilestone(MinecraftClient client, int prevSession, int prevLifetime,
                                                       int sessionFish, int lifetimeFish) {
        var manager = client.getToastManager();
        if (manager == null) return;
        String title = null;
        String desc = null;
        int[] sessionMilestones = {1, 10, 25, 50, 100};
        int[] lifetimeMilestones = {100, 500, 1000};
        for (int m : sessionMilestones) {
            if (prevSession < m && sessionFish >= m) {
                title = "Fishing Milestone!";
                desc = m + " fish this session!";
                break;
            }
        }
        if (title == null) {
            for (int m : lifetimeMilestones) {
                if (prevLifetime < m && lifetimeFish >= m) {
                    title = "Lifetime Achievement!";
                    desc = m + " fish caught total!";
                    break;
                }
            }
        }
        if (title != null && desc != null) {
            SystemToast.show(manager, SystemToast.Type.PERIODIC_NOTIFICATION,
                    Text.literal(title), Text.literal(desc));
        }
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
    
}
