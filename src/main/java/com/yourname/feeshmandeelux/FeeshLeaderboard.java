package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.db.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Facade over SQLite DAOs ({@link FeeshmanDatabase}). Loads when the server starts, not here.
 */
public final class FeeshLeaderboard {

    private FeeshLeaderboard() {
    }

    /** No-op retained for callers; DB opens via {@link net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents}. */
    public static void load() {
    }

    public static void flushIfDirty() {
    }

    public static void syncPlayer(ServerPlayer player) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return;
        }
        long now = System.currentTimeMillis();
        FeeshmanDatabase.write(c -> PlayersDao.upsertSeen(c, player.getStringUUID(),
                player.getName().getString(), now));
    }

    /** @return SQLite row id for sessions table */
    public static long beginSession(ServerPlayer player) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return -1L;
        }
        return FeeshmanDatabase.writeReturning(
                c -> SessionsDao.start(c, player.getStringUUID(), System.currentTimeMillis()), -1L);
    }

    public static void endSession(long sessionRowId, int fishCaught) {
        if (!FeeshmanDatabase.isOpen() || sessionRowId <= 0) {
            return;
        }
        FeeshmanDatabase.write(c ->
                SessionsDao.finish(c, sessionRowId, System.currentTimeMillis(), fishCaught));
    }

    /**
     * @param sessionFishAfterCatch fish count for this login session after incrementing by this catch.
     */
    public static int recordCatch(ServerPlayer player, String biomeKey, CatchDelta delta,
                                  int sessionFishAfterCatch) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return 0;
        }
        int fallback = getPlayerTotal(player);
        return FeeshmanDatabase.writeReturning(c -> CatchesDao.recordCatch(
                        c,
                        player.getStringUUID(),
                        player.getName().getString(),
                        biomeKey != null ? biomeKey : "unknown",
                        delta,
                        System.currentTimeMillis(),
                        sessionFishAfterCatch),
                fallback);
    }

    public static void addFishingSeconds(ServerPlayer player, int seconds) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return;
        }
        FeeshmanDatabase.write(c ->
                PlayerStatsDao.addFishingSeconds(c, player.getStringUUID(), seconds));
    }

    /** @deprecated Use {@link #recordCatch(ServerPlayer, String, CatchDelta, int)} */
    @Deprecated
    public static void addCatch(Player player) {
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        String biomeKey = sp.level() != null
                ? sp.level().getBiome(sp.blockPosition()).unwrapKey()
                .map(k -> k.identifier().toString()).orElse("unknown")
                : "unknown";
        int session = AutoFishService.getSessionFishCount(sp);
        recordCatch(sp, biomeKey, CatchDelta.unknown(), session);
    }

    public static int getPlayerTotal(Player player) {
        if (player == null) {
            return 0;
        }
        if (!FeeshmanDatabase.isOpen()) {
            return 0;
        }
        return readLifetimeByUuid(player.getStringUUID());
    }

    public static int readLifetimeByUuid(String uuidStr) {
        return FeeshmanDatabase.read(c -> LeaderboardDao.lifetimeByUuid(c, uuidStr), 0);
    }

    public static String exportAchievementCsv(ServerPlayer player) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return "";
        }
        List<String> ids = FeeshmanDatabase.read(c -> AchievementsDao.listUnlockedIds(c, player.getStringUUID()),
                Collections.emptyList());
        StringJoiner j = new StringJoiner(",");
        for (String id : ids) {
            j.add(id);
        }
        return j.toString();
    }

    public static Map<String, Integer> getBiomeBreakdown(ServerPlayer player) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return Map.of();
        }
        return FeeshmanDatabase.read(c ->
                CatchesDao.biomeBreakdown(c, player.getStringUUID()), Map.of());
    }

    public static int getBiomeDistinctCount(ServerPlayer player) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return 0;
        }
        return FeeshmanDatabase.read(c ->
                CatchesDao.countDistinctBiomes(c, player.getStringUUID()), 0);
    }

    public static List<Map.Entry<String, Integer>> getTopSince(Instant since, int limit) {
        long ms = since.toEpochMilli();
        return FeeshmanDatabase.read(c -> CatchesDao.topSince(c, ms, limit), List.of());
    }

    public static Instant startOfUtcDay() {
        return Instant.now().truncatedTo(ChronoUnit.DAYS);
    }

    public static Instant minusDaysUtc(int days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }

    public static List<Map.Entry<String, Integer>> topItems(int limit) {
        return FeeshmanDatabase.read(c -> CatchesDao.topItems(c, limit), List.of());
    }

    public static List<String> recentCatchLines(ServerPlayer player, int limit) {
        if (player == null || !FeeshmanDatabase.isOpen()) {
            return List.of();
        }
        return FeeshmanDatabase.read(c ->
                CatchesDao.recentCatchLines(c, player.getStringUUID(), limit), List.of());
    }

    /** Legacy name-based lookup (rename-safe via last_known_name). */
    public static int getPlayerTotal(String displayName) {
        return FeeshmanDatabase.read(c -> LeaderboardDao.lifetimeByDisplayName(c, displayName), 0);
    }

    public static List<Map.Entry<String, Integer>> getTop(int limit) {
        return FeeshmanDatabase.read(c -> LeaderboardDao.topAllTime(c, limit), List.of());
    }

    public static void saveAutoFishPreference(String uuid, boolean enabled) {
        if (!FeeshmanDatabase.isOpen()) return;
        FeeshmanDatabase.write(c -> KvStore.put(c, "autofish." + uuid, enabled ? "1" : "0"));
    }

    public static Boolean loadAutoFishPreference(String uuid) {
        if (!FeeshmanDatabase.isOpen()) return null;
        String val = FeeshmanDatabase.read(c -> KvStore.get(c, "autofish." + uuid).orElse(null), null);
        return val != null ? "1".equals(val) : null;
    }
}
