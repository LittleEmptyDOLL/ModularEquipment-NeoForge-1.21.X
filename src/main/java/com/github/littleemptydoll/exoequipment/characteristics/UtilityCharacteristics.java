package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.CloakingProperties;
import com.github.littleemptydoll.exoequipment.module.EmergencyShieldProperties;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetOperations;
import com.github.littleemptydoll.exoequipment.module.PickupMagnetProperties;

import java.util.List;
import java.util.Optional;

final class UtilityCharacteristics {
    private UtilityCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        addPickupMagnet(result, context);
        addModuleUtilities(result, context);
    }

    private static void addPickupMagnet(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        Optional<PickupMagnetProperties> magnet =
                context.isMatrixScope()
                        ? PickupMagnetOperations
                        .findProperties(
                                context.data(),
                                context.matrixSlot(),
                                CharacteristicsSupport
                                        .poweredModules(context)
                        )
                        : PickupMagnetOperations
                        .findProperties(
                                context.data(),
                                CharacteristicsSupport
                                        .poweredModules(context)
                        );

        magnet.ifPresent(properties -> {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "pickup_magnet.radius",
                            CharacteristicType.CURRENT,
                            properties.radius()
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "pickup_magnet.items",
                            CharacteristicType.STATE,
                            properties.mode()
                                    .acceptsItems()
                                    ? 1.0D
                                    : 0.0D
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "pickup_magnet.experience",
                            CharacteristicType.STATE,
                            properties.mode()
                                    .acceptsExperience()
                                    ? 1.0D
                                    : 0.0D
                    )
            );
        });
    }

    private static void addModuleUtilities(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        double cloakConsumption = 0.0D;
        boolean cloaking = false;
        double emergencyRestore = 0.0D;
        int emergencyCooldown =
                Integer.MAX_VALUE;

        for (var activeModule
                : CharacteristicsSupport
                .installedModules(context)) {

            var definition =
                    activeModule.definition();

            CloakingProperties cloak =
                    definition.cloaking()
                            .orElse(null);

            if (cloak != null) {
                cloakConsumption =
                        Math.max(
                                cloakConsumption,
                                cloak.activeConsumption()
                        );
                cloaking |=
                        activeModule.module()
                                .active();
            }

            EmergencyShieldProperties emergency =
                    definition.emergencyShield()
                            .orElse(null);

            if (emergency != null) {
                emergencyRestore =
                        Math.max(
                                emergencyRestore,
                                emergency.restore()
                        );
                emergencyCooldown =
                        Math.min(
                                emergencyCooldown,
                                emergency.cooldown()
                        );
            }
        }

        if (cloakConsumption > 0.0D
                || cloaking) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "cloaking.active_consumption",
                            CharacteristicType.STATIC,
                            cloakConsumption
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "cloaking.active",
                            CharacteristicType.STATE,
                            cloaking ? 1.0D : 0.0D
                    )
            );
        }

        if (emergencyRestore > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "emergency_shield.restore",
                            CharacteristicType.STATIC,
                            emergencyRestore
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.UTILITY,
                            "emergency_shield.cooldown",
                            CharacteristicType.STATIC,
                            emergencyCooldown
                    )
            );
        }
    }
}
