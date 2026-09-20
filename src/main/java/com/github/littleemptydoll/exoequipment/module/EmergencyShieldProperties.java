package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EmergencyShieldProperties(
        double restore,
        int cooldown
) {
    public static final Codec<EmergencyShieldProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE.fieldOf("restore")
                                    .forGetter(EmergencyShieldProperties::restore),
                            Codec.INT.fieldOf("cooldown")
                                    .forGetter(EmergencyShieldProperties::cooldown)
                    ).apply(instance, EmergencyShieldProperties::new)
            );

    public EmergencyShieldProperties {
        if (!Double.isFinite(restore) || restore <= 0.0D || restore > 1.0D) {
            throw new IllegalArgumentException(
                    "Emergency shield restore must be greater than 0 and at most 1"
            );
        }
        if (cooldown < 0) {
            throw new IllegalArgumentException(
                    "Emergency shield cooldown cannot be negative"
            );
        }
    }
}
