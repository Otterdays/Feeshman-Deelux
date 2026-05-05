package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.db.FeeshmanDatabase;
import com.yourname.feeshmandeelux.network.FeeshmanPayloads;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side auto-fishing logic. Uses authoritative bobber state (hooked entity)
 * instead of client heuristics.
 */
public final class AutoFishService {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    private static final int BASE_RECAST_DELAY = 20;
    private static final int MIN_REACTION_TIME = 3;
    private static final int MAX_REACTION_TIME = 12;
    private static final int BOBBER_STUCK_THRESHOLD = 600;
    private static final double STUCK_MOVEMENT_THRESHOLD = 0.3;
    private static final int NO_ROD_GRACE_PERIOD = 60;
    private static final int LEADERBOARD_FLUSH_INTERVAL = 600;
    private static final int DURABILITY_WARNING_THRESHOLD = 10;
    private static final double BITE_VELOCITY_THRESHOLD = -0.04;
    private static final int BITE_CONFIRM_TICKS = 2;

    private static final String[] LUCKY_COMPLIMENTS = {
            "What a catch!", "You're on fire!", "Legendary angler!",
            "The fish fear you!", "Reel talent!", "Master of the deep!"
    };

    private static final TagKey<Item> TAG_TREASURE =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("feeshmandeelux", "treasure"));
    private static final TagKey<Item> TAG_JUNK =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("feeshmandeelux", "junk"));

    private static int leaderboardFlushCounter = 0;

    private static final Map<UUID, PlayerFishingState> PLAYER_STATES = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            leaderboardFlushCounter++;
            if (leaderboardFlushCounter >= LEADERBOARD_FLUSH_INTERVAL) {
                leaderboardFlushCounter = 0;
                FeeshLeaderboard.flushIfDirty();
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickFishingSeconds(player);
                tickPlayer(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            FeeshLeaderboard.syncPlayer(player);
            PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUUID(), u -> new PlayerFishingState());
            state.sessionStartTime = System.currentTimeMillis();
            state.sessionRowId = FeeshLeaderboard.beginSession(player);
            if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.StatsSyncPayload.TYPE)) {
                int lifetime = FeeshLeaderboard.getPlayerTotal(player);
                int biomes = FeeshmanServerCommands.getBiomeCount(player);
                ServerPlayNetworking.send(player, new FeeshmanPayloads.StatsSyncPayload(
                        state.totalFishCaught, lifetime, state.sessionStartTime, biomes));
            }
            if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.AchievementsSyncPayload.TYPE)) {
                String csv = FeeshLeaderboard.exportAchievementCsv(player);
                ServerPlayNetworking.send(player, new FeeshmanPayloads.AchievementsSyncPayload(csv));
            }
            player.sendSystemMessage(
                    Component.literal(
                            "§a§l🎣 Feeshman Deelux §r§7 • Press §a§l[O]§r§7 to toggle auto-fishing • §7/feeshman for commands"),
                    false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var disconnected = handler.getPlayer();
            if (disconnected != null) {
                UUID uuid = disconnected.getUUID();
                PlayerFishingState st = PLAYER_STATES.remove(uuid);
                if (st != null) {
                    FeeshLeaderboard.endSession(st.sessionRowId, st.totalFishCaught);
                }
            }
            FeeshLeaderboard.flushIfDirty();
        });
    }

    private static void tickFishingSeconds(ServerPlayer player) {
        if (!FeeshmanDatabase.isOpen()) {
            return;
        }
        PlayerFishingState state = PLAYER_STATES.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (!isAutoFishEnabled(player, state)) {
            return;
        }
        if (player.getMainHandItem().getItem() != Items.FISHING_ROD) {
            return;
        }
        state.fishingSecondTicks++;
        if (state.fishingSecondTicks >= 20) {
            state.fishingSecondTicks = 0;
            FeeshLeaderboard.addFishingSeconds(player, 1);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUUID(), u -> new PlayerFishingState());

        if (!isAutoFishEnabled(player, state)) {
            return;
        }

        if (player.getMainHandItem().getItem() != Items.FISHING_ROD) {
            state.noRodGraceTicks++;
            if (state.noRodGraceTicks >= NO_ROD_GRACE_PERIOD) {
                player.sendSystemMessage(
                        Component.literal("§e⚠️ No fishing rod detected. Auto-fishing disabled."), false);
                state.autoFishEnabled = false;
                state.noRodGraceTicks = 0;
            }
            return;
        }
        state.noRodGraceTicks = 0;

        var rod = player.getMainHandItem();
        int remainingUses = rod.getMaxDamage() - rod.getDamageValue();
        if (remainingUses > DURABILITY_WARNING_THRESHOLD) {
            state.durabilityWarned = false;
        } else if (!state.durabilityWarned) {
            state.durabilityWarned = true;
            if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.DurabilityWarningPayload.TYPE)) {
                ServerPlayNetworking.send(player, new FeeshmanPayloads.DurabilityWarningPayload(remainingUses));
            }
        }

        if (state.recastDelayTicks > 0) {
            state.recastDelayTicks--;
            return;
        }

        if (state.humanReactionDelay > 0) {
            state.humanReactionDelay--;
            if (state.humanReactionDelay == 0 && player.fishing != null) {
                reelIn(player, state);
            }
            return;
        }

        FishingHook bobber = player.fishing;
        if (bobber == null) {
            cast(player, state);
            return;
        }

        if (bobber.getHookedIn() != null) {
            state.biteConfirmTicks = 0;
            state.bobberStuckCheckPos = null;
            state.bobberStuckTicks = 0;
            return;
        }

        if (bobber.isInWater() && bobber.getDeltaMovement().y < BITE_VELOCITY_THRESHOLD) {
            state.biteConfirmTicks++;
            if (state.biteConfirmTicks >= BITE_CONFIRM_TICKS) {
                state.biteConfirmTicks = 0;
                state.humanReactionDelay = MIN_REACTION_TIME
                        + ThreadLocalRandom.current().nextInt(MAX_REACTION_TIME - MIN_REACTION_TIME + 1);
                return;
            }
        } else if (state.biteConfirmTicks > 0) {
            state.biteConfirmTicks = Math.max(0, state.biteConfirmTicks - 1);
        }

        Vec3 currentPos = new Vec3(bobber.getX(), bobber.getY(), bobber.getZ());
        if (checkBobberStuck(state, currentPos)) {
            LOGGER.info("Feeshman Deelux: Bobber stuck, recasting...");
            player.sendSystemMessage(
                    Component.literal("§e⚠️ Bobber stuck detected, recasting..."), false);
            recast(player, state);
        }
    }

    private static boolean isAutoFishEnabled(ServerPlayer player, PlayerFishingState state) {
        if (state.autoFishEnabled != null) {
            return state.autoFishEnabled;
        }
        return FeeshmanConfig.isAutoFishEnabled();
    }

    public static void setAutoFishEnabled(ServerPlayer player, boolean enabled) {
        PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUUID(), u -> new PlayerFishingState());
        state.autoFishEnabled = enabled;
    }

    public static Boolean getAutoFishEnabled(ServerPlayer player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUUID());
        if (state == null || state.autoFishEnabled == null) {
            return null;
        }
        return state.autoFishEnabled;
    }

    private static void cast(ServerPlayer player, PlayerFishingState state) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        state.recastDelayTicks = BASE_RECAST_DELAY + ThreadLocalRandom.current().nextInt(20);
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;
        state.biteConfirmTicks = 0;
    }

    private static void reelIn(ServerPlayer player, PlayerFishingState state) {
        Map<String, Integer> before = snapshotItemCounts(player);
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        state.recastDelayTicks = BASE_RECAST_DELAY + ThreadLocalRandom.current().nextInt(40);
        state.totalFishCaught++;
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;
        state.biteConfirmTicks = 0;

        Map<String, Integer> after = snapshotItemCounts(player);
        CatchDelta delta = detectCatchDelta(player, before, after);
        String biomeId = player.level() != null
                ? player.level().getBiome(player.blockPosition()).unwrapKey()
                .map(k -> k.identifier().toString()).orElse("unknown")
                : "unknown";
        int lifetime = FeeshLeaderboard.recordCatch(player, biomeId, delta, state.totalFishCaught);

        sendItemAnnouncementIfDetected(player, before, after);

        int biomes = FeeshmanServerCommands.getBiomeCount(player);
        String compliment = ThreadLocalRandom.current().nextFloat() < 0.05f
                ? LUCKY_COMPLIMENTS[ThreadLocalRandom.current().nextInt(LUCKY_COMPLIMENTS.length)]
                : "";
        if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.FishCaughtPayload.TYPE)) {
            ServerPlayNetworking.send(player, new FeeshmanPayloads.FishCaughtPayload(
                    state.totalFishCaught, lifetime, compliment, biomes));
        }

        if (state.totalFishCaught % 5 == 0) {
            player.sendSystemMessage(
                    Component.literal("§a" + state.totalFishCaught + " fish caught this session!"), false);
        }
    }

    private static Map<String, Integer> snapshotItemCounts(ServerPlayer player) {
        Map<String, Integer> counts = new HashMap<>();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (key != null) {
                    counts.merge(key.toString(), stack.getCount(), Integer::sum);
                }
            }
        }
        return counts;
    }

    private static CatchDelta detectCatchDelta(ServerPlayer player,
                                                Map<String, Integer> before, Map<String, Integer> after) {
        for (Map.Entry<String, Integer> e : after.entrySet()) {
            int prev = before.getOrDefault(e.getKey(), 0);
            if (e.getValue() > prev) {
                String itemId = e.getKey();
                ItemStack sample = findStackOf(player, itemId);
                boolean hasEnchantments = sample != null && !sample.getEnchantments().isEmpty();
                boolean treasure = sample != null && sample.is(h -> h.is(TAG_TREASURE));
                boolean junk = sample != null && sample.is(h -> h.is(TAG_JUNK));
                return new CatchDelta(itemId, treasure, junk, hasEnchantments);
            }
        }
        return CatchDelta.unknown();
    }

    private static void sendItemAnnouncementIfDetected(ServerPlayer player,
                                                     Map<String, Integer> before, Map<String, Integer> after) {
        for (Map.Entry<String, Integer> e : after.entrySet()) {
            int prev = before.getOrDefault(e.getKey(), 0);
            if (e.getValue() > prev) {
                String itemId = e.getKey();
                ItemStack sample = findStackOf(player, itemId);
                boolean hasEnchantments = sample != null && !sample.getEnchantments().isEmpty();
                if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.ItemAnnouncementPayload.TYPE)) {
                    ServerPlayNetworking.send(player, new FeeshmanPayloads.ItemAnnouncementPayload(itemId, hasEnchantments));
                }
                return;
            }
        }
    }

    private static ItemStack findStackOf(ServerPlayer player, String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null || item == Items.AIR) {
            return null;
        }
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == item) {
                return stack;
            }
        }
        return null;
    }

    private static void recast(ServerPlayer player, PlayerFishingState state) {
        player.gameMode.useItem(player, player.level(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        state.recastDelayTicks = 40;
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;
        state.biteConfirmTicks = 0;
    }

    private static boolean checkBobberStuck(PlayerFishingState state, Vec3 currentPos) {
        if (state.bobberStuckCheckPos == null) {
            state.bobberStuckCheckPos = currentPos;
            state.bobberStuckTicks = 0;
            state.lastValidBobberPos = currentPos;
            return false;
        }

        double distance = currentPos.distanceTo(state.bobberStuckCheckPos);
        double distanceFromLastValid = state.lastValidBobberPos != null
                ? currentPos.distanceTo(state.lastValidBobberPos) : distance;

        if (distance >= STUCK_MOVEMENT_THRESHOLD || distanceFromLastValid >= STUCK_MOVEMENT_THRESHOLD) {
            state.bobberStuckCheckPos = currentPos;
            state.bobberStuckTicks = 0;
            state.lastValidBobberPos = currentPos;
            return false;
        }

        state.bobberStuckTicks++;
        return state.bobberStuckTicks >= BOBBER_STUCK_THRESHOLD;
    }

    public static int getSessionFishCount(ServerPlayer player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUUID());
        return state != null ? state.totalFishCaught : 0;
    }

    public static void resetSession(ServerPlayer player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUUID());
        if (state != null) {
            state.totalFishCaught = 0;
            state.sessionStartTime = System.currentTimeMillis();
        }
    }

    public static long getSessionStartTime(ServerPlayer player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUUID());
        return state != null ? state.sessionStartTime : System.currentTimeMillis();
    }

    private static class PlayerFishingState {
        Boolean autoFishEnabled = null;
        int recastDelayTicks = 0;
        int humanReactionDelay = 0;
        int totalFishCaught = 0;
        long sessionRowId = -1L;
        int fishingSecondTicks = 0;
        long sessionStartTime = System.currentTimeMillis();
        int noRodGraceTicks = 0;
        Vec3 bobberStuckCheckPos = null;
        int bobberStuckTicks = 0;
        Vec3 lastValidBobberPos = null;
        boolean durabilityWarned = false;
        int biteConfirmTicks = 0;
    }
}
