package com.github.littleemptydoll.exoequipment.util;

import com.github.littleemptydoll.exoequipment.module.AttributeModifierProperties;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/** Displays attribute modifiers consistently with the characteristics panel. */
public final class AttributeValueFormatter {
    private static final Set<String> PERCENT_ADD_VALUE_ATTRIBUTES = Set.of(
            "minecraft:generic.attack_knockback",
            "minecraft:generic.knockback_resistance",
            "minecraft:generic.explosion_knockback_resistance",
            "minecraft:generic.water_movement_efficiency",
            "minecraft:generic.movement_efficiency",
            "minecraft:generic.fall_damage_multiplier",
            "apothic_attributes:crit_chance",
            "apothic_attributes:crit_damage",
            "apothic_attributes:armor_shred",
            "apothic_attributes:prot_shred",
            "apothic_attributes:protection_shred",
            "apothic_attributes:current_hp_damage",
            "apothic_attributes:life_steal",
            "apothic_attributes:overheal",
            "apothic_attributes:projectile_damage",
            "apothic_attributes:arrow_damage",
            "apothic_attributes:arrow_velocity",
            "apothic_attributes:draw_speed",
            "apothic_attributes:experience_gained",
            "apothic_attributes:healing_received",
            "apothic_attributes:dodge_chance"
    );

    private AttributeValueFormatter() {}

    public static boolean isPercentAddValue(String attributeId) {
        return PERCENT_ADD_VALUE_ATTRIBUTES.contains(attributeId);
    }

    public static String format(ResourceLocation id, AttributeModifierProperties modifier, double efficiency) {
        double amount = modifier.amount() * efficiency;
        return switch (modifier.operation()) {
            case ADD_VALUE -> isPercentAddValue(id.toString())
                    ? NumberFormatter.signed(amount * 100) + "%"
                    : NumberFormatter.signed(amount);
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> NumberFormatter.signed(amount * 100) + "%";
        };
    }
}
