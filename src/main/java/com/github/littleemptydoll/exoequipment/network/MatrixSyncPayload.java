package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record MatrixSyncPayload(ItemStack matrix) implements CustomPacketPayload {

    public static final Type<MatrixSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "matrix_sync"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, MatrixSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    MatrixSyncPayload::matrix,
                    MatrixSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
