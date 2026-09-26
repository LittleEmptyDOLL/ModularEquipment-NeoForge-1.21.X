package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record DamageReductionProperties(
        Optional<Double> defaultReduction,
        Map<ResourceLocation, Double> reductions
) {
    public static final Codec<DamageReductionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE.optionalFieldOf("default")
                                    .forGetter(DamageReductionProperties::defaultReduction),
                            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
                                    .optionalFieldOf("reductions", Map.of())
                                    .forGetter(DamageReductionProperties::reductions)
                    ).apply(instance, DamageReductionProperties::new)
            );

    public DamageReductionProperties {
        if (defaultReduction == null) {
            defaultReduction = Optional.empty();
        }

        defaultReduction.ifPresent(DamageReductionProperties::validate);

        reductions = Map.copyOf(reductions);
        for (Map.Entry<ResourceLocation, Double> entry : reductions.entrySet()) {
            validate(entry.getValue());
        }
    }

    public DamageReductionProperties(Map<ResourceLocation, Double> reductions) {
        this(Optional.empty(), reductions);
    }

    public DamageReductionProperties(double defaultReduction, Map<ResourceLocation, Double> reductions) {
        this(Optional.of(defaultReduction), reductions);
    }

    private static void validate(double reduction) {
        if (!Double.isFinite(reduction) || reduction < 0.0D || reduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Damage reduction must be between 0 and 1: " + reduction
            );
        }
    }

    /**
     * Returns the reduction for a concrete damage type. A {@code null}
     * damage type represents the universal/default reduction and is used by
     * the characteristics UI when it calculates the generic defense value.
     */
    public double reduction(ResourceLocation damageType) {
        double reduction = defaultReduction.orElse(0.0D);

        if (damageType != null) {
            reduction = reductions.getOrDefault(
                    damageType,
                    reduction
            );
        }

        return Math.min(
                1.0D,
                Math.max(0.0D, reduction)
        );
    }
}
