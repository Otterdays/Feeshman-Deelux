package com.yourname.feeshmandeelux;

import com.mojang.blaze3d.platform.InputConstants;
import com.yourname.feeshmandeelux.network.FeeshmanPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FeeshmanDeeluxClient implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    private static FeeshmanDeeluxClient instance;

    private static KeyMapping toggleKey;
    private boolean autoFishEnabled = false;

    public static final Identifier BITE_ALERT_ID = Identifier.fromNamespaceAndPath("feeshmandeelux", "bite_alert");
    public static final SoundEvent BITE_ALERT_SOUND = SoundEvent.createVariableRangeEvent(BITE_ALERT_ID);

    private static final Identifier HUD_ELEMENT_ID = Identifier.fromNamespaceAndPath("feeshmandeelux", "stats_hud");

    private int fishingSessionTicks = 0;
    private int totalFishCaught = 0;
    private int lifetimeFishCaught = 0;
    private int biomeCount = 0;
    private long sessionStartTime = 0;

    private final Set<String> syncedAchievementIds = Collections.synchronizedSet(new HashSet<>());

    private final String[] FISHING_QUOTES = {
            "\"Patience is the angler's virtue.\"",
            "\"The sea rewards those who wait.\"",
            "\"Every cast is a new adventure.\"",
            "\"Fortune favors the persistent fisher.\"",
            "\"Dawn brings the best catches.\"",
            "\"Skill and luck dance together on the water.\"",
            "\"Treasures hide beneath calm waters.\"",
            "\"Night fishing reveals hidden wonders.\"",
            "\"The depths hold ancient secrets.\"",
            "\"Master anglers are made, not born.\""
    };

    public static int getSessionFishCount() {
        return instance != null ? instance.totalFishCaught : 0;
    }

    public static int getBiomesVisitedCount() {
        return instance != null ? instance.biomeCount : 0;
    }

    public static long getSessionStartTime() {
        return instance != null ? instance.sessionStartTime : 0;
    }

    public static int getLifetimeFishCaught() {
        return instance != null ? instance.lifetimeFishCaught : 0;
    }

    public static boolean isAchievementUnlockedOnServer(String achievementId) {
        if (instance == null || achievementId == null || achievementId.isEmpty()) {
            return false;
        }
        return instance.syncedAchievementIds.contains(achievementId);
    }

    private static void applyAchievementCsv(String csv) {
        if (instance == null) {
            return;
        }
        instance.syncedAchievementIds.clear();
        if (csv == null || csv.isBlank()) {
            return;
        }
        for (String part : csv.split(",")) {
            String id = part.trim();
            if (!id.isEmpty()) {
                instance.syncedAchievementIds.add(id);
            }
        }
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Feeshman Deelux Initializing!");

        instance = this;

        Registry.register(BuiltInRegistries.SOUND_EVENT, BITE_ALERT_ID, BITE_ALERT_SOUND);

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.feeshmandeelux.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyMapping.Category.MISC
        ));

        HudElementRegistry.addLast(HUD_ELEMENT_ID, this::extractHudRenderState);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            sessionStartTime = System.currentTimeMillis();
            syncedAchievementIds.clear();
            if (client.player != null) {
                lifetimeFishCaught = FeeshLeaderboard.getPlayerTotal(client.player);
            }

            if (ThreadLocalRandom.current().nextFloat() < 0.3f) {
                String quote = FISHING_QUOTES[ThreadLocalRandom.current().nextInt(FISHING_QUOTES.length)];
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendSystemMessage(Component.literal("§7" + quote));
                    }
                });
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FeeshLeaderboard.flushIfDirty());

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.FishCaughtPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (instance != null) {
                    int prevSession = instance.totalFishCaught;
                    int prevLifetime = instance.lifetimeFishCaught;
                    instance.totalFishCaught = payload.sessionFish();
                    instance.lifetimeFishCaught = payload.lifetimeFish();
                    instance.biomeCount = payload.biomeCount();
                    if (context.player() != null) {
                        context.player().playSound(BITE_ALERT_SOUND, 0.5f, 1.0f);
                        if (!payload.luckyCompliment().isEmpty()) {
                            context.player().sendSystemMessage(Component.literal("§6§l" + payload.luckyCompliment()));
                        }
                    }
                    showAchievementToastIfMilestone(context.client(), prevSession, prevLifetime,
                            payload.sessionFish(), payload.lifetimeFish());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.StatsSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (instance != null) {
                    instance.totalFishCaught = payload.sessionFish();
                    instance.lifetimeFishCaught = payload.lifetimeFish();
                    instance.sessionStartTime = payload.sessionStartTime();
                    instance.biomeCount = payload.biomeCount();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.AchievementsSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> applyAchievementCsv(payload.achievementIdsCsv()));
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.ItemAnnouncementPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.player() != null) {
                    String msg = formatItemAnnouncement(payload.itemId(), payload.hasEnchantments());
                    if (msg != null) {
                        context.player().sendSystemMessage(Component.literal(msg));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(FeeshmanPayloads.DurabilityWarningPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                var manager = context.client().getToastManager();
                if (manager != null) {
                    SystemToast.add(manager, SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            Component.literal("Rod Durability Low"),
                            Component.literal("Only " + payload.remainingUses() + " uses left!"));
                }
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.consumeClick() && client.player != null && client.getConnection() != null) {
                autoFishEnabled = !autoFishEnabled;
                client.getConnection().sendCommand(autoFishEnabled ? "feeshman enable" : "feeshman disable");
            }
        });
    }

    private void extractHudRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!autoFishEnabled) {
            return;
        }
        renderPolishedHud(graphics);
    }

    private void renderPolishedHud(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.font == null) {
            return;
        }
        var font = client.font;

        int hudX = 6;
        int hudY = 6;
        int hudWidth = 220;
        int hudHeight = 160;

        int outerBorderColor = 0x80000000;
        int innerBorderColor = 0xA0222222;
        int backgroundColorMain = 0x85000000;
        int titleBackgroundColor = 0xB0004466;
        int accentColor = 0xFF00DDFF;

        context.fill(hudX - 2, hudY - 2, hudX + hudWidth + 2, hudY + hudHeight + 2, outerBorderColor);
        context.fill(hudX - 1, hudY - 1, hudX + hudWidth + 1, hudY + hudHeight + 1, innerBorderColor);
        context.fill(hudX, hudY, hudX + hudWidth, hudY + hudHeight, backgroundColorMain);

        context.fill(hudX, hudY, hudX + hudWidth, hudY + 20, titleBackgroundColor);
        context.fill(hudX, hudY + 18, hudX + hudWidth, hudY + 20, accentColor);

        String title = "⚡ Feeshman Deelux";
        int titleWidth = font.width(title);
        int titleX = hudX + (hudWidth - titleWidth) / 2;
        context.text(font, title, titleX, hudY + 6, 0xFFFFFFFF, true);

        int contentY = hudY + 26;
        int lineHeight = 14;
        int currentLine = 0;

        String fishText = totalFishCaught > 0
                ? String.format("◆ Fish: %d caught", totalFishCaught)
                : "◆ Fish: Use /feeshstats for stats";
        context.text(font, fishText, hudX + 8, contentY + (currentLine * lineHeight), 0xFF4AE54A, true);
        currentLine++;

        int sessionTicks = sessionStartTime > 0
                ? (int) ((System.currentTimeMillis() - sessionStartTime) / 50)
                : fishingSessionTicks;
        int sessionMinutes = sessionTicks / 1200;
        int sessionSeconds = (sessionTicks % 1200) / 20;
        String timeText = String.format("● Time: %02d:%02d session", sessionMinutes, sessionSeconds);
        context.text(font, timeText, hudX + 8, contentY + (currentLine * lineHeight), 0xFFFFA500, true);
        currentLine++;

        ItemStack rod = client.player.getMainHandItem();
        if (rod.getItem() == Items.FISHING_ROD) {
            int maxDurability = rod.getMaxDamage();
            int currentDamage = rod.getDamageValue();
            int remainingUses = maxDurability - currentDamage;
            int durabilityPercent = maxDurability > 0 ? (remainingUses * 100) / maxDurability : 0;

            String durabilityText = String.format("▲ Rod: %d uses (%d%%)", remainingUses, durabilityPercent);
            int color = durabilityPercent > 50 ? 0xFF4AE54A : durabilityPercent > 20 ? 0xFFFFB347 : 0xFFFF6B6B;
            context.text(font, durabilityText, hudX + 8, contentY + (currentLine * lineHeight), color, true);

            int barWidth = 90;
            int barHeight = 4;
            int barX = hudX + hudWidth - barWidth - 10;
            int barY = contentY + (currentLine * lineHeight) + 3;

            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x60333333);
            int fillWidth = (barWidth * durabilityPercent) / 100;
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, color);

            currentLine++;
        }

        if (client.level != null) {
            String weatherText = client.level.isRaining()
                    ? (client.level.isThundering() ? "♦ Weather: Thunder" : "♦ Weather: Rainy")
                    : "♦ Weather: Clear";
            int weatherColor = client.level.isRaining() ? 0xFF4A9AFF : 0xFFFFD700;
            context.text(font, weatherText, hudX + 8, contentY + (currentLine * lineHeight), weatherColor, true);
            currentLine++;

            long timeOfDay = client.level.getOverworldClockTime() % 24000;
            boolean isDay = timeOfDay < 12000;
            String dayNightText = isDay ? "☀ Time: Day" : "☽ Time: Night";
            int timeColor = isDay ? 0xFFFFD700 : 0xFFADD8E6;
            context.text(font, dayNightText, hudX + 8, contentY + (currentLine * lineHeight), timeColor, true);
            currentLine++;
        }

        if (client.level != null) {
            Holder<Biome> biome = client.level.getBiome(client.player.blockPosition());
            String biomeName = biome.unwrapKey().map(k -> k.identifier().toString()).orElse("unknown");
            biomeName = biomeName.replace("minecraft:", "").replace("_", " ");
            String biomeText = String.format("▼ Biome: %s", capitalizeWords(biomeName));

            int biomeColor = 0xFF40E0D0;
            if (biomeName.contains("ocean")) {
                biomeColor = 0xFF0080FF;
            } else if (biomeName.contains("river")) {
                biomeColor = 0xFF87CEEB;
            } else if (biomeName.contains("swamp")) {
                biomeColor = 0xFF90EE90;
            } else if (biomeName.contains("jungle")) {
                biomeColor = 0xFF32CD32;
            } else if (biomeName.contains("desert")) {
                biomeColor = 0xFFFFA500;
            } else if (biomeName.contains("forest")) {
                biomeColor = 0xFF228B22;
            }

            context.text(font, biomeText, hudX + 8, contentY + (currentLine * lineHeight), biomeColor, true);
            currentLine++;
        }

        int rateTicks = sessionStartTime > 0 ? (int) ((System.currentTimeMillis() - sessionStartTime) / 50) : fishingSessionTicks;
        if (rateTicks > 0) {
            float catchRate = (float) totalFishCaught / (rateTicks / 1200.0f);
            String efficiency = catchRate > 2.0f ? "Excellent" : catchRate > 1.0f ? "Good" : catchRate > 0.5f ? "Fair" : "Slow";
            String rateText = String.format("✦ Rate: %.1f/min (%s)", Math.max(0, catchRate), efficiency);
            int rateColor = catchRate > 2.0f ? 0xFF4AE54A : catchRate > 1.0f ? 0xFF9ACD32 : catchRate > 0.5f ? 0xFFFFB347 : 0xFFFF7F50;
            context.text(font, rateText, hudX + 8, contentY + (currentLine * lineHeight), rateColor, true);
            currentLine++;
        }

        String statusText = autoFishEnabled ? "◈ Status: Server Auto-Fishing" : "◈ Status: Manual (Press O for auto)";
        int statusColor = autoFishEnabled ? 0xFF4AE54A : 0xFFDDA0DD;
        context.text(font, statusText, hudX + 8, contentY + (currentLine * lineHeight), statusColor, true);
        currentLine++;

        if (lifetimeFishCaught > 0) {
            String lifetimeText = String.format("★ Lifetime: %d total catches", lifetimeFishCaught);
            context.text(font, lifetimeText, hudX + 8, contentY + (currentLine * lineHeight), 0xFFFFD700, true);
        }
    }

    private static final TagKey<Item> TAG_TREASURE = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("feeshmandeelux", "treasure"));
    private static final TagKey<Item> TAG_JUNK = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath("feeshmandeelux", "junk"));

    private static String formatItemAnnouncement(String itemId, boolean hasEnchantments) {
        Identifier parsed = Identifier.tryParse(itemId);
        if (parsed == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(parsed).orElse(null);
        if (item == null || item == Items.AIR) {
            return null;
        }
        ItemStack stack = new ItemStack(item);
        String name = stack.getHoverName().getString();
        if (stack.is(h -> h.is(TAG_TREASURE))) {
            return "§6§l✨ " + (hasEnchantments ? "§d" : "§6") + name + " §6§l✨";
        }
        if (stack.is(h -> h.is(TAG_JUNK))) {
            return "§7▸ " + name;
        }
        return "§a▸ " + name;
    }

    private static void showAchievementToastIfMilestone(Minecraft client, int prevSession, int prevLifetime,
                                                        int sessionFish, int lifetimeFish) {
        var manager = client.getToastManager();
        if (manager == null) {
            return;
        }
        String title = null;
        String desc = null;
        int[] sessionMilestones = {1, 10, 25, 50, 100};
        int[] lifetimeMilestones = {100, 500, 1000};
        for (int m : sessionMilestones) {
            if (prevSession < m && sessionFish >= m) {
                title = "Fishing Milestone!";
                desc = m + " fish this session!";
                break;
            }
        }
        if (title == null) {
            for (int m : lifetimeMilestones) {
                if (prevLifetime < m && lifetimeFish >= m) {
                    title = "Lifetime Achievement!";
                    desc = m + " fish caught total!";
                    break;
                }
            }
        }
        if (title != null && desc != null) {
            SystemToast.add(manager, SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    Component.literal(title), Component.literal(desc));
        }
    }

    private String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}
