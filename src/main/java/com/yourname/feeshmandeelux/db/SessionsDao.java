package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class SessionsDao {

    private SessionsDao() {
    }

    /** @return SQLite row id, or negative on failure */
    public static long start(Connection c, String playerUuid, long startedAtMillis) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO sessions(player_uuid, started_at, fish_count) VALUES(?, ?, 0)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playerUuid);
            ps.setLong(2, startedAtMillis);
            ps.executeUpdate();
            var keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        try (PreparedStatement ps = c.prepareStatement("SELECT last_insert_rowid()");
             var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return -1L;
    }

    public static void finish(Connection c, long sessionRowId, long endedAtMillis, int fishCount) throws Exception {
        if (sessionRowId <= 0) {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement("""
                UPDATE sessions SET ended_at = ?, fish_count = ? WHERE id = ?
                """)) {
            ps.setLong(1, endedAtMillis);
            ps.setInt(2, Math.max(0, fishCount));
            ps.setLong(3, sessionRowId);
            ps.executeUpdate();
        }
    }
}
