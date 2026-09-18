package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record InstalledModuleReference(
        int matrixSlot,
        int moduleIndex
) {
    public static final Codec<InstalledModuleReference> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("matrix_slot")
                                    .forGetter(InstalledModuleReference::matrixSlot),
                            Codec.INT
                                    .fieldOf("module_index")
                                    .forGetter(InstalledModuleReference::moduleIndex)
                    ).apply(
                            instance,
                            InstalledModuleReference::new
                    )
            );

    public InstalledModuleReference {
        if (matrixSlot < 0) {
            throw new IllegalArgumentException("Matrix slot cannot be negative");
        }
        if (moduleIndex < 0) {
            throw new IllegalArgumentException("Module index cannot be negative");
        }
    }
}
