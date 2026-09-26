package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonModules;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.TemperatureImpactProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureModifierProperties;

import java.util.Set;

public final class LsoTemperatureOperations {
    private LsoTemperatureOperations() {
    }

    public static TemperatureModifierProperties calculateModifiers(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double temperature = 0.0D;
        double heatResistance = 0.0D;
        double coldResistance = 0.0D;
        double thermalResistance = 0.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            TemperatureModifierProperties properties =
                    activeModule.definition()
                            .temperatureModifier()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            temperature += properties.temperature();
            heatResistance += properties.heatResistance();
            coldResistance += properties.coldResistance();
            thermalResistance += properties.thermalResistance();
        }

        return new TemperatureModifierProperties(
                temperature,
                heatResistance,
                coldResistance,
                thermalResistance
        );
    }

    public static double calculateTemperatureImpactResistance(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double remainingImpact = 1.0D;

        for (ExoskeletonModules.ActiveModule activeModule
                : ExoskeletonModules.activeSupported(data)) {

            if (!ExoskeletonModules.isPowered(
                    activeModule,
                    poweredModules
            )) {
                continue;
            }

            TemperatureImpactProperties properties =
                    activeModule.definition()
                            .temperatureImpact()
                            .orElse(null);

            if (properties == null) {
                continue;
            }

            remainingImpact *= 1.0D - properties.resistance();

            if (remainingImpact <= 0.0D) {
                return 1.0D;
            }
        }

        return Math.max(
                0.0D,
                Math.min(1.0D, 1.0D - remainingImpact)
        );
    }
}
