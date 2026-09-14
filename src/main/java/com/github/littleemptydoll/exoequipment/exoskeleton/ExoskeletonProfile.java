package com.github.littleemptydoll.exoequipment.exoskeleton;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ExoskeletonProfile(
        String name,
        List<Integer> activeMatrices
) {
    private static final int MAX_NAME_LENGTH = 32;

    private static final Codec<ExoskeletonProfile> CODEC_WITH_NAME =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.STRING
                                    .fieldOf("name")
                                    .forGetter(ExoskeletonProfile::name),
                            Codec.INT.listOf()
                                    .fieldOf("active_matrices")
                                    .forGetter(ExoskeletonProfile::activeMatrices)
                    ).apply(
                            instance,
                            ExoskeletonProfile::new
                    )
            );

    private static final Codec<ExoskeletonProfile> LEGACY_CODEC =
            Codec.INT.listOf()
                    .xmap(
                            matrices -> new ExoskeletonProfile("Profile", matrices),
                            ExoskeletonProfile::activeMatrices
                    );

    public static final Codec<ExoskeletonProfile> CODEC =
            Codec.withAlternative(
                    CODEC_WITH_NAME,
                    LEGACY_CODEC
            );

    public ExoskeletonProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be blank");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Profile name cannot exceed " + MAX_NAME_LENGTH + " characters"
            );
        }
        activeMatrices = List.copyOf(activeMatrices);
    }

    public ExoskeletonProfile(List<Integer> activeMatrices) {
        this("Profile", activeMatrices);
    }

    public ExoskeletonProfile withName(String name) {
        return new ExoskeletonProfile(name, activeMatrices);
    }
}
