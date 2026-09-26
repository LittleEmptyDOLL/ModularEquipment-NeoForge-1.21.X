package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class BodyDamageProtectionOperations {
    private BodyDamageProtectionOperations() {}

    public static double calculateChance(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        return 1.0D - calculateRemainingFactor(
                data,
                ExoskeletonModules.activeSupported(data),
                bodyPart,
                poweredModules,
                BodyDamageProtectionProperties::chance
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateRemainingFactor(
                data,
                ExoskeletonModules.activeSupported(data),
                bodyPart,
                poweredModules,
                BodyDamageProtectionProperties::damageReduction
        );
    }

    public static double calculateChance(
            ExoskeletonData data,
            int matrixSlot,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        return 1.0D - calculateRemainingFactor(
                data,
                ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                ),
                bodyPart,
                poweredModules,
                BodyDamageProtectionProperties::chance
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            int matrixSlot,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        return calculateRemainingFactor(
                data,
                ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                ),
                bodyPart,
                poweredModules,
                BodyDamageProtectionProperties::damageReduction
        );
    }

    private static double calculateRemainingFactor(
            ExoskeletonData data,
            List<ExoskeletonModules.ActiveModule> modules,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules,
            ToDoubleFunction<BodyDamageProtectionProperties> valueProvider
    ) {
        double remainingFactor = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule : modules) {
            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            BodyDamageProtectionProperties properties =
                    activeModule.definition()
                            .bodyDamageProtection()
                            .orElse(null);

            if (properties == null
                    || !BodyPart.applies(
                            properties.bodyParts(),
                            bodyPart
                    )) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            double effectiveValue = Math.min(
                    1.0D,
                    Math.max(
                            0.0D,
                            valueProvider.applyAsDouble(properties)
                                    * efficiency
                    )
            );

            remainingFactor *= 1.0D - effectiveValue;

            if (remainingFactor <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, remainingFactor)
        );
    }
}
