package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.mojang.serialization.Codec;

import java.util.List;

public record ExoskeletonProfile(
        List<Integer> activeMatrices
) {
    public static final Codec<ExoskeletonProfile> CODEC =
            Codec.INT.listOf()
                    .xmap(
                            ExoskeletonProfile::new,
                            ExoskeletonProfile::activeMatrices
                    );
}
