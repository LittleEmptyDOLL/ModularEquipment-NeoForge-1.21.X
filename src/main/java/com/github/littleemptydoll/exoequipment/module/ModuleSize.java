package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModuleSize(int width, int height) {
    public static final Codec<ModuleSize> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("width")
                                    .forGetter(ModuleSize::width),

                            Codec.INT.fieldOf("height")
                                    .forGetter(ModuleSize::height)
                    ).apply(instance, ModuleSize::new)
            );

    public ModuleSize {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Module size must be positive");
        }
    }

    public int area() {
        return width * height;
    }

    public static ModuleSize rotateSize(
            ModuleDefinition definition,
            int rotation
    ) {
        if (rotation % 180 == 0) {
            return definition.size();
        }

        return new ModuleSize(
                definition.size().height(),
                definition.size().width()
        );
    }
}
