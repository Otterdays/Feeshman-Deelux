package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class FeeshmanConfigScreen extends Screen {
    private final Screen parent;
    private SliderWidget volumeSlider;
    private ButtonWidget enabledButton;
    private ButtonWidget doneButton;
    
    // Config values
    private float biteAlertVolume;
    private boolean autoFishEnabled;
    
    public FeeshmanConfigScreen(Screen parent) {
        super(Text.literal("Feeshman Deelux Configuration"));
        this.parent = parent;
        
        // Get current values from config
        this.biteAlertVolume = FeeshmanConfig.getBiteAlertVolume();
        this.autoFishEnabled = FeeshmanConfig.isAutoFishEnabled();
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;
        
        // Volume slider
        this.volumeSlider = new SliderWidget(centerX - 100, startY, 200, 20, 
            Text.literal("Bite Alert Volume: " + Math.round(biteAlertVolume * 100) + "%"), 
            biteAlertVolume) {
            
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Bite Alert Volume: " + Math.round(value * 100) + "%"));
            }
            
            @Override
            protected void applyValue() {
                biteAlertVolume = (float) value;
                FeeshmanConfig.setBiteAlertVolume(biteAlertVolume);
            }
        };
        
        // Auto-fish enabled button
        this.enabledButton = ButtonWidget.builder(
            Text.literal("Auto-Fishing: " + (autoFishEnabled ? "§aEnabled" : "§cDisabled")),
            button -> {
                autoFishEnabled = !autoFishEnabled;
                FeeshmanConfig.setAutoFishEnabled(autoFishEnabled);
                button.setMessage(Text.literal("Auto-Fishing: " + (autoFishEnabled ? "§aEnabled" : "§cDisabled")));
            })
            .dimensions(centerX - 100, startY + 40, 200, 20)
            .build();
        
        // Done button
        this.doneButton = ButtonWidget.builder(Text.literal("Done"), button -> {
            this.close();
        })
        .dimensions(centerX - 50, startY + 80, 100, 20)
        .build();
        
        addDrawableChild(volumeSlider);
        addDrawableChild(enabledButton);
        addDrawableChild(doneButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        
        // Draw description
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Configure your Feeshman Deelux settings").formatted(Formatting.GRAY), 
            this.width / 2, 40, 0xAAAAAA);
        
        // Draw current keybind info
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Press [O] to toggle auto-fishing in-game").formatted(Formatting.YELLOW), 
            this.width / 2, this.height - 40, 0xFFFF55);
        
        super.render(context, mouseX, mouseY, delta);
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
} 