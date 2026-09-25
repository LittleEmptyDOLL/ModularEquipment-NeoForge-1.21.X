package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.HungerOperations;
import com.github.littleemptydoll.exoequipment.module.RegenerationOperations;
import com.github.littleemptydoll.exoequipment.module.RevivalProperties;

import java.util.List;

final class SurvivalCharacteristics {
    private SurvivalCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        addRegeneration(result, context);
        addHunger(result, context);
        addRevivalAndThirst(result, context);
    }

    private static void addRegeneration(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double healthPerSecond =
                context.isMatrixScope()
                        ? RegenerationOperations
                        .calculateHealthPerSecond(
                                context.data(),
                                context.matrixSlot(),
                                CharacteristicsSupport
                                        .poweredModules(
                                                context
                                        )
                        )
                        : RegenerationOperations
                        .calculateHealthPerSecond(
                                context.data(),
                                CharacteristicsSupport
                                        .poweredModules(
                                                context
                                        )
                        );

        if (healthPerSecond > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.REGENERATION,
                            "health_per_second",
                            CharacteristicType.CURRENT,
                            healthPerSecond
                    )
            );
        }
    }

    private static void addHunger(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        if (context.isMatrixScope()) {
            return;
        }

        double hunger =
                HungerOperations
                        .calculateExhaustionReduction(
                                context.data(),
                                CharacteristicsSupport
                                        .poweredModules(
                                                context
                                        )
                        );

        if (hunger > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SURVIVAL,
                            "hunger.exhaustion_reduction",
                            CharacteristicType.CURRENT,
                            hunger
                    )
            );
        }
    }

    private static void addRevivalAndThirst(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double revivalRestore = 0.0D;
        int revivalCooldown =
                Integer.MAX_VALUE;
        double thirst = 0.0D;

        for (var activeModule
                : CharacteristicsSupport
                .installedModules(context)) {

            var definition =
                    activeModule.definition();

            RevivalProperties revival =
                    definition.revival()
                            .orElse(null);

            if (revival != null) {
                revivalRestore =
                        Math.max(
                                revivalRestore,
                                revival.restoreHealth()
                        );
                revivalCooldown =
                        Math.min(
                                revivalCooldown,
                                revival.cooldown()
                        );
            }

            if (definition.thirst().isPresent()) {
                thirst =
                        Math.max(
                                thirst,
                                definition.thirst()
                                        .get()
                                        .exhaustionReduction()
                        );
            }
        }

        if (revivalRestore > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SURVIVAL,
                            "revival.restore_health",
                            CharacteristicType.STATIC,
                            revivalRestore
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SURVIVAL,
                            "revival.cooldown",
                            CharacteristicType.STATIC,
                            revivalCooldown
                    )
            );
        }

        if (thirst > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.SURVIVAL,
                            "thirst.exhaustion_reduction",
                            CharacteristicType.STATIC,
                            thirst
                    )
            );
        }
    }
}
