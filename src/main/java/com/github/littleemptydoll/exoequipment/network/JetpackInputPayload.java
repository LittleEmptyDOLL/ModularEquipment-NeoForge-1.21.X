package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.module.JetpackInputState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record JetpackInputPayload(byte input) implements CustomPacketPayload {
    public static final Type<JetpackInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "jetpack_input"
            ));

    public static final StreamCodec<ByteBuf, JetpackInputPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BYTE,
                    JetpackInputPayload::input,
                    JetpackInputPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
