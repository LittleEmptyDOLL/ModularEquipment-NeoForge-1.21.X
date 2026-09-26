package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.Map;
import java.util.function.Predicate;

public record StatusProtectionProperties(
        double harmfulProtection,
        Map<ResourceLocation, Double> protections,
        Map<ResourceLocation, Double> tagProtections
) {
    public static final Codec<StatusProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf(
                                            "harmful_protection",
                                            0.0D
                                    )
                                    .forGetter(
                                            StatusProtectionProperties::harmfulProtection
                                    ),
                            Codec.unboundedMap(
                                            ResourceLocation.CODEC,
                                            Codec.DOUBLE
                                    )
                                    .optionalFieldOf("protections", Map.of())
                                    .forGetter(
                                            StatusProtectionProperties::protections
                                    ),
                            Codec.unboundedMap(
                                            ResourceLocation.CODEC,
                                            Codec.DOUBLE
                                    )
                                    .optionalFieldOf("tags", Map.of())
                                    .forGetter(
                                            StatusProtectionProperties::tagProtections
                                    )
                    ).apply(instance, StatusProtectionProperties::new)
            );

    public StatusProtectionProperties(
            Map<ResourceLocation, Double> protections
    ) {
        this(0.0D, protections, Map.of());
    }

    public StatusProtectionProperties(
            double harmfulProtection,
            Map<ResourceLocation, Double> protections
    ) {
        this(harmfulProtection, protections, Map.of());
    }

    public StatusProtectionProperties(
            Map<ResourceLocation, Double> protections,
            Map<ResourceLocation, Double> tagProtections
    ) {
        this(0.0D, protections, tagProtections);
    }

    public StatusProtectionProperties {
        validate(harmfulProtection, "Harmful status protection");

        protections = copyAndValidate(
                protections,
                "Status protections"
        );
        tagProtections = copyAndValidate(
                tagProtections,
                "Status tag protections"
        );
    }

    public double protection(ResourceLocation effectId) {
        if (effectId == null) {
            return 0.0D;
        }

        return protections.getOrDefault(effectId, 0.0D);
    }

    public double protection(
            ResourceLocation effectId,
            boolean harmful
    ) {
        return protection(effectId, harmful, tagId -> false);
    }

    public double protectionFor(Holder<MobEffect> effect) {
        if (effect == null) {
            return 0.0D;
        }

        ResourceLocation effectId = effect.unwrapKey()
                .map(key -> key.location())
                .orElse(null);
        boolean harmful = effect.value().getCategory()
                == MobEffectCategory.HARMFUL;

        return protection(
                effectId,
                harmful,
                tagId -> effect.is(
                        TagKey.create(
                                Registries.MOB_EFFECT,
                                tagId
                        )
                )
        );
    }

    public double protection(
            ResourceLocation effectId,
            boolean harmful,
            Predicate<ResourceLocation> tagMatcher
    ) {
        if (effectId != null) {
            Double explicit = protections.get(effectId);
            if (explicit != null) {
                return clamp(explicit);
            }
        }

        Double matchedTagProtection = null;

        if (tagMatcher != null) {
            for (Map.Entry<ResourceLocation, Double> entry
                    : tagProtections.entrySet()) {
                if (!tagMatcher.test(entry.getKey())) {
                    continue;
                }

                matchedTagProtection = matchedTagProtection == null
                        ? entry.getValue()
                        : Math.max(
                                matchedTagProtection,
                                entry.getValue()
                        );
            }
        }

        if (matchedTagProtection != null) {
            return clamp(matchedTagProtection);
        }

        return harmful ? harmfulProtection : 0.0D;
    }

    public double tagProtection(ResourceLocation tagId) {
        if (tagId == null) {
            return 0.0D;
        }

        return clamp(tagProtections.getOrDefault(tagId, 0.0D));
    }

    private static Map<ResourceLocation, Double> copyAndValidate(
            Map<ResourceLocation, Double> values,
            String name
    ) {
        if (values == null) {
            throw new IllegalArgumentException(
                    name + " must not be null"
            );
        }

        for (Map.Entry<ResourceLocation, Double> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException(
                        name + " must not contain null entries"
                );
            }

            validate(entry.getValue(), "Status protection");
        }

        return Map.copyOf(values);
    }

    private static void validate(double protection, String name) {
        if (!Double.isFinite(protection)
                || protection < 0.0D
                || protection > 1.0D) {
            throw new IllegalArgumentException(
                    name + " must be between 0 and 1: " + protection
            );
        }
    }

    private static double clamp(double protection) {
        return Math.min(
                1.0D,
                Math.max(0.0D, protection)
        );
    }
}
