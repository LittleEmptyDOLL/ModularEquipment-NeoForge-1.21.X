package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricatorCraftPayload(ResourceLocation recipeId, int amount) implements CustomPacketPayload {
    public static final Type<FabricatorCraftPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "fabricator_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorCraftPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, value) -> {
                        buffer.writeResourceLocation(value.recipeId());
                        buffer.writeVarInt(value.amount());
                    },
                    buffer -> new FabricatorCraftPayload(buffer.readResourceLocation(), buffer.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
