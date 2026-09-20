package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CombatProperties(
        double attackDamage,
        double attackSpeed,
        double entityInteractionRange
) {
    public static final Codec<CombatProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.DOUBLE
                                    .optionalFieldOf("attack_damage", 0.0D)
                                    .forGetter(CombatProperties::attackDamage),
                            Codec.DOUBLE
                                    .optionalFieldOf("attack_speed", 0.0D)
                                    .forGetter(CombatProperties::attackSpeed),
                            Codec.DOUBLE
                                    .optionalFieldOf("entity_interaction_range", 0.0D)
                                    .forGetter(CombatProperties::entityInteractionRange)
                    ).apply(instance, CombatProperties::new)
            );

    public CombatProperties {
        validateNonNegative(attackDamage, "attack damage");
        validateNonNegative(attackSpeed, "attack speed");
        validateNonNegative(entityInteractionRange, "entity interaction range");
    }

    private static void validateNonNegative(
            double value,
            String name
    ) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(
                    "Combat " + name + " must be finite and non-negative"
            );
        }
    }
}
