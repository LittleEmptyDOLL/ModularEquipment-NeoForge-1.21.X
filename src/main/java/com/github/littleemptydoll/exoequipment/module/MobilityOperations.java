package com.github.littleemptydoll.exoequipment.module;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.registry.ModModules;

import java.util.Set;

public final class MobilityOperations {
    private MobilityOperations() {}

    public static MobilityValues calculate(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double movementSpeed = 0.0D;
        double swimSpeed = 0.0D;
        double jumpStrength = 0.0D;
        double stepHeight = 0.0D;

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

                if (poweredModules != null
                        && definition.energy()
                        .filter(energy -> energy.consumption() > 0)
                        .isPresent()
                        && !poweredModules.contains(
                                new InstalledModuleReference(slot, moduleIndex)
                        )) {
                    continue;
                }

                MobilityProperties properties =
                        definition.mobility().orElse(null);

                if (properties == null) {
                    continue;
                }

                movementSpeed += properties.movementSpeed();
                swimSpeed += properties.swimSpeed();
                jumpStrength += properties.jumpStrength();
                stepHeight += properties.stepHeight();
            }
        }

        return new MobilityValues(
                movementSpeed,
                swimSpeed,
                jumpStrength,
                stepHeight
        );
    }

    public record MobilityValues(
            double movementSpeed,
            double swimSpeed,
            double jumpStrength,
            double stepHeight
    ) {
        public MobilityValues {
            if (!Double.isFinite(movementSpeed)
                    || !Double.isFinite(swimSpeed)
                    || !Double.isFinite(jumpStrength)
                    || !Double.isFinite(stepHeight)) {
                throw new IllegalArgumentException(
                        "Mobility values must be finite"
                );
            }
        }
    }
}
