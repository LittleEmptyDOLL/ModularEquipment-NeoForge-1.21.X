package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EntityDetectionProperties(
        double range,
        boolean players,
        boolean mobs,
        boolean hostile
) {
    public static final Codec<EntityDetectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("range")
                                    .forGetter(EntityDetectionProperties::range),
                            Codec.BOOL
                                    .optionalFieldOf("players", false)
                                    .forGetter(EntityDetectionProperties::players),
                            Codec.BOOL
                                    .optionalFieldOf("mobs", false)
                                    .forGetter(EntityDetectionProperties::mobs),
                            Codec.BOOL
                                    .optionalFieldOf("hostile", false)
                                    .forGetter(EntityDetectionProperties::hostile)
                    ).apply(instance, EntityDetectionProperties::new)
            );

    public EntityDetectionProperties {
        if (!Double.isFinite(range) || range <= 0.0D) {
            throw new IllegalArgumentException(
                    "Entity detection range must be finite and positive"
            );
        }

        if (!players && !mobs && !hostile) {
            throw new IllegalArgumentException(
                    "Entity detection must enable at least one target type"
            );
        }
    }
}
