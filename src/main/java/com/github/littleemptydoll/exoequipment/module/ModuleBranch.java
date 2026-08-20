package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public enum ModuleBranch {
    BASIC,
    CIVILIAN,
    MILITARY,
    ENGINEERING,
    EXPERIMENTAL;

    public static final Codec<ModuleBranch> CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    ModuleBranch.valueOf(
                                            value.toUpperCase(Locale.ROOT)
                                    )
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown module branch: " + value
                            );
                        }
                    },
                    branch -> branch.name().toLowerCase(Locale.ROOT)
            );
}
