package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RegenerationProperties(
        double healthPerSecond
) {
    public static final Codec<RegenerationProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("health_per_second")
                                    .forGetter(RegenerationProperties::healthPerSecond)
                    ).apply(instance, RegenerationProperties::new)
            );

    public RegenerationProperties {
        if (!Double.isFinite(healthPerSecond) || healthPerSecond < 0.0D) {
            throw new IllegalArgumentException(
                    "Health regeneration must be finite and non-negative"
            );
        }
    }
}
