package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Environment(EnvType.CLIENT)
public class FeeshmanConfig {
    private static final String CONFIG_FILE_NAME = "feeshman-deelux.properties";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    
    // Default values
    public static final float DEFAULT_BITE_ALERT_VOLUME = 0.7f;
    public static final boolean DEFAULT_AUTO_FISH_ENABLED = false;
    
    // Current values
    private static float biteAlertVolume = DEFAULT_BITE_ALERT_VOLUME;
    private static boolean autoFishEnabled = DEFAULT_AUTO_FISH_ENABLED;
    
    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    
    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save(); // Create default config
            return;
        }
        
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            props.load(input);
            
            biteAlertVolume = Float.parseFloat(props.getProperty("biteAlertVolume", String.valueOf(DEFAULT_BITE_ALERT_VOLUME)));
            autoFishEnabled = Boolean.parseBoolean(props.getProperty("autoFishEnabled", String.valueOf(DEFAULT_AUTO_FISH_ENABLED)));
            
            // Clamp volume to valid range
            biteAlertVolume = Math.max(0.0f, Math.min(1.0f, biteAlertVolume));
            
            LOGGER.info("🎣 Feeshman Deelux: Config loaded successfully");
        } catch (Exception e) {
            LOGGER.error("🎣 Feeshman Deelux: Failed to load config, using defaults: " + e.getMessage());
            biteAlertVolume = DEFAULT_BITE_ALERT_VOLUME;
            autoFishEnabled = DEFAULT_AUTO_FISH_ENABLED;
        }
    }
    
    public static void save() {
        Properties props = new Properties();
        props.setProperty("biteAlertVolume", String.valueOf(biteAlertVolume));
        props.setProperty("autoFishEnabled", String.valueOf(autoFishEnabled));
        
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                props.store(output, "Feeshman Deelux Configuration");
                LOGGER.info("🎣 Feeshman Deelux: Config saved successfully");
            }
        } catch (Exception e) {
            LOGGER.error("🎣 Feeshman Deelux: Failed to save config: " + e.getMessage());
        }
    }
    
    // Getters and setters
    public static float getBiteAlertVolume() {
        return biteAlertVolume;
    }
    
    public static void setBiteAlertVolume(float volume) {
        biteAlertVolume = Math.max(0.0f, Math.min(1.0f, volume));
        save();
    }
    
    public static boolean isAutoFishEnabled() {
        return autoFishEnabled;
    }
    
    public static void setAutoFishEnabled(boolean enabled) {
        autoFishEnabled = enabled;
        save();
    }
} 