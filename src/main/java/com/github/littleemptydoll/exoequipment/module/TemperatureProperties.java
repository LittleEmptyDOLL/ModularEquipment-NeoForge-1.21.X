package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record TemperatureProperties(
        double minTemperature,
        double maxTemperature,
        Optional<Double> efficiencyFalloff,
        Optional<TemperatureBonus> bonus
) {
    public static final double MIN_EFFICIENCY = 0.10D;

    public static final Codec<TemperatureProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("min_temperature")
                                    .forGetter(TemperatureProperties::minTemperature),
                            Codec.DOUBLE
                                    .fieldOf("max_temperature")
                                    .forGetter(TemperatureProperties::maxTemperature),
                            Codec.DOUBLE
                                    .optionalFieldOf("efficiency_falloff")
                                    .forGetter(TemperatureProperties::efficiencyFalloff),
                            TemperatureBonus.CODEC
                                    .optionalFieldOf("bonus")
                                    .forGetter(TemperatureProperties::bonus)
                    ).apply(
                            instance,
                            TemperatureProperties::new
                    )
            );

    public TemperatureProperties {
        if (!Double.isFinite(minTemperature)
                || !Double.isFinite(maxTemperature)) {
            throw new IllegalArgumentException(
                    "Module operating temperatures must be finite"
            );
        }

        if (minTemperature > maxTemperature) {
            throw new IllegalArgumentException(
                    "Minimum operating temperature cannot exceed maximum"
            );
        }

        efficiencyFalloff.ifPresent(value -> {
            if (!Double.isFinite(value) || value < 0.0D) {
                throw new IllegalArgumentException(
                        "Temperature efficiency falloff must be finite and non-negative"
                );
            }
        });
    }
}
