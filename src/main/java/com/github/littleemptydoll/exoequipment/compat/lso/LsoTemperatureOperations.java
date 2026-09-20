package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.TemperatureImpactProperties;
import com.github.littleemptydoll.exoequipment.module.TemperatureModifierProperties;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

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

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0;
                 moduleIndex < matrix.modules().size();
                 moduleIndex++) {

                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                var definition = ModModules.getDefinition(module.id());
                var energy = definition.energy();
                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (energy.isPresent()
                        && energy.get().consumption() > 0
                        && !poweredModules.contains(reference)) {
                    continue;
                }

                TemperatureModifierProperties properties =
                        definition.temperatureModifier().orElse(null);

                if (properties == null) {
                    continue;
                }

                temperature += properties.temperature();
                heatResistance += properties.heatResistance();
                coldResistance += properties.coldResistance();
                thermalResistance += properties.thermalResistance();
            }
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
        double resistance = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices()
                    .get(slot)
                    .matrix()
                    .orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0;
                 moduleIndex < matrix.modules().size();
                 moduleIndex++) {

                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                var definition = ModModules.getDefinition(module.id());
                var energy = definition.energy();
                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (energy.isPresent()
                        && energy.get().consumption() > 0
                        && !poweredModules.contains(reference)) {
                    continue;
                }

                TemperatureImpactProperties properties =
                        definition.temperatureImpact().orElse(null);

                if (properties == null) {
                    continue;
                }

                resistance += properties.resistance();
            }
        }

        return Math.min(1.0D, resistance);
    }
}

