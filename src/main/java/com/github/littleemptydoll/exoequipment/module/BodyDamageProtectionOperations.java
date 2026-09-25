package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BodyDamageProtectionOperations {
    private BodyDamageProtectionOperations() {}

    public static double calculateChance(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remainingChance = 1.0D;

        for (ModuleProtection protection
                : protections(
                        data,
                        ExoskeletonModules.activeSupported(data),
                        bodyPart,
                        poweredModules
                )) {

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            protection.definition(),
                            data.temperature()
                    );

            remainingChance *= 1.0D
                    - protection.properties().chance() * efficiency;

            if (remainingChance <= 0.0D) {
                return 1.0D;
            }
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, 1.0D - remainingChance)
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ModuleProtection protection
                : protections(
                        data,
                        ExoskeletonModules.activeSupported(data),
                        bodyPart,
                        poweredModules
                )) {

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            protection.definition(),
                            data.temperature()
                    );

            multiplier *= 1.0D
                    - protection.properties().damageReduction()
                    * efficiency;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }

    public static double calculateChance(
            ExoskeletonData data,
            int matrixSlot,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remainingChance = 1.0D;

        for (ModuleProtection protection
                : protections(
                        data,
                        ExoskeletonModules.supportedInMatrix(
                                data,
                                matrixSlot
                        ),
                        bodyPart,
                        poweredModules
                )) {

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            protection.definition(),
                            data.temperature()
                    );

            remainingChance *= 1.0D
                    - protection.properties().chance() * efficiency;

            if (remainingChance <= 0.0D) {
                return 1.0D;
            }
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, 1.0D - remainingChance)
        );
    }

    public static double calculateDamageMultiplier(
            ExoskeletonData data,
            int matrixSlot,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ModuleProtection protection
                : protections(
                        data,
                        ExoskeletonModules.supportedInMatrix(
                                data,
                                matrixSlot
                        ),
                        bodyPart,
                        poweredModules
                )) {

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            protection.definition(),
                            data.temperature()
                    );

            multiplier *= 1.0D
                    - protection.properties().damageReduction()
                    * efficiency;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return Math.max(0.0D, multiplier);
    }

    private static List<ModuleProtection> protections(
            ExoskeletonData data,
            List<ExoskeletonModules.ActiveModule> modules,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        List<ModuleProtection> result = new ArrayList<>();

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

            result.add(new ModuleProtection(
                    activeModule.definition(),
                    properties
            ));
        }

        return List.copyOf(result);
    }

    private record ModuleProtection(
            ModuleDefinition definition,
            BodyDamageProtectionProperties properties
    ) {}
}
