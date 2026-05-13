package com.yourname.feeshmandeelux.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Registers custom payload types for S2C sync. Must be called on both client and server.
 */
public final class FeeshmanNetworking {

	public static void registerPayloads() {
		registerClientbound(FeeshmanPayloads.FishCaughtPayload.TYPE, FeeshmanPayloads.FishCaughtPayload.CODEC);
		registerClientbound(FeeshmanPayloads.ItemAnnouncementPayload.TYPE, FeeshmanPayloads.ItemAnnouncementPayload.CODEC);
		registerClientbound(FeeshmanPayloads.DurabilityWarningPayload.TYPE, FeeshmanPayloads.DurabilityWarningPayload.CODEC);
		registerClientbound(FeeshmanPayloads.StatsSyncPayload.TYPE, FeeshmanPayloads.StatsSyncPayload.CODEC);
		registerClientbound(FeeshmanPayloads.AchievementsSyncPayload.TYPE, FeeshmanPayloads.AchievementsSyncPayload.CODEC);
	}

	@SuppressWarnings("null")
	private static <T extends CustomPacketPayload> void registerClientbound(
			CustomPacketPayload.Type<T> type,
			StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		PayloadTypeRegistry.clientboundPlay().register(type, codec);
	}
}
