package com.yourname.feeshmandeelux.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Custom payloads for server-to-client sync (bite sound, HUD stats, announcements, toasts).
 */
public final class FeeshmanPayloads {

    public record FishCaughtPayload(int sessionFish, int lifetimeFish, String luckyCompliment, int biomeCount)
            implements CustomPayload {
        public static final CustomPayload.Id<FishCaughtPayload> ID = new CustomPayload.Id<>(Identifier.of("feeshmandeelux", "fish_caught"));
        public static final PacketCodec<RegistryByteBuf, FishCaughtPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, FishCaughtPayload::sessionFish,
                PacketCodecs.INTEGER, FishCaughtPayload::lifetimeFish,
                PacketCodecs.STRING, FishCaughtPayload::luckyCompliment,
                PacketCodecs.INTEGER, FishCaughtPayload::biomeCount,
                FishCaughtPayload::new);
        public static final CustomPayload.Type<RegistryByteBuf, FishCaughtPayload> TYPE = new CustomPayload.Type<>(ID, CODEC);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record ItemAnnouncementPayload(String itemId, boolean hasEnchantments) implements CustomPayload {
        public static final CustomPayload.Id<ItemAnnouncementPayload> ID = new CustomPayload.Id<>(Identifier.of("feeshmandeelux", "item_announcement"));
        public static final PacketCodec<RegistryByteBuf, ItemAnnouncementPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ItemAnnouncementPayload::itemId,
                PacketCodecs.BOOLEAN, ItemAnnouncementPayload::hasEnchantments,
                ItemAnnouncementPayload::new);
        public static final CustomPayload.Type<RegistryByteBuf, ItemAnnouncementPayload> TYPE = new CustomPayload.Type<>(ID, CODEC);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record DurabilityWarningPayload(int remainingUses) implements CustomPayload {
        public static final CustomPayload.Id<DurabilityWarningPayload> ID = new CustomPayload.Id<>(Identifier.of("feeshmandeelux", "durability_warning"));
        public static final PacketCodec<RegistryByteBuf, DurabilityWarningPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, DurabilityWarningPayload::remainingUses, DurabilityWarningPayload::new);
        public static final CustomPayload.Type<RegistryByteBuf, DurabilityWarningPayload> TYPE = new CustomPayload.Type<>(ID, CODEC);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record StatsSyncPayload(int sessionFish, int lifetimeFish, long sessionStartTime, int biomeCount) implements CustomPayload {
        public static final CustomPayload.Id<StatsSyncPayload> ID = new CustomPayload.Id<>(Identifier.of("feeshmandeelux", "stats_sync"));
        public static final PacketCodec<RegistryByteBuf, StatsSyncPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, StatsSyncPayload::sessionFish,
                PacketCodecs.INTEGER, StatsSyncPayload::lifetimeFish,
                PacketCodecs.LONG, StatsSyncPayload::sessionStartTime,
                PacketCodecs.INTEGER, StatsSyncPayload::biomeCount,
                StatsSyncPayload::new);
        public static final CustomPayload.Type<RegistryByteBuf, StatsSyncPayload> TYPE = new CustomPayload.Type<>(ID, CODEC);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
