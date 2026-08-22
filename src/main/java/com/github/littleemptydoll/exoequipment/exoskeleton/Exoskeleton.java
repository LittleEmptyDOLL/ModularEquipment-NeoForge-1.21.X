package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record Exoskeleton(
        ResourceLocation definitionId
) {
    public static final Codec<Exoskeleton> CODEC =
            ResourceLocation.CODEC.xmap(
                    Exoskeleton::new,
                    Exoskeleton::definitionId
            );
}
