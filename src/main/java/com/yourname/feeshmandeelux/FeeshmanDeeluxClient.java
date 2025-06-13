package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class FeeshmanDeeluxClient implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private boolean autoFishEnabled = false;
    private int recastDelayTicks = 0;
    private final int RECAST_DELAY = 40; // 2 seconds at 20 TPS

    // Placeholder for fishing detection logic
    private boolean shouldReelIn = false; 

    @Override
    public void onInitializeClient() {
        System.out.println("Feeshman Deelux Initializing!");

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.feeshmandeelux.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "category.feeshmandeelux.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                autoFishEnabled = !autoFishEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Feeshman Deelux " + (autoFishEnabled ? "Enabled" : "Disabled")), false);
                }
            }

            if (autoFishEnabled && client.player != null && client.world != null) {
                // Handle recasting delay
                if (recastDelayTicks > 0) {
                    recastDelayTicks--;
                    return;
                }

                ClientPlayerEntity player = client.player;
                if (player.getMainHandStack().getItem().toString().contains("fishing_rod")) {
                    if (player.fishHook == null) {
                        // Recast
                        System.out.println("Feeshman Deelux: Recasting rod.");
                        client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                        recastDelayTicks = RECAST_DELAY; 
                    } else {
                        // Placeholder for reeling in logic
                        if (shouldReelIn) {
                            System.out.println("Feeshman Deelux: Reeling in!");
                            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                            recastDelayTicks = RECAST_DELAY;
                            shouldReelIn = false;
                        }
                    }
                }
            }
        });
    }
}