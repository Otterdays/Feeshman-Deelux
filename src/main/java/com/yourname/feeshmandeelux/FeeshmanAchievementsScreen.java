package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FeeshmanAchievementsScreen extends Screen {
    private final Screen parent;
    private Button backButton;

    private static final Achievement[] ACHIEVEMENTS = {
            new Achievement("🎣 First Cast", "Catch your first fish", 1, "§a✓", FeeshAchievementIds.FIRST_CAST),
            new Achievement("🐟 Getting Started", "Catch 10 fish in one session", 10, "§a✓", FeeshAchievementIds.SESSION_10),
            new Achievement("🌊 Making Waves", "Catch 25 fish in one session", 25, "§e⭐", FeeshAchievementIds.SESSION_25),
            new Achievement("⚡ Lightning Fisher", "Catch 50 fish in one session", 50, "§6⭐⭐", FeeshAchievementIds.SESSION_50),
            new Achievement("🏆 Fishing Master", "Catch 100 fish in one session", 100, "§c⭐⭐⭐", FeeshAchievementIds.SESSION_100),
            new Achievement("🌟 Century Club", "Catch 100 fish lifetime", 100, "§d💎", FeeshAchievementIds.LIFETIME_100),
            new Achievement("💎 Fishing Legend", "Catch 500 fish lifetime", 500, "§b💎💎", FeeshAchievementIds.LIFETIME_500),
            new Achievement("👑 Angling Royalty", "Catch 1000 fish lifetime", 1000, "§6👑", FeeshAchievementIds.LIFETIME_1000),
            new Achievement("🎯 Treasure Hunter", "Find your first treasure", 1, "§6🏆", FeeshAchievementIds.TREASURE_HUNTER),
            new Achievement("🗺️ World Explorer", "Fish in 5 different biomes", 5, "§a🌍", FeeshAchievementIds.WORLD_EXPLORER),
    };

    public FeeshmanAchievementsScreen(Screen parent) {
        super(Component.literal("Feeshman Deelux Achievements"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.backButton = Button.builder(Component.literal("← Back"), button -> this.onClose())
                .bounds(centerX - 50, this.height - 40, 100, 20)
                .build();

        addRenderableWidget(backButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xE0101010);

        graphics.centeredText(this.font,
                Component.literal("🏆 Feeshman Deelux Achievements"),
                this.width / 2, 20, 0xFFD700);

        graphics.centeredText(this.font,
                Component.literal("Your Fishing Journey & Milestones").withStyle(ChatFormatting.GRAY),
                this.width / 2, 35, 0xCCCCCC);

        int lifetimeFish = getCurrentLifetimeFish();
        int sessionFish = getCurrentSessionFish();
        int biomesVisited = getBiomesVisited();

        int statsY = 60;
        graphics.fill(this.width / 2 - 150, statsY, this.width / 2 + 150, statsY + 60, 0xC0000000);
        drawBorder(graphics, this.width / 2 - 150, statsY, 300, 60, 0xFF666666);

        graphics.centeredText(this.font,
                Component.literal("📊 Current Statistics").withStyle(ChatFormatting.YELLOW),
                this.width / 2, statsY + 8, 0xFFFF55);

        graphics.centeredText(this.font,
                Component.literal("🐟 Lifetime: " + lifetimeFish + " fish  •  📈 Session: " + sessionFish
                        + " fish  •  🗺️ Biomes: " + biomesVisited),
                this.width / 2, statsY + 25, 0xFFFFFF);

        graphics.centeredText(this.font,
                Component.literal("⏰ Session Time: " + getFormattedSessionTime()).withStyle(ChatFormatting.AQUA),
                this.width / 2, statsY + 40, 0x55FFFF);

        int startY = 140;
        int achievementHeight = 25;
        int maxVisible = (this.height - startY - 60) / achievementHeight;

        for (int i = 0; i < Math.min(ACHIEVEMENTS.length, maxVisible); i++) {
            Achievement achievement = ACHIEVEMENTS[i];
            int y = startY + i * achievementHeight;

            boolean unlocked = isAchievementUnlocked(achievement, lifetimeFish, sessionFish, biomesVisited);

            int bgColor = unlocked ? 0xB0002200 : 0xB0220000;
            int borderColor = unlocked ? 0xFF00CC00 : 0xFF880000;

            graphics.fill(this.width / 2 - 200, y, this.width / 2 + 200, y + 20, bgColor);
            drawBorder(graphics, this.width / 2 - 200, y, 400, 20, borderColor);

            String status = unlocked ? achievement.icon : "❌";
            int titleColor = unlocked ? 0x88FF88 : 0xFF8888;
            int descColor = unlocked ? 0xFFFFFF : 0xCCCCCC;

            graphics.text(this.font, status, this.width / 2 - 190, y + 6, unlocked ? 0x00FF00 : 0xFF4444, true);

            graphics.text(this.font, achievement.title, this.width / 2 - 170, y + 3, titleColor, true);

            graphics.text(this.font, achievement.description, this.width / 2 - 170, y + 12, descColor, true);

            int current = getCurrentProgress(achievement, lifetimeFish, sessionFish, biomesVisited);
            String progress = "(" + current + "/" + achievement.requirement + ")";
            int progressColor = unlocked ? 0x00FF00 : 0xFFCC00;
            graphics.text(this.font, progress, this.width / 2 + 120, y + 7, progressColor, true);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private boolean isAchievementUnlocked(Achievement achievement, int lifetimeFish, int sessionFish,
                                          int biomesVisited) {
        if (FeeshmanDeeluxClient.isAchievementUnlockedOnServer(achievement.persistenceId)) {
            return true;
        }
        if (FeeshAchievementIds.TREASURE_HUNTER.equals(achievement.persistenceId)) {
            return false;
        }
        return switch (achievement.persistenceId) {
            case FeeshAchievementIds.FIRST_CAST -> lifetimeFish >= achievement.requirement;
            case FeeshAchievementIds.LIFETIME_100, FeeshAchievementIds.LIFETIME_500,
                 FeeshAchievementIds.LIFETIME_1000 -> lifetimeFish >= achievement.requirement;
            case FeeshAchievementIds.SESSION_10, FeeshAchievementIds.SESSION_25,
                 FeeshAchievementIds.SESSION_50, FeeshAchievementIds.SESSION_100 ->
                    sessionFish >= achievement.requirement;
            case FeeshAchievementIds.WORLD_EXPLORER -> biomesVisited >= achievement.requirement;
            default -> false;
        };
    }

    private int getCurrentProgress(Achievement achievement, int lifetimeFish, int sessionFish, int biomesVisited) {
        return switch (achievement.persistenceId) {
            case FeeshAchievementIds.FIRST_CAST, FeeshAchievementIds.LIFETIME_100,
                 FeeshAchievementIds.LIFETIME_500, FeeshAchievementIds.LIFETIME_1000 -> lifetimeFish;
            case FeeshAchievementIds.SESSION_10, FeeshAchievementIds.SESSION_25,
                 FeeshAchievementIds.SESSION_50, FeeshAchievementIds.SESSION_100 -> sessionFish;
            case FeeshAchievementIds.WORLD_EXPLORER -> biomesVisited;
            case FeeshAchievementIds.TREASURE_HUNTER ->
                    FeeshmanDeeluxClient.isAchievementUnlockedOnServer(FeeshAchievementIds.TREASURE_HUNTER) ? 1 : 0;
            default -> 0;
        };
    }

    private int getCurrentLifetimeFish() {
        return FeeshmanDeeluxClient.getLifetimeFishCaught();
    }

    private int getCurrentSessionFish() {
        return FeeshmanDeeluxClient.getSessionFishCount();
    }

    private int getBiomesVisited() {
        return FeeshmanDeeluxClient.getBiomesVisitedCount();
    }

    private String getFormattedSessionTime() {
        long start = FeeshmanDeeluxClient.getSessionStartTime();
        if (start == 0) {
            return "00:00";
        }
        long elapsedSec = (System.currentTimeMillis() - start) / 1000;
        long min = elapsedSec / 60;
        long sec = elapsedSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Achievement {
        final String title;
        final String description;
        final int requirement;
        final String icon;
        final String persistenceId;

        Achievement(String title, String description, int requirement, String icon, String persistenceId) {
            this.title = title;
            this.description = description;
            this.requirement = requirement;
            this.icon = icon;
            this.persistenceId = persistenceId;
        }
    }
}
