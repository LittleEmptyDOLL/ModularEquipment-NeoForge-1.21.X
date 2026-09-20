package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;

public record BodyDamageRegenerationProperties(
        double healthPerSecond,
        Set<BodyPart> bodyParts
) {
    public static final Codec<BodyDamageRegenerationProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("health_per_second")
                                    .forGetter(BodyDamageRegenerationProperties::healthPerSecond),
                            BodyPart.SET_CODEC
                                    .optionalFieldOf("body_parts", Set.of())
                                    .forGetter(BodyDamageRegenerationProperties::bodyParts)
                    ).apply(instance, BodyDamageRegenerationProperties::new)
            );

    public BodyDamageRegenerationProperties(double healthPerSecond) {
        this(healthPerSecond, Set.of());
    }

    public BodyDamageRegenerationProperties {
        bodyParts = Set.copyOf(bodyParts);

        if (!Double.isFinite(healthPerSecond) || healthPerSecond < 0.0D) {
            throw new IllegalArgumentException(
                    "Body damage regeneration must be finite and non-negative"
            );
        }
    }
}
