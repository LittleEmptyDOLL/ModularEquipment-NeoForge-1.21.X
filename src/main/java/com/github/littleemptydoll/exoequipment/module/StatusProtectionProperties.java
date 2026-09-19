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
        protections = Map.copyOf(protections);

        for (Map.Entry<ResourceLocation, Double> entry : protections.entrySet()) {
            double protection = entry.getValue();

            if (!Double.isFinite(protection)
                    || protection < 0.0D
                    || protection > 1.0D) {
                throw new IllegalArgumentException(
                        "Status protection must be between 0 and 1"
                );
            }
        }
    }

    public double protection(ResourceLocation effectId) {
        return Math.min(
                1.0D,
                Math.max(
                        0.0D,
                        protections.getOrDefault(effectId, 0.0D)
                )
        );
    }
}
