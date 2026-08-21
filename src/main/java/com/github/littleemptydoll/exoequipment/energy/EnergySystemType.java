package com.github.littleemptydoll.exoequipment.energy;

import com.github.littleemptydoll.exoequipment.module.ModuleCategory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum EnergySystemType {
    CIVILIAN,
    MILITARY,
    EXPERIMENTAL;

    public static final Codec<EnergySystemType> CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    EnergySystemType.valueOf(
                                            value.toUpperCase(Locale.ROOT)
                                    )
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown energy system type: " + value
                            );
                        }
                    },
                    type -> type.name().toLowerCase(Locale.ROOT)
            );
}
