package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CombatTogglePayload(byte kind) implements CustomPacketPayload {
    public static final byte LASER = 0;
    public static final byte DISCHARGE = 1;
    public static final Type<CombatTogglePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "combat_toggle"));
    public static final StreamCodec<ByteBuf, CombatTogglePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BYTE, CombatTogglePayload::kind,
                    CombatTogglePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
