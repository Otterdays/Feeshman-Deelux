package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class FeeshmanDeeluxClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private boolean autoFishEnabled = false;
    private int recastDelayTicks = 0;
    private final int BASE_RECAST_DELAY = 40; // 2 seconds at 20 TPS
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

    // Enhanced bobber stuck detection
    private Vec3d bobberStuckCheckPos = null;
    private int bobberStuckTicks = 0;
    private final int BOBBER_STUCK_THRESHOLD = 200; // 10 seconds without significant movement
    private final double STUCK_MOVEMENT_THRESHOLD = 0.5; // Larger threshold for stuck detection
    private boolean hasWarnedAboutMob = false;

    // Human-like timing - Made faster and more responsive
    private int humanReactionDelay = 0;
    private final int MIN_REACTION_TIME = 3; // 0.15 seconds (was 0.5s)
    private final int MAX_REACTION_TIME = 12; // 0.6 seconds (was 1.5s)

    // Welcome message system
    private boolean hasShownWelcomeMessage = false;
    private int welcomeMessageDelay = 100; // 5 seconds at 20 TPS
    private int welcomeMessageTimer = 0;

    // New feature variables
    private boolean hasWarnedDurability = false;
    private Map<String, Integer> biomeCatchTracker = new HashMap<>();
    private List<ItemStack> previousInventorySnapshot = new ArrayList<>();
    private long sessionStartTime = 0;

    // Sound volume control (0.0 to 1.0)
    private static float biteAlertVolume = 0.7f;
    private static boolean staticAutoFishEnabled = false;

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

    @Override
    public void onInitializeClient() {
        System.out.println("🎣 Feeshman Deelux Initializing!");

        // Load configuration
        FeeshmanConfig.load();
        biteAlertVolume = FeeshmanConfig.getBiteAlertVolume();

        // Register sound event
        Registry.register(Registries.SOUND_EVENT, BITE_ALERT_ID, BITE_ALERT_SOUND);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.feeshmandeelux.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.feeshmandeelux.general"
        ));

        // Register enhanced HUD renderer
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
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
                    client.player.sendMessage(Text.literal("🎣 §6§lFeeshman Deelux §r§7is ready! Press §a§lO§r§7 to toggle auto-fishing. §e✨"), false);
                    client.player.sendMessage(Text.literal("§7🐟 Advanced bite detection, smart timing, and bite alerts included! Happy fishing! 🌊"), false);
                }
            }

            if (toggleKey.wasPressed()) {
                autoFishEnabled = !autoFishEnabled;
                staticAutoFishEnabled = autoFishEnabled;
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
                
                if (humanReactionDelay > 0) {
                    humanReactionDelay--;
                    if (humanReactionDelay == 0 && client.player.fishHook != null) {
                        // Time to reel in the fish! Use proper right-click simulation
                        System.out.println("🎣 Feeshman Deelux: Reeling in fish!");
                        
                        // Proper right-click simulation for fishing - reel in the fish
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        
                        recastDelayTicks = BASE_RECAST_DELAY + random.nextInt(40); // 2-4 seconds
                        totalFishCaught++;
                        lifetimeFishCaught++;
                        
                        // Track biome catch
                        trackBiomeCatch(client);
                        
                        // Check for new items and announce them
                        checkForNewItems(client.player);
                        
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
                        System.out.println("🎣 Feeshman Deelux: Recasting rod...");
                        
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
                            System.out.println("🎣 Feeshman Deelux: Bobber appears stuck, recasting...");
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
                            System.out.println("🐟 Feeshman Deelux: Fish bite detected!");
                            
                            // Play bite alert sound with configurable volume
                            if (client.world != null && client.player != null) {
                                client.world.playSound(
                                    client.player.getX(),
                                    client.player.getY(), 
                                    client.player.getZ(),
                                    BITE_ALERT_SOUND,
                                    SoundCategory.PLAYERS,
                                    FeeshmanConfig.getBiteAlertVolume(), // Configurable volume
                                    1.0f,  // Pitch
                                    false  // Use distance
                                );
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
        });
    }
    
    private void renderPolishedHUD(DrawContext context) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        var textRenderer = client.textRenderer;
        
        // HUD dimensions and positioning
        int hudX = 4;
        int hudY = 4;
        int hudWidth = 140;
        int hudHeight = 78;
        int borderColor = 0x88000000; // Semi-transparent black
        int titleBgColor = 0xAA006699; // Semi-transparent blue
        int contentBgColor = 0x77000000; // Darker semi-transparent
        
        // Draw main background with border
        context.fill(hudX - 2, hudY - 2, hudX + hudWidth + 2, hudY + hudHeight + 2, borderColor);
        context.fill(hudX, hudY, hudX + hudWidth, hudY + hudHeight, contentBgColor);
        
        // Draw title background
        context.fill(hudX, hudY, hudX + hudWidth, hudY + 14, titleBgColor);
        
        // Title header
        String title = "§l§fFeeshman!";
        int titleWidth = textRenderer.getWidth("Feeshman!");
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        context.drawText(textRenderer, title, titleX, hudY + 3, 0xFFFFFF, true);
        
        // Content area starts below title
        int contentY = hudY + 18;
        
        // Main fish counter
        String fishText = "🐟 " + totalFishCaught;
        context.drawText(textRenderer, fishText, hudX + 4, contentY, 0x55FF55, true);
        
        // Session time
        int sessionMinutes = fishingSessionTicks / 1200;
        int sessionSeconds = (fishingSessionTicks % 1200) / 20;
        String timeText = String.format("⏰ %02d:%02d", sessionMinutes, sessionSeconds);
        context.drawText(textRenderer, timeText, hudX + 4, contentY + 12, 0xFFFF55, true);
        
        // Rod durability
        if (client.player != null) {
            ItemStack rod = client.player.getMainHandStack();
            if (rod.getItem() == Items.FISHING_ROD) {
                int maxDurability = rod.getMaxDamage();
                int currentDamage = rod.getDamage();
                int remainingUses = maxDurability - currentDamage;
                int durabilityPercent = (remainingUses * 100) / maxDurability;
                
                String durabilityText = "🔧 " + remainingUses + " (" + durabilityPercent + "%)";
                int color = durabilityPercent > 50 ? 0x55FF55 : durabilityPercent > 20 ? 0xFFFF55 : 0xFF5555;
                context.drawText(textRenderer, durabilityText, hudX + 4, contentY + 24, color, true);
            }
        }
        
        // Current biome
        if (client.player != null && client.world != null) {
            RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
            String biomeName = biome.getKey().map(key -> key.getValue().toString()).orElse("unknown");
            biomeName = biomeName.replace("minecraft:", "").replace("_", " ");
            String biomeText = "🗺️ " + capitalizeWords(biomeName);
            context.drawText(textRenderer, biomeText, hudX + 4, contentY + 36, 0x55FFFF, true);
        }
        
        // Status indicator
        String statusText = "🎣 Active";
        context.drawText(textRenderer, statusText, hudX + 4, contentY + 48, 0x55FF55, true);
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
            return false;
        }
        
        // Check if bobber has moved significantly (even more lenient threshold)
        double distance = currentPos.distanceTo(bobberStuckCheckPos);
        if (distance < STUCK_MOVEMENT_THRESHOLD) { // 0.5 blocks
            bobberStuckTicks++;
            // Only consider stuck after 15 seconds (300 ticks) of no movement
            if (bobberStuckTicks >= 300) { // Increased from 200 to 300 ticks (15 seconds)
                return true; // Bobber is stuck
            }
        } else {
            // Bobber moved significantly, reset tracking
            bobberStuckCheckPos = currentPos;
            bobberStuckTicks = 0;
        }
        
        return false;
    }
    

    
    private void showFeeshmanHelp(ClientPlayerEntity player) {
        player.sendMessage(Text.literal("§6§l=== 🎣 Feeshman Deelux Help ==="), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§e§lCommands:"), false);
        player.sendMessage(Text.literal("§a/feeshman §7- §fShow this help message"), false);
        player.sendMessage(Text.literal("§a/feeshstats §7- §fView fishing statistics"), false);
        player.sendMessage(Text.literal("§a/feeshstats biome §7- §fView biome catch breakdown"), false);
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
        previousInventorySnapshot.clear();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                previousInventorySnapshot.add(stack.copy());
            }
        }
    }
    
    private void checkForNewItems(ClientPlayerEntity player) {
        // Get current inventory
        List<ItemStack> currentInventory = new ArrayList<>();
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                currentInventory.add(stack.copy());
            }
        }
        
        // Find new items by comparing with previous snapshot
        for (ItemStack currentStack : currentInventory) {
            if (isFishingLoot(currentStack)) {
                boolean foundInPrevious = false;
                int currentCount = currentStack.getCount();
                
                // Check if this item existed before and in what quantity
                for (ItemStack previousStack : previousInventorySnapshot) {
                    if (ItemStack.areItemsEqual(currentStack, previousStack)) {
                        if (currentStack.getCount() > previousStack.getCount()) {
                            // Item count increased, announce the difference
                            int newItems = currentStack.getCount() - previousStack.getCount();
                            for (int i = 0; i < newItems; i++) {
                                announceNewItem(player, currentStack);
                            }
                        }
                        foundInPrevious = true;
                        break;
                    }
                }
                
                // If item wasn't found in previous inventory, it's completely new
                if (!foundInPrevious) {
                    for (int i = 0; i < currentCount; i++) {
                        announceNewItem(player, currentStack);
                    }
                }
            }
        }
        
        // Update snapshot for next comparison
        takeInventorySnapshot(player);
    }
    
    private boolean isFishingLoot(ItemStack stack) {
        return stack.getItem() == Items.COD || stack.getItem() == Items.SALMON || 
               stack.getItem() == Items.TROPICAL_FISH || stack.getItem() == Items.PUFFERFISH ||
               stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.NAME_TAG ||
               stack.getItem() == Items.SADDLE || stack.getItem() == Items.NAUTILUS_SHELL ||
               stack.getItem() == Items.LEATHER_BOOTS || stack.getItem() == Items.LEATHER ||
               stack.getItem() == Items.BONE || stack.getItem() == Items.STRING ||
               stack.getItem() == Items.STICK || stack.getItem() == Items.BOWL ||
               stack.getItem() == Items.ROTTEN_FLESH;
    }
    
    private void announceNewItem(ClientPlayerEntity player, ItemStack stack) {
        String itemName = stack.getName().getString();
        String message = getItemMessage(stack, itemName);
        player.sendMessage(Text.literal(message), false);
    }
    
    private String getItemMessage(ItemStack stack, String itemName) {
        // Use the actual item to determine the message, not just the name
        if (stack.getItem() == Items.ENCHANTED_BOOK) {
            return "📚✨ Ancient knowledge surfaces: " + itemName + "!";
        } else if (stack.getItem() == Items.NAME_TAG) {
            return "🏷️🌟 A mysterious tag emerges: " + itemName + "!";
        } else if (stack.getItem() == Items.SADDLE) {
            return "🐎⚡ Adventure gear acquired: " + itemName + "!";
        } else if (stack.getItem() == Items.NAUTILUS_SHELL) {
            return "🐚💎 Rare ocean treasure: " + itemName + "!";
        } else if (stack.getItem() == Items.COD) {
            return "🐟 Fresh cod caught: " + itemName + "!";
        } else if (stack.getItem() == Items.SALMON) {
            return "🍣 Salmon secured: " + itemName + "!";
        } else if (stack.getItem() == Items.TROPICAL_FISH) {
            return "🌺 Tropical beauty: " + itemName + "!";
        } else if (stack.getItem() == Items.PUFFERFISH) {
            return "🐡 Spiky surprise: " + itemName + "!";
        } else if (stack.getItem() == Items.BONE) {
            return "🦴 Skeletal remains: " + itemName + "!";
        } else if (stack.getItem() == Items.LEATHER_BOOTS) {
            return "👢 Waterlogged boots: " + itemName + "!";
        } else if (stack.getItem() == Items.LEATHER) {
            return "🧳 Leather scraps: " + itemName + "!";
        } else if (stack.getItem() == Items.STRING) {
            return "🧵 Tangled string: " + itemName + "!";
        } else if (stack.getItem() == Items.STICK) {
            return "🪵 Driftwood stick: " + itemName + "!";
        } else if (stack.getItem() == Items.BOWL) {
            return "🥣 Floating bowl: " + itemName + "!";
        } else if (stack.getItem() == Items.ROTTEN_FLESH) {
            return "🧟 Questionable meat: " + itemName + "!";
        } else {
            return "🎣 Reeled in: " + itemName + "!";
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
}