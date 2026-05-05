package com.yourname.feeshmandeelux.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers custom payload types for S2C sync. Must be called on both client and server.
 */
public final class FeeshmanNetworking {

	public static void registerPayloads() {
		PayloadTypeRegistry.clientboundPlay().register(FeeshmanPayloads.FishCaughtPayload.TYPE,
				FeeshmanPayloads.FishCaughtPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FeeshmanPayloads.ItemAnnouncementPayload.TYPE,
				FeeshmanPayloads.ItemAnnouncementPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FeeshmanPayloads.DurabilityWarningPayload.TYPE,
				FeeshmanPayloads.DurabilityWarningPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FeeshmanPayloads.StatsSyncPayload.TYPE,
				FeeshmanPayloads.StatsSyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FeeshmanPayloads.AchievementsSyncPayload.TYPE,
				FeeshmanPayloads.AchievementsSyncPayload.CODEC);
	}
}
