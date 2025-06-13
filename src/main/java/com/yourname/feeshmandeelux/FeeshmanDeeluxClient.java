package com.yourname.feeshmandeelux;

import net.fabricmc.api.ClientModInitializer;

public class FeeshmanDeeluxClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        System.out.println("=================================");
        System.out.println("🎣 Feeshman Deelux Mod Loaded! 🎣");
        System.out.println("=================================");
        System.out.println("✨ Thank you for using our auto-fishing mod!");
        System.out.println("🎮 Press [O] to toggle auto-fishing ON/OFF");
        System.out.println("🔧 How it works:");
        System.out.println("  • Hold a fishing rod and cast your line");
        System.out.println("  • The mod detects fish bites automatically");
        System.out.println("  • Reels in and recasts with human-like timing");
        System.out.println("💝 Happy fishing, and may your chests be full!");
        System.out.println("🚧 Note: Full functionality coming soon!");
        System.out.println("=================================");
    }
}