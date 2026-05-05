package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class PlayerStatsDao {

    private PlayerStatsDao() {
    }

    public static void addFishingSeconds(Connection c, String playerUuid, int seconds) throws Exception {
        if (seconds <= 0) {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT OR IGNORE INTO player_stats(player_uuid) VALUES(?)")) {
            ps.setString(1, playerUuid);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement("""
                UPDATE player_stats SET lifetime_seconds = lifetime_seconds + ?
                WHERE player_uuid = ?
                """)) {
            ps.setInt(1, seconds);
            ps.setString(2, playerUuid);
            ps.executeUpdate();
        }
    }
}
