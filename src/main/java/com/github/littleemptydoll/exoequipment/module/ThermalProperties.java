package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ThermalProperties(
        int heatGeneration,
        int cooling
) {
    public static final Codec<ThermalProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("heat_generation")
                                    .forGetter(ThermalProperties::heatGeneration),
                            Codec.INT
                                    .fieldOf("cooling")
                                    .forGetter(ThermalProperties::cooling)
                    ).apply(
                            instance,
                            ThermalProperties::new
                    )
            );

    public ThermalProperties {
        if (heatGeneration < 0) {
            throw new IllegalArgumentException(
                    "Heat generation cannot be negative"
            );
        }

        if (cooling < 0) {
            throw new IllegalArgumentException(
                    "Cooling cannot be negative"
            );
        }
    }
}
