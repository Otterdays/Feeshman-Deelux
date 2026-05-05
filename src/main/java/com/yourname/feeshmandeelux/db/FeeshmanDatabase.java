package com.yourname.feeshmandeelux.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Single embedded SQLite connection for the dedicated/integrated server.
 * All access is synchronized on one lock (simple, correct; typical write volume is low).
 */
public final class FeeshmanDatabase {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    private static final Object LOCK = new Object();
    private static Connection connection;

    private FeeshmanDatabase() {
    }

    public static boolean isOpen() {
        synchronized (LOCK) {
            try {
                return connection != null && !connection.isClosed();
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public static void open(Path sqliteFile) {
        synchronized (LOCK) {
            if (connection != null) {
                return;
            }
            try {
                Files.createDirectories(sqliteFile.getParent());
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    LOGGER.error("SQLite JDBC driver not found: {}", e.getMessage());
                    return;
                }
                String url = "jdbc:sqlite:" + sqliteFile.toAbsolutePath();
                connection = DriverManager.getConnection(url);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA journal_mode=WAL");
                    st.execute("PRAGMA synchronous=NORMAL");
                    st.execute("PRAGMA foreign_keys=ON");
                    st.execute("PRAGMA busy_timeout=2000");
                }
                MigrationsV1.apply(connection);
                LOGGER.info("Feeshman Deelux: SQLite opened at {}", sqliteFile.toAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Feeshman Deelux: failed to open SQLite: {}", e.getMessage(), e);
                closeQuietly();
            }
        }
    }

    public static void close() {
        synchronized (LOCK) {
            closeQuietly();
        }
    }

    private static void closeQuietly() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.warn("Feeshman Deelux: SQLite close: {}", e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    public static <T> T read(ThrowingFunction<Connection, T> fn, T ifNoDb) {
        synchronized (LOCK) {
            if (connection == null) {
                return ifNoDb;
            }
            try {
                return fn.apply(connection);
            } catch (Exception e) {
                LOGGER.error("Feeshman Deelux: DB read error: {}", e.getMessage(), e);
                return ifNoDb;
            }
        }
    }

    public static void write(ThrowingConsumer<Connection> fn) {
        synchronized (LOCK) {
            if (connection == null) {
                return;
            }
            try {
                fn.accept(connection);
            } catch (Exception e) {
                LOGGER.error("Feeshman Deelux: DB write error: {}", e.getMessage(), e);
            }
        }
    }

    public static <T> T writeReturning(ThrowingFunction<Connection, T> fn, T ifNoDb) {
        synchronized (LOCK) {
            if (connection == null) {
                return ifNoDb;
            }
            try {
                return fn.apply(connection);
            } catch (Exception e) {
                LOGGER.error("Feeshman Deelux: DB write error: {}", e.getMessage(), e);
                return ifNoDb;
            }
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
