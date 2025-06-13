package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class FeeshmanDeeluxClient implements ClientModInitializer {
    
    // Mod state variables
    private static boolean isEnabled = false;
    private static boolean hasShownWelcome = false;
    private static int welcomeTimer = 0;
    private static int toggleCount = 0;
    private static long sessionStartTime = 0;
    
    // Configuration constants
    private static final int WELCOME_DELAY = 200; // 10 seconds at 20 TPS
    
    // Keybinding
    private static KeyBinding toggleKey;
    
    @Override
    public void onInitializeClient() {
        // Still show console message for debugging
        System.out.println("🎣 Feeshman Deelux Mod Loaded! 🎣");
        
        // Register keybinding (O key)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.feeshmandeelux.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.feeshmandeelux"
        ));
        
        sessionStartTime = System.currentTimeMillis();
        
        // Main tick event handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            // Handle welcome message
            handleWelcomeMessage(client);
            
            // Handle keybind toggle
            handleKeybindToggle(client);
        });
    }
    
    private void handleWelcomeMessage(net.minecraft.client.MinecraftClient client) {
        if (!hasShownWelcome) {
            welcomeTimer++;
            if (welcomeTimer >= WELCOME_DELAY) {
                showWelcomeMessage(client);
                hasShownWelcome = true;
            }
        }
    }
    
    private void showWelcomeMessage(net.minecraft.client.MinecraftClient client) {
        try {
            // Send messages to in-game chat instead of console
            client.player.sendMessage(Text.literal(""), false);
            
            client.player.sendMessage(
                Text.literal("═══════════════════════════════════════")
                    .formatted(Formatting.AQUA, Formatting.BOLD), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("🎣 ").formatted(Formatting.GOLD)
                    .append(Text.literal("Welcome to ").formatted(Formatting.WHITE))
                    .append(Text.literal("Feeshman Deelux").formatted(Formatting.AQUA, Formatting.BOLD))
                    .append(Text.literal("! 🎣").formatted(Formatting.GOLD)), 
                false
            );
            
            client.player.sendMessage(Text.literal(""), false);
            
            client.player.sendMessage(
                Text.literal("✨ ").formatted(Formatting.YELLOW)
                    .append(Text.literal("Thank you for using our auto-fishing mod!").formatted(Formatting.WHITE)), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("🎮 ").formatted(Formatting.GREEN)
                    .append(Text.literal("Press ").formatted(Formatting.WHITE))
                    .append(Text.literal("[O]").formatted(Formatting.GOLD, Formatting.BOLD))
                    .append(Text.literal(" to toggle auto-fishing ON/OFF").formatted(Formatting.WHITE)), 
                false
            );
            
            client.player.sendMessage(Text.literal(""), false);
            
            client.player.sendMessage(
                Text.literal("🔧 ").formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal("How it works:").formatted(Formatting.WHITE, Formatting.BOLD)), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("  • ").formatted(Formatting.GRAY)
                    .append(Text.literal("Hold a fishing rod and cast your line").formatted(Formatting.WHITE)), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("  • ").formatted(Formatting.GRAY)
                    .append(Text.literal("The mod detects fish bites automatically").formatted(Formatting.WHITE)), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("  • ").formatted(Formatting.GRAY)
                    .append(Text.literal("Reels in and recasts with human-like timing").formatted(Formatting.WHITE)), 
                false
            );
            
            client.player.sendMessage(Text.literal(""), false);
            
            client.player.sendMessage(
                Text.literal("💝 ").formatted(Formatting.LIGHT_PURPLE)
                    .append(Text.literal("Happy fishing, and may your chests be full!").formatted(Formatting.WHITE, Formatting.ITALIC)), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("═══════════════════════════════════════")
                    .formatted(Formatting.AQUA, Formatting.BOLD), 
                false
            );
            
            client.player.sendMessage(
                Text.literal("🚧 ").formatted(Formatting.YELLOW)
                    .append(Text.literal("Note: Fishing mechanics coming soon!").formatted(Formatting.WHITE, Formatting.ITALIC)), 
                false
            );
            
            System.out.println("Welcome message sent to in-game chat!");
        } catch (Exception e) {
            System.out.println("Error sending welcome message: " + e.getMessage());
        }
    }
    
    private void handleKeybindToggle(net.minecraft.client.MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            isEnabled = !isEnabled;
            toggleCount++;
            
            if (isEnabled) {
                client.player.sendMessage(
                    Text.literal("🎣 ").formatted(Formatting.GREEN)
                        .append(Text.literal("Feeshman Deelux ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("ENABLED").formatted(Formatting.GREEN, Formatting.BOLD))
                        .append(Text.literal(" - Ready to fish deluxe style!").formatted(Formatting.WHITE)), 
                    false
                );
                
                client.player.sendMessage(
                    Text.literal("⚠️ ").formatted(Formatting.YELLOW)
                        .append(Text.literal("Fishing mechanics are in development!").formatted(Formatting.WHITE)), 
                    false
                );
            } else {
                client.player.sendMessage(
                    Text.literal("🛑 ").formatted(Formatting.RED)
                        .append(Text.literal("Feeshman Deelux ").formatted(Formatting.AQUA, Formatting.BOLD))
                        .append(Text.literal("DISABLED").formatted(Formatting.RED, Formatting.BOLD))
                        .append(Text.literal(" - Manual fishing mode").formatted(Formatting.WHITE)), 
                    false
                );
                
                // Show session stats when disabling
                showSessionStats(client);
            }
        }
    }
    
    private void showSessionStats(net.minecraft.client.MinecraftClient client) {
        long sessionTime = (System.currentTimeMillis() - sessionStartTime) / 1000;
        long minutes = sessionTime / 60;
        long seconds = sessionTime % 60;
        
        client.player.sendMessage(
            Text.literal("📊 ").formatted(Formatting.AQUA)
                .append(Text.literal("Session Stats: ").formatted(Formatting.WHITE, Formatting.BOLD))
                .append(Text.literal(toggleCount + " toggles").formatted(Formatting.GREEN))
                .append(Text.literal(" in ").formatted(Formatting.WHITE))
                .append(Text.literal(minutes + "m " + seconds + "s").formatted(Formatting.YELLOW)), 
            false
        );
    }
}