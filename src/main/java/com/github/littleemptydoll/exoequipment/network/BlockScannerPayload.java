package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record BlockScannerPayload(List<BlockPos> positions)
        implements CustomPacketPayload {

    public static final Type<BlockScannerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "block_scanner"
            )
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, List<BlockPos>> POSITIONS_CODEC =
            ByteBufCodecs.collection(
                    ArrayList::new,
                    BlockPos.STREAM_CODEC
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockScannerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    POSITIONS_CODEC,
                    BlockScannerPayload::positions,
                    BlockScannerPayload::new
            );

    public BlockScannerPayload {
        positions = List.copyOf(positions);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
