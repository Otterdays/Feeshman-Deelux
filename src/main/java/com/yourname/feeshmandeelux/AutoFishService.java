package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.network.FeeshmanPayloads;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Server-side auto-fishing logic. Uses authoritative bobber state (HOOKED_IN_ENTITY)
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

    private static final String[] LUCKY_COMPLIMENTS = {
            "What a catch!", "You're on fire!", "Legendary angler!",
            "The fish fear you!", "Reel talent!", "Master of the deep!"
    };

    private static final TagKey<net.minecraft.item.Item> TAG_TREASURE = TagKey.of(RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "treasure"));
    private static final TagKey<net.minecraft.item.Item> TAG_JUNK = TagKey.of(RegistryKeys.ITEM, Identifier.of("feeshmandeelux", "junk"));

    private static int leaderboardFlushCounter = 0;

    private static final Map<UUID, PlayerFishingState> PLAYER_STATES = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            leaderboardFlushCounter++;
            if (leaderboardFlushCounter >= LEADERBOARD_FLUSH_INTERVAL) {
                leaderboardFlushCounter = 0;
                FeeshLeaderboard.flushIfDirty();
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUuid(), u -> new PlayerFishingState());
            state.sessionStartTime = System.currentTimeMillis();
            if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.StatsSyncPayload.ID)) {
                int lifetime = FeeshLeaderboard.getPlayerTotal(player.getName().getString());
                int biomes = FeeshmanServerCommands.getBiomeCount(player);
                ServerPlayNetworking.send(player, new FeeshmanPayloads.StatsSyncPayload(
                        state.totalFishCaught, lifetime, state.sessionStartTime, biomes));
            }
            // Always show on join: Press [O] to toggle (client mod) or /feeshman (vanilla client)
            player.sendMessage(net.minecraft.text.Text.literal("§a§l🎣 Feeshman Deelux §r§7 • Press §a§l[O]§r§7 to toggle auto-fishing • §7/feeshman for commands"), false);
            if (ThreadLocalRandom.current().nextFloat() < 0.3f) {
                String[] quotes = {"\"Patience is the angler's virtue.\"", "\"The sea rewards those who wait.\"", "\"Every cast is a new adventure.\""};
                String quote = quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
                player.sendMessage(net.minecraft.text.Text.literal("§7" + quote), false);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            FeeshLeaderboard.flushIfDirty();
            PLAYER_STATES.remove(handler.getPlayer().getUuid());
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUuid(), u -> new PlayerFishingState());

        if (!isAutoFishEnabled(player, state)) {
            return;
        }

        if (player.getMainHandStack().getItem() != Items.FISHING_ROD) {
            state.noRodGraceTicks++;
            if (state.noRodGraceTicks >= NO_ROD_GRACE_PERIOD) {
                player.sendMessage(net.minecraft.text.Text.literal("§e⚠️ No fishing rod detected. Auto-fishing disabled."), false);
                state.autoFishEnabled = false;
                state.noRodGraceTicks = 0;
            }
            return;
        }
        state.noRodGraceTicks = 0;

        var rod = player.getMainHandStack();
        int remainingUses = rod.getMaxDamage() - rod.getDamage();
        if (remainingUses <= DURABILITY_WARNING_THRESHOLD && !state.durabilityWarned) {
            state.durabilityWarned = true;
            if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.DurabilityWarningPayload.ID)) {
                ServerPlayNetworking.send(player, new FeeshmanPayloads.DurabilityWarningPayload(remainingUses));
            }
        }

        if (state.recastDelayTicks > 0) {
            state.recastDelayTicks--;
            return;
        }

        if (state.humanReactionDelay > 0) {
            state.humanReactionDelay--;
            if (state.humanReactionDelay == 0 && player.fishHook != null) {
                reelIn(player, state);
            }
            return;
        }

        FishingBobberEntity bobber = player.fishHook;
        if (bobber == null) {
            cast(player, state);
            return;
        }

        // Avoid reeling when bobber is hooked in entity (squid, drowned, etc.) — use public API
        if (bobber.getHookedEntity() != null) {
            state.humanReactionDelay = MIN_REACTION_TIME + ThreadLocalRandom.current().nextInt(MAX_REACTION_TIME - MIN_REACTION_TIME);
            return;
        }

        Vec3d currentPos = new Vec3d(bobber.getX(), bobber.getY(), bobber.getZ());
        if (checkBobberStuck(state, currentPos)) {
            LOGGER.info("Feeshman Deelux: Bobber stuck, recasting...");
            player.sendMessage(net.minecraft.text.Text.literal("§e⚠️ Bobber stuck detected, recasting..."), false);
            recast(player, state);
        }
    }

    private static boolean isAutoFishEnabled(ServerPlayerEntity player, PlayerFishingState state) {
        if (state.autoFishEnabled != null) {
            return state.autoFishEnabled;
        }
        return FeeshmanConfig.isAutoFishEnabled();
    }

    public static void setAutoFishEnabled(ServerPlayerEntity player, boolean enabled) {
        PlayerFishingState state = PLAYER_STATES.computeIfAbsent(player.getUuid(), u -> new PlayerFishingState());
        state.autoFishEnabled = enabled;
    }

    public static Boolean getAutoFishEnabled(ServerPlayerEntity player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUuid());
        if (state == null || state.autoFishEnabled == null) {
            return null;
        }
        return state.autoFishEnabled;
    }

    private static void cast(ServerPlayerEntity player, PlayerFishingState state) {
        player.interactionManager.interactItem(player, player.getEntityWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        state.recastDelayTicks = BASE_RECAST_DELAY + ThreadLocalRandom.current().nextInt(20);
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;
    }

    private static void reelIn(ServerPlayerEntity player, PlayerFishingState state) {
        Map<String, Integer> before = snapshotItemCounts(player);
        player.interactionManager.interactItem(player, player.getEntityWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        state.recastDelayTicks = BASE_RECAST_DELAY + ThreadLocalRandom.current().nextInt(40);
        state.totalFishCaught++;
        FeeshLeaderboard.addCatch(player);
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;

        if (player.getEntityWorld() != null) {
            FeeshmanServerCommands.trackBiomeCatch(player, player.getEntityWorld().getBiome(player.getBlockPos()));
        }

        sendItemAnnouncementIfDetected(player, before);

        int lifetime = FeeshLeaderboard.getPlayerTotal(player.getName().getString());
        int biomes = FeeshmanServerCommands.getBiomeCount(player);
        String compliment = ThreadLocalRandom.current().nextFloat() < 0.05f
                ? LUCKY_COMPLIMENTS[ThreadLocalRandom.current().nextInt(LUCKY_COMPLIMENTS.length)]
                : "";
        if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.FishCaughtPayload.ID)) {
            ServerPlayNetworking.send(player, new FeeshmanPayloads.FishCaughtPayload(
                    state.totalFishCaught, lifetime, compliment, biomes));
        }

        if (state.totalFishCaught % 5 == 0) {
            player.sendMessage(net.minecraft.text.Text.literal("§a" + state.totalFishCaught + " fish caught this session!"), false);
        }
    }

    private static Map<String, Integer> snapshotItemCounts(ServerPlayerEntity player) {
        Map<String, Integer> counts = new HashMap<>();
        for (ItemStack stack : player.getInventory()) {
            if (!stack.isEmpty()) {
                String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                counts.merge(id, stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    private static void sendItemAnnouncementIfDetected(ServerPlayerEntity player, Map<String, Integer> before) {
        Map<String, Integer> after = snapshotItemCounts(player);
        for (Map.Entry<String, Integer> e : after.entrySet()) {
            int prev = before.getOrDefault(e.getKey(), 0);
            if (e.getValue() > prev) {
                String itemId = e.getKey();
                ItemStack sample = findStackOf(player, itemId);
                boolean hasEnchantments = sample != null && !sample.getEnchantments().isEmpty();
                if (ServerPlayNetworking.canSend(player, FeeshmanPayloads.ItemAnnouncementPayload.ID)) {
                    ServerPlayNetworking.send(player, new FeeshmanPayloads.ItemAnnouncementPayload(itemId, hasEnchantments));
                }
                return;
            }
        }
    }

    private static ItemStack findStackOf(ServerPlayerEntity player, String itemId) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return null;
        var item = net.minecraft.registry.Registries.ITEM.get(id);
        if (item == Items.AIR) return null;
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() == item) return stack;
        }
        return null;
    }

    private static void recast(ServerPlayerEntity player, PlayerFishingState state) {
        player.interactionManager.interactItem(player, player.getEntityWorld(), player.getMainHandStack(), Hand.MAIN_HAND);
        state.recastDelayTicks = 40;
        state.bobberStuckCheckPos = null;
        state.bobberStuckTicks = 0;
    }

    private static boolean checkBobberStuck(PlayerFishingState state, Vec3d currentPos) {
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
        if (state.bobberStuckTicks >= BOBBER_STUCK_THRESHOLD) {
            return true;
        }
        return false;
    }

    public static int getSessionFishCount(ServerPlayerEntity player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUuid());
        return state != null ? state.totalFishCaught : 0;
    }

    public static void resetSession(ServerPlayerEntity player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUuid());
        if (state != null) {
            state.totalFishCaught = 0;
            state.sessionStartTime = System.currentTimeMillis();
        }
    }

    public static long getSessionStartTime(ServerPlayerEntity player) {
        PlayerFishingState state = PLAYER_STATES.get(player.getUuid());
        return state != null ? state.sessionStartTime : System.currentTimeMillis();
    }

    private static class PlayerFishingState {
        Boolean autoFishEnabled = null;
        int recastDelayTicks = 0;
        int humanReactionDelay = 0;
        int totalFishCaught = 0;
        long sessionStartTime = System.currentTimeMillis();
        int noRodGraceTicks = 0;
        Vec3d bobberStuckCheckPos = null;
        int bobberStuckTicks = 0;
        Vec3d lastValidBobberPos = null;
        boolean durabilityWarned = false;
    }
}
