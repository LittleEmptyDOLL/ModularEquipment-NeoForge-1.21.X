package com.github.littleemptydoll.exoequipment.controller;

import com.github.littleemptydoll.exoequipment.registry.EquipmentDefinition;
import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public record ControllerDefinition (
        ResourceLocation id,
        EquipmentTier tier,
        Rarity rarity,
        int maxProfiles,
        int maxActiveMatrices
) implements EquipmentDefinition {
    public static final Codec<ControllerDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(ControllerDefinition::id),
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(ControllerDefinition::tier),
                            Rarity.CODEC
                                    .fieldOf("rarity")
                                    .forGetter(ControllerDefinition::rarity),
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
