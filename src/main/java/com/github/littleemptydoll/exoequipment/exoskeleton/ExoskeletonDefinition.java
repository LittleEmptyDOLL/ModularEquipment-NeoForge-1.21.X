package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ExoskeletonDefinition(
        ResourceLocation id,
        EquipmentProperties properties
) implements EquipmentDefinition {
    public static final Codec<ExoskeletonDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ExoskeletonDefinition::id),
                            EquipmentProperties.CODEC
                                    .fieldOf("properties")
                                    .forGetter(ExoskeletonDefinition::properties)
                    ).apply(
                            instance,
                            ExoskeletonDefinition::new
                    )
            );
}
