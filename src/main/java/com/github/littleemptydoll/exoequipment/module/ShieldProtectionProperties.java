package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShieldProtectionProperties(
        double transfer
) {
    public static final Codec<ShieldProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("transfer")
                                    .forGetter(ShieldProtectionProperties::transfer)
                    ).apply(instance, ShieldProtectionProperties::new)
            );

    public ShieldProtectionProperties {
        if (!Double.isFinite(transfer)
                || transfer < 0.0D
                || transfer > 1.0D) {
            throw new IllegalArgumentException(
                    "Shield protection transfer must be between 0 and 1: "
                            + transfer
            );
        }
    }
}
