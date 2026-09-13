package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MatrixActionPayload(
        int action,
        int x,
        int y,
        int targetX,
        int targetY
) implements CustomPacketPayload {

    public static final Type<MatrixActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "matrix_action"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, MatrixActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::action,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::x,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::y,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::targetX,
                    ByteBufCodecs.VAR_INT,
                    MatrixActionPayload::targetY,
                    MatrixActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
