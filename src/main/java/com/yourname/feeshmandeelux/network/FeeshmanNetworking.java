package com.yourname.feeshmandeelux.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers custom payload types for S2C sync. Must be called on both client and server.
 */
public final class FeeshmanNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(
                FeeshmanPayloads.FishCaughtPayload.ID,
                FeeshmanPayloads.FishCaughtPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                FeeshmanPayloads.ItemAnnouncementPayload.ID,
                FeeshmanPayloads.ItemAnnouncementPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                FeeshmanPayloads.DurabilityWarningPayload.ID,
                FeeshmanPayloads.DurabilityWarningPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(
                FeeshmanPayloads.StatsSyncPayload.ID,
                FeeshmanPayloads.StatsSyncPayload.CODEC);
    }
}
