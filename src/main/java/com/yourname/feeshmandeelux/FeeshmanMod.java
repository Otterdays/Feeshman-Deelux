package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.network.FeeshmanNetworking;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common entrypoint. Runs on both client and server (including integrated).
 */
public class FeeshmanMod implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    @Override
    public void onInitialize() {
        LOGGER.info("Feeshman Deelux Initializing!");
        FeeshmanConfig.load();
        FeeshLeaderboard.load();
        FeeshmanNetworking.registerPayloads();
        AutoFishService.register();
        FeeshmanServerCommands.register();
    }
}
