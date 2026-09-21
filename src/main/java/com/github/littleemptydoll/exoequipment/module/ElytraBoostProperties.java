package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ElytraBoostProperties(
        double acceleration,
        double maxSpeed
) {
    public static final Codec<ElytraBoostProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE.fieldOf("acceleration")
                                    .forGetter(ElytraBoostProperties::acceleration),
                            Codec.DOUBLE.fieldOf("max_speed")
                                    .forGetter(ElytraBoostProperties::maxSpeed)
                    ).apply(instance, ElytraBoostProperties::new)
            );

    public ElytraBoostProperties {
        if (!Double.isFinite(acceleration) || acceleration <= 0.0D) {
            throw new IllegalArgumentException("Elytra boost acceleration must be finite and positive");
        }
        if (!Double.isFinite(maxSpeed) || maxSpeed <= 0.0D) {
            throw new IllegalArgumentException("Elytra boost max speed must be finite and positive");
        }
    }
}
