package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CloakingProperties(
        int activationEnergy,
        int consumption,
        int cooldown
) {
    public static final Codec<CloakingProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("activation_energy")
                                    .forGetter(CloakingProperties::activationEnergy),
                            Codec.INT
                                    .fieldOf("consumption")
                                    .forGetter(CloakingProperties::consumption),
                            Codec.INT
                                    .fieldOf("cooldown")
                                    .forGetter(CloakingProperties::cooldown)
                    ).apply(instance, CloakingProperties::new)
            );

    public CloakingProperties {
        if (activationEnergy < 0) {
            throw new IllegalArgumentException("Cloaking activation energy cannot be negative");
        }
        if (consumption < 0) {
            throw new IllegalArgumentException("Cloaking consumption cannot be negative");
        }
        if (cooldown < 0) {
            throw new IllegalArgumentException("Cloaking cooldown cannot be negative");
        }
    }
}
