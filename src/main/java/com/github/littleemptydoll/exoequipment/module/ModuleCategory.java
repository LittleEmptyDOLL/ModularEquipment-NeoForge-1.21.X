package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ModuleCategory {
    ENERGY,
    COOLING,
    DEFENSE,
    MOBILITY,
    SURVIVAL,
    SENSOR,
    COMBAT,
    UTILITY,
    EXPERIMENTAL;

    public static final Codec<ModuleCategory> CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    ModuleCategory.valueOf(
                                            value.toUpperCase(Locale.ROOT)
                                    )
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown module category: " + value
                            );
                        }
                    },
                    category -> category.name().toLowerCase(Locale.ROOT)
            );
}
