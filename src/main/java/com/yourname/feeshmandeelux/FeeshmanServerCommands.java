package com.yourname.feeshmandeelux;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Server-side commands for Feeshman Deelux.
 */
public final class FeeshmanServerCommands {

    private static final Map<String, Map<String, Integer>> BIOME_CATCH_TRACKER = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("feeshman")
                .executes(FeeshmanServerCommands::executeFeeshmanHelp)
                .then(CommandManager.literal("toggle")
                        .executes(FeeshmanServerCommands::executeFeeshmanToggle))
                .then(CommandManager.literal("enable")
                        .executes(ctx -> executeFeeshmanSet(ctx, true)))
                .then(CommandManager.literal("disable")
                        .executes(ctx -> executeFeeshmanSet(ctx, false)))
        );

        dispatcher.register(CommandManager.literal("feeshstats")
                .executes(FeeshmanServerCommands::executeFeeshstats)
                .then(CommandManager.literal("biome")
                        .executes(FeeshmanServerCommands::executeFeeshstatsBiome))
        );

        dispatcher.register(CommandManager.literal("feeshleaderboard")
                .executes(FeeshmanServerCommands::executeFeeshleaderboard)
        );
    }

    private static int executeFeeshmanHelp(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.sendMessage(Text.literal("§6§l=== Feeshman Deelux Help ==="), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§e§lCommands:"), false);
        player.sendMessage(Text.literal("§a/feeshman §7- §fShow this help message"), false);
        player.sendMessage(Text.literal("§a/feeshman toggle §7- §fToggle auto-fishing on/off"), false);
        player.sendMessage(Text.literal("§a/feeshman enable §7- §fEnable auto-fishing"), false);
        player.sendMessage(Text.literal("§a/feeshman disable §7- §fDisable auto-fishing"), false);
        player.sendMessage(Text.literal("§a/feeshstats §7- §fView fishing statistics"), false);
        player.sendMessage(Text.literal("§a/feeshstats biome §7- §fView biome catch breakdown"), false);
        player.sendMessage(Text.literal("§a/feeshleaderboard §7- §fView leaderboard"), false);
        player.sendMessage(Text.literal(""), false);
        player.sendMessage(Text.literal("§7Server-side auto-fishing. No client mod required."), false);
        return 1;
    }

    private static int executeFeeshmanToggle(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        Boolean current = AutoFishService.getAutoFishEnabled(player);
        boolean next = current == null ? !FeeshmanConfig.isAutoFishEnabled() : !current;
        AutoFishService.setAutoFishEnabled(player, next);

        String status = next ? "§a§lEnabled" : "§c§lDisabled";
        player.sendMessage(Text.literal("§6§lFeeshman Deelux " + status), false);
        return 1;
    }

    private static int executeFeeshmanSet(CommandContext<ServerCommandSource> ctx, boolean enabled) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        AutoFishService.setAutoFishEnabled(player, enabled);
        String status = enabled ? "§a§lEnabled" : "§c§lDisabled";
        player.sendMessage(Text.literal("§6§lFeeshman Deelux " + status), false);
        return 1;
    }

    private static int executeFeeshstats(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        int sessionFish = AutoFishService.getSessionFishCount(player);
        int lifetimeFish = FeeshLeaderboard.getPlayerTotal(player.getName().getString());
        long sessionTime = (System.currentTimeMillis() - AutoFishService.getSessionStartTime(player)) / 1000;
        int sessionMinutes = (int) (sessionTime / 60);
        Boolean enabled = AutoFishService.getAutoFishEnabled(player);
        boolean status = enabled != null ? enabled : FeeshmanConfig.isAutoFishEnabled();

        player.sendMessage(Text.literal("§6§l=== Feeshman Deelux Statistics ==="), false);
        player.sendMessage(Text.literal("§7Session Fish: §a" + sessionFish), false);
        player.sendMessage(Text.literal("§7Lifetime Fish: §a" + lifetimeFish), false);
        player.sendMessage(Text.literal("§7Session Time: §a" + sessionMinutes + " minutes"), false);
        player.sendMessage(Text.literal("§7Status: " + (status ? "§aEnabled" : "§cDisabled")), false);
        player.sendMessage(Text.literal("§7Use §e/feeshstats biome §7for biome breakdown"), false);
        return 1;
    }

    private static int executeFeeshstatsBiome(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        Map<String, Integer> tracker = BIOME_CATCH_TRACKER.get(player.getName().getString());
        player.sendMessage(Text.literal("§6§l=== Biome Catch Statistics ==="), false);

        if (tracker == null || tracker.isEmpty()) {
            player.sendMessage(Text.literal("§7No biome data yet. Start fishing to track catches!"), false);
            return 1;
        }

        tracker.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> player.sendMessage(
                        Text.literal("§7" + entry.getKey().replace("minecraft:", "") + ": §a" + entry.getValue() + " fish"),
                        false));
        return 1;
    }

    private static int executeFeeshleaderboard(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        player.sendMessage(Text.literal("§6§l=== Feeshman Leaderboard ==="), false);
        List<Map.Entry<String, Integer>> top = FeeshLeaderboard.getTop(5);
        if (top.isEmpty()) {
            player.sendMessage(Text.literal("§7No data yet. Start fishing to populate the leaderboard!"), false);
            return 1;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            player.sendMessage(Text.literal(String.format("§e#%d §7%s: §a%d fish", rank, entry.getKey(), entry.getValue())), false);
            rank++;
        }
        return 1;
    }

    public static void trackBiomeCatch(ServerPlayerEntity player, RegistryEntry<Biome> biome) {
        String playerName = player.getName().getString();
        String biomeName = biome.getKey().map(k -> k.getValue().toString()).orElse("unknown");
        BIOME_CATCH_TRACKER
                .computeIfAbsent(playerName, k -> new HashMap<>())
                .merge(biomeName, 1, Integer::sum);
    }

    public static int getBiomeCount(ServerPlayerEntity player) {
        Map<String, Integer> tracker = BIOME_CATCH_TRACKER.get(player.getName().getString());
        return tracker != null ? tracker.size() : 0;
    }
}
