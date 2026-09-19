package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HungerProperties(
        double exhaustionReduction
) {
    public static final Codec<HungerProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf("exhaustion_reduction", 0.0D)
                                    .forGetter(HungerProperties::exhaustionReduction)
                    ).apply(instance, HungerProperties::new)
            );

    public HungerProperties {
        if (!Double.isFinite(exhaustionReduction)
                || exhaustionReduction < 0.0D
                || exhaustionReduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Exhaustion reduction must be between 0 and 1"
            );
        }
    }
}
