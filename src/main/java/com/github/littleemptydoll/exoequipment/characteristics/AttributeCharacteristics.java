package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

final class AttributeCharacteristics {
    private static final Set<String> MOBILITY = Set.of(
            "minecraft:generic.movement_speed",
            "minecraft:generic.flying_speed",
            "minecraft:generic.jump_strength",
            "minecraft:generic.safe_fall_distance",
            "minecraft:generic.step_height",
            "minecraft:generic.sneaking_speed",
            "minecraft:generic.water_movement_efficiency",
            "minecraft:generic.movement_efficiency",
            "minecraft:generic.gravity",
            "minecraft:generic.fall_damage_multiplier"
    );

    private static final Set<String> COMBAT = Set.of(
            "minecraft:generic.attack_damage",
            "minecraft:generic.attack_knockback",
            "minecraft:generic.attack_speed",
            "minecraft:generic.entity_interaction_range",
            "apothic_attributes:crit_chance",
            "apothic_attributes:crit_damage",
            "apothic_attributes:armor_pierce",
            "apothic_attributes:armor_shred",
            "apothic_attributes:prot_pierce",
            "apothic_attributes:prot_shred",
            "apothic_attributes:current_hp_damage",
            "apothic_attributes:life_steal",
            "apothic_attributes:overheal",
            "apothic_attributes:fire_damage",
            "apothic_attributes:cold_damage",
            "apothic_attributes:projectile_damage",
            "apothic_attributes:arrow_damage",
            "apothic_attributes:arrow_velocity",
            "apothic_attributes:draw_speed"
    );

    private static final Set<String> DEFENSE = Set.of(
            "minecraft:generic.armor",
            "minecraft:generic.armor_toughness",
            "minecraft:generic.knockback_resistance",
            "minecraft:generic.explosion_knockback_resistance",
            "apothic_attributes:dodge_chance"
    );

    private static final Set<String> SURVIVAL = Set.of(
            "minecraft:generic.max_health",
            "minecraft:generic.max_absorption",
            "minecraft:generic.oxygen_bonus",
            "minecraft:generic.burning_time",
            "apothic_attributes:healing_received"
    );

    private static final Set<String> UTILITY = Set.of(
            "minecraft:generic.block_break_speed",
            "minecraft:generic.block_interaction_range",
            "minecraft:generic.luck",
            "minecraft:generic.submerged_mining_speed",
            "minecraft:generic.temptation_range",
            "apothic_attributes:experience_gained"
    );

    private AttributeCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.player() == null) {
            return;
        }

        AttributeOperations.calculate(
                context.player(),
                context.data(),
                context.isMatrixScope()
                        ? context.matrixSlot()
                        : null,
                CharacteristicsSupport
                        .poweredModules(context)
        ).forEach((key, value) -> {
            String operation =
                    switch (key.operation()) {
                        case ADD_VALUE ->
                                "add_value";
                        case ADD_MULTIPLIED_BASE ->
                                "add_multiplied_base";
                        case ADD_MULTIPLIED_TOTAL ->
                                "add_multiplied_total";
                    };

            result.add(
                    new Characteristic(
                            category(key.attributeId()),
                            "attribute."
                                    + key.attributeId()
                                    + "."
                                    + operation,
                            CharacteristicType.CURRENT,
                            value
                    )
            );
        });
    }

    private static CharacteristicCategory category(
            ResourceLocation attributeId
    ) {
        String id = attributeId.toString();

        if (MOBILITY.contains(id)) {
            return CharacteristicCategory.MOBILITY;
        }
        if (COMBAT.contains(id)) {
            return CharacteristicCategory.COMBAT;
        }
        if (DEFENSE.contains(id)) {
            return CharacteristicCategory.DEFENSE;
        }
        if (SURVIVAL.contains(id)) {
            return CharacteristicCategory.SURVIVAL;
        }
        if (UTILITY.contains(id)) {
            return CharacteristicCategory.UTILITY;
        }

        return CharacteristicCategory.ATTRIBUTES;
    }
}
