package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import sfiomn.legendarysurvivaloverhaul.api.bodydamage.BodyPartEnum;

import java.util.Locale;
import java.util.Set;

public record BodyDamageProtectionProperties(
        double chance,
        double damageReduction,
        Set<BodyPartEnum> bodyParts
) {
    private static final Codec<BodyPartEnum> BODY_PART_CODEC =
            Codec.STRING.comapFlatMap(
                    value -> {
                        try {
                            return DataResult.success(
                                    BodyPartEnum.valueOf(value.toUpperCase(Locale.ROOT))
                            );
                        } catch (IllegalArgumentException exception) {
                            return DataResult.error(
                                    () -> "Unknown LSO body part: " + value
                            );
                        }
                    },
                    part -> part.name().toLowerCase(Locale.ROOT)
            );

    private static final Codec<Set<BodyPartEnum>> BODY_PARTS_CODEC =
            BODY_PART_CODEC.listOf().xmap(Set::copyOf, java.util.ArrayList::new);

    public static final Codec<BodyDamageProtectionProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .fieldOf("chance")
                                    .forGetter(BodyDamageProtectionProperties::chance),
                            Codec.DOUBLE
                                    .fieldOf("damage_reduction")
                                    .forGetter(BodyDamageProtectionProperties::damageReduction),
                            BODY_PARTS_CODEC
                                    .fieldOf("body_parts")
                                    .forGetter(BodyDamageProtectionProperties::bodyParts)
                    ).apply(instance, BodyDamageProtectionProperties::new)
            );

    public BodyDamageProtectionProperties {
        bodyParts = Set.copyOf(bodyParts);

        if (!Double.isFinite(chance)
                || chance < 0.0D
                || chance > 1.0D) {
            throw new IllegalArgumentException(
                    "Body damage protection chance must be between 0 and 1"
            );
        }

        if (!Double.isFinite(damageReduction)
                || damageReduction < 0.0D
                || damageReduction > 1.0D) {
            throw new IllegalArgumentException(
                    "Body damage reduction must be between 0 and 1"
            );
        }

        if (bodyParts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Body damage protection must target at least one body part"
            );
        }
    }
}
