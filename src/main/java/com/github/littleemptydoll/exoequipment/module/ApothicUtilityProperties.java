package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ApothicUtilityProperties(
        double experienceGained,
        double healingReceived,
        double dodgeChance
) {
    public static final Codec<ApothicUtilityProperties> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("experience_gained").forGetter(ApothicUtilityProperties::experienceGained),
                    Codec.DOUBLE.fieldOf("healing_received").forGetter(ApothicUtilityProperties::healingReceived),
                    Codec.DOUBLE.fieldOf("dodge_chance").forGetter(ApothicUtilityProperties::dodgeChance)
            ).apply(instance, ApothicUtilityProperties::new));

    public ApothicUtilityProperties {
        if (!Double.isFinite(experienceGained)
                || !Double.isFinite(healingReceived)
                || !Double.isFinite(dodgeChance)) {
            throw new IllegalArgumentException("Apothic utility values must be finite");
        }
    }
}
