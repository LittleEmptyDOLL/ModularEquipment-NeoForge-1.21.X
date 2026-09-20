package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CombatProperties(
        double attackDamage,
        double attackSpeed,
        double attackKnockback,
        double entityInteractionRange,
        double critChance,
        double critDamage,
        double armorPierce,
        double armorShred,
        double protPierce,
        double protShred,
        double currentHpDamage,
        double lifeSteal,
        double overheal,
        double fireDamage,
        double coldDamage,
        double projectileDamage,
        double arrowDamage,
        double arrowVelocity,
        double drawSpeed
) {
    public static final Codec<CombatProperties> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            RecordCodecBuilder.of(
                                    CombatProperties::vanillaProperties,
                                    VanillaCombatProperties.CODEC
                            ),
                            RecordCodecBuilder.of(
                                    CombatProperties::apothicProperties,
                                    ApothicCombatProperties.CODEC
                            )
                    ).apply(instance, CombatProperties::fromCodec)
            );

    private VanillaCombatProperties vanillaProperties() {
        return new VanillaCombatProperties(
                attackDamage,
                attackSpeed,
                attackKnockback,
                entityInteractionRange
        );
    }

    private ApothicCombatProperties apothicProperties() {
        return new ApothicCombatProperties(
                critChance,
                critDamage,
                armorPierce,
                armorShred,
                protPierce,
                protShred,
                currentHpDamage,
                lifeSteal,
                overheal,
                fireDamage,
                coldDamage,
                projectileDamage,
                arrowDamage,
                arrowVelocity,
                drawSpeed
        );
    }

    private static CombatProperties fromCodec(
            VanillaCombatProperties vanilla,
            ApothicCombatProperties apothic
    ) {
        return new CombatProperties(
                vanilla.attackDamage(),
                vanilla.attackSpeed(),
                vanilla.attackKnockback(),
                vanilla.entityInteractionRange(),
                apothic.critChance(),
                apothic.critDamage(),
                apothic.armorPierce(),
                apothic.armorShred(),
                apothic.protPierce(),
                apothic.protShred(),
                apothic.currentHpDamage(),
                apothic.lifeSteal(),
                apothic.overheal(),
                apothic.fireDamage(),
                apothic.coldDamage(),
                apothic.projectileDamage(),
                apothic.arrowDamage(),
                apothic.arrowVelocity(),
                apothic.drawSpeed()
        );
    }

    public CombatProperties {
        validateNonNegative(attackDamage, "attack damage");
        validateNonNegative(attackSpeed, "attack speed");
        validateNonNegative(attackKnockback, "attack knockback");
        validateNonNegative(entityInteractionRange, "entity interaction range");
        validateNonNegative(critChance, "crit chance");
        validateNonNegative(critDamage, "crit damage");
        validateNonNegative(armorPierce, "armor pierce");
        validateNonNegative(armorShred, "armor shred");
        validateNonNegative(protPierce, "protection pierce");
        validateNonNegative(protShred, "protection shred");
        validateNonNegative(currentHpDamage, "current hp damage");
        validateNonNegative(lifeSteal, "life steal");
        validateNonNegative(overheal, "overheal");
        validateNonNegative(fireDamage, "fire damage");
        validateNonNegative(coldDamage, "cold damage");
        validateNonNegative(projectileDamage, "projectile damage");
        validateNonNegative(arrowDamage, "arrow damage");
        validateNonNegative(arrowVelocity, "arrow velocity");
        validateNonNegative(drawSpeed, "draw speed");
    }

    private static void validateNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(
                    "Combat " + name + " must be finite and non-negative"
            );
        }
    }

    private record VanillaCombatProperties(
            double attackDamage,
            double attackSpeed,
            double attackKnockback,
            double entityInteractionRange
    ) {
        private static final MapCodec<VanillaCombatProperties> CODEC =
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                Codec.DOUBLE.optionalFieldOf("attack_damage", 0.0D)
                                        .forGetter(VanillaCombatProperties::attackDamage),
                                Codec.DOUBLE.optionalFieldOf("attack_speed", 0.0D)
                                        .forGetter(VanillaCombatProperties::attackSpeed),
                                Codec.DOUBLE.optionalFieldOf("attack_knockback", 0.0D)
                                        .forGetter(VanillaCombatProperties::attackKnockback),
                                Codec.DOUBLE.optionalFieldOf("entity_interaction_range", 0.0D)
                                        .forGetter(VanillaCombatProperties::entityInteractionRange)
                        ).apply(instance, VanillaCombatProperties::new)
                );
    }

    private record ApothicCombatProperties(
            double critChance,
            double critDamage,
            double armorPierce,
            double armorShred,
            double protPierce,
            double protShred,
            double currentHpDamage,
            double lifeSteal,
            double overheal,
            double fireDamage,
            double coldDamage,
            double projectileDamage,
            double arrowDamage,
            double arrowVelocity,
            double drawSpeed
    ) {
        private static final MapCodec<ApothicCombatProperties> CODEC =
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                Codec.DOUBLE.optionalFieldOf("crit_chance", 0.0D)
                                        .forGetter(ApothicCombatProperties::critChance),
                                Codec.DOUBLE.optionalFieldOf("crit_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::critDamage),
                                Codec.DOUBLE.optionalFieldOf("armor_pierce", 0.0D)
                                        .forGetter(ApothicCombatProperties::armorPierce),
                                Codec.DOUBLE.optionalFieldOf("armor_shred", 0.0D)
                                        .forGetter(ApothicCombatProperties::armorShred),
                                Codec.DOUBLE.optionalFieldOf("prot_pierce", 0.0D)
                                        .forGetter(ApothicCombatProperties::protPierce),
                                Codec.DOUBLE.optionalFieldOf("prot_shred", 0.0D)
                                        .forGetter(ApothicCombatProperties::protShred),
                                Codec.DOUBLE.optionalFieldOf("current_hp_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::currentHpDamage),
                                Codec.DOUBLE.optionalFieldOf("life_steal", 0.0D)
                                        .forGetter(ApothicCombatProperties::lifeSteal),
                                Codec.DOUBLE.optionalFieldOf("overheal", 0.0D)
                                        .forGetter(ApothicCombatProperties::overheal),
                                Codec.DOUBLE.optionalFieldOf("fire_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::fireDamage),
                                Codec.DOUBLE.optionalFieldOf("cold_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::coldDamage),
                                Codec.DOUBLE.optionalFieldOf("projectile_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::projectileDamage),
                                Codec.DOUBLE.optionalFieldOf("arrow_damage", 0.0D)
                                        .forGetter(ApothicCombatProperties::arrowDamage),
                                Codec.DOUBLE.optionalFieldOf("arrow_velocity", 0.0D)
                                        .forGetter(ApothicCombatProperties::arrowVelocity),
                                Codec.DOUBLE.optionalFieldOf("draw_speed", 0.0D)
                                        .forGetter(ApothicCombatProperties::drawSpeed)
                        ).apply(instance, ApothicCombatProperties::new)
                );
    }
}
