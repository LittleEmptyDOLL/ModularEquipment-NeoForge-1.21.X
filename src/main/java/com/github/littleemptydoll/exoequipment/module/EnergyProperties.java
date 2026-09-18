package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnergyProperties(
        int consumption,
        int priority
) {
    public static final Codec<EnergyProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("consumption")
                                    .forGetter(EnergyProperties::consumption),
                            Codec.INT.optionalFieldOf("priority", 0)
                                    .forGetter(EnergyProperties::priority)
                    ).apply(instance, EnergyProperties::new)
            );

    public EnergyProperties(int consumption) {
        this(consumption, 0);
    }

    public EnergyProperties {
        if (consumption < 0) {
            throw new IllegalArgumentException(
                    "Energy consumption cannot be negative"
            );
        }
    }
}
