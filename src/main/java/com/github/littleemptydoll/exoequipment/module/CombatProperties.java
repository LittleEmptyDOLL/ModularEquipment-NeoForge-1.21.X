package com.github.littleemptydoll.exoequipment.module;

import com.mojang.serialization.Codec;
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
                            Codec.DOUBLE.optionalFieldOf("attack_damage", 0.0D).forGetter(CombatProperties::attackDamage),
                            Codec.DOUBLE.optionalFieldOf("attack_speed", 0.0D).forGetter(CombatProperties::attackSpeed),
                            Codec.DOUBLE.optionalFieldOf("attack_knockback", 0.0D).forGetter(CombatProperties::attackKnockback),
                            Codec.DOUBLE.optionalFieldOf("entity_interaction_range", 0.0D).forGetter(CombatProperties::entityInteractionRange),
                            Codec.DOUBLE.optionalFieldOf("crit_chance", 0.0D).forGetter(CombatProperties::critChance),
                            Codec.DOUBLE.optionalFieldOf("crit_damage", 0.0D).forGetter(CombatProperties::critDamage),
                            Codec.DOUBLE.optionalFieldOf("armor_pierce", 0.0D).forGetter(CombatProperties::armorPierce),
                            Codec.DOUBLE.optionalFieldOf("armor_shred", 0.0D).forGetter(CombatProperties::armorShred),
                            Codec.DOUBLE.optionalFieldOf("prot_pierce", 0.0D).forGetter(CombatProperties::protPierce),
                            Codec.DOUBLE.optionalFieldOf("prot_shred", 0.0D).forGetter(CombatProperties::protShred),
                            Codec.DOUBLE.optionalFieldOf("current_hp_damage", 0.0D).forGetter(CombatProperties::currentHpDamage),
                            Codec.DOUBLE.optionalFieldOf("life_steal", 0.0D).forGetter(CombatProperties::lifeSteal),
                            Codec.DOUBLE.optionalFieldOf("overheal", 0.0D).forGetter(CombatProperties::overheal),
                            Codec.DOUBLE.optionalFieldOf("fire_damage", 0.0D).forGetter(CombatProperties::fireDamage),
                            Codec.DOUBLE.optionalFieldOf("cold_damage", 0.0D).forGetter(CombatProperties::coldDamage),
                            Codec.DOUBLE.optionalFieldOf("projectile_damage", 0.0D).forGetter(CombatProperties::projectileDamage),
                            Codec.DOUBLE.optionalFieldOf("arrow_damage", 0.0D).forGetter(CombatProperties::arrowDamage),
                            Codec.DOUBLE.optionalFieldOf("arrow_velocity", 0.0D).forGetter(CombatProperties::arrowVelocity),
                            Codec.DOUBLE.optionalFieldOf("draw_speed", 0.0D).forGetter(CombatProperties::drawSpeed)
                    ).apply(instance, CombatProperties::new)
            );

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
}
