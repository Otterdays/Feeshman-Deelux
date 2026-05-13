package com.yourname.feeshmandeelux.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

/**
 * Custom payloads for server-to-client sync (bite sound, HUD stats, announcements, toasts).
 */
public final class FeeshmanPayloads {

	private static @NonNull Identifier id(@NonNull String path) {
		return Identifier.fromNamespaceAndPath("feeshmandeelux", path);
	}

	private static <T extends CustomPacketPayload> CustomPacketPayload.@NonNull Type<T> payloadType(
			@NonNull String path) {
		return new CustomPacketPayload.Type<>(id(path));
	}

	@SuppressWarnings("null")
	private static StreamCodec<RegistryFriendlyByteBuf, FishCaughtPayload> fishCaughtCodec() {
		return StreamCodec.composite(
				ByteBufCodecs.VAR_INT, FishCaughtPayload::sessionFish,
				ByteBufCodecs.VAR_INT, FishCaughtPayload::lifetimeFish,
				ByteBufCodecs.STRING_UTF8, FishCaughtPayload::luckyCompliment,
				ByteBufCodecs.VAR_INT, FishCaughtPayload::biomeCount,
				FishCaughtPayload::new);
	}

	@SuppressWarnings("null")
	private static StreamCodec<RegistryFriendlyByteBuf, ItemAnnouncementPayload> itemAnnouncementCodec() {
		return StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, ItemAnnouncementPayload::itemId,
				ByteBufCodecs.BOOL, ItemAnnouncementPayload::hasEnchantments,
				ItemAnnouncementPayload::new);
	}

	@SuppressWarnings("null")
	private static StreamCodec<RegistryFriendlyByteBuf, DurabilityWarningPayload> durabilityWarningCodec() {
		return StreamCodec.composite(
				ByteBufCodecs.VAR_INT, DurabilityWarningPayload::remainingUses,
				DurabilityWarningPayload::new);
	}

	@SuppressWarnings("null")
	private static StreamCodec<RegistryFriendlyByteBuf, StatsSyncPayload> statsSyncCodec() {
		return StreamCodec.composite(
				ByteBufCodecs.VAR_INT, StatsSyncPayload::sessionFish,
				ByteBufCodecs.VAR_INT, StatsSyncPayload::lifetimeFish,
				ByteBufCodecs.VAR_LONG, StatsSyncPayload::sessionStartTime,
				ByteBufCodecs.VAR_INT, StatsSyncPayload::biomeCount,
				ByteBufCodecs.BOOL, StatsSyncPayload::autoFishEnabled,
				StatsSyncPayload::new);
	}

	@SuppressWarnings("null")
	private static StreamCodec<RegistryFriendlyByteBuf, AchievementsSyncPayload> achievementsSyncCodec() {
		return StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, AchievementsSyncPayload::achievementIdsCsv,
				AchievementsSyncPayload::new);
	}

	public record FishCaughtPayload(int sessionFish, int lifetimeFish, @NonNull String luckyCompliment, int biomeCount)
			implements CustomPacketPayload {
		public static final CustomPacketPayload.@NonNull Type<FishCaughtPayload> TYPE = payloadType("fish_caught");
		public static final StreamCodec<RegistryFriendlyByteBuf, FishCaughtPayload> CODEC = fishCaughtCodec();

		@Override
		public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record ItemAnnouncementPayload(@NonNull String itemId, boolean hasEnchantments) implements CustomPacketPayload {
		public static final CustomPacketPayload.@NonNull Type<ItemAnnouncementPayload> TYPE = payloadType("item_announcement");
		public static final StreamCodec<RegistryFriendlyByteBuf, ItemAnnouncementPayload> CODEC =
				itemAnnouncementCodec();

		@Override
		public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record DurabilityWarningPayload(int remainingUses) implements CustomPacketPayload {
		public static final CustomPacketPayload.@NonNull Type<DurabilityWarningPayload> TYPE = payloadType("durability_warning");
		public static final StreamCodec<RegistryFriendlyByteBuf, DurabilityWarningPayload> CODEC =
				durabilityWarningCodec();

		@Override
		public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record StatsSyncPayload(int sessionFish, int lifetimeFish, long sessionStartTime, int biomeCount,
	                               boolean autoFishEnabled)
			implements CustomPacketPayload {
		public static final CustomPacketPayload.@NonNull Type<StatsSyncPayload> TYPE = payloadType("stats_sync");
		public static final StreamCodec<RegistryFriendlyByteBuf, StatsSyncPayload> CODEC = statsSyncCodec();

		@Override
		public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Server-to-client comma-separated persisted achievement IDs (SQLite). */
	public record AchievementsSyncPayload(@NonNull String achievementIdsCsv) implements CustomPacketPayload {
		public static final CustomPacketPayload.@NonNull Type<AchievementsSyncPayload> TYPE = payloadType("achievements_sync");
		public static final StreamCodec<RegistryFriendlyByteBuf, AchievementsSyncPayload> CODEC =
				achievementsSyncCodec();

		@Override
		public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
