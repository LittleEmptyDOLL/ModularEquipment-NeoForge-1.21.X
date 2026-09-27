package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ThermalVisionProperties(double range, int activeConsumption) {
    public static final Codec<ThermalVisionProperties> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("range")
                            .forGetter(ThermalVisionProperties::range),
                    Codec.INT.optionalFieldOf("active_consumption", 0)
                            .forGetter(ThermalVisionProperties::activeConsumption)
            ).apply(instance, ThermalVisionProperties::new));

    public ThermalVisionProperties {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException("Thermal vision range must be finite and positive");
        }
        if (activeConsumption < 0) {
            throw new IllegalArgumentException("Thermal vision active consumption cannot be negative");
        }
    }
}
