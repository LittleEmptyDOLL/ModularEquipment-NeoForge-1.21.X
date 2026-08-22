package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MatrixSlot(
        Optional<MatrixData> matrix
) {
    public static final Codec<MatrixSlot> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            MatrixData.CODEC
                                    .optionalFieldOf("matrix")
                                    .forGetter(MatrixSlot::matrix)
                    ).apply(
                            instance,
                            MatrixSlot::new
                    )
            );

    public static MatrixSlot empty() {
        return new MatrixSlot(Optional.empty());
    }
}
