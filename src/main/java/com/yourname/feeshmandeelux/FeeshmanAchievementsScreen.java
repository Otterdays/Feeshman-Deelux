package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FeeshmanAchievementsScreen extends Screen {
    private final Screen parent;
    private ButtonWidget backButton;
    
    // Achievement data
    private static final Achievement[] ACHIEVEMENTS = {
        new Achievement("🎣 First Cast", "Catch your first fish", 1, "§a✓"),
        new Achievement("🐟 Getting Started", "Catch 10 fish in one session", 10, "§a✓"),
        new Achievement("🌊 Making Waves", "Catch 25 fish in one session", 25, "§e⭐"),
        new Achievement("⚡ Lightning Fisher", "Catch 50 fish in one session", 50, "§6⭐⭐"),
        new Achievement("🏆 Fishing Master", "Catch 100 fish in one session", 100, "§c⭐⭐⭐"),
        new Achievement("🌟 Century Club", "Catch 100 fish lifetime", 100, "§d💎"),
        new Achievement("💎 Fishing Legend", "Catch 500 fish lifetime", 500, "§b💎💎"),
        new Achievement("👑 Angling Royalty", "Catch 1000 fish lifetime", 1000, "§6👑"),
        new Achievement("🎯 Treasure Hunter", "Find your first treasure", 1, "§6🏆"),
        new Achievement("🗺️ World Explorer", "Fish in 5 different biomes", 5, "§a🌍"),
    };
    
    public FeeshmanAchievementsScreen(Screen parent) {
        super(Text.literal("Feeshman Deelux Achievements"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        
        // Back button
        this.backButton = ButtonWidget.builder(Text.literal("← Back"), button -> {
            this.close();
        })
        .dimensions(centerX - 50, this.height - 40, 100, 20)
        .build();
        
        addDrawableChild(backButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw solid background to prevent blur
        context.fill(0, 0, this.width, this.height, 0xE0101010);
        
        // Draw enhanced title with better contrast
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("🏆 Feeshman Deelux Achievements"), 
            this.width / 2, 20, 0xFFD700);
        
        // Draw subtitle with better visibility
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Your Fishing Journey & Milestones").formatted(Formatting.GRAY), 
            this.width / 2, 35, 0xCCCCCC);
        
        // Get current stats
        int lifetimeFish = getCurrentLifetimeFish();
        int sessionFish = getCurrentSessionFish();
        int biomesVisited = getBiomesVisited();
        
        // Draw current stats panel with better contrast
        int statsY = 60;
        context.fill(this.width / 2 - 150, statsY, this.width / 2 + 150, statsY + 60, 0xC0000000);
        context.drawBorder(this.width / 2 - 150, statsY, 300, 60, 0xFF666666);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("📊 Current Statistics").formatted(Formatting.YELLOW), 
            this.width / 2, statsY + 8, 0xFFFF55);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("🐟 Lifetime: " + lifetimeFish + " fish  •  📈 Session: " + sessionFish + " fish  •  🗺️ Biomes: " + biomesVisited), 
            this.width / 2, statsY + 25, 0xFFFFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("⏰ Session Time: " + getFormattedSessionTime()).formatted(Formatting.AQUA), 
            this.width / 2, statsY + 40, 0x55FFFF);
        
        // Draw achievements grid
        int startY = 140;
        int achievementHeight = 25;
        int maxVisible = (this.height - startY - 60) / achievementHeight;
        
        for (int i = 0; i < Math.min(ACHIEVEMENTS.length, maxVisible); i++) {
            Achievement achievement = ACHIEVEMENTS[i];
            int y = startY + i * achievementHeight;
            
            // Check if achievement is unlocked
            boolean unlocked = isAchievementUnlocked(achievement, lifetimeFish, sessionFish, biomesVisited);
            
            // Draw achievement background with enhanced colors
            int bgColor = unlocked ? 0xB0002200 : 0xB0220000; // Darker, more opaque backgrounds
            int borderColor = unlocked ? 0xFF00CC00 : 0xFF880000; // Brighter borders
            
            context.fill(this.width / 2 - 200, y, this.width / 2 + 200, y + 20, bgColor);
            context.drawBorder(this.width / 2 - 200, y, 400, 20, borderColor);
            
            // Draw achievement icon and text with better contrast
            String status = unlocked ? achievement.icon : "❌";
            int titleColor = unlocked ? 0x88FF88 : 0xFF8888; // Light green/red
            int descColor = unlocked ? 0xFFFFFF : 0xCCCCCC; // White/light gray
            
            // Draw icon
            context.drawTextWithShadow(this.textRenderer, 
                Text.literal(status), 
                this.width / 2 - 190, y + 6, unlocked ? 0x00FF00 : 0xFF4444);
            
            // Draw title
            context.drawTextWithShadow(this.textRenderer, 
                Text.literal(achievement.title), 
                this.width / 2 - 170, y + 3, titleColor);
            
            // Draw description
            context.drawTextWithShadow(this.textRenderer, 
                Text.literal(achievement.description), 
                this.width / 2 - 170, y + 12, descColor);
            
            // Draw progress for all achievements with enhanced styling
            int current = getCurrentProgress(achievement, lifetimeFish, sessionFish, biomesVisited);
            String progress = "(" + current + "/" + achievement.requirement + ")";
            int progressColor = unlocked ? 0x00FF00 : 0xFFCC00; // Green for completed, gold for in progress
            context.drawTextWithShadow(this.textRenderer, 
                Text.literal(progress), 
                this.width / 2 + 120, y + 7, progressColor);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private boolean isAchievementUnlocked(Achievement achievement, int lifetimeFish, int sessionFish, int biomesVisited) {
        switch (achievement.title) {
            case "🎣 First Cast":
            case "🌟 Century Club":
            case "💎 Fishing Legend":
            case "👑 Angling Royalty":
                return lifetimeFish >= achievement.requirement;
            case "🐟 Getting Started":
            case "🌊 Making Waves":
            case "⚡ Lightning Fisher":
            case "🏆 Fishing Master":
                return sessionFish >= achievement.requirement;
            case "🗺️ World Explorer":
                return biomesVisited >= achievement.requirement;
            case "🎯 Treasure Hunter":
                return lifetimeFish > 0; // Simplified - assume they've found treasure if they've caught fish
            default:
                return false;
        }
    }
    
    private int getCurrentProgress(Achievement achievement, int lifetimeFish, int sessionFish, int biomesVisited) {
        switch (achievement.title) {
            case "🎣 First Cast":
            case "🌟 Century Club":
            case "💎 Fishing Legend":
            case "👑 Angling Royalty":
                return lifetimeFish;
            case "🐟 Getting Started":
            case "🌊 Making Waves":
            case "⚡ Lightning Fisher":
            case "🏆 Fishing Master":
                return sessionFish;
            case "🗺️ World Explorer":
                return biomesVisited;
            default:
                return 0;
        }
    }
    
    private int getCurrentLifetimeFish() {
        // Get lifetime fish from leaderboard for current player
        if (this.client != null && this.client.player != null) {
            String playerName = this.client.player.getName().getString();
            List<Map.Entry<String, Integer>> leaderboard = FeeshLeaderboard.getTop(100);
            for (Map.Entry<String, Integer> entry : leaderboard) {
                if (entry.getKey().equals(playerName)) {
                    return entry.getValue();
                }
            }
        }
        return 0;
    }
    
    private int getCurrentSessionFish() {
        // This would need to be implemented to get current session fish count
        // For now, return 0 as placeholder
        return 0;
    }
    
    private int getBiomesVisited() {
        // This would need to be implemented to get biomes visited count
        // For now, return 0 as placeholder
        return 0;
    }
    
    private String getFormattedSessionTime() {
        // This would need to be implemented to get formatted session time
        // For now, return placeholder
        return "00:00";
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private static class Achievement {
        final String title;
        final String description;
        final int requirement;
        final String icon;
        
        Achievement(String title, String description, int requirement, String icon) {
            this.title = title;
            this.description = description;
            this.requirement = requirement;
            this.icon = icon;
        }
    }
} 