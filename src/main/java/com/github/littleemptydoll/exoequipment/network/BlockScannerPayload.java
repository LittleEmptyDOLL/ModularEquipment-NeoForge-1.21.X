package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import com.github.littleemptydoll.exoequipment.module.ScannedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record BlockScannerPayload(List<ScannedBlock> positions)
        implements CustomPacketPayload {

    public static final Type<BlockScannerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "block_scanner"
            )
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, ScannedBlock> SCANNED_BLOCK_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ScannedBlock::position,
                    ByteBufCodecs.VAR_INT,
                    ScannedBlock::color,
                    ScannedBlock::new
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, List<ScannedBlock>> POSITIONS_CODEC =
            ByteBufCodecs.collection(
                    ArrayList::new,
                    SCANNED_BLOCK_CODEC
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
