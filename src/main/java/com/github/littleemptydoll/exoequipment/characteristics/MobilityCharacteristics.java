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

            var blink =
                    definition.blink()
                            .orElse(null);

            if (blink != null) {
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
        }

        if (jetpackThrust > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.JETPACK,
                            "jetpack.vertical_thrust",
                            CharacteristicType.STATIC,
                            jetpackThrust
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.JETPACK,
                            "jetpack.horizontal_speed",
                            CharacteristicType.STATIC,
                            jetpackSpeed
                    )
            );
        }

        if (elytraAcceleration > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.ELYTRA_BOOST,
                            "elytra.acceleration",
                            CharacteristicType.STATIC,
                            elytraAcceleration
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.ELYTRA_BOOST,
                            "elytra.max_speed",
                            CharacteristicType.STATIC,
                            elytraMaxSpeed
                    )
            );
        }

        if (blinkDistance > 0.0D) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.BLINK,
                            "blink.distance",
                            CharacteristicType.STATIC,
                            blinkDistance
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.BLINK,
                            "blink.activation_energy",
                            CharacteristicType.STATIC,
                            blinkEnergy
                    )
            );
            result.add(
                    new Characteristic(
                            CharacteristicCategory.BLINK,
                            "blink.cooldown",
                            CharacteristicType.STATIC,
                            blinkCooldown
                    )
            );
        }

        if (flight) {
            result.add(
                    new Characteristic(
                            CharacteristicCategory.FLIGHT,
                            "flight",
                            CharacteristicType.STATE,
                            1.0D
                    )
            );
        }
    }
}
