package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.module.BodyDamageProtectionOperations;
import com.github.littleemptydoll.exoequipment.module.BodyPart;
import com.github.littleemptydoll.exoequipment.module.DefenseOperations;
import com.github.littleemptydoll.exoequipment.module.ShieldOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DefenseCharacteristics {
    private DefenseCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        addDamageReduction(result, context);
        addBodyProtection(result, context);
        addShield(result, context);
    }

    private static void addDamageReduction(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        Set<ResourceLocation> damageTypes =
                collectDamageTypes(context);

        double universalMultiplier =
                context.isMatrixScope()
                        ? DefenseOperations
                        .calculateDamageMultiplier(
                                context.data(),
                                context.matrixSlot(),
                                null,
                                CharacteristicsSupport
                                        .poweredModules(
                                                context
                                        )
                        )
                        : DefenseOperations
                        .calculateDamageMultiplier(
                                context.data(),
                                null,
                                CharacteristicsSupport
                                        .poweredModules(
                                                context
                                        )
                        );

        double universalReduction =
                1.0D - universalMultiplier;

        if (universalReduction > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.DEFENSE,
                            "damage_reduction.default",
                            CharacteristicType.CURRENT,
                            universalReduction
                    )
            );
        }

        for (ResourceLocation damageType
                : damageTypes) {

            double multiplier =
                    context.isMatrixScope()
                            ? DefenseOperations
                            .calculateDamageMultiplier(
                                    context.data(),
                                    context.matrixSlot(),
                                    damageType,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            )
                            : DefenseOperations
                            .calculateDamageMultiplier(
                                    context.data(),
                                    damageType,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            );

            double reduction =
                    1.0D - multiplier;

            if (reduction <= 0.0D) {
                continue;
            }

            result.add(
                    new Characteristic(
                            CharacteristicCategory.DEFENSE,
                            "damage_reduction."
                                    + damageType,
                            CharacteristicType.CURRENT,
                            reduction
                    )
            );
        }
    }

    private static Set<ResourceLocation>
    collectDamageTypes(
            CharacteristicsContext context
    ) {
        Set<ResourceLocation> result =
                new HashSet<>();

        if (context.isMatrixScope()) {
            collectDamageTypes(
                    context,
                    context.matrixSlot(),
                    result
            );
            return result;
        }

        for (int slot
                : ExoskeletonState
                .activeMatrixSlots(
                        context.data()
                )) {
            collectDamageTypes(
                    context,
                    slot,
                    result
            );
        }

        return result;
    }

    private static void collectDamageTypes(
            CharacteristicsContext context,
            int slot,
            Set<ResourceLocation> result
    ) {
        MatrixData matrix =
                context.data()
                        .matrices()
                        .get(slot)
                        .matrix()
                        .orElse(null);

        if (matrix == null) {
            return;
        }

        matrix.modules().forEach(module ->
                ModModules.getDefinition(
                        module.id()
                ).damageReduction()
                        .ifPresent(properties ->
                                result.addAll(
                                        properties
                                                .reductions()
                                                .keySet()
                                )
                        )
        );
    }

    private static void addBodyProtection(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        for (BodyPart bodyPart
                : BodyPart.values()) {

            double chance =
                    context.isMatrixScope()
                            ? BodyDamageProtectionOperations
                            .calculateChance(
                                    context.data(),
                                    context.matrixSlot(),
                                    bodyPart,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            )
                            : BodyDamageProtectionOperations
                            .calculateChance(
                                    context.data(),
                                    bodyPart,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            );

            double multiplier =
                    context.isMatrixScope()
                            ? BodyDamageProtectionOperations
                            .calculateDamageMultiplier(
                                    context.data(),
                                    context.matrixSlot(),
                                    bodyPart,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            )
                            : BodyDamageProtectionOperations
                            .calculateDamageMultiplier(
                                    context.data(),
                                    bodyPart,
                                    CharacteristicsSupport
                                            .poweredModules(
                                                    context
                                            )
                            );

            double reduction =
                    1.0D - multiplier;

            String keyPrefix =
                    "body."
                            + bodyPart.name()
                            .toLowerCase();

            if (chance > 0.0D) {
                result.add(
                        new Characteristic(
                                CharacteristicCategory.DEFENSE,
                                keyPrefix + ".chance",
                                CharacteristicType.CURRENT,
                                chance
                        )
                );
            }

            if (reduction > 0.0D) {
                result.add(
                        new Characteristic(
                                CharacteristicCategory.DEFENSE,
                                keyPrefix + ".reduction",
                                CharacteristicType.CURRENT,
                                reduction
                        )
                );
            }
        }
    }

    private static void addShield(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        ShieldOperations.ShieldStatus status =
                context.isMatrixScope()
                        ? ShieldOperations.getStatus(
                                context.data(),
                                context.matrixSlot()
                        )
                        : ShieldOperations.getStatus(
                                context.data()
                        );

        if (!status.hasShields()) {
            return;
        }

        result.add(
                new Characteristic(
                        CharacteristicCategory.SHIELD,
                        "current_strength",
                        CharacteristicType.CURRENT,
                        status.currentEnergy()
                )
        );
        result.add(
                new Characteristic(
                        CharacteristicCategory.SHIELD,
                        "capacity",
                        CharacteristicType.CURRENT,
                        status.capacity()
                )
        );
    }
}
