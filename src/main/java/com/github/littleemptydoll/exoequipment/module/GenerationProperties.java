package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GenerationProperties(
        int generation
) {
    public static final Codec<GenerationProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("generation")
                                    .forGetter(GenerationProperties::generation)
                    ).apply(
                            instance,
                            GenerationProperties::new
                    )
            );

    public GenerationProperties {
        if (generation < 0) {
            throw new IllegalArgumentException(
                    "Energy generation cannot be negative"
            );
        }
    }
}
