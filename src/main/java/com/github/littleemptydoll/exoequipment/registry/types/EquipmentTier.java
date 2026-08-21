package com.github.littleemptydoll.exoequipment.registry.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum EquipmentTier {
    CIVILIAN,
    MILITARY,
    ENGINEERING,
    EXPERIMENTAL;

    public static final Codec<EquipmentTier> CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    EquipmentTier.valueOf(
                                            value.toUpperCase(Locale.ROOT)
                                    )
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown equipment tier: " + value
                            );
                        }
                    },
                    type -> type.name().toLowerCase(Locale.ROOT)
            );
}
