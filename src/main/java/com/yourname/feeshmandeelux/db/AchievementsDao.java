package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class AchievementsDao {

    private AchievementsDao() {
    }

    /** @return true if newly inserted */
    public static boolean unlock(Connection c, String playerUuid, String achievementId, long nowMillis)
            throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT OR IGNORE INTO achievements(player_uuid, achievement_id, unlocked_at)
                VALUES(?, ?, ?)
                """)) {
            ps.setString(1, playerUuid);
            ps.setString(2, achievementId);
            ps.setLong(3, nowMillis);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<String> listUnlockedIds(Connection c, String playerUuid) throws Exception {
        List<String> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT achievement_id FROM achievements WHERE player_uuid = ?
                ORDER BY unlocked_at ASC
                """)) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
            }
        }
        return ids;
    }

    public static boolean has(Connection c, String playerUuid, String achievementId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT 1 FROM achievements WHERE player_uuid = ? AND achievement_id = ? LIMIT 1
                """)) {
            ps.setString(1, playerUuid);
            ps.setString(2, achievementId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
