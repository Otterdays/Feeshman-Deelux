package com.yourname.feeshmandeelux.db;

import com.yourname.feeshmandeelux.FeeshAchievementIds;

import java.sql.Connection;

/**
 * Unlocks achievement rows when thresholds are met (server-side).
 */
public final class AchievementEvaluation {

    private AchievementEvaluation() {
    }

    static void evaluateAfterCatch(Connection c, String uuid, int lifetimeFish, int sessionFish,
                                   int biomeDistinct, boolean treasureThisCatch,
                                   int lifetimeTreasureTotal) throws Exception {
        long now = System.currentTimeMillis();
        unlockIfGe(c, uuid, lifetimeFish, FeeshAchievementIds.FIRST_CAST, 1, now);

        unlockIfGe(c, uuid, sessionFish, FeeshAchievementIds.SESSION_10, 10, now);
        unlockIfGe(c, uuid, sessionFish, FeeshAchievementIds.SESSION_25, 25, now);
        unlockIfGe(c, uuid, sessionFish, FeeshAchievementIds.SESSION_50, 50, now);
        unlockIfGe(c, uuid, sessionFish, FeeshAchievementIds.SESSION_100, 100, now);

        unlockIfGe(c, uuid, lifetimeFish, FeeshAchievementIds.LIFETIME_100, 100, now);
        unlockIfGe(c, uuid, lifetimeFish, FeeshAchievementIds.LIFETIME_500, 500, now);
        unlockIfGe(c, uuid, lifetimeFish, FeeshAchievementIds.LIFETIME_1000, 1000, now);

        if (treasureThisCatch || lifetimeTreasureTotal > 0) {
            AchievementsDao.unlock(c, uuid, FeeshAchievementIds.TREASURE_HUNTER, now);
        }
        if (biomeDistinct >= 5) {
            AchievementsDao.unlock(c, uuid, FeeshAchievementIds.WORLD_EXPLORER, now);
        }
    }

    static void applyImportedLifetime(Connection c, String uuid, int lifetimeCatches) throws Exception {
        long now = System.currentTimeMillis();
        unlockIfGe(c, uuid, lifetimeCatches, FeeshAchievementIds.FIRST_CAST, 1, now);
        unlockIfGe(c, uuid, lifetimeCatches, FeeshAchievementIds.LIFETIME_100, 100, now);
        unlockIfGe(c, uuid, lifetimeCatches, FeeshAchievementIds.LIFETIME_500, 500, now);
        unlockIfGe(c, uuid, lifetimeCatches, FeeshAchievementIds.LIFETIME_1000, 1000, now);
    }

    private static void unlockIfGe(Connection c, String uuid, int value, String achievementId, int threshold,
                                   long now) throws Exception {
        if (value >= threshold) {
            AchievementsDao.unlock(c, uuid, achievementId, now);
        }
    }
}
