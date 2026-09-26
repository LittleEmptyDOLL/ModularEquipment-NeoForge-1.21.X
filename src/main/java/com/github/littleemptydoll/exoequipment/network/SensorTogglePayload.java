package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SensorTogglePayload(byte kind) implements CustomPacketPayload {
    public static final byte ENTITY = 0;
    public static final byte BLOCK = 1;

    public static final Type<SensorTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "sensor_toggle")
    );

    public static final StreamCodec<ByteBuf, SensorTogglePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, SensorTogglePayload::kind,
                    SensorTogglePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
