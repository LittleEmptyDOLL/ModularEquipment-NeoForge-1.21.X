package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HealthProperties(
        double additionalHealth
) {
    public static final Codec<HealthProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("additional_health")
                                    .forGetter(HealthProperties::additionalHealth)
                    ).apply(instance, HealthProperties::new)
            );

    public HealthProperties {
        if (!Double.isFinite(additionalHealth) || additionalHealth < 0.0D) {
            throw new IllegalArgumentException(
                    "Additional health must be finite and non-negative"
            );
        }
    }
}
