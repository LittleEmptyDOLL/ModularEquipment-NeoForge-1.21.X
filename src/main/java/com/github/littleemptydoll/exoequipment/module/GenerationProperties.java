package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GenerationProperties(
        int generation,
        int maxOutput
) {
    public static final Codec<GenerationProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("generation")
                                    .forGetter(GenerationProperties::generation),
                            Codec.INT
                                    .fieldOf("max_output")
                                    .forGetter(GenerationProperties::maxOutput)
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
        if (maxOutput < 0) {
            throw new IllegalArgumentException(
                    "Generator max output cannot be negative"
            );
        }
        if (maxOutput > generation) {
            throw new IllegalArgumentException(
                    "Generator max output cannot exceed generation"
            );
        }
    }
}
