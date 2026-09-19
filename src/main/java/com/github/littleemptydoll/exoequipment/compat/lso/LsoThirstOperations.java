package com.github.littleemptydoll.exoequipment.compat.lso;

import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonData;
import com.github.littleemptydoll.exoequipment.exoskeleton.ExoskeletonState;
import com.github.littleemptydoll.exoequipment.frame.FrameOperations;
import com.github.littleemptydoll.exoequipment.module.InstalledModule;
import com.github.littleemptydoll.exoequipment.module.InstalledModuleReference;
import com.github.littleemptydoll.exoequipment.module.ThirstProperties;
import com.github.littleemptydoll.exoequipment.registry.ModModules;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class LsoThirstOperations {
    private LsoThirstOperations() {
    }

    public static double calculateExhaustionReduction(
            ExoskeletonData data,
            Set<InstalledModuleReference> poweredModules
    ) {
        double multiplier = 1.0D;

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

                InstalledModuleReference reference =
                        new InstalledModuleReference(slot, moduleIndex);

                if (!isPowered(module.id(), reference, poweredModules)) {
                    continue;
                }

                ThirstProperties properties =
                        ModModules.getDefinition(module.id())
                                .thirst()
                                .orElse(null);

                if (properties == null) {
                    continue;
                }

                multiplier *= 1.0D - properties.exhaustionReduction();

                if (multiplier <= 0.0D) {
                    return 1.0D;
                }
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
                Math.min(Float.MAX_VALUE, reduced)
        );
    }

    private static boolean isPowered(
            ResourceLocation moduleId,
            InstalledModuleReference reference,
            Set<InstalledModuleReference> poweredModules
    ) {
        var energy = ModModules.getDefinition(moduleId).energy();

        return energy.isEmpty()
                || energy.get().consumption() <= 0
                || poweredModules.contains(reference);
    }
}
