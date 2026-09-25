package com.github.littleemptydoll.exoequipment.characteristics;

import com.github.littleemptydoll.exoequipment.module.ElytraBoostProperties;
import com.github.littleemptydoll.exoequipment.module.JetpackProperties;

import java.util.List;

final class MobilityCharacteristics {
    private MobilityCharacteristics() {}

    static void add(
            List<Characteristic> result,
            CharacteristicsContext context
    ) {
        boolean flight = false;
        double jetpackThrust = 0.0D;
        double jetpackSpeed = 0.0D;
        double elytraAcceleration = 0.0D;
        double elytraMaxSpeed = 0.0D;
        double blinkDistance = 0.0D;
        int blinkEnergy = 0;
        int blinkCooldown = Integer.MAX_VALUE;

        for (var activeModule
                : CharacteristicsSupport
                .installedModules(context)) {

            var definition =
                    activeModule.definition();

            flight |= definition.flight()
                    .isPresent();

            JetpackProperties jetpack =
                    definition.jetpack()
                            .orElse(null);

            if (jetpack != null) {
                jetpackThrust =
                        Math.max(
                                jetpackThrust,
                                jetpack.verticalThrust()
                        );
                jetpackSpeed =
                        Math.max(
                                jetpackSpeed,
                                jetpack.horizontalSpeed()
                        );

                ElytraBoostProperties elytra =
                        jetpack.elytra()
                                .orElse(null);

                if (elytra != null) {
                    elytraAcceleration =
                            Math.max(
                                    elytraAcceleration,
                                    elytra.acceleration()
                            );
                    elytraMaxSpeed =
                            Math.max(
                                    elytraMaxSpeed,
                                    elytra.maxSpeed()
                            );
                }
            }

            definition.blink()
                    .ifPresent(blink -> {
                    });
        }

        for (var activeModule
                : CharacteristicsSupport
                .installedModules(context)) {

            var blink =
                    activeModule.definition()
                            .blink()
                            .orElse(null);

            if (blink == null) {
                continue;
            }

            blinkDistance =
                    Math.max(
                            blinkDistance,
                            blink.distance()
                    );
            blinkEnergy =
                    Math.max(
                            blinkEnergy,
                            blink.activationEnergy()
                    );
            blinkCooldown =
                    Math.min(
                            blinkCooldown,
                            blink.cooldown()
                    );
        }

        if (flight) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "flight",
                            CharacteristicType.STATIC,
                            1.0D
                    )
            );
        }

        if (jetpackThrust > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "jetpack.vertical_thrust",
                            CharacteristicType.STATIC,
                            jetpackThrust
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "jetpack.horizontal_speed",
                            CharacteristicType.STATIC,
                            jetpackSpeed
                    )
            );
        }

        if (elytraAcceleration > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "elytra.acceleration",
                            CharacteristicType.STATIC,
                            elytraAcceleration
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "elytra.max_speed",
                            CharacteristicType.STATIC,
                            elytraMaxSpeed
                    )
            );
        }

        if (blinkDistance > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "blink.distance",
                            CharacteristicType.STATIC,
                            blinkDistance
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "blink.activation_energy",
                            CharacteristicType.STATIC,
                            blinkEnergy
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.MOBILITY,
                            "blink.cooldown",
                            CharacteristicType.STATIC,
                            blinkCooldown
                    )
            );
        }
    }
}
