package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenExoskeletonPayload() implements CustomPacketPayload {

    public static final Type<OpenExoskeletonPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "open_exoskeleton"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenExoskeletonPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenExoskeletonPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
