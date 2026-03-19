package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.network.FeeshmanNetworking;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server-side entrypoint for Feeshman Deelux.
 * Runs auto-fishing logic and commands on the server; no client mod required.
 */
public class FeeshmanServerMod implements DedicatedServerModInitializer {

    private static final Logger LOGGER = LogManager.getLogger("FeeshmanDeelux");

    @Override
    public void onInitializeServer() {
        LOGGER.info("Feeshman Deelux Server Initializing!");
        FeeshmanConfig.load();
        FeeshLeaderboard.load();
        FeeshmanNetworking.registerPayloads();
        AutoFishService.register();
        FeeshmanServerCommands.register();
    }
}
