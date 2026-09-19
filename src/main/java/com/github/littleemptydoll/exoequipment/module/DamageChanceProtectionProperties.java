package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamageChanceProtectionProperties(
        double chance
) {
    public static final Codec<DamageChanceProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("chance")
                                    .forGetter(DamageChanceProtectionProperties::chance)
                    ).apply(instance, DamageChanceProtectionProperties::new)
            );

    public DamageChanceProtectionProperties {
        if (!Double.isFinite(chance)
                || chance < 0.0D
                || chance > 1.0D) {
            throw new IllegalArgumentException(
                    "Damage protection chance must be between 0 and 1"
            );
        }
    }
}
