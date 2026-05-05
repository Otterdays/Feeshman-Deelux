package com.yourname.feeshmandeelux;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Server-side commands for Feeshman Deelux.
 */
public final class FeeshmanServerCommands {

    private FeeshmanServerCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("feeshman")
                .executes(FeeshmanServerCommands::executeFeeshmanHelp)
                .then(Commands.literal("toggle")
                        .executes(FeeshmanServerCommands::executeFeeshmanToggle))
                .then(Commands.literal("enable")
                        .executes(ctx -> executeFeeshmanSet(ctx, true)))
                .then(Commands.literal("disable")
                        .executes(ctx -> executeFeeshmanSet(ctx, false)))
        );

        dispatcher.register(Commands.literal("feeshstats")
                .executes(FeeshmanServerCommands::executeFeeshstats)
                .then(Commands.literal("biome")
                        .executes(FeeshmanServerCommands::executeFeeshstatsBiome))
        );

        dispatcher.register(Commands.literal("feeshleaderboard")
                .executes(FeeshmanServerCommands::executeFeeshleaderboard)
                .then(Commands.literal("today")
                        .executes(FeeshmanServerCommands::executeFeeshleaderboardToday))
                .then(Commands.literal("week")
                        .executes(FeeshmanServerCommands::executeFeeshleaderboardWeek))
        );

        dispatcher.register(Commands.literal("feeshhistory")
                .executes(ctx -> executeFeeshhistory(ctx, 10))
                .then(Commands.argument("n", IntegerArgumentType.integer(1, 50))
                        .executes(ctx -> executeFeeshhistory(ctx, IntegerArgumentType.getInteger(ctx, "n"))))
        );

        dispatcher.register(Commands.literal("feeshtopitems")
                .executes(ctx -> executeFeeshtopitems(ctx, 10))
                .then(Commands.argument("n", IntegerArgumentType.integer(1, 50))
                        .executes(ctx -> executeFeeshtopitems(ctx, IntegerArgumentType.getInteger(ctx, "n"))))
        );
    }

    private static int executeFeeshmanHelp(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Feeshman Deelux Help ==="), false);
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("§e§lCommands:"), false);
        player.sendSystemMessage(Component.literal("§a/feeshman §7- §fShow this help message"), false);
        player.sendSystemMessage(Component.literal("§a/feeshman toggle §7- §fToggle auto-fishing on/off"), false);
        player.sendSystemMessage(Component.literal("§a/feeshman enable §7- §fEnable auto-fishing"), false);
        player.sendSystemMessage(Component.literal("§a/feeshman disable §7- §fDisable auto-fishing"), false);
        player.sendSystemMessage(Component.literal("§a/feeshstats §7- §fView fishing statistics"), false);
        player.sendSystemMessage(Component.literal("§a/feeshstats biome §7- §fView biome catch breakdown"), false);
        player.sendSystemMessage(Component.literal("§a/feeshleaderboard §7- §fAll-time top 5"), false);
        player.sendSystemMessage(Component.literal("§a/feeshleaderboard today §7- §fTop 5 since UTC midnight"), false);
        player.sendSystemMessage(Component.literal("§a/feeshleaderboard week §7- §fTop 5 last 7 days"), false);
        player.sendSystemMessage(Component.literal("§a/feeshhistory [n] §7- §fLast n catches (default 10)"), false);
        player.sendSystemMessage(Component.literal("§a/feeshtopitems [n] §7- §fTop n caught items (default 10)"), false);
        player.sendSystemMessage(Component.literal(""), false);
        player.sendSystemMessage(Component.literal("§7Server-side auto-fishing. No client mod required."), false);
        return 1;
    }

    private static int executeFeeshmanToggle(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        Boolean current = AutoFishService.getAutoFishEnabled(player);
        boolean next = current == null ? !FeeshmanConfig.isAutoFishEnabled() : !current;
        AutoFishService.setAutoFishEnabled(player, next);

        String status = next ? "§a§lEnabled" : "§c§lDisabled";
        player.sendSystemMessage(Component.literal("§6§lFeeshman Deelux " + status), false);
        return 1;
    }

    private static int executeFeeshmanSet(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        AutoFishService.setAutoFishEnabled(player, enabled);
        String status = enabled ? "§a§lEnabled" : "§c§lDisabled";
        player.sendSystemMessage(Component.literal("§6§lFeeshman Deelux " + status), false);
        return 1;
    }

    private static int executeFeeshstats(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        int sessionFish = AutoFishService.getSessionFishCount(player);
        int lifetimeFish = FeeshLeaderboard.getPlayerTotal(player);
        long sessionTime = (System.currentTimeMillis() - AutoFishService.getSessionStartTime(player)) / 1000;
        int sessionMinutes = (int) (sessionTime / 60);
        Boolean enabled = AutoFishService.getAutoFishEnabled(player);
        boolean status = enabled != null ? enabled : FeeshmanConfig.isAutoFishEnabled();

        player.sendSystemMessage(Component.literal("§6§l=== Feeshman Deelux Statistics ==="), false);
        player.sendSystemMessage(Component.literal("§7Session Fish: §a" + sessionFish), false);
        player.sendSystemMessage(Component.literal("§7Lifetime Fish: §a" + lifetimeFish), false);
        player.sendSystemMessage(Component.literal("§7Session Time: §a" + sessionMinutes + " minutes"), false);
        player.sendSystemMessage(Component.literal("§7Status: " + (status ? "§aEnabled" : "§cDisabled")), false);
        player.sendSystemMessage(Component.literal("§7Use §e/feeshstats biome §7for biome breakdown"), false);
        return 1;
    }

    private static int executeFeeshstatsBiome(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        Map<String, Integer> tracker = FeeshLeaderboard.getBiomeBreakdown(player);
        player.sendSystemMessage(Component.literal("§6§l=== Biome Catch Statistics ==="), false);

        if (tracker.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No biome data yet. Start fishing to track catches!"), false);
            return 1;
        }

        tracker.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> player.sendSystemMessage(
                        Component.literal("§7" + entry.getKey().replace("minecraft:", "") + ": §a" + entry.getValue() + " fish"),
                        false));
        return 1;
    }

    private static int executeFeeshleaderboard(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Feeshman Leaderboard (all-time) ==="), false);
        List<Map.Entry<String, Integer>> top = FeeshLeaderboard.getTop(5);
        printRanked(player, top);
        return 1;
    }

    private static int executeFeeshleaderboardToday(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Leaderboard (since UTC midnight) ==="), false);
        List<Map.Entry<String, Integer>> top = FeeshLeaderboard.getTopSince(FeeshLeaderboard.startOfUtcDay(), 5);
        printRanked(player, top);
        return 1;
    }

    private static int executeFeeshleaderboardWeek(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Leaderboard (last 7 days) ==="), false);
        List<Map.Entry<String, Integer>> top = FeeshLeaderboard.getTopSince(FeeshLeaderboard.minusDaysUtc(7), 5);
        printRanked(player, top);
        return 1;
    }

    private static void printRanked(ServerPlayer player, List<Map.Entry<String, Integer>> top) {
        if (top.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No data yet. Start fishing to populate the leaderboard!"), false);
            return;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            player.sendSystemMessage(Component.literal(String.format("§e#%d §7%s: §a%d fish", rank, entry.getKey(), entry.getValue())), false);
            rank++;
        }
    }

    private static int executeFeeshhistory(CommandContext<CommandSourceStack> ctx, int n) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Recent catches (last " + n + ") ==="), false);
        List<String> lines = FeeshLeaderboard.recentCatchLines(player, n);
        if (lines.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No catch history yet."), false);
            return 1;
        }
        for (String line : lines) {
            player.sendSystemMessage(Component.literal("§7" + line), false);
        }
        return 1;
    }

    private static int executeFeeshtopitems(CommandContext<CommandSourceStack> ctx, int n) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6§l=== Top caught items (global) ==="), false);
        List<Map.Entry<String, Integer>> top = FeeshLeaderboard.topItems(n);
        if (top.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7No item data yet."), false);
            return 1;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> e : top) {
            player.sendSystemMessage(Component.literal(String.format("§e#%d §7%s §a×%d", rank, e.getKey(), e.getValue())), false);
            rank++;
        }
        return 1;
    }

    public static int getBiomeCount(ServerPlayer player) {
        return FeeshLeaderboard.getBiomeDistinctCount(player);
    }
}
