package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.Set;

public final class HungerOperations {
    private HungerOperations() {}

    public static double calculateExhaustionReduction(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            HungerProperties properties =
                    activeModule.definition()
                            .hunger()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            double efficiency =
                    TemperatureOperations.calculateModuleEfficiency(
                            activeModule.definition(),
                            data.temperature()
                    );

            double reduction =
                    properties.exhaustionReduction() * efficiency;

            multiplier *= 1.0D - reduction;

            if (multiplier <= 0.0D) {
                return 0.0D;
            }
        }

        return 1.0D - Math.max(0.0D, multiplier);
    }

    public static float applyExhaustionReduction(
            float previousExhaustion,
            float currentExhaustion,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (currentExhaustion <= previousExhaustion) {
            return currentExhaustion;
        }

        double reduction = calculateExhaustionReduction(
                data,
                poweredModules
        );

        if (reduction <= 0.0D) {
            return currentExhaustion;
        }

        double increase = currentExhaustion - previousExhaustion;
        double reduced = previousExhaustion
                + increase * (1.0D - reduction);

        return (float) Math.max(
                0.0D,
                Math.min(4.0D, reduced)
        );
    }
}
