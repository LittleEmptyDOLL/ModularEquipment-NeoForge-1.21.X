package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlinkProperties(
        double distance,
        int activationEnergy,
        int cooldown
) {
    public static final Codec<BlinkProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("distance")
                                    .forGetter(BlinkProperties::distance),
                            Codec.INT
                                    .fieldOf("activation_energy")
                                    .forGetter(BlinkProperties::activationEnergy),
                            Codec.INT
                                    .fieldOf("cooldown")
                                    .forGetter(BlinkProperties::cooldown)
                    ).apply(instance, BlinkProperties::new)
            );

    public BlinkProperties {
        if (!Double.isFinite(distance) || distance <= 0.0D) {
            throw new IllegalArgumentException("Blink distance must be finite and positive");
        }
        if (activationEnergy < 0) {
            throw new IllegalArgumentException("Blink activation energy cannot be negative");
        }
        if (cooldown < 0) {
            throw new IllegalArgumentException("Blink cooldown cannot be negative");
        }
    }
}
