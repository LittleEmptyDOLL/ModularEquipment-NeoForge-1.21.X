package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ExoskeletonProfileNameSyncPayload(String name) implements CustomPacketPayload {
    public static final Type<ExoskeletonProfileNameSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "exoskeleton_profile_name_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ExoskeletonProfileNameSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(32),
                    ExoskeletonProfileNameSyncPayload::name,
                    ExoskeletonProfileNameSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
