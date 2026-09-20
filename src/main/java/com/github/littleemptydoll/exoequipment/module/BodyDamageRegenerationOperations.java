package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.matrix.MatrixData;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class BodyDamageRegenerationOperations {
    private BodyDamageRegenerationOperations() {}

    public static double calculateHealthPerSecond(
            ExoskeletonData data,
            BodyPart bodyPart,
            Set<InstalledModuleReference> poweredModules
    ) {
        double healthPerSecond = 0.0D;

        for (int slot : ExoskeletonState.activeMatrixSlots(data)) {
            MatrixData matrix = data.matrices().get(slot).matrix().orElse(null);

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

                if (definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(reference)) {
                    continue;
                }

                BodyDamageRegenerationProperties properties =
                        definition.bodyDamageRegeneration().orElse(null);

                if (properties == null
                        || !BodyPart.applies(properties.bodyParts(), bodyPart)) {
                    continue;
                }

                healthPerSecond += properties.healthPerSecond();
            }
        }

        return Math.max(0.0D, healthPerSecond);
    }
}
