package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ExoskeletonDefinition(
        ResourceLocation id
) {
    public static final Codec<ExoskeletonDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ExoskeletonDefinition::id)
                    ).apply(
                            instance,
                            ExoskeletonDefinition::new
                    )
            );

    public ExoskeletonDefinition {}
}
