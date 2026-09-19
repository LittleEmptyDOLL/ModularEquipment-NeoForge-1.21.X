package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class FallProtectionOperations {
    private FallProtectionOperations() {}

    public static double applyProtection(
            double damage,
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        if (damage <= 0.0D) {
            return 0.0D;
        }

        double multiplier = 1.0D;

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

                var properties = definition.fallProtection().orElse(null);

                if (properties == null) {
                    continue;
                }

                multiplier *= 1.0D - properties.damageReduction();

                if (multiplier <= 0.0D) {
                    return 0.0D;
                }
            }
        }

        return damage * Math.max(0.0D, multiplier);
    }
}
