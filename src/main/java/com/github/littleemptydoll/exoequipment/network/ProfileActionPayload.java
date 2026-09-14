package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ProfileActionPayload(int action, int profile, int matrix) implements CustomPacketPayload {
    public static final int SELECT = 0;
    public static final int CREATE = 1;
    public static final int REMOVE = 2;
    public static final int TOGGLE_MATRIX = 3;

    public static final Type<ProfileActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "profile_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProfileActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ProfileActionPayload::action,
                    ByteBufCodecs.VAR_INT, ProfileActionPayload::profile,
                    ByteBufCodecs.VAR_INT, ProfileActionPayload::matrix,
                    ProfileActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
