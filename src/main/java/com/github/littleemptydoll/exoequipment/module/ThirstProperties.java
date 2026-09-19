package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ThirstProperties(
        double exhaustionReduction
) {
    public static final Codec<ThirstProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("exhaustion_reduction")
                                    .forGetter(ThirstProperties::exhaustionReduction)
                    ).apply(instance, ThirstProperties::new)
            );

    public ThirstProperties {
        if (!Double.isFinite(exhaustionReduction)
                || exhaustionReduction < 0.0D
                || exhaustionReduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Thirst exhaustion reduction must be between 0 and 1"
            );
        }
    }
}
