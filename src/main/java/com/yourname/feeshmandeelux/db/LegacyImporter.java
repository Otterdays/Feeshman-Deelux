package com.yourname.feeshmandeelux.db;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

/**
 * One-time migration from {@code config/feeshman-leaderboard.properties} into SQLite.
 */
public final class LegacyImporter {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");
    private static final String KV_KEY_DONE = "legacy_import_done";

    private LegacyImporter() {
    }

    public static void runIfNeeded() {
        Path propsPath = FabricLoader.getInstance().getConfigDir().resolve("feeshman-leaderboard.properties");

        Boolean[] didDeleteOutsideTxn = {Boolean.FALSE};
        FeeshmanDatabase.write(conn -> {
            conn.setAutoCommit(false);
            try {
                if ("1".equals(KvStore.get(conn, KV_KEY_DONE).orElse(""))) {
                    conn.commit();
                    return;
                }
                if (!Files.exists(propsPath)) {
                    KvStore.put(conn, KV_KEY_DONE, "1");
                    conn.commit();
                    LOGGER.info("Feeshman Deelux: no legacy leaderboard file; marked import done");
                    return;
                }

                Properties props = new Properties();
                try (var in = Files.newInputStream(propsPath)) {
                    props.load(in);
                }

                for (String name : props.stringPropertyNames()) {
                    if (name.endsWith(".name")) {
                        continue;
                    }
                    if (name.endsWith(".count")) {
                        String prefix = name.substring(0, name.length() - ".count".length());
                        int count = parseIntSafe(props.getProperty(name, "0"));
                        String pname = props.getProperty(prefix + ".name", prefix);
                        if (isLikelyUuid(prefix)) {
                            CatchesDao.importLegacyTotals(conn, prefix, pname, count);
                        }
                    } else if (!name.contains(".")) {
                        int count = parseIntSafe(props.getProperty(name, "0"));
                        UUID legacy = UUID.nameUUIDFromBytes(("legacy:" + name).getBytes(StandardCharsets.UTF_8));
                        CatchesDao.importLegacyTotals(conn, legacy.toString(), name, count);
                    }
                }

                KvStore.put(conn, KV_KEY_DONE, "1");
                conn.commit();
                didDeleteOutsideTxn[0] = true;
                LOGGER.info("Feeshman Deelux: legacy leaderboard imported into SQLite");
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
                LOGGER.error("Feeshman Deelux: legacy import failed: {}", e.getMessage(), e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ignored) {
                }
            }
        });

        if (didDeleteOutsideTxn[0]) {
            try {
                Files.deleteIfExists(propsPath);
            } catch (Exception e) {
                LOGGER.warn("Feeshman Deelux: could not delete legacy properties file: {}", e.getMessage());
            }
        }
    }

    private static boolean isLikelyUuid(String s) {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int parseIntSafe(String v) {
        if (v == null) return 0;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
