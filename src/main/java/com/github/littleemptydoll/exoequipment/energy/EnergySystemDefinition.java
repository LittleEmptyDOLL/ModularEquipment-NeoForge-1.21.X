package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.registry.types.EquipmentTier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record EnergySystemDefinition(
        ResourceLocation id,
        EquipmentTier tier,
        int maxInput,
        int maxOutput,
        double efficiency
) {
    public static final Codec<EnergySystemDefinition> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC
                                    .fieldOf("id")
                                    .forGetter(EnergySystemDefinition::id),
                            EquipmentTier.CODEC
                                    .fieldOf("tier")
                                    .forGetter(EnergySystemDefinition::tier),
                            Codec.INT
                                    .fieldOf("max_input")
                                    .forGetter(EnergySystemDefinition::maxInput),
                            Codec.INT
                                    .fieldOf("max_output")
                                    .forGetter(EnergySystemDefinition::maxOutput),
                            Codec.DOUBLE
                                    .fieldOf("efficiency")
                                    .forGetter(EnergySystemDefinition::efficiency)
                    ).apply(
                            instance,
                            EnergySystemDefinition::new
                    )
            );

    public EnergySystemDefinition {
        if (maxInput < 0) {
            throw new IllegalArgumentException(
                    "Maximum input cannot be negative"
            );
        }

        if (maxOutput < 0) {
            throw new IllegalArgumentException(
                    "Maximum output cannot be negative"
            );
        }

        if (efficiency < 0.0) {
            throw new IllegalArgumentException(
                    "Efficiency cannot be negative"
            );
        }
    }
}
