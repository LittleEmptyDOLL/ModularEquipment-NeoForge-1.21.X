package com.github.littleemptydoll.exoequipment.network;

import com.github.littleemptydoll.exoequipment.ExoEquipment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Clientbound visual event for a shield impact.
 *
 * partIndex follows ShieldFieldMesh's render order. A negative part index means
 * the damage source had no useful spatial context and the client may use its
 * normal deterministic fallback. directionX/directionZ are normalized in the
 * player's body-local coordinate system and point from the player toward the
 * damage source.
 */
public record ShieldHitPayload(
        int entityId,
        int seed,
        int partIndex,
        float directionX,
        float directionZ,
        float energyLoss
) implements CustomPacketPayload {
    public static final Type<ShieldHitPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExoEquipment.MODID, "shield_hit")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldHitPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ShieldHitPayload::entityId,
                    ByteBufCodecs.VAR_INT, ShieldHitPayload::seed,
                    ByteBufCodecs.VAR_INT, ShieldHitPayload::partIndex,
                    ByteBufCodecs.FLOAT, ShieldHitPayload::directionX,
                    ByteBufCodecs.FLOAT, ShieldHitPayload::directionZ,
                    ByteBufCodecs.FLOAT, ShieldHitPayload::energyLoss,
                    ShieldHitPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
