package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CloakingStatePayload(
        int entityId,
        boolean active
) implements CustomPacketPayload {
    public static final Type<CloakingStatePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "cloaking_state")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CloakingStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CloakingStatePayload::entityId,
                    ByteBufCodecs.BOOL, CloakingStatePayload::active,
                    CloakingStatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
