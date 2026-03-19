package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.network.FeeshmanNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common entrypoint. Runs on both client and server.
 * On dedicated server, FeeshmanServerMod handles server init to avoid double registration.
 */
public class FeeshmanMod implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    @Override
    public void onInitialize() {
        LOGGER.info("Feeshman Deelux Initializing!");
        FeeshmanConfig.load();
        FeeshLeaderboard.load();
        // Dedicated server uses FeeshmanServerMod; only register here for integrated (single-player)
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FeeshmanNetworking.registerPayloads();
            AutoFishService.register();
            FeeshmanServerCommands.register();
        }
    }
}
