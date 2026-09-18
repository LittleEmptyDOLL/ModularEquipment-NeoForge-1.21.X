package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record DamageReductionProperties(
        Map<ResourceLocation, Double> reductions
) {
    public static final Codec<DamageReductionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.unboundedMap(
                                            ResourceLocation.CODEC,
                                            Codec.DOUBLE
                                    )
                                    .fieldOf("reductions")
                                    .forGetter(DamageReductionProperties::reductions)
                    ).apply(
                            instance,
                            DamageReductionProperties::new
                    )
            );

    public DamageReductionProperties {
        reductions = Map.copyOf(reductions);

        for (Map.Entry<ResourceLocation, Double> entry : reductions.entrySet()) {
            double reduction = entry.getValue();

            if (!Double.isFinite(reduction)
                    || reduction < 0.0D
                    || reduction > 1.0D) {
                throw new IllegalArgumentException(
                        "Damage reduction must be between 0 and 1: "
                                + entry.getKey()
                                + " = "
                                + reduction
                );
            }
        }
    }

    public double reduction(ResourceLocation damageType) {
        return Math.min(
                1.0D,
                Math.max(
                        0.0D,
                        reductions.getOrDefault(damageType, 0.0D)
                )
        );
    }
}
