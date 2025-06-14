package com.yourname.feeshmandeelux;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class FeeshLeaderboard {
    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    private static final String FILE_NAME = "feeshman-leaderboard.properties";
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

    private static final Properties data = new Properties();

    public static void load() {
        if (!Files.exists(FILE_PATH)) {
            save();
            return;
        }
        try (InputStream in = Files.newInputStream(FILE_PATH)) {
            data.load(in);
        } catch (Exception e) {
            LOGGER.error("Failed to load leaderboard: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(FILE_PATH)) {
                data.store(out, "Feeshman Deelux Leaderboard");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save leaderboard: " + e.getMessage());
        }
    }

    public static void addCatch(ClientPlayerEntity player) {
        if (player == null) return;
        String name = player.getName().getString();
        int current = Integer.parseInt(data.getProperty(name, "0"));
        data.setProperty(name, String.valueOf(current + 1));
        save();
    }

    public static List<Map.Entry<String, Integer>> getTop(int limit) {
        return data.entrySet().stream()
                .map(e -> Map.entry((String) e.getKey(), Integer.parseInt((String) e.getValue())))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
} 