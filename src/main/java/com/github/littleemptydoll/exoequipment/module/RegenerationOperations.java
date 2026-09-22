package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class RegenerationOperations {
    private RegenerationOperations() {}

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            int matrixSlot,
            Set<InstalledModuleReference> poweredModules
    ) {
        var matrix = data.matrices().get(matrixSlot).matrix().orElse(null);
        if (matrix == null) return 0.0D;

        double healthPerSecond = 0.0D;
        for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
            InstalledModule module = matrix.modules().get(moduleIndex);
            if (!FrameOperations.isModuleSupported(data, module)) continue;

            InstalledModuleReference reference = new InstalledModuleReference(matrixSlot, moduleIndex);
            var definition = ModModules.getDefinition(module.id());
            if (definition.energy().filter(energy -> energy.consumption() > 0).isPresent()
                    && !poweredModules.contains(reference)) continue;

            healthPerSecond += definition.regeneration()
                    .map(RegenerationProperties::healthPerSecond)
                    .map(value -> value * com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations.calculateModuleEfficiency(definition, data.temperature()))
                    .orElse(0.0D);
        }
        return healthPerSecond;
    }

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double healthPerSecond = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            var matrix = data.matrices().get(slot).matrix().orElse(null);

            if (matrix == null) {
                continue;
            }

            for (int moduleIndex = 0; moduleIndex < matrix.modules().size(); moduleIndex++) {
                InstalledModule module = matrix.modules().get(moduleIndex);

                if (!FrameOperations.isModuleSupported(data, module)) {
                    continue;
                }

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                var definition = ModModules.getDefinition(module.id());
                var energy = definition.energy();

                if (energy.isPresent()
                        && energy.get().consumption() > 0
                        && !poweredModules.contains(reference)) {
                    continue;
                }

                healthPerSecond += definition.regeneration()
                        .map(RegenerationProperties::healthPerSecond)
                        .map(value -> value * com.github.littleemptydoll.exoequipment.exoskeleton.TemperatureOperations.calculateModuleEfficiency(definition, data.temperature()))
                        .orElse(0.0D);
            }
        }

        return healthPerSecond;
    }
}
