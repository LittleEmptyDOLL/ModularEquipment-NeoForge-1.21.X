package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MobilityProperties(
        double movementSpeed,
        double swimSpeed,
        double jumpStrength,
        double stepHeight
) {
    public static final Codec<MobilityProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf("movement_speed", 0.0D)
                                    .forGetter(MobilityProperties::movementSpeed),
                            Codec.DOUBLE
                                    .optionalFieldOf("swim_speed", 0.0D)
                                    .forGetter(MobilityProperties::swimSpeed),
                            Codec.DOUBLE
                                    .optionalFieldOf("jump_strength", 0.0D)
                                    .forGetter(MobilityProperties::jumpStrength),
                            Codec.DOUBLE
                                    .optionalFieldOf("step_height", 0.0D)
                                    .forGetter(MobilityProperties::stepHeight)
                    ).apply(instance, MobilityProperties::new)
            );

    public MobilityProperties {
        validateNonNegative(movementSpeed, "movement speed");
        validateNonNegative(swimSpeed, "swim speed");
        validateNonNegative(jumpStrength, "jump strength");
        validateNonNegative(stepHeight, "step height");
    }

    private static void validateNonNegative(
            double value,
            String name
    ) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(
                    "Mobility " + name + " must be finite and non-negative"
            );
        }
    }
}
