package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TemperatureBonus(
        double minTemperature,
        double maxTemperature,
        double maximumBonus
) {
    public static final Codec<TemperatureBonus> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("min_temperature")
                                    .forGetter(TemperatureBonus::minTemperature),
                            Codec.DOUBLE
                                    .fieldOf("max_temperature")
                                    .forGetter(TemperatureBonus::maxTemperature),
                            Codec.DOUBLE
                                    .fieldOf("maximum_bonus")
                                    .forGetter(TemperatureBonus::maximumBonus)
                    ).apply(
                            instance,
                            TemperatureBonus::new
                    )
            );

    public TemperatureBonus {
        if (!Double.isFinite(minTemperature)
                || !Double.isFinite(maxTemperature)
                || !Double.isFinite(maximumBonus)) {
            throw new IllegalArgumentException(
                    "Temperature bonus values must be finite"
            );
        }

        if (minTemperature > maxTemperature) {
            throw new IllegalArgumentException(
                    "Temperature bonus minimum cannot exceed maximum"
            );
        }

        if (maximumBonus < 0.0D) {
            throw new IllegalArgumentException(
                    "Maximum temperature bonus cannot be negative"
            );
        }
    }
}
