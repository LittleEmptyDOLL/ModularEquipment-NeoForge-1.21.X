package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record StatusProtectionProperties(
        Map<ResourceLocation, Double> protections
) {
    public static final Codec<StatusProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                    ResourceLocation.CODEC,
                                    Codec.DOUBLE
                            )
                            .fieldOf("protections")
                            .forGetter(StatusProtectionProperties::protections)
                    ).apply(instance, StatusProtectionProperties::new)
            );

    public StatusProtectionProperties {
        if (protections == null) {
            throw new IllegalArgumentException(
                    "Status protections must not be null"
            );
        }

        for (Map.Entry<ResourceLocation, Double> entry : protections.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException(
                        "Status protections must not contain null entries"
                );
            }

            double protection = entry.getValue();

            if (!Double.isFinite(protection)
                    || protection < 0.0D
                    || protection > 1.0D) {
                throw new IllegalArgumentException(
                        "Status protection must be between 0 and 1"
                );
            }
        }

        protections = Map.copyOf(protections);
    }

    public double protection(ResourceLocation effectId) {
        if (effectId == null) {
            return 0.0D;
        }

        return protections.getOrDefault(effectId, 0.0D);
    }
}
