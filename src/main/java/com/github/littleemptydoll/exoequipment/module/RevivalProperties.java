package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RevivalProperties(
        double restoreHealth,
        int cooldown,
        int invulnerabilityTicks
) {
    public static final Codec<RevivalProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("restore_health")
                                    .forGetter(RevivalProperties::restoreHealth),
                            Codec.INT
                                    .fieldOf("cooldown")
                                    .forGetter(RevivalProperties::cooldown),
                            Codec.INT
                                    .optionalFieldOf("invulnerability_ticks", 40)
                                    .forGetter(RevivalProperties::invulnerabilityTicks)
                    ).apply(instance, RevivalProperties::new)
            );

    public RevivalProperties {
        if (!Double.isFinite(restoreHealth) || restoreHealth <= 0.0D) {
            throw new IllegalArgumentException(
                    "Revival restore health must be finite and positive"
            );
        }

        if (cooldown < 0) {
            throw new IllegalArgumentException(
                    "Revival cooldown cannot be negative"
            );
        }

        if (invulnerabilityTicks < 0) {
            throw new IllegalArgumentException(
                    "Revival invulnerability duration cannot be negative"
            );
        }
    }
}
