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
import net.minecraft.util.math.Vec3d;
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
    private int lastInventoryCount = 0;
    private long sessionStartTime = 0;

    // Sound volume control (0.0 to 1.0)
    private float biteAlertVolume = 0.7f;

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
        "✨ Pure fishing magic in action!",
        "🎭 A true angling virtuoso!"
    };

    // Fishing quotes
    private final String[] FISHING_QUOTES = {
        "🎣 \"Patience is the angler's virtue.\"",
        "🌊 \"The sea rewards those who wait.\"",
        "🐟 \"Every cast is a new opportunity.\"",
        "⭐ \"Fortune favors the persistent fisher.\"",
        "🎯 \"Skill and luck dance together on the waves.\"",
        "🌅 \"Dawn brings the best catches.\"",
        "🎪 \"Fishing is the art of hope.\"",
        "💎 \"Treasures hide beneath calm waters.\"",
        "🎭 \"Every fish has a story to tell.\"",
        "🌟 \"The wise angler learns from every cast.\""
    };

    @Override
    public void onInitializeClient() {
        System.out.println("🎣 Feeshman Deelux Initializing!");

        // Register sound event
        Registry.register(Registries.SOUND_EVENT, BITE_ALERT_ID, BITE_ALERT_SOUND);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.feeshmandeelux.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.feeshmandeelux.general"
        ));

        // Register HUD renderer
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (autoFishEnabled) {
                // Render HUD counter in top-left corner
                String hudText = "🐟 " + totalFishCaught;
                context.drawText(
                    net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                    hudText,
                    4, 4,
                    0x55FF55, // Green color
                    true // Shadow
                );
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
                if (client.player != null) {
                    String status = autoFishEnabled ? "§a§lEnabled" : "§c§lDisabled";
                    client.player.sendMessage(Text.literal("🎣 §6§lFeeshman Deelux " + status), false);
                    
                    if (autoFishEnabled) {
                        fishingSessionTicks = 0;
                        totalFishCaught = 0;
                        hasWarnedDurability = false;
                        lastInventoryCount = countFishInInventory(client.player);
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
                    } else {
                        // Monitor bobber for bite detection
                        FishingBobberEntity bobber = player.fishHook;
                        Vec3d currentPos = bobber.getPos();
                        Vec3d currentVelocity = bobber.getVelocity();
                        
                        if (detectFishBite(bobber, currentPos, currentVelocity)) {
                            // Fish bite detected!
                            System.out.println("🐟 Feeshman Deelux: Fish bite detected!");
                            
                            // Play bite alert sound with configurable volume
                            if (client.world != null && client.player != null) {
                                client.world.playSound(
                                    client.player,
                                    client.player.getBlockPos(),
                                    BITE_ALERT_SOUND,
                                    SoundCategory.PLAYERS,
                                    biteAlertVolume, // Configurable volume
                                    1.0f  // Pitch
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

        // Register /feeshstats command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
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
    
    private void checkForNewItems(ClientPlayerEntity player) {
        int currentCount = countFishInInventory(player);
        if (currentCount > lastInventoryCount) {
            // Find the new item(s)
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (!stack.isEmpty() && isFishingLoot(stack)) {
                    announceNewItem(player, stack);
                    break; // Announce first new item found
                }
            }
        }
        lastInventoryCount = currentCount;
    }
    
    private int countFishInInventory(ClientPlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && isFishingLoot(stack)) {
                count += stack.getCount();
            }
        }
        return count;
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
        // Rare items get special messages
        if (stack.getItem() == Items.ENCHANTED_BOOK) {
            return "📚✨ Ancient knowledge surfaces: " + itemName + "!";
        } else if (stack.getItem() == Items.NAME_TAG) {
            return "🏷️🌟 A mysterious tag emerges: " + itemName + "!";
        } else if (stack.getItem() == Items.SADDLE) {
            return "🐎⚡ Adventure gear acquired: " + itemName + "!";
        } else if (stack.getItem() == Items.NAUTILUS_SHELL) {
            return "🐚💎 Rare ocean treasure: " + itemName + "!";
        } else if (stack.getItem() == Items.COD || stack.getItem() == Items.SALMON || 
                   stack.getItem() == Items.TROPICAL_FISH || stack.getItem() == Items.PUFFERFISH) {
            return "🐟 Fresh catch: " + itemName + "!";
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