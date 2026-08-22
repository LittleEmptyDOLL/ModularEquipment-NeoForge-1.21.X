package com.github.littleemptydoll.exoequipment.controller;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ControllerDefinition(
        ResourceLocation id,
        EquipmentTier tier,
        int maxProfiles
) {
    public static final Codec<ControllerDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ControllerDefinition::id),
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(ControllerDefinition::tier),
                            Codec.INT
                                    .fieldOf("max_profiles")
                                    .forGetter(ControllerDefinition::maxProfiles)
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
    }
}
