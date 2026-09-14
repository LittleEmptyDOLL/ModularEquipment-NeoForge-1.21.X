package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenProfilePayload() implements CustomPacketPayload {
    public static final Type<OpenProfilePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "open_profile")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenProfilePayload> STREAM_CODEC =
            StreamCodec.unit(new OpenProfilePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
