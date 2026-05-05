package com.yourname.feeshmandeelux.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Custom payloads for server-to-client sync (bite sound, HUD stats, announcements, toasts).
 */
public final class FeeshmanPayloads {

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("feeshmandeelux", path);
	}

	public record FishCaughtPayload(int sessionFish, int lifetimeFish, String luckyCompliment, int biomeCount)
			implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<FishCaughtPayload> TYPE = new CustomPacketPayload.Type<>(
				id("fish_caught"));
		public static final StreamCodec<RegistryFriendlyByteBuf, FishCaughtPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, FishCaughtPayload::sessionFish,
				ByteBufCodecs.VAR_INT, FishCaughtPayload::lifetimeFish,
				ByteBufCodecs.STRING_UTF8, FishCaughtPayload::luckyCompliment,
				ByteBufCodecs.VAR_INT, FishCaughtPayload::biomeCount,
				FishCaughtPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record ItemAnnouncementPayload(String itemId, boolean hasEnchantments) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<ItemAnnouncementPayload> TYPE = new CustomPacketPayload.Type<>(
				id("item_announcement"));
		public static final StreamCodec<RegistryFriendlyByteBuf, ItemAnnouncementPayload> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.STRING_UTF8, ItemAnnouncementPayload::itemId,
						ByteBufCodecs.BOOL, ItemAnnouncementPayload::hasEnchantments,
						ItemAnnouncementPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record DurabilityWarningPayload(int remainingUses) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<DurabilityWarningPayload> TYPE = new CustomPacketPayload.Type<>(
				id("durability_warning"));
		public static final StreamCodec<RegistryFriendlyByteBuf, DurabilityWarningPayload> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.VAR_INT, DurabilityWarningPayload::remainingUses,
						DurabilityWarningPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record StatsSyncPayload(int sessionFish, int lifetimeFish, long sessionStartTime, int biomeCount)
			implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<StatsSyncPayload> TYPE = new CustomPacketPayload.Type<>(
				id("stats_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, StatsSyncPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, StatsSyncPayload::sessionFish,
				ByteBufCodecs.VAR_INT, StatsSyncPayload::lifetimeFish,
				ByteBufCodecs.VAR_LONG, StatsSyncPayload::sessionStartTime,
				ByteBufCodecs.VAR_INT, StatsSyncPayload::biomeCount,
				StatsSyncPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Server-to-client comma-separated persisted achievement IDs (SQLite). */
	public record AchievementsSyncPayload(String achievementIdsCsv) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<AchievementsSyncPayload> TYPE = new CustomPacketPayload.Type<>(
				id("achievements_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, AchievementsSyncPayload> CODEC =
				StreamCodec.composite(
						ByteBufCodecs.STRING_UTF8, AchievementsSyncPayload::achievementIdsCsv,
						AchievementsSyncPayload::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
