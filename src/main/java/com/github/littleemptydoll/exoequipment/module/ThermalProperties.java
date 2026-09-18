package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ThermalProperties(
        double heatGeneration,
        double cooling
) {
    public static final Codec<ThermalProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("heat_generation")
                                    .forGetter(ThermalProperties::heatGeneration),
                            Codec.DOUBLE
                                    .fieldOf("cooling")
                                    .forGetter(ThermalProperties::cooling)
                    ).apply(
                            instance,
                            ThermalProperties::new
                    )
            );

    public ThermalProperties {
        if (heatGeneration < 0.0D) {
            throw new IllegalArgumentException(
                    "Heat generation cannot be negative"
            );
        }

        if (cooling < 0.0D) {
            throw new IllegalArgumentException(
                    "Cooling cannot be negative"
            );
        }
    }
}
