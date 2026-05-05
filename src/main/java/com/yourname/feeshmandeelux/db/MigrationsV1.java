package com.yourname.feeshmandeelux.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initial schema for Feeshman stats (v1).
 */
public final class MigrationsV1 {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    private MigrationsV1() {
    }

    public static void apply(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INTEGER PRIMARY KEY
                    )""");
        }
        int version = 0;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next() && !rs.wasNull()) {
                version = rs.getInt(1);
            }
        }
        if (version >= 1) {
            return;
        }
        LOGGER.info("Feeshman Deelux: applying DB migration v1");
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS players (
                        uuid TEXT PRIMARY KEY,
                        last_known_name TEXT NOT NULL,
                        first_seen INTEGER,
                        last_seen INTEGER
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        player_uuid TEXT PRIMARY KEY REFERENCES players(uuid) ON DELETE CASCADE,
                        lifetime_catches INTEGER NOT NULL DEFAULT 0,
                        lifetime_treasure INTEGER NOT NULL DEFAULT 0,
                        lifetime_junk INTEGER NOT NULL DEFAULT 0,
                        lifetime_seconds INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS catches (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_uuid TEXT NOT NULL,
                        ts INTEGER NOT NULL,
                        biome TEXT,
                        item_id TEXT,
                        treasure INTEGER NOT NULL DEFAULT 0,
                        junk INTEGER NOT NULL DEFAULT 0,
                        enchanted INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_catches_player_ts ON catches(player_uuid, ts)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_catches_biome ON catches(biome)");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS biome_catches (
                        player_uuid TEXT NOT NULL,
                        biome TEXT NOT NULL,
                        count INTEGER NOT NULL,
                        PRIMARY KEY(player_uuid, biome)
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS achievements (
                        player_uuid TEXT NOT NULL,
                        achievement_id TEXT NOT NULL,
                        unlocked_at INTEGER NOT NULL,
                        PRIMARY KEY(player_uuid, achievement_id)
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_uuid TEXT NOT NULL,
                        started_at INTEGER NOT NULL,
                        ended_at INTEGER,
                        fish_count INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS kv (
                        key TEXT PRIMARY KEY,
                        value TEXT NOT NULL
                    )""");
            st.execute("INSERT INTO schema_version(version) VALUES (1)");
        }
    }
}
