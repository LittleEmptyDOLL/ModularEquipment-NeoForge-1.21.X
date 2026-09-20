package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TemperatureModifierProperties(
        double temperature,
        double heatResistance,
        double coldResistance,
        double thermalResistance
) {
    public static final Codec<TemperatureModifierProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf("temperature", 0.0D)
                                    .forGetter(TemperatureModifierProperties::temperature),
                            Codec.DOUBLE
                                    .optionalFieldOf("heat_resistance", 0.0D)
                                    .forGetter(TemperatureModifierProperties::heatResistance),
                            Codec.DOUBLE
                                    .optionalFieldOf("cold_resistance", 0.0D)
                                    .forGetter(TemperatureModifierProperties::coldResistance),
                            Codec.DOUBLE
                                    .optionalFieldOf("thermal_resistance", 0.0D)
                                    .forGetter(TemperatureModifierProperties::thermalResistance)
                    ).apply(
                            instance,
                            TemperatureModifierProperties::new
                    )
            );
}
