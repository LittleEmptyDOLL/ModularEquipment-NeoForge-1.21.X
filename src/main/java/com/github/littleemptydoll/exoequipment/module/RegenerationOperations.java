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
                        .orElse(0.0D);
            }
        }

        return healthPerSecond;
    }
}
