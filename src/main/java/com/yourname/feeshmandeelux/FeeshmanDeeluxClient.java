package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
import org.lwjgl.glfw.GLFW;
import java.util.Random;

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

    // Human-like timing
    private int humanReactionDelay = 0;
    private final int MIN_REACTION_TIME = 10; // 0.5 seconds
    private final int MAX_REACTION_TIME = 30; // 1.5 seconds

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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                autoFishEnabled = !autoFishEnabled;
                if (client.player != null) {
                    String status = autoFishEnabled ? "§a§lEnabled" : "§c§lDisabled";
                    client.player.sendMessage(Text.literal("🎣 §6§lFeeshman Deelux " + status), false);
                    
                    if (autoFishEnabled) {
                        fishingSessionTicks = 0;
                        client.player.sendMessage(Text.literal("§7Press O again to disable. Happy fishing! 🐟"), false);
                    } else {
                        int sessionMinutes = fishingSessionTicks / 1200; // 20 TPS * 60 seconds
                        client.player.sendMessage(Text.literal("§7Session: " + sessionMinutes + " minutes, " + totalFishCaught + " fish caught! 📊"), false);
                    }
                }
            }

            if (autoFishEnabled && client.player != null && client.world != null) {
                fishingSessionTicks++;
                
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
                        // Time to reel in the fish!
                        System.out.println("🎣 Feeshman Deelux: Reeling in fish!");
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        recastDelayTicks = BASE_RECAST_DELAY + random.nextInt(40); // 2-4 seconds
                        totalFishCaught++;
                        
                        // Show catch notification
                        if (totalFishCaught % 5 == 0) {
                            client.player.sendMessage(Text.literal("🐟 §a" + totalFishCaught + " fish caught this session! §7Keep it up!"), false);
                        }
                    }
                    return;
                }

                ClientPlayerEntity player = client.player;
                
                // Check if player has fishing rod
                if (player.getMainHandStack().getItem().toString().contains("fishing_rod")) {
                    if (player.fishHook == null) {
                        // Auto-recast with randomized delay
                        System.out.println("🎣 Feeshman Deelux: Recasting rod...");
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
                            
                            // Play bite alert sound
                            if (client.world != null && client.player != null) {
                                client.world.playSound(
                                    client.player,
                                    client.player.getBlockPos(),
                                    BITE_ALERT_SOUND,
                                    SoundCategory.PLAYERS,
                                    0.7f, // Volume
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
    }
    
    private boolean detectFishBite(FishingBobberEntity bobber, Vec3d currentPos, Vec3d currentVelocity) {
        if (biteDetectionCooldown > 0 || lastBobberPos == null || lastBobberVelocity == null) {
            return false;
        }
        
        // Method 1: Velocity-based detection (primary)
        double velocityChange = currentVelocity.distanceTo(lastBobberVelocity);
        boolean suddenVelocityChange = velocityChange > 0.1; // Threshold for bite detection
        
        // Method 2: Downward movement detection
        boolean suddenDownwardMovement = currentVelocity.y < -0.15 && lastBobberVelocity.y > -0.05;
        
        // Method 3: Check if bobber is in water and moving unusually
        boolean inWater = bobber.isInFluid();
        boolean unusualMovement = currentVelocity.horizontalLength() > 0.05;
        
        if ((suddenVelocityChange || suddenDownwardMovement) && inWater) {
            biteDetectionCooldown = 60; // 3 second cooldown to prevent spam detection
            return true;
        }
        
        return false;
    }
    

}