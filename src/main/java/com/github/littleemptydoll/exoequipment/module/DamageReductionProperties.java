package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public record DamageReductionProperties(
        Optional<Double> defaultReduction,
        Map<ResourceLocation, Double> reductions,
        Map<ResourceLocation, Double> tagReductions
) {
    public static final Codec<DamageReductionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE.optionalFieldOf("default")
                                    .forGetter(DamageReductionProperties::defaultReduction),
                            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
                                    .optionalFieldOf("reductions", Map.of())
                                    .forGetter(DamageReductionProperties::reductions),
                            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
                                    .optionalFieldOf("tags", Map.of())
                                    .forGetter(DamageReductionProperties::tagReductions)
                    ).apply(instance, DamageReductionProperties::new)
            );

    public DamageReductionProperties {
        if (defaultReduction == null) {
            defaultReduction = Optional.empty();
        }

        defaultReduction.ifPresent(DamageReductionProperties::validate);

        reductions = reductions == null
                ? Map.of()
                : Map.copyOf(reductions);
        tagReductions = tagReductions == null
                ? Map.of()
                : Map.copyOf(tagReductions);

        for (Map.Entry<ResourceLocation, Double> entry : reductions.entrySet()) {
            validate(entry.getValue());
        }

        for (Map.Entry<ResourceLocation, Double> entry : tagReductions.entrySet()) {
            validate(entry.getValue());
        }
    }

    public DamageReductionProperties(
            Optional<Double> defaultReduction,
            Map<ResourceLocation, Double> reductions
    ) {
        this(defaultReduction, reductions, Map.of());
    }

    public DamageReductionProperties(Map<ResourceLocation, Double> reductions) {
        this(Optional.empty(), reductions, Map.of());
    }

    public DamageReductionProperties(
            double defaultReduction,
            Map<ResourceLocation, Double> reductions
    ) {
        this(Optional.of(defaultReduction), reductions, Map.of());
    }

    public DamageReductionProperties(
            Map<ResourceLocation, Double> reductions,
            Map<ResourceLocation, Double> tagReductions
    ) {
        this(Optional.empty(), reductions, tagReductions);
    }

    public DamageReductionProperties(
            double defaultReduction,
            Map<ResourceLocation, Double> reductions,
            Map<ResourceLocation, Double> tagReductions
    ) {
        this(Optional.of(defaultReduction), reductions, tagReductions);
    }

    private static void validate(double reduction) {
        if (!Double.isFinite(reduction) || reduction < 0.0D || reduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Damage reduction must be between 0 and 1: " + reduction
            );
        }
    }

    /**
     * Resolves reduction for a real incoming damage source.
     *
     * <p>A concrete damage-type entry has the highest precedence. If there
     * is no exact entry, matching tag reductions are considered and the
     * strongest matching tag wins. The default reduction is used only when
     * neither an exact type nor any configured tag matches.</p>
     */
    public double reductionForSource(DamageSource source) {
        if (source == null) {
            return reduction((ResourceLocation) null);
        }

        ResourceLocation damageType =
                source.typeHolder()
                        .unwrapKey()
                        .map(key -> key.location())
                        .orElse(null);

        return reduction(
                damageType,
                tagId -> source.typeHolder().is(
                        TagKey.create(
                                Registries.DAMAGE_TYPE,
                                tagId
                        )
                )
        );
    }

    /**
     * Returns the reduction for a concrete damage type without tag context.
     * A {@code null} damage type represents the universal/default reduction
     * and is used by the characteristics UI for the generic defense value.
     */
    public double reduction(ResourceLocation damageType) {
        return reduction(damageType, tagId -> false);
    }

    /**
     * Resolves a reduction with an externally supplied tag matcher. This is
     * used both by runtime damage handling and by the characteristics UI when
     * it evaluates one configured damage tag in isolation.
     */
    public double reduction(
            ResourceLocation damageType,
            Predicate<ResourceLocation> tagMatcher
    ) {
        if (damageType != null) {
            Double exactReduction = reductions.get(damageType);
            if (exactReduction != null) {
                return clamp(exactReduction);
            }
        }

        Double matchedTagReduction = null;

        if (tagMatcher != null) {
            for (Map.Entry<ResourceLocation, Double> entry : tagReductions.entrySet()) {
                if (!tagMatcher.test(entry.getKey())) {
                    continue;
                }

                matchedTagReduction = matchedTagReduction == null
                        ? entry.getValue()
                        : Math.max(
                                matchedTagReduction,
                                entry.getValue()
                        );
            }
        }

        return clamp(
                matchedTagReduction != null
                        ? matchedTagReduction
                        : defaultReduction.orElse(0.0D)
        );
    }

    private static double clamp(double reduction) {
        return Math.min(
                1.0D,
                Math.max(0.0D, reduction)
        );
    }
}
