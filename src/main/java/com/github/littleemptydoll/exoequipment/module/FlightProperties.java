package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FlightProperties(
        int activeConsumption
) {
    public static final Codec<FlightProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .optionalFieldOf("active_consumption", 0)
                                    .forGetter(FlightProperties::activeConsumption)
                    ).apply(instance, FlightProperties::new)
            );

    public FlightProperties {
        if (activeConsumption < 0) {
            throw new IllegalArgumentException("Flight active consumption cannot be negative");
        }
    }
}
