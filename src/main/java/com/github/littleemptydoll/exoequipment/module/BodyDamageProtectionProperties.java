package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;

public record BodyDamageProtectionProperties(
        double chance,
        double damageReduction,
        Set<BodyPart> bodyParts
) {
    public static final Codec<BodyDamageProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("chance")
                                    .forGetter(BodyDamageProtectionProperties::chance),
                            Codec.DOUBLE
                                    .fieldOf("damage_reduction")
                                    .forGetter(BodyDamageProtectionProperties::damageReduction),
                            BodyPart.SET_CODEC
                                    .optionalFieldOf("body_parts", Set.of())
                                    .forGetter(BodyDamageProtectionProperties::bodyParts)
                    ).apply(instance, BodyDamageProtectionProperties::new)
            );

    public BodyDamageProtectionProperties(double chance, double damageReduction) {
        this(chance, damageReduction, Set.of());
    }

    public BodyDamageProtectionProperties {
        bodyParts = Set.copyOf(bodyParts);

        if (!Double.isFinite(chance)
                || chance < 0.0D
                || chance > 1.0D) {
            throw new IllegalArgumentException(
                    "Body damage protection chance must be between 0 and 1"
            );
        }

        if (!Double.isFinite(damageReduction)
                || damageReduction < 0.0D
                || damageReduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Body damage reduction must be between 0 and 1"
            );
        }
    }
}
