package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RevivalState(
        InstalledModuleReference reference,
        int cooldown
) {
    public static final Codec<RevivalState> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            InstalledModuleReference.CODEC
                                    .fieldOf("reference")
                                    .forGetter(RevivalState::reference),
                            Codec.INT
                                    .fieldOf("cooldown")
                                    .forGetter(RevivalState::cooldown)
                    ).apply(instance, RevivalState::new)
            );

    public RevivalState {
        if (cooldown < 0) {
            throw new IllegalArgumentException(
                    "Revival cooldown cannot be negative"
            );
        }
    }

    public RevivalState tick() {
        return new RevivalState(reference, Math.max(0, cooldown - 1));
    }

    public boolean ready() {
        return cooldown <= 0;
    }
}
