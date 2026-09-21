package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record JetpackProperties(
        double verticalThrust,
        double maxVerticalSpeed,
        double horizontalSpeed,
        int energyConsumption,
        Optional<ElytraBoostProperties> elytra
) {
    public static final Codec<JetpackProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE.fieldOf("vertical_thrust")
                                    .forGetter(JetpackProperties::verticalThrust),
                            Codec.DOUBLE.fieldOf("max_vertical_speed")
                                    .forGetter(JetpackProperties::maxVerticalSpeed),
                            Codec.DOUBLE.fieldOf("horizontal_speed")
                                    .forGetter(JetpackProperties::horizontalSpeed),
                            Codec.INT.optionalFieldOf("energy_consumption", 0)
                                    .forGetter(JetpackProperties::energyConsumption),
                            ElytraBoostProperties.CODEC.optionalFieldOf("elytra")
                                    .forGetter(JetpackProperties::elytra)
                    ).apply(instance, JetpackProperties::new)
            );

    public JetpackProperties {
        if (!Double.isFinite(verticalThrust) || verticalThrust <= 0.0D) {
            throw new IllegalArgumentException("Jetpack vertical thrust must be finite and positive");
        }
        if (!Double.isFinite(maxVerticalSpeed) || maxVerticalSpeed <= 0.0D) {
            throw new IllegalArgumentException("Jetpack max vertical speed must be finite and positive");
        }
        if (!Double.isFinite(horizontalSpeed) || horizontalSpeed < 0.0D) {
            throw new IllegalArgumentException("Jetpack horizontal speed must be finite and non-negative");
        }
        if (energyConsumption < 0) {
            throw new IllegalArgumentException("Jetpack energy consumption cannot be negative");
        }
    }
}
