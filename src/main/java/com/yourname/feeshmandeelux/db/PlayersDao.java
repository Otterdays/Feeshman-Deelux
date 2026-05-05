package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class PlayersDao {

    private PlayersDao() {
    }

    /**
     * Ensure player row exists and refresh display name + last_seen.
     */
    public static void upsertSeen(Connection c, String uuid, String lastKnownName, long nowMillis) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO players(uuid, last_known_name, first_seen, last_seen)
                VALUES(?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    last_known_name = excluded.last_known_name,
                    last_seen = excluded.last_seen
                """)) {
            ps.setString(1, uuid);
            ps.setString(2, lastKnownName);
            ps.setLong(3, nowMillis);
            ps.setLong(4, nowMillis);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR IGNORE INTO player_stats(player_uuid) VALUES(?)")) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        }
    }

    /** Resolve UUID by last_known_name match (exact, case-insensitive). */
    public static String uuidForName(Connection c, String displayName) throws Exception {
        if (displayName == null) return null;
        try (PreparedStatement ps = c.prepareStatement("""
                SELECT uuid FROM players WHERE lower(last_known_name) = lower(?)
                ORDER BY last_seen DESC LIMIT 1
                """)) {
            ps.setString(1, displayName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }
}
