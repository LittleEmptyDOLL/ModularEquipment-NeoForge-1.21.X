package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TemperatureImpactProperties(
        double resistance
) {
    public TemperatureImpactProperties {
        if (resistance < 0.0D || resistance > 1.0D) {
            throw new IllegalArgumentException(
                    "Temperature impact resistance must be between 0 and 1"
            );
        }
    }

    public static final Codec<TemperatureImpactProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf("resistance", 0.0D)
                                    .forGetter(TemperatureImpactProperties::resistance)
                    ).apply(
                            instance,
                            TemperatureImpactProperties::new
                    )
            );
}
