package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LeaderboardDao {

    private LeaderboardDao() {
    }

    public static List<Map.Entry<String, Integer>> topAllTime(Connection c, int limit) throws Exception {
        List<Map.Entry<String, Integer>> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT p.last_known_name, ps.lifetime_catches
                FROM players p
                JOIN player_stats ps ON ps.player_uuid = p.uuid
                ORDER BY ps.lifetime_catches DESC, p.last_known_name ASC
                LIMIT ?
                """)) {
            ps.setInt(1, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(Map.entry(rs.getString(1), rs.getInt(2)));
                }
            }
        }
        return list;
    }

    public static int lifetimeByUuid(Connection c, String uuid) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT lifetime_catches FROM player_stats WHERE player_uuid = ?")) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public static int lifetimeByDisplayName(Connection c, String displayName) throws Exception {
        if (displayName == null) {
            return 0;
        }
        String uuid = PlayersDao.uuidForName(c, displayName);
        if (uuid != null) {
            return lifetimeByUuid(c, uuid);
        }
        return 0;
    }
}
