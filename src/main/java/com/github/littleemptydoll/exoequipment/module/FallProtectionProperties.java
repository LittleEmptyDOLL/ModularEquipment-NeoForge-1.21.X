package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FallProtectionProperties(
        double damageReduction
) {
    public static final Codec<FallProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("damage_reduction")
                                    .forGetter(FallProtectionProperties::damageReduction)
                    ).apply(instance, FallProtectionProperties::new)
            );

    public FallProtectionProperties {
        if (!Double.isFinite(damageReduction)
                || damageReduction < 0.0D
                || damageReduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Fall damage reduction must be between 0 and 1"
            );
        }
    }
}
