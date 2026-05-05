package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FeeshmanConfigScreen extends Screen {
    private final Screen parent;
    private AbstractSliderButton volumeSlider;
    private Button enabledButton;
    private Button achievementsButton;
    private Button doneButton;

    private float biteAlertVolume;
    private boolean autoFishEnabled;

    public FeeshmanConfigScreen(Screen parent) {
        super(Component.literal("Feeshman Deelux Configuration"));
        this.parent = parent;
        this.biteAlertVolume = FeeshmanConfig.getBiteAlertVolume();
        this.autoFishEnabled = FeeshmanConfig.isAutoFishEnabled();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.volumeSlider = new AbstractSliderButton(centerX - 100, startY, 200, 20,
                Component.literal("Bite Alert Volume: " + Math.round(biteAlertVolume * 100) + "%"),
                biteAlertVolume) {
            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Bite Alert Volume: " + Math.round((float) value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                biteAlertVolume = (float) value;
                FeeshmanConfig.setBiteAlertVolume(biteAlertVolume);
            }
        };

        this.enabledButton = Button.builder(
                        Component.literal("Auto-Fishing: " + (autoFishEnabled ? "§aEnabled" : "§cDisabled")),
                        button -> {
                            autoFishEnabled = !autoFishEnabled;
                            FeeshmanConfig.setAutoFishEnabled(autoFishEnabled);
                            button.setMessage(Component.literal("Auto-Fishing: "
                                    + (autoFishEnabled ? "§aEnabled" : "§cDisabled")));
                        })
                .bounds(centerX - 100, startY + 40, 200, 20)
                .build();

        this.achievementsButton = Button.builder(
                        Component.literal("🏆 View Achievements & Milestones"),
                        button -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new FeeshmanAchievementsScreen(this));
                            }
                        })
                .bounds(centerX - 100, startY + 70, 200, 20)
                .build();

        this.doneButton = Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(centerX - 50, startY + 100, 100, 20)
                .build();

        addRenderableWidget(volumeSlider);
        addRenderableWidget(enabledButton);
        addRenderableWidget(achievementsButton);
        addRenderableWidget(doneButton);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xE0101010);

        graphics.centeredText(this.font,
                Component.literal("🎣 Feeshman Deelux Configuration"),
                this.width / 2, 20, 0x00CCFF);

        graphics.centeredText(this.font,
                Component.literal("Ultimate Auto-Fishing Experience Settings").withStyle(ChatFormatting.GRAY),
                this.width / 2, 40, 0xAAAAAA);

        graphics.centeredText(this.font,
                Component.literal("⚡ Advanced Bite Detection • 🛡️ Smart Safety Features • 📊 Live Statistics")
                        .withStyle(ChatFormatting.DARK_GRAY),
                this.width / 2, 55, 0x888888);

        graphics.centeredText(this.font,
                Component.literal("Press [O] to toggle auto-fishing • Use /feeshstats for statistics")
                        .withStyle(ChatFormatting.YELLOW),
                this.width / 2, this.height - 50, 0xFFFF55);

        graphics.centeredText(this.font,
                Component.literal("ModMenu Integration • Volume Controls • Human-like Timing")
                        .withStyle(ChatFormatting.GREEN),
                this.width / 2, this.height - 35, 0x55FF55);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
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
}
