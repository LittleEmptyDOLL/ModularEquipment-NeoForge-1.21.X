package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShieldProperties(
        int capacity,
        double rechargeRate,
        int rechargeDelay
) {
    public static final Codec<ShieldProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("capacity")
                                    .forGetter(ShieldProperties::capacity),
                            Codec.DOUBLE
                                    .fieldOf("recharge_rate")
                                    .forGetter(ShieldProperties::rechargeRate),
                            Codec.INT
                                    .fieldOf("recharge_delay")
                                    .forGetter(ShieldProperties::rechargeDelay)
                    ).apply(
                            instance,
                            ShieldProperties::new
                    )
            );

    public ShieldProperties {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Shield capacity must be positive"
            );
        }

        if (!Double.isFinite(rechargeRate) || rechargeRate < 0.0D) {
            throw new IllegalArgumentException(
                    "Shield recharge rate must be finite and non-negative"
            );
        }

        if (rechargeDelay < 0) {
            throw new IllegalArgumentException(
                    "Shield recharge delay cannot be negative"
            );
        }
    }
}
