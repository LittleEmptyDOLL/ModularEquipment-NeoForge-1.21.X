package com.github.littleemptydoll.exoequipment.controller;

import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.EquipmentProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ControllerDefinition (
        ResourceLocation id,
        EquipmentProperties properties,
        int maxProfiles,
        int maxActiveMatrices
) implements EquipmentDefinition {
    public static final Codec<ControllerDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ControllerDefinition::id),
                            EquipmentProperties.CODEC
                                    .fieldOf("properties")
                                    .forGetter(ControllerDefinition::properties),
                            Codec.INT
                                    .fieldOf("max_profiles")
                                    .forGetter(ControllerDefinition::maxProfiles),
                            Codec.INT
                                    .fieldOf("max_active_matrices")
                                    .forGetter(ControllerDefinition::maxActiveMatrices)
                    ).apply(
                            instance,
                            ControllerDefinition::new
                    )
            );

    public ControllerDefinition {
        if (maxProfiles < 1) {
            throw new IllegalArgumentException(
                    "The maximum number of profiles must be greater than 0."
            );
        }

        if (maxActiveMatrices < 1) {
            throw new IllegalArgumentException(
                    "The maximum number of active matrices must be greater than 0."
            );
        }
    }
}
