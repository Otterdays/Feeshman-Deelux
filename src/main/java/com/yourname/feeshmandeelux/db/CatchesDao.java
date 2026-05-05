package com.yourname.feeshmandeelux.db;

import com.yourname.feeshmandeelux.CatchDelta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CatchesDao {

    private CatchesDao() {
    }

    /**
     * @return lifetime catch count after this catch
     */
    public static int recordCatch(Connection c, String uuid, String lastKnownName, String biomeKey,
                                   CatchDelta delta, long tsMillis, int sessionFishTotal) throws Exception {
        long now = tsMillis;
        PlayersDao.upsertSeen(c, uuid, lastKnownName, now);

        boolean treasureInc = delta.treasure();
        boolean junkInc = delta.junk();
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO catches(player_uuid, ts, biome, item_id, treasure, junk, enchanted)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """)) {
            ps.setString(1, uuid);
            ps.setLong(2, tsMillis);
            ps.setString(3, biomeKey);
            ps.setString(4, delta.itemId());
            ps.setInt(5, treasureInc ? 1 : 0);
            ps.setInt(6, junkInc ? 1 : 0);
            ps.setInt(7, delta.enchanted() ? 1 : 0);
            ps.executeUpdate();
        }

        int tInc = treasureInc ? 1 : 0;
        int jInc = junkInc ? 1 : 0;
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT OR IGNORE INTO player_stats(player_uuid) VALUES(?)
                """)) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("""
                UPDATE player_stats SET lifetime_catches = lifetime_catches + 1,
                    lifetime_treasure = lifetime_treasure + ?,
                    lifetime_junk = lifetime_junk + ?
                WHERE player_uuid = ?
                """)) {
            ps.setInt(1, tInc);
            ps.setInt(2, jInc);
            ps.setString(3, uuid);
            ps.executeUpdate();
        }

        if (biomeKey != null && !biomeKey.isBlank()) {
            try (PreparedStatement ps = c.prepareStatement("""
                    INSERT INTO biome_catches(player_uuid, biome, count) VALUES(?, ?, 1)
                    ON CONFLICT(player_uuid, biome) DO UPDATE SET count = count + 1
                    """)) {
                ps.setString(1, uuid);
                ps.setString(2, biomeKey);
                ps.executeUpdate();
            }
        }

        int lifetime = 0;
        int lt = 0;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT lifetime_catches, lifetime_treasure FROM player_stats WHERE player_uuid = ?")) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lifetime = rs.getInt(1);
                    lt = rs.getInt(2);
                }
            }
        }

        int biomeDistinct = countDistinctBiomes(c, uuid);
        AchievementEvaluation.evaluateAfterCatch(c, uuid, lifetime, sessionFishTotal, biomeDistinct,
                treasureInc, lt);

        return lifetime;
    }

    public static int countDistinctBiomes(Connection c, String uuid) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT COUNT(*) FROM biome_catches WHERE player_uuid = ? AND count > 0
                """)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static Map<String, Integer> biomeBreakdown(Connection c, String uuid) throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT biome, count FROM biome_catches WHERE player_uuid = ?
                ORDER BY count DESC
                """)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
        }
        return map;
    }

    public static List<String> recentCatchLines(Connection c, String uuid, int limit) throws Exception {
        List<String> lines = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT ts, biome, item_id, treasure, junk FROM catches
                WHERE player_uuid = ?
                ORDER BY ts DESC
                LIMIT ?
                """)) {
            ps.setString(1, uuid);
            ps.setInt(2, Math.max(1, Math.min(limit, 50)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long ts = rs.getLong(1);
                    String biome = rs.getString(2);
                    String item = rs.getString(3);
                    boolean tr = rs.getInt(4) == 1;
                    boolean junk = rs.getInt(5) == 1;
                    String tag = tr ? "[T]" : (junk ? "[J]" : "");
                    lines.add(String.format("[%s]%s %s @ %s", java.time.Instant.ofEpochMilli(ts),
                            tag, item != null ? item : "?", biome != null ? biome : "?"));
                }
            }
        }
        return lines;
    }

    public static List<Map.Entry<String, Integer>> topItems(Connection c, int limit) throws Exception {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT item_id, COUNT(*) AS c FROM catches
                WHERE item_id IS NOT NULL AND item_id <> ''
                GROUP BY item_id
                ORDER BY c DESC
                LIMIT ?
                """)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 50)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(Map.entry(rs.getString(1), rs.getInt(2)));
                }
            }
        }
        return list;
    }

    public static List<Map.Entry<String, Integer>> topSince(Connection c, long sinceTsMillis, int limit)
            throws Exception {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT p.last_known_name AS n, SUM(1) AS c
                FROM catches ch
                JOIN players p ON p.uuid = ch.player_uuid
                WHERE ch.ts >= ?
                GROUP BY ch.player_uuid
                ORDER BY c DESC
                LIMIT ?
                """)) {
            ps.setLong(1, sinceTsMillis);
            ps.setInt(2, Math.max(1, Math.min(limit, 50)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(Map.entry(rs.getString(1), rs.getInt(2)));
                }
            }
        }
        return list;
    }

    /** Import legacy leaderboard count for a UUID + display label (no synthetic catch rows). */
    public static void importLegacyTotals(Connection c, String uuid, String displayName,
                                          int lifetimeCatches) throws Exception {
        long now = System.currentTimeMillis();
        PlayersDao.upsertSeen(c, uuid, displayName != null ? displayName : uuid, now);
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT OR IGNORE INTO player_stats(player_uuid) VALUES (?)
                """)) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("""
                UPDATE player_stats SET lifetime_catches = MAX(lifetime_catches, ?)
                WHERE player_uuid = ?
                """)) {
            ps.setInt(1, Math.max(0, lifetimeCatches));
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
        AchievementEvaluation.applyImportedLifetime(c, uuid, lifetimeCatches);
    }
}
