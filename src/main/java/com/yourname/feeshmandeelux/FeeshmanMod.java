package com.yourname.feeshmandeelux;

import com.yourname.feeshmandeelux.db.FeeshmanDatabase;
import com.yourname.feeshmandeelux.db.LegacyImporter;
import com.yourname.feeshmandeelux.network.FeeshmanNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

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

        Path dbPath = FabricLoader.getInstance().getConfigDir()
                .resolve("feeshmandeelux")
                .resolve("stats.sqlite");
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FeeshmanDatabase.open(dbPath);
            LegacyImporter.runIfNeeded();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> FeeshmanDatabase.close());

        // Dedicated server uses FeeshmanServerMod; only register here for integrated (single-player).
        // Avoid double-registration: PayloadTypeRegistry.register throws if called twice with same ID.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FeeshmanNetworking.registerPayloads();
            AutoFishService.register();
            FeeshmanServerCommands.register();
        }
    }
}
