package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BlinkPayload() implements CustomPacketPayload {
    public static final Type<BlinkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "blink")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlinkPayload> STREAM_CODEC =
            StreamCodec.unit(new BlinkPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
