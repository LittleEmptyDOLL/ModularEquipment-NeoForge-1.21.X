package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnergyProperties(
        int consumption
) {
    public static final Codec<EnergyProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("consumption")
                                    .forGetter(EnergyProperties::consumption)
                    ).apply(
                            instance,
                            EnergyProperties::new
                    )
            );

    public EnergyProperties {
        if (consumption < 0) {
            throw new IllegalArgumentException(
                    "Energy consumption cannot be negative"
            );
        }
    }
}
