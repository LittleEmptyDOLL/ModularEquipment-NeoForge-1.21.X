package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public record ExoskeletonDefinition(
        ResourceLocation id,
        Rarity rarity
) {
    public static final Codec<ExoskeletonDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ExoskeletonDefinition::id),
                            Rarity.CODEC
                                    .fieldOf("rarity")
                                    .forGetter(ExoskeletonDefinition::rarity)
                    ).apply(
                            instance,
                            ExoskeletonDefinition::new
                    )
            );

    public ExoskeletonDefinition {}
}
