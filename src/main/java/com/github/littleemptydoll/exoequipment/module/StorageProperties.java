package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StorageProperties(
        int capacity,
        int maxInput,
        int maxOutput
) {
    public static final Codec<StorageProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT
                                    .fieldOf("capacity")
                                    .forGetter(StorageProperties::capacity),
                            Codec.INT
                                    .fieldOf("max_input")
                                    .forGetter(StorageProperties::maxInput),
                            Codec.INT
                                    .fieldOf("max_output")
                                    .forGetter(StorageProperties::maxOutput)
                    ).apply(
                            instance,
                            StorageProperties::new
                    )
            );

    public StorageProperties {
        if (capacity < 0) {
            throw new IllegalArgumentException(
                    "Energy storage capacity cannot be negative"
            );
        }
        if (maxInput < 0) {
            throw new IllegalArgumentException(
                    "Energy storage max input cannot be negative"
            );
        }
        if (maxOutput < 0) {
            throw new IllegalArgumentException(
                    "Energy storage max output cannot be negative"
            );
        }
    }
}
