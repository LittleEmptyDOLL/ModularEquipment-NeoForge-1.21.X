package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.AttributeOperations;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class AttributeCharacteristics {
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
                            category(
                                    key.attributeId()
                            ),
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
        return switch (attributeId.toString()) {
            case "minecraft:generic.movement_speed" ->
                    CharacteristicCategory.MOBILITY;
            case "minecraft:generic.attack_damage",
                 "minecraft:generic.attack_speed" ->
                    CharacteristicCategory.COMBAT;
            case "minecraft:generic.armor",
                 "minecraft:generic.armor_toughness" ->
                    CharacteristicCategory.DEFENSE;
            case "minecraft:generic.max_health",
                 "minecraft:generic.knockback_resistance" ->
                    CharacteristicCategory.SURVIVAL;
            default ->
                    CharacteristicCategory.ATTRIBUTES;
        };
    }
}
