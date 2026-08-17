package com.github.littleemptydoll.exoequipment.matrix;

import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record MatrixData(
        int width,
        int height,
        List<InstalledModule> modules
) {
    public static final Codec<MatrixData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("width")
                                    .forGetter(MatrixData::width),

                            Codec.INT.fieldOf("height")
                                    .forGetter(MatrixData::height),

                            InstalledModule.CODEC
                                    .listOf()
                                    .fieldOf("modules")
                                    .forGetter(MatrixData::modules)
                    ).apply(
                            instance,
                            MatrixData::new
                    )
            );

    public MatrixData {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Module size must be positive");
        }

        modules = List.copyOf(modules);
    }

    public static MatrixData empty(
            int width,
            int height
    ) {
        return new MatrixData(
                width,
                height,
                List.of()
        );
    }
}
