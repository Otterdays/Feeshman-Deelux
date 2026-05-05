package com.yourname.feeshmandeelux.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public final class KvStore {

    private KvStore() {
    }

    public static Optional<String> get(Connection c, String key) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT value FROM kv WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString(1));
                }
            }
        }
        return Optional.empty();
    }

    public static void put(Connection c, String key, String value) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO kv(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }
}
