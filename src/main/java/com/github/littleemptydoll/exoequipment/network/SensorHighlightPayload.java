package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SensorHighlightPayload(
        List<Integer> hostile,
        List<Integer> mobs,
        List<Integer> players
) implements CustomPacketPayload {

    public static final Type<SensorHighlightPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                    ExoEquipment.MODID,
                    "sensor_highlight"
            )
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Integer>> ENTITY_IDS_CODEC =
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ByteBufCodecs.VAR_INT
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SensorHighlightPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ENTITY_IDS_CODEC, SensorHighlightPayload::hostile,
                    ENTITY_IDS_CODEC, SensorHighlightPayload::mobs,
                    ENTITY_IDS_CODEC, SensorHighlightPayload::players,
                    SensorHighlightPayload::new
            );

    public SensorHighlightPayload {
        hostile = List.copyOf(hostile);
        mobs = List.copyOf(mobs);
        players = List.copyOf(players);
    }

    public static SensorHighlightPayload empty() {
        return new SensorHighlightPayload(
                List.of(),
                List.of(),
                List.of()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
