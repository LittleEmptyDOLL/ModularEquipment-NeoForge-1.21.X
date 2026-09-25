package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations;

import java.util.Set;

public final class RegenerationOperations {
    private RegenerationOperations() {}

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            int matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        double healthPerSecond = 0.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.supportedInMatrix(
                        data,
                        matrixSlot
                )) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            healthPerSecond += activeModule.definition()
                    .regeneration()
                    .map(RegenerationProperties::healthPerSecond)
                    .map(value ->
                            value * TemperatureOperations.calculateModuleEfficiency(
                                    activeModule.definition(),
                                    data.temperature()
                            )
                    )
                    .orElse(0.0D);
        }

        return healthPerSecond;
    }

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double healthPerSecond = 0.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            healthPerSecond += activeModule.definition()
                    .regeneration()
                    .map(RegenerationProperties::healthPerSecond)
                    .map(value ->
                            value * TemperatureOperations.calculateModuleEfficiency(
                                    activeModule.definition(),
                                    data.temperature()
                            )
                    )
                    .orElse(0.0D);
        }

        return healthPerSecond;
    }
}
