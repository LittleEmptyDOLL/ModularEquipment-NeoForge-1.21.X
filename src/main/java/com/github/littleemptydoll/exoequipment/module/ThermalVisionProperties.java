package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ThermalVisionProperties(int activeConsumption) {
    public static final Codec<ThermalVisionProperties> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("active_consumption", 0)
                            .forGetter(ThermalVisionProperties::activeConsumption)
            ).apply(instance, ThermalVisionProperties::new));

    public ThermalVisionProperties {
        if (activeConsumption < 0) {
            throw new IllegalArgumentException("Thermal vision active consumption cannot be negative");
        }
    }
}
