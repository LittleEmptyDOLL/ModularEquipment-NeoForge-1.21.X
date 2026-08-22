package com.github.littleemptydoll.exoequipment.frame;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record Frame(
        ResourceLocation definitionId
) {
    public static final Codec<Frame> CODEC =
            ResourceLocation.CODEC.xmap(
                    Frame::new,
                    Frame::definitionId
            );
}
